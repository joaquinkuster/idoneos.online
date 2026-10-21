package com.app.idoneos.controlador;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Autoevaluacion.AutoevaluacionServicio;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;

/**
 * Calificaciones de las autoevaluaciones (MOD-F-04): CU-69 Consultar calificaciones. El docente y el administrador ven una
 * matriz con la mejor nota de cada alumno del curso en cada autoevaluación (GET /calificacion/buscar); el alumno ve las
 * suyas (GET /calificacion/ver/{idInscripcion}).
 */
@Controller
@RequestMapping("/calificacion")
public class CalificacionControlador extends PanelCursoControlador {

    @Autowired
    private AutoevaluacionServicio autoevaluacionServicio;

    @Autowired
    private ProgresoServicio progresoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-69: Matriz de calificaciones del curso: una fila por alumno inscripto y una columna por autoevaluación visible, con
     * la mejor nota. El docente entra con el filtro en su cohorte de trabajo; el administrador ve todas.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de la matriz, o una redirección a la lista de cursos si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarCalificaciones(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            List<Inscripcion> inscripciones = progresoServicio.buscarConFiltros(contexto.curso().getIdCurso(), null);
            List<Autoevaluacion> autoevaluaciones = autoevaluacionServicio
                    .buscarConFiltros(cursoId, null, null, false, contexto.esAdministrador() ? null : usuario.getDocente())
                    .stream().filter(Autoevaluacion::esVisible).toList();

            // Las unidades de cada programa: una autoevaluación solo corresponde a los alumnos de programas que la incluyen
            Map<Integer, Set<Integer>> unidadesPorPrograma = new LinkedHashMap<>();
            for (Programa programa : contexto.curso().getProgramasActivos()) {
                Set<Integer> unidades = new HashSet<>();
                programa.getCronogramaOrdenado().forEach(c -> unidades.add(c.getUnidad().getIdUnidad()));
                unidadesPorPrograma.put(programa.getIdPrograma(), unidades);
            }

            cargarContexto(modelo, contexto, "calificaciones");
            modelo.addAttribute("inscripciones", inscripciones);
            modelo.addAttribute("autoevaluaciones", autoevaluaciones);
            modelo.addAttribute("unidadesPorPrograma", unidadesPorPrograma);
            modelo.addAttribute("cohorteSeleccionada", contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte());
            modelo.addAttribute("cohortes", contexto.curso().getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            modelo.addAttribute("titulo", "Calificaciones | Idóneos Online");
            return "pages/calificacion/buscar";
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
            List<Autoevaluacion> autoevaluaciones = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream()
                    .map(UnidadCronograma::getUnidad).flatMap(unidad -> unidad.getAutoevaluacionesPublicadas().stream())
                    .sorted(Comparator.comparing((Autoevaluacion a) -> a.getUnidad().getIdUnidad())
                            .thenComparingInt(Autoevaluacion::getIdAutoevaluacion))
                    .toList();
            Set<Integer> unidadesHabilitadas = new HashSet<>();
            inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream().filter(inscripcion::estaUnidadHabilitada)
                    .forEach(c -> unidadesHabilitadas.add(c.getUnidad().getIdUnidad()));
            modelo.addAttribute("unidadesHabilitadas", unidadesHabilitadas);
            cargarContexto(modelo, ContextoCurso.deAlumno(inscripcion), "calificaciones");
            modelo.addAttribute("inscripcion", inscripcion);
            modelo.addAttribute("autoevaluaciones", autoevaluaciones);
            modelo.addAttribute("titulo", "Calificaciones | Idóneos Online");
            return "pages/calificacion/ver";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }
}
