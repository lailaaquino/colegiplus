package br.edu.ifpb.pweb2.colegiplus.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.NavPage;
import br.edu.ifpb.pweb2.colegiplus.model.NavPageBuilder;
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

        List<Colegiado> colegiados = colegiadoRepository.findAllByCoordenador(coordenador);
        if (colegiados == null || colegiados.isEmpty()) {
            ModelAndView mv = new ModelAndView("redirect:/reunioes");
            session.setAttribute("mensagem", "Você não está como coordenador de nenhum colegiado.");
            return mv;
        }
        List<Professor> membros = colegiados.stream()
                .filter(c -> c.getMembros() != null)
                .flatMap(c -> c.getMembros().stream())
                .distinct()
                .toList();

        ModelAndView mv = new ModelAndView("reunioes/form");
        mv.addObject("membros", membros);
        mv.addObject("processos", processoRepository.findByStatus(StatusProcesso.DISTRIBUIDO));
        mv.addObject("reuniao", new Reuniao());
        return mv;
    }


    @GetMapping
    public ModelAndView listar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(value = "status", required = false) String status,
            HttpSession session
    ) {
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

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("data").descending());

        Page<Reuniao> reunioes = reuniaoService.listarReunioesDoProfessor(
                professor.getId(),
                statusEnum,
                pageable
        );

        NavPage navPage = NavPageBuilder.newNavPage(
                reunioes.getNumber() + 1,
                reunioes.getTotalElements(),
                reunioes.getTotalPages(),
                size
        );

        mv.addObject("reunioes", reunioes);
        mv.addObject("statusSelecionado", statusEnum);

        mv.addObject("navPage", navPage);

        String resourcePath = "reunioes" + (statusEnum != null ? "?status=" + statusEnum.name() : "");
        mv.addObject("resourcePath", resourcePath);

        return mv;
    }


    @PostMapping
    public String salvarSessao(
            Reuniao reuniao,
            @RequestParam(required = false) List<Long> processosIds,
            @RequestParam(required = false) List<Long> participantesIds,
            HttpSession session,
            RedirectAttributes attr
    ) {
        Professor coordenador = (Professor) session.getAttribute("usuario");
        if (coordenador == null) return "redirect:/auth";

        if (processosIds == null || processosIds.isEmpty() || participantesIds == null || participantesIds.isEmpty()) { 
            attr.addFlashAttribute("mensagemErro", "Selecione ao menos um processo e um participante para agendar a reunião.");
            return "redirect:/reunioes/nova";
        }

        List<Colegiado> colegiados = colegiadoRepository.findAllByCoordenador(coordenador);
        if (colegiados == null || colegiados.isEmpty()) {
            return "redirect:/reunioes";
        }

        Colegiado colegiado = colegiados.get(0);
        reuniao.setColegiado(colegiado);
        reuniao.setStatus(StatusReuniao.PROGRAMADA);

        reuniao.setProcessos(processoRepository.findAllById(processosIds));
        reuniao.setParticipantes(professorRepository.findAllById(participantesIds));

        List<Long> membrosIds = (colegiado.getMembros() == null) ? List.of() : colegiado.getMembros().stream().map(Professor::getId).toList();
        boolean temFora = reuniao.getParticipantes().stream().anyMatch(p -> !membrosIds.contains(p.getId()));
        if (temFora) {
            throw new IllegalArgumentException("Participante não pertence ao colegiado.");
        }

        reuniaoRepository.save(reuniao);
        return "redirect:/reunioes";
    }

    @PostMapping("/{id}/iniciar")
    public String iniciarSessao(@PathVariable("id") Long id, RedirectAttributes attr) {
        try {
            if (id == null){
                throw new IllegalArgumentException("ID da reunião não pode ser nulo.");
            }
            reuniaoService.iniciarSessao(id);
            attr.addFlashAttribute("mensagem", "Sessão de julgamento iniciada com sucesso!");
        } catch (IllegalStateException e) {
            attr.addFlashAttribute("mensagemErro", e.getMessage());
        } catch (Exception e) {
            attr.addFlashAttribute("mensagemErro", "Erro ao iniciar sessão: " + e.getMessage());
        }
        return "redirect:/reunioes";
    }
}
