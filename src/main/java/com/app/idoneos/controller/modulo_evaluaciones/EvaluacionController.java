package com.app.idoneos.controller.modulo_evaluaciones;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * TRAZABILIDAD — Controller para el Módulo de Evaluación y Progreso (MOD-F-04).
 *
 * Mapea y conecta directamente las 12 pantallas de Evaluaciones:
 *   CU-53 — Buscar pool                                  → GET /evaluaciones/pools
 *   CU-54 — Crear pool                                   → GET /evaluaciones/pools/nuevo, POST /evaluaciones/pools/guardar
 *   CU-55 — Modificar pool                               → GET /evaluaciones/pools/{id}/editar, POST /evaluaciones/pools/{id}/editar
 *   CU-56 — Dar de baja pool                             → GET/POST /evaluaciones/pools/{id}/baja
 *   CU-57 — Buscar autoevaluación                        → GET /evaluaciones/autoevaluaciones
 *   CU-58 — Crear autoevaluación                         → GET /evaluaciones/autoevaluaciones/nueva, POST /evaluaciones/autoevaluaciones/guardar
 *   CU-59 — Modificar autoevaluación                     → GET /evaluaciones/autoevaluaciones/{id}/editar, POST /evaluaciones/autoevaluaciones/{id}/editar
 *   CU-60 — Dar de baja autoevaluación                   → GET/POST /evaluaciones/autoevaluaciones/{id}/baja
 *   CU-61 — Buscar intento de autoevaluación             → GET /evaluaciones/intentos
 *   CU-62 — Ver calificaciones                           → GET /evaluaciones/calificaciones
 *   CU-63 — Realizar intento de autoevaluación           → GET /evaluaciones/autoevaluaciones/{id}/rendir, POST /evaluaciones/autoevaluaciones/{id}/enviar
 *   CU-64 — Dar de baja intento de autoevaluación        → GET/POST /evaluaciones/intentos/{id}/baja
 */
@Controller
@RequestMapping("/evaluaciones")
public class EvaluacionController {

    @Autowired private EvaluacionService evaluacionService;
    @Autowired private IntentoService intentoService;
    @Autowired private UnidadService unidadService;
    @Autowired private CursoService cursoService;
    @Autowired private PoolRepository poolRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRespuestaRepository;
    @Autowired private AutoevaluacionRepository autoevaluacionRepository;
    @Autowired private IntentoAutoevaluacionRepository intentoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private RespuestaIntentoRepository respuestaIntentoRepository;

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-53 a CU-56: POOLS DE PREGUNTAS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/pools")
    public String buscarPools(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                              Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        Pool pool = (unidad != null) ? evaluacionService.buscarPoolPorUnidad(unidad).orElse(null) : null;
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("pool", pool);
        model.addAttribute("pools", poolRepository.findAll());
        model.addAttribute("titulo", "CU-53 - Buscar pool | Idóneos Online");
        return "pages/evaluaciones/cu-53-buscar-pool";
    }

