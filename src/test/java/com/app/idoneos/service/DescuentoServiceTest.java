package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Descuento;
import com.app.idoneos.service.descuento.DescuentoServiceImpl;

/**
 * Pruebas de las reglas de negocio de los descuentos (CU-56 a CU-59), sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class DescuentoServiceTest {

    private static final LocalDate DESDE = LocalDate.now().plusDays(1);
    private static final LocalDate HASTA = LocalDate.now().plusDays(30);

    @Autowired
    private DescuentoServiceImpl descuentoService;

    private Descuento descuento(String nombre) {
        return descuentoService.buscarPorNombre(nombre).orElseThrow();
    }

    @Test
    @DisplayName("CU-56: busca por nombre (sin tildes ni mayúsculas) y por estado")
    void buscarDescuentos() {
        assertEquals(1, descuentoService.buscarConFiltros("FRECUENTE", null, "nombre").size());
        assertEquals(1, descuentoService.buscarConFiltros(null, Descuento.ESTADO_PROXIMO, "nombre").size());
        assertEquals(1, descuentoService.buscarConFiltros(null, Descuento.ESTADO_VENCIDO, "nombre").size());
        assertEquals(1, descuentoService.buscarConFiltros(null, Descuento.ESTADO_AGOTADO, "nombre").size());
        assertEquals(2, descuentoService.buscarConFiltros(null, Descuento.ESTADO_VIGENTE, "nombre").size());
        assertTrue(descuentoService.buscarConFiltros(null, Descuento.ESTADO_DADO_DE_BAJA, "nombre").isEmpty());
    }

    @Test
    @DisplayName("CU-56: puede ordenarse por mayor porcentaje")
    void ordenarPorPorcentaje() {
        List<Descuento> descuentos = descuentoService.buscarConFiltros(null, null, "porcentaje");
        assertEquals("Preventa agotada", descuentos.get(0).getNombre());
    }

    @Test
    @DisplayName("CU-57: registra un descuento con cantidad usada en cero y sin condición si no se indica")
    void registrar() {
        Descuento d = descuentoService.registrar("  Nuevo  ", 15f, DESDE, HASTA, 10, null);

        assertEquals("Nuevo", d.getNombre());
        assertEquals(0, d.getCantidadUsada());
        assertEquals(0, d.getCursosRequeridos());
        assertEquals(Descuento.ESTADO_PROXIMO, d.getEstado());
        assertFalse(d.esInactivo());
        assertTrue(d.getFechaCreacion() != null);
    }

    @Test
    @DisplayName("CU-57: informa cuáles campos obligatorios faltan")
    void registrarDescuentoSinCamposObligatorios() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> descuentoService.registrar(" ", null, DESDE, null, null, null));
        assertTrue(e.getMessage().contains("nombre"));
        assertTrue(e.getMessage().contains("porcentaje"));
        assertTrue(e.getMessage().contains("vigencia hasta"));
        assertTrue(e.getMessage().contains("cantidad límite"));
        assertFalse(e.getMessage().contains("vigencia desde"));
    }

    @Test
    @DisplayName("CU-57: valida porcentaje, vigencia, cantidad límite y cursos requeridos")
    void registrarDescuentoConDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> descuentoService.registrar("A", 0f, DESDE, HASTA, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> descuentoService.registrar("A", 100.5f, DESDE, HASTA, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> descuentoService.registrar("A", 10f, HASTA, DESDE, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> descuentoService.registrar("A", 10f, DESDE, HASTA, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> descuentoService.registrar("A", 10f, DESDE, HASTA, 10, -1));
        assertEquals(99f, descuentoService.registrar("Casi total", 99f, DESDE, DESDE, 1, 0).getPorcentaje());
        // no se registran descuentos del 100%: nadie queda sin nada que pagar por un descuento
        assertThrows(IllegalArgumentException.class, () -> descuentoService.registrar("Total", 100f, DESDE, HASTA, 10, 0));
    }

    @Test
    @DisplayName("CU-57: no admite otro descuento con el mismo nombre")
    void registrarDescuentoDuplicado() {
        assertThrows(IllegalArgumentException.class,
                () -> descuentoService.registrar("alumno frecuente", 10f, DESDE, HASTA, 10, 0));
    }

    @Test
    @DisplayName("CU-57: registrar con el nombre de uno dado de baja lo reactiva")
    void registrarDescuentoReactivaElDadoDeBaja() {
        Descuento bienvenida = descuento("Descuento de bienvenida");
        descuentoService.darDeBaja(bienvenida.getIdDescuento());
        assertTrue(descuento("Descuento de bienvenida").esInactivo());

        Descuento d = descuentoService.registrar("Descuento de bienvenida", 40f, DESDE, HASTA, 7, 3);

        assertEquals(bienvenida.getIdDescuento(), d.getIdDescuento());
        assertFalse(d.esInactivo());
        assertEquals(40f, d.getPorcentaje());
        assertEquals(7, d.getCantidadLimite());
        assertEquals(3, d.getCursosRequeridos());
    }

    @Test
    @DisplayName("CU-58: modifica un descuento y registra la fecha de modificación")
    void modificar() {
        Descuento d = descuento("Alumno frecuente");

        descuentoService.modificar(d.getIdDescuento(), "Alumno fiel", 25f, DESDE, HASTA, 60, 4);

        Descuento modificado = descuento("Alumno fiel");
        assertEquals(25f, modificado.getPorcentaje());
        assertEquals(4, modificado.getCursosRequeridos());
        assertTrue(modificado.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-58: un descuento vencido puede modificarse para extender su vigencia")
    void modificarDescuentoVencido() {
        Descuento lanzamiento = descuento("Lanzamiento");

        descuentoService.modificar(lanzamiento.getIdDescuento(), "Lanzamiento", 25f, DESDE, HASTA, 20, 0);

        assertEquals(Descuento.ESTADO_PROXIMO, descuento("Lanzamiento").getEstado());
    }

    @Test
    @DisplayName("CU-58: la cantidad límite no puede ser menor a la cantidad ya usada")
    void modificarDescuentoConLimiteMenorAlUso() {
        Descuento lanzamiento = descuento("Lanzamiento"); // usado 8 veces
        assertThrows(IllegalArgumentException.class, () -> descuentoService
                .modificar(lanzamiento.getIdDescuento(), "Lanzamiento", 25f, DESDE, HASTA, 7, 0));
    }

    @Test
    @DisplayName("CU-58: no admite el nombre de otro descuento ni modificar uno dado de baja")
    void modificarDescuentoConNombreRepetidoODadoDeBaja() {
        Descuento d = descuento("Alumno frecuente");
        assertThrows(IllegalArgumentException.class, () -> descuentoService
                .modificar(d.getIdDescuento(), "LANZAMIENTO", 20f, DESDE, HASTA, 50, 2));

        Descuento promo = descuento("Promo de verano");
        descuentoService.darDeBaja(promo.getIdDescuento());
        assertThrows(IllegalArgumentException.class, () -> descuentoService
                .modificar(promo.getIdDescuento(), "Promo de verano", 15f, DESDE, HASTA, 30, 0));
    }

    @Test
    @DisplayName("CU-59: da de baja un descuento que nunca se aplicó")
    void darDeBaja() {
        Descuento promo = descuento("Promo de verano");

        descuentoService.darDeBaja(promo.getIdDescuento());

        assertTrue(descuento("Promo de verano").esInactivo());
        assertEquals(Descuento.ESTADO_DADO_DE_BAJA, descuento("Promo de verano").getEstado());
    }

    @Test
    @DisplayName("CU-59: no permite dar de baja un descuento ya aplicado e informa la dependencia")
    void darDeBajaDescuentoAplicado() {
        Descuento lanzamiento = descuento("Lanzamiento");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> descuentoService.darDeBaja(lanzamiento.getIdDescuento()));

        assertTrue(e.getMessage().contains("8 inscripciones"));
        assertFalse(descuento("Lanzamiento").esInactivo());
    }

    @Test
    @DisplayName("CU-59: no permite dar de baja dos veces el mismo descuento")
    void darDeBajaDescuentoDadoDeBaja() {
        Descuento promo = descuento("Promo de verano");
        descuentoService.darDeBaja(promo.getIdDescuento());
        assertThrows(IllegalArgumentException.class, () -> descuentoService.darDeBaja(promo.getIdDescuento()));
    }

    @Test
    @DisplayName("La baja masiva es de todo o nada")
    void darDeBajaVarios() {
        Integer promo = descuento("Promo de verano").getIdDescuento();
        Integer lanzamiento = descuento("Lanzamiento").getIdDescuento();

        assertThrows(IllegalArgumentException.class, () -> descuentoService.darDeBajaVarios(List.of(lanzamiento, promo)));
        assertThrows(IllegalArgumentException.class, () -> descuentoService.darDeBajaVarios(List.of()));

        descuentoService.darDeBajaVarios(List.of(promo));
        assertTrue(descuento("Promo de verano").esInactivo());
    }
}
