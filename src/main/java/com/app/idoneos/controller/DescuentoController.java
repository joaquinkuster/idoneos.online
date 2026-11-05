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

/**
 * Controlador de la gestión de descuentos (MOD-F-03), solo para el Administrador.
 *
 * Mapea las pantallas de los casos de uso:
 * CU-56 Buscar descuento (GET /descuento/buscar), CU-57 Registrar descuento, CU-58 Modificar descuento
 * y CU-59 Dar de baja descuento. Los formularios de alta, modificación y baja se muestran como ventanas modales
 * de la pantalla de búsqueda.
 */
@Controller
@RequestMapping("/descuento")
public class DescuentoController {

    @Autowired
    private DescuentoService descuentoService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Ventana de modificación de un descuento, que el listado pide al abrirla (en lugar de dibujar una por fila).
     *
     * @param id     El descuento.
     * @param model El modelo de la vista.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("d", descuentoService.buscarPorId(id).filter(d -> !d.getBaja())
                .orElseThrow(() -> new IllegalArgumentException("Error! El descuento no se encuentra disponible.")));
        return "pages/descuento/modificar :: modal(d=${d})";
    }

    /**
     * CU-56: Busca descuentos. El listado llega completo y los filtros se aplican en la pantalla.
     *
     * @param nombre Parte del nombre (opcional).
     * @param estado Vigente, Próximo, Vencido, Agotado o Dado de baja (opcional).
     * @param orden  "nombre" (A–Z), "recientes" o "porcentaje".
     * @param model El modelo de la vista.
     * @return La vista de búsqueda de descuentos.
     */
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

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-57: Registra un descuento. Responde en JSON.
     *
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
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

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-58: Modifica un descuento. Responde en JSON.
     *
     * @param id Identificador del descuento.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
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

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-59: Da de baja un descuento que nunca se aplicó. Responde en JSON.
     *
     * @param id Identificador del descuento.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            descuentoService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Descuento dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * Da de baja varios descuentos a la vez, todos o ninguno. Responde en JSON.
     *
     * @param ids Identificadores de los descuentos seleccionados.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
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

    // ---------------------------------------------------------------- Listado (motor común)



    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        private static final java.time.format.DateTimeFormatter DIA_MES_ANIO = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
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
