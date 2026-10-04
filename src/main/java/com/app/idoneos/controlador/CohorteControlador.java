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

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Cohorte.CohorteServicioImpl;
import com.app.idoneos.servicio.ParticipacionDocente.ParticipacionDocenteServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;

import com.app.idoneos.utilidades.Utilidades;

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
public class CohorteControlador {

    @Autowired
    private CohorteServicioImpl cohorteServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    @Autowired
    private ParticipacionDocenteServicioImpl participacionDocenteServicio;

    /**
     * CU-11: Busca cohortes de un programa según su estado (Abierta, En dictado, Finalizada) y un rango
     * de fechas de inscripción. Si el usuario es docente (y no administrador), se restringe a los cursos
     * en los que participa como titular o ayudante.
     *
     * @param programaId Identificador del programa.
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
    public String buscarCohortes(@RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(value = "orden", defaultValue = "recientes") String orden,
            Model modelo, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        boolean esAdministrador = usuario.esAdministradorActivo();
        Docente soloDeDocente = esAdministrador ? null : usuario.getDocente();

        List<Cohorte> cohortes = cohorteServicio.buscarConFiltros(programaId, busqueda, estado, desde, hasta,
                orden, soloDeDocente);
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
            participacionDocenteServicio.buscarPorDocente(soloDeDocente).stream()
                    .filter(participacion -> participacion.getCohortePorDefecto() != null)
                    .forEach(participacion -> cohortesDeTrabajo.add(participacion.getCohortePorDefecto().getIdCohorte()));
        }

        modelo.addAttribute("cohortes", cohortes);
        modelo.addAttribute("inscripcionesPorCohorte", inscripcionesPorCohorte);
        modelo.addAttribute("clasesPorCohorte", clasesPorCohorte);
        modelo.addAttribute("cohortesDeTrabajo", cohortesDeTrabajo);
        modelo.addAttribute("programas", programaServicio.obtenerTodo());
        modelo.addAttribute("programaSeleccionado", programaId);
        modelo.addAttribute("busqueda", busqueda);
        modelo.addAttribute("estadoSeleccionado", estado);
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        modelo.addAttribute("ordenSeleccionado", orden);
        modelo.addAttribute("titulo", "Cohortes | Idóneos Online");
        if (esAdministrador) {
            modelo.addAttribute("menuActivo", "cohortes");
            return "pages/panel/cohortes";
        }
        return "pages/gestion/buscarCohortes";
    }

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
     * CU-12: Registra una cohorte para un programa activo. Responde en JSON para que el formulario muestre
     * el resultado sin recargar la página.
     *
     * @param programaId             Identificador del programa.
     * @param fechaInicioInscripcion Fecha de inicio de la inscripción.
     * @param fechaFinInscripcion    Fecha de fin de la inscripción.
     * @param fechaInicioDictado     Fecha de inicio del dictado (si el curso tiene modalidad En vivo).
     * @param fechaFinDictado        Fecha de fin del dictado (si el curso tiene modalidad En vivo).
     * @param semanasAcceso          Semanas de acceso al contenido desde la inscripción.
     * @param cupoMaximo             Cupo máximo de inscriptos (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarCohorte(
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "fechaInicioInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioInscripcion,
            @RequestParam(value = "fechaFinInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinInscripcion,
            @RequestParam(value = "fechaInicioDictado", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDictado,
            @RequestParam(value = "fechaFinDictado", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinDictado,
            @RequestParam(value = "semanasAcceso", required = false) Integer semanasAcceso,
            @RequestParam(value = "cupoMaximo", required = false) Integer cupoMaximo) {
        try {
            if (programaId == null) {
                throw new IllegalArgumentException("Error! Debe seleccionar el programa de la cohorte.");
            }
            cohorteServicio.registrarCohorte(programaId, fechaInicioInscripcion, fechaFinInscripcion,
                    fechaInicioDictado, fechaFinDictado, semanasAcceso, cupoMaximo);
            return Utilidades.respuestaExitosa("Cohorte registrada correctamente.");
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
        }
    }

    /**
     * CU-13: Modifica una cohorte activa. Responde en JSON.
     *
     * @param id                     Identificador de la cohorte.
     * @param fechaInicioInscripcion Fecha de inicio de la inscripción.
     * @param fechaFinInscripcion    Fecha de fin de la inscripción.
     * @param fechaInicioDictado     Fecha de inicio del dictado (si el curso tiene modalidad En vivo).
     * @param fechaFinDictado        Fecha de fin del dictado (si el curso tiene modalidad En vivo).
     * @param semanasAcceso          Semanas de acceso al contenido desde la inscripción.
     * @param cupoMaximo             Cupo máximo de inscriptos (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarCohorte(@PathVariable("id") Integer id,
            @RequestParam(value = "fechaInicioInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioInscripcion,
            @RequestParam(value = "fechaFinInscripcion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinInscripcion,
            @RequestParam(value = "fechaInicioDictado", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDictado,
            @RequestParam(value = "fechaFinDictado", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinDictado,
            @RequestParam(value = "semanasAcceso", required = false) Integer semanasAcceso,
            @RequestParam(value = "cupoMaximo", required = false) Integer cupoMaximo) {
        try {
            cohorteServicio.modificarCohorte(id, fechaInicioInscripcion, fechaFinInscripcion, fechaInicioDictado,
                    fechaFinDictado, semanasAcceso, cupoMaximo);
            return Utilidades.respuestaExitosa("Cohorte modificada correctamente.");
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
        }
    }

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
            return Utilidades.respuestaExitosa("Cohorte dada de baja correctamente.");
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
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
            return Utilidades.respuestaExitosa("Cohortes dadas de baja correctamente.");
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
        }
    }
}
