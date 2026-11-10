package com.app.idoneos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;

import com.app.idoneos.controller.advice.ContextoSesionAdvice;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.usuario.UsuarioDetallesService;

/** Configuración de seguridad de Spring Security. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Duración de la sesión recordada: 30 días. */
    private static final int DIAS_DE_RECORDATORIO = 30;

    /** Nombre de la cookie de sesión. */
    public static final String COOKIE_SESION = "SESION_IDONEOS";

    /** Nombre de la cookie de "Recordarme" (en lugar de remember-me). */
    public static final String COOKIE_RECORDARME = "RECORDARME_IDONEOS";

    @Autowired
    private UsuarioDetallesService usuarioDetallesService;

    /** Clave con la que se firma la cookie de "Recordarme". En producción debe definirse con una clave propia. */
    @Value("${idoneos.seguridad.clave-recordarme:idoneos-online-clave-de-desarrollo}")
    private String claveRecordarme;

    /** Cambia el nombre de la cookie de sesión del contenedor. */
    @Bean
    public ServletContextInitializer nombreDeLaCookieDeSesion() {
        return contexto -> contexto.getSessionCookieConfig().setName(COOKIE_SESION);
    }

    /** Crea el proveedor de autenticación que valida el usuario contra la base de datos. */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider();
        proveedor.setUserDetailsService(usuarioDetallesService);
        proveedor.setPasswordEncoder(passwordEncoder());
        return proveedor;
    }

    /** Crea el codificador de contraseñas del sistema. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Define las reglas de acceso a las rutas, el formulario de inicio de sesión y el cierre de sesión. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/", "/inicio", "/acercaDe", "/error",
                                "/login", "/catalogo/**",
                                "/css/**", "/js/**", "/img/**", "/webjars/**",
                                // Imagen de cada curso (un solo nivel): las carpetas de tema no son públicas
                                "/uploads/curso*/*")
                        .permitAll()
                        // buscar, registrar y modificar inscripciones (Administrador). Corregir
                        // los datos del certificado (Administrador). Dar de baja (Alumno la suya, Administrador)
                        .requestMatchers(HttpMethod.GET, "/inscripcion/buscar").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/inscripcion/modificar/*", "/inscripcion/modalCertificado/*").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/inscripcion/registrar", "/inscripcion/modificar/*")
                        .hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/inscripcion/darDeBaja/*").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/inscripcion/darDeBajaMasiva").hasRole("Administrador")
                        // Mis certificados: el alumno los ve y los descarga. El administrador descarga cualquiera
                        .requestMatchers(HttpMethod.GET, "/certificado/buscar").hasRole("Alumno")
                        .requestMatchers(HttpMethod.GET, "/certificado/descargar/*").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers("/certificado/**").hasRole("Administrador")
                        // pagos.
                        .requestMatchers(HttpMethod.POST, "/pago/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pago/buscar").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/pago/modificar/*").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/pago/registrar", "/pago/modificar/*").hasRole("Administrador")
                        .requestMatchers(HttpMethod.GET, "/pago/estado/*", "/pago/comprobante/*").authenticated()
                        .requestMatchers("/pago/**").hasRole("Alumno")
                        // Ver mis cursos, Acceder curso e Inscribirse (Alumno)
                        // Intereses del alumno: las categorías que le interesan (se le preguntan al ingresar y se editan desde el menú)
                        .requestMatchers("/interes/**").hasRole("Alumno")
                        // Mi biblioteca: los recursos del docente
                        // Explorar y ver los recursos: el docente (los públicos y los suyos) y el administrador (todos). Dar de baja, también ambos
                        .requestMatchers(HttpMethod.GET, "/biblioteca/buscar", "/biblioteca/explorar", "/biblioteca/ver/*", "/biblioteca/archivo/*", "/biblioteca/indice/*")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/biblioteca/darDeBaja/*", "/biblioteca/darDeBajaMasiva").hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/biblioteca/**").hasRole("Docente")
                        .requestMatchers("/inscripcion/**").hasRole("Alumno")
                        .requestMatchers("/recordatorio/**").hasRole("Alumno")
                        // búsqueda de cursos y cohortes (Docente y Administrador)
                        .requestMatchers(HttpMethod.GET, "/curso/buscar", "/cohorte/buscar")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/cohorte/cambiarContexto/*", "/cohorte/establecerPorDefecto/*",
                                "/cohorte/asignarPrograma/*", "/cohorte/finalizarDiseno/*")
                        .hasRole("Docente")
                        // Diseño de curso: el docente lo completa (datos del curso) y el administrador lo consulta
                        .requestMatchers(HttpMethod.POST, "/curso/modificarPorDocente/*").hasRole("Docente")
                        // ABM de programas (el docente gestiona. El administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/programa/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/programa/darDeBaja/*", "/programa/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/programa/**").hasRole("Docente")
                        // Abrir el archivo de un material: cualquier usuario con sesión (el servicio controla el acceso)
                        .requestMatchers(HttpMethod.GET, "/recurso/archivo/*", "/recurso/ver/*", "/recurso/indice/*", "/recurso/resaltados/*").authenticated()
                        // Las anotaciones del alumno sobre un documento: solo las hace y las ve el propio alumno
                        .requestMatchers(HttpMethod.POST, "/recurso/resaltados/*/*").hasRole("Alumno")
                        // ABM de materiales (el docente gestiona. El administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/recurso/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/recurso/darDeBaja/*", "/recurso/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/recurso/**").hasRole("Docente")
                        // glosario.
                        .requestMatchers(HttpMethod.GET, "/glosario/buscarPorTema/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/glosario/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/termino/darDeBaja/*").hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/termino/**").hasRole("Docente")
                        // ABM de temas (el docente agrega y modifica. El administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/tema/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/tema/darDeBaja/*", "/tema/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/tema/**").hasRole("Docente")
                        // ABM de pools (el docente y el administrador los buscan y los dan de baja, el docente
                        // además los crea y los modifica)
                        .requestMatchers(HttpMethod.GET, "/pool/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/pool/darDeBaja/*", "/pool/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/pool/**").hasRole("Docente")
                        // ABM de autoevaluaciones (el docente y el administrador las buscan y las dan de baja, el
                        // docente además las crea y las modifica)
                        .requestMatchers(HttpMethod.GET, "/autoevaluacion/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/autoevaluacion/darDeBaja/*", "/autoevaluacion/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/autoevaluacion/ver/*").hasRole("Alumno")
                        .requestMatchers("/autoevaluacion/**").hasRole("Docente")
                        // seguimiento de los intentos (el docente y el administrador los buscan, el docente
                        // aprueba y el administrador los da de baja)
                        .requestMatchers(HttpMethod.GET, "/intento/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/intento/aprobar/*").hasRole("Docente")
                        .requestMatchers(HttpMethod.POST, "/intento/darDeBaja/*").hasRole("Administrador")
                        // calificaciones (docente y administrador ven la matriz del curso. El alumno, las suyas)
                        .requestMatchers(HttpMethod.GET, "/intento/buscarPorAutoevaluacion").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/intento/buscarPorInscripcion/*").hasRole("Alumno")
                        // el alumno rinde sus intentos (iniciar, responder, entregar...)
                        .requestMatchers("/intento/**").hasRole("Alumno")
                        // cronograma (docente y administrador lo consultan. Solo el docente lo modifica)
                        .requestMatchers(HttpMethod.GET, "/cronograma/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/cronograma/**").hasRole("Docente")
                        // foro.
                        .requestMatchers(HttpMethod.GET, "/foro/buscarPorTema/*", "/consulta/ver/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/foro/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/respuesta/modificar/*").hasRole("Docente")
                        .requestMatchers(HttpMethod.POST, "/consulta/registrar", "/consulta/modificar/*").hasRole("Alumno")
                        .requestMatchers(HttpMethod.POST, "/respuesta/registrar", "/respuesta/modificar/*").hasRole("Docente")
                        .requestMatchers(HttpMethod.POST, "/consulta/darDeBaja/*", "/respuesta/darDeBaja/*").hasRole("Administrador")
                        // docentes del curso (docente y administrador. El alumno ve los de su cohorte)
                        .requestMatchers(HttpMethod.GET, "/participante/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/participante/ver/*").hasRole("Alumno")
                        // progreso de los alumnos del curso (docente y administrador)
                        .requestMatchers(HttpMethod.GET, "/progreso/buscar")
                        .hasAnyRole("Docente", "Administrador")
                        // Acceder curso (Docente. El Administrador lo ve sin gestionar su contenido)
                        .requestMatchers(HttpMethod.GET, "/curso/acceder/*").hasAnyRole("Docente", "Administrador")
                        // gestión de cursos, categorías, cohortes y descuentos (Administrador)
                        .requestMatchers("/curso/**", "/categoria/**", "/cohorte/**", "/descuento/**").hasRole("Administrador")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // El administrador y el docente entran a sus cursos, el alumno a sus inscripciones y los demás roles al inicio
                        .successHandler((request, response, authentication) -> {
                            Usuario usuario = (Usuario) authentication.getPrincipal();
                            // "Recordarme": la sesión (y su cookie) dura 30 días en lugar de cerrarse con el navegador o por
                            // inactividad. Además queda la cookie de "Recordarme" como respaldo si la sesión se pierde
                            if ("true".equals(request.getParameter("recordarme"))) {
                                HttpSession sesion = request.getSession();
                                int segundos = DIAS_DE_RECORDATORIO * 24 * 60 * 60;
                                sesion.setMaxInactiveInterval(segundos);
                                Cookie cookie = new Cookie(COOKIE_SESION, sesion.getId());
                                cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                                cookie.setHttpOnly(true);
                                cookie.setMaxAge(segundos);
                                response.addCookie(cookie);
                            }
                            request.getSession().setAttribute(ContextoSesionAdvice.MENSAJE_DE_SESION,
                                    "Sesión iniciada correctamente. ¡Bienvenido/a, " + usuario.getNombre() + "!");
                            String destino = usuario.esAdministradorActivo() || usuario.esDocenteActivo() ? "/curso/buscar"
                                    : usuario.esAlumnoActivo() ? "/inscripcion/buscar" : "/inicio";
                            response.sendRedirect(request.getContextPath() + destino);
                        })
                        .failureUrl("/login?error=true")
                        .permitAll())
                // "Recordarme": mantiene la sesión iniciada mediante una cookie
                .rememberMe(recordarme -> recordarme
                        .key(claveRecordarme)
                        .rememberMeParameter("recordarme")
                        .rememberMeCookieName(COOKIE_RECORDARME)
                        .tokenValiditySeconds(DIAS_DE_RECORDATORIO * 24 * 60 * 60))
                .userDetailsService(usuarioDetallesService)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies(COOKIE_SESION, COOKIE_RECORDARME)
                        .permitAll())
                .build();
    }
}
