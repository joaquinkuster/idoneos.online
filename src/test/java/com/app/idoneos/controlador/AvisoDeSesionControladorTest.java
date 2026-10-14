package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.controlador.consejo.ContextoSesionConsejo;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * El aviso de inicio de sesión se muestra una sola vez, también en las pantallas del panel (que arman el navbar y
 * los scripts comunes por separado).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AvisoDeSesionControladorTest {

    @Autowired
    private MockMvc mockMvc;

    private int avisos(String url) throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute(ContextoSesionConsejo.MENSAJE_DE_SESION, "Sesión iniciada correctamente. ¡Bienvenido/a!");
        String html = mockMvc.perform(get(url).session(sesion)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();
        int cantidad = 0;
        for (int i = html.indexOf("mostrarExito("); i >= 0; i = html.indexOf("mostrarExito(", i + 1)) {
            cantidad++;
        }
        return cantidad;
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el aviso de inicio de sesión una sola vez")
    void administrador() throws Exception {
        assertEquals(1, avisos("/curso/misCursos"));
        assertEquals(1, avisos("/categoria/buscar"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno ve el aviso de inicio de sesión una sola vez")
    void alumno() throws Exception {
        assertEquals(1, avisos("/inscripcion/misCursos"));
    }
}
