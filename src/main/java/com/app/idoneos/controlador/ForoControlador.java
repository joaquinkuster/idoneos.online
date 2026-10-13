package com.app.idoneos.controlador;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.RespuestaForo;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.ConsultaForo.ConsultaForoServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.servicio.RespuestaForo.RespuestaForoServicio;
import com.app.idoneos.servicio.Unidad.UnidadServicioImpl;
import com.app.idoneos.utilidades.RespuestaUtilidad;

/**
 * Controlador del foro de consultas (MOD-F-02): CU-35 Buscar consulta de foro (GET /foro/buscar, GET /foro/ver/{unidadId}),
 * CU-39 Buscar respuesta de foro (GET /foro/consulta/{id}), CU-36 Registrar consulta (POST /foro/registrar), CU-37 Modificar
 * consulta (POST /foro/modificar/{id}), CU-38 Dar de baja consulta (POST /foro/darDeBaja/{id}), CU-40 Registrar respuesta
 * (POST /foro/responder), CU-41 Modificar respuesta (POST /foro/modificarRespuesta/{id}) y CU-42 Dar de baja respuesta
 * (POST /foro/darDeBajaRespuesta/{id}).
 *
 * Cada unidad tiene su foro ("Foro 1", "Foro 2", … según el orden de la unidad en el programa). Los alumnos con la unidad
 * habilitada plantean consultas; los docentes (titular o ayudante) las responden; el administrador modera dando de baja
 * consultas y respuestas. Todos ven el foro de las unidades a las que tienen acceso.
 */
@Controller
@RequestMapping("/foro")
public class ForoControlador {

    @Autowired
    private ConsultaForoServicioImpl consultaServicio;

    @Autowired
    private RespuestaForoServicio respuestaServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private UnidadServicioImpl unidadServicio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private ParametroServicio parametroServicio;

