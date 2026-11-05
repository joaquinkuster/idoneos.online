package com.app.idoneos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MensajeErrorUtilTest {

    @Test
    @DisplayName("Las cantidades usan el singular o el plural, nunca \"(s)\"")
    void cantidades() {
        assertEquals("1 curso activo asociado", MensajeErrorUtil.formatearCantidad(1, "curso activo asociado", "cursos activos asociados"));
        assertEquals("3 cursos activos asociados", MensajeErrorUtil.formatearCantidad(3, "curso activo asociado", "cursos activos asociados"));
        assertEquals("0 temas", MensajeErrorUtil.formatearCantidad(0, "tema", "temas"));
    }

    @Test
    @DisplayName("El mensaje de baja bloqueada nombra el registro, da el motivo y, si hay, el paso a seguir")
    void bajaBloqueada() {
        assertEquals("Error! No se puede dar de baja la categoría «Finanzas»: tiene 2 cursos activos asociados. "
                + "Dé de baja primero esos cursos.",
                MensajeErrorUtil.armarBajaBloqueada("la categoría", "Finanzas", "tiene 2 cursos activos asociados",
                        "Dé de baja primero esos cursos."));
        assertEquals("Error! No se puede dar de baja la cohorte «X»: tiene 3 alumnos inscriptos activos.",
                MensajeErrorUtil.armarBajaBloqueada("la cohorte", "X", "tiene 3 alumnos inscriptos activos", ""));
    }
}
