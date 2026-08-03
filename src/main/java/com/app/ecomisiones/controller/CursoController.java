package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.service.Categoria.CategoriaServiceImpl;
import com.app.ecomisiones.service.Curso.CursoServiceImpl;
import com.app.ecomisiones.service.Inscripcion.InscripcionServiceImpl;
import com.app.ecomisiones.service.Progreso.ProgresoServiceImpl;
import com.app.ecomisiones.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private CategoriaServiceImpl categoriaService;

    @Autowired
    private InscripcionServiceImpl inscripcionService;

    @Autowired
    private ProgresoServiceImpl progresoService;

    @Autowired
    private UnidadServiceImpl unidadService;

    // ─── Catálogo Público ──────────────────────────────────────────────────────

    @GetMapping
    public String listarCursos(@RequestParam(value = "categoriaId", required = false) Integer categoriaId,
                               @RequestParam(value = "busqueda", required = false) String busqueda,
                               Model model, Authentication auth) {

        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }

        List<Curso> cursos;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            cursos = cursoService.buscarPorNombre(busqueda.trim());
        } else if (categoriaId != null) {
            Optional<Categoria> cat = categoriaService.buscarPorId(categoriaId);
            cursos = cat.map(c -> cursoService.obtenerPorCategoria(c)).orElseGet(() -> cursoService.obtenerPublicados());
        } else {
            cursos = cursoService.obtenerPublicados();
        }

        model.addAttribute("cursos", cursos);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("titulo", "Catálogo de Cursos | Idóneos Online");

        return "pages/cursos/catalogo";
    }

    // ─── Detalle del Curso ─────────────────────────────────────────────────────

    @GetMapping("/{id:\\d+}")
    public String verDetalleCurso(@PathVariable("id") Integer id, Model model, Authentication auth) {
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);
        if (cursoOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cursoOpt.get();
        boolean yaInscripto = false;

        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            Usuario usuario = (Usuario) auth.getPrincipal();
            model.addAttribute("usuario", usuario);
            yaInscripto = inscripcionService.estaInscripto(usuario, curso);
        }

        model.addAttribute("curso", curso);
        model.addAttribute("unidades", unidadService.obtenerPorCurso(curso));
        model.addAttribute("yaInscripto", yaInscripto);
        model.addAttribute("titulo", curso.getNombre() + " | Idóneos Online");

        return "pages/cursos/detalle";
    }

    // ─── Mis Cursos Inscriptos (Alumno) ───────────────────────────────────────

    @GetMapping("/mis-cursos")
    public String listarMisCursos(Authentication auth, Model model) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";

        Usuario usuario = (Usuario) auth.getPrincipal();
        List<Inscripcion> inscripciones = inscripcionService.obtenerPorAlumno(usuario);

        List<Curso> misCursos = new ArrayList<>();
        for (Inscripcion i : inscripciones) {
            if (!i.getBaja()) {
                misCursos.add(i.getCurso());
            }
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("misCursos", misCursos);
        model.addAttribute("titulo", "Mis Cursos | Idóneos Online");

        return "pages/cursos/mis-cursos";
    }

    // ─── Inscripción ───────────────────────────────────────────────────────────

    @PostMapping("/{id}/inscribir")
    public String inscribirseACurso(@PathVariable("id") Integer id, Authentication auth,
                                     RedirectAttributes redirectAttributes) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";

        Usuario usuario = (Usuario) auth.getPrincipal();
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);

        if (cursoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Curso no encontrado.");
            return "redirect:/cursos";
        }

        inscripcionService.inscribirAlumno(usuario, cursoOpt.get());
        redirectAttributes.addFlashAttribute("mensaje", "¡Inscripción exitosa! Ya podés acceder al contenido del curso.");
        return "redirect:/cursos/" + id + "/mi-cursada";
    }

    // ─── Vista de Cursada del Alumno ───────────────────────────────────────────

    @GetMapping("/{id}/mi-cursada")
    public String verMiCursada(@PathVariable("id") Integer id, Authentication auth,
                                Model model, RedirectAttributes redirectAttributes) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";

        Usuario usuario = (Usuario) auth.getPrincipal();
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);
        if (cursoOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cursoOpt.get();
        Optional<Inscripcion> inscripcionOpt = inscripcionService.obtenerPorAlumnoYCurso(usuario, curso);

        if (inscripcionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes inscribirte para acceder a la cursada.");
            return "redirect:/cursos/" + id;
        }

        Inscripcion inscripcion = inscripcionOpt.get();
        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);

        // Mapa de progreso: unidad.id -> completada?
        Map<Integer, Boolean> progresoPorUnidad = new LinkedHashMap<>();
        for (Unidad u : unidades) {
            progresoPorUnidad.put(u.getId(), progresoService.unidadCompletada(inscripcion, u));
        }

        int completadas = progresoService.contarCompletadas(inscripcion);
        int totalUnidades = unidades.size();
        int porcentaje = totalUnidades > 0 ? (completadas * 100 / totalUnidades) : 0;

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("unidades", unidades);
        model.addAttribute("progresoPorUnidad", progresoPorUnidad);
        model.addAttribute("completadas", completadas);
        model.addAttribute("totalUnidades", totalUnidades);
        model.addAttribute("porcentaje", porcentaje);
        model.addAttribute("titulo", "Cursada: " + curso.getNombre());

        return "pages/cursos/mi-cursada";
    }
}