    /**
     * Lista los foros de un curso, uno por unidad del programa, cada uno con su cantidad de consultas, las que están
     * sin responder y su último mensaje. Es el listado del ABM: desde cada fila se entra al foro.
     *
     * @param cursoId            Identificador del curso.
     * @param unidadId           Identificador de la unidad (opcional).
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarForos(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "unidadId", required = false) Integer unidadId, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            boolean esAdministrador = usuario.esAdministradorActivo();
            if (cursoId == null) {
                throw new IllegalArgumentException("Error! Debe seleccionar un curso.");
            }
            Curso curso;
            Programa programa;
            if (esAdministrador) {
                curso = cursoServicio.buscarPorId(cursoId)
                        .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
                programa = curso.getProgramaVigente();
            } else {
                ParticipacionDocente participacion = cursoServicio.validarAccesoDocente(cursoId, usuario.getDocente());
                curso = participacion.getCurso();
                programa = participacion.getProgramaDeTrabajo();
            }
            List<UnidadCronograma> todas = programa == null ? List.of() : programa.getCronogramaOrdenado();
            List<UnidadCronograma> unidades = todas; // todas las unidades: el filtro se aplica en la pantalla

            modelo.addAttribute("curso", curso);
            modelo.addAttribute("unidadesDelPrograma", todas);
            modelo.addAttribute("unidadSeleccionada", unidadId);
            modelo.addAttribute("unidades", unidades);
            modelo.addAttribute("esDocente", !esAdministrador);
            modelo.addAttribute("esAdministrador", esAdministrador);
            modelo.addAttribute("urlBase", "/curso/acceder/" + cursoId);
            modelo.addAttribute("urlMisCursos", esAdministrador ? "/curso/misCursos" : "/curso/buscar");
            modelo.addAttribute("titulo", "Foros | Idóneos Online");
            modelo.addAttribute("menuActivo", "foros");
            return "pages/foro/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/curso/buscar";
        }
    }

    /**
     * CU-35: Muestra el foro de una unidad: sus consultas con el último mensaje más reciente primero, con buscador por
     * texto y rango de fechas. La paginación (filas por página y paginador) la resuelve
     * la propia tabla, como en los demás listados.
     *
     * @param unidadId Identificador de la unidad.
     * @param texto    Texto a buscar (opcional).
     * @param desde    Fecha de registro mínima (opcional).
     * @param hasta    Fecha de registro máxima (opcional).
     * @param modelo   El modelo de la vista.
     * @param auth     La autenticación actual.
     * @return La vista del foro.
     */
    @GetMapping("/ver/{unidadId}")
    public String verForo(@PathVariable("unidadId") Integer unidadId,
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model modelo, Authentication auth) {
        Unidad unidad = unidadServicio.buscarPorId(unidadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        cargarContexto(unidad, (Usuario) auth.getPrincipal(), modelo);

        List<ConsultaForo> encontradas = consultaServicio.buscarConFiltros(unidad, texto, desde, hasta);

        modelo.addAttribute("consultas", encontradas);
        modelo.addAttribute("totalConsultas", unidad.getCantidadConsultasForo());
        modelo.addAttribute("cantidadEncontradas", encontradas.size());
        modelo.addAttribute("texto", texto == null ? "" : texto.trim());
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        modelo.addAttribute("hayBusqueda", (texto != null && !texto.isBlank()) || desde != null || hasta != null);
        modelo.addAttribute("titulo", "Foro " + modelo.getAttribute("numeroOrden") + " | Idóneos Online");
        return "pages/foro/ver";
    }

    /**
     * CU-39: Muestra una consulta con sus respuestas. Con {@code respuestaId} muestra solo esa respuesta (es su enlace
     * permanente).
     *
     * @param id          Identificador de la consulta.
     * @param respuestaId Identificador de una respuesta (opcional).
     * @param modelo      El modelo de la vista.
     * @param auth        La autenticación actual.
     * @return La vista de la consulta.
     */
    @GetMapping("/consulta/{id}")
    public String verConsulta(@PathVariable("id") Integer id,
            @RequestParam(value = "respuestaId", required = false) Integer respuestaId, Model modelo,
            Authentication auth) {
        ConsultaForo consulta = consultaServicio.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        cargarContexto(consulta.getUnidad(), (Usuario) auth.getPrincipal(), modelo);

        List<RespuestaForo> respuestas = consulta.getRespuestasVigentes();
        boolean unaSola = respuestaId != null;
        if (unaSola) {
            respuestas = respuestas.stream().filter(r -> r.getIdRespuesta() == respuestaId).toList();
        }
        modelo.addAttribute("consulta", consulta);
        modelo.addAttribute("respuestas", respuestas);
        modelo.addAttribute("unaSolaRespuesta", unaSola);
        modelo.addAttribute("titulo", "Consulta del foro | Idóneos Online");
        return "pages/foro/consulta";
    }

    /**
     * Carga en el modelo lo que comparten las vistas del foro: la unidad, el curso, el rol de quien mira, el plazo de
     * edición y los enlaces del menú. Verifica que el usuario pueda ver el foro de la unidad.
     */
    private void cargarContexto(Unidad unidad, Usuario usuario, Model modelo) {
        if (!unidadServicio.puedeVerContenido(unidad, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Curso curso = unidad.getCurso();
        boolean esAdministrador = usuario.esAdministradorActivo();
        Docente docente = usuario.getDocente();
        boolean esDocente = !esAdministrador && docente != null && curso.getEquipoDocente().stream()
                .anyMatch(participacion -> participacion.getDocente().getIdDocente() == docente.getIdDocente());

        Programa programa;
        String urlBase;
        String urlMisCursos;
        if (esAdministrador) {
            programa = curso.getProgramaVigente();
            urlBase = "/curso/acceder/" + curso.getIdCurso();
            urlMisCursos = "/curso/misCursos";
        } else if (esDocente) {
            programa = cursoServicio.validarAccesoDocente(curso.getIdCurso(), docente).getProgramaDeTrabajo();
            urlBase = "/curso/acceder/" + curso.getIdCurso();
            urlMisCursos = "/curso/buscar";
        } else {
            Inscripcion inscripcion = inscripcionServicio.buscarConAccesoAUnidad(usuario.getAlumno(), unidad)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
            programa = inscripcion.getCohorte().getPrograma();
            urlBase = "/inscripcion/acceder/" + inscripcion.getIdInscripcion();
            urlMisCursos = "/inscripcion/misCursos";
        }
        int numeroOrden = programa == null ? 0 : programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getUnidad().getIdUnidad() == unidad.getIdUnidad())
                .mapToInt(UnidadCronograma::getNumeroOrden).findFirst().orElse(0);

        modelo.addAttribute("unidad", unidad);
        modelo.addAttribute("curso", curso);
        modelo.addAttribute("numeroOrden", numeroOrden);
        modelo.addAttribute("esAdministrador", esAdministrador);
        modelo.addAttribute("esDocente", esDocente);
        modelo.addAttribute("esAlumno", !esAdministrador && !esDocente);
        modelo.addAttribute("alumnoActual", esAdministrador || esDocente ? null : usuario.getAlumno());
        modelo.addAttribute("docenteActual", esDocente ? docente : null);
        modelo.addAttribute("plazoEdicion", parametroServicio.obtenerEntero(Parametro.PLAZO_EDICION_FORO,
                Parametro.PLAZO_EDICION_FORO_POR_DEFECTO));
        modelo.addAttribute("urlBase", urlBase);
        modelo.addAttribute("urlMisCursos", urlMisCursos);
        modelo.addAttribute("menuActivo", "foros");
    }

    /**
     * CU-36: Registra una consulta en el foro de una unidad. Responde en JSON.
     *
     * @param unidadId Identificador de la unidad.
     * @param texto    El texto de la consulta.
     * @param auth     La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarConsulta(
            @RequestParam(value = "unidadId", required = false) Integer unidadId,
            @RequestParam(value = "texto", required = false) String texto, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            consultaServicio.registrarConsulta(usuario.getAlumno(), unidadId, texto);
            return RespuestaUtilidad.respuestaExitosa("Consulta registrada con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-37: Modifica una consulta propia, dentro del plazo de edición. Responde en JSON.
     *
     * @param id    Identificador de la consulta.
     * @param texto El texto nuevo.
     * @param auth  La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarConsulta(@PathVariable("id") Integer id,
            @RequestParam(value = "texto", required = false) String texto, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            consultaServicio.modificarConsulta(id, usuario.getAlumno(), texto);
            return RespuestaUtilidad.respuestaExitosa("Consulta modificada correctamente.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-38: Da de baja una consulta y sus respuestas (administrador). Responde en JSON.
     *
     * @param id Identificador de la consulta.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaConsulta(@PathVariable("id") Integer id) {
        try {
            Integer unidadId = consultaServicio.buscarPorId(id).map(c -> c.getUnidad().getIdUnidad()).orElse(null);
            consultaServicio.darDeBajaConsulta(id);
            // Después de la baja se vuelve al foro: la consulta dada de baja ya no tiene página propia
            return ResponseEntity.ok(Map.of("mensaje", "Consulta dada de baja con éxito.", "destino",
                    "/foro/ver/" + unidadId));
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-40: Registra la respuesta de un docente a una consulta. Responde en JSON.
     *
     * @param consultaId Identificador de la consulta.
     * @param texto      El texto de la respuesta.
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/responder")
    public ResponseEntity<Map<String, String>> responder(
            @RequestParam(value = "consultaId", required = false) Integer consultaId,
            @RequestParam(value = "texto", required = false) String texto, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            respuestaServicio.registrarRespuesta(usuario.getDocente(), consultaId, texto);
            return RespuestaUtilidad.respuestaExitosa("Respuesta registrada con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-41: Modifica una respuesta propia, dentro del plazo de edición. Responde en JSON.
     *
     * @param id    Identificador de la respuesta.
     * @param texto El texto nuevo.
     * @param auth  La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificarRespuesta/{id}")
    public ResponseEntity<Map<String, String>> modificarRespuesta(@PathVariable("id") Integer id,
            @RequestParam(value = "texto", required = false) String texto, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            respuestaServicio.modificarRespuesta(id, usuario.getDocente(), texto);
            return RespuestaUtilidad.respuestaExitosa("Respuesta modificada correctamente.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-42: Da de baja una respuesta (administrador). Responde en JSON.
     *
     * @param id Identificador de la respuesta.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaRespuesta/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaRespuesta(@PathVariable("id") Integer id) {
        try {
            respuestaServicio.darDeBajaRespuesta(id);
            return RespuestaUtilidad.respuestaExitosa("Respuesta dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }
}
