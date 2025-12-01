package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.ifpb.pweb2.colegiplus.model.Assunto;
import br.edu.ifpb.pweb2.colegiplus.repository.AssuntoRepository;

@Component
public class AssuntoService implements Service<Assunto, Long>{

    @Autowired
    private AssuntoRepository assuntoRepository; 

     @Override
    public List<Assunto> findAll() {
        return assuntoRepository.findAll();
    }

    @Override
    public Assunto findById(Long id) {
        return assuntoRepository.findById(id).orElse(null);
    }

    @Override
    public Assunto save(Assunto assunto) {
        return assuntoRepository.save(assunto);
    }

    public void deleteById(Long id) {
        assuntoRepository.deleteById(id);
    }

    
}
