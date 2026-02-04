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

    boolean existsByStatus(StatusReuniao status);


    @Transactional(readOnly = true)
    @Query("""
      select r
      from Reuniao r
      where (
            exists (select 1 from r.participantes p where p.id = :profId)
            or r.colegiado.coordenador.id = :profId
            or exists (
                select 1
                from r.processos pr
                where pr.relator.id = :profId
            )
      )
      and (:status is null or r.status = :status)
    """)
    Page<Reuniao> findVisiveisParaProfessor(
            @Param("profId") Long profId,
            @Param("status") StatusReuniao status,
            Pageable pageable
    );

}


