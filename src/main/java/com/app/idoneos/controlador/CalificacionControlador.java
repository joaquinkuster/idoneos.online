package com.app.idoneos.controlador;

import java.util.ArrayList;
import java.util.List;

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
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;

/**
 * Calificaciones de las autoevaluaciones (MOD-F-04): CU-69 Consultar calificaciones. Todos los roles ven el mismo listado:
 * una fila por alumno y autoevaluación, con sus intentos, su mejor nota y el resultado. El docente y el administrador ven
 * los alumnos del curso (GET /calificacion/buscar), con los filtros de alumno y de cohorte (el docente entra con su
 * cohorte de trabajo); el alumno ve solo las suyas (GET /calificacion/ver/{idInscripcion}).
 */
@Controller
@RequestMapping("/calificacion")
public class CalificacionControlador extends PanelCursoControlador {

    /**
     * Una fila del listado: la calificación de un alumno en una autoevaluación.
     *
     * @param inscripcion   la inscripción del alumno
     * @param autoevaluacion la autoevaluación
     * @param numeroUnidad  el número de la unidad en el cronograma del programa
     * @param habilitada    si el alumno ya puede acceder a la unidad
     */
    public record FilaCalificacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion, int numeroUnidad, boolean habilitada) {
    }

    @Autowired
    private ProgresoServicio progresoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-69: Listado de calificaciones del curso para el docente y el administrador: una fila por alumno inscripto y
     * autoevaluación. El docente entra con el filtro en su cohorte de trabajo; el administrador ve todas.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a la lista de cursos si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarCalificaciones(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            List<FilaCalificacion> filas = new ArrayList<>();
            for (Inscripcion inscripcion : progresoServicio.buscarConFiltros(contexto.curso().getIdCurso(), null)) {
                filas.addAll(filasDe(inscripcion));
            }

            cargarContexto(modelo, contexto, "calificaciones");
            modelo.addAttribute("filas", filas);
            modelo.addAttribute("verAlumnos", true);
            modelo.addAttribute("urlListado", "/calificacion/buscar?cursoId=" + contexto.curso().getIdCurso());
            modelo.addAttribute("cohorteSeleccionada", contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte());
            modelo.addAttribute("cohortes", contexto.curso().getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            modelo.addAttribute("titulo", "Calificaciones | Idóneos Online");
            return "pages/calificacion/listado";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * CU-69: Calificaciones del alumno en un curso: para cada autoevaluación visible de su programa, sus intentos, su mejor
     * nota y si la aprobó. Solo ve las suyas.
     *
     * @param idInscripcion      Identificador de la inscripción del alumno.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de las calificaciones, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/ver/{idInscripcion}")
    public String verCalificaciones(@PathVariable("idInscripcion") int idInscripcion, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionServicio.validarAcceso(idInscripcion, alumno);
            cargarContexto(modelo, ContextoCurso.deAlumno(inscripcion), "calificaciones");
            modelo.addAttribute("filas", filasDe(inscripcion));
            modelo.addAttribute("verAlumnos", false);
            modelo.addAttribute("urlListado", "/calificacion/ver/" + inscripcion.getIdInscripcion());
            modelo.addAttribute("cohortes", List.of());
            modelo.addAttribute("titulo", "Calificaciones | Idóneos Online");
            return "pages/calificacion/listado";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }

    // ---------------------------------------------------------------- Auxiliares

    /** Las filas de una inscripción: sus autoevaluaciones visibles, unidad por unidad, en el orden del cronograma. */
    private List<FilaCalificacion> filasDe(Inscripcion inscripcion) {
        List<FilaCalificacion> filas = new ArrayList<>();
        for (UnidadCronograma cronograma : inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()) {
            boolean habilitada = inscripcion.estaUnidadHabilitada(cronograma);
            for (Autoevaluacion autoevaluacion : cronograma.getUnidad().getAutoevaluacionesPublicadas()) {
                filas.add(new FilaCalificacion(inscripcion, autoevaluacion, cronograma.getNumeroOrden(), habilitada));
            }
        }
        return filas;
    }
}
