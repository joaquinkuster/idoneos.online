package com.app.idoneos.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.servicio.Descuento.DescuentoServicioImpl;

/**
 * Pruebas de las pantallas de descuentos (CU-56 a CU-59): solo las ve el Administrador.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DescuentoControladorTest {

    private static final String ADMIN = "admin@idoneos.online";
    private static final String DOCENTE = "fausto.spotorno@idoneos.online";
    private static final String ALUMNO = "valentina.ruiz@correo.com";
    private static final String SERVICIO = "usuarioDetallesServicio";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DescuentoServicioImpl descuentoServicio;

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-56: el administrador ve el listado completo con filtros, columnas y modales")
    void buscarDescuentos() throws Exception {
        mockMvc.perform(get("/descuento/buscar")).andExpect(status().isOk())
                .andExpect(view().name("pages/descuento/buscar"))
                .andExpect(model().attribute("menuActivo", "descuentos"))
                .andExpect(model().attribute("descuentos", org.hamcrest.Matchers.hasSize(5)))
                .andExpect(content().string(containsString("data-filtro-front")))
                .andExpect(content().string(containsString("Alumno frecuente")))
                .andExpect(content().string(containsString("Haber comprado 2 cursos")))
                .andExpect(content().string(containsString("modalNuevoDescuento")))
                .andExpect(content().string(containsString("modalEditarDescuento")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-56: el menú lateral del administrador tiene el enlace a Descuentos")
    void menuConDescuentos() throws Exception {
        mockMvc.perform(get("/descuento/buscar"))
                .andExpect(content().string(containsString("/descuento/buscar")))
                .andExpect(content().string(containsString("Descuentos")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-57: registra un descuento y responde en JSON")
    void registrarDescuento() throws Exception {
        mockMvc.perform(post("/descuento/registrar").param("nombre", "Nuevo").param("porcentaje", "12.5")
                .param("vigenciaDesde", LocalDate.now().toString())
                .param("vigenciaHasta", LocalDate.now().plusDays(10).toString())
                .param("cantidadLimite", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("Nuevo")));

        assertTrue(descuentoServicio.buscarPorNombre("Nuevo").isPresent());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-57: informa los campos obligatorios que faltan")
    void registrarDescuentoIncompleto() throws Exception {
        mockMvc.perform(post("/descuento/registrar").param("nombre", "Incompleto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("porcentaje")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-58: modifica un descuento")
    void modificarDescuento() throws Exception {
        int id = descuentoServicio.buscarPorNombre("Promo de verano").orElseThrow().getIdDescuento();

        mockMvc.perform(post("/descuento/modificar/" + id).param("nombre", "Promo de invierno").param("porcentaje", "18")
                .param("vigenciaDesde", LocalDate.now().toString())
                .param("vigenciaHasta", LocalDate.now().plusDays(10).toString())
                .param("cantidadLimite", "30").param("cursosRequeridos", "1"))
                .andExpect(status().isOk());

        assertTrue(descuentoServicio.buscarPorNombre("Promo de invierno").isPresent());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-59: da de baja un descuento sin usos y rechaza uno ya aplicado")
    void darDeBajaDescuento() throws Exception {
        int promo = descuentoServicio.buscarPorNombre("Promo de verano").orElseThrow().getIdDescuento();
        int lanzamiento = descuentoServicio.buscarPorNombre("Lanzamiento").orElseThrow().getIdDescuento();

        mockMvc.perform(post("/descuento/darDeBaja/" + lanzamiento))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("8 inscripciones")));
        mockMvc.perform(post("/descuento/darDeBaja/" + promo)).andExpect(status().isOk());

        assertTrue(descuentoServicio.buscarPorNombre("Promo de verano").orElseThrow().esInactivo());
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("La baja masiva exige al menos un descuento seleccionado")
    void bajaMasivaSinSeleccion() throws Exception {
        mockMvc.perform(post("/descuento/darDeBajaMasiva")).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = DOCENTE, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("El docente no accede a los descuentos")
    void docenteNoAccede() throws Exception {
        mockMvc.perform(get("/descuento/buscar")).andExpect(status().isForbidden());
        mockMvc.perform(post("/descuento/darDeBaja/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = ALUMNO, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("El alumno no accede a los descuentos")
    void alumnoNoAccede() throws Exception {
        mockMvc.perform(get("/descuento/buscar")).andExpect(status().isForbidden());
        mockMvc.perform(post("/descuento/registrar")).andExpect(status().isForbidden());
    }
}
