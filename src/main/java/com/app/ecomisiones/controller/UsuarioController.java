package com.app.ecomisiones.controller;

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

import com.app.ecomisiones.model.Usuario;
import com.app.ecomisiones.service.Usuario.UsuarioServiceImpl;

/**
 * Control del perfil y contraseñas de usuarios en Idóneos Online.
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/verPerfil")
    public String verPerfil(Model modelo, Authentication auth) {
        modelo.addAttribute("usuario", (Usuario) auth.getPrincipal());
        modelo.addAttribute("titulo", "Mi Perfil | Idóneos Online");
        return "pages/perfil/verPerfil";
    }

    @PostMapping("/modificar/{id}")
    public String modificarPerfil(@PathVariable int id,
                                  @RequestParam(name = "nombre") String nombre,
                                  @RequestParam(name = "apellido") String apellido,
                                  @RequestParam(name = "correo") String correo,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id).orElse(null);
            if (usuario == null) {
                throw new IllegalArgumentException("Error! El usuario ingresado no existe.");
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

    @GetMapping("/cambiarContrasena")
    public String verFormularioCambiarContrasena(Model modelo, Authentication auth) {
        modelo.addAttribute("usuario", (Usuario) auth.getPrincipal());
        modelo.addAttribute("titulo", "Cambiar Contraseña | Idóneos Online");
        return "pages/perfil/cambiarContrasena";
    }

    @PostMapping("/cambiarContrasena/{id}")
    public String cambiarContrasena(@PathVariable int id,
                                    @RequestParam(name = "actual") String actual,
                                    @RequestParam(name = "nueva") String nueva,
                                    @RequestParam(name = "nuevaRepetida") String nuevaRepetida,
                                    RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = usuarioService.buscarPorId(id).orElse(null);

            if (usuario == null) {
                throw new IllegalArgumentException("Error! El usuario ingresado no existe.");
            }

            if (!passwordEncoder.matches(actual, usuario.getContrasena())) {
                throw new BadCredentialsException("La contraseña actual ingresada es incorrecta.");
            }

            if (!nueva.equals(nuevaRepetida)) {
                throw new IllegalArgumentException("Las contraseñas nuevas no coinciden.");
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
