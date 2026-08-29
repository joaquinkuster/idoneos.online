package com.app.idoneos.controller.modulo_gestion_academica;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * TRAZABILIDAD — Controller para el Módulo de Gestión Académica (MOD-F-02).
 *
 * Mapea y conecta directamente las 25 pantallas académicas:
 *   CU-15 a CU-38
 */
@Controller
@RequestMapping("/academico")
public class AcademicoController {

    @Autowired private ProgramaService programaService;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private CursoService cursoService;
    @Autowired private UnidadService unidadService;
    @Autowired private MaterialService materialService;
    @Autowired private GlosarioService glosarioService;
    @Autowired private ForoService foroService;
    @Autowired private ConsultaForoRepository consultaForoRepository;
    @Autowired private InscripcionService inscripcionService;
    @Autowired private ProgresoService progresoService;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private CronogramaRepository cronogramaRepository;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private com.app.idoneos.repository.modulo_clases_vivo.ClaseEnVivoRepository claseEnVivoRepository;

    private Usuario obtenerUsuarioAutenticado(Authentication auth) {
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        String email = auth.getName();
        if (email != null) {
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
    // CU-15 a CU-18: PROGRAMAS ACADÉMICOS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/programas")
    public String buscarProgramas(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                  @RequestParam(value = "busqueda", required = false) String busqueda,
                                  @RequestParam(value = "baja", required = false, defaultValue = "true") Boolean baja,
                                  Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Programa> programas = programaService.buscarProgramasConFiltros(cursoId, busqueda, baja);
        model.addAttribute("programas", programas);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("cursoSeleccionado", cursoId);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("bajaSeleccionada", baja);

        // Mapeo de cohortes activas por programa para validación de dependencias en frontend (CU-18)
        Map<Integer, List<Cohorte>> cohortesPorPrograma = new HashMap<>();
        for (Programa p : programas) {
            List<Cohorte> coh = cohorteRepository.findByPrograma(p);
            cohortesPorPrograma.put(p.getId(), coh != null ? coh : List.of());
        }
        model.addAttribute("cohortesPorPrograma", cohortesPorPrograma);

        model.addAttribute("titulo", "CU-15 - Buscar programa | Idóneos Online");
        return "pages/academico/cu-15-buscar-programa";
    }

    @GetMapping("/programas/nuevo")
    public String registrarProgramaForm(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                        Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("titulo", "CU-16 - Registrar programa | Idóneos Online");
        return "pages/academico/cu-16-registrar-programa";
    }

    @PostMapping("/programas/guardar")
    public String guardarPrograma(@RequestParam(required = false) Integer cursoId,
                                  @RequestParam String nombre,
                                  @RequestParam(required = false) String descripcion,
                                  @RequestParam(required = false) String objetivos,
                                  @RequestParam(required = false) String bibliografia,
                                  @RequestParam(required = false) Integer cargaHorariaTotal,
                                  @RequestParam(required = false) Integer idProgramaAnterior,
                                  RedirectAttributes ra) {
        try {
            if (cursoId == null) {
                var primerCurso = cursoService.obtenerTodo().stream().findFirst();
                if (primerCurso.isPresent()) {
                    cursoId = primerCurso.get().getId();
                } else {
                    throw new IllegalArgumentException("Debe seleccionar un curso válido.");
                }
            }
            Programa p = programaService.registrarPrograma(cursoId, nombre, descripcion, "1.0", idProgramaAnterior);
            if (objetivos != null && !objetivos.isBlank()) p.setObjetivos(objetivos);
            if (bibliografia != null && !bibliografia.isBlank()) p.setBibliografia(bibliografia);
            if (cargaHorariaTotal != null && cargaHorariaTotal > 0) p.setCargaHorariaTotal(cargaHorariaTotal);
            programaRepository.save(p);
            ra.addFlashAttribute("mensaje", "Programa registrado con éxito.");
            return "redirect:/academico/programas";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/programas";
        }
    }

    @GetMapping("/programas/{id}/editar")
    public String modificarProgramaForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Programa> pOpt = programaService.buscarPorId(id);
        if (pOpt.isEmpty()) return "redirect:/academico/programas";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("programa", pOpt.get());
        model.addAttribute("titulo", "CU-17 - Modificar programa | Idóneos Online");
        return "pages/academico/cu-17-modificar-programa";
    }

    @PostMapping("/programas/{id}/editar")
    public String actualizarPrograma(@PathVariable Integer id,
                                     @RequestParam String nombre,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam(required = false) String objetivos,
                                     @RequestParam(required = false) String bibliografia,
                                     @RequestParam(required = false) Integer cargaHorariaTotal,
                                     RedirectAttributes ra) {
        try {
            Programa p = programaService.modificarPrograma(id, nombre, descripcion, "1.0");
            if (objetivos != null && !objetivos.isBlank()) p.setObjetivos(objetivos);
            if (bibliografia != null && !bibliografia.isBlank()) p.setBibliografia(bibliografia);
            if (cargaHorariaTotal != null) p.setCargaHorariaTotal(cargaHorariaTotal);
            programaRepository.save(p);
            ra.addFlashAttribute("mensaje", "Programa modificado con éxito.");
            return "redirect:/academico/programas";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/programas";
        }
    }

    @GetMapping("/programas/{id}/baja")
    public String darDeBajaProgramaView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Programa> pOpt = programaService.buscarPorId(id);
        if (pOpt.isEmpty()) return "redirect:/academico/programas";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("programa", pOpt.get());
        model.addAttribute("titulo", "CU-18 - Dar de baja programa | Idóneos Online");
        return "pages/academico/cu-18-dar-de-baja-programa";
    }

