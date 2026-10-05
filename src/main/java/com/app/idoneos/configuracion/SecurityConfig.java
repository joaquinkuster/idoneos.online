package com.app.idoneos.configuracion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.app.idoneos.controlador.ModeloGlobalControlador;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Usuario.UsuarioDetallesServicio;

/**
 * Configuración de seguridad de Spring Security para Idóneos Online.
 * Define el inicio de sesión por formulario (con el correo como nombre de usuario),
 * el cierre de sesión y los permisos de acceso por rol a las rutas del módulo de cursos.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Duración de la sesión recordada: 14 días. */
    private static final int DIAS_DE_RECORDATORIO = 14;

    @Autowired
    private UsuarioDetallesServicio usuarioDetallesServicio;

    /** Clave con la que se firma la cookie de "Recordarme". En producción debe definirse con una clave propia. */
    @Value("${idoneos.seguridad.clave-recordarme:idoneos-online-clave-de-desarrollo}")
    private String claveRecordarme;

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
                        .requestMatchers("/", "/inicio", "/acercaDe", "/error",
                                "/login", "/catalogo/**",
                                "/css/**", "/js/**", "/img/**", "/webjars/**")
                        .permitAll()
                        // CU-02: Ver mis cursos (Alumno)
                        .requestMatchers("/inscripcion/misCursos").hasRole("Alumno")
                        // CU-01 y CU-11: búsqueda de cursos y cohortes (Docente y Administrador)
                        .requestMatchers(HttpMethod.GET, "/curso/buscar", "/cohorte/buscar")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/cohorte/cambiarContexto/*").hasRole("Docente")
                        // CU-03 a CU-14: gestión de cursos, categorías y cohortes (Administrador)
                        .requestMatchers("/curso/**", "/categoria/**", "/cohorte/**").hasRole("Administrador")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // El administrador entra a la gestión de cursos, el alumno a sus cursos y los demás roles al inicio
                        .successHandler((solicitud, respuesta, autenticacion) -> {
                            Usuario usuario = (Usuario) autenticacion.getPrincipal();
                            solicitud.getSession().setAttribute(ModeloGlobalControlador.MENSAJE_DE_SESION,
                                    "Sesión iniciada correctamente. ¡Bienvenido/a, " + usuario.getNombre() + "!");
                            String destino = usuario.esAdministradorActivo() ? "/curso/buscar"
                                    : usuario.esAlumnoActivo() ? "/inscripcion/misCursos" : "/inicio";
                            respuesta.sendRedirect(solicitud.getContextPath() + destino);
                        })
                        .failureUrl("/login?error=true")
                        .permitAll())
                // "Recordarme": mantiene la sesión iniciada mediante una cookie
                .rememberMe(recordarme -> recordarme
                        .key(claveRecordarme)
                        .rememberMeParameter("recordarme")
                        .tokenValiditySeconds(DIAS_DE_RECORDATORIO * 24 * 60 * 60))
                .userDetailsService(usuarioDetallesServicio)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/inicio?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll())
                .build();
    }
}
