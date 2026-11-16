package com.app.idoneos.config;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import com.app.idoneos.controller.advice.ContextoSesionAdvice;
import com.app.idoneos.model.Sesion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.sesion.SesionService;
import com.app.idoneos.service.usuario.CorreoYaRegistradoException;
import com.app.idoneos.service.usuario.UsuarioService;
import com.app.idoneos.util.DispositivoUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Componente de ingreso al sistema. Registra la sesión del usuario que ingresó con su contraseña o con Google. */
@Component
public class AccesoComponent {

    /** Nombre del atributo de sesión con los datos de Google de quien todavía no tiene cuenta. */
    public static final String GOOGLE_PENDIENTE = "googlePendiente";

    /** Minutos que se espera a que complete sus datos quien ingresó con Google y todavía no tiene cuenta. */
    public static final int MINUTOS_GOOGLE_PENDIENTE = 15;

    /** Datos que Google informa de quien todavía no tiene cuenta, hasta que completa su DNI. */
    public record DatosGoogle(String googleId, String correo, String nombre, String apellido, long creado) implements Serializable {

        /** Indica si pasó demasiado tiempo desde que se autorizó el ingreso con Google. */
        public boolean vencido() {
            return System.currentTimeMillis() - creado > MINUTOS_GOOGLE_PENDIENTE * 60_000L;
        }
    }

    @Autowired
    private SesionService sesionService;

    // Se pide al usarse: el servicio de usuarios necesita el codificador de contraseñas de SecurityConfig, que a su vez usa este componente
    @Autowired
    @Lazy
    private UsuarioService usuarioService;

    private final SecurityContextRepository repositorio = new HttpSessionSecurityContextRepository();

    // ----------------------------------------------------------------------
    // Ingreso con contraseña
    // ----------------------------------------------------------------------

    /** Registra la sesión de quien ingresó con su correo y su contraseña y lo lleva a su pantalla de inicio. */
    public void alIngresar(HttpServletRequest request, HttpServletResponse response, Authentication autenticacion) throws IOException {
        Usuario usuario = (Usuario) autenticacion.getPrincipal();
        boolean recordar = "true".equals(request.getParameter("recordarme"));
        try {
            String destino = iniciarSesion(request, response, usuario, recordar);
            response.sendRedirect(request.getContextPath() + destino);
        } catch (IllegalStateException e) {
            new SecurityContextLogoutHandler().logout(request, response, autenticacion);
            response.sendRedirect(request.getContextPath() + "/login?error=limiteSesiones");
        }
    }

    // ----------------------------------------------------------------------
    // Ingreso con Google
    // ----------------------------------------------------------------------

    /**
     * Resuelve el ingreso con Google. Si ya creó su cuenta con Google, registra la sesión. Si el correo tiene una cuenta propia, no
     * lo deja ingresar por esta vía. Si no tiene cuenta, guarda sus datos y lo lleva a completar el DNI, sin crear nada hasta entonces.
     */
    public void alIngresarConGoogle(HttpServletRequest request, HttpServletResponse response, Authentication autenticacion)
            throws IOException {
        OAuth2User google = (OAuth2User) autenticacion.getPrincipal();
        String googleId = google.getName();
        String correo = google.getAttribute("email");
        boolean verificado = Boolean.TRUE.equals(google.getAttribute("email_verified"));
        try {
            Optional<Usuario> existente = usuarioService.ingresarConGoogle(googleId, correo, verificado);
            if (existente.isPresent()) {
                autenticar(request, response, existente.get());
                try {
                    response.sendRedirect(request.getContextPath() + iniciarSesion(request, response, existente.get(), false));
                } catch (IllegalStateException e) {
                    new SecurityContextLogoutHandler().logout(request, response, autenticacion);
                    response.sendRedirect(request.getContextPath() + "/login?error=limiteSesiones");
                }
                return;
            }
            // Todavía no tiene cuenta: no hay sesión iniciada hasta que complete sus datos
            String nombre = google.getAttribute("given_name");
            String apellido = google.getAttribute("family_name");
            request.getSession().setAttribute(GOOGLE_PENDIENTE, new DatosGoogle(googleId, correo.trim().toLowerCase(),
                    nombre == null ? "" : nombre, apellido == null ? "" : apellido, System.currentTimeMillis()));
            descartarAutenticacion(request);
            response.sendRedirect(request.getContextPath() + "/registro/google");
        } catch (CorreoYaRegistradoException e) {
            new SecurityContextLogoutHandler().logout(request, response, autenticacion);
            response.sendRedirect(request.getContextPath() + "/login?error=googleCorreoRegistrado");
        } catch (IllegalArgumentException e) {
            new SecurityContextLogoutHandler().logout(request, response, autenticacion);
            response.sendRedirect(request.getContextPath() + "/login?error=googleNoDisponible");
        }
    }

    // ----------------------------------------------------------------------
    // Sesión
    // ----------------------------------------------------------------------

    /**
     * Deja al usuario autenticado en el pedido, para quien ingresó sin pasar por el formulario de inicio de sesión.
     *
     * @param request El pedido recibido.
     * @param response La respuesta del pedido.
     * @param usuario El usuario que ingresa.
     */
    public void autenticar(HttpServletRequest request, HttpServletResponse response, Usuario usuario) {
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities()));
        SecurityContextHolder.setContext(contexto);
        if (request.getSession(false) != null) {
            request.changeSessionId(); // una sesión nueva evita que se reutilice una anterior
        }
        repositorio.saveContext(contexto, request, response);
    }

    /**
     * Registra la sesión en la base, guarda su token en la cookie y deja el mensaje de bienvenida.
     *
     * @param request El pedido recibido.
     * @param response La respuesta donde se guarda la cookie.
     * @param usuario El usuario que ingresa.
     * @param recordar Indica si eligió "Recordarme".
     * @return La dirección a la que se lo lleva según su rol.
     * @throws IllegalStateException Si la cuenta ya tiene el máximo de sesiones simultáneas.
     */
    public String iniciarSesion(HttpServletRequest request, HttpServletResponse response, Usuario usuario, boolean recordar) {
        Sesion sesion = sesionService.iniciar(usuario, recordar, DispositivoUtil.obtenerIp(request),
                DispositivoUtil.describir(request.getHeader("User-Agent")));
        SesionTokenFilter.guardarCookie(request, response, sesion.getToken(), recordar);
        request.getSession().setAttribute(ContextoSesionAdvice.MENSAJE_DE_SESION,
                "Sesión iniciada correctamente. ¡Bienvenido/a, " + usuario.getNombre() + "!");
        // El administrador y el docente entran a sus cursos, el alumno a sus inscripciones y los demás roles al inicio
        return usuario.esAdministradorActivo() || usuario.esDocenteActivo() ? "/curso/buscar"
                : usuario.esAlumnoActivo() ? "/inscripcion/buscar" : "/inicio";
    }

    /** Quita del pedido y de la sesión del servidor la autenticación, sin cerrar la sesión. */
    public void descartarAutenticacion(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        }
    }
}
