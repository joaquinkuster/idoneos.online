package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
 * Pruebas de la pantalla "Mis cursos" del docente (CU-01), con el mismo diseño que la del alumno.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DocenteMisCursosControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-01: el docente ve sus cursos con el botón 'Acceder al curso' y el pie 'Cursos por página'")
    void docenteVeSusCursosConAccederAlCurso() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("PANEL DOCENTE")))
                .andExpect(content().string(Matchers.containsString("Mercado de Capitales Argentino")))
                .andExpect(content().string(Matchers.containsString("Acceder al curso")))
                .andExpect(content().string(Matchers.containsString("Cursos por página:")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Ver detalle"))))
                // sólo los cursos en los que participa: no aparece el curso de otro titular
                .andExpect(content().string(Matchers.not(Matchers.containsString("Planificación Fiscal Corporativa"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-01: si los filtros no coinciden con ningún curso se avisa y se ofrece limpiarlos")
    void filtrosSinResultados() throws Exception {
        mockMvc.perform(get("/curso/buscar").param("busqueda", "zzz")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("No se encontraron cursos")))
                .andExpect(content().string(Matchers.containsString("Limpiar filtros")));
    }
}
