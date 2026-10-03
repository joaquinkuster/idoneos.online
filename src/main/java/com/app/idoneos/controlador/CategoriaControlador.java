package com.app.idoneos.controlador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;

/**
 * Controlador de la gestión de categorías (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso:
 * CU-07 Buscar categoría (GET /cursos/categorias), CU-08 Registrar categoría, CU-09 Modificar categoría
 * y CU-10 Dar de baja categoría. Los formularios de alta, modificación y baja se muestran como ventanas modales
 * de la pantalla de búsqueda.
 */
@Controller
@RequestMapping("/cursos/categorias")
public class CategoriaControlador {

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    /**
     * CU-07: Busca categorías por nombre.
     *
     * @param nombre Parte del nombre de la categoría.
     * @param baja   Si es true, solo las dadas de baja; si es false, solo las vigentes; si es nulo, todas.
     * @param modelo El modelo de la vista.
     * @return La vista de búsqueda de categorías.
     */
    @GetMapping
    public String buscarCategorias(@RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "baja", required = false) Boolean baja, Model modelo) {
        List<Categoria> categorias = categoriaServicio.buscarConFiltros(nombre, baja);

        // Cursos activos e inscripciones activas por categoría, para validar la modificación y la baja
        Map<Integer, List<Curso>> cursosPorCategoria = new HashMap<>();
        Map<Integer, Long> inscripcionesPorCategoria = new HashMap<>();
        for (Categoria categoria : categorias) {
            cursosPorCategoria.put(categoria.getIdCategoria(), cursoServicio.buscarPorCategoria(categoria));
            inscripcionesPorCategoria.put(categoria.getIdCategoria(),
                    categoriaServicio.contarInscripcionesActivas(categoria));
        }

        modelo.addAttribute("categorias", categorias);
        modelo.addAttribute("cursosPorCategoria", cursosPorCategoria);
        modelo.addAttribute("inscripcionesPorCategoria", inscripcionesPorCategoria);
        modelo.addAttribute("nombreBusqueda", nombre);
        modelo.addAttribute("bajaSeleccionada", baja);
        modelo.addAttribute("titulo", "CU-07 - Buscar categoría | Idóneos Online");
        return "pages/cursos/cu-07-buscar-categoria";
    }

    /**
     * CU-08: Registra una categoría.
     *
     * @param nombre             El nombre de la categoría.
     * @param descripcion        La descripción de la categoría (opcional).
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al listado de categorías, o al formulario si hubo un error.
     */
    @PostMapping("/guardar")
    public String registrarCategoria(@RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaServicio.registrarCategoria(nombre, descripcion);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Categoría '" + categoria.getNombre() + "' registrada con éxito.");
            return "redirect:/cursos/categorias";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/categorias";
        }
    }

    /**
     * CU-09: Modifica el nombre y la descripción de una categoría.
     *
     * @param id                 Identificador de la categoría.
     * @param nombre             El nuevo nombre.
     * @param descripcion        La nueva descripción (opcional).
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al listado de categorías, o al formulario si hubo un error.
     */
    @PostMapping("/{id}/editar")
    public String modificarCategoria(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            RedirectAttributes redirectAttributes) {
        try {
            categoriaServicio.modificarCategoria(id, nombre, descripcion);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría actualizada con éxito.");
            return "redirect:/cursos/categorias";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos/categorias";
        }
    }

    /**
     * CU-10: Da de baja una categoría que no tenga cursos activos asociados.
     *
     * @param id                 Identificador de la categoría.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al listado de categorías.
     */
    @PostMapping("/{id}/baja")
    public String darDeBajaCategoria(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoriaServicio.darDeBajaCategoria(id);
            redirectAttributes.addFlashAttribute("mensaje", "Categoría dada de baja con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos/categorias";
    }
}
