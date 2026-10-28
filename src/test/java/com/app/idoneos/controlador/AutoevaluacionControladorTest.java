package com.app.idoneos.controlador;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.PoolRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;

/**
 * Pruebas de las vistas y los formularios del ABM de autoevaluaciones (CU-64 a CU-67). Fausto Spotorno es titular de
 * "Mercado de Capitales Argentino": su autoevaluación del tema 1 tiene intentos y la del tema 2 no.
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
    private TemaRepositorio temaRepositorio;

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

    private int tema(String titulo) {
        return temaRepositorio.findAll().stream().filter(u -> u.getTitulo().equals(titulo)).mapToInt(Tema::getIdTema)
                .findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Desde la vista del curso, el docente modifica o da de baja cada autoevaluación con el mismo menú que los materiales")
    void menuDesdeLaVistaDelCurso() throws Exception {
        int id = autoevaluacion("Autoevaluación Tema 1: Marco regulatorio");
        mockMvc.perform(get("/curso/acceder/" + curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("aria-label=\"Opciones de la autoevaluación\"")))
                .andExpect(content().string(Matchers.containsString("accion=editar&amp;autoevaluacionId=" + id)))
                .andExpect(content().string(Matchers.containsString("accion=baja&amp;autoevaluacionId=" + id)));

        mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso()).param("accion", "editar")
                .param("autoevaluacionId", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalEditarAutoevaluacion" + id + "\"")));
        mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso()).param("accion", "baja")
                .param("autoevaluacionId", String.valueOf(id)))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalBajaAutoevaluacion" + id + "\"")));
        mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso()).param("accion", "editar")
                .param("autoevaluacionId", "999999"))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-modal=\"modal"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-64: el docente ve el listado con sus columnas, el formulario de alta y los modales de cada autoevaluación")
    void listadoDelDocente() throws Exception {
        mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Autoevaluación Tema 1: Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Intentos permitidos\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Fecha de creación\"")))
                .andExpect(content().string(Matchers.containsString("data-filtro=\"multilista\">Pools")))
                .andExpect(content().string(Matchers.containsString("data-f-pools=\"Marco regulatorio\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Evaluación final\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Intentos registrados\""))))
                .andExpect(content().string(Matchers.containsString("id=\"modalNuevaAutoevaluacion\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarAutoevaluacion")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaAutoevaluacion")))
                .andExpect(content().string(Matchers.containsString("/autoevaluacion/darDeBajaMasiva")))
                .andExpect(content().string(Matchers.containsString("data-autoevaluacion-pools")))
                .andExpect(content().string(Matchers.containsString("ya registra intentos")))
                .andExpect(content().string(Matchers.containsString("/js/autoevaluacion.js")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Los pools se ofrecen agrupados por tema: el propio y los anteriores, nunca los posteriores")
    void poolsQueSeOfrecenAlModificar() throws Exception {
        String pagina = mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso())).andReturn().getResponse()
                .getContentAsString();

        // Tema 1: solo su pool
        String modalTema1 = modal(pagina, autoevaluacion("Autoevaluación Tema 1: Marco regulatorio"));
        assertTrue(modalTema1.contains("Marco regulatorio (6 preguntas)"));
        assertFalse(modalTema1.contains("Renta fija (6 preguntas)"));
        assertFalse(modalTema1.contains("Preguntas del tema"));

        // Tema 2: su pool y el del tema 1, agrupados; no el del tema 3
        String modalTema2 = modal(pagina, autoevaluacion("Autoevaluación Tema 2: Ejercicios de rendimiento"));
        assertTrue(modalTema2.contains("U1 · Marco regulatorio (6 preguntas)"));
        assertTrue(modalTema2.contains("U2 · Renta fija (6 preguntas)"));
        assertTrue(modalTema2.contains("<optgroup label=\"1. Introducción al mercado de capitales\">"));
        assertTrue(modalTema2.contains("<optgroup label=\"2. Renta fija y bonos\">"));
        assertFalse(modalTema2.contains("Preguntas del tema"));

        // Tema 3 (el último): los de los tres temas
        String modalTema3 = modal(pagina, autoevaluacion("Autoevaluación del tema 3"));
        assertTrue(modalTema3.contains("U1 · Marco regulatorio (6 preguntas)"));
        assertTrue(modalTema3.contains("U2 · Renta fija (6 preguntas)"));
        assertTrue(modalTema3.contains("U3 · Preguntas del tema (4 preguntas)"));
    }

    /** El HTML de la ventana de modificación de una autoevaluación. */
    private String modal(String pagina, int idAutoevaluacion) {
        int desde = pagina.indexOf("id=\"modalEditarAutoevaluacion" + idAutoevaluacion + "\"");
        int hasta = pagina.indexOf("id=\"modalBajaAutoevaluacion" + idAutoevaluacion + "\"");
        assertTrue(desde >= 0 && hasta > desde);
        return pagina.substring(desde, hasta);
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el listado y puede dar de baja, pero no registrar ni modificar")
    void listadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/autoevaluacion/buscar").param("cursoId", curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Autoevaluación Tema 2: Ejercicios de rendimiento")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalNuevaAutoevaluacion"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalEditarAutoevaluacion"))))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaAutoevaluacion")));

        mockMvc.perform(post("/autoevaluacion/registrar").param("temaId", String.valueOf(tema("Renta fija y bonos"))))
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
        String tema = String.valueOf(tema("Renta fija y bonos"));
        String pool = String.valueOf(pool("Renta fija"));
        mockMvc.perform(post("/autoevaluacion/registrar").param("temaId", tema).param("nombre", "Nueva autoevaluación")
                .param("tiempoLimite", "30").param("cantidadPreguntas", "3").param("notaMinima", "6")
                .param("fechaApertura", "2026-01-01T09:00").param("navegacionLibre", "true").param("idsPools", pool))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Autoevaluación 'Nueva autoevaluación' registrada con éxito."));

        mockMvc.perform(post("/autoevaluacion/registrar").param("temaId", tema).param("nombre", "Sin pools")
                .param("tiempoLimite", "30").param("cantidadPreguntas", "3").param("notaMinima", "6")
                .param("fechaApertura", "2026-01-01T09:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("idsPools"));

        mockMvc.perform(post("/autoevaluacion/registrar").param("temaId", tema).param("nombre", "Nota alta")
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
        mockMvc.perform(post("/autoevaluacion/darDeBaja/" + autoevaluacion("Autoevaluación Tema 1: Marco regulatorio")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("intentos registrados")));

        mockMvc.perform(post("/autoevaluacion/darDeBajaMasiva").param("ids",
                String.valueOf(autoevaluacion("Autoevaluación Tema 1: Marco regulatorio"))))
                .andExpect(status().isBadRequest());
    }
}
