package com.app.idoneos.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.Cohorte.CohorteServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;

/**
 * Pruebas de la inscripción con el alumno que llega en la sesión del usuario (CU-45). A propósito NO son
 * transaccionales: en la aplicación real el alumno de la sesión está desconectado de Hibernate (sus colecciones no se
 * pueden leer) y dentro de una transacción de prueba ese problema no se ve. Cada prueba borra lo que crea.
 */
@SpringBootTest
@AutoConfigureMockMvc
class InscripcionConSesionControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private CohorteServicioImpl cohorteServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    private Cohorte cohorteAbierta(String curso) {
        Integer idPrograma = programaServicio.buscarPorCurso(cursoServicio.buscarPorNombre(curso).orElseThrow())
                .get(0).getIdPrograma();
        return cohorteServicio.buscarConFiltros(idPrograma, null, "Abierta", null, null, "recientes", null).get(0);
    }

    private void borrarInscripcionesDe(Cohorte cohorte, String correo) {
        List<Inscripcion> creadas = inscripcionRepositorio.findByCohorteAndBajaFalse(cohorte).stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(correo)).toList();
        inscripcionRepositorio.deleteAll(creadas);
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-45: el alumno se inscribe desde su sesión (con el alumno desconectado de Hibernate) y queda registrada")
    void inscribirseConElAlumnoDeLaSesion() throws Exception {
        Cohorte cohorte = cohorteAbierta("Macroeconomía de Coyuntura");
        try {
            mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", String.valueOf(cohorte.getIdCohorte()))
                    .param("terminos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value(containsString("Ya podés acceder")));

            List<Inscripcion> creadas = inscripcionRepositorio.findByCohorteAndBajaFalse(cohorte);
            assertEquals(1, creadas.size());
        } finally {
            borrarInscripcionesDe(cohorte, "valentina.ruiz@correo.com");
        }
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-45: al inscribirse a un curso con costo desde la sesión, la inscripción queda pendiente de pago")
    void inscribirseConCostoDesdeLaSesion() throws Exception {
        Cohorte cohorte = cohorteAbierta("Finanzas Personales e Inversión");
        try {
            mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", String.valueOf(cohorte.getIdCohorte()))
                    .param("terminos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value(containsString("completar el pago")));
        } finally {
            borrarInscripcionesDe(cohorte, "valentina.ruiz@correo.com");
        }
    }
}
