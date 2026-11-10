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

/** Controlador de la categoría. */
@Controller
@RequestMapping("/categoria")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CursoService cursoService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /categoria/buscar: Busca categorías por nombre. */
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

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /categoria/registrar: Registra una categoría. */
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

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /categoria/modificar/{id}: Modifica el nombre y la descripción de una categoría. */
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

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /categoria/darDeBaja/{id}: Da de baja una categoría que no tenga cursos activos asociados. */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            categoriaService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Categoría dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /categoria/darDeBajaMasiva: Da de baja varios categorías a la vez, todos o ninguno: si alguno no puede darse de baja. */
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

    // ----------------------------------------------------------------------
    // Ventana de modificación a pedido
    // ----------------------------------------------------------------------

    /** GET /categoria/modificar/{id}: Ventana de modificación de una categoría. */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        Categoria cat = categoriaService.buscarPorId(id).filter(c -> !c.getBaja())
                .orElseThrow(() -> new IllegalArgumentException("Error! La categoría no se encuentra disponible."));
        model.addAttribute("cat", cat);
        model.addAttribute("inscripcionesPorCategoria", Map.of(cat.getIdCategoria(), categoriaService.contarInscripcionesActivas(cat)));
        model.addAttribute("categoriaBloqueada", categoriaService.tieneCursosConCohortesPublicadas(cat));
        return "pages/categoria/modificar :: modal(cat=${cat})";
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ----------------------------------------------------------------------
        // Listado (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Categoria> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Categoria>("nombre", FiltroUtil.Tipo.TEXTO, Categoria::getNombre),
                new FiltroUtil.Columna<Categoria>("descripcion", FiltroUtil.Tipo.TEXTO, c -> c.getDescripcion()),
                new FiltroUtil.Columna<Categoria>("fechaCreacion", FiltroUtil.Tipo.FECHA, c -> FiltroUtil.formatearFechaHora(c.getFechaCreacion())),
                new FiltroUtil.Columna<Categoria>("estado", FiltroUtil.Tipo.LISTA, c -> c.getBaja() ? "Dada de baja" : "Activa")),
                Categoria::getBaja);
    }
}
