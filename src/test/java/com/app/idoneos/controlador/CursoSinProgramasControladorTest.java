package com.app.idoneos.controlador;

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

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.repositorio.CursoRepositorio;

/**
 * Pruebas de un curso que todavía no tiene programas (o los dio de baja todos): el docente igual puede abrirlo, porque
 * desde ahí registra el primero. "Macroeconomía de Coyuntura" tiene un solo programa, que estas pruebas dan de baja.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CursoSinProgramasControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    /** Deja al curso sin programas activos. */
    private Curso cursoSinProgramas() {
        Curso curso = cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow();
        curso.getProgramas().forEach(programa -> programa.marcarInactivo());
        return curso;
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente abre un curso sin programas y ve cómo registrar el primero")
    void accedeAlCursoSinProgramas() throws Exception {
        Curso curso = cursoSinProgramas();

        mockMvc.perform(get("/curso/acceder/" + curso.getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Este curso todavía no tiene programas")))
                .andExpect(content().string(Matchers.containsString("/programa/buscar?cursoId=" + curso.getIdCurso() + "&amp;nuevo=")))
                .andExpect(content().string(Matchers.containsString("Sin programas")))
                // sin programa no hay unidades ni botón para registrarlas
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar unidad"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-unidad-cabecera"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Las clases en vivo y el listado de programas también se abren en un curso sin programas")
    void otrasPaginasDelCurso() throws Exception {
        Curso curso = cursoSinProgramas();
        String id = String.valueOf(curso.getIdCurso());

        mockMvc.perform(get("/curso/acceder/" + id).param("seccion", "clases")).andExpect(status().isOk());
        mockMvc.perform(get("/programa/buscar").param("cursoId", id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-modal=\"modalNuevoPrograma\""))));
        mockMvc.perform(get("/material/buscar").param("cursoId", id)).andExpect(status().isOk());
        mockMvc.perform(get("/glosario/buscar").param("cursoId", id)).andExpect(status().isOk());
        mockMvc.perform(get("/foro/buscar").param("cursoId", id)).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El botón Registrar programa lleva al listado con el formulario de alta abierto")
    void registrarElPrimerPrograma() throws Exception {
        Curso curso = cursoSinProgramas();
        String id = String.valueOf(curso.getIdCurso());

        mockMvc.perform(get("/programa/buscar").param("cursoId", id).param("nuevo", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalNuevoPrograma\"")))
                .andExpect(content().string(Matchers.containsString("/js/abrirModal.js")));

        mockMvc.perform(post("/programa/registrar").param("cursoId", id).param("nombre", "Primer programa")
                .param("objetivos", "Objetivos").param("bibliografia", "Bibliografía"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Primer programa")));

        // con el primer programa, el curso se ve con sus selectores y sin el aviso
        mockMvc.perform(get("/curso/acceder/" + id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("Este curso todavía no tiene programas"))))
                .andExpect(content().string(Matchers.containsString("Primer programa (Vigente)")))
                .andExpect(content().string(Matchers.containsString("Registrar unidad")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Unidades y cronograma, sin programas, mandan al docente a registrar uno")
    void unidadesYCronogramaRedirigen() throws Exception {
        Curso curso = cursoSinProgramas();
        String id = String.valueOf(curso.getIdCurso());
        String destino = "/programa/buscar?cursoId=" + id + "&nuevo";

        mockMvc.perform(get("/unidad/buscar").param("cursoId", id)).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(destino))
                .andExpect(flash().attribute("error", Matchers.containsString("todavía no tiene programas")));
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", id)).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(destino));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador, sin programas, vuelve al listado de cursos")
    void administradorSinProgramas() throws Exception {
        Curso curso = cursoSinProgramas();
        String id = String.valueOf(curso.getIdCurso());

        mockMvc.perform(get("/unidad/buscar").param("cursoId", id)).andExpect(redirectedUrl("/curso/buscar"));
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", id)).andExpect(redirectedUrl("/curso/buscar"));
    }
}
