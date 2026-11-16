package com.app.idoneos.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.idoneos.model.Sesion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.sesion.SesionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filtro de la sesión del usuario. Comprueba en cada pedido que el token guardado en la cookie del navegador exista en la base
 * y no haya vencido. Si la sesión del servidor se perdió pero el token sigue vigente, vuelve a autenticar al usuario con él.
 */
public class SesionTokenFilter extends OncePerRequestFilter {

    /** Nombre de la cookie con el token de la sesión. */
    public static final String COOKIE_TOKEN = "TOKEN_SESION_IDONEOS";

    /** Duración de la cookie de una sesión recordada, en segundos (30 días). */
    public static final int SEGUNDOS_RECORDADA = 30 * 24 * 60 * 60;

    private static final String[] RUTAS_ESTATICAS = { "/css/", "/js/", "/img/", "/webjars/", "/uploads/" };

    private final SesionService sesionService;

    private final SecurityContextRepository repositorio = new HttpSessionSecurityContextRepository();

    public SesionTokenFilter(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    // ----------------------------------------------------------------------
    // Filtro
    // ----------------------------------------------------------------------

    /** No revisa los archivos estáticos, que no dependen del usuario. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getServletPath();
        for (String estatica : RUTAS_ESTATICAS) {
            if (ruta.startsWith(estatica)) {
                return true;
            }
        }
        return false;
    }

    /** Revisa el token del pedido y deja al usuario autenticado, o sin sesión, según corresponda. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        String token = leerToken(request);
        Authentication actual = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = actual != null && actual.isAuthenticated() && !(actual instanceof AnonymousAuthenticationToken);

        if (token == null) {
            // Una sesión sin token no está registrada y no puede cerrarse desde "Mis sesiones": se descarta
            if (autenticado) {
                descartarSesion(request);
            }
            cadena.doFilter(request, response);
            return;
        }

        Optional<Sesion> registrada = sesionService.buscarVigentePorToken(token);
        Usuario dueno = registrada.map(Sesion::getUsuario).orElse(null);
        boolean valida = dueno != null && !dueno.esInactivo() && dueno.isEnabled()
                && (!autenticado || (actual.getPrincipal() instanceof Usuario usuario && usuario.getIdUsuario() == dueno.getIdUsuario()));
        if (!valida) {
            borrarCookie(response);
            if (autenticado) {
                descartarSesion(request);
            }
            cadena.doFilter(request, response);
            return;
        }

        if (!autenticado) {
            // La sesión del servidor venció, pero el token sigue vigente: se vuelve a autenticar con él
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(dueno, null, dueno.getAuthorities()));
            SecurityContextHolder.setContext(contexto);
            repositorio.saveContext(contexto, request, response);
        }
        sesionService.renovar(registrada.get());
        cadena.doFilter(request, response);
    }

    // ----------------------------------------------------------------------
    // Cookie
    // ----------------------------------------------------------------------

    /**
     * Lee el token de la cookie del pedido.
     *
     * @param request El pedido recibido.
     * @return El token, o {@code null} si no hay cookie.
     */
    public static String leerToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_TOKEN.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Guarda el token de una sesión en la cookie del navegador.
     *
     * @param request El pedido recibido.
     * @param response La respuesta donde se agrega la cookie.
     * @param token El token de la sesión.
     * @param recordar Si es {@code true} la cookie dura 30 días, y si no se borra al cerrar el navegador.
     */
    public static void guardarCookie(HttpServletRequest request, HttpServletResponse response, String token, boolean recordar) {
        Cookie cookie = new Cookie(COOKIE_TOKEN, token);
        cookie.setPath(rutaDeCookie(request));
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        if (recordar) {
            cookie.setMaxAge(SEGUNDOS_RECORDADA);
        }
        response.addCookie(cookie);
    }

    /**
     * Borra la cookie del token del navegador.
     *
     * @param response La respuesta donde se agrega la cookie vencida.
     */
    public static void borrarCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_TOKEN, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private static String rutaDeCookie(HttpServletRequest request) {
        return request.getContextPath().isEmpty() ? "/" : request.getContextPath();
    }

    /** Descarta la sesión del servidor del pedido. */
    private void descartarSesion(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
    }
}
