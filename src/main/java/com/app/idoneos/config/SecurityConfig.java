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

import com.app.idoneos.controlador.consejo.ContextoSesion;
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

    /** Duración de la sesión recordada: 30 días. */
    private static final int DIAS_DE_RECORDATORIO = 30;

    /** Nombre de la cookie de sesión (en lugar de JSESSIONID, que comparten todas las aplicaciones de Tomcat en localhost). */
    public static final String COOKIE_SESION = "SESION_IDONEOS";

    /** Nombre de la cookie de "Recordarme" (en lugar de remember-me). */
    public static final String COOKIE_RECORDARME = "RECORDARME_IDONEOS";

    @Autowired
    private UsuarioDetallesServicio usuarioDetallesServicio;

    /** Clave con la que se firma la cookie de "Recordarme". En producción debe definirse con una clave propia. */
    @Value("${idoneos.seguridad.clave-recordarme:idoneos-online-clave-de-desarrollo}")
    private String claveRecordarme;

    /**
     * Cambia el nombre de la cookie de sesión del contenedor.
     *
     * @return El inicializador que le pone nombre a la cookie de sesión.
     */
    @Bean
    public ServletContextInitializer nombreDeLaCookieDeSesion() {
        return contexto -> contexto.getSessionCookieConfig().setName(COOKIE_SESION);
    }

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
                                "/css/**", "/js/**", "/img/**", "/webjars/**",
                                // Imagen de cada curso (un solo nivel): las carpetas de unidad no son públicas
                                "/uploads/curso*/*")
                        .permitAll()
                        // CU-44, CU-46 y CU-47: buscar, registrar y modificar inscripciones (Administrador); CU-48: corregir
                        // los datos del certificado (Administrador); CU-49: dar de baja (Alumno la suya, Administrador)
                        .requestMatchers(HttpMethod.GET, "/inscripcion/buscar").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/inscripcion/registrar", "/inscripcion/modificar/*")
                        .hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/inscripcion/darDeBaja/*").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers("/certificado/**").hasRole("Administrador")
                        // CU-50 a CU-54: pagos. El aviso de MODO (webhook) es público: solo identifica el pago y el resultado se
                        // consulta a MODO; el administrador busca, registra y modifica pagos; el alumno ve y paga los suyos;
                        // el estado y el comprobante los ve cualquier usuario con sesión (el servicio controla de quién es)
                        .requestMatchers(HttpMethod.POST, "/pago/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pago/buscar").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/pago/registrar", "/pago/modificar/*").hasRole("Administrador")
                        .requestMatchers(HttpMethod.GET, "/pago/estado/*", "/pago/comprobante/*").authenticated()
                        .requestMatchers("/pago/**").hasRole("Alumno")
                        // CU-02, CU-27 y CU-45: Ver mis cursos, Acceder curso e Inscribirse (Alumno)
                        .requestMatchers("/inscripcion/**").hasRole("Alumno")
                        // CU-01 y CU-11: búsqueda de cursos y cohortes (Docente y Administrador)
                        .requestMatchers(HttpMethod.GET, "/curso/buscar", "/cohorte/buscar")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/cohorte/cambiarContexto/*", "/cohorte/establecerPorDefecto/*")
                        .hasRole("Docente")
                        // CU-15 a CU-18: ABM de programas (el docente gestiona; el administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/programa/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/programa/darDeBaja/*", "/programa/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/programa/**").hasRole("Docente")
                        // Abrir el archivo de un material: cualquier usuario con sesión (el servicio controla el acceso)
                        .requestMatchers(HttpMethod.GET, "/material/archivo/*", "/material/ver/*").authenticated()
                        // CU-28 a CU-31: ABM de materiales (el docente gestiona; el administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/material/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/material/darDeBaja/*", "/material/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/material/**").hasRole("Docente")
                        // CU-32 a CU-35: glosario. Verlo: cualquier usuario con sesión (el controlador controla el acceso);
                        // buscar los glosarios de un curso: docente y administrador; dar de baja un término: docente y
                        // administrador; registrar y modificar: docente
                        .requestMatchers(HttpMethod.GET, "/glosario/ver/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/glosario/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/glosario/darDeBaja/*").hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/glosario/**").hasRole("Docente")
                        // CU-19 a CU-22: ABM de unidades (el docente agrega y modifica; el administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/unidad/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/unidad/darDeBaja/*", "/unidad/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/unidad/**").hasRole("Docente")
                        // CU-23 y CU-24: cronograma (docente y administrador lo consultan; solo el docente lo modifica)
                        .requestMatchers(HttpMethod.GET, "/cronograma/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/cronograma/**").hasRole("Docente")
                        // CU-35 a CU-42: foro. Verlo: cualquier usuario con sesión (el controlador controla el acceso);
                        // listar los foros de un curso: docente y administrador; consultar: alumno; responder: docente;
                        // moderar (dar de baja consultas y respuestas): administrador
                        .requestMatchers(HttpMethod.GET, "/foro/ver/*", "/foro/consulta/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/foro/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/foro/registrar", "/foro/modificar/*").hasRole("Alumno")
                        .requestMatchers(HttpMethod.POST, "/foro/responder", "/foro/modificarRespuesta/*").hasRole("Docente")
                        .requestMatchers(HttpMethod.POST, "/foro/darDeBaja/*", "/foro/darDeBajaRespuesta/*").hasRole("Administrador")
                        // CU-25 y CU-27: participantes del curso (docente y administrador; el alumno ve los de su cohorte)
                        .requestMatchers(HttpMethod.GET, "/participante/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/participante/ver/*").hasRole("Alumno")
                        // CU-27: Acceder curso (Docente; el Administrador lo ve sin gestionar su contenido)
                        .requestMatchers(HttpMethod.GET, "/curso/acceder/*").hasAnyRole("Docente", "Administrador")
                        // CU-03 a CU-14 y CU-56 a CU-59: gestión de cursos, categorías, cohortes y descuentos (Administrador)
                        .requestMatchers("/curso/**", "/categoria/**", "/cohorte/**", "/descuento/**").hasRole("Administrador")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // El administrador y el docente entran a sus cursos, el alumno a sus inscripciones y los demás roles al inicio
                        .successHandler((solicitud, respuesta, autenticacion) -> {
                            Usuario usuario = (Usuario) autenticacion.getPrincipal();
                            // "Recordarme": la sesión (y su cookie) dura 30 días en lugar de cerrarse con el navegador o por
                            // inactividad; además queda la cookie de "Recordarme" como respaldo si la sesión se pierde
                            if ("true".equals(solicitud.getParameter("recordarme"))) {
                                HttpSession sesion = solicitud.getSession();
                                int segundos = DIAS_DE_RECORDATORIO * 24 * 60 * 60;
                                sesion.setMaxInactiveInterval(segundos);
                                Cookie cookie = new Cookie(COOKIE_SESION, sesion.getId());
                                cookie.setPath(solicitud.getContextPath().isEmpty() ? "/" : solicitud.getContextPath());
                                cookie.setHttpOnly(true);
                                cookie.setMaxAge(segundos);
                                respuesta.addCookie(cookie);
                            }
                            solicitud.getSession().setAttribute(ContextoSesion.MENSAJE_DE_SESION,
                                    "Sesión iniciada correctamente. ¡Bienvenido/a, " + usuario.getNombre() + "!");
                            String destino = usuario.esAdministradorActivo() || usuario.esDocenteActivo() ? "/curso/buscar"
                                    : usuario.esAlumnoActivo() ? "/inscripcion/misCursos" : "/inicio";
                            respuesta.sendRedirect(solicitud.getContextPath() + destino);
                        })
                        .failureUrl("/login?error=true")
                        .permitAll())
                // "Recordarme": mantiene la sesión iniciada mediante una cookie
                .rememberMe(recordarme -> recordarme
                        .key(claveRecordarme)
                        .rememberMeParameter("recordarme")
                        .rememberMeCookieName(COOKIE_RECORDARME)
                        .tokenValiditySeconds(DIAS_DE_RECORDATORIO * 24 * 60 * 60))
                .userDetailsService(usuarioDetallesServicio)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies(COOKIE_SESION, COOKIE_RECORDARME)
                        .permitAll())
                .build();
    }
}
