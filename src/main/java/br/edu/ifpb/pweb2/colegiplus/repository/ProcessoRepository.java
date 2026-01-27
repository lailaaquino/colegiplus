package br.edu.ifpb.pweb2.colegiplus.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.model.StatusProcesso;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {

    @Transactional(readOnly = true)
    Page<Processo> findByInteressado(Aluno interessado, Pageable pageable);

    @Transactional(readOnly = true)
    Page<Processo> findByRelator(Professor relator, Pageable pageable);

    @Transactional(readOnly = true)
    Page<Processo> findByInteressadoAndStatus(
            Aluno interessado,
            StatusProcesso status,
            Pageable pageable
    );

    @Transactional(readOnly = true)
    Page<Processo> findByInteressadoAndAssunto_Id(
            Aluno interessado,
            Long assuntoId,
            Pageable pageable
    );

    @Transactional(readOnly = true)
    Page<Processo> findByInteressadoAndStatusAndAssunto_Id(
            Aluno interessado,
            StatusProcesso status,
            Long assuntoId,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM Processo p
        WHERE (:status IS NULL OR p.status = :status)
        ORDER BY p.dataRecepcao DESC
        """)
        Page<Processo> filtrarCoordenadorSomenteStatus(
        @Param("status") StatusProcesso status,
        Pageable pageable
        );

        @Query("""
        SELECT p
        FROM Processo p
        WHERE (:status IS NULL OR p.status = :status)
        AND LOWER(p.interessado.nome) LIKE LOWER(CONCAT('%', :nomeAluno, '%'))
        ORDER BY p.dataRecepcao DESC
        """)
        Page<Processo> filtrarCoordenadorStatusENomeAluno(
        @Param("status") StatusProcesso status,
        @Param("nomeAluno") String nomeAluno,
        Pageable pageable
        );

        @Query("""
        SELECT p
        FROM Processo p
        WHERE (:status IS NULL OR p.status = :status)
        AND p.relator IS NOT NULL
        AND LOWER(p.relator.nome) LIKE LOWER(CONCAT('%', :nomeProfessor, '%'))
        ORDER BY p.dataRecepcao DESC
        """)
        Page<Processo> filtrarCoordenadorStatusENomeProfessor(
        @Param("status") StatusProcesso status,
        @Param("nomeProfessor") String nomeProfessor,
        Pageable pageable
        );

        @Query("""
        SELECT p
        FROM Processo p
        WHERE (:status IS NULL OR p.status = :status)
        AND LOWER(p.interessado.nome) LIKE LOWER(CONCAT('%', :nomeAluno, '%'))
        AND p.relator IS NOT NULL
        AND LOWER(p.relator.nome) LIKE LOWER(CONCAT('%', :nomeProfessor, '%'))
        ORDER BY p.dataRecepcao DESC
        """)
        Page<Processo> filtrarCoordenadorCompleto(
        @Param("status") StatusProcesso status,
        @Param("nomeAluno") String nomeAluno,
        @Param("nomeProfessor") String nomeProfessor,
        Pageable pageable
        );

    @Transactional(readOnly = true)
    List<Processo> findByStatus(StatusProcesso status);
}
