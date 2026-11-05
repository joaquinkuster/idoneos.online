package com.app.idoneos.controller;

import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.util.PaginacionUtil;
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

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import com.app.idoneos.service.categoria.CategoriaService;
import com.app.idoneos.service.curso.CursoService;

import com.app.idoneos.util.RespuestaFormularioUtil;

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
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CursoService cursoService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-07: Busca categorías por nombre.
     *
     * @param nombre Parte del nombre de la categoría.
     * @param baja   Si es true, solo las dadas de baja; si es false, solo las vigentes; si es nulo, todas.
     * @param orden  Orden de los resultados: "nombre" (A–Z) o "recientes". Las dadas de baja van al final.
     * @param model El modelo de la vista.
     * @return La vista de búsqueda de categorías.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "baja", required = false) Boolean baja,
            @RequestParam(value = "orden", defaultValue = "nombre") String orden, Model model, jakarta.servlet.http.HttpServletRequest request) {
        List<Categoria> categorias = categoriaService.buscarConFiltros(null, null, orden); // lista completa, ordenada: el motor filtra y pagina
        // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
        PaginacionUtil.Pagina<Categoria> pagina = Filtros.tabla.paginar(categorias, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        categorias = pagina.filas();

        // Cursos activos e inscripciones activas por categoría, para validar la modificación y la baja
        Map<Integer, List<Curso>> cursosPorCategoria = new HashMap<>();
        Map<Integer, Long> inscripcionesPorCategoria = new HashMap<>();
        for (Categoria categoria : categorias) {
            cursosPorCategoria.put(categoria.getIdCategoria(), cursoService.buscarPorCategoria(categoria));
            inscripcionesPorCategoria.put(categoria.getIdCategoria(),
                    categoriaService.contarInscripcionesActivas(categoria));
        }

        model.addAttribute("categorias", categorias);
        model.addAttribute("cursosPorCategoria", cursosPorCategoria);
        model.addAttribute("inscripcionesPorCategoria", inscripcionesPorCategoria);
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/categoria/buscar :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("nombreBusqueda", nombre);
        model.addAttribute("bajaSeleccionada", baja);
        model.addAttribute("ordenSeleccionado", orden);
        model.addAttribute("titulo", "Categorías | Idóneos Online");
        model.addAttribute("menuActivo", "categorias");
        return "pages/categoria/buscar";
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-08: Registra una categoría. Responde en JSON para que el formulario muestre el resultado
     * sin recargar la página.
     *
     * @param nombre      El nombre de la categoría.
     * @param descripcion La descripción de la categoría (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        try {
            Categoria categoria = categoriaService.registrar(nombre, descripcion);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Categoría '" + categoria.getNombre() + "' registrada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-09: Modifica el nombre y la descripción de una categoría. Responde en JSON.
     *
     * @param id          Identificador de la categoría.
     * @param nombre      El nuevo nombre.
     * @param descripcion La nueva descripción (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        try {
            categoriaService.modificar(id, nombre, descripcion);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Categoría actualizada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-10: Da de baja una categoría que no tenga cursos activos asociados. Responde en JSON.
     *
     * @param id Identificador de la categoría.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            categoriaService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Categoría dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
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
            categoriaService.darDeBajaVarios(ids);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Categorías dadas de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Ventana de modificación a pedido

    /**
     * Ventana de modificación de una categoría, que el listado pide al abrirla (en lugar de dibujar una por fila).
     *
     * @param id     La categoría.
     * @param model El modelo de la vista.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        Categoria cat = categoriaService.buscarPorId(id).filter(c -> !c.getBaja())
                .orElseThrow(() -> new IllegalArgumentException("Error! La categoría no se encuentra disponible."));
        model.addAttribute("cat", cat);
        model.addAttribute("inscripcionesPorCategoria", Map.of(cat.getIdCategoria(), categoriaService.contarInscripcionesActivas(cat)));
        return "pages/categoria/modificar :: modal(cat=${cat})";
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado (motor común)


        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<Categoria> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Categoria>("nombre", FiltroUtil.Tipo.TEXTO, Categoria::getNombre),
                new FiltroUtil.Columna<Categoria>("descripcion", FiltroUtil.Tipo.TEXTO, c -> c.getDescripcion()),
                new FiltroUtil.Columna<Categoria>("fechaCreacion", FiltroUtil.Tipo.FECHA, c -> FiltroUtil.formatearFechaHora(c.getFechaCreacion())),
                new FiltroUtil.Columna<Categoria>("estado", FiltroUtil.Tipo.LISTA, c -> c.getBaja() ? "Dada de baja" : "Activa")),
                Categoria::getBaja);
    }
}
