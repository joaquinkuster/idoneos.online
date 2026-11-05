package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.service.inscripcion.InscripcionService;

import com.app.idoneos.util.PaginacionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.alumno.AlumnoService;
import com.app.idoneos.service.cohorte.CohorteService;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.docentecurso.DocenteCursoService;

import java.util.List;

/**
 * Participantes de un curso (CU-25 y CU-27): su equipo docente ({@code DocenteCurso}) y sus alumnos ({@code Alumno}). Lo ven el docente y el administrador desde la
 * gestión del curso (con todas las cohortes) y el alumno desde el acceso a su curso (con los de su cohorte).
 */
@Controller
@RequestMapping("/participante")
public class ParticipanteController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private DocenteCursoService docenteCursoService;

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private CohorteService cohorteService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Muestra los participantes del curso (equipo docente y alumnos) al docente o al administrador.
     *
     * @param cursoId            Identificador del curso.
     * @param model             Modelo de la vista.
     * @param authentication               Autenticación de la persona que consulta.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección con el motivo si no se puede mostrar.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        boolean esAdministrador = usuario.esAdministradorActivo();
        try {
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
            Curso curso = contexto.curso();
            panel.cargarContexto(model, contexto, "docentes");
            List<DocenteCurso> equipo = docenteCursoService.buscarEquipoPorCurso(curso);
            List<Alumno> alumnos = alumnoService.buscarPorCurso(curso);
            model.addAttribute("docentes", equipo);
            model.addAttribute("alumnos", alumnos);
            model.addAttribute("filas", paginar(model, request, equipo, alumnos, curso));
            if (PaginacionUtil.pideSoloFilas(request)) {
                model.addAttribute("soloCohorte", false);
                return "pages/participante/buscar :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("cohortes", cohorteService.buscarPorCurso(curso));
            // Se entra viendo a todos los participantes del curso (de cualquier cohorte); se filtra a mano por cohorte. Preseleccionar la
            // cohorte de trabajo ocultaba a los alumnos de las demás
            model.addAttribute("cohorteSeleccionada", null);
            model.addAttribute("soloCohorte", false);
            model.addAttribute("titulo", "Participantes | Idóneos Online");
            return "pages/participante/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return esAdministrador ? "redirect:/curso/buscar?vista=cards" : "redirect:/curso/buscar";
        }
    }

    /**
     * Muestra el equipo docente y los alumnos de su cohorte al alumno, desde el acceso a su curso.
     *
     * @param idInscripcion      Identificador de la inscripción del alumno.
     * @param model             Modelo de la vista.
     * @param authentication               Autenticación del alumno.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección con el motivo si no se puede mostrar.
     */
    @GetMapping("/ver/{idInscripcion}")
    public String ver(@PathVariable("idInscripcion") int idInscripcion, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionService.validarAcceso(idInscripcion, alumno);
            Curso curso = inscripcion.getCohorte().getPrograma().getCurso();
            panel.cargarContexto(model, ContextoCurso.deAlumno(inscripcion), "docentes");
            model.addAttribute("inscripcion", inscripcion);
            List<DocenteCurso> equipo = docenteCursoService.buscarEquipoPorCurso(curso);
            List<Alumno> alumnos = alumnoService.buscarPorCohorte(inscripcion.getCohorte());
            model.addAttribute("docentes", equipo);
            model.addAttribute("alumnos", alumnos);
            model.addAttribute("filas", paginar(model, request, equipo, alumnos, curso));
            if (PaginacionUtil.pideSoloFilas(request)) {
                model.addAttribute("soloCohorte", true);
                model.addAttribute("curso", curso);
                return "pages/participante/buscar :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("cohortes", List.of());
            model.addAttribute("soloCohorte", true);
            model.addAttribute("titulo", "Participantes | Idóneos Online");
            return "pages/participante/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/buscar";
        }
    }

    // ---------------------------------------------------------------- Listado (filtros y página en el servidor)

    /** Una fila del listado: un docente del equipo o un alumno con su inscripción. */
    public record FilaParticipante(DocenteCurso docente, Alumno alumno, Inscripcion inscripcion) {
    }


    /** Arma una sola lista con el equipo docente y los alumnos, y deja en el modelo los datos del paginador. Devuelve la página pedida. */
    private List<FilaParticipante> paginar(Model model, jakarta.servlet.http.HttpServletRequest request, List<DocenteCurso> equipo,
            List<Alumno> alumnos, Curso curso) {
        List<FilaParticipante> todas = new java.util.ArrayList<>();
        equipo.forEach(d -> todas.add(new FilaParticipante(d, null, null)));
        alumnos.forEach(a -> todas.add(new FilaParticipante(null, a, a.getInscripcionActivaEn(curso))));
        PaginacionUtil.Pagina<FilaParticipante> pagina = Filtros.tabla.paginar(todas, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        return pagina.filas();
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<FilaParticipante> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<FilaParticipante>("nombreYApellido", FiltroUtil.Tipo.TEXTO,
                        f -> f.docente() != null ? nombre(f.docente().getDocente().getUsuario()) : nombre(f.alumno().getUsuario())),
                new FiltroUtil.Columna<FilaParticipante>("rol", FiltroUtil.Tipo.LISTA,
                        f -> f.docente() != null ? (f.docente().getEsTitular() ? "Docente titular" : "Docente ayudante") : "Alumno"),
                new FiltroUtil.Columna<FilaParticipante>("programa", FiltroUtil.Tipo.LISTA,
                        f -> f.alumno() != null && f.inscripcion().getCohorte().getPrograma() != null
                                ? f.inscripcion().getCohorte().getPrograma().getNombre() : ""),
                new FiltroUtil.Columna<FilaParticipante>("cohorte", FiltroUtil.Tipo.LISTA,
                        f -> f.docente() != null ? (f.docente().getCohorteDeTrabajo() != null ? f.docente().getCohorteDeTrabajo().getEtiqueta() : "")
                                : f.inscripcion().getCohorte().getEtiqueta()),
                new FiltroUtil.Columna<FilaParticipante>("fechaCreacion", FiltroUtil.Tipo.FECHA, f -> {
                    java.time.LocalDateTime fecha = (f.docente() != null ? f.docente().getDocente().getUsuario() : f.alumno().getUsuario())
                            .getFechaCreacion();
                    return FiltroUtil.formatearFechaHora(fecha);
                })), f -> false);

        private static String nombre(com.app.idoneos.model.Usuario u) {
            return u.getApellido() + ", " + u.getNombre();
        }
    }
}