    @PostMapping("/programas/{id}/baja")
    public String eliminarPrograma(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            programaService.darDeBajaPrograma(id);
            ra.addFlashAttribute("mensaje", "Programa dado de baja correctamente.");
            return "redirect:/academico/programas";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/programas";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-19 a CU-22: UNIDADES TEMÁTICAS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/unidades")
    public String buscarUnidades(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                 Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Curso> cursos = cursoService.obtenerTodo();
        Curso curso = (cursoId != null) ? cursoService.buscarPorId(cursoId).orElse(null) : (cursos.isEmpty() ? null : cursos.get(0));

        List<Unidad> unidades = (curso != null) ? unidadService.obtenerPorCurso(curso) : unidadService.obtenerTodo();
        List<Unidad> todasUnidades = unidadService.obtenerTodo();

        model.addAttribute("cursos", cursos);
        model.addAttribute("curso", curso);
        model.addAttribute("cursoSeleccionado", curso);
        model.addAttribute("cursoNombre", curso != null ? curso.getNombre() : "Mercado de Capitales Argentino");
        model.addAttribute("unidades", unidades);
        model.addAttribute("todasUnidades", todasUnidades);
        model.addAttribute("titulo", "CU-19 - Buscar unidad | Idóneos Online");
        return "pages/academico/cu-19-buscar-unidad";
    }

    @GetMapping("/unidades/nueva")
    public String agregarUnidadForm(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                    Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("titulo", "CU-20 - Agregar unidad | Idóneos Online");
        return "pages/academico/cu-20-agregar-unidad";
    }

    @PostMapping("/unidades/guardar")
    public String guardarUnidad(@RequestParam(required = false) Integer cursoId,
                                @RequestParam(required = false) Integer programaId,
                                @RequestParam(required = false) Integer unidadExistenteId,
                                @RequestParam(required = false) String titulo,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam(required = false) String contenido,
                                @RequestParam(required = false) Integer numeroOrden,
                                @RequestParam(required = false) Integer semanasDuracion,
                                RedirectAttributes ra) {
        try {
            Unidad unidad;
            Programa programa = null;
            if (programaId != null) {
                programa = programaService.buscarPorId(programaId).orElse(null);
            } else if (cursoId != null) {
                Curso c = cursoService.buscarPorId(cursoId).orElse(null);
                if (c != null) {
                    List<Programa> progs = programaService.buscarPorCurso(c);
                    if (!progs.isEmpty()) programa = progs.get(0);
                }
            }

            if (unidadExistenteId != null) {
                unidad = unidadService.buscarPorId(unidadExistenteId).orElseThrow(() -> new IllegalArgumentException("Unidad existente no encontrada"));
                ra.addFlashAttribute("mensaje", "Unidad vinculada al programa correctamente.");
            } else {
                unidad = new Unidad(titulo, descripcion, (contenido != null && !contenido.isBlank()) ? contenido : "Contenido temático de la unidad");
                unidadService.guardar(unidad);
                ra.addFlashAttribute("mensaje", "Unidad agregada correctamente.");
            }

            if (programa != null) {
                int orden = (numeroOrden != null && numeroOrden > 0) ? numeroOrden : (cronogramaRepository.countByPrograma(programa) + 1);
                int dur = (semanasDuracion != null && semanasDuracion > 0) ? semanasDuracion : 1;
                if (!cronogramaRepository.existsByProgramaAndUnidad(programa, unidad)) {
                    Cronograma cron = new Cronograma(orden, dur, programa, unidad);
                    cronogramaRepository.save(cron);
                }
            }

            return cursoId != null ? "redirect:/academico/unidades?cursoId=" + cursoId : "redirect:/academico/unidades";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/unidades";
        }
    }

