package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.Categoria;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.service.Categoria.CategoriaServiceImpl;
import com.app.ecomisiones.service.Curso.CursoServiceImpl;
import com.app.ecomisiones.service.Usuario.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private CategoriaServiceImpl categoriaService;

    @GetMapping
    public String verPanelAdmin(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("totalUsuarios", usuarioService.obtenerTodo().size());
        model.addAttribute("totalCursos", cursoService.obtenerTodo().size());
        model.addAttribute("totalCategorias", categoriaService.obtenerTodo().size());
        model.addAttribute("titulo", "Panel de Administración | Idóneos Online");
        return "pages/admin/panel";
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("usuarios", usuarioService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Usuarios | Idóneos Online");
        return "pages/admin/usuarios";
    }

    @GetMapping("/categorias")
    public String listarCategorias(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Categorías | Idóneos Online");
        return "pages/admin/categorias";
    }

    @PostMapping("/categorias/crear")
    public String crearCategoria(@RequestParam("nombre") String nombre,
                                 @RequestParam("descripcion") String descripcion,
                                 RedirectAttributes redirectAttributes) {
        Categoria cat = new Categoria(nombre, descripcion);
        categoriaService.guardar(cat);
        redirectAttributes.addFlashAttribute("mensaje", "Categoría creada con éxito.");
        return "redirect:/admin/categorias";
    }
}
