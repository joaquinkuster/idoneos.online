package com.app.idoneos.controlador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;
import com.app.idoneos.utilidades.PaginacionUtilidad;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador de las páginas institucionales: inicio, acerca de y página de error. Reemplaza al controlador
 * de errores por defecto de Spring Boot para mostrar un mensaje claro cuando una ruta no existe, no se tiene
 * acceso a ella o ocurre un fallo inesperado.
 */
@Controller
public class InicioControlador implements ErrorController {

    /** Cantidad de cursos que se destacan en el inicio. */
    private static final int CURSOS_DESTACADOS = 3;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    @Autowired
    private ModalidadServicioImpl modalidadServicio;

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    /**
     * Muestra la página principal con los cursos destacados del catálogo (los que tienen cohortes
     * con inscripción abierta) y las categorías.
     *
     * @param modelo El modelo de la vista.
     * @return La vista de inicio.
     */
    @GetMapping({ "/", "/inicio" })
    public String verInicio(Model modelo) {
        List<Categoria> categorias = categoriaServicio.obtenerTodo();
        List<Curso> cursosDestacados = PaginacionUtilidad.obtenerPagina(
                cursoServicio.buscarEnCatalogo(null, null, null, null, null), 0, CURSOS_DESTACADOS);

        // Cantidad de unidades temáticas de cada curso (según su primer programa activo)
        Map<Integer, Integer> cantidadUnidadesPorCurso = new HashMap<>();
        for (Curso curso : cursosDestacados) {
            List<Programa> programas = programaServicio.buscarPorCurso(curso);
            cantidadUnidadesPorCurso.put(curso.getIdCurso(),
                    programas.isEmpty() ? 0 : programas.get(0).getCantidadUnidades());
        }

        modelo.addAttribute("categorias", categorias);
        modelo.addAttribute("cursos", cursosDestacados);
        modelo.addAttribute("cantUnidadesPorCurso", cantidadUnidadesPorCurso);
        modelo.addAttribute("titulo", "Idóneos Online | Cursos de Finanzas, Economía y Mercado de Capitales");
        return "index";
    }

    /**
     * Muestra la página institucional "Acerca de", con las áreas de formación.
     *
     * @param modelo El modelo de la vista.
     * @return La vista "Acerca de".
     */
    @GetMapping("/acercaDe")
    public String verAcercaDe(Model modelo) {
        modelo.addAttribute("categorias", categoriaServicio.buscarConFiltros(null, false, "nombre"));
        // Identificadores de las modalidades, para enlazar cada una con el catálogo filtrado
        for (Modalidad modalidad : modalidadServicio.obtenerTodo()) {
            switch (modalidad.getNombre()) {
                case Modalidad.EN_VIVO -> modelo.addAttribute("idEnVivo", modalidad.getIdModalidad());
                case Modalidad.GRABADA -> modelo.addAttribute("idGrabada", modalidad.getIdModalidad());
                case Modalidad.CLON_IA -> modelo.addAttribute("idClonIa", modalidad.getIdModalidad());
                default -> { }
            }
        }
        modelo.addAttribute("titulo", "Acerca de | Idóneos Online");
        return "pages/acercaDe";
    }

    /**
     * Muestra la página de error con un mensaje según el código de estado HTTP.
     *
     * @param request La petición que originó el error.
     * @param modelo  El modelo de la vista.
     * @return La vista de error.
     */
    @RequestMapping("/error")
    public String verError(HttpServletRequest request, Model modelo) {
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
        modelo.addAttribute("codigo", codigo);
        modelo.addAttribute("encabezado", encabezado);
        modelo.addAttribute("detalle", detalle);
        modelo.addAttribute("titulo", encabezado + " | Idóneos Online");
        return "pages/error";
    }
}
