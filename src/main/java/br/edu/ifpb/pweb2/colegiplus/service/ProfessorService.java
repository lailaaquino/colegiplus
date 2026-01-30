package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;

@Service
public class ProfessorService {

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Professor> findAll() {
        return professorRepository.findAll();
    }

    public Page<Professor> findAll(Pageable pageable) {
        return professorRepository.findAll(pageable);
    }

    public Professor findById(Long id) {
        return professorRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(Professor professor) {

        String role = professor.isCoordenador() ? "COORDENADOR" : "PROFESSOR";
        String senhaFinal;

        String username = professor.getUser().getUsername();

        if (professor.getId() != null) {
            Professor antigo = professorRepository.findById(professor.getId()).get();

            senhaFinal = (professor.getUser().getPassword() == null || professor.getUser().getPassword().isBlank())
                    ? antigo.getUser().getPassword()
                    : passwordEncoder.encode(professor.getUser().getPassword());
        } else {
            senhaFinal = passwordEncoder.encode(professor.getUser().getPassword());
        }

        UserDetails userSecurity = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(senhaFinal)
                .roles(role)
                .build();

        if (userDetailsManager.userExists(username)) {
            userDetailsManager.updateUser(userSecurity);
        } else {
            userDetailsManager.createUser(userSecurity);
        }

        professor.getUser().setPassword(senhaFinal);
        professor.getUser().setEnabled(true);
        professorRepository.save(professor);
    }

    @Transactional
    public void deleteById(Long id) {
        Professor prof = professorRepository.findById(id).orElse(null);
        if (prof != null) {
            String username = (prof.getUser() != null) ? prof.getUser().getUsername() : null;

            professorRepository.delete(prof);

            professorRepository.flush();

            if (username != null && userDetailsManager.userExists(username)) {

                userDetailsManager.deleteUser(username);
            }
        }
    }

    public boolean existsByMatricula(String matricula) {
        return professorRepository.existsByMatricula(matricula);
    }

    public boolean existsByLogin(String login) {

        return professorRepository.findByUserUsername(login) != null;
    }

    public boolean existsByMatriculaAndIdNot(String matricula, Long id) {
        return professorRepository.existsByMatriculaAndIdNot(matricula, id);
    }

    public boolean existsByLoginAndIdNot(String login, Long id) {

        return professorRepository.existsByUserUsernameAndIdNot(login, id);
    }
}