package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.repository.TipoMaterialRepository;
import com.app.ecomisiones.service.Categoria.CategoriaServiceImpl;
import com.app.ecomisiones.service.Curso.CursoServiceImpl;
import com.app.ecomisiones.service.Material.MaterialServiceImpl;
import com.app.ecomisiones.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/docente")
public class DocenteController {

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private CategoriaServiceImpl categoriaService;

    @Autowired
    private UnidadServiceImpl unidadService;

    @Autowired
    private MaterialServiceImpl materialService;

    @Autowired
    private TipoMaterialRepository tipoMaterialRepository;

    // ─── Panel Principal Docente ───────────────────────────────────────────────

    @GetMapping
    public String panelDocente(Model model, Authentication auth) {
        Usuario docente = (Usuario) auth.getPrincipal();
        List<Curso> misCursos = cursoService.obtenerPorDocente(docente);

        model.addAttribute("usuario", docente);
        model.addAttribute("cursos", misCursos);
        model.addAttribute("titulo", "Panel del Docente | Idóneos Online");

        return "pages/docente/mis-cursos";
    }

    // ─── Crear Curso ───────────────────────────────────────────────────────────

    @GetMapping("/curso/nuevo")
    public String nuevoCursoForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "Nuevo Curso | Idóneos Online");
        return "pages/docente/nuevo-curso";
    }

    @PostMapping("/curso/guardar")
    public String guardarCurso(@RequestParam("nombre") String nombre,
                               @RequestParam("descripcion") String descripcion,
                               @RequestParam("precio") float precio,
                               @RequestParam("categoriaId") Integer categoriaId,
                               Authentication auth, RedirectAttributes redirectAttributes) {

        Usuario docente = (Usuario) auth.getPrincipal();
        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);

        if (catOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Categoría no válida.");
            return "redirect:/docente/curso/nuevo";
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get());
        cursoService.guardar(curso);

        redirectAttributes.addFlashAttribute("mensaje", "¡Curso creado correctamente!");
        return "redirect:/docente";
    }

    // ─── Gestión de Unidades y Materiales de un Curso ─────────────────────────

    @GetMapping("/curso/{cursoId}/gestionar")
    public String gestionarCurso(@PathVariable Integer cursoId, Model model, Authentication auth,
                                  RedirectAttributes redirectAttributes) {

        Optional<Curso> cursoOpt = cursoService.buscarPorId(cursoId);
        if (cursoOpt.isEmpty()) {
            return "redirect:/docente";
        }

        Curso curso = cursoOpt.get();
        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("curso", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("titulo", "Gestionar: " + curso.getNombre());

        return "pages/docente/gestionar-curso";
    }

    // ─── Nueva Unidad ──────────────────────────────────────────────────────────

    @GetMapping("/curso/{cursoId}/unidad/nueva")
    public String nuevaUnidadForm(@PathVariable Integer cursoId, Model model, Authentication auth) {
        Optional<Curso> cursoOpt = cursoService.buscarPorId(cursoId);
        if (cursoOpt.isEmpty()) return "redirect:/docente";

        int siguiente = unidadService.contarUnidadesPorCurso(cursoOpt.get()) + 1;

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("curso", cursoOpt.get());
        model.addAttribute("numeroOrdenSugerido", siguiente);
        model.addAttribute("titulo", "Nueva Unidad | Idóneos Online");

        return "pages/docente/nueva-unidad";
    }

    @PostMapping("/curso/{cursoId}/unidad/guardar")
    public String guardarUnidad(@PathVariable Integer cursoId,
                                @RequestParam("titulo") String titulo,
                                @RequestParam("descripcion") String descripcion,
                                @RequestParam("numeroOrden") int numeroOrden,
                                RedirectAttributes redirectAttributes) {

        Optional<Curso> cursoOpt = cursoService.buscarPorId(cursoId);
        if (cursoOpt.isEmpty()) return "redirect:/docente";

        Unidad unidad = new Unidad(titulo, descripcion, numeroOrden, cursoOpt.get());
        unidadService.guardar(unidad);

        redirectAttributes.addFlashAttribute("mensaje", "¡Unidad creada correctamente!");
        return "redirect:/docente/curso/" + cursoId + "/gestionar";
    }

    @PostMapping("/unidad/{unidadId}/eliminar")
    public String eliminarUnidad(@PathVariable Integer unidadId, @RequestParam("cursoId") Integer cursoId,
                                  RedirectAttributes redirectAttributes) {
        unidadService.buscarPorId(unidadId).ifPresent(unidadService::borrar);
        redirectAttributes.addFlashAttribute("mensaje", "Unidad eliminada.");
        return "redirect:/docente/curso/" + cursoId + "/gestionar";
    }

    // ─── Nuevo Material ────────────────────────────────────────────────────────

    @GetMapping("/unidad/{unidadId}/material/nuevo")
    public String nuevoMaterialForm(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Optional<Unidad> unidadOpt = unidadService.buscarPorId(unidadId);
        if (unidadOpt.isEmpty()) return "redirect:/docente";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", unidadOpt.get());
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "Nuevo Material | Idóneos Online");

        return "pages/docente/nuevo-material";
    }

    @PostMapping("/unidad/{unidadId}/material/guardar")
    public String guardarMaterial(@PathVariable Integer unidadId,
                                   @RequestParam("tipoId") Integer tipoId,
                                   @RequestParam("titulo") String titulo,
                                   @RequestParam("rutaArchivo") String rutaArchivo,
                                   RedirectAttributes redirectAttributes) {

        Optional<Unidad> unidadOpt = unidadService.buscarPorId(unidadId);
        if (unidadOpt.isEmpty()) return "redirect:/docente";

        Unidad unidad = unidadOpt.get();
        TipoMaterial tipo = tipoMaterialRepository.findById(tipoId).orElse(null);
        if (tipo == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Tipo de material no válido.");
            return "redirect:/docente/unidad/" + unidadId + "/material/nuevo";
        }
        Material material = new Material(tipo, titulo, rutaArchivo, unidad);
        materialService.guardar(material);

        redirectAttributes.addFlashAttribute("mensaje", "¡Material cargado correctamente!");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }

    @PostMapping("/material/{materialId}/eliminar")
    public String eliminarMaterial(@PathVariable Integer materialId, @RequestParam("cursoId") Integer cursoId,
                                    RedirectAttributes redirectAttributes) {
        materialService.buscarPorId(materialId).ifPresent(materialService::borrar);
        redirectAttributes.addFlashAttribute("mensaje", "Material eliminado.");
        return "redirect:/docente/curso/" + cursoId + "/gestionar";
    }
}
