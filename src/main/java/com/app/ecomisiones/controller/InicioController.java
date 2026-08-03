package com.app.ecomisiones.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.ecomisiones.model.Categoria;
import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.service.Categoria.CategoriaServiceImpl;
import com.app.ecomisiones.service.Curso.CursoServiceImpl;

/**
 * Controlador principal para la página de inicio y estáticas de Idóneos Online.
 */
@Controller
public class InicioController {

    @Autowired
    private CategoriaServiceImpl categoriaService;

    @Autowired
    private CursoServiceImpl cursoService;

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
