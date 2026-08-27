package com.app.idoneos.controller.modulo_usuarios;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_usuarios.*;

import com.app.idoneos.service.modulo_usuarios.*;
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
 * TRAZABILIDAD — Controller para la autenticación, auto-registro y recuperación
 * de contraseña.
 *
 * MOD-NF-01: Módulo de Usuarios y Notificaciones
 * CU-81 — Registrarse (auto-registro) → GET /registro + POST /registro
 * Actor: Alumno (interesado sin cuenta).
 * Flujo: ingresar nombre/apellido/correo/DNI/contraseña → validar unicidad →
 * registrar con rol Alumno → enviar enlace de validación.
 * EX-CU81-01 (paso 4): correo ya registrado.
 * NOTA PARCIAL: CU-81 paso 6 requiere envío de enlace de validación por email.
 * No implementado; la cuenta queda activa de inmediato. IMPLEMENTADO
 * PARCIALMENTE.
 * NOTA PARCIAL: CU-81 paso 2 solicita DNI. El modelo no tiene campo DNI en
 * Usuario.
 * IMPLEMENTADO PARCIALMENTE.
 *
 * CU-90 — Iniciar sesión → GET /login (vista; POST gestionado por Spring
 * Security)
 * Actor: Alumno, Docente, Administrador.
 * EX-CU90-01 (paso 4): credenciales inválidas.
 * NOTA PARCIAL: CU-90 paso 5 valida límite de sesiones concurrentes. No
 * implementado.
 * NOTA PARCIAL: CU-90 paso 6 registra sesión con IP y dispositivo (tabla
 * SesionUsuario).
 * No implementado. IMPLEMENTADO PARCIALMENTE.
 *
 * CU-91 — Cerrar sesión → GET /login?logout (gestionado por Spring Security)
 * NOTA PARCIAL: CU-91 paso 2 registra la fecha de fin de sesión. No
 * implementado.
 *
 * CU-92 — Recuperar contraseña → GET/POST /recuperar-contrasena
 * GET/POST /resetear-contrasena?token=...
 * Flujo: ingresar correo → generar token UUID (vigencia 2h) →
 * enviar por email → validar token → restablecer contraseña.
 * EX-CU92-01 (paso 4): correo no registrado → mensaje genérico.
 * EX-CU92-02 (paso 7): token expirado → redirect con mensaje.
 * NOTA PARCIAL: el envío real del correo no está implementado.
 * En demo: el token se expone como flashAttribute. ELIMINAR en producción.
 *
 * CU-93 — Buscar sesión → NO IMPLEMENTADO. FALTANTE.
 * CU-94 — Eliminar sesión → NO IMPLEMENTADO. FALTANTE.
 */
@Controller
public class LoginController {

