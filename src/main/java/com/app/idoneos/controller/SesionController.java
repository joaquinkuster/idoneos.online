package com.app.idoneos.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.idoneos.config.SesionTokenFilter;
import com.app.idoneos.model.Sesion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.sesion.SesionService;
import com.app.idoneos.util.AgrupacionListadoUtil;
import com.app.idoneos.util.AgrupacionUtil;
import com.app.idoneos.util.FiltroUtil;
import com.app.idoneos.util.PaginacionUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

import jakarta.servlet.http.HttpServletRequest;

/** Controlador de la sesión. */
@Controller
@RequestMapping("/sesion")
public class SesionController {

    @Autowired
    private SesionService sesionService;

    @Autowired
    private com.app.idoneos.service.notificacion.NotificacionService notificacionService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /sesion/buscar: Busca las sesiones activas del usuario. El administrador ve las de todos los usuarios. */
    @GetMapping("/buscar")
    public String buscar(Model model, HttpServletRequest request, Authentication authentication) {
        Usuario actor = (Usuario) authentication.getPrincipal();
        boolean administrador = actor.esAdministradorActivo();
        List<Sesion> sesiones = administrador ? sesionService.buscarTodasVigentes() : sesionService.buscarVigentes(actor);
        // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
        // El administrador ve las sesiones agrupadas por usuario
        PaginacionUtil.Pagina<Sesion> pagina = administrador
                ? AgrupacionListadoUtil.paginar(Filtros.tabla, sesiones, request.getParameterMap(), request, model,
                        s -> s.getUsuario().getNombreCompleto() + " · " + s.getUsuario().getCorreo(), null, null, "usuario", "inicio")
                : Filtros.tabla.paginar(sesiones, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("sesiones", pagina.filas());
        model.addAttribute("tokenActual", SesionTokenFilter.leerToken(request));
        model.addAttribute("esAdministrador", administrador);
        model.addAttribute("idActual", actor.getIdUsuario());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/sesion/tabla :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("limite", sesionService.obtenerLimite());
        model.addAttribute("titulo", (administrador ? "Sesiones" : "Mis sesiones") + " | Idóneos Online");
        model.addAttribute("menuActivo", "sesiones");
        return administrador ? "pages/sesion/buscarPorAdministrador" : "pages/sesion/buscarPorUsuario";
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /sesion/heartbeat: Mantiene viva la sesión mientras haya una pantalla abierta, y devuelve la cantidad de notificaciones sin leer. La renovación la hace el filtro de la sesión. */
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Long>> heartbeat(Authentication authentication) {
        // Además informa cuántas notificaciones sin leer tiene el usuario, para actualizar la campana del navbar
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas((Usuario) authentication.getPrincipal())));
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /sesion/cerrar/{id}: Cierra de forma forzada una sesión distinta de la actual. */
    @PostMapping("/cerrar/{id}")
    public ResponseEntity<Map<String, String>> cerrar(@PathVariable("id") Integer id, HttpServletRequest request, Authentication authentication) {
        try {
            Usuario actor = (Usuario) authentication.getPrincipal();
            sesionService.cerrar(id, actor.getIdUsuario(), actor.esAdministradorActivo(), SesionTokenFilter.leerToken(request));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Sesión cerrada con éxito.");
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

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Sesion> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Sesion>("usuario", FiltroUtil.Tipo.TEXTO,
                        s -> s.getUsuario().getNombreCompleto() + " " + s.getUsuario().getCorreo()),
                new FiltroUtil.Columna<Sesion>("inicio", FiltroUtil.Tipo.FECHA, s -> FiltroUtil.formatearFechaHora(s.getFechaInicio())),
                new FiltroUtil.Columna<Sesion>("vence", FiltroUtil.Tipo.FECHA, s -> FiltroUtil.formatearFechaHora(s.getFechaFin())),
                new FiltroUtil.Columna<Sesion>("ip", FiltroUtil.Tipo.TEXTO, Sesion::getIp),
                new FiltroUtil.Columna<Sesion>("dispositivo", FiltroUtil.Tipo.TEXTO, Sesion::getDispositivo),
                new FiltroUtil.Columna<Sesion>("tipo", FiltroUtil.Tipo.LISTA,
                        s -> s.getToken().startsWith(SesionService.PREFIJO_RECORDADA) ? "Recordada" : "Común")),
                s -> false);
    }
}
