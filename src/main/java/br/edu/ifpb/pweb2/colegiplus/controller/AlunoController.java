package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.service.AlunoService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/alunos")
public class AlunoController {
    
    @Autowired
    private AlunoService alunoService;
    
    private boolean isPermitidoGerenciar(HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        return "ADMIN".equals(tipo); 
    }

    @GetMapping({"", "/"})
    public ModelAndView listarAlunos(ModelAndView modelAndView, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home"); 
        }
        modelAndView.addObject("alunos", alunoService.findAll());
        modelAndView.setViewName("alunos/list");
        return modelAndView;
    }
    
    @GetMapping({"/form", "/{id}/edit"})
    public ModelAndView mostrarFormulario(@PathVariable(required = false) Long id, ModelAndView modelAndView, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        Aluno aluno;
        if (id == null) {
            aluno = new Aluno(); 
        } else {
            aluno = alunoService.findById(id); 
            if (aluno == null) {
                return new ModelAndView("redirect:/alunos");
            }
        }
        modelAndView.addObject("aluno", aluno);
        modelAndView.setViewName("alunos/form");
        return modelAndView;
    }

    @PostMapping("/form")
    public ModelAndView salvarAluno(@Valid Aluno aluno, BindingResult result, 
                                      ModelAndView modelAndView, RedirectAttributes attr, 
                                      HttpSession session) {
        
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        
        if (result.hasErrors()) {
            modelAndView.setViewName("alunos/form");
            return modelAndView;
        }

        if (aluno.getId() == null) { 
            if (alunoService.existsByMatricula(aluno.getMatricula())) {
                result.rejectValue("matricula", "matricula.exists", "Esta matrícula já está cadastrada.");
            }
            if (alunoService.existsByLogin(aluno.getLogin())) {
                result.rejectValue("login", "login.exists", "Este login já está em uso.");
            }
        } else { 
             if (alunoService.existsByMatriculaAndIdNot(aluno.getMatricula(), aluno.getId())) {
                 result.rejectValue("matricula", "matricula.exists", "Esta matrícula já está cadastrada para outro aluno.");
             }
             if (alunoService.existsByLoginAndIdNot(aluno.getLogin(), aluno.getId())) {
                 result.rejectValue("login", "login.exists", "Este login já está em uso por outro aluno.");
             }
        }

        if (result.hasErrors()) {
            modelAndView.setViewName("alunos/form");
            return modelAndView;
        }
        
        alunoService.save(aluno);
        attr.addFlashAttribute("mensagem", "Aluno salvo com sucesso!");
        modelAndView.setViewName("redirect:/alunos/");
        return modelAndView;
    }
    
    @GetMapping("/{id}/delete")
    public ModelAndView deletarAluno(@PathVariable("id") Long id, ModelAndView modelAndView, RedirectAttributes attr, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        alunoService.deleteById(id);
        attr.addFlashAttribute("mensagem", "Aluno excluído com sucesso!");
        modelAndView.setViewName("redirect:/alunos/");
        return modelAndView;
    }
}