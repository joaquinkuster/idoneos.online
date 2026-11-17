package com.app.idoneos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.notificacion.NotificacionService;

/** Controlador de la notificación. */
@Controller
@RequestMapping("/notificacion")
public class NotificacionController {

    /** Cantidad máxima de notificaciones que muestra el panel cuando se piden todas. */
    private static final int MAXIMO_EN_EL_PANEL = 100;

    @Autowired
    private NotificacionService notificacionService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /notificacion/panel: Muestra las notificaciones del usuario en el panel de la campana, las últimas o todas. */
    @GetMapping("/panel")
    public String panel(@RequestParam(value = "todas", defaultValue = "false") boolean todas, Model model, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        model.addAttribute("notificaciones", todas
                ? notificacionService.buscarTodas(usuario).stream().limit(MAXIMO_EN_EL_PANEL).toList()
                : notificacionService.buscarRecientes(usuario));
        return "fragments/layout/notificaciones :: panel";
    }

    /** GET /notificacion/abrir/{id}: Marca la notificación como leída y lleva a la pantalla a la que se refiere. */
    @GetMapping("/abrir/{id}")
    public String abrir(@PathVariable("id") Integer id, Authentication authentication) {
        String destino = notificacionService.abrir(id, (Usuario) authentication.getPrincipal());
        return "redirect:" + (destino == null || destino.isBlank() ? "/inicio" : destino);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /notificacion/leerTodas: Marca como leídas todas las notificaciones del usuario. */
    @PostMapping("/leerTodas")
    public ResponseEntity<Void> leerTodas(Authentication authentication) {
        notificacionService.marcarTodasLeidas((Usuario) authentication.getPrincipal());
        return ResponseEntity.noContent().build();
    }
}
