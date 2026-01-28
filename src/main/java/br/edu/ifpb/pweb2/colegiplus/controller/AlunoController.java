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

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.model.NavPage;
import br.edu.ifpb.pweb2.colegiplus.model.NavPageBuilder;
import br.edu.ifpb.pweb2.colegiplus.service.AlunoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/alunos")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @GetMapping({ "", "/" })
    public ModelAndView listarAlunos(
            ModelAndView model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size) {
        Pageable paging = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Aluno> pageAlunos = alunoService.findAll(paging);

        NavPage navPage = NavPageBuilder.newNavPage(
                pageAlunos.getNumber() + 1,
                pageAlunos.getTotalElements(),
                pageAlunos.getTotalPages(),
                size);

        model.addObject("alunos", pageAlunos);
        model.addObject("navPage", navPage);
        model.addObject("resourcePath", "alunos");
        model.setViewName("alunos/list");
        return model;
    }

    @GetMapping({ "/form", "/{id}/edit" })
    public ModelAndView mostrarFormulario(@PathVariable(required = false) Long id, ModelAndView modelAndView) {
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
            ModelAndView modelAndView, RedirectAttributes attr) {

        if (result.hasErrors()) {
            modelAndView.setViewName("alunos/form");
            return modelAndView;
        }

        // Validações de duplicidade
        if (aluno.getId() == null) {
            if (alunoService.existsByMatricula(aluno.getMatricula())) {
                result.rejectValue("matricula", "matricula.exists", "Esta matrícula já está cadastrada.");
            }
            if (alunoService.existsByLogin(aluno.getLogin())) {
                result.rejectValue("login", "login.exists", "Este login já está em uso.");
            }
        } else {
            if (alunoService.existsByMatriculaAndIdNot(aluno.getMatricula(), aluno.getId())) {
                result.rejectValue("matricula", "matricula.exists",
                        "Esta matrícula já está cadastrada para outro aluno.");
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
    public ModelAndView delete(@PathVariable Long id, ModelAndView mv, RedirectAttributes attr) {
        try {
            alunoService.deleteById(id);
            attr.addFlashAttribute("mensagem", "Aluno removido com sucesso!");
        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("erro", "Erro ao excluir: Este aluno possui processos vinculados.");
        } catch (Exception e) {
            attr.addFlashAttribute("erro", "Erro inesperado ao tentar remover o aluno.");
        }
        mv.setViewName("redirect:/alunos");
        return mv;
    }
}