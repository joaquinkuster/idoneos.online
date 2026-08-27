package com.app.idoneos.config;

import com.app.idoneos.service.modulo_usuarios.UsuarioDetallesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuración de seguridad Spring Security para Idóneos Online.
 * Soporta autenticación por Formulario y Google OAuth 2.0 (PA-1)
 * con redirección inteligente por rol de usuario.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetallesService usuarioDetallesService;

    @Autowired
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(UsuarioDetallesService usuarioDetallesService) {
        this.usuarioDetallesService = usuarioDetallesService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetallesService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_Administrador".equalsIgnoreCase(role) || "Administrador".equalsIgnoreCase(role)) {
                    response.sendRedirect("/admin");
                    return;
                } else if ("ROLE_Docente".equalsIgnoreCase(role) || "Docente".equalsIgnoreCase(role)) {
                    response.sendRedirect("/docente");
                    return;
                }
            }
            response.sendRedirect("/cursos");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/inicio", "/login", "/registro", "/recuperar-contrasena", "/resetear-contrasena", "/cursos", "/cursos/*", "/public/**", "/css/**", "/js/**", "/img/**").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("Administrador")
                        .requestMatchers("/docente", "/docente/**").hasAnyRole("Docente", "Administrador")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler)
                )
                .userDetailsService(usuarioDetallesService)
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .build();
    }
}
