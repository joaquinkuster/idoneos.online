package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.Categoria;
import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Inscripcion;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.service.Categoria.CategoriaServiceImpl;
import com.app.ecomisiones.service.Curso.CursoServiceImpl;
import com.app.ecomisiones.service.Inscripcion.InscripcionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private CategoriaServiceImpl categoriaService;

    @Autowired
    private InscripcionServiceImpl inscripcionService;

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
            cursos = cat.map(categoria -> cursoService.obtenerPorCategoria(categoria)).orElseGet(() -> cursoService.obtenerPublicados());
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

    @GetMapping("/{id}")
    public String verDetalleCurso(@PathVariable("id") Integer id, Model model, Authentication auth) {
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);
        if (cursoOpt.isEmpty()) {
            return "redirect:/cursos";
        }

        Curso curso = cursoOpt.get();
        boolean yaInscripto = false;

        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            Usuario usuario = (Usuario) auth.getPrincipal();
            model.addAttribute("usuario", usuario);
            yaInscripto = inscripcionService.estaInscripto(usuario, curso);
        }

        model.addAttribute("curso", curso);
        model.addAttribute("yaInscripto", yaInscripto);
        model.addAttribute("titulo", curso.getNombre() + " | Idóneos Online");

        return "pages/cursos/detalle";
    }

    @PostMapping("/{id}/inscribir")
    public String inscribirseACurso(@PathVariable("id") Integer id, Authentication auth, RedirectAttributes redirectAttributes) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) {
            return "redirect:/login";
        }

        Usuario usuario = (Usuario) auth.getPrincipal();
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);

        if (cursoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Curso no encontrado.");
            return "redirect:/cursos";
        }

        Curso curso = cursoOpt.get();
        inscripcionService.inscribirAlumno(usuario, curso);

        redirectAttributes.addFlashAttribute("mensaje", "¡Inscripción exitosa! Ya puedes acceder a las unidades de este curso.");
        return "redirect:/cursos/" + id + "/mi-cursada";
    }

    @GetMapping("/{id}/mi-cursada")
    public String verMiCursada(@PathVariable("id") Integer id, Authentication auth, Model model, RedirectAttributes redirectAttributes) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) {
            return "redirect:/login";
        }

        Usuario usuario = (Usuario) auth.getPrincipal();
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);

        if (cursoOpt.isEmpty()) {
            return "redirect:/cursos";
        }

        Curso curso = cursoOpt.get();
        Optional<Inscripcion> inscripcionOpt = inscripcionService.obtenerPorAlumnoYCurso(usuario, curso);

        if (inscripcionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes estar inscripto para ver la cursada de este curso.");
            return "redirect:/cursos/" + id;
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("inscripcion", inscripcionOpt.get());
        model.addAttribute("titulo", "Cursada: " + curso.getNombre());

        return "pages/cursos/mi-cursada";
    }
}
