package com.app.idoneos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.Usuario.UsuarioServiceImpl;

/**
 * Controller para la gestión del perfil de usuario y cambio de credenciales.
 * Mapea la trazabilidad funcional de CU-80 (Ver perfil), CU-81 (Editar perfil) y CU-86 (Cambiar contraseña).
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * CU-80 — Ver perfil del usuario autenticado.
     */
    @GetMapping("/verPerfil")
    public String verPerfil(Model modelo, Authentication auth) {
        modelo.addAttribute("usuario", (Usuario) auth.getPrincipal());
        modelo.addAttribute("titulo", "Mi Perfil | Idóneos Online");
        return "pages/perfil/verPerfil";
    }

    /**
     * CU-81 — Editar perfil del usuario.
     * Reglas de negocio:
     * - Campos obligatorios: nombre, apellido, correo.
     * - Actualización de contexto de seguridad tras modificación.
     */
    @PostMapping("/modificar/{id}")
    public String modificarPerfil(@PathVariable int id,
                                  @RequestParam(name = "nombre") String nombre,
                                  @RequestParam(name = "apellido") String apellido,
                                  @RequestParam(name = "correo") String correo,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
            if (usuario == null) {
                throw new IllegalArgumentException("CU-81 Excepción: El usuario especificado no existe.");
            }

            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo);

            usuario = usuarioService.modificar(usuario);

            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));

            redirectAttributes.addFlashAttribute("mensaje", "¡Perfil actualizado correctamente!");
            return "redirect:/usuario/verPerfil";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/usuario/verPerfil";
        }
    }

    /**
     * CU-86 — Ver formulario de cambio de contraseña.
     */
    @GetMapping("/cambiarContrasena")
    public String verFormularioCambiarContrasena(Model modelo, Authentication auth) {
        modelo.addAttribute("usuario", (Usuario) auth.getPrincipal());
        modelo.addAttribute("titulo", "Cambiar Contraseña | Idóneos Online");
        return "pages/perfil/cambiarContrasena";
    }

    /**
     * CU-86 — Cambiar contraseña.
     * Reglas de negocio:
     * - Coincidencia de la contraseña actual introducida.
     * - Coincidencia entre la nueva contraseña y su repetición.
     */
    @PostMapping("/cambiarContrasena/{id}")
    public String cambiarContrasena(@PathVariable int id,
                                    @RequestParam(name = "actual") String actual,
                                    @RequestParam(name = "nueva") String nueva,
                                    @RequestParam(name = "nuevaRepetida") String nuevaRepetida,
                                    RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = usuarioService.buscarPorId(id).orElse(null);

            if (usuario == null) {
                throw new IllegalArgumentException("CU-86 Excepción: El usuario especificado no existe.");
            }

            if (!passwordEncoder.matches(actual, usuario.getContrasena())) {
                throw new BadCredentialsException("CU-86 Excepción paso 4: La contraseña actual es incorrecta.");
            }

            if (!nueva.equals(nuevaRepetida)) {
                throw new IllegalArgumentException("CU-86 Excepción paso 5: Las contraseñas nuevas no coinciden.");
            }

            usuario.setContrasena(passwordEncoder.encode(nueva));
            usuario = usuarioService.modificar(usuario);

            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));

            redirectAttributes.addFlashAttribute("mensaje", "¡Contraseña actualizada correctamente!");
            return "redirect:/usuario/cambiarContrasena";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/usuario/cambiarContrasena";
        }
    }
}
