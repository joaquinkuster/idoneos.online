package com.app.idoneos.controlador;

import com.app.idoneos.utilidades.FiltroUtilidad;
import com.app.idoneos.utilidades.PaginacionUtilidad;
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
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Alumno.AlumnoServicio;
import com.app.idoneos.servicio.Cohorte.CohorteServicio;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.DocenteCurso.DocenteCursoServicio;

import java.util.List;

/**
 * Participantes de un curso (CU-25 y CU-27): su equipo docente ({@code DocenteCurso}) y sus alumnos ({@code Alumno}). Lo ven el docente y el administrador desde la
 * gestión del curso (con todas las cohortes) y el alumno desde el acceso a su curso (con los de su cohorte).
 */
@Controller
@RequestMapping("/participante")
public class ParticipanteControlador extends PanelCursoControlador {

    @Autowired
    private DocenteCursoServicio docenteCursoServicio;

    @Autowired
    private AlumnoServicio alumnoServicio;

    @Autowired
    private CohorteServicio cohorteServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Muestra los participantes del curso (equipo docente y alumnos) al docente o al administrador.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             Modelo de la vista.
     * @param auth               Autenticación de la persona que consulta.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección con el motivo si no se puede mostrar.
     */
    @GetMapping("/buscar")
    public String buscarParticipantes(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest solicitud) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        boolean esAdministrador = usuario.esAdministradorActivo();
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            Curso curso = contexto.curso();
            cargarContexto(modelo, contexto, "docentes");
            List<DocenteCurso> equipo = docenteCursoServicio.buscarEquipoPorCurso(curso);
            List<Alumno> alumnos = alumnoServicio.buscarPorCurso(curso);
            modelo.addAttribute("docentes", equipo);
            modelo.addAttribute("alumnos", alumnos);
            modelo.addAttribute("filas", paginar(modelo, solicitud, equipo, alumnos, curso));
            if (PaginacionUtilidad.pideSoloFilas(solicitud)) {
                modelo.addAttribute("soloCohorte", false);
                return "pages/participante/buscar :: filas"; // solo las filas de la página pedida
            }
            modelo.addAttribute("cohortes", cohorteServicio.buscarPorCurso(curso));
            // Se entra viendo a todos los participantes del curso (de cualquier cohorte); se filtra a mano por cohorte. Preseleccionar la
            // cohorte de trabajo ocultaba a los alumnos de las demás
            modelo.addAttribute("cohorteSeleccionada", null);
            modelo.addAttribute("soloCohorte", false);
            modelo.addAttribute("titulo", "Participantes | Idóneos Online");
            return "pages/participante/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return esAdministrador ? "redirect:/curso/misCursos" : "redirect:/curso/buscar";
        }
    }

    /**
     * Muestra el equipo docente y los alumnos de su cohorte al alumno, desde el acceso a su curso.
     *
     * @param idInscripcion      Identificador de la inscripción del alumno.
     * @param modelo             Modelo de la vista.
     * @param auth               Autenticación del alumno.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección con el motivo si no se puede mostrar.
     */
    @GetMapping("/ver/{idInscripcion}")
    public String verParticipantes(@PathVariable("idInscripcion") int idInscripcion, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest solicitud) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionServicio.validarAcceso(idInscripcion, alumno);
            Curso curso = inscripcion.getCohorte().getPrograma().getCurso();
            cargarContexto(modelo, ContextoCurso.deAlumno(inscripcion), "docentes");
            modelo.addAttribute("inscripcion", inscripcion);
            List<DocenteCurso> equipo = docenteCursoServicio.buscarEquipoPorCurso(curso);
            List<Alumno> alumnos = alumnoServicio.buscarPorCohorte(inscripcion.getCohorte());
            modelo.addAttribute("docentes", equipo);
            modelo.addAttribute("alumnos", alumnos);
            modelo.addAttribute("filas", paginar(modelo, solicitud, equipo, alumnos, curso));
            if (PaginacionUtilidad.pideSoloFilas(solicitud)) {
                modelo.addAttribute("soloCohorte", true);
                modelo.addAttribute("curso", curso);
                return "pages/participante/buscar :: filas"; // solo las filas de la página pedida
            }
            modelo.addAttribute("cohortes", List.of());
            modelo.addAttribute("soloCohorte", true);
            modelo.addAttribute("titulo", "Participantes | Idóneos Online");
            return "pages/participante/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }

    // ---------------------------------------------------------------- Listado (filtros y página en el servidor)

    /** Una fila del listado: un docente del equipo o un alumno con su inscripción. */
    public record FilaParticipante(DocenteCurso docente, Alumno alumno, Inscripcion inscripcion) {
    }

    private static String nombre(com.app.idoneos.modelo.Usuario u) {
        return u.getApellido() + ", " + u.getNombre();
    }

    /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
    private static final FiltroUtilidad<FilaParticipante> FILTROS = new FiltroUtilidad<>(List.of(
            new FiltroUtilidad.Columna<FilaParticipante>("nombreYApellido", FiltroUtilidad.Tipo.TEXTO,
                    f -> f.docente() != null ? nombre(f.docente().getDocente().getUsuario()) : nombre(f.alumno().getUsuario())),
            new FiltroUtilidad.Columna<FilaParticipante>("rol", FiltroUtilidad.Tipo.LISTA,
                    f -> f.docente() != null ? (f.docente().getEsTitular() ? "Docente titular" : "Docente ayudante") : "Alumno"),
            new FiltroUtilidad.Columna<FilaParticipante>("programa", FiltroUtilidad.Tipo.LISTA,
                    f -> f.alumno() != null && f.inscripcion().getCohorte().getPrograma() != null
                            ? f.inscripcion().getCohorte().getPrograma().getNombre() : ""),
            new FiltroUtilidad.Columna<FilaParticipante>("cohorte", FiltroUtilidad.Tipo.LISTA,
                    f -> f.docente() != null ? (f.docente().getCohorteDeTrabajo() != null ? f.docente().getCohorteDeTrabajo().getEtiqueta() : "")
                            : f.inscripcion().getCohorte().getEtiqueta()),
            new FiltroUtilidad.Columna<FilaParticipante>("fechaCreacion", FiltroUtilidad.Tipo.FECHA, f -> {
                java.time.LocalDateTime fecha = (f.docente() != null ? f.docente().getDocente().getUsuario() : f.alumno().getUsuario())
                        .getFechaCreacion();
                return fecha == null ? "" : fecha.toString().replace('T', ' ');
            })), f -> false);

    /** Arma una sola lista con el equipo docente y los alumnos, y deja en el modelo los datos del paginador. Devuelve la página pedida. */
    private List<FilaParticipante> paginar(Model modelo, jakarta.servlet.http.HttpServletRequest solicitud, List<DocenteCurso> equipo,
            List<Alumno> alumnos, Curso curso) {
        List<FilaParticipante> todas = new java.util.ArrayList<>();
        equipo.forEach(d -> todas.add(new FilaParticipante(d, null, null)));
        alumnos.forEach(a -> todas.add(new FilaParticipante(null, a, a.getInscripcionActivaEn(curso))));
        List<FilaParticipante> filtradas = FILTROS.filtrar(todas, solicitud.getParameterMap());
        FILTROS.ordenar(filtradas, solicitud.getParameterMap());
        PaginacionUtilidad.Pagina<FilaParticipante> pagina = PaginacionUtilidad.paginar(filtradas, solicitud.getParameterMap(),
                FILTROS.opciones(todas));
        PaginacionUtilidad.aModelo(modelo, pagina);
        return pagina.filas();
    }
}
