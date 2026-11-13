package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.service.curso.CursoService;
import com.app.idoneos.service.inscripcion.InscripcionService;

import com.app.idoneos.util.PaginacionUtil;
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

import com.app.idoneos.service.alumno.AlumnoService;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.pago.PagoService;
import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Descuento;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Controlador de la inscripción. */
@Controller
@RequestMapping("/inscripcion")
public class InscripcionController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private PagoService pagoService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Lista los cursos en los que el alumno está inscripto, con su progreso general. */
    private String buscarPorAlumno(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            List<Inscripcion> todas = inscripcionService.buscarMisCursos(alumno, null, null);
            // El filtro (también el de la URL) y la página los resuelve el servidor: cards, 8 por página
            PaginacionUtil.Pagina<Inscripcion> pagina = Filtros.cards.paginar(todas, request.getParameterMap(), 8,
                    java.util.Comparator.comparing(i -> i.getCurso().getCategoria().getNombre(), String.CASE_INSENSITIVE_ORDER));
            model.addAttribute("agrupar", true);
            model.addAttribute("chipsCategorias", Filtros.cards.armarChips(todas, request.getParameterMap(), "categoriaId",
                    i -> String.valueOf(i.getCurso().getCategoria().getIdCategoria()), i -> i.getCurso().getCategoria().getNombre()));
            model.addAttribute("cursosPorCategoria", Filtros.cards.contar(todas, request.getParameterMap(),
                    i -> i.getCurso().getCategoria().getIdCategoria()));
            PaginacionUtil.cargarEnModelo(model, pagina);

            model.addAttribute("inscripciones", pagina.filas());
            model.addAttribute("grupos", com.app.idoneos.util.AgrupacionUtil.agrupar(pagina.filas(), i -> i.getCurso().getCategoria()));
            model.addAttribute("totalInscripciones", todas.size());
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/inscripcion/buscarPorAlumno :: filas"; // solo las cards de la página pedida
            }
            model.addAttribute("busqueda", busqueda);
            // En los filtros solo se ofrecen los estados que tienen las inscripciones del alumno (en el orden del ciclo de vida)
            List<String> ordenEstados = List.of(com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO,
                    com.app.idoneos.model.EstadoInscripcion.PAGO_EN_CURSO, com.app.idoneos.model.EstadoInscripcion.HABILITADA,
                    com.app.idoneos.model.EstadoInscripcion.SUSPENDIDA, com.app.idoneos.model.EstadoInscripcion.VENCIDA,
                    com.app.idoneos.model.EstadoInscripcion.CANCELADA);
            List<String> ordenAvances = List.of(Inscripcion.AVANCE_SIN_AVANCE, com.app.idoneos.model.EstadoAvance.EN_PROGRESO,
                    com.app.idoneos.model.EstadoAvance.FINALIZADO);
            model.addAttribute("estadosDisponibles", ordenEstados.stream()
                    .filter(e -> todas.stream().anyMatch(i -> e.equals(i.getNombreEstado()))).toList());
            model.addAttribute("avancesDisponibles", ordenAvances.stream()
                    .filter(e -> todas.stream().anyMatch(i -> e.equals(i.getNombreAvance()))).toList());
            model.addAttribute("estadoInscripcionSeleccionado", request.getParameter("estadoInscripcion"));
            model.addAttribute("avanceSeleccionado", request.getParameter("avance"));
            model.addAttribute("titulo", "Mis cursos | Idóneos Online");
            return "pages/inscripcion/buscarPorAlumno";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inicio";
        }
    }

    /** GET /inscripcion/buscar: Lista las inscripciones activas de todos los alumnos (vista del Administrador). */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        if (!((Usuario) authentication.getPrincipal()).esAdministradorActivo()) {
            return buscarPorAlumno(busqueda, estado, model, authentication, redirectAttributes, request);
        }
        // Con las dadas de baja: siguen en el listado (sin acciones) para poder ver sus observaciones
        List<Inscripcion> inscripciones = inscripcionService.buscarTodasConBajas();
        // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
        PaginacionUtil.Pagina<Inscripcion> pagina = Filtros.tabla.paginar(inscripciones, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("inscripciones", pagina.filas());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/inscripcion/buscarPorAdministrador :: filas"; // solo las filas de la página pedida
        }
        // Cursos con inscripciones (filtro) y cursos con cohortes abiertas (alta manual), sin repetir
        model.addAttribute("cursosFiltro", inscripciones.stream().map(Inscripcion::getCurso).distinct()
                .sorted(Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER)).toList());
        model.addAttribute("cohortesFiltro", inscripciones.stream().map(Inscripcion::getCohorte).distinct()
                .sorted(Comparator.comparing((Cohorte c) -> c.getPrograma().getCurso().getNombre(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(c -> c.getPrograma().getNombre(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Cohorte::getFechaInicioInscripcion))
                .toList());
        List<Curso> cursosAbiertos = cursoService.buscarEnCatalogo(null, null, null, null, null);
        model.addAttribute("cohortesAbiertas", cursosAbiertos.stream()
                .flatMap(curso -> cursoService.buscarCohortesAbiertas(curso).stream()).toList());
        model.addAttribute("alumnos", alumnoService.obtenerTodo().stream()
                .filter(alumno -> !alumno.getUsuario().esInactivo())
                .sorted(Comparator.comparing(alumno -> alumno.getUsuario().getNombreCompleto(),
                        String.CASE_INSENSITIVE_ORDER))
                .toList());
        // Descuento que le corresponde hoy a cada alumno, para el recuadro de pago del formulario de alta
        Map<Integer, Descuento> descuentosPorAlumno = new HashMap<>();
        for (Alumno alumno : alumnoService.obtenerTodo()) {
            pagoService.calcularDescuentoPara(alumno)
                    .ifPresent(descuento -> descuentosPorAlumno.put(alumno.getIdAlumno(), descuento));
        }
        model.addAttribute("descuentosPorAlumno", descuentosPorAlumno);
        model.addAttribute("titulo", "Inscripciones | Idóneos Online");
        model.addAttribute("menuActivo", "inscripciones");
        return "pages/inscripcion/buscarPorAdministrador";
    }

    /** GET /inscripcion/acceder/{idInscripcion}: Permite al alumno acceder a un curso en el que está inscripto. */
    @GetMapping("/acceder/{idInscripcion}")
    public String accederCurso(@PathVariable("idInscripcion") int idInscripcion,
            @RequestParam(value = "seccion", defaultValue = "temas") String seccion, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionService.validarAcceso(idInscripcion, alumno);
            String seccionActiva = List.of("temas", "cronograma").contains(seccion) ? seccion : "temas";

            panel.cargarContexto(model, ContextoCurso.deAlumno(inscripcion),
                    "temas".equals(seccionActiva) ? "curso" : seccionActiva);
            panel.cargarDiasMaximosDelCronograma(model, inscripcion.getCohorte().getPrograma().getCurso());
            model.addAttribute("inscripcion", inscripcion);
            model.addAttribute("cohorte", inscripcion.getCohorte());
            // Temas del cronograma, para el board de solo lectura de la sección "cronograma"
            model.addAttribute("temasDelCronograma", inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()
                    .stream().map(TemaCronograma::getTema).toList());
            model.addAttribute("seccion", seccionActiva);
            model.addAttribute("titulo", inscripcion.getCurso().getNombre() + " | Idóneos Online");
            return "pages/curso/acceder";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/buscar";
        }
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /inscripcion/inscribir: El alumno se inscribe a una cohorte con inscripción abierta. */
    @PostMapping("/inscribir")
    public ResponseEntity<Map<String, String>> inscribir(
            @RequestParam(value = "cohorteId", required = false) Integer cohorteId,
            @RequestParam(value = "terminos", defaultValue = "false") boolean terminos, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Inscripcion inscripcion = inscripcionService.inscribir(usuario.getAlumno(), cohorteId, terminos);
            if (!inscripcion.fueHabilitada()) {
                // Con costo: sin alerta de éxito (sugeriría que el lugar está asegurado). La caja avisa que falta el pago
                return RespuestaFormularioUtil.armarRespuestaDeRedireccion(
                        "/pago/caja/" + inscripcion.getIdInscripcion() + "?inscripcionNueva");
            }
            return RespuestaFormularioUtil.armarRespuestaExitosa(
                    "Te inscribiste a «" + inscripcion.getCurso().getNombre() + "». Ya podés acceder desde Mis cursos.",
                    "/inscripcion/buscar");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /inscripcion/registrar: El administrador registra manualmente la inscripción de un alumno. */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "alumnoId", required = false) Integer alumnoId,
            @RequestParam(value = "cohorteId", required = false) Integer cohorteId,
            @RequestParam(value = "terminos", defaultValue = "false") boolean terminos) {
        try {
            Inscripcion inscripcion = inscripcionService.registrar(alumnoId, cohorteId, terminos);
            String mensaje = "Inscripción de " + inscripcion.getAlumno().getUsuario().getNombreCompleto()
                    + " a «" + inscripcion.getCurso().getNombre() + "» registrada con éxito."
                    + (inscripcion.fueHabilitada() ? "" : " Queda pendiente de pago: registrá ahora su pago.");
            return inscripcion.fueHabilitada() ? RespuestaFormularioUtil.armarRespuestaExitosa(mensaje)
                    : RespuestaFormularioUtil.armarRespuestaExitosa(mensaje, "/pago/buscar?registrarPara=" + inscripcion.getIdInscripcion());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /inscripcion/modificar/{id}: El administrador modifica las observaciones y la suspensión del acceso (el vencimiento no se modifica). */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "observaciones", required = false) String observaciones,
            @RequestParam(value = "habilitada", required = false) Boolean habilitada) {
        try {
            inscripcionService.modificar(id, observaciones, habilitada);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Inscripción actualizada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /inscripcion/darDeBaja/{id}: Da de baja una inscripción: el alumno la suya (abandono) o el administrador cualquiera. */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id,
            @RequestParam(value = "motivo", required = false) String motivo, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            inscripcionService.darDeBaja(id, usuario.esAdministradorActivo() ? null : usuario.getAlumno(), motivo);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Inscripción dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /inscripcion/darDeBajaMasiva: Da de baja varias inscripciones a la vez (solo el Administrador). */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarias(
            @RequestParam(value = "ids", required = false) List<Integer> ids) {
        try {
            inscripcionService.darDeBajaVarias(ids);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Inscripciones dadas de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Ventana de modificación a pedido
    // ----------------------------------------------------------------------

    /** GET /inscripcion/modificar/{id}: Ventana de modificación de una inscripción. */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("i", inscripcionService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra disponible.")));
        return "pages/inscripcion/modificar :: modal(i=${i})";
    }

    /** GET /inscripcion/modalCertificado/{id}: Ventana de corrección de los datos del certificado de una inscripción, que el listado pide al abrirla. */
    @GetMapping("/modalCertificado/{id}")
    public String ventanaCertificado(@PathVariable("id") Integer id, Model model) {
        com.app.idoneos.model.Inscripcion i = inscripcionService.buscarPorId(id)
                .filter(x -> x.getCertificadoVigente() != null)
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no tiene un certificado vigente."));
        model.addAttribute("i", i);
        return "pages/certificado/corregir :: modal(i=${i})";
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ----------------------------------------------------------------------
        // Listado del administrador (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Inscripcion> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Inscripcion>("alumno", FiltroUtil.Tipo.TEXTO,
                        i -> i.getAlumno().getUsuario().getNombreCompleto() + " " + i.getAlumno().getUsuario().getDni() + " "
                                + i.getAlumno().getUsuario().getCorreo()),
                new FiltroUtil.Columna<Inscripcion>("curso", FiltroUtil.Tipo.LISTA, i -> i.getCurso().getNombre()),
                new FiltroUtil.Columna<Inscripcion>("cohorte", FiltroUtil.Tipo.LISTA,
                        i -> i.getCohorte().getPrograma().getNombre() + " · " + i.getCohorte().getEtiqueta()),
                new FiltroUtil.Columna<Inscripcion>("estado", FiltroUtil.Tipo.LISTA, Inscripcion::getNombreEstado),
                new FiltroUtil.Columna<Inscripcion>("avance", FiltroUtil.Tipo.LISTA, Inscripcion::getNombreAvance),
                new FiltroUtil.Columna<Inscripcion>("observaciones", FiltroUtil.Tipo.TEXTO,
                        i -> i.getObservaciones() == null ? "" : i.getObservaciones()),
                new FiltroUtil.Columna<Inscripcion>("certificado", FiltroUtil.Tipo.TEXTO,
                        i -> i.getCertificadoVigente() == null ? "" : i.getCertificadoVigente().getNumero()),
                new FiltroUtil.Columna<Inscripcion>("fechaVencimiento", FiltroUtil.Tipo.FECHA,
                        i -> FiltroUtil.formatearFecha(i.getFechaVencimientoAcceso())),
                new FiltroUtil.Columna<Inscripcion>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        i -> FiltroUtil.formatearFechaHora(i.getFechaCreacion()))),
                Inscripcion::esInactivo);

        /** Define las columnas de "Mis cursos" del alumno, que se filtran en el servidor. */
        public static final FiltroUtil<Inscripcion> cards = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Inscripcion>("busqueda", FiltroUtil.Tipo.TEXTO,
                        i -> i.getCurso().getNombre() + " " + (i.getCurso().getDescripcion() == null ? "" : i.getCurso().getDescripcion())),
                new FiltroUtil.Columna<Inscripcion>("categoriaId", FiltroUtil.Tipo.LISTA,
                        i -> String.valueOf(i.getCurso().getCategoria().getIdCategoria())),
                new FiltroUtil.Columna<Inscripcion>("estadoInscripcion", FiltroUtil.Tipo.LISTA, Inscripcion::getNombreEstado),
                new FiltroUtil.Columna<Inscripcion>("avance", FiltroUtil.Tipo.LISTA, Inscripcion::getNombreAvance)),
                i -> false);
    }
}
