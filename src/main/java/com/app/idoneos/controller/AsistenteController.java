package com.app.idoneos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.asistente.AsistenteService;
import com.app.idoneos.service.asistente.AsistenteService.Respuesta;

/** Controlador del asistente. */
@RestController
@RequestMapping("/asistente")
public class AsistenteController {

    @Autowired
    private AsistenteService asistenteService;

    /** GET /asistente/pendientes: Devuelve lo que muestra el asistente: el saludo, la lista de pendientes (hasta 5) y las acciones rápidas. */
    @GetMapping("/pendientes")
    public Respuesta pendientes(Authentication authentication) {
        return asistenteService.armar((Usuario) authentication.getPrincipal());
    }
}
