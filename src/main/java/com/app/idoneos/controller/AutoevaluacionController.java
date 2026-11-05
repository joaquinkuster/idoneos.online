package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.service.inscripcion.InscripcionService;

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

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.autoevaluacion.AutoevaluacionService;
import com.app.idoneos.service.autoevaluacion.AutoevaluacionService.SolicitudAutoevaluacion;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.intentoautoevaluacion.IntentoAutoevaluacionService;
import com.app.idoneos.service.pool.PoolService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/**
 * Controlador del ABM de autoevaluaciones (MOD-F-04): CU-64 Buscar autoevaluación (GET /autoevaluacion/buscar), CU-65 Crear
 * autoevaluación (POST /autoevaluacion/registrar), CU-66 Modificar autoevaluación (POST /autoevaluacion/modificar/{id}) y
 * CU-67 Dar de baja autoevaluación (POST /autoevaluacion/darDeBaja/{id} y /autoevaluacion/darDeBajaMasiva).
 *
 * El docente (titular o ayudante) gestiona las autoevaluaciones de los cursos en los que participa; el administrador puede
 * buscarlas y darlas de baja en cualquier curso.
 */
@Controller
@RequestMapping("/autoevaluacion")
public class AutoevaluacionController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private InscripcionService inscripcionService;

    /**
     * Los pools de un tema del programa, para ofrecerlos agrupados en el selector del formulario.
     *
     * @param numeroOrden número de orden del tema en el cronograma
     * @param titulo      título del tema
     * @param pools       los pools del tema
     */
    public record GrupoDePools(int numeroOrden, String titulo, List<Pool> pools) {
    }

    @Autowired
    private AutoevaluacionService autoevaluacionService;

    @Autowired
    private PoolService poolService;

    @Autowired
    private IntentoAutoevaluacionService intentoService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-64: Lista las autoevaluaciones de un curso con sus pools, tiempo límite e intentos permitidos. Desde el listado
     * el docente las crea y las modifica en ventanas.
     *
     * @param cursoId            Identificador del curso.
     * @param accion             "editar" o "baja": abre sola la ventana de esa autoevaluación (desde la vista del curso).
     * @param autoevaluacionId   Identificador de la autoevaluación de la acción (opcional).
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "autoevaluacionId", required = false) Integer autoevaluacionId,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "temaId", required = false) Integer temaId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
            boolean esAdministrador = contexto.esAdministrador();
            Docente docente = esAdministrador ? null : usuario.getDocente();
            Programa programa = contexto.programa();
            List<TemaCronograma> temas = programa == null ? List.of() : programa.getCronogramaOrdenado();
            // Listado completo: los filtros se aplican en la pantalla
            List<Autoevaluacion> autoevaluaciones = autoevaluacionService.buscarConFiltros(cursoId, null, null, false, docente);
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<Autoevaluacion> pagina = Filtros.tabla.paginar(autoevaluaciones, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            if (PaginacionUtil.pideSoloFilas(request)) {
                model.addAttribute("autoevaluaciones", pagina.filas());
                model.addAttribute("puedeGestionar", !esAdministrador);
                return "pages/autoevaluacion/buscar :: filas"; // solo las filas de la página pedida
            }
            List<Pool> pools = poolService.buscarConFiltros(cursoId, null, null, false, docente);
            cargarPoolsDelFormulario(model, pools, temas);

            String abrirModal = null;
            boolean hayAccion = accion != null && autoevaluacionId != null
                    && autoevaluaciones.stream().anyMatch(a -> a.getIdAutoevaluacion() == autoevaluacionId && !a.getBaja());
            if (hayAccion && "baja".equals(accion)) {
                abrirModal = "modalBajaAutoevaluacion" + autoevaluacionId;
            } else if (hayAccion && "editar".equals(accion) && !esAdministrador) {
                abrirModal = "modalModificarAutoevaluacion" + autoevaluacionId;
            }

            // Desde «Completar contenido» se llega con ?nuevo&temaId=: se abre sola la ventana para registrar la autoevaluación de ese tema
            if (abrirModal == null && !esAdministrador && nuevo != null) {
                abrirModal = "modalNuevaAutoevaluacion";
            }
            panel.cargarContexto(model, contexto, "autoevaluaciones");
            model.addAttribute("temaSeleccionada", temaId);
            model.addAttribute("abrirModal", abrirModal);
            model.addAttribute("autoevaluaciones", pagina.filas());
            model.addAttribute("temas", temas);
            model.addAttribute("pools", pools);
            model.addAttribute("puedeGestionar", !esAdministrador);
            model.addAttribute("titulo", "Autoevaluaciones | Idóneos Online");
            return "pages/autoevaluacion/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * Carga en el modelo lo que necesita el formulario: para cada pool los temas que pueden usarlo y los pools agrupados por tema.
     */
    private void cargarPoolsDelFormulario(Model model, List<Pool> pools, List<TemaCronograma> temas) {
        // Para cada pool, los temas cuyos autoevaluaciones pueden usarlo (el formulario filtra con esto)
        Map<Integer, String> temasPorPool = new LinkedHashMap<>();
        for (Pool pool : pools) {
            temasPorPool.put(pool.getIdPool(), String.join(",", autoevaluacionService.temasQuePuedenUsar(pool)
                    .stream().map(u -> String.valueOf(u.getIdTema())).toList()));
        }

        // Los pools que se ofrecen, agrupados por el tema a la que pertenecen (en el orden del cronograma)
        List<GrupoDePools> gruposDePools = new ArrayList<>();
        for (TemaCronograma cronograma : temas) {
            List<Pool> deLaTema = pools.stream()
                    .filter(p -> p.getTema().getIdTema() == cronograma.getTema().getIdTema()).toList();
            if (!deLaTema.isEmpty()) {
                gruposDePools.add(new GrupoDePools(cronograma.getNumeroOrden(), cronograma.getTema().getTitulo(), deLaTema));
            }
        }
        model.addAttribute("temasPorPool", temasPorPool);
        model.addAttribute("gruposDePools", gruposDePools);
    }

    /**
     * Ventana de modificación de una autoevaluación, que el listado pide al abrirla (en lugar de dibujar una por fila).
     *
     * @param id     La autoevaluación.
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        Autoevaluacion a = autoevaluacionService.buscarPorId(id).filter(x -> !x.getBaja())
                .orElseThrow(() -> new IllegalArgumentException("Error! La autoevaluación no se encuentra disponible."));
        Integer cursoId = a.getTema().getCurso().getIdCurso();
        ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
        Docente docente = contexto.esAdministrador() ? null : usuario.getDocente();
        Programa programa = contexto.programa();
        List<TemaCronograma> temas = programa == null ? List.of() : programa.getCronogramaOrdenado();
        cargarPoolsDelFormulario(model, poolService.buscarConFiltros(cursoId, null, null, false, docente), temas);
        model.addAttribute("curso", contexto.curso());
        model.addAttribute("temas", temas);
        model.addAttribute("a", a);
        return "pages/autoevaluacion/modificar :: modal(a=${a})";
    }

    /**
     * CU-27 (flujo del alumno) y CU-70: Ficha de la autoevaluación para el alumno: sus reglas, el historial de sus intentos
     * (con la mejor nota) y el botón para realizar un intento, continuar el que tiene en curso o el motivo por el que no puede
     * rendir ahora.
     *
     * @param id                 Identificador de la autoevaluación.
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de la ficha, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable("id") Integer id,
            @RequestParam(value = "cursoFinalizado", required = false) String cursoFinalizado, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            Autoevaluacion autoevaluacion = autoevaluacionService.buscarPorId(id).filter(Autoevaluacion::esVisible)
                    .orElseThrow(() -> new IllegalArgumentException("Error! La autoevaluación no se encuentra disponible."));
            ContextoCurso contexto = panel.contextoDeLaTema(autoevaluacion.getTema(), usuario);
            Inscripcion inscripcion = inscripcionService.buscarConAccesoATema(alumno, autoevaluacion.getTema())
                    .orElseThrow(() -> new IllegalArgumentException("Error! Todavía no podés acceder a este tema."));
            List<IntentoAutoevaluacion> intentos = intentoService.buscarPorInscripcionYAutoevaluacion(inscripcion,
                    autoevaluacion);
            intentos.forEach(intentoService::cerrarSiVencido); // si se le agotó el tiempo, queda corregido
            intentos = intentoService.buscarPorInscripcionYAutoevaluacion(inscripcion, autoevaluacion);

            panel.cargarContexto(model, contexto, "autoevaluaciones");
            model.addAttribute("autoevaluacion", autoevaluacion);
            model.addAttribute("inscripcion", inscripcion);
            model.addAttribute("intentos", intentos);
            model.addAttribute("enCurso", intentos.stream().filter(IntentoAutoevaluacion::estaEnCurso).findFirst().orElse(null));
            model.addAttribute("motivo", intentoService.motivoNoDisponible(alumno, autoevaluacion).orElse(null));
            model.addAttribute("mejorNota", autoevaluacion.mejorNota(inscripcion));
            model.addAttribute("restantes", autoevaluacion.intentosRestantes(inscripcion));
            // Aviso de curso finalizado: solo si llegó con ?cursoFinalizado y la inscripción tiene su certificado vigente
            model.addAttribute("cursoFinalizado", cursoFinalizado != null && inscripcion.getCertificadoVigente() != null);
            model.addAttribute("titulo", autoevaluacion.getNombre() + " | Idóneos Online");
            return "pages/autoevaluacion/ver";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-65: Registra una autoevaluación asociada a uno o más pools. Responde en JSON.
     *
     * @param temaId           Identificador del tema.
     * @param nombre             El nombre.
     * @param tiempoLimite       El tiempo límite en minutos.
     * @param cantidadPreguntas  La cantidad de preguntas que se sortean.
     * @param notaMinima         La nota mínima para aprobar.
     * @param intentosPermitidos La cantidad de intentos permitidos (vacío: sin límite).
     * @param fechaApertura      La fecha y hora de apertura.
     * @param fechaCierre        La fecha y hora de cierre (opcional).
     * @param descuentoPorError  Si las opciones incorrectas restan.
     * @param navegacionLibre    Si el alumno puede desplazarse libremente entre las preguntas.
     * @param permiteSalir       Si el alumno puede salir del intento en curso.
     * @param oculto             Si queda oculta para los alumnos.
     * @param idsPools           Los pools de los que se sortea.
     * @param authentication               La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "temaId", required = false) Integer temaId,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "tiempoLimite", required = false) String tiempoLimite,
            @RequestParam(value = "cantidadPreguntas", required = false) String cantidadPreguntas,
            @RequestParam(value = "notaMinima", required = false) String notaMinima,
            @RequestParam(value = "intentosPermitidos", required = false) String intentosPermitidos,
            @RequestParam(value = "fechaApertura", required = false) String fechaApertura,
            @RequestParam(value = "fechaCierre", required = false) String fechaCierre,
            @RequestParam(value = "descuentoPorError", defaultValue = "false") boolean descuentoPorError,
            @RequestParam(value = "navegacionLibre", defaultValue = "false") boolean navegacionLibre,
            @RequestParam(value = "permiteSalir", defaultValue = "false") boolean permiteSalir,
            @RequestParam(value = "oculto", defaultValue = "false") boolean oculto,
            @RequestParam(value = "idsPools", required = false) List<Integer> idsPools, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Autoevaluacion autoevaluacion = autoevaluacionService.registrar(usuario.getDocente(), temaId,
                    new SolicitudAutoevaluacion(nombre, tiempoLimite, cantidadPreguntas, notaMinima, intentosPermitidos,
                            fechaApertura, fechaCierre, descuentoPorError, navegacionLibre, permiteSalir, oculto, idsPools));
            return panel.exitoConContenidoPendiente("Autoevaluación '" + autoevaluacion.getNombre() + "' registrada con éxito.", null,
                    autoevaluacion.getTema().getCurso().getIdCurso(), usuario.getDocente());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-66: Modifica una autoevaluación. Con intentos registrados solo cambian la fecha de cierre, la visibilidad y los
     * intentos permitidos. Responde en JSON.
     *
     * @param id                 Identificador de la autoevaluación.
     * @param nombre             El nombre.
     * @param tiempoLimite       El tiempo límite en minutos.
     * @param cantidadPreguntas  La cantidad de preguntas que se sortean.
     * @param notaMinima         La nota mínima para aprobar.
     * @param intentosPermitidos La cantidad de intentos permitidos (vacío: sin límite).
     * @param fechaApertura      La fecha y hora de apertura.
     * @param fechaCierre        La fecha y hora de cierre (opcional).
     * @param descuentoPorError  Si las opciones incorrectas restan.
     * @param navegacionLibre    Si el alumno puede desplazarse libremente entre las preguntas.
     * @param permiteSalir       Si el alumno puede salir del intento en curso.
     * @param oculto             Si queda oculta para los alumnos.
     * @param idsPools           Los pools de los que se sortea.
     * @param authentication               La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "tiempoLimite", required = false) String tiempoLimite,
            @RequestParam(value = "cantidadPreguntas", required = false) String cantidadPreguntas,
            @RequestParam(value = "notaMinima", required = false) String notaMinima,
            @RequestParam(value = "intentosPermitidos", required = false) String intentosPermitidos,
            @RequestParam(value = "fechaApertura", required = false) String fechaApertura,
            @RequestParam(value = "fechaCierre", required = false) String fechaCierre,
            @RequestParam(value = "descuentoPorError", defaultValue = "false") boolean descuentoPorError,
            @RequestParam(value = "navegacionLibre", defaultValue = "false") boolean navegacionLibre,
            @RequestParam(value = "permiteSalir", defaultValue = "false") boolean permiteSalir,
            @RequestParam(value = "oculto", defaultValue = "false") boolean oculto,
            @RequestParam(value = "idsPools", required = false) List<Integer> idsPools, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            autoevaluacionService.modificar(id, usuario.getDocente(),
                    new SolicitudAutoevaluacion(nombre, tiempoLimite, cantidadPreguntas, notaMinima, intentosPermitidos,
                            fechaApertura, fechaCierre, descuentoPorError, navegacionLibre, permiteSalir, oculto, idsPools));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Autoevaluación modificada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-67: Da de baja una autoevaluación sin intentos. Responde en JSON.
     *
     * @param id   Identificador de la autoevaluación.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id,
            Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            // El administrador puede dar de baja las autoevaluaciones de cualquier curso
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            autoevaluacionService.darDeBaja(id, docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Autoevaluación dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * Da de baja varias autoevaluaciones a la vez, todas o ninguna. Responde en JSON.
     *
     * @param ids  Identificadores de las autoevaluaciones seleccionadas.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarias(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            autoevaluacionService.darDeBajaVarias(ids, docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Autoevaluaciones dadas de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado (motor común)


        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<Autoevaluacion> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Autoevaluacion>("nombre", FiltroUtil.Tipo.TEXTO, Autoevaluacion::getNombre),
                new FiltroUtil.Columna<Autoevaluacion>("tema", FiltroUtil.Tipo.LISTA, a -> a.getTema().getTitulo()),
                new FiltroUtil.Columna<Autoevaluacion>("pools", FiltroUtil.Tipo.MULTILISTA,
                        a -> a.getPoolsActivos().stream().map(p -> p.getNombre()).collect(java.util.stream.Collectors.joining("|"))),
                new FiltroUtil.Columna<Autoevaluacion>("tiempo", FiltroUtil.Tipo.NUMERO,
                        a -> a.getTiempoLimite() == null ? "" : String.valueOf(a.getTiempoLimite())),
                new FiltroUtil.Columna<Autoevaluacion>("preguntas", FiltroUtil.Tipo.NUMERO, a -> String.valueOf(a.getCantidadPreguntas())),
                new FiltroUtil.Columna<Autoevaluacion>("nota", FiltroUtil.Tipo.NUMERO, a -> String.valueOf(a.getNotaMinima())),
                new FiltroUtil.Columna<Autoevaluacion>("intentos", FiltroUtil.Tipo.TEXTO,
                        a -> a.getIntentosPermitidos() == null ? "Sin límite" : String.valueOf(a.getIntentosPermitidos())),
                new FiltroUtil.Columna<Autoevaluacion>("visibilidad", FiltroUtil.Tipo.LISTA, a -> a.getOculto() ? "Oculta" : "Visible"),
                new FiltroUtil.Columna<Autoevaluacion>("apertura", FiltroUtil.Tipo.FECHA, a -> FiltroUtil.formatearFechaHora(a.getFechaApertura())),
                new FiltroUtil.Columna<Autoevaluacion>("cierre", FiltroUtil.Tipo.FECHA, a -> FiltroUtil.formatearFechaHora(a.getFechaCierre())),
                new FiltroUtil.Columna<Autoevaluacion>("fechaCreacion", FiltroUtil.Tipo.FECHA, a -> FiltroUtil.formatearFechaHora(a.getFechaCreacion()))),
                Autoevaluacion::getBaja);
    }
}
