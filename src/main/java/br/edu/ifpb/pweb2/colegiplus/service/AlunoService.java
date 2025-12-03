package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.repository.AlunoRepository;

@Service
public class AlunoService {
    
    @Autowired
    private AlunoRepository alunoRepository;
    

    public List<Aluno> findAll() {
        return alunoRepository.findAll();
    }

    public Aluno findById(Long id) {
        return alunoRepository.findById(id).orElse(null);
    }


    @Transactional
    public Aluno save(Aluno aluno) {

        return alunoRepository.save(aluno);
    }
    
   
    @Transactional
    public void deleteById(Long id) {
        alunoRepository.deleteById(id);
    }

    public boolean existsByMatricula(String matricula) {
        return alunoRepository.existsByMatricula(matricula);
    }
    
    public boolean existsByLogin(String login) {
        return alunoRepository.existsByLogin(login);
    }
    
    public boolean existsByMatriculaAndIdNot(String matricula, Long id) {
        return alunoRepository.existsByMatriculaAndIdNot(matricula, id);
    }
    
    public boolean existsByLoginAndIdNot(String login, Long id) {
        return alunoRepository.existsByLoginAndIdNot(login, id);
    }
}