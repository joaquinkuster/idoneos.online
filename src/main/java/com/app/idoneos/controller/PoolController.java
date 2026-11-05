package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.util.PaginacionUtil;
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

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.OpcionRespuesta;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Pregunta;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.pool.PoolService;
import com.app.idoneos.service.pool.PoolService.SolicitudPregunta;
import com.app.idoneos.util.RespuestaFormularioUtil;

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
public class PoolController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private PoolService poolService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-60: Lista los pools de un curso, con su tema y la cantidad de preguntas cargadas. Trae también los datos de
     * cada pool y del alta para las ventanas de crear y modificar.
     *
     * @param cursoId            Identificador del curso.
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "temaId", required = false) Integer temaId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
            boolean esAdministrador = contexto.esAdministrador();
            Docente docente = esAdministrador ? null : usuario.getDocente();
            // Listado completo: los filtros se aplican en la pantalla
            List<Pool> pools = poolService.buscarConFiltros(cursoId, null, null, false, docente);
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<Pool> pagina = Filtros.tabla.paginar(pools, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            model.addAttribute("pools", pagina.filas());
            if (PaginacionUtil.pideSoloFilas(request)) {
                model.addAttribute("puedeGestionar", !esAdministrador);
                return "pages/pool/buscar :: filas"; // solo las filas de la página pedida
            }

            Programa programa = contexto.programa();
            panel.cargarContexto(model, contexto, "pools");
            model.addAttribute("temas", programa == null || esAdministrador ? List.of() : programa.getCronogramaOrdenado());
            model.addAttribute("datosNuevo", aJson(Map.of("nombre", "", "preguntas", List.of())));
            model.addAttribute("puedeGestionar", !esAdministrador);
            // Desde «Completar contenido» se llega con ?nuevo&temaId=: se abre sola la ventana para crear el pool de ese tema
            model.addAttribute("abrirModal", !esAdministrador && nuevo != null ? "modalNuevoPool" : null);
            model.addAttribute("temaSeleccionada", temaId);
            model.addAttribute("titulo", "Pools | Idóneos Online");
            return "pages/pool/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-61: Registra un pool con sus preguntas. Responde en JSON.
     *
     * @param temaId  Identificador del tema.
     * @param nombre    El nombre del pool.
     * @param preguntas Las preguntas, en JSON.
     * @param authentication      La autenticación actual.
     * @return Una respuesta con el mensaje de éxito (y el listado como destino) o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "temaId", required = false) Integer temaId,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "preguntas", required = false) String preguntas, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Pool pool = poolService.registrar(usuario.getDocente(), temaId, nombre, leerPreguntas(preguntas));
            return panel.exitoConContenidoPendiente("Pool '" + pool.getNombre() + "' creado con éxito.", null,
                    pool.getTema().getCurso().getIdCurso(), usuario.getDocente());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-62: Modifica un pool sin intentos asociados. Responde en JSON.
     *
     * @param id        Identificador del pool.
     * @param nombre    El nombre del pool.
     * @param preguntas Las preguntas que debe conservar, en JSON.
     * @param authentication      La autenticación actual.
     * @return Una respuesta con el mensaje de éxito (y el listado como destino) o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "preguntas", required = false) String preguntas, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            poolService.modificar(id, usuario.getDocente(), nombre, leerPreguntas(preguntas));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Pool modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-63: Da de baja un pool que no está asociado a ninguna autoevaluación activa. Responde en JSON.
     *
     * @param id   Identificador del pool.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            // El administrador puede dar de baja los pools de cualquier curso
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            poolService.darDeBaja(id, docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Pool dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * Da de baja varios pools a la vez, todos o ninguno. Responde en JSON.
     *
     * @param ids  Identificadores de los pools seleccionados.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            poolService.darDeBajaVarios(ids, docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Pools dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
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
    private Map<String, Object> buscarDatos(Pool pool) {
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

    // ---------------------------------------------------------------- Ventana de modificación a pedido

    /**
     * Ventana de modificación de un pool, que el listado pide al abrirla (en lugar de dibujar una por fila).
     *
     * @param id     El pool.
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        Pool p = poolService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El pool no se encuentra disponible."));
        ContextoCurso contexto = panel.contextoDelPanel(p.getTema().getCurso().getIdCurso(), usuario); // verifica el acceso al curso
        model.addAttribute("curso", contexto.curso());
        model.addAttribute("p", p);
        model.addAttribute("datosPorPool", Map.of(p.getIdPool(), aJson(buscarDatos(p))));
        return "pages/pool/modificar :: modal(p=${p})";
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado (motor común)

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<Pool> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Pool>("nombre", FiltroUtil.Tipo.TEXTO, Pool::getNombre),
                new FiltroUtil.Columna<Pool>("tema", FiltroUtil.Tipo.LISTA, p -> p.getTema().getTitulo()),
                new FiltroUtil.Columna<Pool>("preguntas", FiltroUtil.Tipo.NUMERO, p -> String.valueOf(p.getCantidadPreguntas())),
                new FiltroUtil.Columna<Pool>("autoevaluaciones", FiltroUtil.Tipo.NUMERO,
                        p -> String.valueOf(p.getAutoevaluacionesActivas().size())),
                new FiltroUtil.Columna<Pool>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        p -> FiltroUtil.formatearFechaHora(p.getFechaCreacion()))),
                Pool::getBaja);
    }
}
