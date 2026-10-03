package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;

/**
 * Pruebas de las reglas de negocio de las categorías (CU-07 a CU-10), sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class CategoriaServicioTest {

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    private Categoria categoria(String nombre) {
        return categoriaServicio.buscarPorNombre(nombre).orElseThrow();
    }

    @Test
    @DisplayName("CU-07: busca categorías por nombre y por estado")
    void buscarCategorias() {
        assertEquals(1, categoriaServicio.buscarConFiltros("macro", null).size());
        assertTrue(categoriaServicio.buscarConFiltros(null, true).isEmpty());
        assertFalse(categoriaServicio.buscarConFiltros(null, false).isEmpty());
    }

    @Test
    @DisplayName("CU-08: registra una categoría con su fecha de creación")
    void registrarCategoria() {
        Categoria categoria = categoriaServicio.registrarCategoria("  Bonos  ", "Renta fija");

        assertEquals("Bonos", categoria.getNombre());
        assertFalse(categoria.esInactivo());
        assertTrue(categoria.getFechaCreacion() != null);
    }

    @Test
    @DisplayName("CU-08: el nombre es obligatorio")
    void registrarCategoriaSinNombre() {
        assertThrows(IllegalArgumentException.class, () -> categoriaServicio.registrarCategoria("  ", null));
    }

    @Test
    @DisplayName("CU-08: no admite otra categoría activa con el mismo nombre")
    void registrarCategoriaDuplicada() {
        assertThrows(IllegalArgumentException.class, () -> categoriaServicio.registrarCategoria("macroeconomía", null));
    }

    @Test
    @DisplayName("CU-08: registrar una categoría con el nombre de una dada de baja la reactiva")
    void registrarCategoriaReactivaLaDadaDeBaja() {
        Categoria cripto = categoria("Criptoactivos");
        categoriaServicio.darDeBajaCategoria(cripto.getIdCategoria());
        assertTrue(categoria("Criptoactivos").esInactivo());

        Categoria reactivada = categoriaServicio.registrarCategoria("Criptoactivos", "Nueva descripción");

        assertEquals(cripto.getIdCategoria(), reactivada.getIdCategoria());
        assertFalse(reactivada.esInactivo());
        assertEquals("Nueva descripción", reactivada.getDescripcion());
    }

    @Test
    @DisplayName("CU-09: modifica el nombre y la descripción de una categoría sin inscripciones")
    void modificarCategoria() {
        Categoria cripto = categoria("Criptoactivos");

        Categoria modificada = categoriaServicio.modificarCategoria(cripto.getIdCategoria(), "Criptomonedas", "Nuevo");

        assertEquals("Criptomonedas", modificada.getNombre());
        assertTrue(modificada.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-09: no modifica una categoría con inscripciones activas")
    void modificarCategoriaConInscripciones() {
        Categoria mercado = categoria("Mercado de Capitales");
        assertTrue(categoriaServicio.contarInscripcionesActivas(mercado) > 0);

        assertThrows(IllegalArgumentException.class,
                () -> categoriaServicio.modificarCategoria(mercado.getIdCategoria(), "Otro", null));
    }

    @Test
    @DisplayName("CU-09: el nombre no puede quedar vacío ni coincidir con otra categoría activa")
    void modificarCategoriaConNombreInvalido() {
        Categoria cripto = categoria("Criptoactivos");

        assertThrows(IllegalArgumentException.class,
                () -> categoriaServicio.modificarCategoria(cripto.getIdCategoria(), " ", null));
        assertThrows(IllegalArgumentException.class,
                () -> categoriaServicio.modificarCategoria(cripto.getIdCategoria(), "Macroeconomía", null));
    }

    @Test
    @DisplayName("CU-10: no da de baja una categoría con cursos activos")
    void darDeBajaCategoriaConCursos() {
        Categoria macro = categoria("Macroeconomía");

        assertThrows(IllegalArgumentException.class, () -> categoriaServicio.darDeBajaCategoria(macro.getIdCategoria()));
    }

    @Test
    @DisplayName("CU-10: da de baja una categoría sin cursos activos")
    void darDeBajaCategoriaSinCursos() {
        Categoria cripto = categoria("Criptoactivos");

        categoriaServicio.darDeBajaCategoria(cripto.getIdCategoria());

        assertTrue(categoria("Criptoactivos").esInactivo());
    }
}
