package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;

@Service
public class ProfessorService {
    
    @Autowired
    private ProfessorRepository professorRepository;
    
    public List<Professor> findAll() {
        return professorRepository.findAll();
    }

    public Professor findById(Long id) {
        return professorRepository.findById(id).orElse(null);
    }

    @Transactional
    public Professor save(Professor professor) {
        return professorRepository.save(professor);
    }
    
    @Transactional
    public void deleteById(Long id) {
        professorRepository.deleteById(id);
    }
    
    public boolean existsByMatricula(String matricula) {
        return professorRepository.existsByMatricula(matricula);
    }
    
    public boolean existsByLogin(String login) {
        return professorRepository.findByLogin(login) != null;
    }
    
    public boolean existsByMatriculaAndIdNot(String matricula, Long id) {
        return professorRepository.existsByMatriculaAndIdNot(matricula, id);
    }
    
    public boolean existsByLoginAndIdNot(String login, Long id) {
        return professorRepository.existsByLoginAndIdNot(login, id);
    }
}