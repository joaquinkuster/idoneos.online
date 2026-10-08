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
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.utilidades.PaginacionUtilidad;
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

    /** Cantidad de cursos por página por defecto. El valor 0 significa "todos". */
    private static final int CURSOS_POR_PAGINA = 10;

    @Autowired
    private InscripcionServicioImpl inscripcionServicio;

    /**
     * CU-02: Lista los cursos en los que el alumno está inscripto, con su progreso general,
     * filtrando por nombre del curso y estado de la inscripción (Pendiente, En Progreso o Finalizado).
     *
     * @param busqueda           Parte del nombre del curso.
     * @param estado             Estado de la inscripción.
     * @param porPagina          Cantidad de cursos por página (10, 25, 50 o 0 para ver todos).
     * @param page               Número de página (empieza en 0).
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Ver mis cursos".
     */
    @GetMapping("/misCursos")
    public String verMisCursos(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "porPagina", defaultValue = "" + CURSOS_POR_PAGINA) int porPagina,
            @RequestParam(value = "page", defaultValue = "0") int page, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            List<Inscripcion> inscripciones = inscripcionServicio.buscarMisCursos(alumno, busqueda, estado);
            int tamanioPagina = porPagina > 0 ? porPagina : Math.max(inscripciones.size(), 1);
            int totalPaginas = PaginacionUtilidad.calcularTotalPaginas(inscripciones.size(), tamanioPagina);
            int pagina = PaginacionUtilidad.ajustarPagina(page, totalPaginas);
            List<Inscripcion> inscripcionesPagina = PaginacionUtilidad.obtenerPagina(inscripciones, pagina, tamanioPagina);

            modelo.addAttribute("inscripciones", inscripcionesPagina);
            modelo.addAttribute("totalInscripciones", inscripciones.size());
            modelo.addAttribute("currentPage", pagina);
            modelo.addAttribute("totalPages", totalPaginas);
            modelo.addAttribute("desdeCurso", inscripcionesPagina.isEmpty() ? 0 : pagina * tamanioPagina + 1);
            modelo.addAttribute("hastaCurso", inscripcionesPagina.isEmpty() ? 0 : pagina * tamanioPagina + inscripcionesPagina.size());
            modelo.addAttribute("porPagina", porPagina);
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
            modelo.addAttribute("esDocente", false);
            modelo.addAttribute("urlBase", "/inscripcion/acceder/" + idInscripcion);
            modelo.addAttribute("urlMisCursos", "/inscripcion/misCursos");
            modelo.addAttribute("seccion", seccionActiva);
            modelo.addAttribute("menuActivo", seccionActiva);
            modelo.addAttribute("titulo", "CU-27 - " + inscripcion.getCurso().getNombre() + " | Idóneos Online");
            return "pages/curso/acceder";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }
}
