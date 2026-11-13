package com.app.idoneos.controller;

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

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.EstadoClaseEnVivo;
import com.app.idoneos.repository.ClaseEnVivoRepository;
import com.app.idoneos.repository.CursoRepository;

/** Pruebas de las pantallas y los formularios del ABM de clases en vivo. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClaseEnVivoControllerTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String ADMIN = "admin@idoneos.online";
    private static final String VALENTINA = "valentina.ruiz@correo.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;

    private String curso() {
        return String.valueOf(cursoRepository.findAll().stream()
                .filter(c -> c.getNombre().equals("Mercado de Capitales Argentino")).findFirst().orElseThrow().getIdCurso());
    }

    private int clase(String estado) {
        return claseEnVivoRepository.findAll().stream().filter(c -> c.getEstadoClaseEnVivo().getNombre().equals(estado))
                .mapToInt(ClaseEnVivo::getIdClaseEnVivo).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El docente ve el listado con sus acciones, el formulario de alta y la ficha de una clase")
    void docenteVeLasPantallas() throws Exception {
        mockMvc.perform(get("/claseEnVivo/buscar").param("cursoId", curso())).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Programar clase")))
                .andExpect(content().string(Matchers.containsString("Cancelar seleccionada/s")))
                .andExpect(content().string(Matchers.containsString("Clase 1: Panorama del mercado de capitales")))
                .andExpect(content().string(Matchers.containsString("Fecha y hora")));
        // La ficha de la clase ya no existe: todo se hace desde el listado
        mockMvc.perform(get("/claseEnVivo/ver/" + clase(EstadoClaseEnVivo.PROGRAMADA))).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/claseEnVivo/modificar/" + clase(EstadoClaseEnVivo.PROGRAMADA))).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Editar clase en vivo")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El administrador ve el listado con la baja de las finalizadas y no puede programar ni cancelar")
    void administradorSoloDaDeBaja() throws Exception {
        mockMvc.perform(get("/claseEnVivo/buscar").param("cursoId", curso())).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Dar de baja seleccionada/s")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Programar clase"))));
        mockMvc.perform(post("/claseEnVivo/cancelar/" + clase(EstadoClaseEnVivo.PROGRAMADA))).andExpect(status().isForbidden());
        mockMvc.perform(post("/claseEnVivo/darDeBaja/" + clase(EstadoClaseEnVivo.FINALIZADA))).andExpect(status().isOk());
        // Cualquier clase se puede dar de baja, también una programada
        mockMvc.perform(post("/claseEnVivo/darDeBaja/" + clase(EstadoClaseEnVivo.PROGRAMADA))).andExpect(status().isOk());
        // Y las dadas de baja no aparecen en el listado
        mockMvc.perform(get("/claseEnVivo/buscar").param("cursoId", curso())).andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("Clase 0: Presentación del curso"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El docente cancela una clase programada y no puede dar de baja una finalizada")
    void docenteCancelaPeroNoDaDeBaja() throws Exception {
        mockMvc.perform(post("/claseEnVivo/cancelar/" + clase(EstadoClaseEnVivo.PROGRAMADA))).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Clase en vivo cancelada con éxito."));
        mockMvc.perform(post("/claseEnVivo/darDeBaja/" + clase(EstadoClaseEnVivo.FINALIZADA))).andExpect(status().isForbidden());
        mockMvc.perform(post("/claseEnVivo/cancelarMasiva")).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno no entra al ABM de clases en vivo")
    void alumnoNoEntra() throws Exception {
        mockMvc.perform(get("/claseEnVivo/buscar").param("cursoId", curso())).andExpect(status().isForbidden());
        mockMvc.perform(post("/claseEnVivo/cancelar/" + clase(EstadoClaseEnVivo.PROGRAMADA))).andExpect(status().isForbidden());
    }
}
