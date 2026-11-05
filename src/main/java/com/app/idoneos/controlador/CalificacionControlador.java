package com.app.idoneos.controlador;

import com.app.idoneos.utilidades.FiltroUtilidad;
import com.app.idoneos.utilidades.PaginacionUtilidad;
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
import com.app.idoneos.modelo.TemaCronograma;
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
     * @param numeroTema  el número del tema en el cronograma del programa
     * @param habilitada    si el alumno ya puede acceder al tema
     */
    public record FilaCalificacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion, int numeroTema, boolean habilitada) {
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
            Authentication auth, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest solicitud) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            List<FilaCalificacion> filas = new ArrayList<>();
            for (Inscripcion inscripcion : progresoServicio.buscarConFiltros(contexto.curso().getIdCurso(), null)) {
                filas.addAll(filasDe(inscripcion));
            }

            Integer cohorteSeleccionada = contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte();
            // Entra con el filtro en la cohorte de trabajo; si el navegador pide otra página o cambia el filtro, manda su propia elección
            java.util.Map<String, String[]> parametros = new java.util.HashMap<>(solicitud.getParameterMap());
            if (!PaginacionUtilidad.pideSoloFilas(solicitud) && !parametros.containsKey("cohorte") && cohorteSeleccionada != null) {
                parametros.put("cohorte", new String[] { String.valueOf(cohorteSeleccionada) });
            }
            // El filtro, el orden por columna y la página los resuelve el servidor
            List<FilaCalificacion> filtradas = FILTROS.filtrar(filas, parametros);
            FILTROS.ordenar(filtradas, parametros);
            PaginacionUtilidad.Pagina<FilaCalificacion> pagina = PaginacionUtilidad.paginar(filtradas, parametros, FILTROS.opciones(filas));
            PaginacionUtilidad.aModelo(modelo, pagina);

            cargarContexto(modelo, contexto, "calificaciones");
            modelo.addAttribute("filas", pagina.filas());
            modelo.addAttribute("verAlumnos", true);
            if (PaginacionUtilidad.pideSoloFilas(solicitud)) {
                return "pages/calificacion/listado :: filas"; // solo las filas de la página pedida
            }
            modelo.addAttribute("urlListado", "/calificacion/buscar?cursoId=" + contexto.curso().getIdCurso());
            modelo.addAttribute("cohorteSeleccionada", cohorteSeleccionada);
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
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest solicitud) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionServicio.validarAcceso(idInscripcion, alumno);
            cargarContexto(modelo, ContextoCurso.deAlumno(inscripcion), "calificaciones");
            List<FilaCalificacion> todas = filasDe(inscripcion);
            List<FilaCalificacion> filtradas = FILTROS.filtrar(todas, solicitud.getParameterMap());
            FILTROS.ordenar(filtradas, solicitud.getParameterMap());
            PaginacionUtilidad.Pagina<FilaCalificacion> pagina = PaginacionUtilidad.paginar(filtradas, solicitud.getParameterMap(), FILTROS.opciones(todas));
            PaginacionUtilidad.aModelo(modelo, pagina);
            modelo.addAttribute("filas", pagina.filas());
            modelo.addAttribute("verAlumnos", false);
            if (PaginacionUtilidad.pideSoloFilas(solicitud)) {
                return "pages/calificacion/listado :: filas"; // solo las filas de la página pedida
            }
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

    /** Las filas de una inscripción: sus autoevaluaciones visibles, tema por tema, en el orden del cronograma. */
    private List<FilaCalificacion> filasDe(Inscripcion inscripcion) {
        List<FilaCalificacion> filas = new ArrayList<>();
        for (TemaCronograma cronograma : inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()) {
            boolean habilitada = inscripcion.estaTemaHabilitada(cronograma);
            for (Autoevaluacion autoevaluacion : cronograma.getTema().getAutoevaluacionesPublicadas()) {
                filas.add(new FilaCalificacion(inscripcion, autoevaluacion, cronograma.getNumeroOrden(), habilitada));
            }
        }
        return filas;
    }

    // ---------------------------------------------------------------- Listado (filtros y página en el servidor)

    private static String resultado(FilaCalificacion f) {
        Float nota = f.autoevaluacion().mejorNota(f.inscripcion());
        return f.autoevaluacion().estaAprobadaPor(f.inscripcion()) ? "Aprobada"
                : (nota != null ? "No aprobada" : (f.habilitada() ? "Sin rendir" : "Tema bloqueada"));
    }

    /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
    private static final FiltroUtilidad<FilaCalificacion> FILTROS = new FiltroUtilidad<>(List.of(
            new FiltroUtilidad.Columna<FilaCalificacion>("alumno", FiltroUtilidad.Tipo.TEXTO,
                    f -> f.inscripcion().getAlumno().getUsuario().getNombreCompleto() + " "
                            + f.inscripcion().getAlumno().getUsuario().getDni() + " " + f.inscripcion().getAlumno().getUsuario().getCorreo()),
            new FiltroUtilidad.Columna<FilaCalificacion>("cohorte", FiltroUtilidad.Tipo.LISTA,
                    f -> String.valueOf(f.inscripcion().getCohorte().getIdCohorte())),
            new FiltroUtilidad.Columna<FilaCalificacion>("tema", FiltroUtilidad.Tipo.LISTA, f -> f.autoevaluacion().getTema().getTitulo()),
            new FiltroUtilidad.Columna<FilaCalificacion>("autoevaluacion", FiltroUtilidad.Tipo.LISTA, f -> f.autoevaluacion().getNombre()),
            new FiltroUtilidad.Columna<FilaCalificacion>("intentos", FiltroUtilidad.Tipo.NUMERO,
                    f -> String.valueOf(f.autoevaluacion().intentosDe(f.inscripcion()).size())),
            new FiltroUtilidad.Columna<FilaCalificacion>("mejorNota", FiltroUtilidad.Tipo.NUMERO,
                    f -> java.util.Objects.toString(f.autoevaluacion().mejorNota(f.inscripcion()), "")),
            new FiltroUtilidad.Columna<FilaCalificacion>("resultado", FiltroUtilidad.Tipo.LISTA, CalificacionControlador::resultado)),
            f -> false);
}
