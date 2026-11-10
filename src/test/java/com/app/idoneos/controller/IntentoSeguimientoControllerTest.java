package com.app.idoneos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.IntentoAutoevaluacionRepository;

/** Pruebas de las pantallas de seguimiento: intentos y calificaciones. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IntentoSeguimientoControllerTest {

    private static final String VALENTINA = "valentina.ruiz@correo.com";
    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String ADMIN = "admin@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private IntentoAutoevaluacionRepository intentoRepository;

    private Inscripcion valentina() {
        return inscripcionRepository.findByBajaFalse().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(VALENTINA)
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
    }

    private String cursoId() {
        return String.valueOf(valentina().getCurso().getIdCurso());
    }

    private int intentoConNota(float nota) {
        return intentoRepository.findByInscripcionAndBajaFalse(valentina()).stream()
                .filter(i -> i.getNota() != null && i.getNota() == nota)
                .mapToInt(IntentoAutoevaluacion::getIdIntentoAutoevaluacion).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-68: el docente ve los intentos con el filtro en su cohorte y la opción de aprobar, no la de dar de baja")
    void docenteVeLosIntentos() throws Exception {
        mockMvc.perform(get("/intento/buscar").param("cursoId", cursoId()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/intento/buscar"))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Intentos de autoevaluación"),
                        Matchers.containsString("Valentina Ruiz"),
                        Matchers.containsString("data-campo=\"resultado\""),
                        Matchers.containsString("name=\"cohorte\""),
                        Matchers.containsString("Aprobar intento"),
                        Matchers.not(Matchers.containsString("Dar de baja")),
                        Matchers.not(Matchers.containsString("Revisar")))));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-68 y CU-72: el administrador ve los intentos y puede darlos de baja, no aprobarlos")
    void administradorVeLosIntentos() throws Exception {
        mockMvc.perform(get("/intento/buscar").param("cursoId", cursoId()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Valentina Ruiz"),
                        Matchers.containsString("Dar de baja"),
                        Matchers.not(Matchers.containsString("Aprobar intento")))));

        mockMvc.perform(post("/intento/aprobar/" + intentoConNota(4f))).andExpect(status().isForbidden());

        mockMvc.perform(post("/intento/darDeBaja/" + intentoConNota(8f)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("volvió a estar pendiente")));
        mockMvc.perform(post("/intento/darDeBaja/" + intentoConNota(4f)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Intento dado de baja con éxito."));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-71: el docente aprueba un intento; el administrador no puede dar de baja desde su cuenta")
    void docenteApruebaUnIntento() throws Exception {
        mockMvc.perform(post("/intento/aprobar/" + intentoConNota(4f)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Intento aprobado con éxito."));
        mockMvc.perform(post("/intento/aprobar/" + intentoConNota(4f)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/intento/darDeBaja/" + intentoConNota(8f))).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno no accede al seguimiento de intentos ni a la matriz del curso")
    void alumnoNoAccede() throws Exception {
        mockMvc.perform(get("/intento/buscar").param("cursoId", cursoId())).andExpect(status().isForbidden());
        mockMvc.perform(post("/intento/aprobar/" + intentoConNota(4f))).andExpect(status().isForbidden());
        mockMvc.perform(get("/intento/buscarPorAutoevaluacion").param("cursoId", cursoId())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-69: el docente ve la matriz con la mejor nota de cada alumno")
    void docenteVeLaMatriz() throws Exception {
        mockMvc.perform(get("/intento/buscarPorAutoevaluacion").param("cursoId", cursoId()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/intento/listado"))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Resultados por autoevaluación"),
                        Matchers.containsString("Valentina Ruiz"),
                        Matchers.containsString("Autoevaluación Tema 1: Marco regulatorio"),
                        Matchers.containsString("8,00"),
                        Matchers.containsString("name=\"cohorte\""))));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-69: el alumno ve solo sus calificaciones")
    void alumnoVeSusCalificaciones() throws Exception {
        mockMvc.perform(get("/intento/buscarPorInscripcion/" + valentina().getIdInscripcion()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/intento/listado"))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Mis calificaciones"),
                        Matchers.containsString("Autoevaluación Tema 1: Marco regulatorio"),
                        Matchers.containsString("Resultados por autoevaluación"),
                        Matchers.containsString("Tema 1"),
                        Matchers.containsString("8,00"),
                        Matchers.containsString("Aprobada"),
                        Matchers.containsString("Sin rendir"))));

        // La inscripción de otro alumno no se puede abrir
        Inscripcion ajena = inscripcionRepository.findByBajaFalse().stream()
                .filter(i -> !i.getAlumno().getUsuario().getCorreo().equals(VALENTINA)).findFirst().orElseThrow();
        mockMvc.perform(get("/intento/buscarPorInscripcion/" + ajena.getIdInscripcion())).andExpect(status().is3xxRedirection());
    }
}
