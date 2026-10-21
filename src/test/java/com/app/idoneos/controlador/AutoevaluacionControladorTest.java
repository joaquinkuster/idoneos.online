package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.PoolRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;

/**
 * Pruebas de las vistas y los formularios del ABM de autoevaluaciones (CU-64 a CU-67). Fausto Spotorno es titular de
 * "Mercado de Capitales Argentino": su autoevaluación de la unidad 1 tiene intentos y la de la unidad 2 no.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutoevaluacionControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String ADMIN = "admin@idoneos.online";
    private static final String VALENTINA = "valentina.ruiz@correo.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    @Autowired
    private PoolRepositorio poolRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    private String curso() {
        return String.valueOf(cursoRepositorio.findAll().stream()
                .filter(c -> c.getNombre().equals("Mercado de Capitales Argentino")).findFirst().orElseThrow().getIdCurso());
    }

    private int autoevaluacion(String nombre) {
        return autoevaluacionRepositorio.findAll().stream().filter(a -> a.getNombre().equals(nombre))
                .mapToInt(Autoevaluacion::getIdAutoevaluacion).findFirst().orElseThrow();
    }

    private int pool(String nombre) {
        return poolRepositorio.findAll().stream().filter(p -> p.getNombre().equals(nombre)).mapToInt(Pool::getIdPool)
                .findFirst().orElseThrow();
    }

    private int unidad(String titulo) {
        return unidadRepositorio.findAll().stream().filter(u -> u.getTitulo().equals(titulo)).mapToInt(Unidad::getIdUnidad)
                .findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-64: el docente ve el listado con sus columnas, el formulario de alta y los modales de cada autoevaluación")
    void listadoDelDocente() throws Exception {
        mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Autoevaluación Unidad 1: Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Evaluación final\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Intentos permitidos\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalNuevaAutoevaluacion\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarAutoevaluacion")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaAutoevaluacion")))
                .andExpect(content().string(Matchers.containsString("/autoevaluacion/darDeBajaMasiva")))
                .andExpect(content().string(Matchers.containsString("data-autoevaluacion-pools")))
                .andExpect(content().string(Matchers.containsString("ya registra intentos")))
                .andExpect(content().string(Matchers.containsString("/js/autoevaluacion.js")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el listado y puede dar de baja, pero no registrar ni modificar")
    void listadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Autoevaluación Unidad 2: Ejercicios de rendimiento")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalNuevaAutoevaluacion"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalEditarAutoevaluacion"))))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaAutoevaluacion")));

        mockMvc.perform(post("/autoevaluacion/registrar").param("unidadId", String.valueOf(unidad("Renta fija y bonos"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al ABM de autoevaluaciones")
    void elAlumnoNoAccede() throws Exception {
        mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-65 y CU-66: registrar y modificar responden en JSON e informan los errores con su campo")
    void registrarYModificar() throws Exception {
        String unidad = String.valueOf(unidad("Renta fija y bonos"));
        String pool = String.valueOf(pool("Renta fija"));
        mockMvc.perform(post("/autoevaluacion/registrar").param("unidadId", unidad).param("nombre", "Nueva autoevaluación")
                .param("tiempoLimite", "30").param("cantidadPreguntas", "3").param("notaMinima", "6")
                .param("fechaApertura", "2026-01-01T09:00").param("navegacionLibre", "true").param("idsPools", pool))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Autoevaluación 'Nueva autoevaluación' registrada con éxito."));

        mockMvc.perform(post("/autoevaluacion/registrar").param("unidadId", unidad).param("nombre", "Sin pools")
                .param("tiempoLimite", "30").param("cantidadPreguntas", "3").param("notaMinima", "6")
                .param("fechaApertura", "2026-01-01T09:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("idsPools"));

        mockMvc.perform(post("/autoevaluacion/registrar").param("unidadId", unidad).param("nombre", "Nota alta")
                .param("tiempoLimite", "30").param("cantidadPreguntas", "3").param("notaMinima", "12")
                .param("fechaApertura", "2026-01-01T09:00").param("idsPools", pool))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("notaMinima"))
                .andExpect(jsonPath("$.error").value(Matchers.containsString("mayor a 10")));

        mockMvc.perform(post("/autoevaluacion/modificar/" + autoevaluacion("Nueva autoevaluación"))
                .param("nombre", "Renombrada").param("tiempoLimite", "30").param("cantidadPreguntas", "3")
                .param("notaMinima", "6").param("fechaApertura", "2026-01-01T09:00").param("idsPools", pool))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Autoevaluación modificada correctamente."));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-67: dar de baja responde en JSON y se rechaza si la autoevaluación tiene intentos")
    void darDeBaja() throws Exception {
        mockMvc.perform(post("/autoevaluacion/darDeBaja/" + autoevaluacion("Autoevaluación Unidad 1: Marco regulatorio")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("intentos registrados")));

        mockMvc.perform(post("/autoevaluacion/darDeBajaMasiva").param("ids",
                String.valueOf(autoevaluacion("Autoevaluación Unidad 1: Marco regulatorio"))))
                .andExpect(status().isBadRequest());
    }
}
