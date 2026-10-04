package com.app.idoneos.controlador;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;

/**
 * Pruebas de la baja masiva (todos o ninguno) y de las pantallas del catálogo y de la ficha del curso.
 * No son transaccionales: comprueban que, ante un error, la base de datos queda sin cambios.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BajaMasivaControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La baja masiva es de todo o nada: si una categoría no puede darse de baja, no se da de baja ninguna")
    void bajaMasivaTodoONada() throws Exception {
        Integer cripto = categoriaServicio.buscarPorNombre("Criptoactivos").orElseThrow().getIdCategoria();
        Integer macro = categoriaServicio.buscarPorNombre("Macroeconomía").orElseThrow().getIdCategoria();

        mockMvc.perform(post("/categoria/darDeBajaMasiva").param("ids", cripto.toString(), macro.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Macroeconomía")));

        assertFalse(categoriaServicio.buscarPorNombre("Criptoactivos").orElseThrow().esInactivo());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La baja masiva exige al menos un elemento seleccionado")
    void bajaMasivaSinSeleccion() throws Exception {
        mockMvc.perform(post("/curso/darDeBajaMasiva")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/cohorte/darDeBajaMasiva")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CU-06: el catálogo admite elegir la cantidad de cursos por página, incluso todos")
    void catalogoConCursosPorPagina() throws Exception {
        mockMvc.perform(get("/catalogo").param("porPagina", "10")).andExpect(status().isOk())
                .andExpect(model().attribute("porPagina", 10));
        mockMvc.perform(get("/catalogo").param("porPagina", "0")).andExpect(status().isOk())
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    @DisplayName("CU-06: la ficha del curso es una página independiente y pública")
    void fichaDelCurso() throws Exception {
        mockMvc.perform(get("/catalogo/ficha/1")).andExpect(status().isOk())
                .andExpect(view().name("pages/ficha"))
                .andExpect(model().attributeExists("cursoSeleccionado", "cohortesAbiertas"));
        mockMvc.perform(get("/catalogo/ficha/99999")).andExpect(status().is3xxRedirection());
    }
}
