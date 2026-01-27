package br.edu.ifpb.pweb2.colegiplus.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.StatusReuniao;

@Repository
public interface  ReuniaoRepository extends JpaRepository<Reuniao, Long> {

    List<Reuniao> findByColegiado(Colegiado colegiado);

    List<Reuniao> findByColegiadoAndStatus(Colegiado colegiado, StatusReuniao status);

    boolean existsByColegiadoAndStatus(Colegiado colegiado, StatusReuniao status);

    @Query("""
    select distinct r
    from Reuniao r
    left join r.participantes p
    where p.id = :profId
        or r.colegiado.coordenador.id = :profId
    order by r.data asc
    """)
    List<Reuniao> findVisiveisParaProfessor(@Param("profId") Long profId);

    @Query("""
    select distinct r
    from Reuniao r
    left join r.participantes p
    where (p.id = :profId or r.colegiado.coordenador.id = :profId)
        and r.status = :status
    order by r.data asc
    """)
    List<Reuniao> findVisiveisParaProfessorAndStatus(@Param("profId") Long profId,
                                                    @Param("status") StatusReuniao status);


    @Transactional(readOnly = true)
    @Query("""
        SELECT r
        FROM Reuniao r
        WHERE
          (
            (r.colegiado.coordenador.id = :professorId)
            OR
            EXISTS (
              SELECT 1
              FROM Colegiado c
              JOIN c.membros m
              WHERE c = r.colegiado
                AND m.id = :professorId
            )
          )
          AND (:status IS NULL OR r.status = :status)
        ORDER BY r.data DESC
    """)
    Page<Reuniao> findVisiveisParaProfessor(
            @Param("professorId") Long professorId,
            @Param("status") StatusReuniao status,
            Pageable pageable
    );
}


