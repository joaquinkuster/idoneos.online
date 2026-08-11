package com.app.idoneos.controller;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.RolUsuario;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.service.Usuario.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller para la autenticación, registro y recuperación de contraseña de usuarios (CU-75, CU-84, CU-85, CU-86).
 */
@Controller
public class LoginController {

    @Autowired private UsuarioServiceImpl usuarioService;
    @Autowired private AlumnoRepository alumnoRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * CU-84 — Iniciar sesión / CU-85 — Cerrar sesión.
     */
    @GetMapping("/login")
    public String verLogin(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("mensaje", "CU-84 Excepción paso 4: La contraseña o el correo ingresado es inválido.");
        } else if (logout != null) {
            model.addAttribute("mensaje", "CU-85: Has cerrado sesión correctamente.");
        }
        model.addAttribute("titulo", "Iniciar Sesión | Idóneos Online");
        return "pages/login";
    }

    /**
     * CU-75 — Formulario de auto-registro de alumno.
     */
    @GetMapping("/registro")
    public String verRegistro(Model model) {
        model.addAttribute("titulo", "Crear Cuenta | Idóneos Online");
        return "pages/registro";
    }

    /**
     * CU-75 — Registrarse (Auto-registro de alumno).
     * Reglas de negocio:
     * - Campos obligatorios (nombre, apellido, correo, contraseña).
     * - Unicidad de correo electrónico (Excepción CU-75, paso 5).
     * - Creación de la entidad Alumno vinculada.
     */
    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre,
                                   @RequestParam String apellido,
                                   @RequestParam String correo,
                                   @RequestParam String contrasena,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (usuarioService.buscarPorCorreo(correo).isPresent()) {
                throw new IllegalArgumentException("CU-75 Excepción paso 5: Ya existe una cuenta con el correo " + correo);
            }

            Usuario nuevo = new Usuario(nombre, apellido, correo, contrasena, RolUsuario.Alumno);
            Usuario guardado = usuarioService.guardar(nuevo);
            alumnoRepository.save(new Alumno(guardado));

            redirectAttributes.addFlashAttribute("mensaje", "¡Cuenta creada exitosamente! Ya podés iniciar sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/registro";
        }
    }

    /**
     * CU-86 — Formulario de solicitud de recuperación de contraseña.
     */
    @GetMapping("/recuperar-contrasena")
    public String verRecuperarContrasena(Model model) {
        model.addAttribute("titulo", "Recuperar Contraseña | Idóneos Online");
        return "pages/recuperar-contrasena";
    }

    /**
     * CU-86 — Solicitar token de recuperación de contraseña por correo.
     */
    @PostMapping("/recuperar-contrasena")
    public String solicitarRecuperacion(@RequestParam String correo, RedirectAttributes ra) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCorreo(correo);
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            String token = UUID.randomUUID().toString();
            u.setTokenRecuperacion(token);
            u.setExpiracionToken(LocalDateTime.now().plusHours(2));
            usuarioService.modificar(u);
            ra.addFlashAttribute("mensaje", "Se ha enviado un enlace de recuperación a tu correo electrónico.");
            ra.addFlashAttribute("tokenDemo", token);
        } else {
            ra.addFlashAttribute("mensaje", "Si el correo existe en nuestro sistema, recibirás un enlace de recuperación.");
        }
        return "redirect:/recuperar-contrasena";
    }

    /**
     * CU-86 — Validar token de restablecimiento de contraseña.
     */
    @GetMapping("/resetear-contrasena")
    public String verResetearContrasena(@RequestParam String token, Model model, RedirectAttributes ra) {
        Optional<Usuario> usuarioOpt = usuarioService.obtenerTodo().stream()
                .filter(u -> token.equals(u.getTokenRecuperacion()))
                .findFirst();

        if (usuarioOpt.isEmpty() || usuarioOpt.get().getExpiracionToken() == null
                || LocalDateTime.now().isAfter(usuarioOpt.get().getExpiracionToken())) {
            ra.addFlashAttribute("mensaje", "CU-86 Excepción: El enlace de recuperación es inválido o ha expirado.");
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        model.addAttribute("titulo", "Restablecer Contraseña | Idóneos Online");
        return "pages/resetear-contrasena";
    }

    /**
     * CU-86 — Restablecer nueva contraseña.
     */
    @PostMapping("/resetear-contrasena")
    public String procesarResetearContrasena(@RequestParam String token,
                                             @RequestParam String nuevaContrasena,
                                             RedirectAttributes ra) {
        Optional<Usuario> usuarioOpt = usuarioService.obtenerTodo().stream()
                .filter(u -> token.equals(u.getTokenRecuperacion()))
                .findFirst();

        if (usuarioOpt.isEmpty() || usuarioOpt.get().getExpiracionToken() == null
                || LocalDateTime.now().isAfter(usuarioOpt.get().getExpiracionToken())) {
            ra.addFlashAttribute("mensaje", "CU-86 Excepción: El token de recuperación ha expirado.");
            return "redirect:/login";
        }

        Usuario u = usuarioOpt.get();
        u.setContrasena(passwordEncoder.encode(nuevaContrasena));
        u.setTokenRecuperacion(null);
        u.setExpiracionToken(null);
        usuarioService.modificar(u);

        ra.addFlashAttribute("mensaje", "¡Contraseña restablecida exitosamente! Ya podés iniciar sesión.");
        return "redirect:/login";
    }
}
