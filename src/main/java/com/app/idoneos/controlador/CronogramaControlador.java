package com.app.idoneos.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Unidad.UnidadServicio;
import com.app.idoneos.servicio.UnidadCronograma.UnidadCronogramaServicio;
import com.app.idoneos.utilidades.RespuestaJsonUtilidad;

/**
 * Controlador del cronograma (MOD-F-02): CU-23 Buscar cronograma (GET /cronograma/buscar) y CU-24 Modificar cronograma
 * (POST /cronograma/modificar).
 *
 * La página muestra el cronograma del programa por defecto del docente (o el vigente, si lo ve el administrador). El
 * docente (titular o ayudante) lo modifica mientras el programa no tenga inscripciones activas; en ese caso, y para el
 * administrador, se muestra en solo lectura. El alumno ve el cronograma desde la vista de su curso.
 */
@Controller
@RequestMapping("/cronograma")
public class CronogramaControlador extends PanelCursoControlador {

    /** Mínimo de unidades con material que se usa si el parámetro no está cargado. */
    private static final int MINIMO_POR_DEFECTO = 1;

    @Autowired
    private UnidadCronogramaServicio cronogramaServicio;

    @Autowired
    private UnidadServicio unidadServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-23: Muestra el cronograma del programa de trabajo de un curso, con sus unidades, el orden, las semanas de cada
     * una y el material cargado. Si el docente puede modificarlo, la misma página permite hacerlo (CU-24).
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del cronograma, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarCronograma(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            boolean esAdministrador = contexto.esAdministrador();
            Curso curso = contexto.curso();
            Programa programa = contexto.programa();
            Cohorte cohorte = esAdministrador ? null : contexto.participacion().getCohorteDeTrabajo();
            if (programa == null) {
                return avisarCursoSinProgramas(contexto, redirectAttributes);
            }
            boolean conInscripciones = programa.getCantidadInscripcionesActivas() > 0;
            // Las unidades activas del curso que algún programa incluye: las que se pueden poner en este cronograma
            List<Unidad> unidadesDelCurso = unidadServicio.buscarPorCurso(curso).stream()
                    .filter(u -> !u.getProgramasQueLaIncluyen().isEmpty()).toList();

            cargarContexto(modelo, contexto, "cronograma");
            cargarSemanasMaximasDelCronograma(modelo);
            modelo.addAttribute("cohorte", cohorte);
            modelo.addAttribute("cronograma", programa.getCronogramaOrdenado());
            modelo.addAttribute("unidadesDelCurso", unidadesDelCurso);
            modelo.addAttribute("puedeEditar", !esAdministrador && !conInscripciones);
            modelo.addAttribute("soloLecturaPorInscripciones", !esAdministrador && conInscripciones);
            modelo.addAttribute("minimoUnidadesConMaterial", parametroServicio
                    .obtenerEntero(Parametro.MINIMO_UNIDADES_CON_MATERIAL, MINIMO_POR_DEFECTO));
            modelo.addAttribute("titulo", "Cronograma | Idóneos Online");
            return "pages/cronograma/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-24: Modifica el cronograma de un programa: las unidades que lo componen, su orden y sus semanas. Responde en
     * JSON.
     *
     * @param programaId Identificador del programa.
     * @param ids        Identificadores de las unidades, en el orden del cronograma.
     * @param semanas    Semanas de cada unidad, en el mismo orden.
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar")
    public ResponseEntity<Map<String, String>> modificarCronograma(
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "semanas", required = false) List<String> semanas, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            cronogramaServicio.modificarCronograma(usuario.getDocente(), programaId, ids, semanas);
            return RespuestaJsonUtilidad.respuestaExitosa("Cronograma modificado correctamente.");
        } catch (Exception e) {
            return RespuestaJsonUtilidad.respuestaConError(e);
        }
    }
}
