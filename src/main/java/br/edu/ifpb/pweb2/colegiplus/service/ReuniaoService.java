package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.StatusProcesso;
import br.edu.ifpb.pweb2.colegiplus.model.StatusReuniao;
import br.edu.ifpb.pweb2.colegiplus.model.TipoDecisao;
import br.edu.ifpb.pweb2.colegiplus.model.TipoVoto;
import br.edu.ifpb.pweb2.colegiplus.model.Voto;
import br.edu.ifpb.pweb2.colegiplus.repository.ProcessoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ReuniaoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.VotoRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ReuniaoService {

    @Autowired
    private ReuniaoRepository reuniaoRepository;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private VotoRepository votoRepository;

    @Transactional
    public void agendarSessao(Reuniao reuniao) {
        reuniao.setStatus(StatusReuniao.PROGRAMADA);
        reuniaoRepository.save(reuniao);

        if (reuniao.getProcessos() != null) {
            for (Processo p : reuniao.getProcessos()) {
                p.setStatus(StatusProcesso.EM_PAUTA);
                processoRepository.save(p);
            }
        }
    }
    
    @Transactional(readOnly = true)
    public Page<Reuniao> listarReunioesDoProfessor(Long professorId, StatusReuniao status, Pageable pageable) {
        return reuniaoRepository.findVisiveisParaProfessor(professorId, status, pageable);
    }

    @Transactional
    public void iniciarSessao(Long idReuniao) {
        if (reuniaoRepository.existsByStatus(StatusReuniao.EM_JULGAMENTO)) {
            throw new IllegalStateException("Não é possível iniciar: já existe uma sessão em julgamento.");
        }

        Reuniao reuniao = reuniaoRepository.findById(idReuniao)
                .orElseThrow(() -> new IllegalArgumentException("Reunião não encontrada"));
        
        reuniao.setStatus(StatusReuniao.EM_JULGAMENTO);
        reuniaoRepository.save(reuniao);
    }

    public TipoDecisao apregoarECalcularResultado(Long reuniaoId, Long processoId, HttpServletRequest request) {
        Reuniao reuniao = reuniaoRepository.findById(reuniaoId)
                .orElseThrow(() -> new RuntimeException("Reunião não encontrada."));

        if (reuniao.getStatus() != StatusReuniao.EM_JULGAMENTO) {
            throw new RuntimeException("Reunião não está em julgamento.");
        }

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado."));

        if (processo.getRelator() == null) {
            throw new RuntimeException("Processo sem relator.");
        }
        if (processo.getDecisaoRelator() == null) {
            throw new RuntimeException("Relator ainda não definiu decisão.");
        }

        Long relatorId = processo.getRelator().getId();
        List<Professor> participantes = (reuniao.getParticipantes() == null) ? List.of() : reuniao.getParticipantes();

        long qtdComRelator = 0;
        long qtdDivergente = 0;

        for (Professor m : participantes) {
            if (m.getId().equals(relatorId)) continue;

            String marcacao = request.getParameter("marcacao_" + m.getId());
            if (marcacao == null || marcacao.isBlank()) {
                throw new RuntimeException("Falta indicar a marcação do membro: " + m.getNome());
            }

            Voto voto = votoRepository
                    .findByReuniaoIdAndProcessoIdAndProfessorId(reuniaoId, processoId, m.getId())
                    .orElseGet(() -> {
                        Voto v = new Voto();
                        v.setReuniao(reuniao);
                        v.setProcesso(processo);
                        v.setProfessor(m);
                        return v;
                    });

            if ("AUSENTE".equalsIgnoreCase(marcacao)) {
                voto.setAusente(true);
                voto.setTipoVoto(null);
            } else {
                voto.setAusente(false);
                voto.setTipoVoto(TipoVoto.valueOf(marcacao));

                if (voto.getTipoVoto() == TipoVoto.COM_RELATOR) qtdComRelator++;
                if (voto.getTipoVoto() == TipoVoto.DIVERGENTE) qtdDivergente++;
            }

            votoRepository.save(voto);
        }

        if (qtdComRelator == 0 && qtdDivergente == 0) {
            throw new RuntimeException("Não há votos computáveis (todos ausentes?).");
        }

        TipoDecisao decisaoRelator = processo.getDecisaoRelator();

        TipoDecisao resultado;
        if (qtdComRelator >= qtdDivergente) {
            resultado = decisaoRelator;
        } else {
            resultado = (decisaoRelator == TipoDecisao.DEFERIMENTO)
                    ? TipoDecisao.INDEFERIMENTO
                    : TipoDecisao.DEFERIMENTO;
        }

        processo.setResultadoColegiado(resultado);
        processoRepository.save(processo);

        return resultado;
    }

}
