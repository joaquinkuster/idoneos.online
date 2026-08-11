package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.EmailService;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller para la gestión del Foro de Consultas por Unidad (CU-23 a CU-26).
 */
@Controller
@RequestMapping("/foro")
public class ForoController {

    @Autowired private ConsultaForoRepository consultaRepo;
    @Autowired private RespuestaForoRepository respuestaRepo;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private DictadoDocenteRepository dictadoDocenteRepository;
    @Autowired private DocenteRepository docenteRepo;
    @Autowired private EmailService emailService;

    @GetMapping("/unidad/{unidadId}")
    public String verForoUnidad(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        List<ConsultaForo> consultas = consultaRepo.findByUnidadAndBajaFalseOrderByFechaDesc(unidad);

        model.addAttribute("usuario", usuario);
        model.addAttribute("unidad", unidad);
        model.addAttribute("curso", unidad.getCurso());
        model.addAttribute("consultas", consultas);
        model.addAttribute("titulo", "Foro — " + unidad.getTitulo() + " | Idóneos Online");
        return "pages/foro/foro-unidad";
    }

    @PostMapping("/unidad/{unidadId}/consulta")
    public String nuevaConsulta(@PathVariable Integer unidadId,
                                @RequestParam String texto,
                                Authentication auth,
                                RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/cursos";
        Usuario usuario = (Usuario) auth.getPrincipal();
        ConsultaForo consulta = consultaRepo.save(new ConsultaForo(texto, unidad, usuario));

        // CU-24: Notificar al docente titular del curso
        if (unidad.getCurso() != null) {
            dictadoDocenteRepository.findAll().stream()
                    .filter(dd -> dd.getDictado() != null && dd.getDictado().getPrograma() != null 
                            && dd.getDictado().getPrograma().getCurso().getId() == unidad.getCurso().getId()
                            && !dd.isEsSupervisor() && dd.getDocente() != null && dd.getDocente().getUsuario() != null)
                    .findFirst()
                    .ifPresent(dd -> emailService.enviarNuevaConsultaForo(
                            dd.getDocente().getUsuario().getCorreo(), consulta));
        }

        ra.addFlashAttribute("mensaje", "Consulta publicada en el foro.");
        return "redirect:/foro/unidad/" + unidadId;
    }

    @PostMapping("/consulta/{consultaId}/responder")
    public String responderConsulta(@PathVariable Integer consultaId,
                                    @RequestParam String texto,
                                    Authentication auth,
                                    RedirectAttributes ra) {
        ConsultaForo consulta = consultaRepo.findById(consultaId).orElse(null);
        if (consulta == null) return "redirect:/cursos";
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepo.findById(usuario.getId()).orElse(null);
        if (docente == null) {
            ra.addFlashAttribute("mensaje", "Solo los docentes pueden responder consultas.");
            return "redirect:/foro/unidad/" + consulta.getUnidad().getId();
        }

        RespuestaForo respuesta = respuestaRepo.save(new RespuestaForo(texto, consulta, docente));

        // Notificar al alumno autor de la consulta
        if (consulta.getUsuario() != null) {
            emailService.enviarRespuestaForo(consulta.getUsuario().getCorreo(), respuesta);
        }

        ra.addFlashAttribute("mensaje", "Respuesta enviada.");
        return "redirect:/foro/unidad/" + consulta.getUnidad().getId();
    }
}
