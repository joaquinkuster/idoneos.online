package com.app.idoneos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import com.app.idoneos.service.sesion.SesionService;
import com.app.idoneos.service.usuario.UsuarioDetallesService;

/** Configuración de seguridad de Spring Security. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Nombre de la cookie de sesión del contenedor. */
    public static final String COOKIE_SESION = "SESION_IDONEOS";

    @Autowired
    private UsuarioDetallesService usuarioDetallesService;

    @Autowired
    private SesionService sesionService;

    @Autowired
    private AccesoComponent accesoComponent;

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
                // En cada pedido comprueba que la sesión del usuario siga registrada y vigente (cookie y base de datos)
                .addFilterAfter(new SesionTokenFilter(sesionService), SecurityContextHolderFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/", "/inicio", "/acercaDe", "/error",
                                "/login", "/catalogo/**",
                                // Registro, validación del correo y recuperación de la contraseña
                                "/registro", "/registro/google", "/usuario/completarGoogle", "/oauth2/**", "/login/oauth2/**",
                                "/recuperar/*", "/usuario/autoregistrar", "/usuario/validarCorreo/*",
                                "/usuario/solicitarRecuperacion", "/usuario/restablecerContrasena",
                                "/css/**", "/js/**", "/img/**", "/webjars/**",
                                // Portada de cada curso: el resto de las carpetas no es público
                                "/uploads/portadas/*",
                                // Fotos de perfil, que se ven en el menú y en las listas de participantes
                                "/uploads/perfiles/**", "/uploads/institucion/**")
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
                        .requestMatchers(HttpMethod.GET, "/recurso/buscar", "/recurso/estadoOcr").hasAnyRole("Docente", "Administrador")
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
                        // ABM de clases en vivo (el docente y el administrador las buscan, el docente además las programa,
                        // las modifica y las cancela, y solo el administrador da de baja las finalizadas)
                        .requestMatchers(HttpMethod.GET, "/claseEnVivo/buscar")
                        .hasAnyRole("Docente", "Administrador")
                        // la sala, su estado y el chat: el docente de la clase y los alumnos de su cohorte (el servicio verifica cuál)
                        .requestMatchers(HttpMethod.GET, "/claseEnVivo/sala/*", "/claseEnVivo/estado/*", "/claseEnVivo/chat/*/eventos",
                                "/claseEnVivo/chat/*/mensajes")
                        .hasAnyRole("Alumno", "Docente")
                        .requestMatchers(HttpMethod.POST, "/claseEnVivo/chat/*/mensajes").hasAnyRole("Alumno", "Docente")
                        // el servidor de medios consulta quién puede transmitir y mirar
                        .requestMatchers(HttpMethod.POST, "/streaming/autorizar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/claseEnVivo/darDeBaja/*", "/claseEnVivo/darDeBajaMasiva")
                        .hasRole("Administrador")
                        .requestMatchers("/claseEnVivo/**").hasRole("Docente")
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
                        .requestMatchers("/curso/**", "/categoria/**", "/cohorte/**", "/descuento/**", "/configuracion/**", "/auditoria/**", "/reporte/**", "/estadisticas/**").hasRole("Administrador")
                        // ABM de usuarios (Administrador). El perfil propio y el cambio de rol los puede usar cualquier usuario con sesión
                        .requestMatchers(HttpMethod.GET, "/usuario/buscar", "/usuario/modificar/*").hasRole("Administrador")
                        .requestMatchers(HttpMethod.POST, "/usuario/registrar", "/usuario/modificar/*", "/usuario/darDeBaja/*",
                                "/usuario/darDeBajaMasiva")
                        .hasRole("Administrador")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        // El administrador y el docente entran a sus cursos, el alumno a sus inscripciones y los demás roles al inicio
                        // Registra la sesión en la base y guarda su token en la cookie. "Recordarme" la hace durar 30 días. Si la
                        // cuenta ya tiene el máximo de sesiones, no se la deja ingresar
                        .successHandler(accesoComponent::alIngresar)
                        // Una cuenta con el correo sin validar se distingue de las credenciales incorrectas
                        .failureHandler((request, response, excepcion) -> response.sendRedirect(request.getContextPath()
                                + (excepcion instanceof DisabledException ? "/login?error=noValidado" : "/login?error=true")))
                        .permitAll())
                // Ingreso con Google: si el correo ya tiene cuenta se la vincula, y si no, se le pide el DNI para crearla
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .successHandler(accesoComponent::alIngresarConGoogle)
                        .failureHandler((request, response, excepcion) -> response.sendRedirect(request.getContextPath()
                                + "/login?error=google")))
                .userDetailsService(usuarioDetallesService)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        // Al cerrar sesión se borra su registro de la base y la cookie del token
                        .addLogoutHandler((request, response, authentication) -> {
                            sesionService.cerrarPorToken(SesionTokenFilter.leerToken(request));
                            SesionTokenFilter.borrarCookie(response);
                        })
                        .deleteCookies(COOKIE_SESION)
                        .permitAll())
                .build();
    }
}
