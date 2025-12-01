package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.repository.ColegiadoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;

@Service
public class ColegiadoService {

    @Autowired
    private ColegiadoRepository colegiadoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Transactional
    public void save(Colegiado colegiado) {
        colegiadoRepository.save(colegiado);
    }

    public List<Colegiado> findAll() {
        return colegiadoRepository.findAll();
    }

    @Transactional
    public Colegiado findById(Long id) {
        return colegiadoRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteById(Long id) {
        colegiadoRepository.deleteById(id);
    }

    public List<Professor> findAllProfessores() {
        return professorRepository.findAll();
    }
}