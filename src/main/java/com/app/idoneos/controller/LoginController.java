package com.app.idoneos.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador del inicio de sesión. El procesamiento del formulario lo realiza Spring Security.
 */
@Controller
public class LoginController {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Muestra el formulario de inicio de sesión. Si el usuario ya tiene una sesión iniciada,
     * lo redirige al inicio.
     *
     * @param error  Indica que el inicio de sesión anterior falló.
     * @param logout Indica que el usuario acaba de cerrar sesión.
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual, si existe.
     * @return La vista de inicio de sesión.
     */
    @GetMapping("/login")
    public String ver(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/inicio";
        }
        if (error != null) {
            model.addAttribute("errorLogin", "Correo o contraseña incorrectos, o la cuenta no está habilitada.");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente. ¡Hasta pronto!");
        }
        model.addAttribute("titulo", "Iniciar sesión | Idóneos Online");
        return "pages/login";
    }
}
