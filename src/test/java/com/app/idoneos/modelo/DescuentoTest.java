package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del estado derivado de un descuento (CU-56): se desactiva solo al vencer o al agotarse.
 */
class DescuentoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 13, 12, 0);

    private Descuento descuento(int desdeDias, int hastaDias, int limite, int usada) {
        return new Descuento("Prueba", 0, 10, AHORA.plusDays(desdeDias), AHORA.plusDays(hastaDias), limite, usada);
    }

    @Test
    @DisplayName("Vigente: dentro de su vigencia y con usos disponibles")
    void vigente() {
        Descuento d = descuento(-1, 10, 5, 2);
        assertEquals(Descuento.ESTADO_VIGENTE, d.getEstado(AHORA));
        assertTrue(d.estaVigente(AHORA));
        assertEquals(3, d.getCantidadDisponible());
    }

    @Test
    @DisplayName("Próximo: todavía no comenzó su vigencia")
    void proximo() {
        Descuento d = descuento(2, 10, 5, 0);
        assertEquals(Descuento.ESTADO_PROXIMO, d.getEstado(AHORA));
        assertFalse(d.estaVigente(AHORA));
    }

    @Test
    @DisplayName("Vencido: terminó su vigencia")
    void vencido() {
        Descuento d = descuento(-10, -1, 5, 1);
        assertEquals(Descuento.ESTADO_VENCIDO, d.getEstado(AHORA));
        assertFalse(d.estaVigente(AHORA));
    }

    @Test
    @DisplayName("Agotado: alcanzó su cantidad límite aunque siga dentro de la vigencia")
    void agotado() {
        Descuento d = descuento(-1, 10, 5, 5);
        assertEquals(Descuento.ESTADO_AGOTADO, d.getEstado(AHORA));
        assertFalse(d.estaVigente(AHORA));
        assertEquals(0, d.getCantidadDisponible());
    }

    @Test
    @DisplayName("Dado de baja: tiene prioridad sobre cualquier otro estado")
    void dadoDeBaja() {
        Descuento d = descuento(-1, 10, 5, 0);
        d.marcarInactivo();
        assertEquals(Descuento.ESTADO_DADO_DE_BAJA, d.getEstado(AHORA));
        assertFalse(d.estaVigente(AHORA));
    }
}
