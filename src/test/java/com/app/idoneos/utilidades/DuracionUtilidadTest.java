package com.app.idoneos.utilidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las duraciones de calendario: se guardan en días y se expresan en la unidad que mejor las describe.
 */
class DuracionUtilidadTest {

    @Test
    @DisplayName("Una duración se expresa en meses, semanas o días según sea múltiplo exacto")
    void formatear() {
        assertEquals("1 día", DuracionUtilidad.formatear(1));
        assertEquals("10 días", DuracionUtilidad.formatear(10));
        assertEquals("1 semana", DuracionUtilidad.formatear(7));
        assertEquals("9 semanas", DuracionUtilidad.formatear(63));
        assertEquals("1 mes", DuracionUtilidad.formatear(30));
        assertEquals("2 meses", DuracionUtilidad.formatear(60));
        assertEquals("45 días", DuracionUtilidad.formatear(45));
    }

    @Test
    @DisplayName("Una cantidad en días, semanas o meses se convierte a días")
    void aDias() {
        assertEquals(5, DuracionUtilidad.aDias(5, "dias"));
        assertEquals(21, DuracionUtilidad.aDias(3, "semanas"));
        assertEquals(60, DuracionUtilidad.aDias(2, "meses"));
        assertThrows(IllegalArgumentException.class, () -> DuracionUtilidad.aDias(1, "años"));
    }
}
