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
 * Controlador para el registro de progreso del alumno por unidad.
 */
@Controller
@RequestMapping("/progreso")
public class ProgresoController {

    @Autowired
    private ProgresoServiceImpl progresoService;

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private InscripcionServiceImpl inscripcionService;

    @Autowired
    private UnidadServiceImpl unidadService;

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
            redirectAttributes.addFlashAttribute("mensaje", "Curso o unidad no encontrados.");
            return "redirect:/cursos";
        }

        Curso curso = cursoOpt.get();
        Unidad unidad = unidadOpt.get();

        Optional<Inscripcion> inscripcionOpt = inscripcionService.obtenerPorAlumnoYCurso(alumno, curso);

        if (inscripcionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "No estás inscripto en este curso.");
            return "redirect:/cursos/" + cursoId;
        }

        progresoService.marcarCompletada(inscripcionOpt.get(), unidad);
        redirectAttributes.addFlashAttribute("mensaje", "¡Unidad marcada como completada!");

        return "redirect:/cursos/" + cursoId + "/mi-cursada";
    }
}
