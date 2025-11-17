package br.edu.ifpb.pweb2.colegiplus.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.model.StatusProcesso;
import br.edu.ifpb.pweb2.colegiplus.repository.ProcessoRepository;
import jakarta.transaction.Transactional;

@Component
public class ProcessoService implements Service<Processo, Long> {

    @Autowired
    private ProcessoRepository processoRepository;

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
        return processoRepository.save(p);
    }


    @Transactional
    public Processo saveForAluno(Processo p, Aluno alunoInteressado) {
        if (p.getId() == null) {
            p.setNumero(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            p.setInteressado(alunoInteressado);
            p.setStatus(StatusProcesso.CRIADO);
            p.setDataRecepcao(Date.from(Instant.now()));
        }
        return processoRepository.save(p);
    }

    public List<Processo> listarProcessosDoProfessor(Professor professor) {
        return processoRepository.findByRelator(professor);
    }

    public List<Processo> listarProcessosDoCoordenador(Professor professor) {
        return processoRepository.findAll();
    }


    public List<Processo> filtrarProcessosDoAluno(
            Aluno aluno,
            String status,
            Long assuntoId,
            String ordem) {

        List<Processo> processos = processoRepository.findByInteressado(aluno);

        if (status != null && !status.isEmpty()) {
            processos = processos.stream()
                    .filter(p -> p.getStatus() != null
                            && p.getStatus().name().equalsIgnoreCase(status))
                    .toList();
        }

        if (assuntoId != null) {
            processos = processos.stream()
                    .filter(p -> p.getAssunto() != null
                            && p.getAssunto().getId().equals(assuntoId))
                    .toList();
        }

        if ("desc".equalsIgnoreCase(ordem)) {
            processos = processos.stream()
                    .sorted((a, b) -> {
                        Date da = a.getDataRecepcao();
                        Date db = b.getDataRecepcao();
                        if (da == null && db == null) return 0;
                        if (da == null) return 1;
                        if (db == null) return -1;
                        return db.compareTo(da); 
                    })
                    .toList();
        } else {
            processos = processos.stream()
                    .sorted((a, b) -> {
                        Date da = a.getDataRecepcao();
                        Date db = b.getDataRecepcao();
                        if (da == null && db == null) return 0;
                        if (da == null) return 1;
                        if (db == null) return -1;
                        return da.compareTo(db);
                    })
                    .toList();
        }

        return processos;
    }
}
