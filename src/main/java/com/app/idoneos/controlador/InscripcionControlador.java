package com.app.idoneos.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.utilidades.Utilidades;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;

/**
 * Controlador de las inscripciones del alumno (MOD-F-01).
 *
 * Mapea la pantalla del caso de uso CU-02 Ver mis cursos (GET /inscripcion/misCursos).
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
            int totalPaginas = Utilidades.calcularTotalPaginas(inscripciones.size(), tamanioPagina);
            int pagina = Utilidades.ajustarPagina(page, totalPaginas);
            List<Inscripcion> inscripcionesPagina = Utilidades.obtenerPagina(inscripciones, pagina, tamanioPagina);

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
            return "pages/misCursos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inicio";
        }
    }
}
