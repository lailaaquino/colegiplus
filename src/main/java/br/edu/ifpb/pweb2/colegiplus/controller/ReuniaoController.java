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
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ReuniaoRepository;
import br.edu.ifpb.pweb2.colegiplus.service.ReuniaoService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/reunioes")
public class ReuniaoController {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private ColegiadoRepository colegiadoRepository;

    @Autowired
    private ReuniaoRepository reuniaoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private ReuniaoService reuniaoService;

    @GetMapping("/nova")
    public ModelAndView formNovaSessao(HttpSession session) {
        Professor coordenador = (Professor) session.getAttribute("usuario");
        if (coordenador == null) return new ModelAndView("redirect:/auth");

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
        if (professor == null) return new ModelAndView("redirect:/auth");

        StatusReuniao statusEnum = null;
        if (status != null && !status.isBlank() && !"null".equals(status)) {
            try {
                statusEnum = StatusReuniao.valueOf(status);
            } catch (IllegalArgumentException ignored) {
                statusEnum = null;
            }
        }

        List<Reuniao> reunioes = reuniaoService.listarReunioesDoProfessor(professor.getId(), statusEnum);

        mv.addObject("reunioes", reunioes != null ? reunioes : new ArrayList<Reuniao>());
        mv.addObject("statusSelecionado", statusEnum);
        return mv;
    }

    @PostMapping
    public String salvarSessao(Reuniao reuniao,
                              @RequestParam(required = false) List<Long> processosIds,
                              @RequestParam(required = false) List<Long> participantesIds,
                              HttpSession session) {
        Professor coordenador = (Professor) session.getAttribute("usuario");
        if (coordenador == null) return "redirect:/auth";

        Colegiado colegiado = colegiadoRepository.findByCoordenador(coordenador);

        reuniao.setColegiado(colegiado);
        reuniao.setStatus(StatusReuniao.PROGRAMADA);

        if (processosIds != null && !processosIds.isEmpty()) {
            reuniao.setProcessos(processoRepository.findAllById(processosIds));
        }

        if (participantesIds != null && !participantesIds.isEmpty()) {
            List<Professor> participantes = professorRepository.findAllById(participantesIds);

            List<Long> membrosIds = colegiado.getMembros().stream().map(Professor::getId).toList();
            boolean temFora = participantes.stream().anyMatch(p -> !membrosIds.contains(p.getId()));
            if (temFora) {
                throw new IllegalArgumentException("Participante não pertence ao colegiado.");
            }

            reuniao.setParticipantes(participantes);
        } else {
            reuniao.setParticipantes(colegiado.getMembros());
        }

        reuniaoRepository.save(reuniao);
        return "redirect:/reunioes";
    }
}
