package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.Categoria;
import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.service.Categoria.CategoriaServiceImpl;
import com.app.ecomisiones.service.Curso.CursoServiceImpl;
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

    @GetMapping
    public String panelDocente(Model model, Authentication auth) {
        Usuario docente = (Usuario) auth.getPrincipal();
        List<Curso> misCursos = cursoService.obtenerPorDocente(docente);

        model.addAttribute("usuario", docente);
        model.addAttribute("cursos", misCursos);
        model.addAttribute("titulo", "Panel del Docente | Idóneos Online");

        return "pages/docente/mis-cursos";
    }

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

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get(), docente);
        cursoService.guardar(curso);

        redirectAttributes.addFlashAttribute("mensaje", "¡Curso creado correctamente!");
        return "redirect:/docente";
    }
}
