package com.app.idoneos.controller.modulo_usuarios;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.modulo_cursos.CategoriaServiceImpl;
import com.app.idoneos.service.modulo_cursos.CursoServiceImpl;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller principal para la página institucional de bienvenida y catálogo público (CU-01).
 */
@Controller
public class InicioController {

    @Autowired private CategoriaServiceImpl categoriaService;
    @Autowired private CursoServiceImpl cursoService;

    /**
     * CU-01 — Página principal con catálogo público de cursos destacados y categorías de finanzas.
     */
    @GetMapping({"/", "/inicio"})
    public String verInicio(@RequestParam(value = "login", required = false) String login, Model model, Authentication auth) {
        if (login != null) {
            model.addAttribute("mensaje", "¡Bienvenido a Idóneos Online! Has iniciado sesión correctamente.");
        }

        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }

        List<Categoria> categorias = categoriaService.obtenerTodo();
        List<Curso> cursosDestacados = cursoService.obtenerPublicados();

        model.addAttribute("categorias", categorias);
        model.addAttribute("cursos", cursosDestacados);
        model.addAttribute("titulo", "Idóneos Online | Cursos de Finanzas, Economía y Mercado de Capitales");

        return "index";
    }

    @GetMapping("/acercaDe")
    public String verAcercaDe(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
        model.addAttribute("titulo", "Acerca de | Idóneos Online");
        return "pages/acercaDe";
    }

    @GetMapping("/error")
    public String verError(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
        model.addAttribute("titulo", "Página no encontrada | Idóneos Online");
        return "pages/error";
    }
}

