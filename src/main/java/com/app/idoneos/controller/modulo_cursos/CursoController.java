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

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
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
                               Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);

        List<Curso> cursos;
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            Usuario u = (Usuario) auth.getPrincipal();
            if (u.esDocente() && !u.esAdmin() && u.getDocente() != null) {
                // Restricción por rol docente titular / ayudante
                cursos = cursoService.buscarCursosPorDocente(u.getDocente().getId());
            } else {
                cursos = cursoService.buscarCursosAdminConFiltros(busqueda, categoriaId, nivelId, docenteId, null);
            }
        } else {
            cursos = cursoService.buscarCursosPublicadosConFiltros(busqueda, categoriaId, modalidadId);
        }

        model.addAttribute("cursos", cursos);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("modalidades", modalidadRepository.findAll());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categoriaSeleccionada", categoriaId);
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
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);

        List<Curso> cursos = cursoService.buscarCursosPublicadosConFiltros(busqueda, categoriaId, modalidadId);
        model.addAttribute("cursos", cursos);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("modalidades", modalidadRepository.findAll());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("titulo", "CU-06 - Catálogo de Cursos | Idóneos Online");

        return "pages/cursos/cu-06-explorar-catalogo-de-cursos";
    }

    /**
     * CU-02 — Ver mis cursos (Alumno).
     * Vista: cu-02-ver-mis-cursos.html
     */
    @GetMapping("/mis-cursos")
    public String verMisCursos(Authentication auth, Model model) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";
        Usuario usuario = (Usuario) auth.getPrincipal();

        List<Inscripcion> inscripciones = inscripcionService.obtenerPorAlumno(usuario);
        List<Curso> misCursos = new ArrayList<>();
        for (Inscripcion i : inscripciones) {
            if (!i.getBaja() && i.getCohorte() != null && i.getCohorte().getPrograma() != null) {
                misCursos.add(i.getCohorte().getPrograma().getCurso());
            }
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("misCursos", misCursos);
        model.addAttribute("titulo", "CU-02 - Mis Cursos | Idóneos Online");

        return "pages/cursos/cu-02-ver-mis-cursos";
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
        model.addAttribute("categorias", categoriaService.buscarCategoriasConFiltros(nombre, baja));
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
                                 Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        if (programaId != null) {
            model.addAttribute("cohortes", cohorteService.buscarCohortesConFiltros(programaId, estado, null, null));
        }
        model.addAttribute("cursos", cursoService.obtenerTodo());
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
    public String guardarCohorte(@RequestParam Integer programaId,
                                 @RequestParam String fechaInicioInscripcion,
                                 @RequestParam String fechaFinInscripcion,
                                 @RequestParam(required = false) String fechaInicioDictado,
                                 @RequestParam(required = false) String fechaFinDictado,
                                 @RequestParam(defaultValue = "12") int semanasAcceso,
                                 @RequestParam(required = false) Integer cupoMaximo,
                                 RedirectAttributes ra) {
        try {
            cohorteService.registrarCohorte(programaId, fechaInicioInscripcion, fechaFinInscripcion,
                    fechaInicioDictado, fechaFinDictado, semanasAcceso, cupoMaximo);
            ra.addFlashAttribute("mensaje", "Cohorte registrada correctamente.");
            return "redirect:/cursos/cohortes?programaId=" + programaId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/cohortes/nueva?programaId=" + programaId;
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
            return "redirect:/cursos/cohortes?programaId=" + c.getPrograma().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/cohortes/" + id + "/editar";
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
            Cohorte c = cohorteService.buscarPorId(id).orElse(null);
            Integer pId = (c != null && c.getPrograma() != null) ? c.getPrograma().getId() : null;
            cohorteService.darDeBajaCohorte(id);
            ra.addFlashAttribute("mensaje", "Cohorte cancelada / dada de baja correctamente.");
            return pId != null ? "redirect:/cursos/cohortes?programaId=" + pId : "redirect:/cursos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/cohortes/" + id + "/baja";
        }
    }
}
