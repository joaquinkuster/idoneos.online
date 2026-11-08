package com.app.idoneos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.asistente.AsistenteService;
import com.app.idoneos.service.asistente.AsistenteService.Respuesta;

/**
 * Asistente navegacional: informa qué tiene pendiente el usuario según su rol activo (autoevaluaciones del alumno, cohortes pendientes de
 * contenido del docente, pagos pendientes del administrador). Lo consume el panel flotante de la esquina inferior derecha.
 */
@RestController
@RequestMapping("/asistente")
public class AsistenteController {

    @Autowired
    private AsistenteService asistenteService;

    /**
     * Devuelve lo que muestra el asistente: el saludo, la lista de pendientes (hasta 5) y las acciones rápidas.
     *
     * @param authentication La autenticación actual.
     * @return Los pendientes del usuario.
     */
    @GetMapping("/pendientes")
    public Respuesta pendientes(Authentication authentication) {
        return asistenteService.armar((Usuario) authentication.getPrincipal());
    }
}
