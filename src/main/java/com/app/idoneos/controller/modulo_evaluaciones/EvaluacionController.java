package com.app.idoneos.controller.modulo_evaluaciones;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;
import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;

import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.Evaluacion.EvaluacionService;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;
import com.app.idoneos.service.modulo_configuracion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * TRAZABILIDAD — Controller para la gestión de Pools de Preguntas, Autoevaluaciones e Intentos.
 *
 * MOD-F-04: Módulo de Evaluación y Progreso
 *   CU-53 — Buscar pool              → GET /docente/unidad/{unidadId}/pool
 *   CU-54 — Crear pool               → POST /docente/unidad/{unidadId}/pool/crear y POST /docente/pool/{poolId}/pregunta/agregar
 *   CU-55 — Modificar pool           → POST /docente/pool/{poolId}/modificar, POST /docente/pregunta/{id}/modificar,
 *                                       POST /docente/opcion/{id}/modificar, POST /docente/opcion/{id}/borrar,
 *                                       POST /docente/pregunta/{id}/borrar
 *   CU-56 — Dar de baja pool         → POST /docente/pool/{poolId}/baja (y POST /docente/pregunta/{id}/borrar)
 *   CU-57 — Buscar autoevaluación    → GET /docente/curso/{cursoId}/gestionar
 *   CU-61 — Buscar intento           → GET /evaluacion/{autoevaluacionId}/resultado
 *   CU-62 — Realizar intento         → GET /evaluacion/{autoevaluacionId}/rendir  (vista)
 *                                       POST /evaluacion/{autoevaluacionId}/enviar (envío y corrección)
 */
@Controller
public class EvaluacionController {

    @Autowired private EvaluacionService evaluacionService;
    @Autowired private IntentoService intentoService;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private IntentoAutoevaluacionRepository intentoRepository;

    // ─── DOCENTE — Pools de Preguntas (CU-53 a CU-56) ─────────────────────────

    /**
     * TRAZABILIDAD: CU-53 — Buscar pool.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La unidad existe.
     * Flujo paso 4: recupera el pool de preguntas de la unidad (si existe) con todas sus preguntas.
     * NOTA PARCIAL: CU-53 especifica filtro por nombre. No implementado.
     */
    @GetMapping("/docente/unidad/{unidadId}/pool")
    public String verPool(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", unidad);
        // CU-53 paso 4: recupera el pool de la unidad. Si no existe, devuelve null (la vista mostrará el formulario de creación).
        model.addAttribute("pool", evaluacionService.buscarPoolPorUnidad(unidad).orElse(null));
        model.addAttribute("titulo", "Pool de Preguntas | " + unidad.getTitulo());
        return "pages/docente/gestionar-pool";
    }

