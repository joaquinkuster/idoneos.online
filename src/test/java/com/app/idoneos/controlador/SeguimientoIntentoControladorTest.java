package com.app.idoneos.controlador;

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

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;

/**
 * Pruebas de las pantallas de seguimiento: intentos (CU-68, CU-71, CU-72) y calificaciones (CU-69). Valentina Ruiz es la
 * alumna con intentos de 4,00 y 8,00 en la autoevaluación de la unidad 1.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguimientoIntentoControladorTest {

    private static final String VALENTINA = "valentina.ruiz@correo.com";
    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String ADMIN = "admin@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private IntentoAutoevaluacionRepositorio intentoRepositorio;

    private Inscripcion valentina() {
        return inscripcionRepositorio.findByBajaFalse().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(VALENTINA)
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
    }

    private String cursoId() {
        return String.valueOf(valentina().getCurso().getIdCurso());
    }

    private int intentoConNota(float nota) {
        return intentoRepositorio.findByInscripcionAndBajaFalse(valentina()).stream()
                .filter(i -> i.getNota() != null && i.getNota() == nota)
                .mapToInt(IntentoAutoevaluacion::getIdIntentoAutoevaluacion).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
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
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesServicio")
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
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
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
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al seguimiento de intentos ni a la matriz del curso")
    void alumnoNoAccede() throws Exception {
        mockMvc.perform(get("/intento/buscar").param("cursoId", cursoId())).andExpect(status().isForbidden());
        mockMvc.perform(post("/intento/aprobar/" + intentoConNota(4f))).andExpect(status().isForbidden());
        mockMvc.perform(get("/calificacion/buscar").param("cursoId", cursoId())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-69: el docente ve la matriz con la mejor nota de cada alumno")
    void docenteVeLaMatriz() throws Exception {
        mockMvc.perform(get("/calificacion/buscar").param("cursoId", cursoId()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/calificacion/listado"))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Resultados por autoevaluación"),
                        Matchers.containsString("Valentina Ruiz"),
                        Matchers.containsString("Autoevaluación Unidad 1: Marco regulatorio"),
                        Matchers.containsString("8,00"),
                        Matchers.containsString("name=\"cohorte\""))));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-69: el alumno ve solo sus calificaciones")
    void alumnoVeSusCalificaciones() throws Exception {
        mockMvc.perform(get("/calificacion/ver/" + valentina().getIdInscripcion()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/calificacion/listado"))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Mis calificaciones"),
                        Matchers.containsString("Autoevaluación Unidad 1: Marco regulatorio"),
                        Matchers.containsString("Resultados por autoevaluación"),
                        Matchers.containsString("Unidad 1"),
                        Matchers.containsString("8,00"),
                        Matchers.containsString("Aprobada"),
                        Matchers.containsString("Sin rendir"))));

        // La inscripción de otro alumno no se puede abrir
        Inscripcion ajena = inscripcionRepositorio.findByBajaFalse().stream()
                .filter(i -> !i.getAlumno().getUsuario().getCorreo().equals(VALENTINA)).findFirst().orElseThrow();
        mockMvc.perform(get("/calificacion/ver/" + ajena.getIdInscripcion())).andExpect(status().is3xxRedirection());
    }
}
