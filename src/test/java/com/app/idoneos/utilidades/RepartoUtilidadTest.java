package com.app.idoneos.utilidades;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del reparto proporcional de un total entero entre varias partes.
 */
class RepartoUtilidadTest {

    @Test
    @DisplayName("Un reparto parejo da lo mismo a todas las partes")
    void parejo() {
        assertArrayEquals(new int[] { 400, 400, 400 }, RepartoUtilidad.repartirProporcional(1200, List.of(5, 5, 5)));
    }

    @Test
    @DisplayName("El reparto respeta la proporción de los pesos")
    void proporcional() {
        assertArrayEquals(new int[] { 200, 400 }, RepartoUtilidad.repartirProporcional(600, List.of(100, 200)));
    }

    @Test
    @DisplayName("El redondeo no pierde minutos: la suma es siempre el total y cada parte recibe al menos 1")
    void sinPerdidas() {
        for (int total = 3; total < 400; total += 7) {
            int[] partes = RepartoUtilidad.repartirProporcional(total, List.of(1, 10, 33));
            assertEquals(total, Arrays.stream(partes).sum());
            assertTrue(Arrays.stream(partes).allMatch(parte -> parte >= 1));
        }
        assertEquals(100, Arrays.stream(RepartoUtilidad.repartirProporcional(100, List.of(1, 1, 1))).sum());
    }

    @Test
    @DisplayName("Con el total justo, cada parte recibe exactamente 1; si no alcanza, se rechaza")
    void totalJusto() {
        assertArrayEquals(new int[] { 1, 1, 1 }, RepartoUtilidad.repartirProporcional(3, List.of(50, 1, 7)));
        assertThrows(IllegalArgumentException.class, () -> RepartoUtilidad.repartirProporcional(2, List.of(1, 1, 1)));
    }
}
