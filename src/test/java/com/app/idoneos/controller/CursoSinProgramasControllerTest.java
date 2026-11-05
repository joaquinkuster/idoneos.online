package com.app.idoneos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.app.idoneos.model.Curso;
import com.app.idoneos.repository.CursoRepository;

/**
 * Pruebas de un curso que todavía no tiene programas (o los dio de baja todos): el docente igual puede abrirlo, porque
 * desde ahí registra el primero. "Macroeconomía de Coyuntura" tiene un solo programa, que estas pruebas dan de baja.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CursoSinProgramasControllerTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepository cursoRepository;

    /** Deja al curso sin programas ni cohortes activos. */
    private Curso cursoSinProgramas() {
        Curso curso = cursoRepository.findAll().stream().filter(c -> c.getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow();
        curso.getProgramas().forEach(programa -> programa.marcarInactivo());
        curso.getCohortes().forEach(cohorte -> cohorte.setBaja(true));
        return curso;
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El docente abre un curso sin programas y ve cómo registrar el primero")
    void accedeAlCursoSinProgramas() throws Exception {
        Curso curso = cursoSinProgramas();

        mockMvc.perform(get("/curso/acceder/" + curso.getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Este curso todavía no tiene cohortes")))
                .andExpect(content().string(Matchers.containsString("/programa/buscar?cursoId=" + curso.getIdCurso() + "&amp;nuevo=")))
                // sin programa no hay temas ni botón para registrarlas
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-tema-cabecera"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Las clases en vivo y el listado de programas también se abren en un curso sin programas")
    void otrasPaginasDelCurso() throws Exception {
        Curso curso = cursoSinProgramas();
        String id = String.valueOf(curso.getIdCurso());

        mockMvc.perform(get("/curso/acceder/" + id).param("seccion", "clases")).andExpect(status().isOk());
        mockMvc.perform(get("/programa/buscar").param("cursoId", id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-modal=\"modalPrograma\""))));
        mockMvc.perform(get("/recurso/buscar").param("cursoId", id)).andExpect(status().isOk());
        mockMvc.perform(get("/termino/buscar").param("cursoId", id)).andExpect(status().isOk());
        mockMvc.perform(get("/consulta/buscar").param("cursoId", id)).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El botón Registrar programa lleva al listado con el formulario de alta abierto")
    void registrarElPrimerPrograma() throws Exception {
        Curso curso = cursoSinProgramas();
        String id = String.valueOf(curso.getIdCurso());

        mockMvc.perform(get("/programa/buscar").param("cursoId", id).param("nuevo", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalPrograma\"")))
                .andExpect(content().string(Matchers.containsString("/js/pages/programa/programa.js")));

        mockMvc.perform(post("/programa/registrar").param("cursoId", id).param("nombre", "Primer programa")
                .param("objetivos", "Objetivos").param("bibliografia", "Bibliografía"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Primer programa")));

        // con el primer programa, el curso se ve con sus selectores y sin el aviso
        mockMvc.perform(get("/curso/acceder/" + id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString(">Registrar programa</a>"))))
                .andExpect(content().string(Matchers.containsString("Registrar tema")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Los temas se abren sin programas (son del curso) y el cronograma se ve vacío")
    void temasYCronogramaVacio() throws Exception {
        Curso curso = cursoSinProgramas();
        String id = String.valueOf(curso.getIdCurso());

        mockMvc.perform(get("/tema/buscar").param("cursoId", id)).andExpect(status().isOk());
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("No hay cronograma para mostrar")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El administrador, sin programas, ve el cronograma vacío")
    void administradorSinProgramas() throws Exception {
        Curso curso = cursoSinProgramas();
        String id = String.valueOf(curso.getIdCurso());

        mockMvc.perform(get("/cronograma/buscar").param("cursoId", id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("No hay cronograma para mostrar")));
    }
}
