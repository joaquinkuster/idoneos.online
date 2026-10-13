package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;

/**
 * Pruebas de la lista de participantes (CU-25 y la sección de participantes del CU-27, MOD-F-02).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParticipanteControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private int cursoDeMercado() {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals("Mercado de Capitales Argentino"))
                .mapToInt(c -> c.getIdCurso()).findFirst().orElseThrow();
    }

    private int inscripcionDe(String correo) {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(correo))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-25: el docente ve el equipo docente y los alumnos del curso, con filtros que se aplican en la pantalla")
    void docenteVeLosParticipantes() throws Exception {
        mockMvc.perform(get("/participante/buscar").param("cursoId", String.valueOf(cursoDeMercado())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/participante/buscar"))
                .andExpect(model().attribute("soloCohorte", false))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Participantes"),
                        Matchers.containsString("Docente titular"),
                        Matchers.containsString("Alumno"),
                        // filtros en el navegador: sin botón "Filtrar"
                        Matchers.containsString("data-filtro-front"),
                        Matchers.containsString("name=\"cohorte\""),
                        Matchers.containsString("data-f-rol"),
                        Matchers.not(Matchers.containsString(">Filtrar<")))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-25: el administrador también ve los participantes de cualquier curso, dentro del panel del curso")
    void administradorVeLosParticipantes() throws Exception {
        mockMvc.perform(get("/participante/buscar").param("cursoId", String.valueOf(cursoDeMercado())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Este programa")));
        mockMvc.perform(get("/participante/buscar")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el alumno ve el equipo docente y los alumnos de su cohorte, sin filtro ni columna de cohorte")
    void alumnoVeLosDeSuCohorte() throws Exception {
        mockMvc.perform(get("/participante/ver/" + inscripcionDe("valentina.ruiz@correo.com")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("soloCohorte", true))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Docente titular"),
                        Matchers.containsString("Valentina"),
                        Matchers.not(Matchers.containsString("name=\"cohorte\"")))));
        // el alumno no entra a la vista del docente
        mockMvc.perform(get("/participante/buscar").param("cursoId", String.valueOf(cursoDeMercado())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente no entra a la vista del alumno")
    void docenteNoEntraALaVistaDelAlumno() throws Exception {
        mockMvc.perform(get("/participante/ver/1")).andExpect(status().isForbidden());
    }
}
