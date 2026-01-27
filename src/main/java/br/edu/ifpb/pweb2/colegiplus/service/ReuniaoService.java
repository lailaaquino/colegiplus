package br.edu.ifpb.pweb2.colegiplus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.StatusProcesso;
import br.edu.ifpb.pweb2.colegiplus.model.StatusReuniao;
import br.edu.ifpb.pweb2.colegiplus.repository.ProcessoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ReuniaoRepository;

@Service
public class ReuniaoService {

    @Autowired
    private ReuniaoRepository reuniaoRepository;

    @Autowired
    private ProcessoRepository processoRepository;

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
}
