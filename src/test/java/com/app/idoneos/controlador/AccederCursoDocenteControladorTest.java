package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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

import com.app.idoneos.repositorio.CursoRepositorio;

/**
 * Pruebas de la pantalla "Acceder curso" del docente (CU-27, MOD-F-02).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccederCursoDocenteControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    private int idCurso(String nombre) {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow()
                .getIdCurso();
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el docente ve todas las unidades habilitadas, con su contenido y sin avance")
    void docenteVeLasUnidadesSinBloqueos() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Mercado de Capitales Argentino")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Panel docente")))
                .andExpect(content().string(Matchers.containsString("Introducción al mercado de capitales")))
                .andExpect(content().string(Matchers.containsString("Renta variable y opciones")))
                .andExpect(content().string(Matchers.containsString("Autoevaluación Unidad 1: Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("En directo ahora")))
                .andExpect(content().string(Matchers.containsString("Este curso")))
                // sin bloqueos, sin avance del alumno ni intentos propios
                .andExpect(content().string(Matchers.not(Matchers.containsString("Completá la unidad anterior"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Bloqueada"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Sin intentos"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Acceso hasta"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el cronograma del docente muestra la semana de la cohorte pero no el atraso del alumno")
    void docenteVeElCronogramaSinAtraso() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Mercado de Capitales Argentino"))
                .param("seccion", "cronograma"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Semana actual de la cohorte: 3 de 9")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Vas al día"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Vas por detrás"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el docente ve las clases en vivo de la cohorte")
    void docenteVeLasClasesEnVivo() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Mercado de Capitales Argentino")).param("seccion", "clases"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Clase 1: Panorama del mercado de capitales")));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: un docente no puede acceder a un curso en el que no participa")
    void docenteAjenoAlCurso() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Macroeconomía de Coyuntura")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"))
                .andExpect(flash().attribute("error", Matchers.containsString("No participás en este curso")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el alumno no puede usar el acceso del docente")
    void alumnoNoAccedeComoDocente() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Mercado de Capitales Argentino")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El menú del docente ya no tiene la opción Mis cohortes")
    void menuDelDocenteSinMisCohortes() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("Mis cohortes"))))
                .andExpect(content().string(Matchers.containsString("/curso/acceder/")));
    }
}
