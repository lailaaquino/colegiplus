package br.edu.ifpb.pweb2.colegiplus.controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.NavPage;
import br.edu.ifpb.pweb2.colegiplus.model.NavPageBuilder;
import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.model.Reuniao;
import br.edu.ifpb.pweb2.colegiplus.model.StatusProcesso;
import br.edu.ifpb.pweb2.colegiplus.model.StatusReuniao;
import br.edu.ifpb.pweb2.colegiplus.model.TipoDecisao;
import br.edu.ifpb.pweb2.colegiplus.model.Voto;
import br.edu.ifpb.pweb2.colegiplus.repository.ColegiadoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProcessoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ReuniaoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.VotoRepository;
import br.edu.ifpb.pweb2.colegiplus.service.ReuniaoService;
import br.edu.ifpb.pweb2.colegiplus.service.VotoService;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private VotoService votoService;

    @Autowired
    private VotoRepository votoRepository;

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

        List<Professor> participantes = professorRepository.findAllById(participantesIds);
        boolean jaTem = participantes.stream().anyMatch(p -> p.getId().equals(coordenador.getId()));
        if (!jaTem) participantes.add(coordenador);

        reuniao.setParticipantes(participantes);

        List<Long> permitidos = new java.util.ArrayList<>();
        if (colegiado.getMembros() != null) {
            permitidos.addAll(colegiado.getMembros().stream().map(Professor::getId).toList());
        }
        if (colegiado.getCoordenador() != null) {
            permitidos.add(colegiado.getCoordenador().getId());
        }

        boolean temFora = reuniao.getParticipantes().stream()
                .anyMatch(p -> !permitidos.contains(p.getId()));

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

    @GetMapping("/{id}")
    public ModelAndView detalhes(@PathVariable Long id, HttpSession session) {
        Professor professor = (Professor) session.getAttribute("usuario");
        if (professor == null) return new ModelAndView("redirect:/auth");

        Reuniao reuniao = reuniaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reunião não encontrada."));

        boolean participa = reuniao.getParticipantes() != null &&
            reuniao.getParticipantes().stream().anyMatch(p -> p.getId().equals(professor.getId()));

        if (!participa) return new ModelAndView("redirect:/reunioes");

        ModelAndView mv = new ModelAndView("reunioes/detalhes");
        mv.addObject("reuniao", reuniao);
        mv.addObject("professorLogado", professor);

        mv.addObject("meusVotos", votoService.mapearVotosDoProfessorNaReuniao(professor.getId(), reuniao.getId()));
        mv.addObject("podeVotar", reuniao.getStatus() == StatusReuniao.EM_JULGAMENTO);

        return mv;
    }

    @PostMapping("/{reuniaoId}/processos/{processoId}/votar")
    public String votar(
        @PathVariable Long reuniaoId,
        @PathVariable Long processoId,
        @RequestParam TipoDecisao decisao,
        @RequestParam(required = false) String justificativa,
        @RequestParam(required = false) MultipartFile parecerFile,
        HttpSession session,
        RedirectAttributes ra
    ) {
        Professor professor = (Professor) session.getAttribute("usuario");
        if (professor == null) return "redirect:/auth";

        try {
            votoService.votar(reuniaoId, processoId, professor.getId(), decisao, justificativa, parecerFile);
            ra.addFlashAttribute("mensagem", "Voto registrado com sucesso.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }

        return "redirect:/reunioes/" + reuniaoId;
    }

    @PostMapping("/{reuniaoId}/processos/{processoId}/membros/{professorId}/marcar")
    public String marcarTipoVoto(
            @PathVariable Long reuniaoId,
            @PathVariable Long processoId,
            @PathVariable Long professorId,
            @RequestParam("marcacao") String marcacao,
            HttpSession session,
            RedirectAttributes ra
    ) {
        if (session.getAttribute("tipoUsuario") == null
                || !"COORDENADOR".equals(session.getAttribute("tipoUsuario").toString())) {
            return "redirect:/reunioes";
        }

        try {
            votoService.marcarTipoVotoNaConducao(reuniaoId, processoId, professorId, marcacao);
            ra.addFlashAttribute("mensagem", "Marcação atualizada.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }

        return "redirect:/reunioes/" + reuniaoId + "/conducao";
    }



    @GetMapping("/{id}/conducao")
    public ModelAndView conduzir(@PathVariable Long id, HttpSession session) {
        Professor usuario = (Professor) session.getAttribute("usuario");
        if (usuario == null) return new ModelAndView("redirect:/auth");

        Object tipoObj = session.getAttribute("tipoUsuario");
        String tipoUsuario = (tipoObj == null) ? "" : tipoObj.toString();
        if (!"COORDENADOR".equals(tipoUsuario)) return new ModelAndView("redirect:/reunioes");

        Reuniao reuniao = reuniaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reunião não encontrada."));

        if (reuniao.getStatus() != StatusReuniao.EM_JULGAMENTO) {
            return new ModelAndView("redirect:/reunioes");
        }

        List<Professor> participantes = (reuniao.getParticipantes() == null) ? List.of() : reuniao.getParticipantes();
        List<Processo> processos = (reuniao.getProcessos() == null) ? List.of() : reuniao.getProcessos();

        Map<Long, Map<Long, Voto>> votosPorProcesso = processos.stream()
                .collect(Collectors.toMap(
                        Processo::getId,
                        p -> votoRepository.findByReuniaoIdAndProcessoId(reuniao.getId(), p.getId()).stream()
                                .collect(Collectors.toMap(
                                        v -> v.getProfessor().getId(),
                                        Function.identity()
                                ))
                        , (a, b) -> b
                ));

        ModelAndView mv = new ModelAndView("reunioes/conducao");
        mv.addObject("reuniao", reuniao);
        mv.addObject("participantes", participantes);
        mv.addObject("votosPorProcesso", votosPorProcesso);
        return mv;
    }


    @PostMapping("/{reuniaoId}/processos/{processoId}/apregoar")
    public String apregoar(
            @PathVariable Long reuniaoId,
            @PathVariable Long processoId,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes ra
    ) {
        if (session.getAttribute("tipoUsuario") == null
                || !"COORDENADOR".equals(session.getAttribute("tipoUsuario").toString())) {
            return "redirect:/reunioes";
        }

        try {
            TipoDecisao resultado = reuniaoService.apregoarECalcularResultado(reuniaoId, processoId, request);
            ra.addFlashAttribute("mensagem", "Resultado calculado: " + resultado.name());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }

        return "redirect:/reunioes/" + reuniaoId + "/conducao";
    }

}
