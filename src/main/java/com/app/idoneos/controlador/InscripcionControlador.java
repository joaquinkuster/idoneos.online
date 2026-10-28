package com.app.idoneos.controlador;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.app.idoneos.servicio.Alumno.AlumnoServicioImpl;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Pago.PagoServicio;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Controlador de las inscripciones.
 *
 * Mapea las pantallas de los casos de uso CU-02 Ver mis cursos (GET /inscripcion/misCursos), CU-27 Acceder curso
 * (GET /inscripcion/acceder/{idInscripcion}) y, en el módulo de inscripciones (MOD-F-03), CU-44 Buscar inscripción
 * (GET /inscripcion/buscar, del Administrador), CU-45 Inscribir al curso, CU-46 Registrar inscripción,
 * CU-47 Modificar inscripción y CU-49 Dar de baja inscripción.
 */
@Controller
@RequestMapping("/inscripcion")
public class InscripcionControlador extends PanelCursoControlador {

    @Autowired
    private AlumnoServicioImpl alumnoServicio;

    @Autowired
    private PagoServicio pagoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-02: Lista los cursos en los que el alumno está inscripto, con su progreso general,
     * filtrando por nombre del curso y estado de la inscripción (En Progreso o Finalizado).
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
            modelo.addAttribute("titulo", "Mis cursos | Idóneos Online");
            return "pages/inscripcion/misCursos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inicio";
        }
    }

    /**
     * CU-44: Lista las inscripciones activas de todos los alumnos (vista del Administrador). El listado llega
     * completo y los filtros por curso, alumno y estado de acceso se aplican en la pantalla.
     *
     * @param modelo El modelo de la vista.
     * @return La vista de búsqueda de inscripciones.
     */
    @GetMapping("/buscar")
    public String buscarInscripciones(Model modelo) {
        List<Inscripcion> inscripciones = inscripcionServicio.buscarConFiltros(null, null, null);
        // Cursos con inscripciones (filtro) y cursos con cohortes abiertas (alta manual), sin repetir
        modelo.addAttribute("inscripciones", inscripciones);
        modelo.addAttribute("cursosFiltro", inscripciones.stream().map(Inscripcion::getCurso).distinct()
                .sorted(Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
        modelo.addAttribute("cohortesFiltro", inscripciones.stream().map(Inscripcion::getCohorte).distinct()
                .sorted(Comparator.comparing((Cohorte c) -> c.getPrograma().getCurso().getNombre(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(c -> c.getPrograma().getNombre(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Cohorte::getFechaInicioInscripcion))
                .toList());
        List<Curso> cursosAbiertos = cursoServicio.buscarEnCatalogo(null, null, null, null, null);
        modelo.addAttribute("cohortesAbiertas", cursosAbiertos.stream()
                .flatMap(curso -> cursoServicio.buscarCohortesAbiertas(curso).stream()).toList());
        modelo.addAttribute("alumnos", alumnoServicio.obtenerTodo().stream()
                .filter(alumno -> !alumno.getUsuario().esInactivo())
                .sorted(Comparator.comparing(alumno -> alumno.getUsuario().getNombreCompleto(),
                        String.CASE_INSENSITIVE_ORDER))
                .toList());
        // Descuento que le corresponde hoy a cada alumno, para el recuadro de pago del formulario de alta
        Map<Integer, Descuento> descuentosPorAlumno = new HashMap<>();
        for (Alumno alumno : alumnoServicio.obtenerTodo()) {
            pagoServicio.calcularDescuentoPara(alumno)
                    .ifPresent(descuento -> descuentosPorAlumno.put(alumno.getIdAlumno(), descuento));
        }
        modelo.addAttribute("descuentosPorAlumno", descuentosPorAlumno);
        modelo.addAttribute("titulo", "Inscripciones | Idóneos Online");
        modelo.addAttribute("menuActivo", "inscripciones");
        return "pages/inscripcion/buscarAdministrador";
    }

    /**
     * CU-27: Permite al alumno acceder a un curso en el que está inscripto: los temas con su contenido (acordeón),
     * el cronograma con su avance o las clases en vivo de su cohorte.
     *
     * @param idInscripcion      El identificador de la inscripción del alumno.
     * @param seccion            La sección a mostrar: "temas" (por defecto), "cronograma" o "clases".
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Acceder curso", o una redirección a "Mis cursos" si no puede acceder.
     */
    @GetMapping("/acceder/{idInscripcion}")
    public String accederCurso(@PathVariable("idInscripcion") int idInscripcion,
            @RequestParam(value = "seccion", defaultValue = "temas") String seccion, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionServicio.validarAcceso(idInscripcion, alumno);
            String seccionActiva = List.of("temas", "cronograma", "clases").contains(seccion) ? seccion : "temas";

            cargarContexto(modelo, ContextoCurso.deAlumno(inscripcion),
                    "temas".equals(seccionActiva) ? "curso" : seccionActiva);
            cargarSemanasMaximasDelCronograma(modelo);
            modelo.addAttribute("inscripcion", inscripcion);
            modelo.addAttribute("cohorte", inscripcion.getCohorte());
            // Temas del cronograma, para el tablero de solo lectura de la sección "cronograma"
            modelo.addAttribute("temasDelCronograma", inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()
                    .stream().map(TemaCronograma::getTema).toList());
            modelo.addAttribute("seccion", seccionActiva);
            modelo.addAttribute("titulo", inscripcion.getCurso().getNombre() + " | Idóneos Online");
            return "pages/curso/acceder";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-45: El alumno se inscribe a una cohorte con inscripción abierta. Responde en JSON y lleva a Mis cursos.
     *
     * @param cohorteId Identificador de la cohorte elegida.
     * @param terminos  Si aceptó los términos y condiciones de las políticas de privacidad.
     * @param auth      La autenticación actual.
     * @return Una respuesta con el mensaje de éxito (y el destino) o el mensaje de error.
     */
    @PostMapping("/inscribir")
    public ResponseEntity<Map<String, String>> inscribir(
            @RequestParam(value = "cohorteId", required = false) Integer cohorteId,
            @RequestParam(value = "terminos", defaultValue = "false") boolean terminos, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Inscripcion inscripcion = inscripcionServicio.inscribir(usuario.getAlumno(), cohorteId, terminos);
            String curso = inscripcion.getCurso().getNombre();
            String mensaje = inscripcion.estaHabilitada()
                    ? "Te inscribiste a «" + curso + "». Ya podés acceder desde Mis cursos."
                    : "Te inscribiste a «" + curso + "». Para acceder al contenido tenés que completar el pago.";
            return RespuestaFormularioUtilidad.respuestaExitosa(mensaje, inscripcion.estaHabilitada() ? "/inscripcion/misCursos"
                    : "/pago/caja/" + inscripcion.getIdInscripcion());
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-46: El administrador registra manualmente la inscripción de un alumno. Responde en JSON.
     *
     * @param alumnoId  Identificador del alumno.
     * @param cohorteId Identificador de la cohorte.
     * @param terminos  Si confirmó que el alumno acepta los términos y condiciones.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "alumnoId", required = false) Integer alumnoId,
            @RequestParam(value = "cohorteId", required = false) Integer cohorteId,
            @RequestParam(value = "terminos", defaultValue = "false") boolean terminos) {
        try {
            Inscripcion inscripcion = inscripcionServicio.registrarInscripcion(alumnoId, cohorteId, terminos);
            String mensaje = "Inscripción de " + inscripcion.getAlumno().getUsuario().getNombreCompleto()
                    + " a «" + inscripcion.getCurso().getNombre() + "» registrada con éxito."
                    + (inscripcion.estaHabilitada() ? "" : " Queda pendiente de pago: registrá ahora su pago.");
            return inscripcion.estaHabilitada() ? RespuestaFormularioUtilidad.respuestaExitosa(mensaje)
                    : RespuestaFormularioUtilidad.respuestaExitosa(mensaje, "/pago/buscar?registrarPara=" + inscripcion.getIdInscripcion());
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-47: El administrador modifica las observaciones, la habilitación y el vencimiento del acceso. Responde en JSON.
     *
     * @param id            Identificador de la inscripción.
     * @param observaciones Las observaciones (opcional).
     * @param habilitada    Si el acceso está habilitado.
     * @param vencimiento   El último día de acceso.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "habilitada", required = false) Boolean habilitada,
            @RequestParam(value = "vencimiento", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimiento) {
        try {
            inscripcionServicio.modificarInscripcion(id, observaciones, habilitada, vencimiento);
            return RespuestaFormularioUtilidad.respuestaExitosa("Inscripción actualizada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-49: Da de baja una inscripción: el alumno la suya (abandono) o el administrador cualquiera. Responde en JSON.
     *
     * @param id     Identificador de la inscripción.
     * @param motivo El motivo u observación de la baja (opcional).
     * @param auth   La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id,
            @RequestParam(value = "motivo", required = false) String motivo, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            inscripcionServicio.darDeBajaInscripcion(id, usuario.esAdministradorActivo() ? null : usuario.getAlumno(), motivo);
            return RespuestaFormularioUtilidad.respuestaExitosa("Inscripción dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Da de baja varias inscripciones a la vez (solo el Administrador). Es de todo o nada. Responde en JSON.
     *
     * @param ids Identificadores de las inscripciones seleccionadas.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarias(
            @RequestParam(value = "ids", required = false) List<Integer> ids) {
        try {
            inscripcionServicio.darDeBajaVarias(ids);
            return RespuestaFormularioUtilidad.respuestaExitosa("Inscripciones dadas de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }
}
