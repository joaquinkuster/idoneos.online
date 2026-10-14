package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la regla de numeración de los comprobantes.
 */
class ComprobanteTest {

    @Test
    @DisplayName("El número lleva el año de la emisión y el identificador del pago con seis dígitos")
    void numeroConAnioYPago() {
        assertEquals("COMP-2026-000012", Comprobante.generarNumero(12, LocalDateTime.of(2026, 10, 14, 9, 30)));
        assertEquals("COMP-2027-123456", Comprobante.generarNumero(123456, LocalDateTime.of(2027, 1, 2, 0, 0)));
    }

    @Test
    @DisplayName("Un mismo pago genera siempre el mismo número dentro del año")
    void numeroEstable() {
        LocalDateTime fecha = LocalDateTime.of(2026, 3, 1, 12, 0);

        assertEquals(Comprobante.generarNumero(7, fecha), Comprobante.generarNumero(7, fecha.plusDays(90)));
    }
}
