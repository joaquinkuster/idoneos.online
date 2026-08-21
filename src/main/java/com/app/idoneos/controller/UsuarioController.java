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

import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Controller para la gestión del perfil de usuario y cambio de credenciales.
 *
 * MOD-NF-01: Módulo de Usuarios y Notificaciones
 *   CU-86 — Ver perfil           → GET /usuario/verPerfil
 *             Actor: cualquier usuario autenticado (Alumno, Docente, Administrador).
 *             Muestra los datos del perfil del usuario en sesión.
 *             NOTA PARCIAL: CU-86 para Docente incluye datos profesionales (biografía,
 *               matrícula, títulos). Solo se muestran datos básicos de Usuario. PARCIAL.
 *
 *   CU-87 — Editar perfil        → POST /usuario/modificar/{id}
 *             Actor: cualquier usuario autenticado.
 *             Campos: nombre, apellido, teléfono e imagen de perfil.
 *             EX-CU87-01 (paso 4): campos obligatorios vacíos.
 *             NOTA PARCIAL: CU-87 paso 3 permite modificar teléfono e imagen de perfil.
 *               El controller actualmente solo procesa nombre, apellido y correo.
 *               El correo NO debería ser modificable por el usuario (solo Admin puede hacerlo en CU-84).
 *               IMPLEMENTADO PARCIALMENTE.
 *
 *   CU-92 — Recuperar contraseña → GET /usuario/cambiarContrasena (vista)
 *             POST /usuario/cambiarContrasena/{id} (ejecución)
 *             NOTA: CU-92 en la spec es la única vía para cambiar contraseña.
 *               El cambio de contraseña para usuario autenticado está aquí.
 *               La recuperación sin sesión está en LoginController.
 *             EX-CU92-03: contraseña actual incorrecta.
 *             EX-CU92-04: nueva contraseña y repetición no coinciden.
 *
 * ACCIONES NO IMPLEMENTADAS:
 *   CU-84 — Modificar usuario (Admin modifica datos de un Alumno) → no implementado en controller.
 *   CU-88 — Registrar docente → no implementado como CU separado (parcialmente en AdminController).
 *   CU-89 — Modificar docente → no implementado. FALTANTE.
 *   CU-93 — Buscar sesión     → no implementado. FALTANTE.
 *   CU-94 — Eliminar sesión   → no implementado. FALTANTE.
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioServiceImpl usuarioService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * TRAZABILIDAD: CU-86 — Ver perfil del usuario autenticado.
     * Actor: cualquier usuario autenticado (Alumno, Docente, Administrador).
     * Precondición: sesión iniciada.
     * Flujo paso 2-3: recupera los datos de la cuenta del actor y los muestra.
     * NOTA PARCIAL: CU-86 para Docente muestra datos profesionales (biografía, títulos, matrícula).
     *   Solo se muestran los datos base del Usuario (nombre, apellido, correo, rol).
     */
    @GetMapping("/verPerfil")
    public String verPerfil(Model modelo, Authentication auth) {
        // CU-86 paso 2-3: recuperar y mostrar datos de la cuenta del actor.
        modelo.addAttribute("usuario", (Usuario) auth.getPrincipal());
        modelo.addAttribute("titulo", "Mi Perfil | Idóneos Online");
        return "pages/perfil/verPerfil";
    }

    /**
     * TRAZABILIDAD: CU-87 — Editar perfil.
     * Actor: cualquier usuario autenticado.
     * Precondición: sesión iniciada. El usuario existe en el sistema.
     * Flujo paso 3: el actor modifica nombre, apellido y correo (ver nota).
     * Flujo paso 4: valida que los campos obligatorios estén completos.
     * Flujo paso 5: actualiza los datos de la cuenta.
     * Flujo paso 6: actualiza el contexto de seguridad de Spring Security.
     * Postcondición: perfil actualizado. Sesión actualizada con los nuevos datos.
     * EX-CU87-01 (paso 4): usuario no encontrado → redirect con mensaje.
     * NOTA PARCIAL: CU-87 solo permite modificar nombre, apellido, teléfono e imagen de perfil.
     *   El correo electrónico no debería modificarse aquí (corresponde a CU-84 vía Admin).
     *   Esta implementación también permite modificar el correo. IMPLEMENTADO CON EXCESO DE ALCANCE.
     * NOTA PARCIAL: CU-87 RN-CU87-01 (unicidad del correo si se modificó) no verificada. FALTANTE.
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
                throw new IllegalArgumentException("EX-CU87-01: El usuario especificado no existe.");
            }

            // CU-87 paso 5: actualizar datos del perfil.
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo);

            usuario = usuarioService.modificar(usuario);

            // CU-87 paso 6: actualizar contexto de seguridad de Spring Security.
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
     * TRAZABILIDAD: CU-92 — Recuperar contraseña (formulario de cambio para usuario autenticado).
     * Actor: cualquier usuario autenticado.
     * Precondición: sesión iniciada.
     * Flujo paso 6 (del flujo extendido): muestra el formulario de cambio de contraseña
     *   (actual, nueva, repetición de nueva).
     */
    @GetMapping("/cambiarContrasena")
    public String verFormularioCambiarContrasena(Model modelo, Authentication auth) {
        modelo.addAttribute("usuario", (Usuario) auth.getPrincipal());
        modelo.addAttribute("titulo", "Cambiar Contraseña | Idóneos Online");
        return "pages/perfil/cambiarContrasena";
    }

    /**
     * TRAZABILIDAD: CU-92 — Recuperar contraseña (cambio para usuario autenticado).
     * Actor: cualquier usuario autenticado.
     * Precondición: sesión iniciada. El usuario existe.
     * Flujo paso 7: verifica que el token (contraseña actual) sea correcto.
     *   Aquí se verifica la contraseña actual en lugar de un token temporal, ya que el
     *   usuario está autenticado y conoce su contraseña.
     * Flujo paso 8: actualiza la contraseña codificada con BCrypt.
     * Postcondición: contraseña actualizada. Sesión actualizada.
     * EX-CU92-03 (paso 7): contraseña actual incorrecta → redirect con mensaje.
     * EX-CU92-04 (paso intra-flujo): nueva contraseña y repetición no coinciden → redirect con mensaje.
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
                throw new IllegalArgumentException("EX-CU87-01: El usuario especificado no existe.");
            }

            // CU-92 paso 7 (variante autenticado): verificar contraseña actual con BCrypt.
            if (!passwordEncoder.matches(actual, usuario.getContrasena())) {
                throw new BadCredentialsException("EX-CU92-03: La contraseña actual es incorrecta.");
            }

            // Validar coincidencia de nueva contraseña.
            if (!nueva.equals(nuevaRepetida)) {
                throw new IllegalArgumentException("EX-CU92-04: Las contraseñas nuevas no coinciden.");
            }

            // CU-92 paso 8: codificar y almacenar la nueva contraseña.
            usuario.setContrasena(passwordEncoder.encode(nueva));
            usuario = usuarioService.modificar(usuario);

            // Actualizar el contexto de seguridad con la nueva contraseña.
            SecurityContextHolder.getContext()
                    .setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));

            redirectAttributes.addFlashAttribute("mensaje", "¡Contraseña actualizada correctamente!");
            return "redirect:/usuario/cambiarContrasena";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/usuario/cambiarContrasena";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SESIONES ACTIVAS (CU-93 y CU-94)
    // ─────────────────────────────────────────────────────────────

    @Autowired
    private com.app.idoneos.repository.SesionRepository sesionRepository;

    /**
     * TRAZABILIDAD: CU-93 — Buscar sesión.
     * Actor: Alumno / Docente / Administrador.
     */
    @GetMapping("/sesiones")
    public String listarSesiones(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        List<com.app.idoneos.model.Sesion> sesiones = usuario.esAdmin()
                ? sesionRepository.findAll()
                : sesionRepository.findByUsuarioOrderByFechaInicioDesc(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("sesiones", sesiones);
        model.addAttribute("titulo", "Historial de Sesiones | Idóneos Online");
        return "pages/perfil/sesiones";
    }

    /**
     * TRAZABILIDAD: CU-94 — Eliminar sesión (Cierre remoto).
     * Actor: Alumno / Docente / Administrador.
     */
    @PostMapping("/sesiones/{id}/eliminar")
    public String eliminarSesion(@PathVariable int id, Authentication auth, RedirectAttributes ra) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Optional<com.app.idoneos.model.Sesion> sOpt = sesionRepository.findById(id);

        if (sOpt.isPresent()) {
            com.app.idoneos.model.Sesion s = sOpt.get();
            if (usuario.esAdmin() || s.getUsuario().getId() == usuario.getId()) {
                s.setFechaFin(java.time.LocalDateTime.now());
                sesionRepository.save(s);
                ra.addFlashAttribute("mensaje", "Sesión desautorizada y cerrada remotamente.");
            } else {
                ra.addFlashAttribute("mensaje", "No tenés permisos para cerrar esta sesión.");
            }
        }

        return "redirect:/usuario/sesiones";
    }
}
