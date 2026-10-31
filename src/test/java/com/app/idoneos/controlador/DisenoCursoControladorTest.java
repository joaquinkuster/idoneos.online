package com.app.idoneos.controlador;

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

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.NivelRepositorio;
import com.app.idoneos.servicio.Curso.DisenoCursoServicio;

/**
 * Pruebas de la vista Diseño de curso (el mapa de lo que el docente completa: datos del curso, temas, programa vigente y
 * contenido), del modal Editar curso y del estado que alimenta los indicadores del menú lateral.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DisenoCursoControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private NivelRepositorio nivelRepositorio;

    @Autowired
    private DisenoCursoServicio disenoCursoServicio;

    private Curso macro() {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow();
    }

    // ------------------------------------------------------------------ Estado del diseño

    @Test
    @DisplayName("El estado del diseño cuenta los temas, mira el programa vigente y lista lo que falta de contenido")
    void estadoDelDiseno() {
        Curso curso = macro();

        DisenoCursoServicio.EstadoDiseno estado = disenoCursoServicio.estadoDe(curso);

        assertEquals(curso.getProgramaVigente().getIdPrograma(), estado.programaVigente().getIdPrograma());
        assertEquals(curso.getProgramaVigente().getCantidadTemas(), estado.contenido().size());
        assertTrue(estado.isTemasCompletos());
        assertEquals(estado.contenido().stream().filter(c -> !c.getFaltantes().isEmpty()).count(),
                estado.getTemasIncompletos().size());
        assertTrue(disenoCursoServicio.columnasDeContenido(curso).contains("Material"));
        assertTrue(disenoCursoServicio.columnasDeContenido(curso).contains("Autoevaluación"));
    }

    @Test
    @DisplayName("Un curso sin los datos académicos no tiene el diseño completo y el estado dice qué falta")
    void datosIncompletos() {
        Curso curso = macro();
        curso.setObjetivos(null);

        DisenoCursoServicio.EstadoDiseno estado = disenoCursoServicio.estadoDe(curso);

        assertFalse(estado.isDatosCompletos());
        assertFalse(estado.isCompleto());
        assertFalse(estado.faltantesDeDatos().isEmpty());
    }

    // ------------------------------------------------------------------ Vista

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve el mapa del diseño con Editar curso y el menú lateral con los indicadores")
    void vistaDelDocente() throws Exception {
        mockMvc.perform(get("/diseno/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Datos del curso")))
                .andExpect(content().string(Matchers.containsString("Programa vigente")))
                .andExpect(content().string(Matchers.containsString("Editar curso")))
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarCurso\"")))
                .andExpect(content().string(Matchers.containsString("/curso/completarDatos/" + macro().getIdCurso())))
                // el menú: Diseño de curso (con Temas y Programas) y Dictado (con Cohortes)
                .andExpect(content().string(Matchers.containsString("pn-menu-estado")))
                .andExpect(content().string(Matchers.containsString("Dictado")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Cronograma"))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador consulta el diseño pero no lo edita")
    void vistaDelAdministrador() throws Exception {
        mockMvc.perform(get("/diseno/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Datos del curso")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-bs-target=\"#modalEditarCurso\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalEditarCurso\""))));
        mockMvc.perform(post("/curso/completarDatos/" + macro().getIdCurso())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al diseño del curso")
    void noEsParaAlumnos() throws Exception {
        mockMvc.perform(get("/diseno/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Editar curso

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Editar curso guarda nivel, descripción, objetivos y carga horaria, y rechaza lo que falta")
    void completarDatos() throws Exception {
        Curso curso = macro();
        int nivel = nivelRepositorio.findAll().get(0).getIdNivel();
        String url = "/curso/completarDatos/" + curso.getIdCurso();

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
