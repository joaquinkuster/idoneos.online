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
    @Autowired private com.app.idoneos.repository.modulo_usuarios.UsuarioRepository usuarioRepository;

    private Usuario obtenerUsuarioAutenticado(Authentication auth) {
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        String email = auth.getName();
        if (email != null && usuarioRepository != null) {
            return usuarioRepository.findByCorreo(email).orElse(null);
        }
        return null;
    }

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        Usuario u = obtenerUsuarioAutenticado(auth);
        if (u != null) {
            model.addAttribute("usuario", u);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-53 a CU-56: POOLS DE PREGUNTAS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/pools")
    public String buscarPools(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                              @RequestParam(value = "unidadId", required = false) Integer unidadId,
                              @RequestParam(value = "q", required = false) String query,
                              @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                              @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                              Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        Curso curso = (cursoId != null) ? cursoService.buscarPorId(cursoId).orElse(null) : null;
        List<Unidad> unidades = (curso != null) ? unidadService.obtenerPorCurso(curso) : unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : null;

        if (curso == null && unidad != null && unidad.getCurso() != null) {
            curso = unidad.getCurso();
        }
        if (curso == null) {
            List<Curso> todos = cursoService.obtenerTodo();
            if (!todos.isEmpty()) curso = todos.get(0);
        }

        final Curso cursoFinal = curso;
        List<Pool> todosPools = poolRepository.findAll().stream()
                .filter(p -> !p.isBaja())
                .filter(p -> unidadId == null || (p.getUnidad() != null && Objects.equals(p.getUnidad().getId(), unidadId)))
                .filter(p -> {
                    if (cursoId == null) return true;
                    if (p.getUnidad() == null) return true;
                    return p.getUnidad().getCurso() != null && Objects.equals(p.getUnidad().getCurso().getId(), cursoId);
                })
                .filter(p -> query == null || query.isBlank() || p.getNombre().toLowerCase().contains(query.toLowerCase()))
                .toList();

        // Si el filtro por curso puntual no tiene pools creados en esa unidad pero hay pools en el sistema, mostrar pools vigentes para que la tabla nunca aparezca vacía sin razón
        if (todosPools.isEmpty() && (query == null || query.isBlank()) && unidadId == null) {
            todosPools = poolRepository.findAll().stream().filter(p -> !p.isBaja()).toList();
        }

        // Paginación
        int totalElements = todosPools.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<Pool> poolsPaginados = (fromIndex < totalElements) ? todosPools.subList(fromIndex, toIndex) : Collections.emptyList();

        model.addAttribute("curso", cursoFinal);
        model.addAttribute("cursoSeleccionado", cursoFinal);
        model.addAttribute("unidades", unidades);
        model.addAttribute("todasLasUnidades", unidadService.obtenerTodo().stream().filter(u -> !u.isBaja()).toList());
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("pool", (poolsPaginados.isEmpty() ? null : poolsPaginados.get(0)));
        model.addAttribute("pools", poolsPaginados);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("size", size);
        model.addAttribute("fromIndex", (totalElements > 0 ? fromIndex + 1 : 0));
        model.addAttribute("toIndex", toIndex);
        model.addAttribute("titulo", "CU-53 - Buscar pool | Idóneos Online");
        return "pages/evaluaciones/cu-53-buscar-pool";
    }

    @PostMapping("/pools/guardar")
    public String guardarPool(@RequestParam Integer unidadId,
                              @RequestParam String nombre,
                              @RequestParam(required = false) String descripcion,
                              jakarta.servlet.http.HttpServletRequest request,
                              RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            Pool pool = new Pool(nombre, unidad);
            pool = poolRepository.save(pool);

            // Procesar preguntas dinámicas p1, p2, p3... pn
            java.util.Enumeration<String> paramNames = request.getParameterNames();
            java.util.Set<String> preguntaPrefixes = new java.util.TreeSet<>();
            
            while (paramNames.hasMoreElements()) {
                String p = paramNames.nextElement();
                if (p.endsWith("_texto")) {
                    preguntaPrefixes.add(p.substring(0, p.indexOf("_texto")));
                }
            }

            for (String prefix : preguntaPrefixes) {
                String texto = request.getParameter(prefix + "_texto");
                String tipo = request.getParameter(prefix + "_tipo");
                if (texto != null && !texto.isBlank()) {
                    boolean esMultiple = !"vf".equalsIgnoreCase(tipo);
                    Pregunta pregunta = new Pregunta(texto, esMultiple, pool);
                    pregunta = preguntaRepository.save(pregunta);

                    if (!esMultiple) {
                        // Pregunta Verdadero / Falso
                        String vfOpt = request.getParameter(prefix + "_opt");
                        boolean esVerdaderoCorrecto = "true".equalsIgnoreCase(vfOpt) || "1".equals(vfOpt);
                        opcionRespuestaRepository.save(new OpcionRespuesta("Verdadero", esVerdaderoCorrecto, pregunta));
                        opcionRespuestaRepository.save(new OpcionRespuesta("Falso", !esVerdaderoCorrecto, pregunta));
                    } else {
                        // Pregunta Opción Múltiple (1 o varias opciones correctas)
                        String[] correctas = request.getParameterValues(prefix + "_correctas");
                        if (correctas == null || correctas.length == 0) {
                            String cSingle = request.getParameter(prefix + "_correcta");
                            if (cSingle != null) correctas = new String[]{cSingle};
                            else correctas = new String[]{"1"};
                        }
                        java.util.Set<String> setCorrectas = new java.util.HashSet<>(java.util.Arrays.asList(correctas));

                        // Buscar todas las opciones enviadas para esta pregunta: prefix_opt1, prefix_opt2...
                        int optIdx = 1;
                        while (true) {
                            String optTexto = request.getParameter(prefix + "_opt" + optIdx);
                            if (optTexto == null) {
                                if (optIdx > 20) break;
                                optIdx++;
                                continue;
                            }
                            if (!optTexto.isBlank()) {
                                boolean esCorrecta = setCorrectas.contains(String.valueOf(optIdx));
                                opcionRespuestaRepository.save(new OpcionRespuesta(optTexto, esCorrecta, pregunta));
                            }
                            optIdx++;
                        }
                    }
                }
            }

            ra.addFlashAttribute("mensaje", "Pool '" + nombre + "' creado exitosamente con sus preguntas y opciones.");
            return "redirect:/evaluaciones/pools?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/pools?unidadId=" + unidadId;
        }
    }

    @GetMapping("/pools/{id}/detalles")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> obtenerDetallesPool(@PathVariable Integer id) {
        Optional<Pool> pOpt = evaluacionService.buscarPoolPorId(id);
        if (pOpt.isEmpty()) return org.springframework.http.ResponseEntity.notFound().build();

        Pool pool = pOpt.get();
        List<Pregunta> preguntas = evaluacionService.preguntasPorPool(pool);
        List<Map<String, Object>> listaPreguntas = new java.util.ArrayList<>();

        for (Pregunta preg : preguntas) {
            List<OpcionRespuesta> opciones = opcionRespuestaRepository.findByPreguntaAndBajaFalse(preg);
            Map<String, Object> pregMap = new HashMap<>();
            pregMap.put("id", preg.getId());
            pregMap.put("texto", preg.getTexto());
            pregMap.put("esOpcionMultiple", preg.isEsOpcionMultiple());

            List<Map<String, Object>> opcionesList = new java.util.ArrayList<>();
            for (OpcionRespuesta opt : opciones) {
                Map<String, Object> optMap = new HashMap<>();
                optMap.put("id", opt.getId());
                optMap.put("texto", opt.getTexto());
                optMap.put("esCorrecta", opt.isEsCorrecta());
                opcionesList.add(optMap);
            }
            pregMap.put("opciones", opcionesList);
            listaPreguntas.add(pregMap);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", pool.getId());
        resp.put("nombre", pool.getNombre());
        resp.put("unidad", (pool.getUnidad() != null ? pool.getUnidad().getTitulo() : "General"));
        resp.put("preguntas", listaPreguntas);

        return org.springframework.http.ResponseEntity.ok(resp);
    }

    @PostMapping("/pools/{id}/editar")
    public String actualizarPool(@PathVariable Integer id,
                                 @RequestParam String nombre,
                                 jakarta.servlet.http.HttpServletRequest request,
                                 RedirectAttributes ra) {
        try {
            Pool pool = evaluacionService.buscarPoolPorId(id).orElseThrow(() -> new IllegalArgumentException("Pool no encontrado"));
            pool.setNombre(nombre);
            pool.setUltimaModificacion(LocalDateTime.now());
            evaluacionService.guardarPool(pool);

            // Procesar nuevas preguntas añadidas en el formulario de edición
            java.util.Enumeration<String> paramNames = request.getParameterNames();
            java.util.Set<String> preguntaPrefixes = new java.util.TreeSet<>();
            
            while (paramNames.hasMoreElements()) {
                String p = paramNames.nextElement();
                if (p.endsWith("_texto")) {
                    preguntaPrefixes.add(p.substring(0, p.indexOf("_texto")));
                }
            }

            for (String prefix : preguntaPrefixes) {
                String texto = request.getParameter(prefix + "_texto");
                String tipo = request.getParameter(prefix + "_tipo");
                if (texto != null && !texto.isBlank()) {
                    boolean esMultiple = !"vf".equalsIgnoreCase(tipo);
                    Pregunta pregunta = new Pregunta(texto, esMultiple, pool);
                    pregunta = preguntaRepository.save(pregunta);

                    if (!esMultiple) {
                        String vfOpt = request.getParameter(prefix + "_opt");
                        boolean esVerdaderoCorrecto = "true".equalsIgnoreCase(vfOpt) || "1".equals(vfOpt);
                        opcionRespuestaRepository.save(new OpcionRespuesta("Verdadero", esVerdaderoCorrecto, pregunta));
                        opcionRespuestaRepository.save(new OpcionRespuesta("Falso", !esVerdaderoCorrecto, pregunta));
                    } else {
                        String[] correctas = request.getParameterValues(prefix + "_correctas");
                        if (correctas == null || correctas.length == 0) {
                            String cSingle = request.getParameter(prefix + "_correcta");
                            if (cSingle != null) correctas = new String[]{cSingle};
                            else correctas = new String[]{"1"};
                        }
                        java.util.Set<String> setCorrectas = new java.util.HashSet<>(java.util.Arrays.asList(correctas));

                        int optIdx = 1;
                        while (true) {
                            String optTexto = request.getParameter(prefix + "_opt" + optIdx);
                            if (optTexto == null) {
                                if (optIdx > 20) break;
                                optIdx++;
                                continue;
                            }
                            if (!optTexto.isBlank()) {
                                boolean esCorrecta = setCorrectas.contains(String.valueOf(optIdx));
                                opcionRespuestaRepository.save(new OpcionRespuesta(optTexto, esCorrecta, pregunta));
                            }
                            optIdx++;
                        }
                    }
                }
            }

            ra.addFlashAttribute("mensaje", "Pool modificado y actualizado correctamente.");
            return "redirect:/evaluaciones/pools?unidadId=" + (pool.getUnidad() != null ? pool.getUnidad().getId() : "");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/pools";
        }
    }

    @PostMapping("/pools/pregunta/{id}/editar")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> editarPreguntaAjax(@PathVariable Integer id,
                                                                        @RequestParam String texto,
                                                                        @RequestParam(required = false, defaultValue = "multiple") String tipo,
                                                                        jakarta.servlet.http.HttpServletRequest request) {
        try {
            Optional<Pregunta> pOpt = evaluacionService.buscarPreguntaPorId(id);
            if (pOpt.isEmpty()) return org.springframework.http.ResponseEntity.notFound().build();

            Pregunta pregunta = pOpt.get();
            pregunta.setTexto(texto);
            boolean esMultiple = !"vf".equalsIgnoreCase(tipo);
            pregunta.setEsOpcionMultiple(esMultiple);
            preguntaRepository.save(pregunta);

            // Dar de baja opciones anteriores
            List<OpcionRespuesta> opcionesPrevias = opcionRespuestaRepository.findByPreguntaAndBajaFalse(pregunta);
            for (OpcionRespuesta op : opcionesPrevias) {
                op.setBaja(true);
                opcionRespuestaRepository.save(op);
            }

            if (!esMultiple) {
                String vfOpt = request.getParameter("vf_opt");
                boolean esVerdaderoCorrecto = "true".equalsIgnoreCase(vfOpt) || "1".equals(vfOpt);
                opcionRespuestaRepository.save(new OpcionRespuesta("Verdadero", esVerdaderoCorrecto, pregunta));
                opcionRespuestaRepository.save(new OpcionRespuesta("Falso", !esVerdaderoCorrecto, pregunta));
            } else {
                String[] correctas = request.getParameterValues("correctas");
                if (correctas == null || correctas.length == 0) {
                    String cSingle = request.getParameter("correcta");
                    if (cSingle != null) correctas = new String[]{cSingle};
                    else correctas = new String[]{"1"};
                }
                java.util.Set<String> setCorrectas = new java.util.HashSet<>(java.util.Arrays.asList(correctas));

                int optIdx = 1;
                while (true) {
                    String optTexto = request.getParameter("opt" + optIdx);
                    if (optTexto == null) {
                        if (optIdx > 20) break;
                        optIdx++;
                        continue;
                    }
                    if (!optTexto.isBlank()) {
                        boolean esCorrecta = setCorrectas.contains(String.valueOf(optIdx));
                        opcionRespuestaRepository.save(new OpcionRespuesta(optTexto, esCorrecta, pregunta));
                    }
                    optIdx++;
                }
            }

            return org.springframework.http.ResponseEntity.ok(Map.of("success", true, "mensaje", "Pregunta actualizada correctamente"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/pools/pregunta/{id}/eliminar")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> eliminarPreguntaAjax(@PathVariable Integer id) {
        try {
            Optional<Pregunta> pOpt = evaluacionService.buscarPreguntaPorId(id);
            if (pOpt.isPresent()) {
                evaluacionService.borrarPregunta(pOpt.get());
                return org.springframework.http.ResponseEntity.ok(Map.of("success", true, "mensaje", "Pregunta eliminada"));
            }
            return org.springframework.http.ResponseEntity.notFound().build();
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/pools/{id}/baja")
    public String eliminarPool(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Pool pool = evaluacionService.buscarPoolPorId(id).orElse(null);
            Integer uId = (pool != null && pool.getUnidad() != null) ? pool.getUnidad().getId() : null;
            if (pool != null) {
                pool.setBaja(true);
                pool.setUltimaModificacion(LocalDateTime.now());
                poolRepository.save(pool);
            }
            ra.addFlashAttribute("mensaje", "Pool dado de baja correctamente.");
            return uId != null ? "redirect:/evaluaciones/pools?unidadId=" + uId : "redirect:/evaluaciones/pools";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/pools";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-57 a CU-60: AUTOEVALUACIONES
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/autoevaluaciones")
    public String buscarAutoevaluaciones(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                         @RequestParam(value = "unidadId", required = false) Integer unidadId,
                                         @RequestParam(value = "q", required = false) String query,
                                         Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        Curso curso = (cursoId != null) ? cursoService.buscarPorId(cursoId).orElse(null) : null;
        List<Unidad> unidades = (curso != null) ? unidadService.obtenerPorCurso(curso) : unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : null;

        if (curso == null && unidad != null && unidad.getCurso() != null) {
            curso = unidad.getCurso();
        }
        if (curso == null) {
            List<Curso> todos = cursoService.obtenerTodo();
            if (!todos.isEmpty()) curso = todos.get(0);
        }

        final Curso cursoFinal = curso;
        List<Autoevaluacion> autoevaluaciones = autoevaluacionRepository.findAll().stream()
                .filter(a -> !a.isBaja())
                .filter(a -> unidad == null || (a.getUnidad() != null && Objects.equals(a.getUnidad().getId(), unidad.getId())))
                .filter(a -> cursoFinal == null || (a.getUnidad() != null && unidades.contains(a.getUnidad())))
                .filter(a -> query == null || query.isBlank() || a.getNombre().toLowerCase().contains(query.toLowerCase()))
                .toList();

        model.addAttribute("curso", cursoFinal);
        model.addAttribute("cursoSeleccionado", cursoFinal);
        model.addAttribute("unidades", unidades);
        model.addAttribute("todasLasUnidades", unidadService.obtenerTodo().stream().filter(u -> !u.isBaja()).toList());
        model.addAttribute("todosLosPools", poolRepository.findAll().stream().filter(p -> !p.isBaja()).toList());
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("autoevaluaciones", autoevaluaciones);
        model.addAttribute("autoevaluacion", (autoevaluaciones.isEmpty() ? null : autoevaluaciones.get(0)));
        model.addAttribute("query", query);
        model.addAttribute("titulo", "CU-57 - Buscar autoevaluación | Idóneos Online");
        return "pages/evaluaciones/cu-57-buscar-autoevaluacion";
    }

    @PostMapping("/autoevaluaciones/guardar")
    public String guardarAutoevaluacion(@RequestParam Integer unidadId,
                                        @RequestParam String nombre,
                                        @RequestParam(defaultValue = "30") int tiempoLimite,
                                        @RequestParam(defaultValue = "10") int cantidadPreguntas,
                                        @RequestParam(required = false) Integer intentosPermitidos,
                                        @RequestParam(required = false) List<Integer> poolIds,
                                        RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            Autoevaluacion ae = new Autoevaluacion(nombre, tiempoLimite, LocalDateTime.now(), unidad);
            ae.setCantidadPreguntas(cantidadPreguntas);
            ae.setIntentosPermitidos(intentosPermitidos);
            ae = autoevaluacionRepository.save(ae);

            if (poolIds != null && !poolIds.isEmpty()) {
                List<Pool> pools = poolRepository.findAllById(poolIds);
                for (Pool p : pools) {
                    try {
                        evaluacionService.asociarPool(p, ae);
                    } catch (Exception ignored) {}
                }
            }

            ra.addFlashAttribute("mensaje", "Autoevaluación '" + nombre + "' creada exitosamente.");
            return "redirect:/evaluaciones/autoevaluaciones?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/autoevaluaciones";
        }
    }

    @PostMapping("/autoevaluaciones/{id}/editar")
    public String actualizarAutoevaluacion(@PathVariable Integer id,
                                           @RequestParam String nombre,
                                           @RequestParam int tiempoLimite,
                                           @RequestParam(defaultValue = "10") int cantidadPreguntas,
                                           @RequestParam(required = false) Integer intentosPermitidos,
                                           RedirectAttributes ra) {
        try {
            Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(id).orElseThrow(() -> new IllegalArgumentException("Autoevaluación no encontrada"));
            ae.setNombre(nombre);
            ae.setTiempoLimite(tiempoLimite);
            ae.setCantidadPreguntas(cantidadPreguntas);
            ae.setIntentosPermitidos(intentosPermitidos);
            ae.setUltimaModificacion(LocalDateTime.now());
            evaluacionService.guardarAutoevaluacion(ae);
            ra.addFlashAttribute("mensaje", "Autoevaluación modificada correctamente.");
            return "redirect:/evaluaciones/autoevaluaciones?unidadId=" + (ae.getUnidad() != null ? ae.getUnidad().getId() : "");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/autoevaluaciones";
        }
    }

    @PostMapping("/autoevaluaciones/{id}/baja")
    public String eliminarAutoevaluacion(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(id).orElse(null);
            Integer uId = (ae != null && ae.getUnidad() != null) ? ae.getUnidad().getId() : null;
            if (ae != null) {
                ae.setBaja(true);
                ae.setUltimaModificacion(LocalDateTime.now());
                autoevaluacionRepository.save(ae);
            }
            ra.addFlashAttribute("mensaje", "Autoevaluación dada de baja correctamente.");
            return uId != null ? "redirect:/evaluaciones/autoevaluaciones?unidadId=" + uId : "redirect:/evaluaciones/autoevaluaciones";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluaciones/autoevaluaciones";
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
    public String verCalificaciones(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                    @RequestParam(value = "unidadId", required = false) Integer unidadId,
                                    Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        Curso curso = (cursoId != null) ? cursoService.buscarPorId(cursoId).orElse(null) : null;
        List<Unidad> unidades = (curso != null) ? unidadService.obtenerPorCurso(curso) : unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        if (curso == null && unidad != null && unidad.getCurso() != null) {
            curso = unidad.getCurso();
        }
        if (curso == null) {
            List<Curso> todos = cursoService.obtenerTodo();
            if (!todos.isEmpty()) curso = todos.get(0);
        }

        List<Autoevaluacion> autoevaluaciones = (unidad != null) ? evaluacionService.buscarAutoevaluacionesPorUnidad(unidad) : List.of();
        List<IntentoAutoevaluacion> intentos = autoevaluaciones.stream()
                .flatMap(ae -> intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae).stream())
                .toList();

        model.addAttribute("curso", curso);
        model.addAttribute("cursoSeleccionado", curso);
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

        Map<Integer, List<OpcionRespuesta>> opcionesPorPregunta = new HashMap<>();
        for (Pregunta p : preguntas) {
            opcionesPorPregunta.put(p.getId(), opcionRespuestaRepository.findByPreguntaAndBajaFalse(p));
        }

        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("opcionesPorPregunta", opcionesPorPregunta);
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
