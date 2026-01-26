package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
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

    public List<Reuniao> listarPorColegiado(Colegiado colegiado) {
        return reuniaoRepository.findByColegiado(colegiado);
    }

    public List<Reuniao> listarPorColegiadoEStatus(Colegiado colegiado, StatusReuniao status) {
        return reuniaoRepository.findByColegiadoAndStatus(colegiado, status);
    }
}
