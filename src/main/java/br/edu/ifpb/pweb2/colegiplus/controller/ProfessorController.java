package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.service.ProfessorService;

@Controller
@RequestMapping("/professores")
public class ProfessorController {
    
    @Autowired
    private ProfessorService professorService;
    
    private boolean isPermitidoGerenciar(HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        return "ADMIN".equals(tipo); 
    }

    @GetMapping({"", "/"})
    public ModelAndView listarProfessores(ModelAndView modelAndView, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home"); 
        }
        modelAndView.addObject("professores", professorService.findAll());
        modelAndView.setViewName("professores/list");
        return modelAndView;
    }
    
    @GetMapping({"/form", "/{id}/edit"})
    public ModelAndView mostrarFormulario(@PathVariable(required = false) Long id, ModelAndView modelAndView, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        
        Professor professor;
        if (id == null) {
            professor = new Professor(); 
        } else {
            professor = professorService.findById(id);
            if (professor == null) {
                return new ModelAndView("redirect:/professores");
            }
        }
        
        modelAndView.addObject("professor", professor);
        modelAndView.setViewName("professores/form");
        return modelAndView;
    }

    @PostMapping("/form")
    public ModelAndView salvarProfessor(@Valid Professor professor, BindingResult result, 
                                      ModelAndView modelAndView, RedirectAttributes attr, 
                                      HttpSession session) {
        
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        
        if (result.hasErrors()) {
            modelAndView.setViewName("professores/form");
            return modelAndView;
        }

        if (professor.getId() == null) { 
            if (professorService.existsByMatricula(professor.getMatricula())) {
                result.rejectValue("matricula", "matricula.exists", "Esta matrícula já está cadastrada.");
            }
            if (professorService.existsByLogin(professor.getLogin())) {
                result.rejectValue("login", "login.exists", "Este login já está em uso.");
            }
        } else { 
             if (professorService.existsByMatriculaAndIdNot(professor.getMatricula(), professor.getId())) {
                 result.rejectValue("matricula", "matricula.exists", "Esta matrícula já está cadastrada para outro professor.");
             }
             if (professorService.existsByLoginAndIdNot(professor.getLogin(), professor.getId())) {
                 result.rejectValue("login", "login.exists", "Este login já está em uso por outro professor.");
             }
        }

        if (result.hasErrors()) {
            modelAndView.setViewName("professores/form");
            return modelAndView;
        }
        
        professorService.save(professor);
        attr.addFlashAttribute("mensagem", "Professor salvo com sucesso!");
        modelAndView.setViewName("redirect:/professores/");
        return modelAndView;
    }
    
    
    @GetMapping("/{id}/delete")
    public ModelAndView deletarProfessor(@PathVariable("id") Long id, ModelAndView modelAndView, RedirectAttributes attr, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        
        professorService.deleteById(id);
        attr.addFlashAttribute("mensagem", "Professor excluído com sucesso!");
        modelAndView.setViewName("redirect:/professores/");
        return modelAndView;
    }
}