    @PostMapping("/unidades/asociar")
    public String asociarUnidadExistente(@RequestParam Integer programaId,
                                         @RequestParam Integer unidadId,
                                         @RequestParam(required = false) Integer numeroOrden,
                                         @RequestParam(required = false) Integer semanasDuracion,
                                         RedirectAttributes ra) {
        try {
            Programa programa = programaService.buscarPorId(programaId)
                    .orElseThrow(() -> new IllegalArgumentException("Programa no encontrado con ID: " + programaId));
            Unidad unidad = unidadService.buscarPorId(unidadId)
                    .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada con ID: " + unidadId));

            int orden = (numeroOrden != null && numeroOrden > 0) ? numeroOrden : (cronogramaRepository.countByPrograma(programa) + 1);
            int dur = (semanasDuracion != null && semanasDuracion > 0) ? semanasDuracion : 1;

            if (!cronogramaRepository.existsByProgramaAndUnidad(programa, unidad)) {
                Cronograma cron = new Cronograma(orden, dur, programa, unidad);
                cronogramaRepository.save(cron);
            }

            ra.addFlashAttribute("mensaje", "Unidad asociada al cronograma del programa exitosamente.");
            Integer cId = (programa.getCurso() != null) ? programa.getCurso().getIdCurso() : null;
            return cId != null ? "redirect:/academico/unidades?cursoId=" + cId : "redirect:/academico/unidades";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/unidades";
        }
    }

