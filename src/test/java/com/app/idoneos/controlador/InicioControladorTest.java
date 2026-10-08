package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de las páginas institucionales: inicio, acerca de y página de error.
 */
@SpringBootTest
@AutoConfigureMockMvc
class InicioControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("El inicio y acerca de son públicos")
    void paginasPublicas() throws Exception {
        mockMvc.perform(get("/inicio")).andExpect(status().isOk()).andExpect(view().name("index"));
        mockMvc.perform(get("/acercaDe")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("La página de error muestra un mensaje claro según el código de estado")
    void paginaDeError() throws Exception {
        mockMvc.perform(get("/error")).andExpect(view().name("pages/error"))
                .andExpect(content().string(Matchers.containsString("Página no encontrada")));
        mockMvc.perform(get("/error").requestAttr("jakarta.servlet.error.status_code", 403))
                .andExpect(content().string(Matchers.containsString("Acceso denegado")));
        mockMvc.perform(get("/error").requestAttr("jakarta.servlet.error.status_code", 500))
                .andExpect(content().string(Matchers.containsString("Ocurrió un error inesperado")));
    }
}
