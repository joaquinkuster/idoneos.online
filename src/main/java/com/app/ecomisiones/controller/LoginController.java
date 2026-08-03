package com.app.ecomisiones.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.ecomisiones.model.RolUsuario;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.service.Usuario.UsuarioServiceImpl;

import jakarta.validation.Valid;

/**
 * Controlador para la autenticación y registro de usuarios en Idóneos Online.
 */
@Controller
public class LoginController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    @GetMapping("/login")
    public String verLogin(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout, Model model) {
        
        if (error != null) {
            model.addAttribute("mensaje", "Error! La contraseña o el correo ingresado es inválido.");
        } else if (logout != null) {
            model.addAttribute("mensaje", "Hecho! Has cerrado sesión correctamente.");
        }
        
        model.addAttribute("titulo", "Iniciar Sesión | Idóneos Online");
        return "pages/login";
    }

    @GetMapping("/registro")
    public String verRegistro(Model modelo) {
        modelo.addAttribute("titulo", "Crear Cuenta | Idóneos Online");
        return "pages/registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@Valid @ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            usuario.setRol(RolUsuario.Alumno);
            Usuario nuevoUsuario = usuarioService.guardar(usuario);

            if (nuevoUsuario == null) {
                throw new IllegalArgumentException("Error! El usuario ingresado es inválido.");
            }

            redirectAttributes.addFlashAttribute("mensaje", "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/registro";
        }
    }
}
