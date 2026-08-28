package com.app.idoneos.controller.modulo_cursos;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
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
 * TRAZABILIDAD — Controller para el Módulo de Cursos (MOD-F-01).
 *
 * Mapea y conecta directamente las 14 pantallas de Cursos:
 *   CU-01 — Buscar curso                  → GET /cursos
 *   CU-02 — Ver mis cursos                → GET /cursos/mis-cursos
 *   CU-03 — Registrar curso               → GET /cursos/nuevo, POST /cursos/guardar
 *   CU-04 — Modificar curso               → GET /cursos/{id}/editar, POST /cursos/{id}/editar
 *   CU-05 — Dar de baja curso             → GET/POST /cursos/{id}/baja
 *   CU-06 — Explorar catálogo de cursos   → GET /cursos/catalogo y GET /cursos/{id}
 *   CU-07 — Buscar categoría              → GET /cursos/categorias
 *   CU-08 — Registrar categoría           → GET /cursos/categorias/nueva, POST /cursos/categorias/guardar
 *   CU-09 — Modificar categoría           → GET /cursos/categorias/{id}/editar, POST /cursos/categorias/{id}/editar
 *   CU-10 — Dar de baja categoría         → GET/POST /cursos/categorias/{id}/baja
 *   CU-11 — Buscar cohorte                → GET /cursos/cohortes
 *   CU-12 — Registrar cohorte             → GET /cursos/cohortes/nueva, POST /cursos/cohortes/guardar
 *   CU-13 — Modificar cohorte             → GET /cursos/cohortes/{id}/editar, POST /cursos/cohortes/{id}/editar
 *   CU-14 — Dar de baja cohorte           → GET/POST /cursos/cohortes/{id}/baja
 */
