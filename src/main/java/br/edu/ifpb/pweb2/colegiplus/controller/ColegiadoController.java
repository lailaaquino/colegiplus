package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
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

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.NavPage;
import br.edu.ifpb.pweb2.colegiplus.model.NavPageBuilder;
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
    public ModelAndView listColegiados(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int size,
            ModelAndView mv,
            HttpSession session
    ) {
        if (!isPermitidoGerenciar(session)) {
            return new ModelAndView("redirect:/home");
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").ascending());
        Page<Colegiado> colegiados = colegiadoService.findAll(pageable);

        NavPage navPage = NavPageBuilder.newNavPage(
                colegiados.getNumber() + 1,
                colegiados.getTotalElements(),
                colegiados.getTotalPages(),
                size
        );

        mv.setViewName("colegiados/list");
        mv.addObject("colegiados", colegiados);
        mv.addObject("navPage", navPage);
        mv.addObject("resourcePath", "colegiados");
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