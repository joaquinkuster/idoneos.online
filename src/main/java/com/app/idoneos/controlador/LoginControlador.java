package com.app.idoneos.controlador;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador del inicio de sesión. El procesamiento del formulario lo realiza Spring Security.
 */
@Controller
public class LoginControlador {

    /**
     * Muestra el formulario de inicio de sesión. Si el usuario ya tiene una sesión iniciada,
     * lo redirige al inicio.
     *
     * @param error  Indica que el inicio de sesión anterior falló.
     * @param modelo El modelo de la vista.
     * @param auth   La autenticación actual, si existe.
     * @return La vista de inicio de sesión.
     */
    @GetMapping("/seguridad/login")
    public String verLogin(@RequestParam(value = "error", required = false) String error, Model modelo,
            Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/inicio";
        }
        if (error != null) {
            modelo.addAttribute("error", "Correo o contraseña incorrectos, o la cuenta no está habilitada.");
        }
        modelo.addAttribute("titulo", "Iniciar sesión | Idóneos Online");
        return "pages/seguridad/cu-90-iniciar-sesion";
    }
}
