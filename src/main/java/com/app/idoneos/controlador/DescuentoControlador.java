package com.app.idoneos.controlador;

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

import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.servicio.Descuento.DescuentoServicioImpl;
import com.app.idoneos.utilidades.RespuestaUtilidad;

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
public class DescuentoControlador {

    @Autowired
    private DescuentoServicioImpl descuentoServicio;

    /**
     * CU-56: Busca descuentos. El listado llega completo y los filtros se aplican en la pantalla.
     *
     * @param nombre Parte del nombre (opcional).
     * @param estado Vigente, Próximo, Vencido, Agotado o Dado de baja (opcional).
     * @param orden  "nombre" (A–Z), "recientes" o "porcentaje".
     * @param modelo El modelo de la vista.
     * @return La vista de búsqueda de descuentos.
     */
    @GetMapping("/buscar")
    public String buscarDescuentos(@RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "orden", defaultValue = "nombre") String orden, Model modelo) {
        List<Descuento> descuentos = descuentoServicio.buscarConFiltros(null, null, orden);
        modelo.addAttribute("descuentos", descuentos);
        modelo.addAttribute("nombreBusqueda", nombre);
        modelo.addAttribute("estadoSeleccionado", estado);
        modelo.addAttribute("ordenSeleccionado", orden);
        modelo.addAttribute("titulo", "Descuentos | Idóneos Online");
        modelo.addAttribute("menuActivo", "descuentos");
        return "pages/descuento/buscar";
    }

    /**
     * CU-57: Registra un descuento. Responde en JSON.
     *
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarDescuento(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "porcentaje", required = false) Float porcentaje,
            @RequestParam(value = "vigenciaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenciaDesde,
            @RequestParam(value = "vigenciaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenciaHasta,
            @RequestParam(value = "cantidadLimite", required = false) Integer cantidadLimite,
            @RequestParam(value = "cursosRequeridos", required = false) Integer cursosRequeridos) {
        try {
            Descuento descuento = descuentoServicio.registrarDescuento(nombre, porcentaje, vigenciaDesde,
                    vigenciaHasta, cantidadLimite, cursosRequeridos);
            return RespuestaUtilidad.respuestaExitosa("Descuento '" + descuento.getNombre() + "' registrado con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-58: Modifica un descuento. Responde en JSON.
     *
     * @param id Identificador del descuento.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarDescuento(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "porcentaje", required = false) Float porcentaje,
            @RequestParam(value = "vigenciaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenciaDesde,
            @RequestParam(value = "vigenciaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenciaHasta,
            @RequestParam(value = "cantidadLimite", required = false) Integer cantidadLimite,
            @RequestParam(value = "cursosRequeridos", required = false) Integer cursosRequeridos) {
        try {
            descuentoServicio.modificarDescuento(id, nombre, porcentaje, vigenciaDesde, vigenciaHasta,
                    cantidadLimite, cursosRequeridos);
            return RespuestaUtilidad.respuestaExitosa("Descuento actualizado con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-59: Da de baja un descuento que nunca se aplicó. Responde en JSON.
     *
     * @param id Identificador del descuento.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaDescuento(@PathVariable("id") Integer id) {
        try {
            descuentoServicio.darDeBajaDescuento(id);
            return RespuestaUtilidad.respuestaExitosa("Descuento dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
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
            descuentoServicio.darDeBajaVarios(ids);
            return RespuestaUtilidad.respuestaExitosa("Descuentos dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }
}
