package br.edu.ifpb.pweb2.colegiplus.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.colegiplus.model.Voto;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    Optional<Voto> findByProfessorIdAndReuniaoIdAndProcessoId(
        Long professorId,
        Long reuniaoId,
        Long processoId
    );

    List<Voto> findByReuniaoIdAndProcessoId(Long reuniaoId, Long processoId);

    Optional<Voto> findByReuniaoIdAndProcessoIdAndProfessorId(Long reuniaoId, Long processoId, Long professorId);
}