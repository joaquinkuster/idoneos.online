package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.service.Curso.CursoServiceImpl;
import com.app.idoneos.service.Inscripcion.InscripcionServiceImpl;
import com.app.idoneos.service.Progreso.ProgresoServiceImpl;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller para el seguimiento del avance y progreso de cursada del alumno (CU-46: Buscar progreso).
 */
@Controller
@RequestMapping("/progreso")
public class ProgresoController {

    @Autowired private ProgresoServiceImpl progresoService;
    @Autowired private CursoServiceImpl cursoService;
    @Autowired private InscripcionServiceImpl inscripcionService;
    @Autowired private UnidadServiceImpl unidadService;

    /**
     * CU-46 — Marcar unidad temática como completada por el alumno inscripto.
     */
    @PostMapping("/completar")
    public String marcarUnidadCompletada(@RequestParam("cursoId") Integer cursoId,
                                          @RequestParam("unidadId") Integer unidadId,
                                          Authentication auth,
                                          RedirectAttributes redirectAttributes) {

        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) {
            return "redirect:/login";
        }

        Usuario alumno = (Usuario) auth.getPrincipal();

        Optional<Curso> cursoOpt = cursoService.buscarPorId(cursoId);
        Optional<Unidad> unidadOpt = unidadService.buscarPorId(unidadId);

        if (cursoOpt.isEmpty() || unidadOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "CU-46 Excepción: Curso o unidad no encontrados.");
            return "redirect:/cursos";
        }

        Curso curso = cursoOpt.get();
        Unidad unidad = unidadOpt.get();

        Optional<Inscripcion> inscripcionOpt = inscripcionService.obtenerPorAlumnoYCurso(alumno, curso);

        if (inscripcionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "CU-46 Excepción: No estás inscripto en este curso.");
            return "redirect:/cursos/" + cursoId;
        }

        progresoService.marcarCompletada(inscripcionOpt.get(), unidad);
        redirectAttributes.addFlashAttribute("mensaje", "¡Unidad marcada como completada!");

        return "redirect:/cursos/" + cursoId + "/mi-cursada";
    }
}
