package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;
import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Component;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.StatusProcesso;
import br.edu.ifpb.pweb2.colegiplus.repository.ProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.transaction.Transactional;
import java.util.UUID;
import br.edu.ifpb.pweb2.colegiplus.repository.AlunoRepository;

@Component
public class ProcessoService implements Service <Processo, Long> {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    public List<Processo> findAll() {
        return processoRepository.findAll();
    }

    @Override
    public Processo findById(Long id) {
        return processoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Processo save(Processo p) {
        if (p.getId() == null) { 
            
            p.setNumero(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            
            // Como ainda não existe cadastro de aluno, pegamos o primeiro cadastrado no banco.
            Aluno alunoMock = alunoRepository.findAll().get(0); 
            p.setInteressado(alunoMock);
            
            p.setStatus(StatusProcesso.CRIADO); 
            p.setDataRecepcao(Date.from(Instant.now()));

        }
        return processoRepository.save(p);
    }

}
