package br.edu.ifpb.pweb2.colegiplus.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.repository.AlunoRepository;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class AlunoService {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Aluno> findAll() {
        return alunoRepository.findAll();
    }

    public Page<Aluno> findAll(Pageable pageable) {
        return alunoRepository.findAll(pageable);
    }

    public Aluno findById(Long id) {
        return alunoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Aluno save(Aluno aluno) {
        String role = "ALUNO";
        String username = aluno.getUser().getUsername();
        String senhaFinal;

        if (aluno.getId() != null) {
            Aluno antigo = alunoRepository.findById(aluno.getId()).get();
            senhaFinal = (aluno.getUser().getPassword() == null || aluno.getUser().getPassword().isBlank())
                    ? antigo.getUser().getPassword()
                    : passwordEncoder.encode(aluno.getUser().getPassword());
        } else {
            senhaFinal = passwordEncoder.encode(aluno.getUser().getPassword());
        }

        UserDetails userSecurity = User
                .withUsername(username)
                .password(senhaFinal)
                .roles(role)
                .build();

        if (userDetailsManager.userExists(username)) {
            userDetailsManager.updateUser(userSecurity);
        } else {
            userDetailsManager.createUser(userSecurity);
        }

        aluno.getUser().setPassword(senhaFinal);
        aluno.getUser().setEnabled(true);
        return alunoRepository.save(aluno);
    }

    @Transactional
    public void deleteById(Long id) {
        Aluno aluno = alunoRepository.findById(id).orElse(null);
        if (aluno != null) {
            String username = aluno.getUser().getUsername();

            alunoRepository.delete(aluno);

            alunoRepository.flush();

            if (userDetailsManager.userExists(username)) {
                userDetailsManager.deleteUser(username);
            }
        }
    }

    public boolean existsByMatricula(String matricula) {
        return alunoRepository.existsByMatricula(matricula);
    }

    public boolean existsByLogin(String login) {
        return alunoRepository.existsByUserUsername(login);
    }

    public boolean existsByMatriculaAndIdNot(String matricula, Long id) {
        return alunoRepository.existsByMatriculaAndIdNot(matricula, id);
    }

    public boolean existsByLoginAndIdNot(String login, Long id) {
        return alunoRepository.existsByUserUsernameAndIdNot(login, id);
    }
}