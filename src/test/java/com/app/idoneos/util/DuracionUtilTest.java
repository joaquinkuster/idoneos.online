package com.app.idoneos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las duraciones de calendario: se guardan en días y se expresan en la unidad que mejor las describe.
 */
class DuracionUtilTest {

    @Test
    @DisplayName("Una duración se expresa en meses, semanas o días según sea múltiplo exacto")
    void formatear() {
        assertEquals("1 día", DuracionUtil.formatear(1));
        assertEquals("10 días", DuracionUtil.formatear(10));
        assertEquals("1 semana", DuracionUtil.formatear(7));
        assertEquals("9 semanas", DuracionUtil.formatear(63));
        assertEquals("1 mes", DuracionUtil.formatear(30));
        assertEquals("2 meses", DuracionUtil.formatear(60));
        assertEquals("45 días", DuracionUtil.formatear(45));
    }

    @Test
    @DisplayName("Una cantidad en días, semanas o meses se convierte a días")
    void aDias() {
        assertEquals(5, DuracionUtil.convertirADias(5, "dias"));
        assertEquals(21, DuracionUtil.convertirADias(3, "semanas"));
        assertEquals(60, DuracionUtil.convertirADias(2, "meses"));
        assertThrows(IllegalArgumentException.class, () -> DuracionUtil.convertirADias(1, "años"));
    }
}
