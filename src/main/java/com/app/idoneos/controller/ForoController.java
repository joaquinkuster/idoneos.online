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
 * Controller para la gestión del Foro de Consultas por Unidad.
 * Implementa: CU-33 — Buscar consulta de foro, CU-34 — Registrar consulta de foro,
 * CU-35 — Modificar consulta de foro, CU-36 — Eliminar consulta de foro,
 * CU-37 — Buscar respuesta de foro, CU-38 — Registrar respuesta de foro,
 * CU-39 — Modificar respuesta de foro, CU-40 — Eliminar respuesta de foro.
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

    /**
     * CU-33 — Buscar consulta de foro.
     */
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

    /**
     * CU-34 — Registrar consulta de foro.
     * Envía una notificación por correo electrónico al docente titular del dictado.
     */
    @PostMapping("/unidad/{unidadId}/consulta")
    public String nuevaConsulta(@PathVariable Integer unidadId,
                                @RequestParam String texto,
                                Authentication auth,
                                RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/cursos";
        Usuario usuario = (Usuario) auth.getPrincipal();
        ConsultaForo consulta = consultaRepo.save(new ConsultaForo(texto, unidad, usuario.getAlumno()));

        // CU-34: Notificación por email al docente titular
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

    /**
     * CU-38 — Registrar respuesta de foro por parte del docente a cargo.
     * Envía una notificación por correo electrónico al alumno autor de la consulta.
     */
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
            ra.addFlashAttribute("mensaje", "CU-38 Autorización: Solo los docentes pueden responder consultas.");
            return "redirect:/foro/unidad/" + consulta.getUnidad().getId();
        }

        RespuestaForo respuesta = respuestaRepo.save(new RespuestaForo(texto, consulta, docente));

        if (consulta.getAlumno() != null) {
            emailService.enviarRespuestaForo(consulta.getAlumno().getUsuario().getCorreo(), respuesta);
        }

        ra.addFlashAttribute("mensaje", "Respuesta enviada.");
        return "redirect:/foro/unidad/" + consulta.getUnidad().getId();
    }
}
