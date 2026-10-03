package com.app.idoneos.configuracion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.app.idoneos.servicio.Usuario.UsuarioDetallesServicio;

/**
 * Configuración de seguridad de Spring Security para Idóneos Online.
 * Define el inicio de sesión por formulario (con el correo como nombre de usuario),
 * el cierre de sesión y los permisos de acceso por rol a las rutas del módulo de cursos.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetallesServicio usuarioDetallesServicio;

    /**
     * Proveedor de autenticación que valida el usuario contra la base de datos.
     *
     * @return El proveedor de autenticación configurado.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider();
        proveedor.setUserDetailsService(usuarioDetallesServicio);
        proveedor.setPasswordEncoder(passwordEncoder());
        return proveedor;
    }

    /**
     * Codificador de contraseñas del sistema.
     *
     * @return Un codificador BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define las reglas de acceso a las rutas, el formulario de inicio de sesión y el cierre de sesión.
     *
     * @param http La configuración de seguridad HTTP.
     * @return La cadena de filtros de seguridad.
     * @throws Exception Si ocurre un error de configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/", "/inicio", "/acercaDe", "/novedades", "/error",
                                "/seguridad/login", "/cursos/catalogo", "/cursos/*/ficha",
                                "/css/**", "/js/**", "/img/**", "/webjars/**")
                        .permitAll()
                        // CU-02: Ver mis cursos (Alumno)
                        .requestMatchers("/cursos/mis-cursos").hasRole("Alumno")
                        // CU-01 y CU-11: búsqueda de cursos y cohortes (Docente y Administrador)
                        .requestMatchers(HttpMethod.GET, "/cursos", "/cursos/cohortes")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/cursos/cohortes/*/contexto").hasRole("Docente")
                        // CU-03 a CU-14: gestión de cursos, categorías y cohortes (Administrador)
                        .requestMatchers("/cursos/**").hasRole("Administrador")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/seguridad/login")
                        .loginProcessingUrl("/seguridad/login")
                        .defaultSuccessUrl("/inicio?login=true", true)
                        .failureUrl("/seguridad/login?error=true")
                        .permitAll())
                .userDetailsService(usuarioDetallesServicio)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/inicio?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .build();
    }
}
