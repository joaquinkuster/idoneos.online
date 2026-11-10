package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.service.categoria.CategoriaServiceImpl;

/** Pruebas de las reglas de negocio de las categorías, sobre los datos de la semilla. */
@SpringBootTest
@Transactional
class CategoriaServiceTest {

    @Autowired
    private CategoriaServiceImpl categoriaService;

    private Categoria categoria(String nombre) {
        return categoriaService.buscarPorNombre(nombre).orElseThrow();
    }

    @Test
    @DisplayName("CU-07: busca categorías por nombre y por estado")
    void buscarCategorias() {
        assertEquals(1, categoriaService.buscarConFiltros("macro", null, "nombre").size());
        assertTrue(categoriaService.buscarConFiltros(null, true, "nombre").isEmpty());
        assertFalse(categoriaService.buscarConFiltros(null, false, "nombre").isEmpty());
    }

    @Test
    @DisplayName("CU-08: registra una categoría con su fecha de creación")
    void registrar() {
        Categoria categoria = categoriaService.registrar("  Bonos  ", "Renta fija");

        assertEquals("Bonos", categoria.getNombre());
        assertFalse(categoria.esInactivo());
        assertTrue(categoria.getFechaCreacion() != null);
    }

    @Test
    @DisplayName("CU-08: el nombre es obligatorio")
    void registrarCategoriaSinNombre() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.registrar("  ", null));
    }

    @Test
    @DisplayName("CU-08: no admite otra categoría activa con el mismo nombre")
    void registrarCategoriaDuplicada() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.registrar("macroeconomía", null));
    }

    @Test
    @DisplayName("CU-08: registrar una categoría con el nombre de una dada de baja la reactiva")
    void registrarCategoriaReactivaLaDadaDeBaja() {
        Categoria cripto = categoria("Criptoactivos");
        categoriaService.darDeBaja(cripto.getIdCategoria());
        assertTrue(categoria("Criptoactivos").esInactivo());

        Categoria reactivada = categoriaService.registrar("Criptoactivos", "Nueva descripción");

        assertEquals(cripto.getIdCategoria(), reactivada.getIdCategoria());
        assertFalse(reactivada.esInactivo());
        assertEquals("Nueva descripción", reactivada.getDescripcion());
    }

    @Test
    @DisplayName("CU-09: modifica el nombre y la descripción de una categoría sin inscripciones")
    void modificar() {
        Categoria cripto = categoria("Criptoactivos");

        Categoria modificada = categoriaService.modificar(cripto.getIdCategoria(), "Criptomonedas", "Nuevo");

        assertEquals("Criptomonedas", modificada.getNombre());
        assertTrue(modificada.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-09: no modifica una categoría con inscripciones activas")
    void modificarCategoriaConInscripciones() {
        Categoria mercado = categoria("Mercado de Capitales");
        assertTrue(categoriaService.contarInscripcionesActivas(mercado) > 0);

        assertThrows(IllegalArgumentException.class,
                () -> categoriaService.modificar(mercado.getIdCategoria(), "Otro", null));
    }

    @Test
    @DisplayName("CU-09: el nombre no puede quedar vacío ni coincidir con otra categoría activa")
    void modificarCategoriaConNombreInvalido() {
        Categoria cripto = categoria("Criptoactivos");

        assertThrows(IllegalArgumentException.class,
                () -> categoriaService.modificar(cripto.getIdCategoria(), " ", null));
        assertThrows(IllegalArgumentException.class,
                () -> categoriaService.modificar(cripto.getIdCategoria(), "Macroeconomía", null));
    }

    @Test
    @DisplayName("CU-10: no da de baja una categoría con cursos activos")
    void darDeBajaCategoriaConCursos() {
        Categoria macro = categoria("Macroeconomía");

        assertThrows(IllegalArgumentException.class, () -> categoriaService.darDeBaja(macro.getIdCategoria()));
    }

    @Test
    @DisplayName("CU-10: da de baja una categoría sin cursos activos")
    void darDeBajaCategoriaSinCursos() {
        Categoria cripto = categoria("Criptoactivos");

        categoriaService.darDeBaja(cripto.getIdCategoria());

        assertTrue(categoria("Criptoactivos").esInactivo());
    }

    @Test
    @DisplayName("CU-07: ordena por nombre o por las más recientes, con las dadas de baja al final")
    void ordenarCategorias() {
        categoriaService.darDeBaja(categoria("Criptoactivos").getIdCategoria());

        List<Categoria> porNombre = categoriaService.buscarConFiltros(null, null, "nombre");
        List<Categoria> recientes = categoriaService.buscarConFiltros(null, null, "recientes");

        assertTrue(porNombre.get(porNombre.size() - 1).esInactivo());
        assertTrue(recientes.get(recientes.size() - 1).esInactivo());
        List<Categoria> vigentes = porNombre.subList(0, porNombre.size() - 1);
        assertEquals(vigentes.stream().map(Categoria::getNombre).sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                vigentes.stream().map(Categoria::getNombre).toList());
        assertTrue(recientes.get(0).getIdCategoria() > recientes.get(1).getIdCategoria());
    }
}
