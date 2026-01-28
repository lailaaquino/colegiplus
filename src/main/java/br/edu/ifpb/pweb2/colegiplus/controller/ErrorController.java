package br.edu.ifpb.pweb2.colegiplus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "auth/acesso-negado";
    }
}

