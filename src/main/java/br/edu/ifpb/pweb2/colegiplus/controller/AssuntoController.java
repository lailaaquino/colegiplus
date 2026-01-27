package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.colegiplus.model.Assunto;
import br.edu.ifpb.pweb2.colegiplus.model.NavPage;
import br.edu.ifpb.pweb2.colegiplus.model.NavPageBuilder;
import br.edu.ifpb.pweb2.colegiplus.service.AssuntoService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/assuntos")
public class AssuntoController {

    @Autowired
    private AssuntoService assuntoService;

    @GetMapping
    public ModelAndView listar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size,
            ModelAndView mv,
            HttpSession session
    ) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"ADMIN".equals(tipo)) {
            return new ModelAndView("redirect:/");
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Assunto> assuntos = assuntoService.findAll(pageable);

        NavPage navPage = NavPageBuilder.newNavPage(
                assuntos.getNumber() + 1,
                assuntos.getTotalElements(),
                assuntos.getTotalPages(),
                size
        );

        mv.setViewName("assuntos/list");
        mv.addObject("assuntos", assuntos);
        mv.addObject("navPage", navPage);
        mv.addObject("resourcePath", "assuntos");
        return mv;
    }


    @GetMapping("/form")
    public ModelAndView getForm(ModelAndView mv, HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"ADMIN".equals(tipo)) {
            return new ModelAndView("redirect:/assuntos");
        }
        mv.setViewName("assuntos/form");
        mv.addObject("assunto", new Assunto());
        return mv;
    }

    @PostMapping
    public ModelAndView save(Assunto assunto, ModelAndView mv, RedirectAttributes attr, HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"ADMIN".equals(tipo)) {
            return new ModelAndView("redirect:/assuntos");
        }
        assuntoService.save(assunto);
        attr.addFlashAttribute("mensagem", "Assunto salvo com sucesso!");
        mv.setViewName("redirect:/assuntos");
        return mv;
    }

    @GetMapping("/{id}")
    public ModelAndView editar(@PathVariable("id") Long id, ModelAndView mv, HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"ADMIN".equals(tipo)) {
            return new ModelAndView("redirect:/assuntos");
        }
        mv.setViewName("assuntos/form");
        mv.addObject("assunto", assuntoService.findById(id));
        return mv;
    }

    @GetMapping("/{id}/delete")
    public ModelAndView delete(@PathVariable Long id, ModelAndView mv, RedirectAttributes attr, HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");

        if (!"ADMIN".equals(tipo)) {
            attr.addFlashAttribute("erro", "Você não tem permissão para realizar esta operação.");
            mv.setViewName("redirect:/assuntos");
            return mv;
        }

        try {
            assuntoService.deleteById(id);

            attr.addFlashAttribute("mensagem", "Assunto removido com sucesso!");

        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("erro",
                    "Erro ao excluir: Este assunto está associado a processos e não pode ser removido.");

        } catch (Exception e) {
            attr.addFlashAttribute("erro", "Erro inesperado ao tentar remover o assunto.");
        }

        mv.setViewName("redirect:/assuntos");
        return mv;
    }
}
