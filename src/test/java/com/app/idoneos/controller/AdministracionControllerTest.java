package com.app.idoneos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Pruebas de las pantallas de administración (auditoría, reportes, estadísticas y configuración): solo las ve el Administrador. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdministracionControllerTest {

    private static final String ADMIN = "admin@idoneos.online";
    private static final String DOCENTE = "fausto.spotorno@idoneos.online";
    private static final String ALUMNO = "valentina.ruiz@correo.com";
    private static final String SERVICIO = "usuarioDetallesService";

    /** Las pantallas de administración que se revisan. */
    private static final List<String> PANTALLAS = List.of("/auditoria/buscar", "/reporte/buscar", "/estadisticas", "/configuracion/buscar");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("CU-103, CU-113 a CU-116: el administrador accede a auditoría, reportes, estadísticas y configuración")
    void administradorAccede() throws Exception {
        for (String pantalla : PANTALLAS) {
            mockMvc.perform(get(pantalla)).andExpect(status().isOk());
        }
    }

    @Test
    @WithUserDetails(value = DOCENTE, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("El docente no accede a las pantallas de administración")
    void docenteNoAccede() throws Exception {
        for (String pantalla : PANTALLAS) {
            mockMvc.perform(get(pantalla)).andExpect(status().isForbidden());
        }
    }

    @Test
    @WithUserDetails(value = ALUMNO, userDetailsServiceBeanName = SERVICIO)
    @DisplayName("El alumno no accede a las pantallas de administración")
    void alumnoNoAccede() throws Exception {
        for (String pantalla : PANTALLAS) {
            mockMvc.perform(get(pantalla)).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("El visitante sin sesión es redirigido al inicio de sesión")
    void visitanteVaAlLogin() throws Exception {
        for (String pantalla : PANTALLAS) {
            mockMvc.perform(get(pantalla)).andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }
}
