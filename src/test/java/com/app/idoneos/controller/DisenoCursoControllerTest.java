package com.app.idoneos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.app.idoneos.model.Curso;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.NivelRepository;
import com.app.idoneos.service.curso.CursoService;

/**
 * Pruebas de la vista Diseño de curso (el mapa de lo que el docente completa: datos del curso, temas, programa vigente y
 * contenido), del modal Editar curso y del estado que alimenta los indicadores del menú lateral.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DisenoCursoControllerTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private NivelRepository nivelRepository;

    @Autowired
    private CursoService cursoService;

    private Curso macro() {
        return cursoRepository.findAll().stream().filter(c -> c.getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow();
    }

    // ------------------------------------------------------------------ Estado del diseño

    @Test
    @DisplayName("El estado del diseño cuenta los temas, mira el programa vigente y lista lo que falta de contenido")
    void estadoDelDiseno() {
        Curso curso = macro();

        CursoService.EstadoDiseno estado = cursoService.calcularEstadoDiseno(curso);

        assertEquals(curso.getProgramaVigente().getIdPrograma(), estado.programaVigente().getIdPrograma());
        assertEquals(curso.getProgramaVigente().getCantidadTemas(), estado.contenido().size());
        assertTrue(estado.isTemasCompletos());
        assertEquals(estado.contenido().stream().filter(c -> !c.getFaltantes().isEmpty()).count(),
                estado.getTemasIncompletos().size());
        assertTrue(cursoService.buscarColumnasDeContenido(curso).contains("Material"));
        assertTrue(cursoService.buscarColumnasDeContenido(curso).contains("Autoevaluación"));
    }

    @Test
    @DisplayName("Un curso sin los datos académicos no tiene el diseño completo y el estado dice qué falta")
    void datosIncompletos() {
        Curso curso = macro();
        curso.setObjetivos(null);

        CursoService.EstadoDiseno estado = cursoService.calcularEstadoDiseno(curso);

        assertFalse(estado.isDatosCompletos());
        assertFalse(estado.isCompleto());
        assertFalse(estado.faltantesDeDatos().isEmpty());
    }

    // ------------------------------------------------------------------ Inicio del curso

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El inicio del curso muestra la card con el estado de la cohorte, Datos del curso como ventana y el menú nuevo")
    void inicioDelDocente() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + macro().getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("ec-carta")))
                .andExpect(content().string(Matchers.containsString("Diseño del curso")))
                .andExpect(content().string(Matchers.containsString("id=\"modalModificarCurso\"")))
                .andExpect(content().string(Matchers.containsString("/curso/modificarPorDocente/" + macro().getIdCurso())))
                // el menú: Programas, Temas y Cohortes, habilitados según el paso del diseño; sin la página Diseño
                .andExpect(content().string(Matchers.containsString("Programas")))
                .andExpect(content().string(Matchers.containsString("Cohortes")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Diseño de curso"))));
        mockMvc.perform(get("/diseno/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Con los datos del curso incompletos el paso actual es el 1, pero el menú se habilita por lo ya hecho (programa y temas)")
    void menuSeHabilitaPorLoYaHecho() throws Exception {
        Curso curso = macro();
        curso.setObjetivos(null);
        assertEquals(1, cursoService.calcularEstadoDiseno(curso, (com.app.idoneos.model.Cohorte) null).getPasoActual());
        // la línea de tiempo marca el programa y los temas como hechos: el menú los habilita aunque los datos sigan pendientes
        assertTrue(cursoService.calcularEstadoDiseno(curso, (com.app.idoneos.model.Cohorte) null).getNivelDeMenu() >= 3);

        mockMvc.perform(get("/curso/acceder/" + curso.getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("title=\"Cohortes\"")))
                .andExpect(content().string(Matchers.containsString("title=\"Programas\"")))
                .andExpect(content().string(Matchers.containsString("title=\"Temas\"")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El administrador ve el estado de la cohorte pero no edita los datos del curso")
    void inicioDelAdministrador() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + macro().getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("ec-carta")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-bs-target=\"#modalModificarCurso\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalModificarCurso\""))));
        mockMvc.perform(post("/curso/modificarPorDocente/" + macro().getIdCurso())).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Editar curso

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Editar curso guarda nivel, descripción, objetivos y carga horaria, y rechaza lo que falta")
    void completarDatos() throws Exception {
        Curso curso = macro();
        int nivel = nivelRepository.findAll().get(0).getIdNivel();
        String url = "/curso/modificarPorDocente/" + curso.getIdCurso();

        mockMvc.perform(post(url).param("nivelId", String.valueOf(nivel)).param("descripcion", "  Una descripción ")
                .param("objetivos", " Entender la coyuntura ").param("cargaHoraria", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Datos del curso guardados correctamente."));

        assertEquals("Una descripción", curso.getDescripcion());
        assertEquals("Entender la coyuntura", curso.getObjetivos());
        assertEquals(600, curso.getCargaHorariaMinutos());
        assertEquals(nivel, curso.getNivel().getIdNivel());

        mockMvc.perform(post(url).param("objetivos", "Objetivos"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("nivelId"));
        mockMvc.perform(post(url).param("nivelId", String.valueOf(nivel)).param("objetivos", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("objetivos"));
    }
}
