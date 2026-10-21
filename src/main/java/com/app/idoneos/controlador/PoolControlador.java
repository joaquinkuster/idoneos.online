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
 * Controlador del ABM de pools de preguntas (MOD-F-04): CU-60 Buscar pool (GET /pool/buscar), CU-61 Crear pool (POST
 * /pool/registrar), CU-62 Modificar pool (POST /pool/modificar/{id}) y CU-63 Dar de baja pool (POST /pool/darDeBaja/{id} y
 * /pool/darDeBajaMasiva). Crear y modificar se hacen en ventanas por pasos del listado.
 *
 * El docente (titular o ayudante) gestiona los pools de los cursos en los que participa; el administrador puede
 * buscarlos y darlos de baja en cualquier curso.
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
     * CU-60: Lista los pools de un curso, con su unidad y la cantidad de preguntas cargadas. Trae también los datos de
     * cada pool y del alta para las ventanas de crear y modificar.
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

            Programa programa = contexto.programa();
            Map<Integer, String> datosPorPool = new LinkedHashMap<>();
            for (Pool pool : pools) {
                datosPorPool.put(pool.getIdPool(), aJson(datosDelPool(pool)));
            }

            cargarContexto(modelo, contexto, "pools");
            modelo.addAttribute("pools", pools);
            modelo.addAttribute("unidades", programa == null || esAdministrador ? List.of() : programa.getCronogramaOrdenado());
            modelo.addAttribute("datosPorPool", datosPorPool);
            modelo.addAttribute("datosNuevo", aJson(Map.of("nombre", "", "preguntas", List.of())));
            modelo.addAttribute("puedeGestionar", !esAdministrador);
            modelo.addAttribute("titulo", "Pools | Idóneos Online");
            return "pages/pool/buscar";
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
            return RespuestaFormularioUtilidad.respuestaExitosa("Pool '" + pool.getNombre() + "' creado con éxito.");
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
            poolServicio.modificarPool(id, usuario.getDocente(), nombre, leerPreguntas(preguntas));
            return RespuestaFormularioUtilidad.respuestaExitosa("Pool modificado correctamente.");
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
