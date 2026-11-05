package com.app.idoneos.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.service.categoria.CategoriaServiceImpl;

/**
 * Pruebas de la baja masiva (todos o ninguno) y de las pantallas del catálogo y de la ficha del curso.
 * No son transaccionales: comprueban que, ante un error, la base de datos queda sin cambios.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BajaMasivaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaServiceImpl categoriaService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("La baja masiva es de todo o nada: si una categoría no puede darse de baja, no se da de baja ninguna")
    void bajaMasivaTodoONada() throws Exception {
        Integer cripto = categoriaService.buscarPorNombre("Criptoactivos").orElseThrow().getIdCategoria();
        Integer macro = categoriaService.buscarPorNombre("Macroeconomía").orElseThrow().getIdCategoria();

        mockMvc.perform(post("/categoria/darDeBajaMasiva").param("ids", cripto.toString(), macro.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Macroeconomía")));

        assertFalse(categoriaService.buscarPorNombre("Criptoactivos").orElseThrow().esInactivo());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("La baja masiva exige al menos un elemento seleccionado")
    void bajaMasivaSinSeleccion() throws Exception {
        mockMvc.perform(post("/curso/darDeBajaMasiva")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/cohorte/darDeBajaMasiva")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/inscripcion/darDeBajaMasiva")).andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("La baja masiva de inscripciones es de todo o nada: si una no existe, no se da de baja ninguna")
    void bajaMasivaDeInscripcionesTodoONada() throws Exception {
        Integer activa = inscripcionRepository.findAll().stream().filter(i -> !i.esInactivo()).findFirst().orElseThrow()
                .getIdInscripcion();

        mockMvc.perform(post("/inscripcion/darDeBajaMasiva").param("ids", activa.toString(), "999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("No se dio de baja ninguna")));

        assertFalse(inscripcionRepository.findById(activa).orElseThrow().esInactivo());
    }

    @Test
    @DisplayName("CU-06: el catálogo entrega todos los cursos y los filtros y las páginas se aplican en el navegador")
    void catalogoConFiltrosEnElNavegador() throws Exception {
        mockMvc.perform(get("/catalogo")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-filtro-front")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-por-pagina")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-paginador")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-tarjeta")));
    }

    @Test
    @DisplayName("CU-06: la ficha del curso es una página independiente y pública")
    void fichaDelCurso() throws Exception {
        mockMvc.perform(get("/catalogo/ficha/1")).andExpect(status().isOk())
                .andExpect(view().name("pages/curso/ficha"))
                .andExpect(model().attributeExists("cursoSeleccionado", "cohortesAbiertas"));
        mockMvc.perform(get("/catalogo/ficha/99999")).andExpect(status().is3xxRedirection());
    }
}
