package com.app.idoneos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.TipoNotificacion;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.NotificacionRepository;
import com.app.idoneos.repository.TipoNotificacionRepository;
import com.app.idoneos.service.recordatoriocohorte.RecordatorioCohorteService;

/**
 * Pruebas de «Recordarme»: la ficha ofrece las cohortes próximas con su estado entre paréntesis, el alumno marca y quita el
 * recordatorio, y el sistema crea la notificación cuando faltan los días de anticipación.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecordatorioCohorteControllerTest {

    private static final String ALUMNA = "valentina.ruiz@correo.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private TipoNotificacionRepository tipoNotificacionRepository;

    @Autowired
    private RecordatorioCohorteService recordatorioService;

    private Curso tecnico() {
        return cursoRepository.findAll().stream().filter(c -> c.getNombre().equals("Análisis Técnico Bursátil")).findFirst().orElseThrow();
    }

    private Cohorte proxima() {
        return tecnico().getCohortes().stream().filter(c -> !c.getBaja() && com.app.idoneos.model.EstadoCohorte.PROXIMA.equals(c.getEstado())).findFirst().orElseThrow();
    }

    private Alumno alumna() {
        return alumnoRepository.findAll().stream().filter(a -> a.getUsuario().getCorreo().equals(ALUMNA)).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = ALUMNA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("La ficha ofrece la cohorte próxima con su estado entre paréntesis y el botón Recordarme")
    void fichaConCohorteProxima() throws Exception {
        mockMvc.perform(get("/catalogo/ficha/" + tecnico().getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("(Próxima)")))
                .andExpect(content().string(Matchers.containsString("data-recordar")))
                .andExpect(content().string(Matchers.containsString("Recordarme")));
    }

    @Test
    @WithUserDetails(value = ALUMNA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno marca y quita el recordatorio de una cohorte próxima")
    void recordarYQuitar() throws Exception {
        int id = proxima().getIdCohorte();

        mockMvc.perform(post("/recordatorio/recordar/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("te vamos a avisar")));
        assertTrue(recordatorioService.idsDeCohortesRecordadas(alumna()).contains(id));
        // marcarlo de nuevo no duplica el recordatorio
        mockMvc.perform(post("/recordatorio/recordar/" + id)).andExpect(status().isOk());

        mockMvc.perform(post("/recordatorio/quitar/" + id)).andExpect(status().isOk());
        assertFalse(recordatorioService.idsDeCohortesRecordadas(alumna()).contains(id));
        mockMvc.perform(post("/recordatorio/quitar/" + id)).andExpect(status().isBadRequest());

        // y se puede volver a marcar
        mockMvc.perform(post("/recordatorio/recordar/" + id)).andExpect(status().isOk());
        assertTrue(recordatorioService.idsDeCohortesRecordadas(alumna()).contains(id));
    }

    @Test
    @WithUserDetails(value = ALUMNA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Solo se puede pedir el recordatorio de una cohorte próxima")
    void soloCohortesProximas() throws Exception {
        Cohorte abierta = cursoRepository.findAll().stream().flatMap(c -> c.getCohortes().stream())
                .filter(c -> !c.getBaja() && com.app.idoneos.model.EstadoCohorte.ABIERTA.equals(c.getEstado())).findFirst().orElseThrow();

        mockMvc.perform(post("/recordatorio/recordar/" + abierta.getIdCohorte())).andExpect(status().isBadRequest());
        mockMvc.perform(post("/recordatorio/recordar/999999")).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El docente no pide recordatorios")
    void noEsParaDocentes() throws Exception {
        mockMvc.perform(post("/recordatorio/recordar/" + proxima().getIdCohorte())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cuando faltan los días de anticipación el sistema crea una sola notificación de recordatorio")
    void creaLaNotificacionUnaSolaVez() {
        Cohorte cohorte = proxima();
        // la inscripción abre en tres días: ya está dentro de la anticipación (7 días por defecto)
        cohorte.setFechaInicioInscripcion(LocalDateTime.now().plusDays(3));
        cohorte.setFechaFinInscripcion(LocalDateTime.now().plusDays(40));
        recordatorioService.recordar(alumna(), cohorte.getIdCohorte());
        TipoNotificacion tipo = tipoNotificacionRepository.findByNombre(TipoNotificacion.RECORDATORIO_COHORTE).orElseThrow();

        assertEquals(1, recordatorioService.crearNotificacionesPendientes());
        assertTrue(notificacionRepository.existsByUsuarioAndCohorteAndTipoNotificacion(alumna().getUsuario(), cohorte, tipo));
        assertEquals(0, recordatorioService.crearNotificacionesPendientes(), "avisa una sola vez");
    }

    @Test
    @DisplayName("Todavía no avisa si falta más que la anticipación, ni si el alumno quitó el recordatorio")
    void noAvisaFueraDeTiempoNiQuitado() {
        Cohorte cohorte = proxima();
        cohorte.setFechaInicioInscripcion(LocalDateTime.now().plusDays(30));
        cohorte.setFechaFinInscripcion(LocalDateTime.now().plusDays(60));
        if (cohorte.getFechaInicioDictado() != null) {
            cohorte.setFechaInicioDictado(LocalDateTime.now().plusDays(90));
            cohorte.setFechaFinDictado(LocalDateTime.now().plusDays(120));
        }
        recordatorioService.recordar(alumna(), cohorte.getIdCohorte());

        assertEquals(0, recordatorioService.crearNotificacionesPendientes(), "faltan 30 días");

        cohorte.setFechaInicioInscripcion(LocalDateTime.now().plusDays(2));
        recordatorioService.quitar(alumna(), cohorte.getIdCohorte());
        assertEquals(0, recordatorioService.crearNotificacionesPendientes(), "el alumno lo quitó");
    }
}
