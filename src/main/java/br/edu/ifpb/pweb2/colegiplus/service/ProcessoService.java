package br.edu.ifpb.pweb2.colegiplus.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
    public Processo saveForAluno(Processo p, Aluno alunoInteressado, MultipartFile requerimentoFile) {

        boolean jaDistribuido =
            p.getDataDistribuicao() != null ||
            p.getRelator() != null ||
            (p.getStatus() != null && p.getStatus() != StatusProcesso.CRIADO);

        if (requerimentoFile != null && !requerimentoFile.isEmpty() && jaDistribuido) {
            throw new RuntimeException("Não é permitido enviar requerimento após a distribuição.");
        }

        if (p.getId() == null) {
            p.setNumero(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            p.setInteressado(alunoInteressado);
            p.setStatus(StatusProcesso.CRIADO);
            p.setDataRecepcao(Date.from(Instant.now()));
        }

        if (requerimentoFile != null && !requerimentoFile.isEmpty()) {
            validarPdf(requerimentoFile);

            try {
                p.setRequerimentoNome(requerimentoFile.getOriginalFilename());
                p.setRequerimentoContentType(requerimentoFile.getContentType());
                p.setRequerimentoPdf(requerimentoFile.getBytes());
            } catch (Exception e) {
                throw new RuntimeException("Falha ao ler o PDF enviado.");
            }
        }

        return processoRepository.save(p);
    }

    private void validarPdf(MultipartFile f) {
        String ct = f.getContentType();
        if (ct == null || !ct.equalsIgnoreCase("application/pdf")) {
            throw new RuntimeException("Arquivo inválido. Envie um PDF.");
        }
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

    public List<Processo> filtrarProcessosDoCoordenador(
            String status,
            String nomeAluno,
            String nomeProfessor) {

        List<Processo> processos = processoRepository.findAll();

        if (status != null && !status.isBlank()) {
            StatusProcesso sp = StatusProcesso.valueOf(status);
            processos = processos.stream()
                    .filter(p -> p.getStatus() == sp)
                    .toList();
        }

        if (nomeAluno != null && !nomeAluno.isBlank()) {
            processos = processos.stream()
                    .filter(p -> p.getInteressado() != null &&
                                p.getInteressado().getNome().toLowerCase().contains(nomeAluno.toLowerCase()))
                    .toList();
        }

        if (nomeProfessor != null && !nomeProfessor.isBlank()) {
            processos = processos.stream()
                    .filter(p -> p.getRelator() != null &&
                                p.getRelator().getNome().toLowerCase().contains(nomeProfessor.toLowerCase()))
                    .toList();
        }

        return processos;
    }


    public List<Processo> listarProcessosDoProfessor(Professor professor) {
        return processoRepository.findByRelator(professor);
    }

    @Transactional 
    public void distribuirProcesso(Long processoId, Professor relator) {
        Processo p = this.findById(processoId);
        if (p ==null) {
            throw new IllegalArgumentException ("Processo não encontrado");
        }

        p.setRelator(relator);
        p.setStatus(StatusProcesso.DISTRIBUIDO);
        p.setDataDistribuicao(Date.from(Instant.now()));

        processoRepository.save(p);
    }

    public List<Processo> findByStatus(StatusProcesso status) {
        return processoRepository.findByStatus(status);
    }
}
