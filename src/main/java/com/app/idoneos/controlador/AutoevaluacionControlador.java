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

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Autoevaluacion.AutoevaluacionServicio;
import com.app.idoneos.servicio.Autoevaluacion.AutoevaluacionServicio.SolicitudAutoevaluacion;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.IntentoAutoevaluacion.IntentoAutoevaluacionServicio;
import com.app.idoneos.servicio.Pool.PoolServicio;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

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
public class AutoevaluacionControlador extends PanelCursoControlador {

    /**
     * Los pools de una unidad del programa, para ofrecerlos agrupados en el selector del formulario.
     *
     * @param numeroOrden número de orden de la unidad en el cronograma
     * @param titulo      título de la unidad
     * @param pools       los pools de la unidad
     */
    public record GrupoDePools(int numeroOrden, String titulo, List<Pool> pools) {
    }

    @Autowired
    private AutoevaluacionServicio autoevaluacionServicio;

    @Autowired
    private PoolServicio poolServicio;

    @Autowired
    private IntentoAutoevaluacionServicio intentoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-64: Lista las autoevaluaciones de un curso con sus pools, tiempo límite e intentos permitidos. Desde el listado
     * el docente las crea y las modifica en ventanas.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarAutoevaluaciones(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            boolean esAdministrador = contexto.esAdministrador();
            Docente docente = esAdministrador ? null : usuario.getDocente();
            Programa programa = contexto.programa();
            List<UnidadCronograma> unidades = programa == null ? List.of() : programa.getCronogramaOrdenado();
            // Listado completo: los filtros se aplican en la pantalla
            List<Autoevaluacion> autoevaluaciones = autoevaluacionServicio.buscarConFiltros(cursoId, null, null, false, docente);
            List<Pool> pools = poolServicio.buscarConFiltros(cursoId, null, null, false, docente);

            // Para cada pool, las unidades cuyas autoevaluaciones pueden usarlo (el formulario filtra con esto)
            Map<Integer, String> unidadesPorPool = new LinkedHashMap<>();
            for (Pool pool : pools) {
                unidadesPorPool.put(pool.getIdPool(), String.join(",", autoevaluacionServicio.unidadesQuePuedenUsar(pool)
                        .stream().map(u -> String.valueOf(u.getIdUnidad())).toList()));
            }

            // Los pools que se ofrecen, agrupados por la unidad a la que pertenecen (en el orden del cronograma)
            List<GrupoDePools> gruposDePools = new ArrayList<>();
            for (UnidadCronograma cronograma : unidades) {
                List<Pool> deLaUnidad = pools.stream()
                        .filter(p -> p.getUnidad().getIdUnidad() == cronograma.getUnidad().getIdUnidad()).toList();
                if (!deLaUnidad.isEmpty()) {
                    gruposDePools.add(new GrupoDePools(cronograma.getNumeroOrden(), cronograma.getUnidad().getTitulo(), deLaUnidad));
                }
            }

            cargarContexto(modelo, contexto, "autoevaluaciones");
            modelo.addAttribute("gruposDePools", gruposDePools);
            modelo.addAttribute("autoevaluaciones", autoevaluaciones);
            modelo.addAttribute("unidades", unidades);
            modelo.addAttribute("pools", pools);
            modelo.addAttribute("unidadesPorPool", unidadesPorPool);
            modelo.addAttribute("puedeGestionar", !esAdministrador);
            modelo.addAttribute("titulo", "Autoevaluaciones | Idóneos Online");
            return "pages/autoevaluacion/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * CU-27 (flujo del alumno) y CU-70: Ficha de la autoevaluación para el alumno: sus reglas, el historial de sus intentos
     * (con la mejor nota) y el botón para realizar un intento, continuar el que tiene en curso o el motivo por el que no puede
     * rendir ahora.
     *
     * @param id                 Identificador de la autoevaluación.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de la ficha, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/ver/{id}")
    public String verAutoevaluacion(@PathVariable("id") Integer id, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            Autoevaluacion autoevaluacion = autoevaluacionServicio.buscarPorId(id).filter(Autoevaluacion::esVisible)
                    .orElseThrow(() -> new IllegalArgumentException("Error! La autoevaluación no se encuentra disponible."));
            ContextoCurso contexto = contextoDeLaUnidad(autoevaluacion.getUnidad(), usuario);
            Inscripcion inscripcion = inscripcionServicio.buscarConAccesoAUnidad(alumno, autoevaluacion.getUnidad())
                    .orElseThrow(() -> new IllegalArgumentException("Error! Todavía no podés acceder a esta unidad."));
            List<IntentoAutoevaluacion> intentos = intentoServicio.buscarPorInscripcionYAutoevaluacion(inscripcion,
                    autoevaluacion);
            intentos.forEach(intentoServicio::cerrarSiVencido); // si se le agotó el tiempo, queda corregido
            intentos = intentoServicio.buscarPorInscripcionYAutoevaluacion(inscripcion, autoevaluacion);

            cargarContexto(modelo, contexto, "autoevaluaciones");
            modelo.addAttribute("autoevaluacion", autoevaluacion);
            modelo.addAttribute("inscripcion", inscripcion);
            modelo.addAttribute("intentos", intentos);
            modelo.addAttribute("enCurso", intentos.stream().filter(IntentoAutoevaluacion::estaEnCurso).findFirst().orElse(null));
            modelo.addAttribute("motivo", intentoServicio.motivoNoDisponible(alumno, autoevaluacion).orElse(null));
            modelo.addAttribute("mejorNota", autoevaluacion.mejorNota(inscripcion));
            modelo.addAttribute("restantes", autoevaluacion.intentosRestantes(inscripcion));
            modelo.addAttribute("titulo", autoevaluacion.getNombre() + " | Idóneos Online");
            return "pages/autoevaluacion/ver";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-65: Registra una autoevaluación asociada a uno o más pools. Responde en JSON.
     *
     * @param unidadId           Identificador de la unidad.
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
     * @param auth               La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarAutoevaluacion(
            @RequestParam(value = "unidadId", required = false) Integer unidadId,
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
            @RequestParam(value = "idsPools", required = false) List<Integer> idsPools, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Autoevaluacion autoevaluacion = autoevaluacionServicio.registrarAutoevaluacion(usuario.getDocente(), unidadId,
                    new SolicitudAutoevaluacion(nombre, tiempoLimite, cantidadPreguntas, notaMinima, intentosPermitidos,
                            fechaApertura, fechaCierre, descuentoPorError, navegacionLibre, permiteSalir, oculto, idsPools));
            return RespuestaFormularioUtilidad
                    .respuestaExitosa("Autoevaluación '" + autoevaluacion.getNombre() + "' registrada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
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
     * @param auth               La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarAutoevaluacion(@PathVariable("id") Integer id,
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
            @RequestParam(value = "idsPools", required = false) List<Integer> idsPools, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            autoevaluacionServicio.modificarAutoevaluacion(id, usuario.getDocente(),
                    new SolicitudAutoevaluacion(nombre, tiempoLimite, cantidadPreguntas, notaMinima, intentosPermitidos,
                            fechaApertura, fechaCierre, descuentoPorError, navegacionLibre, permiteSalir, oculto, idsPools));
            return RespuestaFormularioUtilidad.respuestaExitosa("Autoevaluación modificada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-67: Da de baja una autoevaluación sin intentos. Responde en JSON.
     *
     * @param id   Identificador de la autoevaluación.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaAutoevaluacion(@PathVariable("id") Integer id,
            Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            // El administrador puede dar de baja las autoevaluaciones de cualquier curso
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            autoevaluacionServicio.darDeBajaAutoevaluacion(id, docente);
            return RespuestaFormularioUtilidad.respuestaExitosa("Autoevaluación dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Da de baja varias autoevaluaciones a la vez, todas o ninguna. Responde en JSON.
     *
     * @param ids  Identificadores de las autoevaluaciones seleccionadas.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarias(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            autoevaluacionServicio.darDeBajaVarias(ids, docente);
            return RespuestaFormularioUtilidad.respuestaExitosa("Autoevaluaciones dadas de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }
}
