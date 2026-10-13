package com.app.idoneos.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;
import com.app.idoneos.servicio.Participante.ParticipanteServicioImpl;

/**
 * Controlador de la lista de participantes de un curso (MOD-F-02).
 *
 * Mapea las pantallas de CU-25 Ver participantes (GET /participante/buscar, para el docente y el administrador) y de
 * la sección de participantes de CU-27 Acceder curso (GET /participante/ver/{idInscripcion}, para el alumno).
 */
@Controller
@RequestMapping("/participante")
public class ParticipanteControlador {

    @Autowired
    private ParticipanteServicioImpl participanteServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private InscripcionServicioImpl inscripcionServicio;

    /**
     * CU-25: Lista el equipo docente y los alumnos de todas las cohortes de un curso. Los filtros (nombre o apellido,
     * rol y cohorte) se aplican en la pantalla.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de participantes, o una redirección a la lista de cursos si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarParticipantes(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        boolean esAdministrador = usuario.esAdministradorActivo();
        try {
            if (cursoId == null) {
                throw new IllegalArgumentException("Error! Debe seleccionar un curso.");
            }
            Curso curso;
            if (esAdministrador) {
                curso = cursoServicio.buscarPorId(cursoId).filter(c -> !c.esInactivo())
                        .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
            } else {
                curso = cursoServicio.validarAccesoDocente(cursoId, usuario.getDocente()).getCurso();
            }
            modelo.addAttribute("curso", curso);
            modelo.addAttribute("participantes", participanteServicio.buscarParticipantes(curso, null, null));
            modelo.addAttribute("cohortes", curso.getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            modelo.addAttribute("soloCohorte", false);
            modelo.addAttribute("esDocente", !esAdministrador);
            modelo.addAttribute("esAdministrador", esAdministrador);
            modelo.addAttribute("urlBase", "/curso/acceder/" + cursoId);
            modelo.addAttribute("urlMisCursos", esAdministrador ? "/curso/misCursos" : "/curso/buscar");
            modelo.addAttribute("menuActivo", "participantes");
            modelo.addAttribute("titulo", "Participantes | Idóneos Online");
            return "pages/participante/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return esAdministrador ? "redirect:/curso/misCursos" : "redirect:/curso/buscar";
        }
    }

    /**
     * CU-27: Lista el equipo docente completo del curso y los alumnos de la propia cohorte del alumno.
     *
     * @param idInscripcion      El identificador de la inscripción del alumno.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de participantes, o una redirección a "Mis cursos" si no puede acceder.
     */
    @GetMapping("/ver/{idInscripcion}")
    public String verParticipantes(@PathVariable("idInscripcion") int idInscripcion, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionServicio.validarAcceso(idInscripcion, alumno);
            modelo.addAttribute("inscripcion", inscripcion);
            modelo.addAttribute("curso", inscripcion.getCurso());
            modelo.addAttribute("participantes",
                    participanteServicio.buscarParticipantesPorCohorte(inscripcion.getCohorte(), null, null));
            modelo.addAttribute("cohortes", java.util.List.of());
            modelo.addAttribute("soloCohorte", true);
            modelo.addAttribute("esDocente", false);
            modelo.addAttribute("esAdministrador", false);
            modelo.addAttribute("urlBase", "/inscripcion/acceder/" + idInscripcion);
            modelo.addAttribute("urlMisCursos", "/inscripcion/misCursos");
            modelo.addAttribute("menuActivo", "participantes");
            modelo.addAttribute("titulo", "Participantes | Idóneos Online");
            return "pages/participante/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }
}
