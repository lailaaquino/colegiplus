package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping(value = "/login")
    public ModelAndView getForm(ModelAndView model) {
        model.setViewName("auth/login");
        return model;
    }
}


