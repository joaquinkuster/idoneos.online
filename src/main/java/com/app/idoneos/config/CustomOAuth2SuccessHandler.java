package com.app.idoneos.config;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.RolUsuario;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.UsuarioRepository;
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
import java.util.Optional;

/**
 * Handler de éxito para autenticación mediante Google OAuth 2.0 (PA-1).
 * Crea automáticamente la cuenta del Alumno si no existe y establece el contexto de seguridad.
 */
@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String givenName = oauth2User.getAttribute("given_name");
        String familyName = oauth2User.getAttribute("family_name");

        if (email != null) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndBajaFalse(email);
            Usuario usuario;

            if (usuarioOpt.isEmpty()) {
                String nombre = (givenName != null) ? givenName : (name != null ? name : "Usuario");
                String apellido = (familyName != null) ? familyName : "Google";

                usuario = new Usuario(nombre, apellido, email, null, RolUsuario.Alumno);
                usuario.setEmailValidado(true);
                usuario.setGoogleId(oauth2User.getAttribute("sub"));
                usuario = usuarioRepository.save(usuario);

                if (!alumnoRepository.existsById(usuario.getId())) {
                    alumnoRepository.save(new Alumno(usuario));
                }
            } else {
                usuario = usuarioOpt.get();
                if (usuario.esAlumno() && !alumnoRepository.existsById(usuario.getId())) {
                    alumnoRepository.save(new Alumno(usuario));
                }
            }

            // Actualizar contexto de seguridad con nuestra entidad Usuario
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        response.sendRedirect("/cursos");
    }
}
