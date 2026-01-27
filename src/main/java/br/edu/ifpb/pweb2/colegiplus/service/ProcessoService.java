package br.edu.ifpb.pweb2.colegiplus.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<Processo> filtrarProcessosDoAluno(
            Aluno aluno,
            String status,
            Long assuntoId,
            Pageable pageable
    ) {
        if (status != null && !status.isBlank() && assuntoId != null) {
            StatusProcesso sp = StatusProcesso.valueOf(status.toUpperCase());
            return processoRepository.findByInteressadoAndStatusAndAssunto_Id(aluno, sp, assuntoId, pageable);
        }

        if (status != null && !status.isBlank()) {
            StatusProcesso sp = StatusProcesso.valueOf(status.toUpperCase());
            return processoRepository.findByInteressadoAndStatus(aluno, sp, pageable);
        }

        if (assuntoId != null) {
            return processoRepository.findByInteressadoAndAssunto_Id(aluno, assuntoId, pageable);
        }

        return processoRepository.findByInteressado(aluno, pageable);
    }

    public Page<Processo> filtrarProcessosDoCoordenador(
            String status,
            String nomeAluno,
            String nomeProfessor,
            Pageable pageable
    ) {
        String alunoLike = (nomeAluno == null || nomeAluno.isBlank()) ? null : nomeAluno.trim();
        String profLike = (nomeProfessor == null || nomeProfessor.isBlank()) ? null : nomeProfessor.trim();
        StatusProcesso sp = (status == null || status.isBlank()) ? null : StatusProcesso.valueOf(status.toUpperCase());

        return processoRepository.filtrarCoordenador(sp, alunoLike, profLike, pageable);
    }

    public Page<Processo> listarProcessosDoProfessor(Professor professor, Pageable pageable) {
        return processoRepository.findByRelator(professor, pageable);
    }

    @Transactional
    public void distribuirProcesso(Long processoId, Professor relator) {
        Processo p = this.findById(processoId);
        if (p == null) {
            throw new IllegalArgumentException("Processo não encontrado");
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
