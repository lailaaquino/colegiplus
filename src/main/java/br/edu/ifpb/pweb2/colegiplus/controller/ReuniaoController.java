package br.edu.ifpb.pweb2.colegiplus.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.StatusProcesso;
import br.edu.ifpb.pweb2.colegiplus.model.StatusReuniao;
import br.edu.ifpb.pweb2.colegiplus.repository.ColegiadoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProcessoRepository;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reunioes")
public class ReuniaoController {

    @Autowired
    private ProcessoRepository processoRepository;
    @Autowired
    private ColegiadoRepository colegiadoRepository;
    @Autowired
    private br.edu.ifpb.pweb2.colegiplus.repository.ReuniaoRepository reuniaoRepository;

    @GetMapping("/nova")
    public ModelAndView formNovaSessao(HttpSession session) {
        Professor coordenador = (Professor) session.getAttribute("usuario");
        Colegiado colegiado = colegiadoRepository.findByCoordenador(coordenador);

        ModelAndView mv = new ModelAndView("reunioes/form");
        mv.addObject("membros", colegiado.getMembros());
        mv.addObject("processos", processoRepository.findByStatus(StatusProcesso.DISTRIBUIDO));
        mv.addObject("reuniao", new Reuniao());
        return mv;
    }

    @GetMapping
    public ModelAndView listar(@RequestParam(value = "status", required = false) String status, HttpSession session) {
        ModelAndView mv = new ModelAndView("reunioes/list");
        Professor professor = (Professor) session.getAttribute("usuario");

        if (professor == null) {
            return new ModelAndView("redirect:/auth");
        }

        Colegiado colegiado = colegiadoRepository.findByCoordenador(professor);
        if (colegiado == null) {
            List<Colegiado> todos = colegiadoRepository.findAll();
            for (Colegiado c : todos) {
                if (c.getMembros() != null && c.getMembros().stream().anyMatch(m -> m.getId().equals(professor.getId()))) {
                    colegiado = c;
                    break;
                }
            }
        }

        if (colegiado == null) {
            mv.addObject("reunioes", new ArrayList<Reuniao>());
            return mv;
        }

        List<Reuniao> reunioes;
        StatusReuniao statusEnum = null;

        if (status != null && !status.isBlank() && !status.equals("null")) {
            try {
                statusEnum = StatusReuniao.valueOf(status);
                reunioes = reuniaoRepository.findByColegiadoAndStatus(colegiado, statusEnum);
            } catch (IllegalArgumentException e) {
                reunioes = reuniaoRepository.findByColegiado(colegiado);
            }
        } else {
            reunioes = reuniaoRepository.findByColegiado(colegiado);
        }

        mv.addObject("reunioes", reunioes);
        mv.addObject("statusSelecionado", statusEnum);
        return mv;
    }

    @PostMapping
    public String salvarSessao(Reuniao reuniao, @RequestParam(required = false) List<Long> processosIds, HttpSession session) {
        Professor coordenador = (Professor) session.getAttribute("usuario");
        Colegiado colegiado = colegiadoRepository.findByCoordenador(coordenador);

        reuniao.setColegiado(colegiado);
        reuniao.setStatus(StatusReuniao.PROGRAMADA); 

        if (processosIds != null) {
            reuniao.setProcessos(processoRepository.findAllById(processosIds));
        }

        reuniaoRepository.save(reuniao);
        return "redirect:/reunioes";
    }
}
