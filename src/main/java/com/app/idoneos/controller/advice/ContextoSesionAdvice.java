package com.app.idoneos.controller.advice;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.app.idoneos.model.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Consejo (advice) de controladores con el contexto de la sesión que necesitan todas las pantallas: el usuario autenticado (por
 * ejemplo, para la barra de navegación) y el mensaje que se muestra una sola vez después de un inicio de sesión.
 * Spring lo aplica a todos los controladores, así que ninguno tiene que agregarlos al modelo por su cuenta.
 */
@ControllerAdvice
public class ContextoSesionAdvice {

    /** Nombre del atributo de sesión con un mensaje que se muestra una sola vez en la página siguiente. */
    public static final String MENSAJE_DE_SESION = "mensajeDeSesion";

    /**
     * Si hay un mensaje guardado en la sesión (por ejemplo, el de inicio de sesión exitoso), lo agrega al modelo
     * como "mensaje", para que se muestre una sola vez, y lo elimina de la sesión.
     *
     * @param request La solicitud HTTP.
     * @param model    El modelo de la vista.
     */
    @ModelAttribute
    public void agregarMensajeDeSesion(HttpServletRequest request, Model model) {
        HttpSession sesion = request.getSession(false); // no crea sesiones para los visitantes
        Object mensaje = sesion == null ? null : sesion.getAttribute(MENSAJE_DE_SESION);
        if (mensaje != null) {
            sesion.removeAttribute(MENSAJE_DE_SESION);
            if (!model.containsAttribute("mensaje")) {
                model.addAttribute("mensaje", mensaje);
            }
        }
    }

    /**
     * Agrega el usuario autenticado al modelo de todas las vistas.
     *
     * @param authentication La autenticación actual, si existe.
     * @return El usuario autenticado, o {@code null} si no hay sesión iniciada.
     */
    @ModelAttribute("usuario")
    public Usuario agregarUsuario(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            return (Usuario) authentication.getPrincipal();
        }
        return null;
    }
}