    @GetMapping("/unidades/{id}/editar")
    public String modificarUnidadForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(id);
        if (uOpt.isEmpty()) return "redirect:/academico/unidades";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidad", uOpt.get());
        model.addAttribute("titulo", "CU-21 - Modificar unidad | Idóneos Online");
        return "pages/academico/cu-21-modificar-unidad";
    }

    @PostMapping("/unidades/{id}/editar")
    public String actualizarUnidad(@PathVariable Integer id,
                                   @RequestParam String titulo,
                                   @RequestParam(required = false) String descripcion,
                                   @RequestParam(required = false) String contenido,
                                   RedirectAttributes ra) {
        try {
            Unidad u = unidadService.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            u.setTitulo(titulo);
            u.setDescripcion(descripcion);
            if (contenido != null && !contenido.isBlank()) {
                u.setContenido(contenido);
            }
            unidadService.modificar(u);
            ra.addFlashAttribute("mensaje", "Unidad actualizada con éxito.");
            Integer cId = (u.getCurso() != null) ? u.getCurso().getIdCurso() : null;
            return cId != null ? "redirect:/academico/unidades?cursoId=" + cId : "redirect:/academico/unidades";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/unidades/" + id + "/editar";
        }
    }

    @GetMapping("/unidades/{id}/quitar")
    public String quitarUnidadView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(id);
        if (uOpt.isEmpty()) return "redirect:/academico/unidades";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidad", uOpt.get());
        model.addAttribute("titulo", "CU-22 - Quitar unidad | Idóneos Online");
        return "pages/academico/cu-22-quitar-unidad";
    }

    @PostMapping({"/unidades/{id}/quitar", "/unidades/{id}/baja"})
    public String eliminarUnidad(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Unidad u = unidadService.buscarPorId(id).orElse(null);
            Integer cId = (u != null && u.getCurso() != null) ? u.getCurso().getIdCurso() : null;
            unidadService.darDeBaja(id);
            ra.addFlashAttribute("mensaje", "Unidad desvinculada con éxito.");
            return cId != null ? "redirect:/academico/unidades?cursoId=" + cId : "redirect:/academico/unidades";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/unidades";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-23, CU-24, CU-25: CRONOGRAMA Y PARTICIPANTES
    // ─────────────────────────────────────────────────────────────

    @GetMapping({"/cronogramas", "/cronograma"})
    public String buscarCronograma(@RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                   @RequestParam(value = "cursoId", required = false) Integer cursoId,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Cohorte> cohortes = cohorteRepository.findAll();
        Cohorte cohorte = (cohorteId != null) ? cohorteRepository.findById(cohorteId).orElse(null) : (cohortes.isEmpty() ? null : cohortes.get(0));

        Curso curso = null;
        if (cursoId != null) {
            curso = cursoService.buscarPorId(cursoId).orElse(null);
        } else if (cohorte != null && cohorte.getPrograma() != null && cohorte.getPrograma().getCurso() != null) {
            curso = cohorte.getPrograma().getCurso();
        } else {
            List<Curso> todos = cursoService.obtenerTodo();
            if (!todos.isEmpty()) curso = todos.get(0);
        }

        List<Cronograma> cronogramasList = (cohorte != null && cohorte.getPrograma() != null) ? cohorte.getPrograma().getCronogramas() : List.of();
        model.addAttribute("cronogramas", cronogramasList);
        model.addAttribute("cohortes", cohortes);
        model.addAttribute("cohorteSeleccionada", cohorte);
        model.addAttribute("cohorte", cohorte);
        model.addAttribute("curso", curso);
        model.addAttribute("cursoSeleccionado", curso);
        if (curso != null) {
            model.addAttribute("unidades", unidadService.obtenerPorCurso(curso));
        } else {
            model.addAttribute("unidades", unidadService.obtenerTodo());
        }
        model.addAttribute("titulo", "CU-23 - Buscar cronograma | Idóneos Online");
        return "pages/academico/cu-23-buscar-cronograma";
    }

    @GetMapping({"/cronogramas/modificar", "/cronograma/modificar", "/cronogramas/{id}/editar"})
    public String modificarCronogramaForm(@PathVariable(required = false) Integer id,
                                          @RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                          Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        Integer targetId = (id != null) ? id : cohorteId;
        Cohorte cohorte = null;
        if (targetId != null) {
            cohorte = cohorteRepository.findById(targetId).orElse(null);
        }
        if (cohorte == null) {
            List<Cohorte> cohortes = cohorteRepository.findAll();
            if (!cohortes.isEmpty()) cohorte = cohortes.get(0);
        }

        Curso curso = (cohorte != null && cohorte.getPrograma() != null) ? cohorte.getPrograma().getCurso() : null;
        if (curso == null) {
            List<Curso> todos = cursoService.obtenerTodo();
            if (!todos.isEmpty()) curso = todos.get(0);
        }

        List<Cronograma> cronogramasList = (cohorte != null && cohorte.getPrograma() != null) ? cohorte.getPrograma().getCronogramas() : List.of();
        model.addAttribute("cronogramas", cronogramasList);
        model.addAttribute("cohorte", cohorte);
        model.addAttribute("cohorteSeleccionada", cohorte);
        model.addAttribute("curso", curso);
        model.addAttribute("cursoSeleccionado", curso);
        model.addAttribute("unidades", (curso != null) ? unidadService.obtenerPorCurso(curso) : unidadService.obtenerTodo());
        model.addAttribute("titulo", "CU-24 - Modificar cronograma | Idóneos Online");
        return "pages/academico/cu-24-modificar-cronograma";
    }

    @PostMapping({"/cronograma/guardar", "/cronogramas/guardar", "/cronogramas/modificar"})
    public String guardarModificacionesCronograma(@RequestParam(required = false) Integer cronogramaId,
                                                  @RequestParam(required = false) Integer cohorteId,
                                                  @RequestParam(required = false) Integer semanasDuracion,
                                                  @RequestParam(required = false) Integer numeroOrden,
                                                  RedirectAttributes ra) {
        try {
            if (cronogramaId != null && cronogramaRepository != null) {
                cronogramaRepository.findById(cronogramaId).ifPresent(c -> {
                    if (semanasDuracion != null) c.setSemanasDuracion(semanasDuracion);
                    if (numeroOrden != null) c.setNumeroOrden(numeroOrden);
                    cronogramaRepository.save(c);
                });
            }
            ra.addFlashAttribute("mensaje", "Cronograma actualizado con éxito.");
            return (cohorteId != null) ? "redirect:/academico/cronograma?cohorteId=" + cohorteId : "redirect:/academico/cronograma";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/cronograma";
        }
    }

    @GetMapping("/participantes")
    public String verParticipantes(@RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                   @RequestParam(value = "cursoId", required = false) Integer cursoId,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Cohorte> cohortes = cohorteRepository.findAll();
        Cohorte cohorte = (cohorteId != null) ? cohorteRepository.findById(cohorteId).orElse(null) : (cohortes.isEmpty() ? null : cohortes.get(0));

        Curso curso = null;
        if (cursoId != null) {
            curso = cursoService.buscarPorId(cursoId).orElse(null);
        } else if (cohorte != null && cohorte.getPrograma() != null && cohorte.getPrograma().getCurso() != null) {
            curso = cohorte.getPrograma().getCurso();
        } else {
            List<Curso> todos = cursoService.obtenerTodo();
            if (!todos.isEmpty()) curso = todos.get(0);
        }

        List<Inscripcion> inscripciones = (cohorte != null) ? inscripcionService.obtenerPorCohorte(cohorte) : inscripcionService.obtenerTodo();
        model.addAttribute("cohortes", cohortes);
        model.addAttribute("cohorteSeleccionada", cohorte);
        model.addAttribute("cohorte", cohorte);
        model.addAttribute("curso", curso);
        model.addAttribute("cursoSeleccionado", curso);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("participantes", inscripciones);
        model.addAttribute("titulo", "CU-25 - Ver participantes | Idóneos Online");
        return "pages/academico/cu-25-ver-participantes";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-26 & CU-26b: AULA VIRTUAL Y MODO EDICIÓN
    // ─────────────────────────────────────────────────────────────

    @GetMapping({"/curso", "/curso/{id}", "/aula"})
    public String accederCurso(@PathVariable(required = false) Integer id,
                               @RequestParam(value = "cursoId", required = false) Integer cursoIdParam,
                               Model model, Authentication auth) {
        Usuario usuario = obtenerUsuarioAutenticado(auth);
        if (usuario == null) return "redirect:/login";

        Integer targetId = (id != null) ? id : (cursoIdParam != null ? cursoIdParam : null);
        Curso curso = null;
        if (targetId != null) {
            curso = cursoService.buscarPorId(targetId).orElse(null);
        } else {
            List<Curso> todos = cursoService.obtenerTodo();
            if (!todos.isEmpty()) curso = todos.get(0);
        }

        if (curso == null) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("curso", new Curso());
            model.addAttribute("unidades", List.of());
            model.addAttribute("titulo", "CU-26 - Aula Virtual | Idóneos Online");
            return "pages/academico/cu-26-acceder-curso";
        }

        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);

        // Datos de progreso del alumno
        Optional<Inscripcion> inscripcionOpt = inscripcionService.obtenerPorAlumnoYCurso(usuario, curso);
        Map<Integer, Boolean> unidadHabilitadaMap = new HashMap<>();
        Map<Integer, Boolean> unidadCompletadaMap = new HashMap<>();
        int porcentajeAvance = 0;
        boolean atrasado = false;

        if (inscripcionOpt.isPresent()) {
            Inscripcion inscripcion = inscripcionOpt.get();
            porcentajeAvance = progresoService.calcularPorcentajeAvance(inscripcion);
            atrasado = progresoService.detectarAtraso(inscripcion);
            for (Unidad u : unidades) {
                unidadHabilitadaMap.put(u.getId(), progresoService.esUnidadHabilitada(inscripcion, u));
                unidadCompletadaMap.put(u.getId(), progresoService.unidadCompletada(inscripcion, u));
            }
            model.addAttribute("inscripcion", inscripcion);
        } else {
            // Si es docente o admin, todas las unidades están habilitadas
            for (Unidad u : unidades) {
                unidadHabilitadaMap.put(u.getId(), true);
                unidadCompletadaMap.put(u.getId(), false);
            }
        }

        // Clase en vivo activa / programada del curso
        final Curso cursoFinal = curso;
        ClaseEnVivo claseVivoActiva = null;
        if (cursoFinal != null && claseEnVivoRepository != null) {
            claseVivoActiva = claseEnVivoRepository.findAll().stream()
                    .filter(c -> !c.getBaja() && c.getCohorte() != null && c.getCohorte().getPrograma() != null && cursoFinal.equals(c.getCohorte().getPrograma().getCurso()))
                    .findFirst().orElse(null);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("claseEnVivo", claseVivoActiva);
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadHabilitadaMap", unidadHabilitadaMap);
        model.addAttribute("unidadCompletadaMap", unidadCompletadaMap);
        model.addAttribute("porcentajeAvance", porcentajeAvance);
        model.addAttribute("atrasado", atrasado);
        model.addAttribute("titulo", "CU-26 - Aula Virtual: " + curso.getNombre() + " | Idóneos Online");
        return "pages/academico/cu-26-acceder-curso";
    }

    @GetMapping("/curso/{id}/edicion")
    public String accederCursoModoEdicion(@PathVariable Integer id, Model model, Authentication auth) {
        Usuario usuario = obtenerUsuarioAutenticado(auth);
        if (usuario == null) return "redirect:/login";

        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cOpt.get();
        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("modoEdicion", true);
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "CU-26b - Modo Edición: " + curso.getNombre() + " | Idóneos Online");
        return "pages/academico/cu-26b-acceder-curso-modo-edicion-docente-administrador";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-27 a CU-30: MATERIALES EDUCATIVOS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/materiales")
    public String buscarMateriales(@RequestParam(value = "cursoId", required = false) Integer cursoId,
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

        List<Material> materiales = (unidad != null) ? materialService.obtenerPorUnidad(unidad) : List.of();
        model.addAttribute("curso", curso);
        model.addAttribute("cursoSeleccionado", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("materiales", materiales);
        model.addAttribute("titulo", "CU-27 - Buscar material | Idóneos Online");
        return "pages/academico/cu-27-buscar-material";
    }

    @GetMapping("/materiales/nuevo")
    public String subirMaterialForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                    Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-28 - Subir material | Idóneos Online");
        return "pages/academico/cu-28-subir-material";
    }

    @PostMapping("/materiales/guardar")
    public String guardarMaterial(@RequestParam Integer unidadId,
                                  @RequestParam(required = false) String nombre,
                                  @RequestParam(value = "titulo", required = false) String titulo,
                                  @RequestParam(required = false) String descripcion,
                                  @RequestParam String url,
                                  @RequestParam(required = false) Integer tipoMaterialId,
                                  @RequestParam(required = false) Boolean oculto,
                                  Authentication auth,
                                  RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inválida"));
            TipoMaterial tipo = (tipoMaterialId != null) ? tipoMaterialRepository.findById(tipoMaterialId).orElse(null) : null;
            if (tipo == null) {
                List<TipoMaterial> tipos = tipoMaterialRepository.findAll();
                if (!tipos.isEmpty()) tipo = tipos.get(0);
            }
            Docente docente = null;
            Usuario u = obtenerUsuarioAutenticado(auth);
            if (u != null) {
                docente = docenteRepository.findById(u.getId()).orElse(null);
            }
            if (docente == null) {
                List<Docente> docentes = docenteRepository.findAll();
                docente = docentes.isEmpty() ? null : docentes.get(0);
            }

            String tituloFinal = (titulo != null && !titulo.isBlank()) ? titulo : nombre;
            Material m = new Material(tituloFinal, docente, tipo, unidad);
            m.setRutaArchivo(url);
            m.setContenido(descripcion);
            if (oculto != null) m.setOculto(oculto);
            materialService.guardar(m);
            ra.addFlashAttribute("mensaje", "Material subido exitosamente.");
            return "redirect:/academico/materiales?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/materiales/nuevo?unidadId=" + unidadId;
        }
    }

    @GetMapping("/materiales/{id}/editar")
    public String modificarMaterialForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Material> mOpt = materialService.buscarPorId(id);
        if (mOpt.isEmpty()) return "redirect:/academico/materiales";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("material", mOpt.get());
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "CU-29 - Modificar material | Idóneos Online");
        return "pages/academico/cu-29-modificar-material";
    }

    @PostMapping("/materiales/{id}/editar")
    public String actualizarMaterial(@PathVariable Integer id,
                                     @RequestParam(required = false) String nombre,
                                     @RequestParam(value = "titulo", required = false) String titulo,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam String url,
                                     @RequestParam(required = false) Boolean oculto,
                                     RedirectAttributes ra) {
        try {
            Material m = materialService.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Material no encontrado"));
            String tituloFinal = (titulo != null && !titulo.isBlank()) ? titulo : nombre;
            if (tituloFinal != null) m.setTitulo(tituloFinal);
            m.setContenido(descripcion);
            m.setRutaArchivo(url);
            if (oculto != null) m.setOculto(oculto);
            materialService.modificar(m);
            ra.addFlashAttribute("mensaje", "Material actualizado con éxito.");
            return "redirect:/academico/materiales?unidadId=" + m.getUnidad().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/materiales/" + id + "/editar";
        }
    }

    @GetMapping("/materiales/{id}/baja")
    public String darDeBajaMaterialView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Material> mOpt = materialService.buscarPorId(id);
        if (mOpt.isEmpty()) return "redirect:/academico/materiales";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("material", mOpt.get());
        model.addAttribute("titulo", "CU-30 - Dar de baja material | Idóneos Online");
        return "pages/academico/cu-30-dar-de-baja-material";
    }

    @PostMapping("/materiales/{id}/baja")
    public String eliminarMaterial(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Material m = materialService.buscarPorId(id).orElse(null);
            Integer uId = (m != null && m.getUnidad() != null) ? m.getUnidad().getId() : null;
            materialService.darDeBaja(id);
            ra.addFlashAttribute("mensaje", "Material dado de baja correctamente.");
            return uId != null ? "redirect:/academico/materiales?unidadId=" + uId : "redirect:/academico/materiales";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/materiales/" + id + "/baja";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-31 a CU-34: GLOSARIO
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/glosario")
    public String buscarGlosario(@RequestParam(value = "cursoId", required = false) Integer cursoId,
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

        List<TerminoGlosario> terminos = (unidad != null) ? glosarioService.obtenerPorUnidad(unidad) : List.of();
        model.addAttribute("curso", curso);
        model.addAttribute("cursoSeleccionado", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("terminos", terminos);
        model.addAttribute("titulo", "CU-31 - Buscar término de glosario | Idóneos Online");
        return "pages/academico/cu-31-buscar-termino-de-glosario";
    }

    @GetMapping("/glosario/nuevo")
    public String registrarTerminoForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                       Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-32 - Registrar término de glosario | Idóneos Online");
        return "pages/academico/cu-32-registrar-termino-de-glosario";
    }

    @PostMapping("/glosario/guardar")
    public String guardarTermino(@RequestParam Integer unidadId,
                                 @RequestParam String termino,
                                 @RequestParam String definicion,
                                 RedirectAttributes ra) {
        try {
            Unidad u = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            glosarioService.registrarTermino(new TerminoGlosario(termino, definicion, u));
            ra.addFlashAttribute("mensaje", "Término registrado con éxito.");
            return "redirect:/academico/glosario?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/glosario/nuevo?unidadId=" + unidadId;
        }
    }

    @GetMapping("/glosario/{id}/editar")
    public String modificarTerminoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<TerminoGlosario> tOpt = glosarioService.buscarPorId(id);
        if (tOpt.isEmpty()) return "redirect:/academico/glosario";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("termino", tOpt.get());
        model.addAttribute("titulo", "CU-33 - Modificar término de glosario | Idóneos Online");
        return "pages/academico/cu-33-modificar-termino-de-glosario";
    }

    @PostMapping("/glosario/{id}/editar")
    public String actualizarTermino(@PathVariable Integer id,
                                    @RequestParam String termino,
                                    @RequestParam String definicion,
                                    RedirectAttributes ra) {
        try {
            TerminoGlosario tg = glosarioService.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Término no encontrado"));
            tg.setTermino(termino);
            tg.setDefinicion(definicion);
            glosarioService.modificarTermino(tg);
            ra.addFlashAttribute("mensaje", "Término actualizado correctamente.");
            return "redirect:/academico/glosario?unidadId=" + tg.getUnidad().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/glosario/" + id + "/editar";
        }
    }

    @GetMapping("/glosario/{id}/baja")
    public String darDeBajaTerminoView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<TerminoGlosario> tOpt = glosarioService.buscarPorId(id);
        if (tOpt.isEmpty()) return "redirect:/academico/glosario";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("termino", tOpt.get());
        model.addAttribute("titulo", "CU-34 - Dar de baja término de glosario | Idóneos Online");
        return "pages/academico/cu-34-dar-de-baja-termino-de-glosario";
    }

    @PostMapping("/glosario/{id}/baja")
    public String eliminarTermino(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            TerminoGlosario tg = glosarioService.buscarPorId(id).orElse(null);
            Integer uId = (tg != null && tg.getUnidad() != null) ? tg.getUnidad().getId() : null;
            glosarioService.darDeBaja(id);
            ra.addFlashAttribute("mensaje", "Término dado de baja con éxito.");
            return uId != null ? "redirect:/academico/glosario?unidadId=" + uId : "redirect:/academico/glosario";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/glosario/" + id + "/baja";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-35 a CU-38: FORO DE CONSULTAS
    // ─────────────────────────────────────────────────────────────

    @GetMapping({"/foro", "/consultas"})
    public String buscarForo(@RequestParam(value = "cursoId", required = false) Integer cursoId,
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

        List<ConsultaForo> consultas = (unidad != null) ? foroService.obtenerConsultasPorUnidad(unidad) : List.of();
        model.addAttribute("curso", curso);
        model.addAttribute("cursoSeleccionado", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("consultas", consultas);
        model.addAttribute("titulo", "CU-35 - Buscar consulta de foro | Idóneos Online");
        return "pages/academico/cu-35-buscar-consulta-de-foro";
    }

    @GetMapping({"/foro/nueva", "/consultas/nueva"})
    public String registrarConsultaForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                        Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-36 - Registrar consulta de foro | Idóneos Online");
        return "pages/academico/cu-36-registrar-consulta-de-foro";
    }

    @PostMapping({"/foro/guardar", "/consultas/guardar"})
    public String guardarConsulta(@RequestParam(required = false) Integer unidadId,
                                  @RequestParam String texto,
                                  Authentication auth, RedirectAttributes ra) {
        Usuario u = obtenerUsuarioAutenticado(auth);
        if (u == null) return "redirect:/login";
        try {
            Unidad unidad = null;
            if (unidadId != null) {
                unidad = unidadService.buscarPorId(unidadId).orElse(null);
            }
            if (unidad == null) {
                List<Unidad> unidades = unidadService.obtenerTodo();
                if (!unidades.isEmpty()) unidad = unidades.get(0);
            }
            if (unidad == null) throw new IllegalArgumentException("No hay unidades disponibles para registrar la consulta");
            
            foroService.crearConsulta(texto, u, unidad);
            ra.addFlashAttribute("mensaje", "Consulta publicada en el foro.");
            return "redirect:/academico/consultas?unidadId=" + unidad.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return unidadId != null ? "redirect:/academico/consultas/nueva?unidadId=" + unidadId : "redirect:/academico/consultas";
        }
    }

    @GetMapping({"/foro/{id}/editar", "/consultas/{id}/editar"})
    public String modificarConsultaForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ConsultaForo> cOpt = foroService.buscarConsultaPorId(id);
        if (cOpt.isEmpty()) return "redirect:/academico/consultas";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("consulta", cOpt.get());
        model.addAttribute("titulo", "CU-37 - Modificar consulta de foro | Idóneos Online");
        return "pages/academico/cu-37-modificar-consulta-de-foro";
    }

    @PostMapping({"/foro/{id}/editar", "/consultas/{id}/editar"})
    public String actualizarConsulta(@PathVariable Integer id,
                                     @RequestParam String texto,
                                     RedirectAttributes ra) {
        try {
            ConsultaForo c = foroService.buscarConsultaPorId(id).orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));
            c.setTexto(texto);
            foroService.modificarConsulta(c);
            ra.addFlashAttribute("mensaje", "Consulta modificada con éxito.");
            return "redirect:/academico/consultas?unidadId=" + c.getUnidad().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/consultas/" + id + "/editar";
        }
    }

    @GetMapping({"/foro/{id}/baja", "/consultas/{id}/baja"})
    public String darDeBajaConsultaView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ConsultaForo> cOpt = foroService.buscarConsultaPorId(id);
        if (cOpt.isEmpty()) return "redirect:/academico/consultas";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("consulta", cOpt.get());
        model.addAttribute("titulo", "CU-38 - Dar de baja consulta de foro | Idóneos Online");
        return "pages/academico/cu-38-dar-de-baja-consulta-de-foro";
    }

    @PostMapping({"/foro/{id}/baja", "/consultas/{id}/baja"})
    public String eliminarConsulta(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            ConsultaForo c = foroService.buscarConsultaPorId(id).orElse(null);
            Integer uId = (c != null && c.getUnidad() != null) ? c.getUnidad().getId() : null;
            foroService.darDeBajaConsulta(id);
            ra.addFlashAttribute("mensaje", "Consulta dada de baja correctamente.");
            return uId != null ? "redirect:/academico/consultas?unidadId=" + uId : "redirect:/academico/consultas";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/consultas/" + id + "/baja";
        }
    }
}
