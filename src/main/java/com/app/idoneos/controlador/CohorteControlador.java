package com.app.idoneos.controlador;

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

import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Cohorte.CohorteServicioImpl;
import com.app.idoneos.servicio.DocenteCurso.DocenteCursoServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;

import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Controlador de la gestión de cohortes (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso:
 * CU-11 Buscar cohorte (GET /cohorte/buscar), CU-12 Registrar cohorte, CU-13 Modificar cohorte
 * y CU-14 Dar de baja cohorte. Los formularios de alta, modificación y baja se muestran como ventanas modales
 * de la pantalla de búsqueda.
 */
@Controller
@RequestMapping("/cohorte")
public class CohorteControlador extends PanelCursoControlador {

    @Autowired
    private CohorteServicioImpl cohorteServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    @Autowired
    private DocenteCursoServicioImpl docenteCursoServicio;

    @Autowired
    private CursoServicioImpl cursoServicioImpl;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-11: Busca cohortes de un programa según su estado (Abierta, En dictado, Finalizada) y un rango
     * de fechas de inscripción. Si el usuario es docente (y no administrador), se restringe a los cursos
     * en los que participa como titular o ayudante.
     *
     * @param cursoId    Identificador del curso. El administrador sin curso ve las cohortes de todos los cursos.
     * @param programaId Identificador del programa.
     * @param nuevo      Si está presente, se abre el alta de cohorte (con el curso elegido) al cargar la pantalla.
     * @param busqueda   Parte del nombre del curso o del programa.
     * @param estado     Estado de la cohorte.
     * @param desde      Fecha desde la que la inscripción debe estar abierta.
     * @param hasta      Fecha hasta la que la inscripción debe estar abierta.
     * @param orden      Orden de los resultados: "recientes" o "curso" (A–Z). Las dadas de baja van al final.
     * @param modelo     El modelo de la vista.
     * @param auth       La autenticación actual.
     * @return La vista de búsqueda de cohortes.
     */
    @GetMapping("/buscar")
    public String buscarCohortes(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(value = "orden", defaultValue = "recientes") String orden,
            Model modelo, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        boolean esAdministrador = usuario.esAdministradorActivo();
        Docente soloDeDocente = esAdministrador ? null : usuario.getDocente();

        // El administrador ve las cohortes de todos los cursos, o las de un curso si entra desde su panel
        Curso curso = null;
        if (esAdministrador && cursoId != null) {
            try {
                curso = contextoDelPanel(cursoId, usuario).curso();
            } catch (IllegalArgumentException e) {
                return "redirect:/curso/misCursos";
            }
        }
        final Curso cursoDelPanel = curso;

        List<Cohorte> cohortes = cohorteServicio.buscarConFiltros(
                // Listado completo: los filtros (también los que llegan por la URL) se aplican en la pantalla
                null, null, null, null, null, orden, soloDeDocente).stream()
                .filter(cohorte -> cursoDelPanel == null
                        || cohorte.getCurso().getIdCurso() == cursoDelPanel.getIdCurso())
                .toList();
        Map<Integer, List<Inscripcion>> inscripcionesPorCohorte = new HashMap<>();
        Map<Integer, Long> clasesPorCohorte = new HashMap<>();
        for (Cohorte cohorte : cohortes) {
            inscripcionesPorCohorte.put(cohorte.getIdCohorte(),
                    cohorte.getInscripciones().stream().filter(inscripcion -> !inscripcion.esInactivo()).toList());
            clasesPorCohorte.put(cohorte.getIdCohorte(),
                    cohorte.getClasesEnVivo().stream().filter(clase -> !clase.getBaja()).count());
        }

        // Cohortes que el docente tiene como contexto de trabajo por defecto
        Set<Integer> cohortesDeTrabajo = new HashSet<>();
        if (soloDeDocente != null) {
            docenteCursoServicio.buscarPorDocente(soloDeDocente).stream()
                    .filter(participacion -> participacion.getCohortePorDefecto() != null)
                    .forEach(participacion -> cohortesDeTrabajo.add(participacion.getCohortePorDefecto().getIdCohorte()));
        }

        modelo.addAttribute("cohortes", cohortes);
        modelo.addAttribute("inscripcionesPorCohorte", inscripcionesPorCohorte);
        modelo.addAttribute("clasesPorCohorte", clasesPorCohorte);
        modelo.addAttribute("cohortesDeTrabajo", cohortesDeTrabajo);
        modelo.addAttribute("programas", curso == null ? programaServicio.obtenerTodo() : curso.getProgramasActivos());
        modelo.addAttribute("programaSeleccionado", programaId);
        modelo.addAttribute("cursoSeleccionado", curso == null ? null : curso.getIdCurso());
        modelo.addAttribute("abrirAlta", nuevo != null);
        modelo.addAttribute("plazoMinimoContenidoDias", cohorteServicio.plazoMinimoParaCompletarContenidoDias());
        // Qué le falta al contenido de cada programa para poder abrir la inscripción (el formulario avisa si la cohorte queda pendiente)
        Map<Integer, List<String>> faltantesPorPrograma = new HashMap<>();
        for (var programa : programaServicio.obtenerTodo()) {
            faltantesPorPrograma.put(programa.getIdPrograma(), cohorteServicio.faltantesDeContenido(programa));
        }
        modelo.addAttribute("faltantesPorPrograma", faltantesPorPrograma);
        modelo.addAttribute("faltantesSinPrograma", cohorteServicio.faltantesDeContenido(null));
        modelo.addAttribute("busqueda", busqueda);
        modelo.addAttribute("estadoSeleccionado", estado);
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        modelo.addAttribute("ordenSeleccionado", orden);
        modelo.addAttribute("accesoDiasMaximos", Cohorte.DIAS_ACCESO_MAXIMOS);
        modelo.addAttribute("cupoMaximoPermitido", Cohorte.CUPO_MAXIMO_PERMITIDO);
        modelo.addAttribute("titulo", "Cohortes | Idóneos Online");
        if (esAdministrador) {
            modelo.addAttribute("cursos", cursoServicioImpl.obtenerTodo());
            if (curso != null) {
                cargarContexto(modelo, ContextoCurso.deAdministrador(curso), "cohortes");
            } else {
                modelo.addAttribute("menuActivo", "cohortes");
            }
            return "pages/cohorte/buscarAdministrador";
        }
        modelo.addAttribute("programasDelCurso", cohortes.stream().filter(c -> c.esPendiente() && !c.tienePrograma())
                .collect(java.util.stream.Collectors.toMap(Cohorte::getIdCohorte,
                        c -> c.getCurso().getProgramasActivos())));
        return "pages/cohorte/buscarDocente";
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-12: Registra una cohorte de un curso, con o sin programa. Nace Pendiente si todavía no tiene un programa con el
     * contenido completo. Responde en JSON para que el formulario muestre el resultado sin recargar la página.
     *
     * @param cursoId                Identificador del curso.
     * @param programaId             Identificador del programa (opcional).
     * @param fechaInicioInscripcion Fecha de inicio de la inscripción.
     * @param fechaFinInscripcion    Fecha de fin de la inscripción.
     * @param fechaInicioDictado     Fecha de inicio del dictado (si el curso tiene modalidad En vivo).
     * @param accesoDias          Días de acceso al contenido (opcional: si no se indica, la duración del curso más el tiempo extra).
     * @param cupoMaximo             Cupo máximo de inscriptos (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarCohorte(
            @RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "fechaInicioInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioInscripcion,
            @RequestParam(value = "fechaFinInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinInscripcion,
            @RequestParam(value = "fechaInicioDictado", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDictado,
            @RequestParam(value = "accesoDias", required = false) Integer accesoDias,
            @RequestParam(value = "tiempoExtra", required = false) Integer tiempoExtra,
            @RequestParam(value = "cupoMaximo", required = false) Integer cupoMaximo) {
        try {
            if (cursoId == null) {
                throw RespuestaFormularioUtilidad.campoInvalido("cursoId", "Error! Debe seleccionar el curso de la cohorte.");
            }
            Cohorte cohorte = cohorteServicio.registrarCohorte(cursoId, programaId, fechaInicioInscripcion,
                    fechaFinInscripcion, fechaInicioDictado,
                    accesoDias != null ? accesoDias : accesoConTiempoExtra(cursoServicio.buscarActivoPorId(cursoId), tiempoExtra),
                    cupoMaximo);
            return RespuestaFormularioUtilidad.respuestaExitosa(cohorte.esPendiente()
                    ? "Cohorte registrada como pendiente: se activa cuando el contenido del programa esté completo."
                    : "Cohorte registrada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-11: Cambia el contexto de trabajo del docente a la cohorte seleccionada (la establece como
     * cohorte y programa por defecto en su participación en el curso).
     *
     * @param id                 Identificador de la cohorte.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al listado de cohortes.
     */
    @PostMapping("/cambiarContexto/{id}")
    public String cambiarContexto(@PathVariable("id") Integer id, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Cohorte cohorte = cohorteServicio.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa."));
            cohorteServicio.cambiarContextoDeTrabajo(cohorte, usuario.getDocente());
            redirectAttributes.addFlashAttribute("mensaje", "Ahora trabajás sobre la cohorte seleccionada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cohorte/buscar";
    }

    /**
     * El docente elige el programa de una cohorte pendiente que el administrador registró sin programa. Responde en JSON.
     *
     * @param id         Identificador de la cohorte.
     * @param programaId Identificador del programa (debe ser del curso de la cohorte).
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/asignarPrograma/{id}")
    public ResponseEntity<Map<String, String>> asignarPrograma(@PathVariable("id") Integer id,
            @RequestParam(value = "programaId", required = false) Integer programaId, Authentication auth) {
        try {
            if (programaId == null) {
                throw RespuestaFormularioUtilidad.campoInvalido("programaId", "Error! Debe seleccionar el programa de la cohorte.");
            }
            Usuario usuario = (Usuario) auth.getPrincipal();
            Cohorte cohorte = cohorteServicio.asignarPrograma(id, programaId, usuario.getDocente());
            return RespuestaFormularioUtilidad.respuestaExitosa(cohorte.esPendiente()
                    ? "Programa asignado. La cohorte sigue pendiente hasta que el contenido del programa esté completo."
                    : "Programa asignado. La cohorte quedó activa.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Guarda la cohorte elegida como cohorte por defecto del docente (junto con su programa) en su participación en
     * el curso. Responde en JSON (lo usa el selector de la vista del curso).
     *
     * @param id   Identificador de la cohorte.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/establecerPorDefecto/{id}")
    public ResponseEntity<Map<String, String>> establecerPorDefecto(@PathVariable("id") Integer id,
            Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Cohorte cohorte = cohorteServicio.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa."));
            cohorteServicio.cambiarContextoDeTrabajo(cohorte, usuario.getDocente());
            return RespuestaFormularioUtilidad.respuestaExitosa("Ahora trabajás sobre la cohorte seleccionada.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-13: Modifica una cohorte activa. Responde en JSON.
     *
     * @param id                     Identificador de la cohorte.
     * @param fechaInicioInscripcion Fecha de inicio de la inscripción.
     * @param fechaFinInscripcion    Fecha de fin de la inscripción.
     * @param fechaInicioDictado     Fecha de inicio del dictado (si el curso tiene modalidad En vivo).
     * @param accesoDias          Días de acceso al contenido (opcional: si no se indica, la duración del curso más el tiempo extra).
     * @param cupoMaximo             Cupo máximo de inscriptos (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarCohorte(@PathVariable("id") Integer id,
            @RequestParam(value = "fechaInicioInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioInscripcion,
            @RequestParam(value = "fechaFinInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinInscripcion,
            @RequestParam(value = "fechaInicioDictado", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDictado,
            @RequestParam(value = "accesoDias", required = false) Integer accesoDias,
            @RequestParam(value = "tiempoExtra", required = false) Integer tiempoExtra,
            @RequestParam(value = "cupoMaximo", required = false) Integer cupoMaximo) {
        try {
            if (accesoDias == null) {
                accesoDias = accesoConTiempoExtra(cohorteServicio.buscarPorId(id)
                        .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa.")).getCurso(), tiempoExtra);
            }
            cohorteServicio.modificarCohorte(id, fechaInicioInscripcion, fechaFinInscripcion, fechaInicioDictado,
                    accesoDias, cupoMaximo);
            return RespuestaFormularioUtilidad.respuestaExitosa("Cohorte modificada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Calcula los días de acceso de una cohorte: la duración del curso más el tiempo extra para quien se atrase.
     *
     * @param curso       El curso de la cohorte.
     * @param tiempoExtra El tiempo extra, en días (opcional).
     * @return Los días de acceso.
     */
    private static int accesoConTiempoExtra(Curso curso, Integer tiempoExtra) {
        return curso.getDuracionDias() + (tiempoExtra == null ? 0 : tiempoExtra);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-14: Da de baja una cohorte que no tenga inscripciones ni clases en vivo activas, y la desvincula
     * de los docentes que la tienen asignada por defecto. Responde en JSON.
     *
     * @param id Identificador de la cohorte.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaCohorte(@PathVariable("id") Integer id) {
        try {
            cohorteServicio.darDeBajaCohorte(id);
            return RespuestaFormularioUtilidad.respuestaExitosa("Cohorte dada de baja correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Da de baja varios cohortes a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja
     * ninguno. Responde en JSON.
     *
     * @param ids Identificadores de los cohortes seleccionados.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids) {
        try {
            cohorteServicio.darDeBajaVarios(ids);
            return RespuestaFormularioUtilidad.respuestaExitosa("Cohortes dadas de baja correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }
}
