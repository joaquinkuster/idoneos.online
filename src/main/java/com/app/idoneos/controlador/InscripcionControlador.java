package com.app.idoneos.controlador;

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
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;

/**
 * Controlador de las inscripciones del alumno (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso CU-02 Ver mis cursos (GET /inscripcion/misCursos) y, en el módulo de
 * gestión académica (MOD-F-02), CU-27 Acceder curso (GET /inscripcion/acceder/{idInscripcion}).
 */
@Controller
@RequestMapping("/inscripcion")
public class InscripcionControlador {

    @Autowired
    private InscripcionServicioImpl inscripcionServicio;

    /**
     * CU-02: Lista los cursos en los que el alumno está inscripto, con su progreso general,
     * filtrando por nombre del curso y estado de la inscripción (Pendiente, En Progreso o Finalizado).
     *
     * @param busqueda           Parte del nombre del curso.
     * @param estado             Estado de la inscripción.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Ver mis cursos".
     */
    @GetMapping("/misCursos")
    public String verMisCursos(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            List<Inscripcion> inscripciones = inscripcionServicio.buscarMisCursos(alumno, busqueda, estado);

            modelo.addAttribute("inscripciones", inscripciones);
            modelo.addAttribute("totalInscripciones", inscripciones.size());
            modelo.addAttribute("busqueda", busqueda);
            modelo.addAttribute("estadoSeleccionado", estado);
            modelo.addAttribute("titulo", "CU-02 - Mis Cursos | Idóneos Online");
            return "pages/inscripcion/misCursos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inicio";
        }
    }

    /**
     * CU-27: Permite al alumno acceder a un curso en el que está inscripto: las unidades con su contenido (acordeón),
     * el cronograma con su avance o las clases en vivo de su cohorte.
     *
     * @param idInscripcion      El identificador de la inscripción del alumno.
     * @param seccion            La sección a mostrar: "unidades" (por defecto), "cronograma" o "clases".
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Acceder curso", o una redirección a "Mis cursos" si no puede acceder.
     */
    @GetMapping("/acceder/{idInscripcion}")
    public String accederCurso(@PathVariable("idInscripcion") int idInscripcion,
            @RequestParam(value = "seccion", defaultValue = "unidades") String seccion, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionServicio.validarAcceso(idInscripcion, alumno);
            String seccionActiva = List.of("unidades", "cronograma", "clases").contains(seccion) ? seccion : "unidades";

            modelo.addAttribute("inscripcion", inscripcion);
            modelo.addAttribute("curso", inscripcion.getCurso());
            modelo.addAttribute("programa", inscripcion.getCohorte().getPrograma());
            modelo.addAttribute("cohorte", inscripcion.getCohorte());
            // Unidades del cronograma, para el tablero de solo lectura de la sección "cronograma"
            modelo.addAttribute("unidadesDelCronograma", inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()
                    .stream().map(UnidadCronograma::getUnidad).toList());
            modelo.addAttribute("esDocente", false);
            modelo.addAttribute("urlBase", "/inscripcion/acceder/" + idInscripcion);
            modelo.addAttribute("urlMisCursos", "/inscripcion/misCursos");
            modelo.addAttribute("seccion", seccionActiva);
            modelo.addAttribute("menuActivo", "unidades".equals(seccionActiva) ? "curso" : seccionActiva);
            modelo.addAttribute("titulo", "CU-27 - " + inscripcion.getCurso().getNombre() + " | Idóneos Online");
            return "pages/curso/acceder";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }
}
