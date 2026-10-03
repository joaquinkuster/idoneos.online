package com.app.idoneos.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.utilidades.Utilidades;

/**
 * Controlador de las páginas institucionales: inicio, acerca de y novedades.
 */
@Controller
public class InicioControlador {

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    /**
     * Muestra la página principal con los cursos destacados del catálogo (los que tienen cohortes
     * con inscripción abierta) y las categorías.
     *
     * @param login  Indica que el usuario acaba de iniciar sesión.
     * @param logout Indica que el usuario acaba de cerrar sesión.
     * @param modelo El modelo de la vista.
     * @return La vista de inicio.
     */
    @GetMapping({ "/", "/inicio" })
    public String verInicio(@RequestParam(value = "login", required = false) String login,
            @RequestParam(value = "logout", required = false) String logout, Model modelo) {
        if (login != null) {
            modelo.addAttribute("mensaje", "¡Bienvenido a Idóneos Online! Has iniciado sesión correctamente.");
        }
        if (logout != null) {
            modelo.addAttribute("mensaje", "Has cerrado sesión correctamente. ¡Hasta pronto!");
        }

        List<Categoria> categorias = categoriaServicio.obtenerTodo();
        List<Curso> cursosDestacados = Utilidades.obtenerPagina(
                cursoServicio.buscarEnCatalogo(null, null, null, null, null), 0, 6);

        modelo.addAttribute("categorias", categorias);
        modelo.addAttribute("cursos", cursosDestacados);
        modelo.addAttribute("titulo", "Idóneos Online | Cursos de Finanzas, Economía y Mercado de Capitales");
        return "index";
    }

    /**
     * Muestra la página institucional "Acerca de".
     *
     * @param modelo El modelo de la vista.
     * @return La vista "Acerca de".
     */
    @GetMapping("/acercaDe")
    public String verAcercaDe(Model modelo) {
        modelo.addAttribute("titulo", "Acerca de | Idóneos Online");
        return "pages/acercaDe";
    }

    /**
     * Muestra la página de novedades y actualizaciones.
     *
     * @param modelo El modelo de la vista.
     * @return La vista de novedades.
     */
    @GetMapping("/novedades")
    public String verNovedades(Model modelo) {
        modelo.addAttribute("titulo", "Novedades y actualizaciones | Idóneos Online");
        return "pages/novedades";
    }
}
