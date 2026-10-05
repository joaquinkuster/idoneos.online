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

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.InscripcionRepositorio;

/**
 * Pruebas de la pantalla "Mis cursos" del alumno cuando no tiene cursos para mostrar (CU-02).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MisCursosVacioControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Test
    @WithUserDetails(value = "lucia.fernandez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-02: un alumno sin cursos ve un mensaje y un botón para explorar el catálogo")
    void alumnoSinCursos() throws Exception {
        for (Inscripcion inscripcion : inscripcionRepositorio.findAll()) {
            inscripcion.marcarInactivo();
        }
        inscripcionRepositorio.flush();

        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Todavía no tenés cursos")))
                .andExpect(content().string(Matchers.containsString("Explorar catálogo")));
    }

    @Test
    @WithUserDetails(value = "tomas.herrera@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-02: el alumno de la semilla sin inscripciones ve el mensaje y el botón del catálogo")
    void alumnoDeLaSemillaSinInscripciones() throws Exception {
        mockMvc.perform(get("/inscripcion/misCursos")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Todavía no tenés cursos")))
                .andExpect(content().string(Matchers.containsString("Explorar catálogo")));
    }

    @Test
    @WithUserDetails(value = "lucia.fernandez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-02: si los filtros no coinciden con ningún curso se avisa y se ofrece limpiarlos")
    void filtrosSinResultados() throws Exception {
        mockMvc.perform(get("/inscripcion/misCursos").param("busqueda", "zzz")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("No se encontraron cursos")))
                .andExpect(content().string(Matchers.containsString("Limpiar filtros")));
    }
}
