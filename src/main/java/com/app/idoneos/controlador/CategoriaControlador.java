package com.app.idoneos.controlador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

import com.app.idoneos.utilidades.Utilidades;

/**
 * Controlador de la gestión de categorías (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso:
 * CU-07 Buscar categoría (GET /categoria/buscar), CU-08 Registrar categoría, CU-09 Modificar categoría
 * y CU-10 Dar de baja categoría. Los formularios de alta, modificación y baja se muestran como ventanas modales
 * de la pantalla de búsqueda.
 */
@Controller
@RequestMapping("/categoria")
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
     * @param orden  Orden de los resultados: "nombre" (A–Z) o "recientes". Las dadas de baja van al final.
     * @param modelo El modelo de la vista.
     * @return La vista de búsqueda de categorías.
     */
    @GetMapping("/buscar")
    public String buscarCategorias(@RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "baja", required = false) Boolean baja,
            @RequestParam(value = "orden", defaultValue = "nombre") String orden, Model modelo) {
        List<Categoria> categorias = categoriaServicio.buscarConFiltros(nombre, baja, orden);

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
        modelo.addAttribute("ordenSeleccionado", orden);
        modelo.addAttribute("titulo", "Categorías | Idóneos Online");
        modelo.addAttribute("menuActivo", "categorias");
        return "pages/panel/categorias";
    }

    /**
     * CU-08: Registra una categoría. Responde en JSON para que el formulario muestre el resultado
     * sin recargar la página.
     *
     * @param nombre      El nombre de la categoría.
     * @param descripcion La descripción de la categoría (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarCategoria(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        try {
            Categoria categoria = categoriaServicio.registrarCategoria(nombre, descripcion);
            return Utilidades.respuestaExitosa("Categoría '" + categoria.getNombre() + "' registrada con éxito.");
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
        }
    }

    /**
     * CU-09: Modifica el nombre y la descripción de una categoría. Responde en JSON.
     *
     * @param id          Identificador de la categoría.
     * @param nombre      El nuevo nombre.
     * @param descripcion La nueva descripción (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarCategoria(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        try {
            categoriaServicio.modificarCategoria(id, nombre, descripcion);
            return Utilidades.respuestaExitosa("Categoría actualizada con éxito.");
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
        }
    }

    /**
     * CU-10: Da de baja una categoría que no tenga cursos activos asociados. Responde en JSON.
     *
     * @param id Identificador de la categoría.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaCategoria(@PathVariable("id") Integer id) {
        try {
            categoriaServicio.darDeBajaCategoria(id);
            return Utilidades.respuestaExitosa("Categoría dada de baja con éxito.");
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
        }
    }

    /**
     * Da de baja varios categorías a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja
     * ninguno. Responde en JSON.
     *
     * @param ids Identificadores de los categorías seleccionados.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids) {
        try {
            categoriaServicio.darDeBajaVarios(ids);
            return Utilidades.respuestaExitosa("Categorías dadas de baja con éxito.");
        } catch (Exception e) {
            return Utilidades.respuestaConError(e);
        }
    }
}
