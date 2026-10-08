package com.app.idoneos.controlador;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de la pantalla a la que se redirige al iniciar sesión según el rol del usuario.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginRedireccionControladorTest {

    private static final String CLAVE_DE_PRUEBA = "123456";

    @Autowired
    private MockMvc mockMvc;

    private void iniciarSesion(String correo, String destinoEsperado) throws Exception {
        mockMvc.perform(formLogin("/login").user(correo).password(CLAVE_DE_PRUEBA))
                .andExpect(redirectedUrl(destinoEsperado));
    }

    @Test
    @DisplayName("El docente entra a Mis cursos")
    void docenteEntraAMisCursos() throws Exception {
        iniciarSesion("fausto.spotorno@idoneos.online", "/curso/buscar");
    }

    @Test
    @DisplayName("El administrador entra a la gestión de cursos")
    void administradorEntraALaGestionDeCursos() throws Exception {
        iniciarSesion("admin@idoneos.online", "/curso/buscar");
    }

    @Test
    @DisplayName("El alumno entra a sus cursos")
    void alumnoEntraASusCursos() throws Exception {
        iniciarSesion("valentina.ruiz@correo.com", "/inscripcion/misCursos");
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Al cerrar sesión, el docente vuelve al inicio de sesión")
    void docenteCierraSesion() throws Exception {
        mockMvc.perform(post("/logout")).andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Al cerrar sesión, el administrador vuelve al inicio de sesión")
    void administradorCierraSesion() throws Exception {
        mockMvc.perform(post("/logout")).andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Al cerrar sesión, el alumno vuelve al inicio de sesión")
    void alumnoCierraSesion() throws Exception {
        mockMvc.perform(post("/logout")).andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    @DisplayName("El inicio de sesión muestra el mensaje de despedida después de cerrar sesión")
    void loginMuestraElMensajeDeDespedida() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Has cerrado sesi")));
    }
}
