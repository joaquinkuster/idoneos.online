package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.curso.CursoService;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.cohorte.CohorteService;
import com.app.idoneos.service.docentecurso.DocenteCursoService;
import com.app.idoneos.service.programa.ProgramaService;

import com.app.idoneos.util.RespuestaFormularioUtil;
import com.app.idoneos.util.PaginacionUtil;

/** Controlador de la cohorte. */
@Controller
@RequestMapping("/cohorte")
public class CohorteController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private DocenteCursoService docenteCursoService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private com.app.idoneos.repository.InscripcionRepository inscripcionRepository;

    @Autowired
    private com.app.idoneos.repository.ClaseEnVivoRepository claseEnVivoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /cohorte/faltantes: Qué le falta al contenido de un programa para que una cohorte pueda estar Activa (el formulario de alta lo pide al elegir el programa, en lugar de calcularlo para todos al cargar el listado). */
    @GetMapping("/faltantes")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<String> faltantesDelPrograma(@RequestParam(value = "programaId", required = false) Integer programaId) {
        if (programaId == null) {
            return cohorteService.faltantesDeContenido(null);
        }
        return programaService.buscarPorId(programaId).filter(p -> !p.getBaja())
                .map(cohorteService::faltantesDeContenido).orElse(List.of());
    }

    /** GET /cohorte/modificar/{id}: Ventana de modificación de una cohorte. */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        Cohorte cohorte = cohorteService.buscarPorId(id).filter(c -> !c.getBaja())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra disponible."));
        if (!usuario.esAdministradorActivo() && !cohorte.getCurso().tieneDocente(usuario.getDocente())) {
            throw new IllegalArgumentException("Error! No tenés acceso a esta cohorte.");
        }
        model.addAttribute("coh", cohorte);
        model.addAttribute("plazoMinimoContenidoDias", cohorteService.plazoMinimoParaCompletarContenidoDias());
        model.addAttribute("accesoDiasMaximos", Cohorte.DIAS_ACCESO_MAXIMOS);
        model.addAttribute("cupoMaximoPermitido", Cohorte.CUPO_MAXIMO_PERMITIDO);
        return "pages/cohorte/modificar :: modal(cohorte=${coh})";
    }

    /** GET /cohorte/buscar: Busca cohortes de un programa según su estado. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "asignar", required = false) Integer asignar,
            @RequestParam(value = "volver", required = false) String volver,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(value = "orden", defaultValue = "recientes") String orden,
            Model model, Authentication authentication, jakarta.servlet.http.HttpServletRequest request) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        boolean esAdministrador = usuario.esAdministradorActivo();
        Docente soloDeDocente = esAdministrador ? null : usuario.getDocente();
        if (!esAdministrador && cursoId == null) {
            // Las cohortes del docente están dentro del panel de cada uno de sus cursos
            return "redirect:/curso/buscar";
        }

        // El administrador ve las cohortes de todos los cursos, o las de un curso si entra desde su panel
        Curso curso = null;
        if (cursoId != null) {
            try {
                curso = panel.contextoDelPanel(cursoId, usuario).curso();
            } catch (IllegalArgumentException e) {
                return "redirect:/curso/buscar?vista=cards";
            }
        }
        final Curso cursoDelPanel = curso;

        List<Cohorte> cohortes = cohorteService.buscarConFiltros(
                // Listado completo: los filtros (también los que llegan por la URL) se aplican en la pantalla
                null, null, null, null, null, orden, soloDeDocente).stream()
                .filter(cohorte -> cursoDelPanel == null
                        || cohorte.getCurso().getIdCurso() == cursoDelPanel.getIdCurso())
                .toList();
        // Cantidad de inscripciones y de clases vigentes por cohorte, en una consulta cada una (no una por cohorte)
        Map<Integer, Long> inscripcionesPorCohorte = new HashMap<>();
        inscripcionRepository.contarPorCohorte().forEach(f -> inscripcionesPorCohorte.put((Integer) f[0], (Long) f[1]));
        Map<Integer, Long> clasesPorCohorte = new HashMap<>();
        claseEnVivoRepository.contarPorCohorte().forEach(f -> clasesPorCohorte.put((Integer) f[0], (Long) f[1]));

        // Cohortes que el docente tiene como contexto de trabajo por defecto
        Set<Integer> cohortesDeTrabajo = new HashSet<>();
        if (soloDeDocente != null) {
            docenteCursoService.buscarPorDocente(soloDeDocente).stream()
                    .map(participacion -> participacion.getCohorteDeTrabajo())
                    .filter(java.util.Objects::nonNull)
                    .forEach(cohorte -> cohortesDeTrabajo.add(cohorte.getIdCohorte()));
        }

        // Se ve una página por vez: el motor común filtra, ordena y pagina en el servidor
        final List<Cohorte> todasLasCohortes = cohortes;
        PaginacionUtil.Pagina<Cohorte> pagina = Filtros.tabla.paginar(cohortes, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        cohortes = pagina.filas();
        model.addAttribute("cohortes", cohortes);
        model.addAttribute("inscripcionesPorCohorte", inscripcionesPorCohorte);
        model.addAttribute("clasesPorCohorte", clasesPorCohorte);
        if (esAdministrador && PaginacionUtil.pideSoloFilas(request)) {
            return "pages/cohorte/buscarPorAdministrador :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("cohortesDeTrabajo", cohortesDeTrabajo);
        List<com.app.idoneos.model.Programa> programasOfrecidos = curso == null
                ? programaService.obtenerTodo().stream().filter(p -> !p.getBaja()).toList()
                : curso.getProgramasActivos();
        model.addAttribute("programas", programasOfrecidos);
        model.addAttribute("programaSeleccionado", programaId);
        model.addAttribute("cursoSeleccionado", curso == null ? null : curso.getIdCurso());
        model.addAttribute("abrirAlta", nuevo != null);
        model.addAttribute("plazoMinimoContenidoDias", cohorteService.plazoMinimoParaCompletarContenidoDias());
        // Qué le falta al contenido de cada programa para poder abrir la inscripción (el formulario avisa si la cohorte queda pendiente)
        Map<Integer, List<String>> faltantesPorPrograma = new HashMap<>();
        // Solo con un curso del panel: calcularlo para todos los programas de todos los cursos hacía lento el listado del administrador.
        if (curso != null) {
            for (var programa : programasOfrecidos) {
                faltantesPorPrograma.put(programa.getIdPrograma(), cohorteService.faltantesDeContenido(programa));
            }
        }
        model.addAttribute("faltantesPorPrograma", faltantesPorPrograma);
        model.addAttribute("faltantesSinPrograma", cohorteService.faltantesDeContenido(null));
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("ordenSeleccionado", orden);
        model.addAttribute("accesoDiasMaximos", Cohorte.DIAS_ACCESO_MAXIMOS);
        model.addAttribute("cupoMaximoPermitido", Cohorte.CUPO_MAXIMO_PERMITIDO);
        model.addAttribute("titulo", "Cohortes | Idóneos Online");
        if (esAdministrador) {
            model.addAttribute("cursos", cursoService.obtenerTodo());
            if (curso != null) {
                panel.cargarContexto(model, ContextoCurso.deAdministrador(curso), "cohortes");
            } else {
                model.addAttribute("menuActivo", "cohortes");
            }
            return "pages/cohorte/buscarPorAdministrador";
        }
        // Las cohortes a las que el docente todavía puede asignar (o cambiar) el programa: pendientes y sin inscriptos
        if (curso != null) {
            // El docente ve las cohortes dentro del panel de su curso, como el administrador
            panel.cargarContexto(model, panel.contextoDelPanel(cursoId, usuario), "cohortes");
        }
        // Las ventanas de asignar programa son de todas las cohortes que pueden recibirlo, no solo de las de la página visible
        model.addAttribute("cohortesAsignables", todasLasCohortes.stream()
                .filter(c -> c.esPendiente() && !c.getBaja() && c.getCantidadInscriptos() == 0).toList());
        model.addAttribute("programasDelCurso", todasLasCohortes.stream().filter(c -> c.esPendiente() && !c.getBaja() && c.getCantidadInscriptos() == 0)
                .collect(java.util.stream.Collectors.toMap(Cohorte::getIdCohorte,
                        c -> c.getCurso().getProgramasActivos())));
        // Si se llegó desde el diseño del curso (?volver=curso), al asignar el programa se vuelve al inicio del curso
        model.addAttribute("volverAlCurso", asignar != null && "curso".equals(volver));
        // Desde el diseño del curso se llega con ?asignar=<cohorte>: se abre la ventana de asignar programa de esa cohorte
        model.addAttribute("abrirModal", asignar != null && ((java.util.Map<?, ?>) model.getAttribute("programasDelCurso")).containsKey(asignar)
                ? "modalAsignarPrograma" + asignar : null);
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/cohorte/buscarPorDocente :: filas"; // solo las filas de la página pedida
        }
        return "pages/cohorte/buscarPorDocente";
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /cohorte/registrar: Registra una cohorte de un curso, con o sin programa. */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "fechaInicioInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioInscripcion,
            @RequestParam(value = "fechaFinInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinInscripcion,
            @RequestParam(value = "fechaInicioDictado", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDictado,
            @RequestParam(value = "accesoDias", required = false) Integer accesoDias,
            @RequestParam(value = "tiempoExtra", required = false) Integer tiempoExtra,
            @RequestParam(value = "cupoMaximo", required = false) Integer cupoMaximo) {
        try {
            if (cursoId == null) {
                throw RespuestaFormularioUtil.armarCampoInvalido("cursoId", "Error! Debe seleccionar el curso de la cohorte.");
            }
            // El administrador no elige el programa: la cohorte nace pendiente de contenido y el docente le asigna el suyo
            Cohorte cohorte = cohorteService.registrar(cursoId, null, fechaInicioInscripcion,
                    fechaFinInscripcion, fechaInicioDictado,
                    accesoDias != null ? accesoDias : accesoConTiempoExtra(cursoService.buscarActivoPorId(cursoId), tiempoExtra),
                    cupoMaximo);
            return RespuestaFormularioUtil.armarRespuestaExitosa(cohorte.esPendiente()
                    ? "Cohorte registrada como pendiente de contenido: cuando el docente le asigne su programa pasa a inscripción próxima y se habilita para que los alumnos se inscriban en su fecha de inicio."
                    : "Cohorte registrada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /cohorte/cambiarContexto/{id}: Cambia el contexto de trabajo del docente a la cohorte seleccionada (la establece como cohorte y programa por defecto en su participación en el curso). */
    @PostMapping("/cambiarContexto/{id}")
    public String cambiarContexto(@PathVariable("id") Integer id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Cohorte cohorte = cohorteService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa."));
            cohorteService.cambiarContextoDeTrabajo(cohorte, usuario.getDocente());
            redirectAttributes.addFlashAttribute("mensaje", "Ahora trabajás sobre la cohorte seleccionada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cohorte/buscar";
    }

    /** POST /cohorte/asignarPrograma/{id}: El docente elige el programa de una cohorte pendiente que el administrador registró sin programa. */
    @PostMapping("/asignarPrograma/{id}")
    public ResponseEntity<Map<String, String>> asignarPrograma(@PathVariable("id") Integer id,
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "volverAlCurso", defaultValue = "false") boolean volverAlCurso, Authentication authentication) {
        try {
            if (programaId == null) {
                throw RespuestaFormularioUtil.armarCampoInvalido("programaId", "Error! Debe seleccionar el programa de la cohorte.");
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Cohorte cohorte = cohorteService.asignarPrograma(id, programaId, usuario.getDocente());
            String mensaje = "Programa asignado a la cohorte. Falta finalizar el diseño para publicarla.";
            // Después de asignar se pregunta si finaliza el diseño. Si no, lo puede hacer más tarde desde el diseño del curso
            Map<String, String> cuerpo = new java.util.LinkedHashMap<>(
                    RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje, TITULO_FINALIZAR_DISENO,
                            textoFinalizarDiseno(cohorte), "Finalizar diseño", "/curso/acceder/" + cohorte.getCurso().getIdCurso()).getBody());
            cuerpo.put("siguientePost", "/cohorte/finalizarDiseno/" + cohorte.getIdCohorte());
            cuerpo.put("siguienteTipo", "advertencia");
            // Desde el diseño del curso se vuelve al inicio del curso, donde sigue el diseño
            cuerpo.put("destino", volverAlCurso ? "/curso/acceder/" + cohorte.getCurso().getIdCurso() : null);
            if (cuerpo.get("destino") == null) {
                cuerpo.remove("destino");
            }
            return ResponseEntity.ok(cuerpo);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Título de la pregunta que se hace después de asignar o registrar un programa. */
    public static final String TITULO_FINALIZAR_DISENO = "¿Deseás finalizar el diseño?";

    /** Arma el texto de la pregunta de finalizar el diseño: lo publicado es un compromiso con los alumnos. */
    public static String textoFinalizarDiseno(Cohorte cohorte) {
        return "La cohorte " + cohorte.getEtiqueta() + " quedará publicada en el catálogo. Lo publicado es un compromiso de la empresa con los alumnos "
                + "que lo consultan o ya se inscribieron, por eso ya no se podrán modificar los datos del curso. "
                + "Si te equivocás, solicitá al administrador la baja de la cohorte para crearla de nuevo.";
    }

    /** POST /cohorte/finalizarDiseno/{id}: Finaliza el diseño de una cohorte pendiente y la publica en el catálogo. */
    @PostMapping("/finalizarDiseno/{id}")
    public ResponseEntity<Map<String, String>> finalizarDiseno(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Cohorte cohorte = cohorteService.finalizarDiseno(id, usuario.getDocente());
            Map<String, String> cuerpo = new java.util.LinkedHashMap<>(
                    RespuestaFormularioUtil.armarRespuestaExitosa("Diseño finalizado. La cohorte quedó publicada en el catálogo.").getBody());
            cuerpo.put("destino", "/curso/acceder/" + cohorte.getCurso().getIdCurso());
            return ResponseEntity.ok(cuerpo);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /cohorte/establecerPorDefecto/{id}: Guarda la cohorte elegida como cohorte por defecto del docente (junto con su programa) en su participación en el curso. */
    @PostMapping("/establecerPorDefecto/{id}")
    public ResponseEntity<Map<String, String>> establecerPorDefecto(@PathVariable("id") Integer id,
            Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Cohorte cohorte = cohorteService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa."));
            cohorteService.cambiarContextoDeTrabajo(cohorte, usuario.getDocente());
            return RespuestaFormularioUtil.armarRespuestaExitosa("Ahora trabajás sobre la cohorte seleccionada.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /cohorte/modificar/{id}: Modifica una cohorte activa. */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "fechaInicioInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioInscripcion,
            @RequestParam(value = "fechaFinInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinInscripcion,
            @RequestParam(value = "fechaInicioDictado", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDictado,
            @RequestParam(value = "accesoDias", required = false) Integer accesoDias,
            @RequestParam(value = "tiempoExtra", required = false) Integer tiempoExtra,
            @RequestParam(value = "cupoMaximo", required = false) Integer cupoMaximo) {
        try {
            if (accesoDias == null) {
                accesoDias = accesoConTiempoExtra(cohorteService.buscarPorId(id)
                        .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa.")).getCurso(), tiempoExtra);
            }
            cohorteService.modificar(id, fechaInicioInscripcion, fechaFinInscripcion, fechaInicioDictado,
                    accesoDias, cupoMaximo);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Cohorte modificada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Calcula los días de acceso de una cohorte. */
    private static int accesoConTiempoExtra(Curso curso, Integer tiempoExtra) {
        return curso.getDuracionDias() + (tiempoExtra == null ? 0 : tiempoExtra);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /cohorte/darDeBaja/{id}: Da de baja una cohorte que no tenga inscripciones ni clases en vivo activas. */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            cohorteService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Cohorte dada de baja correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /cohorte/darDeBajaMasiva: Da de baja varios cohortes a la vez, todos o ninguno: si alguno no puede darse de baja. */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids) {
        try {
            cohorteService.darDeBajaVarios(ids);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Cohortes dadas de baja correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ----------------------------------------------------------------------
        // Listado del administrador (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado del administrador, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Cohorte> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Cohorte>("curso", FiltroUtil.Tipo.LISTA, c -> c.getCurso().getNombre()),
                new FiltroUtil.Columna<Cohorte>("programa", FiltroUtil.Tipo.LISTA,
                        c -> c.getPrograma() != null ? c.getPrograma().getNombre() : "Sin programa"),
                new FiltroUtil.Columna<Cohorte>("fechaInicioInscripcion", FiltroUtil.Tipo.FECHA, c -> FiltroUtil.formatearFecha(c.getFechaInicioInscripcion())),
                new FiltroUtil.Columna<Cohorte>("fechaFinInscripcion", FiltroUtil.Tipo.FECHA, c -> FiltroUtil.formatearFecha(c.getFechaFinInscripcion())),
                new FiltroUtil.Columna<Cohorte>("fechaInicioDictado", FiltroUtil.Tipo.FECHA, c -> FiltroUtil.formatearFecha(c.getFechaInicioDictado())),
                new FiltroUtil.Columna<Cohorte>("fechaFinDictado", FiltroUtil.Tipo.FECHA, c -> FiltroUtil.formatearFecha(c.getFechaFinDictado())),
                new FiltroUtil.Columna<Cohorte>("cupoMaximo", FiltroUtil.Tipo.NUMERO,
                        c -> c.getCupoMaximo() != null ? String.valueOf(c.getCupoMaximo()) : ""),
                new FiltroUtil.Columna<Cohorte>("accesoDias", FiltroUtil.Tipo.NUMERO, c -> String.valueOf(c.getAccesoDias())),
                new FiltroUtil.Columna<Cohorte>("estado", FiltroUtil.Tipo.LISTA, c -> c.getBaja() ? "Dada de baja" : c.getEstado()),
                new FiltroUtil.Columna<Cohorte>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        c -> FiltroUtil.formatearFechaHora(c.getFechaCreacion()))),
                Cohorte::getBaja);
    }
}
