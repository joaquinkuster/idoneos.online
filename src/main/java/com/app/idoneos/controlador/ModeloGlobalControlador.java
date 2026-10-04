package com.app.idoneos.controlador;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.app.idoneos.modelo.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Consejo global de controladores. Garantiza que el usuario autenticado esté siempre disponible
 * en el modelo de las plantillas Thymeleaf (por ejemplo, para la barra de navegación).
 */
@ControllerAdvice
public class ModeloGlobalControlador {

    /** Nombre del atributo de sesión con un mensaje que se muestra una sola vez en la página siguiente. */
    public static final String MENSAJE_DE_SESION = "mensajeDeSesion";

    /**
     * Si hay un mensaje guardado en la sesión (por ejemplo, el de inicio de sesión exitoso), lo agrega al modelo
     * como "mensaje", para que se muestre una sola vez, y lo elimina de la sesión.
     *
     * @param solicitud La solicitud HTTP.
     * @param modelo    El modelo de la vista.
     */
    @ModelAttribute
    public void agregarMensajeDeSesion(HttpServletRequest solicitud, Model modelo) {
        HttpSession sesion = solicitud.getSession(false); // no crea sesiones para los visitantes
        Object mensaje = sesion == null ? null : sesion.getAttribute(MENSAJE_DE_SESION);
        if (mensaje != null) {
            sesion.removeAttribute(MENSAJE_DE_SESION);
            if (!modelo.containsAttribute("mensaje")) {
                modelo.addAttribute("mensaje", mensaje);
            }
        }
    }

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