    /**
     * TRAZABILIDAD: CU-54 — Crear pool.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La unidad existe. No existe un pool previo para la unidad.
     * Flujo paso 3: valida que no exista ya un pool para la unidad (RN-CU52-01).
     * Flujo paso 4: registra el pool con el nombre dado.
     * Postcondición: pool creado y asociado a la unidad.
     * EX-CU54-01 (RN-CU52-01): ya existe un pool → redirect con mensaje de excepción.
     */
    @PostMapping("/docente/unidad/{unidadId}/pool/crear")
    public String crearPool(@PathVariable Integer unidadId,
                            @RequestParam String nombre,
                            RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        // CU-54 paso 3: verificar unicidad del pool por unidad.
        if (evaluacionService.buscarPoolPorUnidad(unidad).isPresent()) {
            ra.addFlashAttribute("mensaje", "EX-CU54-01: Esta unidad ya cuenta con un pool de preguntas registrado.");
            return "redirect:/docente/unidad/" + unidadId + "/pool";
        }
        Pool pool = new Pool(nombre, unidad);
        evaluacionService.guardarPool(pool);
        ra.addFlashAttribute("mensaje", "Pool creado correctamente.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-54 — Crear pool (Cargar preguntas de opción múltiple / Verdadero-Falso).
     * Actor: Docente.
     * Precondición: el pool existe. El docente accede al formulario de carga de preguntas.
     * Flujo paso 5-8: registra la pregunta con su texto, tipo (opción múltiple/V-F) y opciones de respuesta,
     *   marcando la opción correcta.
     * Postcondición: pregunta registrada con sus opciones.
     */
    @PostMapping("/docente/pool/{poolId}/pregunta/agregar")
    public String agregarPregunta(@PathVariable Integer poolId,
                                  @RequestParam String texto,
                                  @RequestParam(defaultValue = "true") Boolean esOpcionMultiple,
                                  @RequestParam List<String> opciones,
                                  @RequestParam Integer correcta,
                                  RedirectAttributes ra) {
        Pool pool = evaluacionService.buscarPoolPorId(poolId).orElse(null);
        if (pool == null) return "redirect:/docente";

        Pregunta pregunta = new Pregunta(texto, esOpcionMultiple, pool);
        pregunta = evaluacionService.guardarPregunta(pregunta);

        for (int i = 0; i < opciones.size(); i++) {
            OpcionRespuesta opcion = new OpcionRespuesta(opciones.get(i), i == correcta, pregunta);
            evaluacionService.guardarOpcion(opcion);
        }

        ra.addFlashAttribute("mensaje", "Pregunta agregada.");
        return "redirect:/docente/unidad/" + pool.getUnidad().getId() + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-55 — Modificar pool (nombre del pool).
     * Actor: Docente.
     */
    @PostMapping("/docente/pool/{poolId}/modificar")
    public String modificarPool(@PathVariable Integer poolId,
                                @RequestParam String nombre,
                                RedirectAttributes ra) {
        Pool pool = evaluacionService.buscarPoolPorId(poolId).orElse(null);
        if (pool == null) return "redirect:/docente";

        if (nombre == null || nombre.isBlank()) {
            ra.addFlashAttribute("mensaje", "El nombre del pool no puede estar vacío.");
            return "redirect:/docente/unidad/" + pool.getUnidad().getId() + "/pool";
        }

        pool.setNombre(nombre.trim());
        evaluacionService.guardarPool(pool);
        ra.addFlashAttribute("mensaje", "Nombre del pool actualizado.");
        return "redirect:/docente/unidad/" + pool.getUnidad().getId() + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-55 — Modificar pregunta del pool.
     * Actor: Docente.
     */
    @PostMapping("/docente/pregunta/{preguntaId}/modificar")
    public String modificarPregunta(@PathVariable Integer preguntaId,
                                    @RequestParam String texto,
                                    @RequestParam(defaultValue = "true") Boolean esOpcionMultiple,
                                    RedirectAttributes ra) {
        Pregunta p = evaluacionService.buscarPreguntaPorId(preguntaId).orElse(null);
        if (p == null) return "redirect:/docente";

        p.setTexto(texto.trim());
        p.setEsOpcionMultiple(esOpcionMultiple);
        evaluacionService.guardarPregunta(p);

        ra.addFlashAttribute("mensaje", "Pregunta modificada.");
        return "redirect:/docente/unidad/" + p.getPool().getUnidad().getId() + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-55 — Modificar opción de respuesta de una pregunta.
     * Actor: Docente.
     */
    @PostMapping("/docente/opcion/{opcionId}/modificar")
    public String modificarOpcion(@PathVariable Integer opcionId,
                                  @RequestParam String texto,
                                  @RequestParam(defaultValue = "false") boolean esCorrecta,
                                  RedirectAttributes ra) {
        Optional<OpcionRespuesta> oOpt = evaluacionService.buscarOpcionPorId(opcionId);
        if (oOpt.isEmpty()) return "redirect:/docente";

        OpcionRespuesta op = oOpt.get();
        op.setTexto(texto.trim());
        op.setEsCorrecta(esCorrecta);
        evaluacionService.guardarOpcion(op);

        ra.addFlashAttribute("mensaje", "Opción actualizada.");
        return "redirect:/docente/unidad/" + op.getPregunta().getPool().getUnidad().getId() + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-55 — Eliminar opción de respuesta.
     * Actor: Docente.
     */
    @PostMapping("/docente/opcion/{opcionId}/borrar")
    public String borrarOpcion(@PathVariable Integer opcionId, RedirectAttributes ra) {
        Optional<OpcionRespuesta> oOpt = evaluacionService.buscarOpcionPorId(opcionId);
        if (oOpt.isEmpty()) return "redirect:/docente";

        OpcionRespuesta op = oOpt.get();
        Integer unidadId = op.getPregunta().getPool().getUnidad().getId();
        evaluacionService.borrarOpcion(op);

        ra.addFlashAttribute("mensaje", "Opción eliminada.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-55 — Eliminar pregunta individual del pool (Caso C).
     * Actor: Docente.
     */
    @PostMapping("/docente/pregunta/{preguntaId}/borrar")
    public String borrarPregunta(@PathVariable Integer preguntaId, RedirectAttributes ra) {
        Pregunta p = evaluacionService.buscarPreguntaPorId(preguntaId).orElse(null);
        if (p == null) return "redirect:/docente";
        Integer unidadId = p.getPool().getUnidad().getId();
        evaluacionService.borrarPregunta(p);
        ra.addFlashAttribute("mensaje", "Pregunta eliminada del pool.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-56 — Dar de baja pool completo.
     * Actor: Administrador / Docente.
     */
    @PostMapping("/docente/pool/{poolId}/baja")
    public String darBajaPool(@PathVariable Integer poolId, RedirectAttributes ra) {
        Pool pool = evaluacionService.buscarPoolPorId(poolId).orElse(null);
        if (pool == null) return "redirect:/docente";

        Integer unidadId = pool.getUnidad().getId();
        evaluacionService.borrarPool(pool);
        ra.addFlashAttribute("mensaje", "Pool de preguntas dado de baja correctamente.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    // ─── DOCENTE — Autoevaluaciones (CU-57 a CU-60) ───────────────────────────

    /**
     * TRAZABILIDAD: CU-57 — Buscar autoevaluación.
     * Actor: Administrador / Docente.
     */
    @GetMapping("/docente/unidad/{unidadId}/autoevaluaciones")
    public String listarAutoevaluaciones(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        List<Autoevaluacion> autoevaluaciones = evaluacionService.buscarAutoevaluacionesPorUnidad(unidad);

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", unidad);
        model.addAttribute("autoevaluaciones", autoevaluaciones);
        model.addAttribute("pools", evaluacionService.buscarPoolPorUnidad(unidad).map(List::of).orElse(List.of()));
        model.addAttribute("titulo", "Autoevaluaciones | " + unidad.getTitulo());
        return "pages/docente/autoevaluaciones";
    }

    /**
     * TRAZABILIDAD: CU-58 — Crear autoevaluación.
     * Actor: Docente.
     */
    @PostMapping("/docente/unidad/{unidadId}/autoevaluacion/crear")
    public String crearAutoevaluacion(@PathVariable Integer unidadId,
                                      @RequestParam String nombre,
                                      @RequestParam(defaultValue = "30") int tiempoLimite,
                                      @RequestParam(defaultValue = "3") int intentosPermitidos,
                                      @RequestParam String fechaApertura,
                                      @RequestParam(required = false) String fechaCierre,
                                      @RequestParam(defaultValue = "false") boolean oculto,
                                      RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        try {
            LocalDateTime fApertura = LocalDateTime.parse(fechaApertura + "T00:00:00");
            LocalDateTime fCierre = (fechaCierre != null && !fechaCierre.isBlank()) ? LocalDateTime.parse(fechaCierre + "T23:59:59") : null;

            Autoevaluacion ae = new Autoevaluacion(nombre.trim(), tiempoLimite, fApertura, unidad);
            ae.setIntentosPermitidos(intentosPermitidos);
            ae.setFechaCierre(fCierre);
            ae.setOculto(oculto);
            ae.setBaja(false);

            evaluacionService.guardarAutoevaluacion(ae);
            ra.addFlashAttribute("mensaje", "Autoevaluación '" + nombre + "' creada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al crear autoevaluación: " + e.getMessage());
        }

        return "redirect:/docente/unidad/" + unidadId + "/autoevaluaciones";
    }

    /**
     * TRAZABILIDAD: CU-59 — Modificar autoevaluación (Caso A y Caso B).
     * Actor: Docente.
     */
    @PostMapping("/docente/autoevaluacion/{id}/modificar")
    public String modificarAutoevaluacion(@PathVariable Integer id,
                                          @RequestParam String nombre,
                                          @RequestParam int tiempoLimite,
                                          @RequestParam int intentosPermitidos,
                                          @RequestParam(required = false) String fechaCierre,
                                          @RequestParam(defaultValue = "false") boolean oculto,
                                          RedirectAttributes ra) {
        Optional<Autoevaluacion> aeOpt = evaluacionService.buscarAutoevaluacionPorId(id);
        if (aeOpt.isEmpty()) return "redirect:/docente";

        Autoevaluacion ae = aeOpt.get();
        try {
            ae.setNombre(nombre.trim());
            ae.setTiempoLimite(tiempoLimite);
            ae.setIntentosPermitidos(intentosPermitidos);
            ae.setOculto(oculto);
            if (fechaCierre != null && !fechaCierre.isBlank()) {
                ae.setFechaCierre(LocalDateTime.parse(fechaCierre + "T23:59:59"));
            }
            ae.setUltimaModificacion(LocalDateTime.now());
            evaluacionService.guardarAutoevaluacion(ae);
            ra.addFlashAttribute("mensaje", "Autoevaluación modificada exitosamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al modificar autoevaluación: " + e.getMessage());
        }

        return "redirect:/docente/unidad/" + ae.getUnidad().getId() + "/autoevaluaciones";
    }

    /**
     * TRAZABILIDAD: CU-60 — Dar de baja autoevaluación.
     * Actor: Administrador / Docente.
     */
    @PostMapping("/docente/autoevaluacion/{id}/baja")
    public String darBajaAutoevaluacion(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Autoevaluacion> aeOpt = evaluacionService.buscarAutoevaluacionPorId(id);
        if (aeOpt.isEmpty()) return "redirect:/docente";

        Autoevaluacion ae = aeOpt.get();
        Integer unidadId = ae.getUnidad().getId();
        evaluacionService.borrarAutoevaluacion(ae);
        ra.addFlashAttribute("mensaje", "Autoevaluación dada de baja correctamente.");
        return "redirect:/docente/unidad/" + unidadId + "/autoevaluaciones";
    }

    // ─── ALUMNO / DOCENTE / ADMIN — Intentos y Calificaciones (CU-61 a CU-64) ────

    /**
     * TRAZABILIDAD: CU-61 — Buscar intento de autoevaluación (Docente / Administrador).
     * Actor: Administrador / Docente.
     */
    @GetMapping("/docente/autoevaluacion/{autoevaluacionId}/intentos")
    public String listarIntentosDocente(@PathVariable Integer autoevaluacionId, Model model, Authentication auth) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/docente";

        List<IntentoAutoevaluacion> intentos = intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae);

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("intentos", intentos);
        model.addAttribute("titulo", "Intentos | " + ae.getNombre());
        return "pages/docente/intentos-autoevaluacion";
    }

    /**
     * TRAZABILIDAD: CU-62 — Ver calificaciones (Docente / Administrador).
     * Actor: Docente / Administrador.
     */
    @GetMapping("/docente/unidad/{unidadId}/calificaciones")
    public String verCalificacionesUnidad(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        List<Autoevaluacion> autoevaluaciones = evaluacionService.buscarAutoevaluacionesPorUnidad(unidad);
        List<IntentoAutoevaluacion> todosIntentos = autoevaluaciones.stream()
                .flatMap(ae -> intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae).stream())
                .toList();

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", unidad);
        model.addAttribute("autoevaluaciones", autoevaluaciones);
        model.addAttribute("intentos", todosIntentos);
        model.addAttribute("titulo", "Calificaciones | " + unidad.getTitulo());
        return "pages/docente/calificaciones";
    }

    /**
     * TRAZABILIDAD: CU-63 — Realizar intento de autoevaluación (vista del examen con carga secuencial de preguntas).
     * Actor: Alumno.
     * Precondición: sesión con rol Alumno. La autoevaluación existe y está activa.
     * Flujo: sortea aleatoriamente preguntas del pool y presenta opciones.
     */
    @GetMapping("/evaluacion/{autoevaluacionId}/rendir")
    public String verExamen(@PathVariable Integer autoevaluacionId, Model model, Authentication auth) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();

        try {
            intentoService.iniciarIntento(ae, usuario);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        List<Pregunta> preguntas = intentoService.sortearPreguntas(ae);

        model.addAttribute("usuario", usuario);
        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("titulo", "Autoevaluación: " + ae.getNombre());
        return "pages/alumno/rendir-examen";
    }

    /**
     * TRAZABILIDAD: CU-63 — Realizar intento de autoevaluación (Entregar intento, calcular nota y validar aprobación).
     * Actor: Alumno.
     */
    @PostMapping("/evaluacion/{autoevaluacionId}/enviar")
    public String enviarExamen(@PathVariable Integer autoevaluacionId,
                               @RequestParam Map<String, String> form,
                               Authentication auth,
                               RedirectAttributes ra) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(ae);

        Map<Integer, Integer> respuestas = new HashMap<>();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (entry.getKey().startsWith("pregunta_")) {
                try {
                    Integer preguntaId = Integer.parseInt(entry.getKey().replace("pregunta_", ""));
                    Integer opcionId = Integer.parseInt(entry.getValue());
                    respuestas.put(preguntaId, opcionId);
                } catch (NumberFormatException ignored) {}
            }
        }

        IntentoAutoevaluacion resultado = intentoService.corregirYGuardar(intento, respuestas);
        ra.addFlashAttribute("intentoId", resultado.getId());
        ra.addFlashAttribute("nota", resultado.getNota());
        ra.addFlashAttribute("aprobado", intentoService.estaAprobado(resultado));
        return "redirect:/evaluacion/" + autoevaluacionId + "/resultado";
    }

    /**
     * TRAZABILIDAD: CU-61 — Buscar intento de autoevaluación (Historial del alumno).
     * Actor: Alumno.
     */
    @GetMapping("/evaluacion/{autoevaluacionId}/resultado")
    public String verResultado(@PathVariable Integer autoevaluacionId, Model model, Authentication auth) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        List<IntentoAutoevaluacion> historial = intentoService.historialPorAlumno(ae, usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("historial", historial);
        model.addAttribute("titulo", "Resultado | " + ae.getNombre());
        return "pages/alumno/resultado-examen";
    }

    /**
     * TRAZABILIDAD: CU-64 — Dar de baja intento de autoevaluación.
     * Actor: Administrador.
     */
    @PostMapping("/evaluacion/intento/{intentoId}/baja")
    public String darBajaIntento(@PathVariable Integer intentoId, Authentication auth, RedirectAttributes ra) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        if (!usuario.esAdmin()) {
            ra.addFlashAttribute("mensaje", "Solo los administradores pueden anular intentos.");
            return "redirect:/admin";
        }

        Optional<IntentoAutoevaluacion> iOpt = intentoRepository.findById(intentoId);
        if (iOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Intento no encontrado.");
            return "redirect:/admin";
        }

        IntentoAutoevaluacion intento = iOpt.get();
        intento.setBaja(true);
        intentoRepository.save(intento);

        ra.addFlashAttribute("mensaje", "Intento anulado correctamente.");
        return "redirect:/docente/autoevaluacion/" + intento.getAutoevaluacion().getId() + "/intentos";
    }
}

