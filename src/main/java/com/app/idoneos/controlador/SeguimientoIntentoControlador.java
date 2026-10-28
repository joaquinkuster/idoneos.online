package com.app.idoneos.controlador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.IntentoAutoevaluacion.IntentoAutoevaluacionServicio;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Seguimiento de los intentos de autoevaluación de los alumnos de un curso (MOD-F-04): CU-68 Buscar intentos (GET
 * /intento/buscar), CU-71 Aprobar intento (POST /intento/aprobar/{id}, docente) y CU-72 Dar de baja intento (POST
 * /intento/darDeBaja/{id}, administrador). Las pantallas con las que el alumno rinde sus intentos están en
 * {@link IntentoAutoevaluacionControlador}.
 */
@Controller
@RequestMapping("/intento")
public class SeguimientoIntentoControlador extends PanelCursoControlador {

    /**
     * Un intento del listado con su número entre los del mismo alumno en la misma autoevaluación.
     *
     * @param intento el intento
     * @param numero  su número de orden (1 es el primero)
     */
    public record FilaIntento(IntentoAutoevaluacion intento, int numero) {
    }

    @Autowired
    private IntentoAutoevaluacionServicio intentoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-68: Lista los intentos de autoevaluación de los alumnos del curso, de todas sus cohortes. Los filtros se aplican
     * en la pantalla; el docente entra con el filtro en su cohorte de trabajo.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a la lista de cursos si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarIntentos(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            List<IntentoAutoevaluacion> intentos = intentoServicio.buscarDelCurso(cursoId,
                    contexto.esAdministrador() ? null : usuario.getDocente());

            // Número de cada intento entre los del mismo alumno en la misma autoevaluación (del más reciente al más antiguo)
            Map<String, Integer> contadores = new HashMap<>();
            List<FilaIntento> filas = new ArrayList<>();
            for (IntentoAutoevaluacion intento : intentos) {
                String clave = intento.getInscripcion().getIdInscripcion() + "-"
                        + intento.getAutoevaluacion().getIdAutoevaluacion();
                filas.add(new FilaIntento(intento, contadores.merge(clave, 1, Integer::sum)));
            }
            filas = filas.reversed();

            cargarContexto(modelo, contexto, "intentos");
            modelo.addAttribute("filas", filas);
            modelo.addAttribute("cohorteSeleccionada", contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte());
            modelo.addAttribute("cohortes", contexto.curso().getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            modelo.addAttribute("puedeAprobar", !contexto.esAdministrador());
            modelo.addAttribute("puedeDarDeBaja", contexto.esAdministrador());
            modelo.addAttribute("titulo", "Intentos | Idóneos Online");
            return "pages/intento/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-71: El docente aprueba un intento entregado que no alcanzó la nota mínima. Responde en JSON.
     *
     * @param id   Identificador del intento.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/aprobar/{id}")
    public ResponseEntity<Map<String, String>> aprobarIntento(@PathVariable("id") Integer id, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            intentoServicio.aprobarPorDocente(id, usuario.getDocente());
            return RespuestaFormularioUtilidad.respuestaExitosa("Intento aprobado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-72: El administrador da de baja un intento. Responde en JSON.
     *
     * @param id Identificador del intento.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaIntento(@PathVariable("id") Integer id) {
        try {
            boolean revirtio = intentoServicio.darDeBajaIntento(id);
            return RespuestaFormularioUtilidad.respuestaExitosa(revirtio
                    ? "Intento dado de baja con éxito. El alumno ya no aprueba el tema: volvió a estar pendiente y se anuló su certificado, si tenía."
                    : "Intento dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }
}