    @Autowired
    private UsuarioServiceImpl usuarioService;
    @Autowired
    private AlumnoRepository alumnoRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * TRAZABILIDAD: CU-90 — Iniciar sesión (vista del formulario de login).
     * TRAZABILIDAD: CU-91 — Cerrar sesión (confirmación).
     * Actor: cualquier usuario (con o sin sesión).
     * Flujo paso 2 (CU-90): muestra el formulario de login (correo + contraseña).
     * EX-CU90-01 (paso 4): Spring Security redirige a /login?error cuando las
     * credenciales son inválidas.
     * CU-91 paso 3: Spring Security cierra la sesión y redirige a /login?logout.
     * NOTA PARCIAL: CU-90 paso 6 debería registrar la sesión con IP y dispositivo.
     * No implementado.
     */
    @GetMapping("/login")
    public String verLogin(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            // CU-90 EX-CU90-01: credenciales inválidas.
            model.addAttribute("mensaje", "EX-CU90-01: La contraseña o el correo electrónico ingresado es inválido.");
        } else if (logout != null) {
            // CU-91: cierre de sesión confirmado.
            model.addAttribute("mensaje", "CU-91: Has cerrado sesión correctamente.");
        }
        model.addAttribute("titulo", "Iniciar Sesión | Idóneos Online");
        return "pages/login";
    }

    /**
     * TRAZABILIDAD: CU-81 — Registrarse (formulario GET).
     * Actor: interesado sin cuenta.
     * Flujo paso 2: muestra el formulario de registro con campos nombre, apellido,
     * correo, contraseña.
     * NOTA PARCIAL: CU-81 paso 2 solicita también DNI. El modelo no tiene campo DNI
     * en Usuario.
     */
    @GetMapping("/registro")
    public String verRegistro(Model model) {
        model.addAttribute("titulo", "Crear Cuenta | Idóneos Online");
        return "pages/registro";
    }

    /**
     * TRAZABILIDAD: CU-81 — Registrarse (POST).
     * Actor: interesado sin cuenta.
     * Precondición: no posee cuenta en el sistema.
     * Flujo paso 4: valida campos obligatorios y unicidad del correo electrónico.
     * Flujo paso 5: registra la cuenta con rol Alumno (sin validación de correo en
     * esta versión).
     * Flujo paso 6: en prod. envía enlace de validación. En demo: la cuenta queda
     * activa directamente.
     * Postcondición: cuenta registrada con rol Alumno.
     * EX-CU81-01 (paso 4): correo ya registrado → redirect a /registro con mensaje.
     * NOTA PARCIAL: CU-81 pasos 6-8 (validación por enlace de email) no están
     * implementados.
     */
    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String correo,
            @RequestParam String contrasena,
            RedirectAttributes redirectAttributes) {
        try {
            // CU-81 paso 4: verificar unicidad del correo electrónico.
            if (usuarioService.buscarPorCorreo(correo).isPresent()) {
                throw new IllegalArgumentException("EX-CU81-01: Ya existe una cuenta con el correo " + correo
                        + ". Iniciá sesión o recuperá tu contraseña.");
            }

            // CU-81 paso 5: registrar Usuario con rol Alumno y crear entidad Alumno.
            Usuario nuevo = new Usuario(nombre, apellido, correo, contrasena, RolUsuario.Alumno);
            Usuario guardado = usuarioService.guardar(nuevo);
            alumnoRepository.save(new Alumno(guardado));

            // CU-81 paso 9 (sin pasos 6-8 implementados): informa éxito y habilita login.
            redirectAttributes.addFlashAttribute("mensaje", "¡Cuenta creada exitosamente! Ya podés iniciar sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/registro";
        }
    }

    /**
     * TRAZABILIDAD: CU-92 — Recuperar contraseña (formulario de solicitud).
     * Actor: usuario no autenticado con cuenta existente.
     * Flujo paso 2: muestra el formulario para ingresar el correo electrónico.
     */
    @GetMapping("/recuperar-contrasena")
    public String verRecuperarContrasena(Model model) {
        model.addAttribute("titulo", "Recuperar Contraseña | Idóneos Online");
        return "pages/recuperar-contrasena";
    }

    /**
     * TRAZABILIDAD: CU-92 — Recuperar contraseña (solicitar token).
     * Actor: usuario no autenticado.
     * Flujo paso 4: verifica que el correo esté registrado.
     * Flujo paso 5: genera token UUID con vigencia de 2 horas y lo almacena en el
     * usuario.
     * Postcondición: en producción → correo enviado con enlace. En demo → token
     * visible.
     * EX-CU92-01 (paso 4): correo no registrado → mensaje genérico (no revela
     * existencia de la cuenta).
     * NOTA: en producción ELIMINAR la exposición del tokenDemo como flashAttribute.
     */
    @PostMapping("/recuperar-contrasena")
    public String solicitarRecuperacion(@RequestParam String correo, RedirectAttributes ra) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorCorreo(correo);
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            // CU-92 paso 5: genera token UUID con vigencia de 2 horas.
            String token = UUID.randomUUID().toString();
            u.setTokenRecuperacion(token);
            u.setExpiracionToken(LocalDateTime.now().plusHours(2));
            usuarioService.modificar(u);
            ra.addFlashAttribute("mensaje", "Se ha enviado un enlace de recuperación a tu correo electrónico.");
            // DEMO: exponer token en la vista. ELIMINAR en producción.
            ra.addFlashAttribute("tokenDemo", token);
        } else {
            // EX-CU92-01: correo no registrado — mensaje genérico.
            ra.addFlashAttribute("mensaje",
                    "Si el correo existe en nuestro sistema, recibirás un enlace de recuperación.");
        }
        return "redirect:/recuperar-contrasena";
    }

    /**
     * TRAZABILIDAD: CU-92 — Recuperar contraseña (validar token y mostrar
     * formulario).
     * Actor: usuario no autenticado con token válido recibido por email.
     * Flujo paso 7: valida que el token exista y no haya expirado (vigencia 2
     * horas).
     * EX-CU92-02 (paso 7): token inválido o expirado → redirect a /login con
     * mensaje.
     */
    @GetMapping("/resetear-contrasena")
    public String verResetearContrasena(@RequestParam String token, Model model, RedirectAttributes ra) {
        Optional<Usuario> usuarioOpt = usuarioService.obtenerTodo().stream()
                .filter(u -> token.equals(u.getTokenRecuperacion()))
                .findFirst();

        // CU-92 paso 7: verificar existencia y vigencia del token.
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getExpiracionToken() == null
                || LocalDateTime.now().isAfter(usuarioOpt.get().getExpiracionToken())) {
            ra.addFlashAttribute("mensaje",
                    "EX-CU92-02: El enlace de recuperación es inválido o ha expirado. Solicitá uno nuevo.");
            return "redirect:/login";
        }

        model.addAttribute("token", token);
        model.addAttribute("titulo", "Restablecer Contraseña | Idóneos Online");
        return "pages/resetear-contrasena";
    }

    /**
     * TRAZABILIDAD: CU-92 — Recuperar contraseña (restablecer nueva contraseña).
     * Actor: usuario no autenticado con token válido.
     * Flujo paso 8: revalida el token, codifica la nueva contraseña con BCrypt,
     * limpia el token.
     * Postcondición: contraseña actualizada. Token eliminado. Redirige al login.
     * EX-CU92-02: token expirado entre los pasos 7 y 8 → redirect con mensaje.
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
            ra.addFlashAttribute("mensaje", "EX-CU92-02: El token de recuperación ha expirado. Solicitá uno nuevo.");
            return "redirect:/login";
        }

        // CU-92 paso 8: codificar la nueva contraseña y eliminar el token.
        Usuario u = usuarioOpt.get();
        u.setContrasena(passwordEncoder.encode(nuevaContrasena));
        u.setTokenRecuperacion(null);
        u.setExpiracionToken(null);
        usuarioService.modificar(u);

        ra.addFlashAttribute("mensaje", "¡Contraseña restablecida exitosamente! Ya podés iniciar sesión.");
        return "redirect:/login";
    }
}
