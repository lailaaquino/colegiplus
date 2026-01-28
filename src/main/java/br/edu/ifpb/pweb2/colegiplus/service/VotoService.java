package br.edu.ifpb.pweb2.colegiplus.service;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.StatusReuniao;
import br.edu.ifpb.pweb2.colegiplus.model.TipoDecisao;
import br.edu.ifpb.pweb2.colegiplus.model.TipoVoto;
import br.edu.ifpb.pweb2.colegiplus.model.Voto;
import br.edu.ifpb.pweb2.colegiplus.repository.ProcessoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ReuniaoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.VotoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VotoService {

    private final VotoRepository votoRepository;
    private final ReuniaoRepository reuniaoRepository;
    private final ProfessorRepository professorRepository;
    private final ProcessoRepository processoRepository;
    
    @Transactional(readOnly = true)
    public Map<Long, Voto> mapearVotosDoProfessorNaReuniao(Long professorId, Long reuniaoId) {
        Reuniao reuniao = reuniaoRepository.findById(reuniaoId)
            .orElseThrow(() -> new RuntimeException("Reunião não encontrada."));

        return reuniao.getVotos() == null ? Map.of() :
            reuniao.getVotos().stream()
                .filter(v -> v.getProfessor() != null && v.getProfessor().getId().equals(professorId))
                .collect(Collectors.toMap(v -> v.getProcesso().getId(), v -> v, (a, b) -> b));
    }

    @Transactional
    public void votar(
        Long reuniaoId,
        Long processoId,
        Long professorId,
        TipoDecisao decisao,
        String justificativa,
        MultipartFile parecerFile
    ) {
        Reuniao reuniao = reuniaoRepository.findById(reuniaoId)
            .orElseThrow(() -> new RuntimeException("Reunião não encontrada."));

        Processo processo = processoRepository.findById(processoId)
            .orElseThrow(() -> new RuntimeException("Processo não encontrado."));

        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new RuntimeException("Professor não encontrado."));

        boolean participa = reuniao.getParticipantes() != null &&
            reuniao.getParticipantes().stream().anyMatch(p -> p.getId().equals(professorId));
        if (!participa) throw new RuntimeException("Você não participa desta reunião.");

        boolean naPauta = reuniao.getProcessos() != null &&
            reuniao.getProcessos().stream().anyMatch(p -> p.getId().equals(processoId));
        if (!naPauta) throw new RuntimeException("Este processo não está na pauta da reunião.");

        boolean ehRelator = processo.getRelator() != null && processo.getRelator().getId().equals(professorId);
        if (ehRelator) { 

            try {
                processo.setDecisaoRelator(decisao);
                processo.setParecer(parecerFile.getBytes());
                processo.setDataParecer(Date.from(Instant.now()));
                processoRepository.save(processo);
                return;
            } catch (Exception e) {
                throw new RuntimeException("Falha ao ler o parecer enviado.");
            }
        }

        if (decisao == null) throw new RuntimeException("Informe seu voto (deferir/indeferir).");

        Voto voto = votoRepository.findByReuniaoIdAndProcessoIdAndProfessorId(reuniaoId, processoId, professorId)
            .orElseGet(Voto::new);

        voto.setReuniao(reuniao);
        voto.setProcesso(processo);
        voto.setProfessor(professor);
        voto.setDecisao(decisao);

        String just = (justificativa == null) ? null : justificativa.trim();
        voto.setJustificativa(just != null && !just.isBlank() ? just : null);

        votoRepository.save(voto);
    }

    private void validarPdf(MultipartFile f) {
        String ct = f.getContentType();
        if (ct == null || !ct.equalsIgnoreCase("application/pdf")) {
            throw new RuntimeException("Arquivo inválido. Envie um PDF.");
        }
    }

    public void marcarTipoVotoNaConducao(Long reuniaoId, Long processoId, Long professorId, String marcacao) {

        Reuniao reuniao = reuniaoRepository.findById(reuniaoId)
                .orElseThrow(() -> new RuntimeException("Reunião não encontrada."));

        if (reuniao.getStatus() != StatusReuniao.EM_JULGAMENTO) {
            throw new RuntimeException("A reunião não está em julgamento.");
        }

        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado."));

        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado."));

        if (processo.getRelator() != null && processo.getRelator().getId().equals(professorId)) {
            throw new RuntimeException("O relator não entra nessa marcação (ele vota pelo parecer).");
        }

        Voto voto = votoRepository
                .findByReuniaoIdAndProcessoIdAndProfessorId(reuniaoId, processoId, professorId)
                .orElseGet(() -> {
                    Voto v = new Voto();
                    v.setReuniao(reuniao);
                    v.setProcesso(processo);
                    v.setProfessor(professor);
                    v.setAusente(true); 
                    return v;
                });

        boolean professorVotou = voto.getDecisao() != null && Boolean.FALSE.equals(voto.getAusente());
        if ("AUSENTE".equalsIgnoreCase(marcacao) && professorVotou) {
            throw new RuntimeException("Não é possível marcar AUSENTE: o professor já votou.");
        }

        if ("AUSENTE".equalsIgnoreCase(marcacao)) {
            voto.setAusente(true);
            voto.setTipoVoto(null); 
        } else {
            voto.setAusente(false);
            voto.setTipoVoto(TipoVoto.valueOf(marcacao));
        }

        votoRepository.save(voto);
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

        List<Professor> membros = (reuniao.getParticipantes() == null) ? List.of() : reuniao.getParticipantes();
        Long relatorId = processo.getRelator().getId();

        for (Professor m : membros) {
            if (m.getId().equals(relatorId)) continue;

            String key = "marcacao_" + m.getId();
            String marcacao = request.getParameter(key);
            if (marcacao == null || marcacao.isBlank()) {
                throw new RuntimeException("Falta marcar: " + m.getNome());
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

            if ("AUSENTE".equals(marcacao)) {
                if (voto.getDecisao() != null && Boolean.FALSE.equals(voto.getAusente())) {
                    throw new RuntimeException("Não pode marcar AUSENTE: " + m.getNome() + " já votou.");
                }
                voto.setAusente(true);
                voto.setTipoVoto(null);
            } else {
                voto.setAusente(false);
                voto.setTipoVoto(TipoVoto.valueOf(marcacao));
            }

            votoRepository.save(voto);
        }

        List<Voto> votos = votoRepository.findByReuniaoIdAndProcessoId(reuniaoId, processoId);

        long defer = votos.stream()
                .filter(v -> Boolean.FALSE.equals(v.getAusente()))
                .filter(v -> v.getProfessor() != null && !v.getProfessor().getId().equals(relatorId))
                .filter(v -> v.getDecisao() == TipoDecisao.DEFERIMENTO)
                .count();

        long indefer = votos.stream()
                .filter(v -> Boolean.FALSE.equals(v.getAusente()))
                .filter(v -> v.getProfessor() != null && !v.getProfessor().getId().equals(relatorId))
                .filter(v -> v.getDecisao() == TipoDecisao.INDEFERIMENTO)
                .count();

        TipoDecisao resultado = (defer >= indefer) ? TipoDecisao.DEFERIMENTO : TipoDecisao.INDEFERIMENTO;

        processo.setResultadoColegiado(resultado);
        processoRepository.save(processo);

        return resultado;
    }
}