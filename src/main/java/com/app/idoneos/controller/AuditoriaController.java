package com.app.idoneos.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.idoneos.model.Auditoria;
import com.app.idoneos.service.auditoria.AuditoriaService;
import com.app.idoneos.service.detalleauditoria.DetalleAuditoriaService;
import com.app.idoneos.util.FiltroUtil;
import com.app.idoneos.util.PaginacionUtil;

import jakarta.servlet.http.HttpServletRequest;

/** Controlador de la auditoría: solo consulta. Los registros los generan los disparadores de la base de datos. */
@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private DetalleAuditoriaService detalleAuditoriaService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /auditoria/buscar: Lista las acciones registradas, filtradas por usuario, tipo de acción, entidad y fecha. */
    @GetMapping("/buscar")
    public String buscar(Model model, HttpServletRequest request) {
        List<Auditoria> registros = auditoriaService.buscarRecientes();
        // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
        PaginacionUtil.Pagina<Auditoria> pagina = Filtros.tabla.paginar(registros, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("registros", pagina.filas());
        Map<Integer, String> entidades = new HashMap<>();
        pagina.filas().forEach(a -> entidades.put(a.getIdAuditoria(), legible(a.getEntidadAfectada())));
        model.addAttribute("entidades", entidades);
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/auditoria/buscar :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("titulo", "Auditoría | Idóneos Online");
        model.addAttribute("menuActivo", "auditoria");
        return "pages/auditoria/buscar";
    }

    /** GET /auditoria/detalle/{id}: Ventana con los campos afectados por una acción, con su valor anterior y su valor nuevo. */
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") Integer id, Model model) {
        Auditoria auditoria = auditoriaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El registro de auditoría no se encuentra disponible."));
        model.addAttribute("a", auditoria);
        model.addAttribute("entidad", legible(auditoria.getEntidadAfectada()));
        model.addAttribute("detalles", detalleAuditoriaService.buscarPorAuditoria(auditoria));
        return "pages/auditoria/detalle :: modal(a=${a})";
    }

    /** Pasa el nombre de la entidad (por ejemplo TemaRecurso) a como se lee (Tema recurso). */
    static String legible(String entidad) {
        String separado = entidad.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
        return separado.isEmpty() ? separado : separado.substring(0, 1) + separado.substring(1).toLowerCase();
    }

    // ----------------------------------------------------------------------
    // Listado (motor común)
    // ----------------------------------------------------------------------

    /** Columnas de filtro, orden y página del listado (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Auditoria> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Auditoria>("usuario", FiltroUtil.Tipo.TEXTO,
                        a -> a.getUsuario().getNombreCompleto() + " " + a.getUsuario().getCorreo()),
                new FiltroUtil.Columna<Auditoria>("accion", FiltroUtil.Tipo.LISTA, a -> a.getTipoAuditoria().getNombre()),
                new FiltroUtil.Columna<Auditoria>("entidad", FiltroUtil.Tipo.LISTA, a -> legible(a.getEntidadAfectada())),
                new FiltroUtil.Columna<Auditoria>("registro", FiltroUtil.Tipo.NUMERO, a -> String.valueOf(a.getIdRegistro())),
                new FiltroUtil.Columna<Auditoria>("ip", FiltroUtil.Tipo.TEXTO, Auditoria::getIpUsuario),
                new FiltroUtil.Columna<Auditoria>("fecha", FiltroUtil.Tipo.FECHA, a -> FiltroUtil.formatearFechaHora(a.getFechaHora()))),
                a -> false);
    }
}
