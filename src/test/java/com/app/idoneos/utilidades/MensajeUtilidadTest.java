package com.app.idoneos.utilidades;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MensajeUtilidadTest {

    @Test
    @DisplayName("Las cantidades usan el singular o el plural, nunca \"(s)\"")
    void cantidades() {
        assertEquals("1 curso activo asociado", MensajeUtilidad.cantidad(1, "curso activo asociado", "cursos activos asociados"));
        assertEquals("3 cursos activos asociados", MensajeUtilidad.cantidad(3, "curso activo asociado", "cursos activos asociados"));
        assertEquals("0 unidades", MensajeUtilidad.cantidad(0, "unidad", "unidades"));
    }

    @Test
    @DisplayName("El mensaje de baja bloqueada nombra el registro, da el motivo y, si hay, el paso a seguir")
    void bajaBloqueada() {
        assertEquals("Error! No se puede dar de baja la categoría «Finanzas»: tiene 2 cursos activos asociados. "
                + "Dé de baja primero esos cursos.",
                MensajeUtilidad.bajaBloqueada("la categoría", "Finanzas", "tiene 2 cursos activos asociados",
                        "Dé de baja primero esos cursos."));
        assertEquals("Error! No se puede dar de baja la cohorte «X»: tiene 3 alumnos inscriptos activos.",
                MensajeUtilidad.bajaBloqueada("la cohorte", "X", "tiene 3 alumnos inscriptos activos", ""));
    }
}
