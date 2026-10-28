package com.app.idoneos.controlador;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Progreso.DetalleProgreso;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;

/**
 * Controlador del progreso de los alumnos de un curso (MOD-F-03).
 *
 * Mapea la pantalla de CU-55 Buscar progreso (GET /progreso/buscar), para el docente y el administrador. El detalle de
 * cada alumno por tema se despliega debajo de su fila.
 */
@Controller
@RequestMapping("/progreso")
public class ProgresoControlador extends PanelCursoControlador {

    @Autowired
    private ProgresoServicio progresoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-55: Lista los alumnos inscriptos en el curso, de todas sus cohortes, con su avance y, desplegable debajo de
     * cada uno, el detalle por tema. El filtro por alumno se aplica en la pantalla.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del progreso, o una redirección a la lista de cursos si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarProgresos(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            cargarContexto(modelo, contexto, "progreso");
            List<Inscripcion> inscripciones = progresoServicio.buscarConFiltros(contexto.curso().getIdCurso(), null);
            Map<Integer, DetalleProgreso> detalles = new LinkedHashMap<>();
            for (Inscripcion inscripcion : inscripciones) {
                detalles.put(inscripcion.getIdInscripcion(),
                        progresoServicio.buscarDetalle(inscripcion.getIdInscripcion()));
            }
            modelo.addAttribute("inscripciones", inscripciones);
            modelo.addAttribute("detalles", detalles);
            // El docente entra con el filtro en su cohorte de trabajo; el administrador ve todas
            modelo.addAttribute("cohorteSeleccionada", contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte());
            modelo.addAttribute("cohortes", contexto.curso().getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            modelo.addAttribute("titulo", "Progreso | Idóneos Online");
            return "pages/progreso/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }
}
