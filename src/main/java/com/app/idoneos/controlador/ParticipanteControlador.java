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
            Authentication auth, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        boolean esAdministrador = usuario.esAdministradorActivo();
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            Curso curso = contexto.curso();
            cargarContexto(modelo, contexto, "docentes");
            modelo.addAttribute("docentes", docenteCursoServicio.buscarEquipoPorCurso(curso));
            modelo.addAttribute("alumnos", alumnoServicio.buscarPorCurso(curso));
            modelo.addAttribute("cohortes", cohorteServicio.buscarPorCurso(curso));
            // El docente entra con el filtro en su cohorte de trabajo; el administrador ve todas
            modelo.addAttribute("cohorteSeleccionada", contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte());
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
            RedirectAttributes redirectAttributes) {
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
            modelo.addAttribute("docentes", docenteCursoServicio.buscarEquipoPorCurso(curso));
            modelo.addAttribute("alumnos", alumnoServicio.buscarPorCohorte(inscripcion.getCohorte()));
            modelo.addAttribute("cohortes", List.of());
            modelo.addAttribute("soloCohorte", true);
            modelo.addAttribute("titulo", "Participantes | Idóneos Online");
            return "pages/participante/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }
}
