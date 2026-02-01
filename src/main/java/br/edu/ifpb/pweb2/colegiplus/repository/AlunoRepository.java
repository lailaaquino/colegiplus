package br.edu.ifpb.pweb2.colegiplus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Aluno findByUserUsername(String username);

    boolean existsByMatricula(String matricula);

    boolean existsByUserUsername(String username);

    List<Aluno> findByNomeContainingIgnoreCase(String nome);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    boolean existsByUserUsernameAndIdNot(String username, Long id);
}