@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired private CursoService cursoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private CohorteService cohorteService;
    @Autowired private InscripcionService inscripcionService;
    @Autowired private ProgresoService progresoService;
    @Autowired private UnidadService unidadService;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private NivelRepository nivelRepository;
    @Autowired private ModalidadRepository modalidadRepository;
    @Autowired private SupervisorRepository supervisorRepository;
    @Autowired private com.app.idoneos.repository.modulo_usuarios.UsuarioRepository usuarioRepository;
    @Autowired private com.app.idoneos.repository.modulo_gestion_academica.ProgramaRepository programaRepository;
    @Autowired private com.app.idoneos.repository.modulo_gestion_academica.CronogramaRepository cronogramaRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private com.app.idoneos.repository.modulo_inscripciones.InscripcionRepository inscripcionRepository;

    private Usuario obtenerUsuarioActual(Authentication auth) {
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        } else if (auth.getName() != null) {
            return usuarioRepository.findByCorreo(auth.getName()).orElse(null);
        }
        return null;
    }

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        Usuario u = obtenerUsuarioActual(auth);
        if (u != null) {
            model.addAttribute("usuario", u);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-01 & CU-06: BÚSQUEDA Y CATÁLOGO DE CURSOS
    // ─────────────────────────────────────────────────────────────

    /**
     * CU-01 — Buscar curso (Docente / Administrador).
     * Vista: cu-01-buscar-curso.html
     */
    @GetMapping
    public String buscarCursos(@RequestParam(value = "busqueda", required = false) String busqueda,
                               @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
                               @RequestParam(value = "nivelId", required = false) Integer nivelId,
                               @RequestParam(value = "docenteId", required = false) Integer docenteId,
                               @RequestParam(value = "modalidadId", required = false) Integer modalidadId,
                               @RequestParam(value = "ordenBajasPrimero", defaultValue = "false") Boolean ordenBajasPrimero,
                               @RequestParam(value = "page", defaultValue = "0") Integer page,
                               Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);

        List<Curso> cursos;
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            Usuario u = (Usuario) auth.getPrincipal();
            if (u.esDocente() && !u.esAdmin() && u.getDocente() != null) {
                // Restricción por rol docente titular / ayudante
                cursos = cursoService.buscarCursosPorDocente(u.getDocente().getId());
            } else {
                cursos = cursoService.buscarCursosAdminConFiltros(busqueda, categoriaId, nivelId, docenteId, null, ordenBajasPrimero);
            }
        } else {
            cursos = cursoService.buscarCursosPublicadosConFiltros(busqueda, categoriaId, modalidadId);
        }

        int pageSize = 8;
        int totalCursos = cursos.size();
        int totalPages = (int) Math.ceil((double) totalCursos / pageSize);
        if (totalPages == 0) totalPages = 1;
        int currentPage = (page != null && page >= 0) ? page : 0;
        if (currentPage >= totalPages) currentPage = totalPages - 1;

        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalCursos);
        List<Curso> cursosPaginados = (fromIndex <= toIndex && fromIndex < totalCursos) ? cursos.subList(fromIndex, toIndex) : Collections.emptyList();

        // Mapeo dinámico de inscripciones activas por curso para validación en CU-05
        java.util.Map<Integer, List<Inscripcion>> inscripcionesPorCurso = new java.util.HashMap<>();
        // Mapeo dinámico de programas activos por curso para validación bloqueante en CU-05
        java.util.Map<Integer, List<Programa>> programasPorCurso = new java.util.HashMap<>();
        for (Curso c : cursosPaginados) {
            inscripcionesPorCurso.put(c.getId(), inscripcionRepository.findByCursoAndBajaFalse(c));
            programasPorCurso.put(c.getId(), programaRepository.findByCursoAndBajaFalse(c));
        }

        model.addAttribute("cursos", cursosPaginados);
        model.addAttribute("inscripcionesPorCurso", inscripcionesPorCurso);
        model.addAttribute("programasPorCurso", programasPorCurso);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("modalidades", modalidadRepository.findAll());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("ordenBajasPrimero", ordenBajasPrimero);
        model.addAttribute("titulo", "CU-01 - Buscar curso | Idóneos Online");

        return "pages/cursos/cu-01-buscar-curso";
    }

    /**
     * CU-06 — Explorar catálogo de cursos (Público / Alumno).
     * Vista: cu-06-explorar-catalogo-de-cursos.html
     */
    @GetMapping("/catalogo")
    public String explorarCatalogo(@RequestParam(value = "busqueda", required = false) String busqueda,
                                   @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
                                   @RequestParam(value = "modalidadId", required = false) Integer modalidadId,
                                   @RequestParam(value = "cursoId", required = false) Integer cursoId,
                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);

        List<Curso> todosLosCursos = cursoService.buscarCursosPublicadosConFiltros(busqueda, categoriaId, modalidadId);
        
        // Paginación en memoria de 4 cursos por página para catálogo
        int pageSize = 4;
        int totalCursos = todosLosCursos.size();
        int totalPages = (int) Math.ceil((double) totalCursos / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalCursos);
        List<Curso> cursosPaginados = (fromIndex <= toIndex && fromIndex < totalCursos) ? todosLosCursos.subList(fromIndex, toIndex) : Collections.emptyList();

        model.addAttribute("cursos", cursosPaginados);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("modalidades", modalidadRepository.findAll());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("titulo", "CU-06 - Catálogo de Cursos | Idóneos Online");

        // Calcular cantidad de unidades temáticas por curso
        java.util.Map<Integer, Integer> cantUnidadesPorCurso = new java.util.HashMap<>();
        for (Curso c : todosLosCursos) {
            List<Programa> progs = programaRepository.findByCursoAndBajaFalse(c);
            int cant = 0;
            if (!progs.isEmpty()) {
                cant = cronogramaRepository.countByPrograma(progs.get(0));
            }
            cantUnidadesPorCurso.put(c.getId(), cant > 0 ? cant : 3);
        }
        model.addAttribute("cantUnidadesPorCurso", cantUnidadesPorCurso);

        // Si se selecciona un curso o por defecto el primero de la página actual
        Curso cursoSeleccionado = null;
        if (cursoId != null) {
            cursoSeleccionado = cursoService.buscarPorId(cursoId).orElse(null);
        }
        if (cursoSeleccionado == null && !cursosPaginados.isEmpty()) {
            cursoSeleccionado = cursosPaginados.get(0);
        } else if (cursoSeleccionado == null && !todosLosCursos.isEmpty()) {
            cursoSeleccionado = todosLosCursos.get(0);
        }

        if (cursoSeleccionado != null) {
            model.addAttribute("cursoSeleccionado", cursoSeleccionado);
            List<Programa> programas = programaRepository.findByCursoAndBajaFalse(cursoSeleccionado);
            if (!programas.isEmpty()) {
                Programa primerProg = programas.get(0);
                model.addAttribute("programaSeleccionado", primerProg);
                model.addAttribute("cronogramas", cronogramaRepository.findByProgramaOrderByNumeroOrdenAsc(primerProg));
                model.addAttribute("cohortesAbiertas", cohorteRepository.findByProgramaAndBajaFalse(primerProg));
            }
        }

        return "pages/cursos/cu-06-explorar-catalogo-de-cursos";
    }

    /**
     * CU-02 — Ver mis cursos (Alumno).
     * Vista: cu-02-ver-mis-cursos.html
     */
    @GetMapping("/mis-cursos")
    public String verMisCursos(Authentication auth, Model model) {
        Usuario usuario = obtenerUsuarioActual(auth);
        if (usuario == null) return "redirect:/login";

        List<Inscripcion> inscripciones = inscripcionService.obtenerPorAlumno(usuario);
        List<Curso> misCursos = new ArrayList<>();
        if (inscripciones != null) {
            for (Inscripcion i : inscripciones) {
                if (!i.getBaja() && i.getCohorte() != null && i.getCohorte().getPrograma() != null) {
                    misCursos.add(i.getCohorte().getPrograma().getCurso());
                }
            }
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("inscripciones", inscripciones != null ? inscripciones : Collections.emptyList());
        model.addAttribute("misCursos", misCursos);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "CU-02 - Mis Cursos | Idóneos Online");

        return "pages/cursos/cu-02-ver-mis-cursos";
    }

    @GetMapping("/{id}/ficha")
    public String verFichaCurso(@PathVariable Integer id) {
        return "redirect:/cursos/catalogo?cursoId=" + id;
    }

    // ─────────────────────────────────────────────────────────────
    // CU-03, CU-04, CU-05: GESTIÓN DE CURSO
    // ─────────────────────────────────────────────────────────────

    /**
     * CU-03 — Registrar curso (Formulario GET).
     * Vista: cu-03-registrar-curso.html
     */
    @GetMapping("/nuevo")
    public String registrarCursoForm(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("modalidades", modalidadRepository.findAll());
        model.addAttribute("titulo", "CU-03 - Registrar curso | Idóneos Online");
        return "pages/cursos/cu-03-registrar-curso";
    }

    /**
     * CU-03 — Registrar curso (POST).
     */
    @PostMapping("/guardar")
    public String guardarCurso(@RequestParam String nombre,
                               @RequestParam String descripcion,
                               @RequestParam float precio,
                               @RequestParam Integer categoriaId,
                               @RequestParam Integer docenteTitularId,
                               @RequestParam(required = false) Integer docenteSupervisorId,
                               @RequestParam(required = false) Integer nivelId,
                               @RequestParam(defaultValue = "false") boolean emiteCertificado,
                               RedirectAttributes ra) {
        try {
            Categoria categoria = categoriaService.buscarPorId(categoriaId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría inválida"));
            Docente titular = docenteRepository.findById(docenteTitularId)
                    .orElseThrow(() -> new IllegalArgumentException("Docente titular inválido"));
            Nivel nivel = (nivelId != null) ? nivelRepository.findById(nivelId).orElse(null) : null;

            Curso curso = new Curso(nombre, descripcion, precio, categoria, nivel, titular);
            curso.setEmiteCertificado(emiteCertificado);
            cursoService.registrarCursoConEquipo(curso, docenteSupervisorId);

            ra.addFlashAttribute("mensaje", "Curso '" + nombre + "' registrado con éxito.");
            return "redirect:/cursos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/nuevo";
        }
    }

    /**
     * CU-04 — Modificar curso (Formulario GET).
     * Vista: cu-04-modificar-curso.html
     */
    @GetMapping("/{id}/editar")
    public String modificarCursoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cOpt.get();
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("curso", curso);
        model.addAttribute("cursoEditar", curso);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("supervisores", supervisorRepository.findByCurso(curso));
        model.addAttribute("titulo", "CU-04 - Modificar curso | Idóneos Online");

        return "pages/cursos/cu-04-modificar-curso";
    }

    /**
     * CU-04 — Modificar curso (POST).
     */
    @PostMapping("/{id}/editar")
    public String actualizarCurso(@PathVariable Integer id,
                                  @RequestParam String nombre,
                                  @RequestParam String descripcion,
                                  @RequestParam float precio,
                                  @RequestParam Integer categoriaId,
                                  @RequestParam Integer docenteTitularId,
                                  @RequestParam(required = false) Integer docenteSupervisorId,
                                  @RequestParam(required = false) Integer nivelId,
                                  @RequestParam(defaultValue = "false") boolean emiteCertificado,
                                  RedirectAttributes ra) {
        try {
            cursoService.modificarCurso(id, nombre, descripcion, precio, categoriaId, docenteTitularId, docenteSupervisorId, nivelId, emiteCertificado);
            ra.addFlashAttribute("mensaje", "Curso modificado correctamente.");
            return "redirect:/cursos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/" + id + "/editar";
        }
    }

    /**
     * CU-05 — Dar de baja curso (Vista GET de confirmación / Modal).
     * Vista: cu-05-dar-de-baja-curso.html
     */
    @GetMapping("/{id}/baja")
    public String darDeBajaCursoView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/cursos";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("curso", cOpt.get());
        model.addAttribute("titulo", "CU-05 - Dar de baja curso | Idóneos Online");
        return "pages/cursos/cu-05-dar-de-baja-curso";
    }

    /**
     * CU-05 — Dar de baja curso (POST).
     */
    @PostMapping("/{id}/baja")
    public String darDeBajaCurso(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            cursoService.darDeBajaCurso(id);
            ra.addFlashAttribute("mensaje", "Curso dado de baja exitosamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-07, CU-08, CU-09, CU-10: CATEGORÍAS
    // ─────────────────────────────────────────────────────────────

    /**
     * CU-07 — Buscar categoría.
     * Vista: cu-07-buscar-categoria.html
     */
    @GetMapping("/categorias")
    public String buscarCategorias(@RequestParam(value = "nombre", required = false) String nombre,
                                   @RequestParam(value = "baja", required = false) Boolean baja,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Categoria> categorias = categoriaService.buscarCategoriasConFiltros(nombre, baja);
        
        // Mapeo dinámico de cursos asociados activos por categoría para CU-10 (Regla de dependencia)
        java.util.Map<Integer, List<Curso>> cursosPorCategoria = new java.util.HashMap<>();
        for (Categoria cat : categorias) {
            cursosPorCategoria.put(cat.getId(), cursoRepository.findByCategoriaAndBajaFalse(cat));
        }

        model.addAttribute("categorias", categorias);
        model.addAttribute("cursosPorCategoria", cursosPorCategoria);
        model.addAttribute("nombreBusqueda", nombre);
        model.addAttribute("titulo", "CU-07 - Buscar categoría | Idóneos Online");
        return "pages/cursos/cu-07-buscar-categoria";
    }

    /**
     * CU-08 — Registrar categoría (GET).
     * Vista: cu-08-registrar-categoria.html
     */
    @GetMapping("/categorias/nueva")
    public String registrarCategoriaForm(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("titulo", "CU-08 - Registrar categoría | Idóneos Online");
        return "pages/cursos/cu-08-registrar-categoria";
    }

    /**
     * CU-08 — Registrar categoría (POST).
     */
    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@RequestParam String nombre,
                                   @RequestParam(required = false) String descripcion,
                                   RedirectAttributes ra) {
        try {
            categoriaService.guardar(new Categoria(nombre, descripcion));
            ra.addFlashAttribute("mensaje", "Categoría '" + nombre + "' registrada con éxito.");
            return "redirect:/cursos/categorias";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/categorias/nueva";
        }
    }

    /**
     * CU-09 — Modificar categoría (GET).
     * Vista: cu-09-modificar-categoria.html
     */
    @GetMapping("/categorias/{id}/editar")
    public String modificarCategoriaForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Categoria> catOpt = categoriaService.buscarPorId(id);
        if (catOpt.isEmpty()) return "redirect:/cursos/categorias";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("categoria", catOpt.get());
        model.addAttribute("titulo", "CU-09 - Modificar categoría | Idóneos Online");
        return "pages/cursos/cu-09-modificar-categoria";
    }

    /**
     * CU-09 — Modificar categoría (POST).
     */
    @PostMapping("/categorias/{id}/editar")
    public String actualizarCategoria(@PathVariable Integer id,
                                      @RequestParam String nombre,
                                      @RequestParam(required = false) String descripcion,
                                      RedirectAttributes ra) {
        try {
            Categoria c = categoriaService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
            c.setNombre(nombre);
            c.setDescripcion(descripcion);
            categoriaService.modificar(c);
            ra.addFlashAttribute("mensaje", "Categoría actualizada con éxito.");
            return "redirect:/cursos/categorias";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/categorias/" + id + "/editar";
        }
    }

    /**
     * CU-10 — Dar de baja categoría (GET/POST).
     * Vista: cu-10-dar-de-baja-categoria.html
     */
    @GetMapping("/categorias/{id}/baja")
    public String darDeBajaCategoriaView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Categoria> catOpt = categoriaService.buscarPorId(id);
        if (catOpt.isEmpty()) return "redirect:/cursos/categorias";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("categoria", catOpt.get());
        model.addAttribute("titulo", "CU-10 - Dar de baja categoría | Idóneos Online");
        return "pages/cursos/cu-10-dar-de-baja-categoria";
    }

    @PostMapping("/categorias/{id}/baja")
    public String eliminarCategoria(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            categoriaService.darDeBaja(id);
            ra.addFlashAttribute("mensaje", "Categoría eliminada con éxito.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos/categorias";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-11, CU-12, CU-13, CU-14: COHORTES
    // ─────────────────────────────────────────────────────────────

    /**
     * CU-11 — Buscar cohorte.
     * Vista: cu-11-buscar-cohorte.html
     */
    @GetMapping("/cohortes")
    public String buscarCohortes(@RequestParam(value = "programaId", required = false) Integer programaId,
                                 @RequestParam(value = "estado", required = false) String estado,
                                 @RequestParam(value = "busqueda", required = false) String busqueda,
                                 Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Cohorte> cohortes = cohorteService.buscarCohortesConFiltros(programaId, estado, null, null);
        if (busqueda != null && !busqueda.isBlank()) {
            String q = busqueda.toLowerCase().trim();
            cohortes = cohortes.stream().filter(c -> {
                String cNombre = (c.getPrograma() != null && c.getPrograma().getCurso() != null) ? c.getPrograma().getCurso().getNombre() : "";
                String pNombre = (c.getPrograma() != null) ? c.getPrograma().getNombre() : "";
                return cNombre.toLowerCase().contains(q) || pNombre.toLowerCase().contains(q);
            }).toList();
        }
        
        // Mapeo dinámico de inscripciones activas por cohorte para CU-14
        java.util.Map<Integer, List<Inscripcion>> inscripcionesPorCohorte = new java.util.HashMap<>();
        for (Cohorte coh : cohortes) {
            inscripcionesPorCohorte.put(coh.getId(), inscripcionRepository.findByCohorteAndBajaFalse(coh));
        }

        model.addAttribute("cohortes", cohortes);
        model.addAttribute("inscripcionesPorCohorte", inscripcionesPorCohorte);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("titulo", "CU-11 - Buscar cohorte | Idóneos Online");
        return "pages/cursos/cu-11-buscar-cohorte";
    }

    /**
     * CU-12 — Registrar cohorte (GET).
     * Vista: cu-12-registrar-cohorte.html
     */
    @GetMapping("/cohortes/nueva")
    public String registrarCohorteForm(@RequestParam(value = "programaId", required = false) Integer programaId,
                                       Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("programaId", programaId);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("titulo", "CU-12 - Registrar cohorte | Idóneos Online");
        return "pages/cursos/cu-12-registrar-cohorte";
    }

    /**
     * CU-12 — Registrar cohorte (POST).
     */
    @PostMapping("/cohortes/guardar")
    public String guardarCohorte(@RequestParam(required = false) Integer programaId,
                                 @RequestParam String fechaInicioInscripcion,
                                 @RequestParam String fechaFinInscripcion,
                                 @RequestParam(required = false) String fechaInicioDictado,
                                 @RequestParam(required = false) String fechaFinDictado,
                                 @RequestParam(defaultValue = "12") int semanasAcceso,
                                 @RequestParam(required = false) Integer cupoMaximo,
                                 RedirectAttributes ra) {
        try {
            if (programaId == null) {
                programaId = 1; // Fallback al programa por defecto
            }
            cohorteService.registrarCohorte(programaId, fechaInicioInscripcion, fechaFinInscripcion,
                    fechaInicioDictado, fechaFinDictado, semanasAcceso, cupoMaximo);
            ra.addFlashAttribute("mensaje", "Cohorte registrada correctamente.");
            return "redirect:/cursos/cohortes";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/cohortes";
        }
    }

    /**
     * CU-13 — Modificar cohorte (GET).
     * Vista: cu-13-modificar-cohorte.html
     */
    @GetMapping("/cohortes/{id}/editar")
    public String modificarCohorteForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Cohorte> cOpt = cohorteService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/cursos/cohortes";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cohorte", cOpt.get());
        model.addAttribute("titulo", "CU-13 - Modificar cohorte | Idóneos Online");
        return "pages/cursos/cu-13-modificar-cohorte";
    }

    /**
     * CU-13 — Modificar cohorte (POST).
     */
    @PostMapping("/cohortes/{id}/editar")
    public String actualizarCohorte(@PathVariable Integer id,
                                    @RequestParam String fechaInicioInscripcion,
                                    @RequestParam String fechaFinInscripcion,
                                    @RequestParam(required = false) String fechaInicioDictado,
                                    @RequestParam(required = false) String fechaFinDictado,
                                    @RequestParam int semanasAcceso,
                                    @RequestParam(required = false) Integer cupoMaximo,
                                    RedirectAttributes ra) {
        try {
            Cohorte c = cohorteService.modificarCohorte(id, fechaInicioInscripcion, fechaFinInscripcion,
                    fechaInicioDictado, fechaFinDictado, semanasAcceso, cupoMaximo);
            ra.addFlashAttribute("mensaje", "Cohorte modificada correctamente.");
            return "redirect:/cursos/cohortes";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/cohortes";
        }
    }

    /**
     * CU-14 — Dar de baja cohorte (GET/POST).
     * Vista: cu-14-dar-de-baja-cohorte.html
     */
    @GetMapping("/cohortes/{id}/baja")
    public String darDeBajaCohorteView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Cohorte> cOpt = cohorteService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/cursos/cohortes";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cohorte", cOpt.get());
        model.addAttribute("titulo", "CU-14 - Dar de baja cohorte | Idóneos Online");
        return "pages/cursos/cu-14-dar-de-baja-cohorte";
    }

    @PostMapping("/cohortes/{id}/baja")
    public String eliminarCohorte(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            cohorteService.darDeBajaCohorte(id);
            ra.addFlashAttribute("mensaje", "Cohorte cancelada / dada de baja correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos/cohortes";
    }
}
