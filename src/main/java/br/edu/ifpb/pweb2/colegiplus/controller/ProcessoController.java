package br.edu.ifpb.pweb2.colegiplus.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import br.edu.ifpb.pweb2.colegiplus.model.Aluno;
import br.edu.ifpb.pweb2.colegiplus.model.Colegiado;
import br.edu.ifpb.pweb2.colegiplus.model.Processo;
import br.edu.ifpb.pweb2.colegiplus.model.Professor;
import br.edu.ifpb.pweb2.colegiplus.repository.AssuntoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ColegiadoRepository;
import br.edu.ifpb.pweb2.colegiplus.repository.ProfessorRepository;
import br.edu.ifpb.pweb2.colegiplus.service.ProcessoService;
import jakarta.servlet.http.HttpSession;


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

    @GetMapping
    public ModelAndView listar(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long assuntoId,
        @RequestParam(required = false, defaultValue = "asc") String ordem,
        @RequestParam(required = false) String nomeAluno,
        @RequestParam(required = false) String nomeProfessor,
        HttpSession session) {

        ModelAndView mv = new ModelAndView("processos/list");
        String tipo = (String) session.getAttribute("tipoUsuario");
        Object usuario = session.getAttribute("usuario");

        List<Processo> processos = List.of();

        if ("ALUNO".equals(tipo)) {
            Aluno aluno = (Aluno) usuario;
            processos = processoService.filtrarProcessosDoAluno(aluno, status, assuntoId, ordem);

            mv.addObject("assuntos", assuntoRepository.findAll());
            mv.addObject("statusSelecionado", status);
            mv.addObject("assuntoSelecionado", assuntoId);
            mv.addObject("ordemSelecionada", ordem);
        }
        else if ("PROFESSOR".equals(tipo)) {
            Professor professor = (Professor) usuario;
            processos = processoService.listarProcessosDoProfessor(professor);
        }
        else if ("COORDENADOR".equals(tipo)) {
            Professor coord = (Professor) usuario;

            processos = processoService.filtrarProcessosDoCoordenador(status, nomeAluno, nomeProfessor);

            Colegiado colegiado = colegiadoRepository.findByCoordenador(coord);
            List<Professor> membros = (colegiado != null)
                    ? colegiado.getMembros()
                    : professorRepository.findAll();

            mv.addObject("membros", membros);
            mv.addObject("statusSelecionado", status);
            mv.addObject("nomeAluno", nomeAluno);
            mv.addObject("nomeProfessor", nomeProfessor);
        }

        mv.addObject("processos", processos);
        return mv;
    }


    @GetMapping("/novo")
    public ModelAndView formNovo(HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"ALUNO".equals(tipo)) {
            return new ModelAndView("redirect:/processos");
        }

        ModelAndView mv = new ModelAndView("processos/form");
        mv.addObject("processo", new Processo());
        mv.addObject("assuntos", assuntoRepository.findAll());
        return mv;
    }

    @PostMapping
    public String salvar(Processo processo, HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"ALUNO".equals(tipo)) {
            return "redirect:/processos";
        }
        Aluno aluno = (Aluno) session.getAttribute("usuario");
        processoService.saveForAluno(processo, aluno);
        return "redirect:/processos";
    }

    @GetMapping ("/{id}/distribuir")
    public ModelAndView formDistribuir(@PathVariable Long id, HttpSession session) {
        String tipo = (String) session.getAttribute("tipoUsuario");
        if (!"COORDENADOR".equals(tipo)) {
           return new ModelAndView("redirect:/processos");
        }


        Professor coord = (Professor) session.getAttribute("usuario");
        Processo processo = processoService.findById(id);

        Colegiado colegiado = colegiadoRepository.findByCoordenador(coord);
        List <Professor> membros = (colegiado !=null)
            ? colegiado.getMembros()
            : professorRepository.findAll();

        ModelAndView modelAndView = new ModelAndView("processos/distribuir");
        modelAndView.addObject("processo", processo);
        modelAndView.addObject("membros", membros);
        return modelAndView; 
    }

    @PostMapping("/{id}/distribuir")
    public String distribuir(
        @PathVariable Long id,
        @RequestParam("professorId") Long professorId,
        HttpSession session) {

    String tipo = (String) session.getAttribute("tipoUsuario");
    if (!"COORDENADOR".equals(tipo)) {
        return "redirect:/processos";
    }

    Professor relator = professorRepository.findById(professorId).orElse(null);
    if (relator != null) {
        processoService.distribuirProcesso(id, relator);
    }
    return "redirect:/processos";
        }  
    }
    
