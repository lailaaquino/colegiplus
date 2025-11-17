package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.repository.AlunoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @GetMapping
    public ModelAndView getForm(ModelAndView model) {
        model.setViewName("auth/login");
        return model;
    }

    @PostMapping
    public ModelAndView valide(
            @RequestParam String login,
            @RequestParam String senha,
            HttpSession session,
            ModelAndView model,
            RedirectAttributes redirectAttts) {

        Aluno aluno = isAlunoValido(login, senha);
        if (aluno != null) {
            session.setAttribute("usuario", aluno);
            session.setAttribute("tipoUsuario", "ALUNO");
            model.setViewName("redirect:/home");
            return model;
        }

        Professor professor = isProfessorValido(login, senha);
        if (professor != null) {
            session.setAttribute("usuario", professor);
            if (professor.isCoordenador()) {
                session.setAttribute("tipoUsuario", "COORDENADOR");
            } else {
                session.setAttribute("tipoUsuario", "PROFESSOR");
            }
            model.setViewName("redirect:/home");
            return model;
        }

        redirectAttts.addFlashAttribute("mensagem", "Login e/ou senha inválidos!");
        model.setViewName("redirect:/auth");
        return model;
    }


    @GetMapping("/logout")
    public ModelAndView logout(ModelAndView mav, HttpSession session) {
        session.invalidate();
        mav.setViewName("redirect:/auth");
        return mav;
    }

    private Aluno isAlunoValido(String login, String senha) {
        Aluno alunoBD = alunoRepository.findByLogin(login);
        if (alunoBD != null && alunoBD.getSenha().equals(senha)) {
            return alunoBD;
        }
        return null;
    }

    private Professor isProfessorValido(String login, String senha) {
        Professor profBD = professorRepository.findByLogin(login);
        if (profBD != null && profBD.getSenha().equals(senha)) {
            return profBD;
        }
        return null;
    }
}
