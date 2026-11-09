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
    private UsuarioDetallesService usuarioDetallesService;

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
        proveedor.setUserDetailsService(usuarioDetallesService);
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
                                // Imagen de cada curso (un solo nivel): las carpetas de tema no son públicas
                                "/uploads/curso*/*")
                        .permitAll()
                        // CU-44, CU-46 y CU-47: buscar, registrar y modificar inscripciones (Administrador); CU-48: corregir
                        // los datos del certificado (Administrador); CU-49: dar de baja (Alumno la suya, Administrador)
                        .requestMatchers(HttpMethod.GET, "/inscripcion/buscar").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/inscripcion/modificar/*", "/inscripcion/modalCertificado/*").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/inscripcion/registrar", "/inscripcion/modificar/*")
                        .hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/inscripcion/darDeBaja/*").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/inscripcion/darDeBajaMasiva").hasRole("Administrador")
                        // Mis certificados: el alumno los ve y los descarga; el administrador descarga cualquiera
                        .requestMatchers(HttpMethod.GET, "/certificado/buscar").hasRole("Alumno")
                        .requestMatchers(HttpMethod.GET, "/certificado/descargar/*").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers("/certificado/**").hasRole("Administrador")
                        // CU-50 a CU-54: pagos. El aviso de MODO (webhook) es público: solo identifica el pago y el resultado se
                        // consulta a MODO; el administrador busca, registra y modifica pagos; el alumno ve y paga los suyos;
                        // el estado y el comprobante los ve cualquier usuario con sesión (el servicio controla de quién es)
                        .requestMatchers(HttpMethod.POST, "/pago/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pago/buscar").hasAnyRole("Alumno", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/pago/modificar/*").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/pago/registrar", "/pago/modificar/*").hasRole("Administrador")
                        .requestMatchers(HttpMethod.GET, "/pago/estado/*", "/pago/comprobante/*").authenticated()
                        .requestMatchers("/pago/**").hasRole("Alumno")
                        // CU-02, CU-27 y CU-45: Ver mis cursos, Acceder curso e Inscribirse (Alumno)
                        // Intereses del alumno: las categorías que le interesan (se le preguntan al ingresar y se editan desde el menú)
                        .requestMatchers("/interes/**").hasRole("Alumno")
                        // Mi biblioteca: los recursos del docente
                        // Explorar y ver los recursos: el docente (los públicos y los suyos) y el administrador (todos); dar de baja, también ambos
                        .requestMatchers(HttpMethod.GET, "/biblioteca/buscar", "/biblioteca/explorar", "/biblioteca/ver/*", "/biblioteca/archivo/*", "/biblioteca/indice/*")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/biblioteca/darDeBaja/*", "/biblioteca/darDeBajaMasiva").hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/biblioteca/**").hasRole("Docente")
                        .requestMatchers("/inscripcion/**").hasRole("Alumno")
                        .requestMatchers("/recordatorio/**").hasRole("Alumno")
                        // CU-01 y CU-11: búsqueda de cursos y cohortes (Docente y Administrador)
                        .requestMatchers(HttpMethod.GET, "/curso/buscar", "/cohorte/buscar")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/cohorte/cambiarContexto/*", "/cohorte/establecerPorDefecto/*",
                                "/cohorte/asignarPrograma/*")
                        .hasRole("Docente")
                        // Diseño de curso: el docente lo completa (datos del curso) y el administrador lo consulta
                        .requestMatchers(HttpMethod.POST, "/curso/modificarPorDocente/*").hasRole("Docente")
                        // CU-15 a CU-18: ABM de programas (el docente gestiona; el administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/programa/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/programa/darDeBaja/*", "/programa/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/programa/**").hasRole("Docente")
                        // Abrir el archivo de un material: cualquier usuario con sesión (el servicio controla el acceso)
                        .requestMatchers(HttpMethod.GET, "/recurso/archivo/*", "/recurso/ver/*", "/recurso/indice/*", "/recurso/resaltados/*").authenticated()
                        // Las anotaciones del alumno sobre un documento: solo las hace y las ve el propio alumno
                        .requestMatchers(HttpMethod.POST, "/recurso/resaltados/*/*").hasRole("Alumno")
                        // CU-28 a CU-31: ABM de materiales (el docente gestiona; el administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/recurso/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/recurso/darDeBaja/*", "/recurso/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/recurso/**").hasRole("Docente")
                        // CU-32 a CU-35: glosario. Verlo: cualquier usuario con sesión (el controlador controla el acceso);
                        // buscar los glosarios de un curso: docente y administrador; dar de baja un término: docente y
                        // administrador; registrar y modificar: docente
                        .requestMatchers(HttpMethod.GET, "/glosario/buscarPorTema/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/glosario/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/termino/darDeBaja/*").hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/termino/**").hasRole("Docente")
                        // CU-19 a CU-22: ABM de temas (el docente agrega y modifica; el administrador busca y da de baja)
                        .requestMatchers(HttpMethod.GET, "/tema/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/tema/darDeBaja/*", "/tema/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/tema/**").hasRole("Docente")
                        // CU-60 a CU-63: ABM de pools (docente y administrador los buscan y los dan de baja; el docente los
                        // crea y los modifica)
                        .requestMatchers(HttpMethod.GET, "/pool/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/pool/darDeBaja/*", "/pool/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/pool/**").hasRole("Docente")
                        // CU-64 a CU-67: ABM de autoevaluaciones (docente y administrador las buscan y las dan de baja; el
                        // docente las crea y las modifica)
                        .requestMatchers(HttpMethod.GET, "/autoevaluacion/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/autoevaluacion/darDeBaja/*", "/autoevaluacion/darDeBajaMasiva")
                        .hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/autoevaluacion/ver/*").hasRole("Alumno")
                        .requestMatchers("/autoevaluacion/**").hasRole("Docente")
                        // CU-68, CU-71 y CU-72: seguimiento de los intentos (docente y administrador los buscan; el docente
                        // aprueba; el administrador los da de baja)
                        .requestMatchers(HttpMethod.GET, "/intento/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.POST, "/intento/aprobar/*").hasRole("Docente")
                        .requestMatchers(HttpMethod.POST, "/intento/darDeBaja/*").hasRole("Administrador")
                        // CU-69: calificaciones (docente y administrador ven la matriz del curso; el alumno, las suyas)
                        .requestMatchers(HttpMethod.GET, "/intento/buscarPorAutoevaluacion").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/intento/buscarPorInscripcion/*").hasRole("Alumno")
                        // CU-70: el alumno rinde sus intentos (iniciar, responder, entregar...)
                        .requestMatchers("/intento/**").hasRole("Alumno")
                        // CU-23 y CU-24: cronograma (docente y administrador lo consultan; solo el docente lo modifica)
                        .requestMatchers(HttpMethod.GET, "/cronograma/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers("/cronograma/**").hasRole("Docente")
                        // CU-35 a CU-42: foro. Verlo: cualquier usuario con sesión (el controlador controla el acceso);
                        // listar los foros de un curso: docente y administrador; consultar: alumno; responder: docente;
                        // moderar (dar de baja consultas y respuestas): administrador
                        .requestMatchers(HttpMethod.GET, "/foro/buscarPorTema/*", "/consulta/ver/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/foro/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/respuesta/modificar/*").hasRole("Docente")
                        .requestMatchers(HttpMethod.POST, "/consulta/registrar", "/consulta/modificar/*").hasRole("Alumno")
                        .requestMatchers(HttpMethod.POST, "/respuesta/registrar", "/respuesta/modificar/*").hasRole("Docente")
                        .requestMatchers(HttpMethod.POST, "/consulta/darDeBaja/*", "/respuesta/darDeBaja/*").hasRole("Administrador")
                        // CU-25 y CU-27: docentes del curso (docente y administrador; el alumno ve los de su cohorte)
                        .requestMatchers(HttpMethod.GET, "/participante/buscar").hasAnyRole("Docente", "Administrador")
                        .requestMatchers(HttpMethod.GET, "/participante/ver/*").hasRole("Alumno")
                        // CU-55: progreso de los alumnos del curso (docente y administrador)
                        .requestMatchers(HttpMethod.GET, "/progreso/buscar")
                        .hasAnyRole("Docente", "Administrador")
                        // CU-27: Acceder curso (Docente; el Administrador lo ve sin gestionar su contenido)
                        .requestMatchers(HttpMethod.GET, "/curso/acceder/*").hasAnyRole("Docente", "Administrador")
                        // CU-03 a CU-14 y CU-56 a CU-59: gestión de cursos, categorías, cohortes y descuentos (Administrador)
                        .requestMatchers("/curso/**", "/categoria/**", "/cohorte/**", "/descuento/**").hasRole("Administrador")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // El administrador y el docente entran a sus cursos, el alumno a sus inscripciones y los demás roles al inicio
                        .successHandler((request, response, authentication) -> {
                            Usuario usuario = (Usuario) authentication.getPrincipal();
                            // "Recordarme": la sesión (y su cookie) dura 30 días en lugar de cerrarse con el navegador o por
                            // inactividad; además queda la cookie de "Recordarme" como respaldo si la sesión se pierde
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
