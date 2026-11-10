package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.service.curso.CursoService;

import com.app.idoneos.util.PaginacionUtil;
import java.util.HashMap;
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

import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.service.tema.TemaService;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.programa.ProgramaService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Controlador del programa. */
@Controller
@RequestMapping("/programa")
public class ProgramaController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private TemaService temaService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /programa/buscar: Busca los programas de un curso por nombre y estado (Vigente, Anterior o Dado de baja). */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "editar", required = false) Integer editar, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, (Usuario) authentication.getPrincipal());
            boolean esAdministrador = contexto.esAdministrador();
            Curso curso = contexto.curso();
            List<Programa> programas = programaService.buscarConFiltros(curso, null, null); // lista completa: la usa el asistente
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<Programa> pagina = Filtros.tabla.paginar(programas, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            model.addAttribute("programasPagina", pagina.filas());

            panel.cargarContexto(model, contexto, "programas");
            model.addAttribute("programas", programas);
            model.addAttribute("programasActivos", curso.getProgramasActivos());
            // Lo que dibuja la vista Versiones: una entrada por programa vigente en el listado, con el programa del que deriva
            java.time.format.DateTimeFormatter mesAnio = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy", java.util.Locale.of("es", "AR"));
            List<Map<String, Object>> versiones = new java.util.ArrayList<>();
            programas.stream().filter(p -> !p.getBaja()).sorted(java.util.Comparator.comparing(Programa::getFechaCreacion)).forEach(p -> {
                Map<String, Object> version = new LinkedHashMap<>();
                version.put("id", p.getIdPrograma());
                version.put("nombre", p.getNombre());
                version.put("temas", p.getCantidadTemas());
                version.put("fecha", p.getFechaCreacion().format(mesAnio));
                version.put("cohortes", p.getCantidadCohortesActivas());
                version.put("anterior", p.getProgramaAnterior() != null ? p.getProgramaAnterior().getIdPrograma() : null);
                version.put("vigente", p.esVigente());
                versiones.add(version);
            });
            model.addAttribute("versiones", versiones);
            // Si cada programa está completo y, por tema del curso, el contenido que tiene (para el asistente de programas)
            Map<Integer, Boolean> completos = new HashMap<>();
            programas.forEach(p -> completos.put(p.getIdPrograma(), cursoService.estaDisenoCompleto(curso, p)));
            model.addAttribute("completos", completos);
            if (PaginacionUtil.pideSoloFilas(request)) {
                panel.cargarContexto(model, contexto, "programas");
                model.addAttribute("puedeGestionar", !esAdministrador);
                return "pages/programa/buscar :: filas"; // solo las filas de la página pedida
            }
            List<Tema> temasDelCurso = temaService.buscarPorCurso(curso);
            model.addAttribute("temasDelCurso", temasDelCurso);
            Map<Integer, Map<String, Boolean>> contenido = new LinkedHashMap<>();
            temasDelCurso.forEach(t -> contenido.put(t.getIdTema(), cursoService.buscarContenidoDeTema(curso, t).celdas()));
            model.addAttribute("contenidoPorTema", contenido);
            model.addAttribute("columnasContenido", cursoService.buscarColumnasDeContenido(curso));
            // Lo que necesita el asistente de programas (se lee en la página como JSON)
            List<Map<String, Object>> temasJson = new java.util.ArrayList<>();
            temasDelCurso.forEach(t -> temasJson.add(Map.of("id", t.getIdTema(), "titulo", t.getTitulo(),
                    "contenido", contenido.get(t.getIdTema()))));
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("cursoId", curso.getIdCurso());
            datos.put("duracionDias", curso.getDuracionDias());
            datos.put("minimoTemas", programaService.buscarMinimoDeContenido());
            datos.put("temas", temasJson);
            datos.put("columnas", cursoService.buscarColumnasDeContenido(curso));
            // Las cohortes pendientes de contenido a las que se asigna el programa que se registre (si el curso tiene sus datos completos)
            datos.put("cohortesPendientes", !curso.getFaltantesDeDatosAcademicos().isEmpty() ? List.of()
                    : curso.getCohortes().stream().filter(c -> !c.getBaja() && c.esPendiente() && !c.tienePrograma() && c.getCantidadInscriptos() == 0)
                            .sorted(java.util.Comparator.comparing(com.app.idoneos.model.Cohorte::getFechaInicioInscripcion))
                            .map(com.app.idoneos.model.Cohorte::getMesAnio).toList());
            model.addAttribute("datos", datos);
            model.addAttribute("abrirModal", !esAdministrador && (nuevo != null || editar != null) ? "modalPrograma" : null);
            model.addAttribute("programaAEditar", editar);
            model.addAttribute("nombreBusqueda", nombre);
            model.addAttribute("estadoSeleccionado", estado);
            model.addAttribute("puedeGestionar", !esAdministrador);
            model.addAttribute("titulo", "Programas | Idóneos Online");
            return "pages/programa/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /programa/registrar: Registra un programa para un curso, opcionalmente a partir de uno anterior. */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "programaOrigenId", required = false) Integer programaOrigenId,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "conCronograma", required = false) Boolean conCronograma,
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "dias", required = false) List<String> dias, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Programa programa = programaService.registrar(usuario.getDocente(), cursoId, programaOrigenId,
                    nombre, descripcion, cronogramaRecibido(conCronograma, ids), dias);
            String mensaje = "Programa '" + programa.getNombre() + "' registrado con éxito.";
            // Alerta guía: registrar los temas, si todavía no llegan al mínimo, o armar el cronograma
            return panel.exitoConPasoSiguiente(mensaje, programa.getCurso().getIdCurso(), usuario.getDocente());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /programa/modificar/{id}: Modifica un programa sin inscripciones activas. */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "conCronograma", required = false) Boolean conCronograma,
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "dias", required = false) List<String> dias, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            programaService.modificar(id, usuario.getDocente(), nombre, descripcion,
                    cronogramaRecibido(conCronograma, ids), dias);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Programa modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Obtiene los temas del cronograma que envió el asistente, con conCronograma y sin temas. */
    private static List<Integer> cronogramaRecibido(Boolean conCronograma, List<Integer> ids) {
        if (!Boolean.TRUE.equals(conCronograma)) {
            return ids;
        }
        return ids == null ? List.of() : ids;
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /programa/darDeBaja/{id}: Da de baja un programa sin cohortes activas. */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            // El administrador puede dar de baja los programas de cualquier curso
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            programaService.darDeBaja(id, docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Programa dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /programa/darDeBajaMasiva: Da de baja varios programas a la vez, todos o ninguno. */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            programaService.darDeBajaVarios(ids, docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Programas dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ----------------------------------------------------------------------
        // Listado (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Programa> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Programa>("nombre", FiltroUtil.Tipo.TEXTO, Programa::getNombre),
                new FiltroUtil.Columna<Programa>("descripcion", FiltroUtil.Tipo.TEXTO, Programa::getDescripcion),
                new FiltroUtil.Columna<Programa>("anterior", FiltroUtil.Tipo.TEXTO,
                        p -> p.getProgramaAnterior() == null ? null : p.getProgramaAnterior().getNombre()),
                new FiltroUtil.Columna<Programa>("temas", FiltroUtil.Tipo.NUMERO, p -> String.valueOf(p.getCantidadTemas())),
                new FiltroUtil.Columna<Programa>("ultimaModificacion", FiltroUtil.Tipo.FECHA, p -> FiltroUtil.formatearFechaHora(p.getUltimaModificacion())),
                new FiltroUtil.Columna<Programa>("fechaCreacion", FiltroUtil.Tipo.FECHA, p -> FiltroUtil.formatearFechaHora(p.getFechaCreacion()))),
                Programa::getBaja);
    }
}
