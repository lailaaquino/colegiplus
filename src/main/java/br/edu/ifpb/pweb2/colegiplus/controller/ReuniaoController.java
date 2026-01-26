package br.edu.ifpb.pweb2.colegiplus.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.StatusProcesso;
import br.edu.ifpb.pweb2.colegiplus.model.StatusReuniao;
import br.edu.ifpb.pweb2.colegiplus.repository.ColegiadoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProcessoRepository;
import br.edu.ifpb.pweb2.colegiplus.service.ReuniaoService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reunioes")
public class ReuniaoController {

    @Autowired
    private ReuniaoService reuniaoService;
    @Autowired
    private ProcessoRepository processoRepository;
    @Autowired
    private ColegiadoRepository colegiadoRepository;

    @GetMapping("/nova")
    public ModelAndView formNovaSessao(HttpSession session) {
        Professor coordenador = (Professor) session.getAttribute("usuario");
        String tipo = (String) session.getAttribute("tipoUsuario");

        if (!"COORDENADOR".equals(tipo) || coordenador == null) {
            return new ModelAndView("redirect:/");
        }

        Colegiado colegiado = colegiadoRepository.findByCoordenador(coordenador);
        if (colegiado == null) {
            ModelAndView mv = new ModelAndView("redirect:/");
            mv.addObject("mensagem", "Você não está vinculado a nenhum colegiado. Solicite ao administrador.");
            return mv;
        }

        ModelAndView mv = new ModelAndView("reunioes/form");
        mv.addObject("reuniao", new Reuniao());
        List<Processo> processos = processoRepository.findByStatus(StatusProcesso.DISTRIBUIDO);
        mv.addObject("processos", processos != null ? processos : java.util.Collections.emptyList());
        List<Professor> membros = colegiado.getMembros() != null ? colegiado.getMembros() : java.util.Collections.emptyList();
        mv.addObject("membros", membros);
        mv.addObject("colegiado", colegiado);
        return mv;
    }

    @GetMapping
    public ModelAndView listar(@RequestParam(value = "status", required = false) String statusStr, HttpSession session) {
        ModelAndView mv = new ModelAndView("reunioes/list");
        Professor professor = (Professor) session.getAttribute("usuario");

        Colegiado colegiado = null;

        colegiado = colegiadoRepository.findByCoordenador(professor);

        if (colegiado == null && professor.getColegiadosMembro() != null && !professor.getColegiadosMembro().isEmpty()) {
            colegiado = professor.getColegiadosMembro().get(0);
        }

        if (colegiado == null) {
            mv.addObject("reunioes", java.util.Collections.emptyList());
            return mv;
        }

        StatusReuniao status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = StatusReuniao.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                status = null;
            }
        }

        List<Reuniao> reunioes = (status == null)
                ? reuniaoService.listarPorColegiado(colegiado)
                : reuniaoService.listarPorColegiadoEStatus(colegiado, status);

        mv.addObject("reunioes", (reunioes != null) ? reunioes : java.util.Collections.emptyList());
        mv.addObject("statusSelecionado", status);
        return mv;
    }

    @PostMapping
    public String salvarSessao(Reuniao reuniao, @RequestParam(required = false) List<Long> processosIds, HttpSession session) {
        Professor coordenador = (Professor) session.getAttribute("usuario");
        Colegiado colegiado = colegiadoRepository.findByCoordenador(coordenador);

        reuniao.setColegiado(colegiado);
        if (processosIds != null) {
            reuniao.setProcessos(processoRepository.findAllById(processosIds));
        }

        reuniaoService.agendarSessao(reuniao);
        return "redirect:/reunioes";
    }
}
