package com.app.idoneos.controlador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;
import com.app.idoneos.utilidades.Utilidades;

/**
 * Controlador de las páginas institucionales: inicio y acerca de.
 */
@Controller
public class InicioControlador {

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
        List<Curso> cursosDestacados = Utilidades.obtenerPagina(
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

}
