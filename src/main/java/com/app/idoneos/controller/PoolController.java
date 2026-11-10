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

/** Controlador del pool. */
@Controller
@RequestMapping("/pool")
public class PoolController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private PoolService poolService;

    @Autowired
    private ObjectMapper objectMapper;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /pool/buscar: Lista los pools de un curso, con su tema y la cantidad de preguntas cargadas. */
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
            model.addAttribute("temas", esAdministrador ? List.of() : panel.temasDelCursoEnOrden(contexto));
            model.addAttribute("datosNuevo", aJson(Map.of("nombre", "", "preguntas", List.of())));
            model.addAttribute("puedeGestionar", !esAdministrador);
            // Desde "Completar contenido" se llega con ?nuevo&temaId=: se abre sola la ventana para crear el pool de ese tema
            model.addAttribute("abrirModal", !esAdministrador && nuevo != null ? "modalNuevoPool" : null);
            model.addAttribute("temaSeleccionada", temaId);
            model.addAttribute("titulo", "Pools | Idóneos Online");
            return "pages/pool/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /pool/registrar: Registra un pool con sus preguntas. */
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

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /pool/modificar/{id}: Modifica un pool sin intentos asociados. */
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

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /pool/darDeBaja/{id}: Da de baja un pool que no está asociado a ninguna autoevaluación activa. */
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

    /** POST /pool/darDeBajaMasiva: Da de baja varios pools a la vez, todos o ninguno. */
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

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Lee las preguntas que envía el editor, que las serializa como una lista JSON. */
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

    /** Arma los datos que el editor muestra de un pool: su nombre y sus preguntas con sus opciones vigentes. */
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

    // ----------------------------------------------------------------------
    // Ventana de modificación a pedido
    // ----------------------------------------------------------------------

    /** GET /pool/modificar/{id}: Ventana de modificación de un pool, que el listado pide al abrirla (en lugar de dibujar una por fila). */
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

        // ----------------------------------------------------------------------
        // Listado (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
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
