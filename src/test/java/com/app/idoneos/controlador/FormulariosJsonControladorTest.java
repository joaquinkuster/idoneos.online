package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de las respuestas JSON de los formularios de categorías y cohortes: el resultado se muestra
 * en el propio formulario, sin recargar la página.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FormulariosJsonControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Registrar una categoría responde en JSON con el mensaje de éxito")
    void registrarCategoriaResponde200() throws Exception {
        mockMvc.perform(post("/categoria/registrar").param("nombre", "Bonos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Bonos")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un error de validación responde 400 con el mensaje para la alerta del formulario")
    void errorDeValidacionResponde400() throws Exception {
        mockMvc.perform(post("/categoria/registrar").param("nombre", "Macroeconomía"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Ya existe una categoría activa")));
        mockMvc.perform(post("/curso/registrar").param("nombre", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("campos obligatorios")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Dar de baja responde en JSON, y una cohorte inexistente informa el error")
    void darDeBajaResponde() throws Exception {
        mockMvc.perform(post("/cohorte/darDeBaja/99999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no se encuentra activa")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Los listados se ordenan por nombre por defecto y aceptan el orden más reciente")
    void listadosAceptanElOrden() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(status().isOk());
        mockMvc.perform(get("/curso/buscar").param("orden", "recientes")).andExpect(status().isOk());
        mockMvc.perform(get("/categoria/buscar").param("orden", "recientes")).andExpect(status().isOk());
        mockMvc.perform(get("/cohorte/buscar").param("orden", "curso")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un valor numérico que no se puede interpretar informa el error del caso de uso en lugar de un error interno")
    void numeroDemasiadoGrande() throws Exception {
        mockMvc.perform(post("/cohorte/registrar").param("programaId", "1").param("semanasAcceso", "100000000000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no pueden superar las 100 semanas")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Abrir desde el navegador una operación que solo admite POST vuelve al inicio con un aviso")
    void operacionSoloPostAbiertaConGet() throws Exception {
        mockMvc.perform(get("/cohorte/registrar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/inicio"));
    }
}
