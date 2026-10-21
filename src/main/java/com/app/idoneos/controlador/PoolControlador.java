package com.app.idoneos.controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Pool.PoolServicio;
import com.app.idoneos.servicio.Pool.PoolServicio.SolicitudPregunta;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Controlador del ABM de pools de preguntas (MOD-F-04): CU-60 Buscar pool (GET /pool/buscar y GET /pool/ver/{id}), CU-61
 * Crear pool (GET /pool/nuevo y POST /pool/registrar), CU-62 Modificar pool (POST /pool/modificar/{id}) y CU-63 Dar de
 * baja pool (POST /pool/darDeBaja/{id} y /pool/darDeBajaMasiva).
 *
 * El docente (titular o ayudante) gestiona los pools de los cursos en los que participa; el administrador puede
 * buscarlos, verlos y darlos de baja en cualquier curso.
 */
@Controller
@RequestMapping("/pool")
public class PoolControlador extends PanelCursoControlador {

    @Autowired
    private PoolServicio poolServicio;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-60: Lista los pools de un curso, con su unidad y la cantidad de preguntas cargadas.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarPools(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            boolean esAdministrador = contexto.esAdministrador();
            Docente docente = esAdministrador ? null : usuario.getDocente();
            // Listado completo: los filtros se aplican en la pantalla
            List<Pool> pools = poolServicio.buscarConFiltros(cursoId, null, null, false, docente);

            cargarContexto(modelo, contexto, "pools");
            modelo.addAttribute("pools", pools);
            modelo.addAttribute("puedeGestionar", !esAdministrador);
            modelo.addAttribute("titulo", "Pools | Idóneos Online");
            return "pages/pool/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * CU-61: Abre el editor para crear un pool (solo docente). Las unidades que se ofrecen son las del programa de trabajo.
     *
     * @param cursoId            Identificador del curso.
     * @param unidadId           Unidad que viene elegida (opcional).
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del editor, o una redirección si no se puede acceder.
     */
    @GetMapping("/nuevo")
    public String nuevoPool(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "unidadId", required = false) Integer unidadId, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            if (contexto.esAdministrador()) {
                throw new IllegalArgumentException("Error! Solo los docentes pueden crear pools.");
            }
            Programa programa = contexto.programa();
            if (programa == null) {
                return avisarCursoSinProgramas(contexto, redirectAttributes);
            }
            List<UnidadCronograma> unidades = programa.getCronogramaOrdenado();
            if (unidades.isEmpty()) {
                throw new IllegalArgumentException("Error! El programa todavía no tiene unidades: registrá una para poder cargar pools.");
            }
            cargarContexto(modelo, contexto, "pools");
            modelo.addAttribute("pool", null);
            modelo.addAttribute("unidades", unidades);
            modelo.addAttribute("unidadSeleccionada", unidadId);
            modelo.addAttribute("datosPool", aJson(Map.of("nombre", "", "preguntas", List.of())));
            modelo.addAttribute("soloLectura", false);
            modelo.addAttribute("titulo", "Crear pool | Idóneos Online");
            return "pages/pool/editar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * CU-60 (detalle) y CU-62: Muestra un pool con sus preguntas y opciones. El docente lo puede modificar mientras no
     * tenga intentos asociados; si no, y para el administrador, se muestra de solo lectura.
     *
     * @param id                 Identificador del pool.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del editor, o una redirección si no se puede acceder.
     */
    @GetMapping("/ver/{id}")
    public String verPool(@PathVariable("id") Integer id, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Pool pool = poolServicio.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Error! El pool no se encuentra activo."));
            ContextoCurso contexto = contextoDelPanel(pool.getUnidad().getCurso().getIdCurso(), (Usuario) auth.getPrincipal());
            boolean conIntentos = pool.tieneIntentosActivos();

            cargarContexto(modelo, contexto, "pools");
            modelo.addAttribute("pool", pool);
            modelo.addAttribute("unidades", List.of());
            modelo.addAttribute("datosPool", aJson(datosDelPool(pool)));
            modelo.addAttribute("soloLectura", contexto.esAdministrador() || conIntentos);
            modelo.addAttribute("conIntentos", conIntentos);
            modelo.addAttribute("titulo", "Pool | Idóneos Online");
            return "pages/pool/editar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-61: Registra un pool con sus preguntas. Responde en JSON.
     *
     * @param unidadId  Identificador de la unidad.
     * @param nombre    El nombre del pool.
     * @param preguntas Las preguntas, en JSON.
     * @param auth      La autenticación actual.
     * @return Una respuesta con el mensaje de éxito (y el listado como destino) o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarPool(
            @RequestParam(value = "unidadId", required = false) Integer unidadId,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "preguntas", required = false) String preguntas, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Pool pool = poolServicio.registrarPool(usuario.getDocente(), unidadId, nombre, leerPreguntas(preguntas));
            return RespuestaFormularioUtilidad.respuestaExitosa("Pool '" + pool.getNombre() + "' creado con éxito.",
                    "/pool/buscar?cursoId=" + pool.getUnidad().getCurso().getIdCurso());
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-62: Modifica un pool sin intentos asociados. Responde en JSON.
     *
     * @param id        Identificador del pool.
     * @param nombre    El nombre del pool.
     * @param preguntas Las preguntas que debe conservar, en JSON.
     * @param auth      La autenticación actual.
     * @return Una respuesta con el mensaje de éxito (y el listado como destino) o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarPool(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "preguntas", required = false) String preguntas, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Pool pool = poolServicio.modificarPool(id, usuario.getDocente(), nombre, leerPreguntas(preguntas));
            return RespuestaFormularioUtilidad.respuestaExitosa("Pool modificado correctamente.",
                    "/pool/buscar?cursoId=" + pool.getUnidad().getCurso().getIdCurso());
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-63: Da de baja un pool que no está asociado a ninguna autoevaluación activa. Responde en JSON.
     *
     * @param id   Identificador del pool.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaPool(@PathVariable("id") Integer id, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            // El administrador puede dar de baja los pools de cualquier curso
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            poolServicio.darDeBajaPool(id, docente);
            return RespuestaFormularioUtilidad.respuestaExitosa("Pool dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Da de baja varios pools a la vez, todos o ninguno. Responde en JSON.
     *
     * @param ids  Identificadores de los pools seleccionados.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            poolServicio.darDeBajaVarios(ids, docente);
            return RespuestaFormularioUtilidad.respuestaExitosa("Pools dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Auxiliares

    /**
     * Lee las preguntas que envía el editor, que las serializa como una lista JSON.
     */
    private List<SolicitudPregunta> leerPreguntas(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<SolicitudPregunta>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error! No se pudieron leer las preguntas del formulario.");
        }
    }

    /**
     * Arma los datos que el editor muestra de un pool: su nombre y sus preguntas con sus opciones vigentes.
     */
    private Map<String, Object> datosDelPool(Pool pool) {
        List<Map<String, Object>> preguntas = new ArrayList<>();
        for (Pregunta pregunta : pool.getPreguntasActivas()) {
            List<Map<String, Object>> opciones = new ArrayList<>();
            for (OpcionRespuesta opcion : pregunta.getOpcionesActivas()) {
                Map<String, Object> datosOpcion = new LinkedHashMap<>();
                datosOpcion.put("id", opcion.getIdOpcionRespuesta());
                datosOpcion.put("texto", opcion.getTexto());
                datosOpcion.put("correcta", opcion.getEsCorrecta());
                opciones.add(datosOpcion);
            }
            Map<String, Object> datosPregunta = new LinkedHashMap<>();
            datosPregunta.put("id", pregunta.getIdPregunta());
            datosPregunta.put("texto", pregunta.getTexto());
            datosPregunta.put("opcionMultiple", pregunta.getEsOpcionMultiple());
            datosPregunta.put("opciones", opciones);
            preguntas.add(datosPregunta);
        }
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("nombre", pool.getNombre());
        datos.put("preguntas", preguntas);
        return datos;
    }

    private String aJson(Object datos) {
        try {
            return objectMapper.writeValueAsString(datos);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo armar el contenido del pool.", e);
        }
    }
}
