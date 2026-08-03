package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.Alumno;
import com.app.ecomisiones.model.RolUsuario;
import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.repository.AlumnoRepository;
import com.app.ecomisiones.service.Usuario.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para la autenticación y registro de usuarios en Idóneos Online.
 */
@Controller
public class LoginController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @GetMapping("/login")
    public String verLogin(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("mensaje", "Error! La contraseña o el correo ingresado es inválido.");
        } else if (logout != null) {
            model.addAttribute("mensaje", "Hecho! Has cerrado sesión correctamente.");
        }
        model.addAttribute("titulo", "Iniciar Sesión | Idóneos Online");
        return "pages/login";
    }

    @GetMapping("/registro")
    public String verRegistro(Model model) {
        model.addAttribute("titulo", "Crear Cuenta | Idóneos Online");
        return "pages/registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre,
                                   @RequestParam String apellido,
                                   @RequestParam String correo,
                                   @RequestParam String contrasena,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (usuarioService.buscarPorCorreo(correo).isPresent()) {
                throw new IllegalArgumentException("Ya existe una cuenta con ese correo electrónico.");
            }

            Usuario nuevo = new Usuario(nombre, apellido, correo, contrasena, RolUsuario.Alumno);
            Usuario guardado = usuarioService.guardar(nuevo);

            // Crear el subtipo Alumno para integridad referencial
            alumnoRepository.save(new Alumno(guardado));

            redirectAttributes.addFlashAttribute("mensaje", "¡Cuenta creada exitosamente! Ya podés iniciar sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/registro";
        }
    }
}
