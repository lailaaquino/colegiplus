package br.edu.ifpb.pweb2.colegiplus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.StatusReuniao;

@Repository
public interface  ReuniaoRepository extends JpaRepository<Reuniao, Long> {

    List<Reuniao> findByColegiado(Colegiado colegiado);

    List<Reuniao> findByColegiadoAndStatus(Colegiado colegiado, StatusReuniao status);

    boolean existsByColegiadoAndStatus(Colegiado colegiado, StatusReuniao status);
}


