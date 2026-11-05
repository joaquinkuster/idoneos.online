package com.app.idoneos.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.service.pago.PagoService;
import com.app.idoneos.service.descuento.DescuentoService;
import com.app.idoneos.service.categoria.CategoriaService;
import com.app.idoneos.service.curso.CursoService;
import com.app.idoneos.service.programa.ProgramaService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador de las páginas institucionales: inicio, acerca de y página de error. Reemplaza al controlador
 * de errores por defecto de Spring Boot para mostrar un mensaje claro cuando una ruta no existe, no se tiene
 * acceso a ella o ocurre un fallo inesperado.
 */
@Controller
public class InicioController implements ErrorController {

    /** Cantidad de cursos que se destacan en el inicio. */
    private static final int CURSOS_DESTACADOS = 8;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private DescuentoService descuentoService;

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CursoService cursoService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Muestra la página principal con los cursos destacados del catálogo (los que tienen cohortes
     * con inscripción abierta) y las categorías.
     *
     * @param model El modelo de la vista.
     * @return La vista de inicio.
     */
    @GetMapping({ "/", "/inicio" })
    public String ver(Model model, Authentication authentication) {
        List<Curso> catalogo = cursoService.buscarEnCatalogo(null, null, null, null, null);
        // Los destacados van del curso con más inscripciones históricas al que tiene menos
        List<Curso> cursosDestacados = cursoService.buscarDestacados(CURSOS_DESTACADOS);

        // Cantidad de cursos del catálogo por categoría: solo se muestran las categorías con cursos disponibles
        Map<Integer, Long> cursosPorCategoria = new HashMap<>();
        for (Curso curso : catalogo) {
            cursosPorCategoria.merge(curso.getCategoria().getIdCategoria(), 1L, Long::sum);
        }
        List<Categoria> categorias = categoriaService.buscarConFiltros(null, false, "nombre").stream()
                .filter(categoria -> cursosPorCategoria.containsKey(categoria.getIdCategoria())).toList();

        // Cantidad de temas de cada curso (según su primer programa activo)
        Map<Integer, Integer> cantidadTemasPorCurso = new HashMap<>();
        for (Curso curso : cursosDestacados) {
            List<Programa> programas = programaService.buscarPorCurso(curso);
            cantidadTemasPorCurso.put(curso.getIdCurso(),
                    programas.isEmpty() ? 0 : programas.get(0).getCantidadTemas());
        }

        // Los destacados que solo tienen cohortes próximas llevan la etiqueta «Próximamente»
        java.util.Set<Integer> cursosProximamente = new java.util.HashSet<>();
        cursosDestacados.stream().filter(curso -> cursoService.buscarCohortesAbiertas(curso).isEmpty())
                .forEach(curso -> cursosProximamente.add(curso.getIdCurso()));
        model.addAttribute("cursosProximamente", cursosProximamente);
        model.addAttribute("categorias", categorias);
        model.addAttribute("cursosPorCategoria", cursosPorCategoria);
        // Descuentos vigentes: las cards de los cursos con costo avisan que hay descuentos
        model.addAttribute("descuentosVigentes", descuentoService.buscarVigentes());
        model.addAttribute("descuentoAplicable", pagoService.calcularDescuentoPara(alumnoDe(authentication)).orElse(null));
        model.addAttribute("cursos", cursosDestacados);
        model.addAttribute("cantTemasPorCurso", cantidadTemasPorCurso);
        model.addAttribute("titulo", "Idóneos Online | Cursos de Finanzas, Economía y Mercado de Capitales");
        return "index";
    }

    /**
     * Muestra la página institucional "Acerca de", con las áreas de formación.
     *
     * @param model El modelo de la vista.
     * @return La vista "Acerca de".
     */
    @GetMapping("/acercaDe")
    public String verAcercaDe(Model model) {
        model.addAttribute("titulo", "Acerca de | Idóneos Online");
        return "pages/acercaDe";
    }

    /**
     * Muestra la página de error con un mensaje según el código de estado HTTP.
     *
     * @param request La petición que originó el error.
     * @param model  El modelo de la vista.
     * @return La vista de error.
     */
    @RequestMapping("/error")
    public String verError(HttpServletRequest request, Model model) {
        Object estado = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int codigo = estado != null ? Integer.parseInt(estado.toString()) : 404;
        String encabezado;
        String detalle;
        if (codigo == 403) {
            encabezado = "Acceso denegado";
            detalle = "No tenés permisos para acceder a esta página con tu usuario.";
        } else if (codigo >= 500) {
            encabezado = "Ocurrió un error inesperado";
            detalle = "Tuvimos un problema al procesar tu solicitud. Intentá nuevamente en unos minutos.";
        } else {
            encabezado = "Página no encontrada";
            detalle = "La página que buscás no existe o fue movida.";
        }
        model.addAttribute("codigo", codigo);
        model.addAttribute("encabezado", encabezado);
        model.addAttribute("detalle", detalle);
        model.addAttribute("titulo", encabezado + " | Idóneos Online");
        return "pages/error";
    }

    /** El alumno que está mirando, o {@code null} si es un visitante o no es alumno. */
    private Alumno alumnoDe(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof Usuario usuario ? usuario.getAlumno() : null;
    }
}
