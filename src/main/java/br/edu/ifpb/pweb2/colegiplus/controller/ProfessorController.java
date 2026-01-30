package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.colegiplus.model.NavPage;
import br.edu.ifpb.pweb2.colegiplus.model.NavPageBuilder;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.service.ProfessorService;
import jakarta.validation.Valid;
import br.edu.ifpb.pweb2.colegiplus.model.User;

@Controller
@RequestMapping("/professores")
public class ProfessorController {
    
    @Autowired
    private ProfessorService professorService;

    @GetMapping({"", "/"})
    public ModelAndView listarProfessores(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size,
            ModelAndView mv
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Professor> professores = professorService.findAll(pageable);

        NavPage navPage = NavPageBuilder.newNavPage(
                professores.getNumber() + 1,
                professores.getTotalElements(),
                professores.getTotalPages(),
                size
        );

        mv.setViewName("professores/list");
        mv.addObject("professores", professores);
        mv.addObject("navPage", navPage);
        mv.addObject("resourcePath", "professores");
        return mv;
    }

    @GetMapping({"/form", "/{id}/edit"})
    public ModelAndView mostrarFormulario(@PathVariable(required = false) Long id, ModelAndView modelAndView) {
        Professor professor;
        if (id == null) {
            professor = new Professor(); 
            professor.setUser(new User());
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
                                      ModelAndView modelAndView, RedirectAttributes attr) {
        
        if (result.hasErrors()) {
            modelAndView.setViewName("professores/form");
            return modelAndView;
        }
        String username = professor.getUser().getUsername();

        
        if (professor.getId() == null) { 
            if (professorService.existsByMatricula(professor.getMatricula())) {
                result.rejectValue("matricula", "matricula.exists", "Esta matrícula já está cadastrada.");
            }
            if (professorService.existsByLogin(username)) {
                result.rejectValue("user.username", "login.exists", "Este login já está em uso.");
            }
        } else { 
             if (professorService.existsByMatriculaAndIdNot(professor.getMatricula(), professor.getId())) {
                 result.rejectValue("matricula", "matricula.exists", "Esta matrícula já está cadastrada para outro professor.");
             }
             if (professorService.existsByLoginAndIdNot(username, professor.getId())) {
                 result.rejectValue("user.username", "login.exists", "Este login já está em uso por outro professor.");
             }
        }

        if (result.hasErrors()) {
            modelAndView.setViewName("professores/form");
            return modelAndView;
        }
        
        professorService.save(professor);
        attr.addFlashAttribute("mensagem", "Professor salvo com sucesso!");
        modelAndView.setViewName("redirect:/professores");
        return modelAndView;
    }
    
    @GetMapping("/{id}/delete")
    public ModelAndView delete(@PathVariable Long id, ModelAndView mv, RedirectAttributes attr) {
        try {
            professorService.deleteById(id);
            attr.addFlashAttribute("mensagem", "Professor removido com sucesso!");
        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("erro", "Erro ao excluir: Este professor possui vínculos (processos, colegiados).");
        } catch (Exception e) {
            attr.addFlashAttribute("erro", "Erro inesperado ao tentar remover o professor.");
        }
        
        mv.setViewName("redirect:/professores");
        return mv;
    }
}