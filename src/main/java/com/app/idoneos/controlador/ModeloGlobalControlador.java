package com.app.idoneos.controlador;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.app.idoneos.modelo.Usuario;

/**
 * Consejo global de controladores. Garantiza que el usuario autenticado esté siempre disponible
 * en el modelo de las plantillas Thymeleaf (por ejemplo, para la barra de navegación).
 */
@ControllerAdvice
public class ModeloGlobalControlador {

    /**
     * Agrega el usuario autenticado al modelo de todas las vistas.
     *
     * @param auth La autenticación actual, si existe.
     * @return El usuario autenticado, o {@code null} si no hay sesión iniciada.
     */
    @ModelAttribute("usuario")
    public Usuario agregarUsuario(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        return null;
    }
}
