package com.app.idoneos.controller;

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

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.service.cohorte.CohorteServiceImpl;
import com.app.idoneos.service.curso.CursoServiceImpl;
import com.app.idoneos.service.programa.ProgramaServiceImpl;

/**
 * Pruebas de la inscripción con el alumno que llega en la sesión del usuario (CU-45). A propósito NO son
 * transaccionales: en la aplicación real el alumno de la sesión está desconectado de Hibernate (sus colecciones no se
 * pueden leer) y dentro de una transacción de prueba ese problema no se ve. Cada prueba borra lo que crea.
 */
@SpringBootTest
@AutoConfigureMockMvc
class InscripcionConSesionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CohorteServiceImpl cohorteService;

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private ProgramaServiceImpl programaService;

    private Cohorte cohorteAbierta(String curso) {
        Integer idPrograma = programaService.buscarPorCurso(cursoService.buscarPorNombre(curso).orElseThrow())
                .get(0).getIdPrograma();
        return cohorteService.buscarConFiltros(idPrograma, null, "Abierta", null, null, "recientes", null).get(0);
    }

    private void borrarInscripcionesDe(Cohorte cohorte, String correo) {
        List<Inscripcion> creadas = inscripcionRepository.findByCohorteAndBajaFalse(cohorte).stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(correo)).toList();
        inscripcionRepository.deleteAll(creadas);
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-45: el alumno se inscribe desde su sesión (con el alumno desconectado de Hibernate) y queda registrada")
    void inscribirseConElAlumnoDeLaSesion() throws Exception {
        Cohorte cohorte = cohorteAbierta("Macroeconomía de Coyuntura");
        try {
            mockMvc.perform(post("/inscripcion/inscribir").param("cohorteId", String.valueOf(cohorte.getIdCohorte()))
                    .param("terminos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensaje").value(containsString("Ya podés acceder")));

            List<Inscripcion> creadas = inscripcionRepository.findByCohorteAndBajaFalse(cohorte);
            assertEquals(1, creadas.size());
        } finally {
            borrarInscripcionesDe(cohorte, "valentina.ruiz@correo.com");
        }
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesService")
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
