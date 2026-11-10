package com.app.idoneos.controller;

import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.util.PaginacionUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.model.Descuento;
import com.app.idoneos.service.descuento.DescuentoService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Controlador del descuento. */
@Controller
@RequestMapping("/descuento")
public class DescuentoController {

    @Autowired
    private DescuentoService descuentoService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /descuento/modificar/{id}: Ventana de modificación de un descuento. */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("d", descuentoService.buscarPorId(id).filter(d -> !d.getBaja())
                .orElseThrow(() -> new IllegalArgumentException("Error! El descuento no se encuentra disponible.")));
        return "pages/descuento/modificar :: modal(d=${d})";
    }

    /** GET /descuento/buscar: Busca descuentos. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "orden", defaultValue = "nombre") String orden, Model model, jakarta.servlet.http.HttpServletRequest request) {
        List<Descuento> descuentos = descuentoService.buscarConFiltros(null, null, orden);
        // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
        PaginacionUtil.Pagina<Descuento> pagina = Filtros.tabla.paginar(descuentos, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("descuentos", pagina.filas());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/descuento/buscar :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("nombreBusqueda", nombre);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("ordenSeleccionado", orden);
        model.addAttribute("titulo", "Descuentos | Idóneos Online");
        model.addAttribute("menuActivo", "descuentos");
        return "pages/descuento/buscar";
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /descuento/registrar: Registra un descuento. */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "porcentaje", required = false) Float porcentaje,
            @RequestParam(value = "vigenciaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenciaDesde,
            @RequestParam(value = "vigenciaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenciaHasta,
            @RequestParam(value = "cantidadLimite", required = false) Integer cantidadLimite,
            @RequestParam(value = "cursosRequeridos", required = false) Integer cursosRequeridos) {
        try {
            Descuento descuento = descuentoService.registrar(nombre, porcentaje, vigenciaDesde,
                    vigenciaHasta, cantidadLimite, cursosRequeridos);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Descuento '" + descuento.getNombre() + "' registrado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /descuento/modificar/{id}: Modifica un descuento. */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "porcentaje", required = false) Float porcentaje,
            @RequestParam(value = "vigenciaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenciaDesde,
            @RequestParam(value = "vigenciaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenciaHasta,
            @RequestParam(value = "cantidadLimite", required = false) Integer cantidadLimite,
            @RequestParam(value = "cursosRequeridos", required = false) Integer cursosRequeridos) {
        try {
            descuentoService.modificar(id, nombre, porcentaje, vigenciaDesde, vigenciaHasta,
                    cantidadLimite, cursosRequeridos);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Descuento actualizado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /descuento/darDeBaja/{id}: Da de baja un descuento que nunca se aplicó. */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            descuentoService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Descuento dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /descuento/darDeBajaMasiva: Da de baja varios descuentos a la vez, todos o ninguno. */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids) {
        try {
            descuentoService.darDeBajaVarios(ids);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Descuentos dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Listado (motor común)
    // ----------------------------------------------------------------------

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        private static final java.time.format.DateTimeFormatter DIA_MES_ANIO = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Descuento> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Descuento>("nombre", FiltroUtil.Tipo.TEXTO, Descuento::getNombre),
                new FiltroUtil.Columna<Descuento>("porcentaje", FiltroUtil.Tipo.NUMERO, d -> String.valueOf(d.getPorcentaje())),
                new FiltroUtil.Columna<Descuento>("vigencia", FiltroUtil.Tipo.TEXTO,
                        d -> d.getVigenciaDesde().format(DIA_MES_ANIO) + " al " + d.getVigenciaHasta().format(DIA_MES_ANIO)),
                new FiltroUtil.Columna<Descuento>("usos", FiltroUtil.Tipo.TEXTO, d -> d.getCantidadUsada() + " de " + d.getCantidadLimite()),
                new FiltroUtil.Columna<Descuento>("condicion", FiltroUtil.Tipo.LISTA,
                        d -> d.getCursosRequeridos() > 0 ? "Haber comprado " + d.getCursosRequeridos()
                                + (d.getCursosRequeridos() == 1 ? " curso" : " cursos") : "Sin condición"),
                new FiltroUtil.Columna<Descuento>("estado", FiltroUtil.Tipo.LISTA, Descuento::getEstado),
                new FiltroUtil.Columna<Descuento>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        d -> FiltroUtil.formatearFechaHora(d.getFechaCreacion()))),
                Descuento::getBaja);
    }
}
