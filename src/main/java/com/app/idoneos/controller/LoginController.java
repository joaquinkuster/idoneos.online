package com.app.idoneos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.config.AccesoComponent;
import com.app.idoneos.service.sesion.SesionService;
import com.app.idoneos.service.usuario.UsuarioService;

/** Controlador del inicio de sesión, el registro y la recuperación de la contraseña. */
@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SesionService sesionService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /login: Muestra el formulario de inicio de sesión. */
    @GetMapping("/login")
    public String ver(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout, Model model, Authentication authentication) {
        if (haySesion(authentication)) {
            return "redirect:/inicio";
        }
        if ("noValidado".equals(error)) {
            model.addAttribute("errorLogin", "Tu correo electrónico todavía no fue validado. Revisá tu casilla y usá el enlace que te enviamos. "
                    + "Si no lo encontrás, pedí uno nuevo desde «¿Olvidaste tu contraseña?».");
        } else if ("googleCorreoRegistrado".equals(error)) {
            model.addAttribute("error", "Error! Ese correo ya tiene una cuenta. Iniciá sesión con tu correo y tu contraseña. "
                    + "Si no la recordás, usá «¿Olvidaste tu contraseña?».");
        } else if ("googleNoDisponible".equals(error)) {
            model.addAttribute("error", "Error! No se pudo ingresar con Google. La cuenta no se encuentra activa.");
        } else if ("google".equals(error)) {
            model.addAttribute("error", "Error! No se pudo ingresar con Google. Intentá nuevamente.");
        } else if ("limiteSesiones".equals(error)) {
            int limite = sesionService.obtenerLimite();
            model.addAttribute("error", "Error! Alcanzaste el límite de " + limite + (limite == 1 ? " sesión activa" : " sesiones activas")
                    + " en esta cuenta. Cerrá una desde otro dispositivo o esperá a que venza.");
        } else if (error != null) {
            model.addAttribute("errorLogin", "Correo o contraseña incorrectos, o la cuenta no está habilitada.");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente. ¡Hasta pronto!");
        }
        model.addAttribute("titulo", "Iniciar sesión | Idóneos Online");
        return "pages/login";
    }

    /** GET /registro: Muestra el formulario para crear una cuenta de alumno. */
    @GetMapping("/registro")
    public String registro(Model model, Authentication authentication) {
        if (haySesion(authentication)) {
            return "redirect:/inicio";
        }
        model.addAttribute("titulo", "Crear cuenta | Idóneos Online");
        return "pages/auth/registro";
    }

    /** GET /registro/google: Pide el DNI a quien ingresó con Google y todavía no tiene cuenta. */
    @GetMapping("/registro/google")
    public String registroGoogle(Model model, jakarta.servlet.http.HttpSession sesion) {
        AccesoComponent.DatosGoogle datos = sesion.getAttribute(AccesoComponent.GOOGLE_PENDIENTE) instanceof AccesoComponent.DatosGoogle pendiente
                ? pendiente : null;
        if (datos == null || datos.vencido()) {
            sesion.removeAttribute(AccesoComponent.GOOGLE_PENDIENTE);
            return "redirect:/login?error=google";
        }
        model.addAttribute("correoGoogle", datos.correo());
        model.addAttribute("nombreGoogle", datos.nombre());
        model.addAttribute("apellidoGoogle", datos.apellido());
        model.addAttribute("titulo", "Completar cuenta | Idóneos Online");
        return "pages/auth/google";
    }

    /** GET /usuario/validarCorreo/{token}: Valida el correo de una cuenta con el enlace recibido y muestra el resultado. */
    @GetMapping("/usuario/validarCorreo/{token}")
    public String validarCorreo(@PathVariable("token") String token, Model model) {
        try {
            usuarioService.validarCorreo(token);
            return resultado(model, true, "Correo validado",
                    "Tu correo electrónico fue validado. Ya podés iniciar sesión con tu cuenta.");
        } catch (IllegalArgumentException e) {
            return resultado(model, false, "No se pudo validar el correo", sinPrefijo(e.getMessage()));
        }
    }

    /** GET /recuperar/{token}: Muestra el formulario para crear una contraseña nueva con el enlace recibido. */
    @GetMapping("/recuperar/{token}")
    public String recuperar(@PathVariable("token") String token, Model model) {
        try {
            usuarioService.buscarPorToken(token);
            model.addAttribute("token", token);
            model.addAttribute("titulo", "Nueva contraseña | Idóneos Online");
            return "pages/auth/restablecer";
        } catch (IllegalArgumentException e) {
            return resultado(model, false, "Enlace no disponible", sinPrefijo(e.getMessage()));
        }
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Indica si hay un usuario con la sesión iniciada. */
    private boolean haySesion(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /** Prepara la pantalla corta que informa el resultado de un enlace del correo. */
    private String resultado(Model model, boolean exito, String titulo, String mensaje) {
        model.addAttribute("exito", exito);
        model.addAttribute("tituloResultado", titulo);
        model.addAttribute("mensajeResultado", mensaje);
        model.addAttribute("titulo", titulo + " | Idóneos Online");
        return "pages/auth/resultado";
    }

    /** Quita el prefijo "Error! " de un mensaje, que ya no hace falta en una pantalla propia. */
    private String sinPrefijo(String mensaje) {
        return mensaje == null ? "" : mensaje.replaceFirst("^Error!\\s*", "");
    }
}
