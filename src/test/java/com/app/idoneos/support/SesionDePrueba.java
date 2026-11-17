package com.app.idoneos.support;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.app.idoneos.config.SesionTokenFilter;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.sesion.SesionService;

import jakarta.servlet.http.Cookie;

/**
 * Utilidad de pruebas que registra la sesión del usuario autenticado con {@code @WithUserDetails}.
 * El filtro de sesión descarta a quien no trae el token de una sesión guardada, y en las pruebas no hay un inicio de sesión real.
 */
public class SesionDePrueba implements RequestPostProcessor {

    /** Dirección desde la que se registran las sesiones de prueba. */
    private static final String IP = "127.0.0.1";

    /** Dispositivo con el que se registran las sesiones de prueba. */
    private static final String DISPOSITIVO = "Pruebas automáticas";

    /** Último token registrado para cada usuario, para no superar el límite de sesiones simultáneas. */
    private final Map<Integer, String> tokens = new ConcurrentHashMap<>();

    private final SesionService sesionService;

    public SesionDePrueba(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    /**
     * Agrega al pedido la cookie con el token de la sesión del usuario autenticado, si hay uno.
     *
     * @param request El pedido que se va a enviar.
     * @return El mismo pedido, con la cookie si correspondía.
     */
    @Override
    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
        Authentication autenticacion = TestSecurityContextHolder.getContext().getAuthentication();
        boolean traeToken = request.getCookies() != null
                && Arrays.stream(request.getCookies()).anyMatch(c -> SesionTokenFilter.COOKIE_TOKEN.equals(c.getName()));
        if (autenticacion != null && autenticacion.getPrincipal() instanceof Usuario usuario && !traeToken) {
            String token = tokens.get(usuario.getIdUsuario());
            if (token == null || sesionService.buscarVigentePorToken(token).isEmpty()) {
                token = sesionService.iniciar(usuario, false, IP, DISPOSITIVO).getToken();
                tokens.put(usuario.getIdUsuario(), token);
            }
            request.setCookies(new Cookie(SesionTokenFilter.COOKIE_TOKEN, token));
        }
        return request;
    }
}
