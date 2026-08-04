package com.app.idoneos.config;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.Usuario.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler de éxito para autenticación mediante Google OAuth 2.0 (PA-1).
 * Delegación a UsuarioService en un contexto transaccional.
 */
@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String givenName = oauth2User.getAttribute("given_name");
        String familyName = oauth2User.getAttribute("family_name");
        String googleSub = oauth2User.getAttribute("sub");

        if (email != null) {
            String nombre = (givenName != null) ? givenName : (name != null ? name : "Usuario");
            String apellido = (familyName != null) ? familyName : "Google";

            Usuario usuario = usuarioService.procesarUsuarioOAuth2(email, nombre, apellido, googleSub);

            // Actualizar contexto de seguridad con nuestra entidad Usuario
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        response.sendRedirect("/cursos");
    }
}
