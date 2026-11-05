package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.service.tema.TemaService;

import com.app.idoneos.util.PaginacionUtil;
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

import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.RespuestaForo;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.consultaforo.ConsultaForoService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/**
 * Controlador de las consultas del foro (MOD-F-02): CU-35 Buscar consulta de foro (GET /consulta/buscar, GET
 * /consulta/buscarPorTema/{temaId}), CU-39 Buscar respuesta de foro (GET /consulta/ver/{id}), CU-36 Registrar consulta (POST
 * /consulta/registrar), CU-37 Modificar consulta (POST /consulta/modificar/{id}) y CU-38 Dar de baja consulta (POST
 * /consulta/darDeBaja/{id}). Las respuestas están en {@link RespuestaController}.
 *
 * Cada tema tiene su foro ("Foro 1", "Foro 2", … según el orden del tema en el programa). Los alumnos con el tema
 * habilitada plantean consultas; los docentes (titular o ayudante) las responden; el administrador modera dando de baja
 * consultas y respuestas. Todos ven el foro de los temas a las que tienen acceso.
 */
@Controller
@RequestMapping("/consulta")
public class ConsultaController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private TemaService temaService;

    @Autowired
    private ConsultaForoService consultaService;

    @Autowired
    private ParametroService parametroService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Lista los foros de un curso, uno por tema del programa, cada uno con su cantidad de consultas, las que están
     * sin responder y su último mensaje. Es el listado del ABM: desde cada fila se entra al foro.
     *
     * @param cursoId            Identificador del curso.
     * @param temaId           Identificador del tema (opcional).
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarForos(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "temaId", required = false) Integer temaId, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, (Usuario) authentication.getPrincipal());
            Programa programa = contexto.programa();
            List<TemaCronograma> todas = programa == null ? List.of() : programa.getCronogramaOrdenado();

            panel.cargarContexto(model, contexto, "foros");
            model.addAttribute("temasDelPrograma", todas);
            model.addAttribute("temaSeleccionada", temaId);
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<TemaCronograma> pagina = Filtros.tabla.paginar(todas, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            model.addAttribute("temas", pagina.filas());
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/consulta/buscar :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("titulo", "Foros | Idóneos Online");
            return "pages/consulta/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * CU-35: Muestra el foro de un tema: sus consultas con el último mensaje más reciente primero, con buscador por
     * texto y rango de fechas. La paginación (filas por página y paginador) la resuelve
     * la propia tabla, como en los demás listados.
     *
     * @param temaId Identificador del tema.
     * @param texto    Texto a buscar (opcional).
     * @param desde    Fecha de registro mínima (opcional).
     * @param hasta    Fecha de registro máxima (opcional).
     * @param model   El modelo de la vista.
     * @param authentication     La autenticación actual.
     * @return La vista del foro.
     */
    @GetMapping("/buscarPorTema/{temaId}")
    public String verForo(@PathVariable("temaId") Integer temaId,
            @RequestParam(value = "texto", required = false) String texto,
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model, Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        Tema tema = temaService.buscarPorId(temaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        panel.cargarContextoDelForo(tema, (Usuario) authentication.getPrincipal(), model);

        List<ConsultaForo> encontradas = consultaService.buscarConFiltros(tema, texto, desde, hasta);

        // La búsqueda por texto y fechas la resuelve el servicio; la página, el servidor
        PaginacionUtil.Pagina<ConsultaForo> pagina = PaginacionUtil.paginar(encontradas, request.getParameterMap(), java.util.Map.of());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("consultas", pagina.filas());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/consulta/ver :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("totalConsultas", tema.getCantidadConsultasForo());
        model.addAttribute("cantidadEncontradas", encontradas.size());
        model.addAttribute("texto", texto == null ? "" : texto.trim());
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("hayBusqueda", (texto != null && !texto.isBlank()) || desde != null || hasta != null);
        model.addAttribute("titulo", "Foro " + model.getAttribute("numeroOrden") + " | Idóneos Online");
        return "pages/consulta/ver";
    }

    /**
     * CU-39: Muestra una consulta con sus respuestas. Con {@code respuestaId} muestra solo esa respuesta (es su enlace
     * permanente).
     *
     * @param id          Identificador de la consulta.
     * @param respuestaId Identificador de una respuesta (opcional).
     * @param model      El modelo de la vista.
     * @param authentication        La autenticación actual.
     * @return La vista de la consulta.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable("id") Integer id,
            @RequestParam(value = "respuestaId", required = false) Integer respuestaId, Model model,
            Authentication authentication) {
        ConsultaForo consulta = consultaService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        panel.cargarContextoDelForo(consulta.getTema(), (Usuario) authentication.getPrincipal(), model);

        List<RespuestaForo> respuestas = consulta.getRespuestasVigentes();
        boolean unaSola = respuestaId != null;
        if (unaSola) {
            respuestas = respuestas.stream().filter(r -> r.getIdRespuesta() == respuestaId).toList();
        }
        model.addAttribute("consulta", consulta);
        model.addAttribute("respuestas", respuestas);
        model.addAttribute("unaSolaRespuesta", unaSola);
        model.addAttribute("titulo", "Consulta del foro | Idóneos Online");
        return "pages/consulta/consulta";
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-36: Registra una consulta en el foro de un tema. Responde en JSON.
     *
     * @param temaId Identificador del tema.
     * @param texto    El texto de la consulta.
     * @param authentication     La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "temaId", required = false) Integer temaId,
            @RequestParam(value = "texto", required = false) String texto, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            consultaService.registrar(usuario.getAlumno(), temaId, texto);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Consulta registrada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-37: Modifica una consulta propia, dentro del plazo de edición. Responde en JSON.
     *
     * @param id    Identificador de la consulta.
     * @param texto El texto nuevo.
     * @param authentication  La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "texto", required = false) String texto, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            consultaService.modificar(id, usuario.getAlumno(), texto);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Consulta modificada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-38: Da de baja una consulta y sus respuestas (administrador). Responde en JSON.
     *
     * @param id Identificador de la consulta.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            Integer temaId = consultaService.buscarPorId(id).map(c -> c.getTema().getIdTema()).orElse(null);
            consultaService.darDeBaja(id);
            // Después de la baja se vuelve al foro: la consulta dada de baja ya no tiene página propia
            return RespuestaFormularioUtil.armarRespuestaExitosa("Consulta dada de baja con éxito.", "/consulta/buscarPorTema/" + temaId);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado de foros (motor común)

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<TemaCronograma> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<TemaCronograma>("nombre", FiltroUtil.Tipo.TEXTO, u -> "Foro " + u.getNumeroOrden()),
                new FiltroUtil.Columna<TemaCronograma>("tema", FiltroUtil.Tipo.LISTA, u -> u.getTema().getTitulo()),
                new FiltroUtil.Columna<TemaCronograma>("consultas", FiltroUtil.Tipo.NUMERO,
                        u -> String.valueOf(u.getTema().getCantidadConsultasForo())),
                new FiltroUtil.Columna<TemaCronograma>("sinResponder", FiltroUtil.Tipo.NUMERO,
                        u -> String.valueOf(u.getTema().getCantidadConsultasSinResponder())),
                new FiltroUtil.Columna<TemaCronograma>("ultimoMensaje", FiltroUtil.Tipo.FECHA,
                        u -> FiltroUtil.formatearFechaHora(u.getTema().getUltimaActividadForo()))),
                u -> false);
    }
}
