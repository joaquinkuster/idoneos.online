package com.app.idoneos.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Controlador del inicio de sesión. */
@Controller
public class LoginController {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /login: Muestra el formulario de inicio de sesión. */
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
