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
import com.app.idoneos.repositorio.InscripcionRepositorio;

/**
 * Pruebas de la pantalla de progreso de los alumnos (CU-55, MOD-F-03).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProgresoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private Inscripcion inscripcionDeMercado() {
        return inscripcionRepositorio.findByBajaFalse().stream()
                .filter(i -> i.getCurso().getNombre().equals("Mercado de Capitales Argentino")
                        && i.getUnidadesCompletadas() > 0)
                .findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-55: el docente ve el avance de los alumnos del curso, con el filtro por alumno en la pantalla")
    void docenteVeElProgreso() throws Exception {
        Inscripcion inscripcion = inscripcionDeMercado();
        mockMvc.perform(get("/progreso/buscar").param("cursoId", String.valueOf(inscripcion.getCurso().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/progreso/buscar"))
                .andExpect(model().attributeExists("inscripciones"))
                // el docente entra con el filtro en su cohorte de trabajo
                .andExpect(model().attribute("cohorteSeleccionada", Matchers.notNullValue()))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Progreso de los alumnos"),
                        Matchers.containsString(inscripcion.getAlumno().getUsuario().getNombreCompleto()),
                        Matchers.containsString("Unidades completadas"),
                        Matchers.containsString("pn-btn-ver"),
                        Matchers.containsString("data-filtro-front"),
                        Matchers.containsString("data-campo=\"alumno\""),
                        Matchers.containsString("data-f-alumno"),
                        Matchers.containsString("name=\"cohorte\""),
                        Matchers.containsString("data-f-cohorte"),
                        Matchers.not(Matchers.containsString(">Filtrar<")))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-55: el administrador también ve el progreso de cualquier curso; sin curso vuelve al listado")
    void administradorVeElProgreso() throws Exception {
        Inscripcion inscripcion = inscripcionDeMercado();
        mockMvc.perform(get("/progreso/buscar").param("cursoId", String.valueOf(inscripcion.getCurso().getIdCurso())))
                .andExpect(status().isOk())
                // el administrador ve todas las cohortes
                .andExpect(model().attribute("cohorteSeleccionada", Matchers.nullValue()))
                .andExpect(content().string(Matchers.containsString(
                        inscripcion.getAlumno().getUsuario().getNombreCompleto())));
        mockMvc.perform(get("/progreso/buscar")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-55: debajo de cada alumno se despliega la tabla de unidades, con si fue completada y la fecha")
    void detalleDebajoDeCadaAlumno() throws Exception {
        Inscripcion inscripcion = inscripcionDeMercado();
        mockMvc.perform(get("/progreso/buscar").param("cursoId", String.valueOf(inscripcion.getCurso().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("detalles"))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("data-progreso-detalle"),
                        Matchers.containsString("pn-fila-detalle"),
                        Matchers.containsString("Fecha en que se completó"),
                        Matchers.containsString(inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()
                                .get(0).getUnidad().getTitulo()),
                        Matchers.containsString("pn-btn-ver"))));
    }

    @Test
    @WithUserDetails(value = "lucia.fernandez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-55: el alumno no entra a la pantalla de progreso de los alumnos")
    void alumnoNoEntra() throws Exception {
        Inscripcion inscripcion = inscripcionDeMercado();
        mockMvc.perform(get("/progreso/buscar").param("cursoId", String.valueOf(inscripcion.getCurso().getIdCurso())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CU-55: sin sesión se pide iniciar sesión")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/progreso/buscar")).andExpect(status().is3xxRedirection());
    }
}
