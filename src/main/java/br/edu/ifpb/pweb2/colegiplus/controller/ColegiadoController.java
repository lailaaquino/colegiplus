package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.service.ColegiadoService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/colegiados") 
public class ColegiadoController {

    @Autowired
    private ColegiadoService colegiadoService;

    private boolean isPermitidoGerenciar(HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        return "ADMIN".equals(tipo); 
    }

    @GetMapping
    public ModelAndView listColegiados(ModelAndView mv, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        mv.addObject("colegiados", colegiadoService.findAll());
        mv.setViewName("colegiados/list");
        return mv;
    }

    @GetMapping("/form")
    public ModelAndView getForm(ModelAndView mv, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        mv.addObject("colegiado", new Colegiado());
        mv.addObject("todosProfessores", colegiadoService.findAllProfessores()); 
        mv.setViewName("colegiados/form");
        return mv;
    }

    @GetMapping("/{id}/edit")
    public ModelAndView editColegiado(@PathVariable("id") Long id, ModelAndView mv, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        Colegiado colegiado = colegiadoService.findById(id);
        mv.addObject("colegiado", colegiado);
        mv.addObject("todosProfessores", colegiadoService.findAllProfessores());
        mv.setViewName("colegiados/form");
        return mv;
    }

    @PostMapping
    public ModelAndView saveColegiado(Colegiado colegiado, RedirectAttributes attr, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        colegiadoService.save(colegiado);
        return new ModelAndView("redirect:/colegiados"); 
    }

    @GetMapping("/{id}/delete")
    public ModelAndView deleteColegiado(@PathVariable("id") Long id, RedirectAttributes attr, HttpSession session) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }
        colegiadoService.deleteById(id);
        return new ModelAndView("redirect:/colegiados");
    }
}