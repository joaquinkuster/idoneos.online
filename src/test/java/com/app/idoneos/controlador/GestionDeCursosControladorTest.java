package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de los controladores del módulo de gestión de cursos (MOD-F-01): acceso por rol
 * y vistas que se muestran en cada caso de uso.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GestionDeCursosControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CU-06: el catálogo de cursos es público")
    void catalogoEsPublico() throws Exception {
        mockMvc.perform(get("/catalogo"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/catalogo"))
                .andExpect(model().attributeExists("cursos", "categorias", "niveles", "modalidades"));
    }

    @Test
    @DisplayName("El inicio de sesión es público y la sección Novedades ya no existe")
    void loginPublicoYSinNovedades() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("pages/login"));
        mockMvc.perform(get("/acercaDe")).andExpect(status().isOk())
                .andExpect(model().attributeExists("idEnVivo", "idGrabada", "idClonIa"));
        mockMvc.perform(get("/novedades")).andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Al iniciar sesión con \"Recordarme\" se guarda la cookie de sesión recordada, y sin él no")
    void iniciarSesionConRecordarme() throws Exception {
        mockMvc.perform(post("/login").param("username", "admin@idoneos.online").param("password", "123456")
                .param("recordarme", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"))
                .andExpect(cookie().exists("remember-me"));
        mockMvc.perform(post("/login").param("username", "admin@idoneos.online").param("password", "123456"))
                .andExpect(cookie().doesNotExist("remember-me"));
    }

    @Test
    @DisplayName("Al iniciar sesión, cada rol va a su pantalla: el alumno a sus inscripciones y el docente a sus cursos")
    void iniciarSesionRedirigeSegunElRol() throws Exception {
        mockMvc.perform(post("/login").param("username", "lucia.fernandez@correo.com").param("password", "123456"))
                .andExpect(redirectedUrl("/inscripcion/misCursos"));
        mockMvc.perform(post("/login").param("username", "fausto.spotorno@idoneos.online").param("password", "123456"))
                .andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @DisplayName("CU-01: un visitante sin sesión es redirigido al inicio de sesión")
    void visitanteNoPuedeBuscarCursos() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-01, CU-07 y CU-11: el administrador accede a los listados del panel de cursos, categorías y cohortes")
    void administradorAccedeALasBusquedas() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(status().isOk())
                .andExpect(view().name("pages/curso/buscarAdministrador"));
        mockMvc.perform(get("/categoria/buscar")).andExpect(status().isOk())
                .andExpect(view().name("pages/categoria/buscar"));
        mockMvc.perform(get("/cohorte/buscar")).andExpect(status().isOk())
                .andExpect(view().name("pages/cohorte/buscarAdministrador"));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve las vistas de tarjetas de cursos y cohortes, y no puede dar de baja")
    void docenteNoAccedeAlPanel() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(view().name("pages/curso/buscarDocente"));
        mockMvc.perform(get("/cohorte/buscar")).andExpect(view().name("pages/cohorte/buscarDocente"));
        mockMvc.perform(post("/curso/darDeBajaMasiva").param("ids", "1")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El rol por defecto solo puede ser uno de los roles vigentes del usuario")
    void cambiarRolPorDefectoInvalido() throws Exception {
        mockMvc.perform(post("/usuario/cambiarRolPorDefecto").param("idRol", "99999"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/usuario/cambiarRolPorDefecto")).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-01 y CU-11: el docente busca cursos y cohortes, pero no gestiona categorías")
    void docenteBuscaCursosYCohortes() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(status().isOk());
        mockMvc.perform(get("/cohorte/buscar")).andExpect(status().isOk());
        mockMvc.perform(get("/categoria/buscar")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "lucia.fernandez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-02: el alumno ve sus cursos y no accede a la gestión de cursos")
    void alumnoVeSusCursos() throws Exception {
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk())
                .andExpect(view().name("pages/inscripcion/misCursos"))
                .andExpect(model().attributeExists("inscripciones"));
        mockMvc.perform(get("/curso/buscar")).andExpect(status().isForbidden());
    }
}
