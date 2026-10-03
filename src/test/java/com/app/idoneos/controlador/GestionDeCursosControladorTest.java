package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
        mockMvc.perform(get("/cursos/catalogo"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-06-explorar-catalogo-de-cursos"))
                .andExpect(model().attributeExists("cursos", "categorias", "niveles", "modalidades"));
    }

    @Test
    @DisplayName("CU-01: un visitante sin sesión es redirigido al inicio de sesión")
    void visitanteNoPuedeBuscarCursos() throws Exception {
        mockMvc.perform(get("/cursos")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-01, CU-07 y CU-11: el administrador accede a la búsqueda de cursos, categorías y cohortes")
    void administradorAccedeALasBusquedas() throws Exception {
        mockMvc.perform(get("/cursos")).andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-01-buscar-curso"));
        mockMvc.perform(get("/cursos/categorias")).andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-07-buscar-categoria"));
        mockMvc.perform(get("/cursos/cohortes")).andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-11-buscar-cohorte"));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-01 y CU-11: el docente busca cursos y cohortes, pero no gestiona categorías")
    void docenteBuscaCursosYCohortes() throws Exception {
        mockMvc.perform(get("/cursos")).andExpect(status().isOk());
        mockMvc.perform(get("/cursos/cohortes")).andExpect(status().isOk());
        mockMvc.perform(get("/cursos/categorias")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "lucia.fernandez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-02: el alumno ve sus cursos y no accede a la gestión de cursos")
    void alumnoVeSusCursos() throws Exception {
        mockMvc.perform(get("/cursos/mis-cursos")).andExpect(status().isOk())
                .andExpect(view().name("pages/cursos/cu-02-ver-mis-cursos"))
                .andExpect(model().attributeExists("inscripciones"));
        mockMvc.perform(get("/cursos")).andExpect(status().isForbidden());
    }
}
