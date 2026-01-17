package br.edu.ifpb.pweb2.colegiplus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.Voto;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    List<Voto> findByProcessoAndReuniao(Processo processo, Reuniao reuniao);

    List<Voto> findByReuniao(Reuniao reuniao);

    boolean existsByProfessorAndProcessoAndReuniao(Professor professor, Processo processo, Reuniao reuniao);

    Voto findByProfessorAndProcessoAndReuniao(Professor professor, Processo processo, Reuniao reuniao);  
}