    @GetMapping("/pools/nuevo")
    public String crearPoolForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-54 - Crear pool | Idóneos Online");
        return "pages/evaluaciones/cu-54-crear-pool";
    }

    @PostMapping("/pools/guardar")
    public String guardarPool(@RequestParam Integer unidadId,
                              @RequestParam String nombre,
                              RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            if (evaluacionService.buscarPoolPorUnidad(unidad).isPresent()) {
                ra.addFlashAttribute("error", "Esta unidad ya cuenta con un pool de preguntas registrado.");
                return "redirect:/evaluaciones/pools?unidadId=" + unidadId;
            }
            Pool pool = new Pool(nombre, unidad);
            poolRepository.save(pool);
            ra.addFlashAttribute("mensaje", "Pool creado exitosamente.");
            return "redirect:/evaluaciones/pools?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/pools/nuevo?unidadId=" + unidadId;
        }
    }

    @GetMapping("/pools/{id}/editar")
    public String modificarPoolForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Pool> poolOpt = evaluacionService.buscarPoolPorId(id);
        if (poolOpt.isEmpty()) return "redirect:/evaluaciones/pools";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("pool", poolOpt.get());
        model.addAttribute("titulo", "CU-55 - Modificar pool | Idóneos Online");
        return "pages/evaluaciones/cu-55-modificar-pool";
    }

    @PostMapping("/pools/{id}/editar")
    public String actualizarPool(@PathVariable Integer id, @RequestParam String nombre, RedirectAttributes ra) {
        try {
            Pool pool = evaluacionService.buscarPoolPorId(id).orElseThrow(() -> new IllegalArgumentException("Pool no encontrado"));
            pool.setNombre(nombre);
            evaluacionService.guardarPool(pool);
            ra.addFlashAttribute("mensaje", "Pool modificado correctamente.");
            return "redirect:/evaluaciones/pools?unidadId=" + pool.getUnidad().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/pools/" + id + "/editar";
        }
    }

    @GetMapping("/pools/{id}/baja")
    public String darDeBajaPoolView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Pool> poolOpt = evaluacionService.buscarPoolPorId(id);
        if (poolOpt.isEmpty()) return "redirect:/evaluaciones/pools";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("pool", poolOpt.get());
        model.addAttribute("titulo", "CU-56 - Dar de baja pool | Idóneos Online");
        return "pages/evaluaciones/cu-56-dar-de-baja-pool";
    }

    @PostMapping("/pools/{id}/baja")
    public String eliminarPool(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Pool pool = evaluacionService.buscarPoolPorId(id).orElse(null);
            Integer uId = (pool != null && pool.getUnidad() != null) ? pool.getUnidad().getId() : null;
            if (pool != null) {
                evaluacionService.borrarPool(pool);
            }
            ra.addFlashAttribute("mensaje", "Pool dado de baja correctamente.");
            return uId != null ? "redirect:/evaluaciones/pools?unidadId=" + uId : "redirect:/evaluaciones/pools";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/pools/" + id + "/baja";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-57 a CU-60: AUTOEVALUACIONES
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/autoevaluaciones")
    public String buscarAutoevaluaciones(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                         Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        List<Autoevaluacion> autoevaluaciones = (unidad != null) ? evaluacionService.buscarAutoevaluacionesPorUnidad(unidad) : List.of();
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("autoevaluaciones", autoevaluaciones);
        model.addAttribute("titulo", "CU-57 - Buscar autoevaluación | Idóneos Online");
        return "pages/evaluaciones/cu-57-buscar-autoevaluacion";
    }

    @GetMapping("/autoevaluaciones/nueva")
    public String crearAutoevaluacionForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                          Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-58 - Crear autoevaluación | Idóneos Online");
        return "pages/evaluaciones/cu-58-crear-autoevaluacion";
    }

    @PostMapping("/autoevaluaciones/guardar")
    public String guardarAutoevaluacion(@RequestParam Integer unidadId,
                                        @RequestParam String nombre,
                                        @RequestParam(defaultValue = "30") int tiempoLimite,
                                        @RequestParam(required = false) Integer intentosPermitidos,
                                        RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            Autoevaluacion ae = new Autoevaluacion(nombre, tiempoLimite, LocalDateTime.now(), unidad);
            ae.setIntentosPermitidos(intentosPermitidos);
            evaluacionService.guardarAutoevaluacion(ae);
            ra.addFlashAttribute("mensaje", "Autoevaluación creada exitosamente.");
            return "redirect:/evaluaciones/autoevaluaciones?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/autoevaluaciones/nueva?unidadId=" + unidadId;
        }
    }

    @GetMapping("/autoevaluaciones/{id}/editar")
    public String modificarAutoevaluacionForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Autoevaluacion> aeOpt = evaluacionService.buscarAutoevaluacionPorId(id);
        if (aeOpt.isEmpty()) return "redirect:/evaluaciones/autoevaluaciones";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("autoevaluacion", aeOpt.get());
        model.addAttribute("titulo", "CU-59 - Modificar autoevaluación | Idóneos Online");
        return "pages/evaluaciones/cu-59-modificar-autoevaluacion";
    }

    @PostMapping("/autoevaluaciones/{id}/editar")
    public String actualizarAutoevaluacion(@PathVariable Integer id,
                                           @RequestParam String nombre,
                                           @RequestParam int tiempoLimite,
                                           @RequestParam(required = false) Integer intentosPermitidos,
                                           RedirectAttributes ra) {
        try {
            Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(id).orElseThrow(() -> new IllegalArgumentException("Autoevaluación no encontrada"));
            ae.setNombre(nombre);
            ae.setTiempoLimite(tiempoLimite);
            ae.setIntentosPermitidos(intentosPermitidos);
            evaluacionService.guardarAutoevaluacion(ae);
            ra.addFlashAttribute("mensaje", "Autoevaluación modificada correctamente.");
            return "redirect:/evaluaciones/autoevaluaciones?unidadId=" + ae.getUnidad().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/autoevaluaciones/" + id + "/editar";
        }
    }

    @GetMapping("/autoevaluaciones/{id}/baja")
    public String darDeBajaAutoevaluacionView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Autoevaluacion> aeOpt = evaluacionService.buscarAutoevaluacionPorId(id);
        if (aeOpt.isEmpty()) return "redirect:/evaluaciones/autoevaluaciones";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("autoevaluacion", aeOpt.get());
        model.addAttribute("titulo", "CU-60 - Dar de baja autoevaluación | Idóneos Online");
        return "pages/evaluaciones/cu-60-dar-de-baja-autoevaluacion";
    }

    @PostMapping("/autoevaluaciones/{id}/baja")
    public String eliminarAutoevaluacion(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(id).orElse(null);
            Integer uId = (ae != null && ae.getUnidad() != null) ? ae.getUnidad().getId() : null;
            if (ae != null) {
                evaluacionService.borrarAutoevaluacion(ae);
            }
            ra.addFlashAttribute("mensaje", "Autoevaluación dada de baja correctamente.");
            return uId != null ? "redirect:/evaluaciones/autoevaluaciones?unidadId=" + uId : "redirect:/evaluaciones/autoevaluaciones";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/autoevaluaciones/" + id + "/baja";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-61, CU-62, CU-63, CU-64: INTENTOS Y CALIFICACIONES
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/intentos")
    public String buscarIntentos(@RequestParam(value = "autoevaluacionId", required = false) Integer autoevaluacionId,
                                 Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<IntentoAutoevaluacion> intentos;
        if (autoevaluacionId != null) {
            Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
            intentos = (ae != null) ? intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae) : List.of();
        } else {
            intentos = intentoRepository.findAll();
        }

        model.addAttribute("intentos", intentos);
        model.addAttribute("autoevaluaciones", autoevaluacionRepository.findAll());
        model.addAttribute("titulo", "CU-61 - Buscar intento de autoevaluación | Idóneos Online");
        return "pages/evaluaciones/cu-61-buscar-intento-de-autoevaluacion";
    }

    @GetMapping("/calificaciones")
    public String verCalificaciones(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                    Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        List<Autoevaluacion> autoevaluaciones = (unidad != null) ? evaluacionService.buscarAutoevaluacionesPorUnidad(unidad) : List.of();
        List<IntentoAutoevaluacion> intentos = autoevaluaciones.stream()
                .flatMap(ae -> intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae).stream())
                .toList();

        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("autoevaluaciones", autoevaluaciones);
        model.addAttribute("intentos", intentos);
        model.addAttribute("titulo", "CU-62 - Ver calificaciones | Idóneos Online");
        return "pages/evaluaciones/cu-62-ver-calificaciones";
    }

    @GetMapping("/autoevaluaciones/{id}/rendir")
    public String realizarIntentoView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Autoevaluacion> aeOpt = evaluacionService.buscarAutoevaluacionPorId(id);
        if (aeOpt.isEmpty()) return "redirect:/evaluaciones/autoevaluaciones";

        agregarUsuarioAlModelo(model, auth);
        Autoevaluacion ae = aeOpt.get();
        List<Pregunta> preguntas = intentoService.sortearPreguntas(ae);

        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("titulo", "CU-63 - Realizar intento de autoevaluación | Idóneos Online");
        return "pages/evaluaciones/cu-63-realizar-intento-de-autoevaluacion";
    }

    @PostMapping("/autoevaluaciones/{id}/enviar")
    public String enviarIntento(@PathVariable Integer id,
                                @RequestParam Map<String, String> form,
                                Authentication auth, RedirectAttributes ra) {
        Optional<Autoevaluacion> aeOpt = evaluacionService.buscarAutoevaluacionPorId(id);
        if (aeOpt.isEmpty()) return "redirect:/evaluaciones/autoevaluaciones";

        Autoevaluacion ae = aeOpt.get();
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(ae);

        // Asociar la inscripción activa del usuario logueado si es Alumno
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            Usuario usuario = (Usuario) auth.getPrincipal();
            List<Inscripcion> inscripciones = inscripcionRepository.findByUsuarioAndBajaFalse(usuario);
            if (!inscripciones.isEmpty()) {
                intento.setInscripcion(inscripciones.get(0));
            }
        }

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
        return "redirect:/evaluaciones/intento/" + resultado.getId() + "/resultado";
    }

    /**
     * CU-63 — Pantalla de resultado del intento de autoevaluación con nota y desglose de respuestas.
     */
    @GetMapping("/intento/{intentoId}/resultado")
    public String verResultadoIntento(@PathVariable Integer intentoId, Model model, Authentication auth) {
        Optional<IntentoAutoevaluacion> iOpt = intentoRepository.findById(intentoId);
        if (iOpt.isEmpty()) return "redirect:/evaluaciones/intentos";

        agregarUsuarioAlModelo(model, auth);
        IntentoAutoevaluacion intento = iOpt.get();
        List<RespuestaIntento> respuestas = respuestaIntentoRepository.findByIntentoAutoevaluacion(intento);

        boolean aprobado = intentoService.estaAprobado(intento);
        model.addAttribute("intento", intento);
        model.addAttribute("aprobado", aprobado);
        model.addAttribute("respuestas", respuestas);
        model.addAttribute("titulo", "Resultado de Evaluación | Idóneos Online");
        return "pages/evaluaciones/resultado-intento";
    }

    @GetMapping("/intentos/{id}/baja")
    public String darDeBajaIntentoView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<IntentoAutoevaluacion> iOpt = intentoRepository.findById(id);
        if (iOpt.isEmpty()) return "redirect:/evaluaciones/intentos";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("intento", iOpt.get());
        model.addAttribute("titulo", "CU-64 - Dar de baja intento de autoevaluación | Idóneos Online");
        return "pages/evaluaciones/cu-64-dar-de-baja-intento-de-autoevaluacion";
    }

    @PostMapping("/intentos/{id}/baja")
    public String eliminarIntento(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Optional<IntentoAutoevaluacion> iOpt = intentoRepository.findById(id);
            if (iOpt.isPresent()) {
                IntentoAutoevaluacion i = iOpt.get();
                i.setBaja(true);
                intentoRepository.save(i);
            }
            ra.addFlashAttribute("mensaje", "Intento de autoevaluación anulado exitosamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evaluaciones/intentos";
    }
}
