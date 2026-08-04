package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.service.Categoria.CategoriaServiceImpl;
import com.app.ecomisiones.service.Curso.CursoServiceImpl;
import com.app.ecomisiones.service.Usuario.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private CategoriaServiceImpl categoriaService;

    // ─── Panel Principal ───────────────────────────────────────────────────────

    @GetMapping
    public String verPanelAdmin(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("totalUsuarios", usuarioService.obtenerTodo().size());
        model.addAttribute("totalCursos", cursoService.obtenerTodo().size());
        model.addAttribute("totalCategorias", categoriaService.obtenerTodo().size());
        model.addAttribute("titulo", "Panel de Administración | Idóneos Online");
        return "pages/admin/panel";
    }

    // ─── Gestión de Usuarios ───────────────────────────────────────────────────

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("usuarios", usuarioService.obtenerTodo());
        model.addAttribute("roles", RolUsuario.values());
        model.addAttribute("titulo", "Gestión de Usuarios | Idóneos Online");
        return "pages/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle-baja")
    public String toggleBaja(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        usuarioService.buscarPorId(id).ifPresent(u -> {
            u.setBaja(!u.getBaja());
            usuarioService.modificar(u);
        });
        redirectAttributes.addFlashAttribute("mensaje", "Estado del usuario actualizado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/cambiar-rol")
    public String cambiarRol(@PathVariable Integer id,
                              @RequestParam("rol") RolUsuario rol,
                              RedirectAttributes redirectAttributes) {
        usuarioService.buscarPorId(id).ifPresent(u -> {
            u.setRol(rol);
            usuarioService.modificar(u);
        });
        redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle-clon-ia")
    public String toggleClonIA(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> uOpt = usuarioService.buscarPorId(id);
        uOpt.ifPresent(u -> {
            if (u.getDocente() != null) {
                Docente d = u.getDocente();
                if (d.getFechaConsentimientoClon() != null) {
                    d.setFechaConsentimientoClon(null); // deshabilitar
                } else {
                    d.setFechaConsentimientoClon(java.time.LocalDateTime.now()); // habilitar
                }
                // Save via usuario cascade
                usuarioService.modificar(u);
            }
        });
        redirectAttributes.addFlashAttribute("mensaje", "Estado de Clon IA actualizado.");
        return "redirect:/admin/usuarios";
    }

    // ─── Gestión de Cursos ─────────────────────────────────────────────────────

    @GetMapping("/cursos")
    public String listarCursos(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Cursos | Idóneos Online");
        return "pages/admin/cursos";
    }

    @PostMapping("/cursos/{id}/toggle-publicado")
    public String togglePublicado(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        cursoService.buscarPorId(id).ifPresent(c -> {
            c.setPublicado(!c.getPublicado());
            cursoService.modificar(c);
        });
        redirectAttributes.addFlashAttribute("mensaje", "Estado de publicación actualizado.");
        return "redirect:/admin/cursos";
    }

    @PostMapping("/cursos/{id}/eliminar")
    public String eliminarCurso(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        cursoService.buscarPorId(id).ifPresent(cursoService::borrar);
        redirectAttributes.addFlashAttribute("mensaje", "Curso eliminado del sistema.");
        return "redirect:/admin/cursos";
    }

    // ─── Gestión de Categorías ─────────────────────────────────────────────────

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

    @PostMapping("/categorias/{id}/eliminar")
    public String eliminarCategoria(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        categoriaService.buscarPorId(id).ifPresent(c -> {
            c.setBaja(true);
            categoriaService.modificar(c);
        });
        redirectAttributes.addFlashAttribute("mensaje", "Categoría eliminada.");
        return "redirect:/admin/categorias";
    }
}
