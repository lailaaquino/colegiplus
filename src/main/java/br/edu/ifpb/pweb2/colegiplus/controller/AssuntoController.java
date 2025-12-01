package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.colegiplus.model.Assunto;
import br.edu.ifpb.pweb2.colegiplus.service.AssuntoService;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/assuntos")
public class AssuntoController {

    @Autowired
    private AssuntoService assuntoService;


    @GetMapping
    public ModelAndView listar(ModelAndView mv, HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"ADMIN".equals(tipo)) {
            return new ModelAndView("redirect:/");
        }
        mv.setViewName("assuntos/list");
        mv.addObject("assuntos", assuntoService.findAll());
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
            return new ModelAndView("redirect:/assuntos");
        }
        assuntoService.deleteById(id);
        attr.addFlashAttribute("mensagem", "Assunto removido com sucesso!");
        mv.setViewName("redirect:/assuntos");
        return mv;
        }
}
