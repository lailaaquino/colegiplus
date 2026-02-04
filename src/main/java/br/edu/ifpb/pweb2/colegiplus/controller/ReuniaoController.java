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
import org.springframework.security.core.Authentication;
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
    public ModelAndView formNovaSessao(Authentication authentication, RedirectAttributes attr) {
        Professor coordenador = professorRepository.findByUserUsername(authentication.getName());

        List<Colegiado> colegiados = colegiadoRepository.findAllByCoordenador(coordenador);
        if (colegiados == null || colegiados.isEmpty()) {
            attr.addFlashAttribute("mensagemErro", "Você não está como coordenador de nenhum colegiado.");
            return new ModelAndView("redirect:/reunioes");
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
            Authentication authentication) {
        ModelAndView mv = new ModelAndView("reunioes/list");
        Professor professor = professorRepository.findByUserUsername(authentication.getName());

        StatusReuniao statusEnum = null;
        if (status != null && !status.isBlank() && !"null".equals(status)) {
            try {
                statusEnum = StatusReuniao.valueOf(status);
            } catch (IllegalArgumentException ignored) {
            }
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("data").descending());
        Page<Reuniao> reunioes = reuniaoService.listarReunioesDoProfessor(professor.getId(), statusEnum, pageable);

        NavPage navPage = NavPageBuilder.newNavPage(reunioes.getNumber() + 1, reunioes.getTotalElements(),
                reunioes.getTotalPages(), size);

        mv.addObject("reunioes", reunioes);
        mv.addObject("statusSelecionado", statusEnum);
        mv.addObject("navPage", navPage);
        mv.addObject("resourcePath", "reunioes" + (statusEnum != null ? "?status=" + statusEnum.name() : ""));

        return mv;
    }

    @PostMapping
    public String salvarSessao(
            Reuniao reuniao,
            @RequestParam(required = false) List<Long> processosIds,
            @RequestParam(required = false) List<Long> participantesIds,
            Authentication authentication,
            RedirectAttributes attr) {
        Professor coordenador = professorRepository.findByUserUsername(authentication.getName());

        if (processosIds == null || processosIds.isEmpty() || participantesIds == null || participantesIds.isEmpty()) {
            attr.addFlashAttribute("mensagemErro", "Selecione ao menos um processo e um participante.");
            return "redirect:/reunioes/nova";
        }

        List<Colegiado> colegiados = colegiadoRepository.findAllByCoordenador(coordenador);
        if (colegiados == null || colegiados.isEmpty())
            return "redirect:/reunioes";

        List<Processo> processosSelecionados = processoRepository.findAllById(processosIds);

        processosSelecionados.forEach(p -> {
            p.setStatus(StatusProcesso.EM_PAUTA);
        });

        Colegiado colegiado = colegiados.get(0);
        reuniao.setColegiado(colegiado);
        reuniao.setStatus(StatusReuniao.PROGRAMADA);
        reuniao.setProcessos(processoRepository.findAllById(processosIds));

        List<Professor> participantes = professorRepository.findAllById(participantesIds);
        if (participantes.stream().noneMatch(p -> p.getId().equals(coordenador.getId()))) {
            participantes.add(coordenador);
        }
        reuniao.setParticipantes(participantes);

        reuniaoRepository.save(reuniao);
        return "redirect:/reunioes";
    }

    @PostMapping("/{id}/iniciar")
    public String iniciarSessao(@PathVariable("id") Long id, RedirectAttributes attr) {
        try {
            reuniaoService.iniciarSessao(id);
            attr.addFlashAttribute("mensagem", "Sessão de julgamento iniciada com sucesso!");
        } catch (Exception e) {
            attr.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/reunioes";
    }

    @GetMapping("/{id}")
    public ModelAndView detalhes(@PathVariable Long id, Authentication authentication) {
        Professor professor = professorRepository.findByUserUsername(authentication.getName());
        Reuniao reuniao = reuniaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reunião não encontrada."));

        boolean participa = reuniao.getParticipantes().stream().anyMatch(p -> p.getId().equals(professor.getId()));
        if (!participa)
            return new ModelAndView("redirect:/reunioes");

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
            Authentication authentication,
            RedirectAttributes ra) {
        Professor professor = professorRepository.findByUserUsername(authentication.getName());
        try {
            votoService.votar(reuniaoId, processoId, professor.getId(), decisao, justificativa, parecerFile);
            ra.addFlashAttribute("mensagem", "Voto registrado com sucesso.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/reunioes/" + reuniaoId;
    }

    @GetMapping("/{id}/conducao")
    public ModelAndView conduzir(@PathVariable Long id) {

        Reuniao reuniao = reuniaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reunião não encontrada."));

        if (reuniao.getStatus() != StatusReuniao.EM_JULGAMENTO &&
                reuniao.getStatus() != StatusReuniao.ENCERRADA) {
            return new ModelAndView("redirect:/reunioes");
        }

        List<Professor> participantes = (reuniao.getParticipantes() == null) ? List.of() : reuniao.getParticipantes();
        List<Processo> processos = (reuniao.getProcessos() == null) ? List.of() : reuniao.getProcessos();

        Map<Long, Map<Long, Voto>> votosPorProcesso = processos.stream()
                .collect(Collectors.toMap(
                        Processo::getId,
                        p -> votoRepository.findByReuniaoIdAndProcessoId(reuniao.getId(), p.getId()).stream()
                                .collect(Collectors.toMap(v -> v.getProfessor().getId(), Function.identity())),
                        (a, b) -> b));

        ModelAndView mv = new ModelAndView("reunioes/conducao");
        mv.addObject("reuniao", reuniao);
        mv.addObject("participantes", participantes);
        mv.addObject("votosPorProcesso", votosPorProcesso);
        return mv;
    }

    @PostMapping("/{reuniaoId}/processos/{processoId}/apregoar")
    public String apregoar(@PathVariable Long reuniaoId, @PathVariable Long processoId, HttpServletRequest request,
            RedirectAttributes ra) {
        try {
            TipoDecisao resultado = reuniaoService.apregoarECalcularResultado(reuniaoId, processoId, request);
            ra.addFlashAttribute("mensagem", "Resultado calculado: " + resultado.name());
        } catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/reunioes/" + reuniaoId + "/conducao";
    }

    @PostMapping("/{id}/finalizar") // Removido o /reunioes inicial
    public String finalizarSessao(@PathVariable Long id, RedirectAttributes attr) {
        Reuniao reuniao = reuniaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reunião não encontrada"));

        reuniao.setStatus(StatusReuniao.ENCERRADA);

        if (reuniao.getProcessos() != null) {
            reuniao.getProcessos().forEach(processo -> {
                processo.setStatus(StatusProcesso.JULGADO);
                processoRepository.save(processo);
            });
        }

        reuniaoRepository.save(reuniao);

        attr.addFlashAttribute("mensagem", "Sessão encerrada com sucesso! Os votos foram congelados.");

        // Ajuste o redirect para o nome correto do seu método @GetMapping (que é
        // /conducao)
        return "redirect:/reunioes/" + id + "/conducao";
    }

}