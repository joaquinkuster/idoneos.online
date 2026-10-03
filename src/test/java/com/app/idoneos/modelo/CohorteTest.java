package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del modelo {@link Cohorte}: estado calculado según las fechas y cupo disponible.
 */
class CohorteTest {

    private final LocalDateTime ahora = LocalDateTime.now();

    @Test
    @DisplayName("Una cohorte cuya inscripción todavía no abrió está Próxima")
    void estadoProxima() {
        Cohorte cohorte = new Cohorte(null, ahora.plusDays(5), ahora.plusDays(30), 10);

        assertEquals("Próxima", cohorte.getEstado());
        assertFalse(cohorte.estaAbierta());
    }

    @Test
    @DisplayName("Una cohorte dentro de su período de inscripción está Abierta")
    void estadoAbierta() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(5), ahora.plusDays(30), 10);

        assertEquals("Abierta", cohorte.getEstado());
        assertTrue(cohorte.estaAbierta());
    }

    @Test
    @DisplayName("Una cohorte con la inscripción cerrada y dictado vigente está En dictado")
    void estadoEnDictado() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(60), ahora.minusDays(30), 10);
        cohorte.setFechaInicioDictado(ahora.minusDays(20));
        cohorte.setFechaFinDictado(ahora.plusDays(40));

        assertEquals("En dictado", cohorte.getEstado());
    }

    @Test
    @DisplayName("Una cohorte con el dictado terminado está Finalizada")
    void estadoFinalizadaConDictado() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(200), ahora.minusDays(170), 10);
        cohorte.setFechaInicioDictado(ahora.minusDays(160));
        cohorte.setFechaFinDictado(ahora.minusDays(80));

        assertEquals("Finalizada", cohorte.getEstado());
    }

    @Test
    @DisplayName("Sin dictado, la cohorte finaliza cuando vencen las semanas de acceso desde el fin de la inscripción")
    void estadoSinDictado() {
        Cohorte vigente = new Cohorte(null, ahora.minusDays(40), ahora.minusDays(10), 10);
        Cohorte vencida = new Cohorte(null, ahora.minusDays(400), ahora.minusDays(370), 10);

        assertEquals("En dictado", vigente.getEstado());
        assertEquals("Finalizada", vencida.getEstado());
    }

    @Test
    @DisplayName("Sin cupo máximo no hay cupo disponible que calcular")
    void cupoDisponible() {
        Cohorte cohorte = new Cohorte(null, ahora, ahora.plusDays(10), 10);
        assertNull(cohorte.getCupoDisponible());

        cohorte.setCupoMaximo(5);
        assertEquals(5, cohorte.getCupoDisponible());
    }

    @Test
    @DisplayName("La baja lógica marca la cohorte como inactiva")
    void bajaLogica() {
        Cohorte cohorte = new Cohorte(null, ahora, ahora.plusDays(10), 10);
        assertFalse(cohorte.esInactivo());

        cohorte.marcarInactivo();

        assertTrue(cohorte.esInactivo());
    }
}
