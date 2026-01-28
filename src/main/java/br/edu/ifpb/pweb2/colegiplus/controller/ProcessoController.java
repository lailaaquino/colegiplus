package br.edu.ifpb.pweb2.colegiplus.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.NavPage;
import br.edu.ifpb.pweb2.colegiplus.model.NavPageBuilder;
import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.repository.AlunoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.AssuntoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ColegiadoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;
import br.edu.ifpb.pweb2.colegiplus.service.ProcessoService;

@Controller
@RequestMapping("/processos")
public class ProcessoController {

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private AssuntoRepository assuntoRepository;

    @Autowired
    private ColegiadoRepository colegiadoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @GetMapping
    public ModelAndView listar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assuntoId,
            @RequestParam(required = false, defaultValue = "asc") String ordem,
            @RequestParam(required = false) String nomeAluno,
            @RequestParam(required = false) String nomeProfessor,
            Authentication authentication
    ) {
        ModelAndView mv = new ModelAndView("processos/list");
        String login = authentication.getName();
        Page<Processo> processos = Page.empty();

        // Lógica de Aluno
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ALUNO"))) {
            Aluno aluno = alunoRepository.findByLogin(login);
            Pageable pageable = PageRequest.of(page - 1, size, 
                "desc".equalsIgnoreCase(ordem) ? Sort.by("dataRecepcao").descending() : Sort.by("dataRecepcao").ascending());
            
            processos = processoService.filtrarProcessosDoAluno(aluno, status, assuntoId, pageable);
            mv.addObject("assuntos", assuntoRepository.findAll());
        } 
        // Lógica de Professor/Coordenador
        else {
            Professor prof = professorRepository.findByLogin(login);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("dataRecepcao").descending());

            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDENADOR"))) {
                processos = processoService.filtrarProcessosDoCoordenador(status, nomeAluno, nomeProfessor, pageable);
                mv.addObject("statusSelecionado", status);
                mv.addObject("nomeAluno", nomeAluno);
                mv.addObject("nomeProfessor", nomeProfessor);
            } else {
                processos = processoService.listarProcessosDoProfessor(prof, pageable);
            }
        }

        NavPage navPage = NavPageBuilder.newNavPage(processos.getNumber() + 1, processos.getTotalElements(), processos.getTotalPages(), size);
        mv.addObject("processos", processos);
        mv.addObject("navPage", navPage);
        mv.addObject("resourcePath", "processos");
        mv.addObject("statusSelecionado", status);
        mv.addObject("assuntoSelecionado", assuntoId);
        mv.addObject("ordemSelecionada", ordem);

        return mv;
    }

    @GetMapping("/novo")
    public ModelAndView formNovo() {
        ModelAndView mv = new ModelAndView("processos/form");
        mv.addObject("processo", new Processo());
        mv.addObject("assuntos", assuntoRepository.findAll());
        return mv;
    }

    @PostMapping
    public String salvar(Processo processo, @RequestParam(value="requerimentoFile", required=false) MultipartFile requerimentoFile, Authentication authentication) {
        Aluno aluno = alunoRepository.findByLogin(authentication.getName());
        processoService.saveForAluno(processo, aluno, requerimentoFile);
        return "redirect:/processos";
    }

    @GetMapping("/{id}/distribuir")
    public ModelAndView formDistribuir(@PathVariable Long id, Authentication authentication) {
        Professor coord = professorRepository.findByLogin(authentication.getName());
        Processo processo = processoService.findById(id);
        List<Colegiado> colegiados = colegiadoRepository.findAllByCoordenador(coord);

        List<Professor> membros = colegiados.stream()
                .filter(c -> c.getMembros() != null)
                .flatMap(c -> c.getMembros().stream())
                .distinct().toList();

        if (membros.isEmpty()) { membros = professorRepository.findAll(); }

        ModelAndView mv = new ModelAndView("processos/distribuir");
        mv.addObject("processo", processo);
        mv.addObject("membros", membros);
        return mv;
    }

    @PostMapping("/{id}/distribuir")
    public String distribuir(@PathVariable Long id, @RequestParam("professorId") Long professorId) {
        Professor relator = professorRepository.findById(professorId).orElse(null);
        if (relator != null) {
            processoService.distribuirProcesso(id, relator);
        }
        return "redirect:/processos";
    }

    @GetMapping("/{id}/requerimento")
    public ResponseEntity<byte[]> baixarRequerimento(@PathVariable Long id) {
        Processo processo = processoService.findById(id);
        if (processo.getRequerimentoPdf() == null) { return ResponseEntity.notFound().build(); }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + processo.getRequerimentoNome() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(processo.getRequerimentoPdf());
    }
}