package com.app.idoneos.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import com.app.idoneos.model.EstadoCohorte;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del modelo {@link Cohorte}: estado calculado según las fechas y cupo disponible.
 */
class CohorteTest {

    private final LocalDateTime ahora = LocalDateTime.now();

    @Test
    @DisplayName("Una cohorte cuya inscripción todavía no abrió está en Inscripción próxima")
    void estadoProxima() {
        Cohorte cohorte = new Cohorte(null, ahora.plusDays(5), ahora.plusDays(30), 10);

        assertEquals(EstadoCohorte.PROXIMA, cohorte.calcularEstado(ahora, false));
        assertEquals(EstadoCohorte.PROXIMA, cohorte.getEstado());
        assertFalse(cohorte.estaAbierta());
        assertTrue(cohorte.estaProxima());
    }

    @Test
    @DisplayName("Una cohorte dentro de su período de inscripción está en Inscripción abierta, aunque el dictado ya haya empezado")
    void estadoAbierta() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(5), ahora.plusDays(30), 10);

        assertEquals(EstadoCohorte.ABIERTA, cohorte.calcularEstado(ahora, false));
        assertTrue(cohorte.estaAbierta());
        cohorte.setFechaInicioDictado(ahora.minusDays(1));
        cohorte.setFechaFinDictado(ahora.plusDays(60));
        assertEquals(EstadoCohorte.ABIERTA, cohorte.calcularEstado(ahora, false));
    }

    @Test
    @DisplayName("Con dictado: Inscripción cerrada hasta que empieza, En dictado mientras dura y Dictado finalizado después (con inscripciones en juego)")
    void estadoConDictado() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(60), ahora.minusDays(30), 10);
        cohorte.setFechaInicioDictado(ahora.plusDays(10));
        cohorte.setFechaFinDictado(ahora.plusDays(60));
        assertEquals(EstadoCohorte.CERRADA, cohorte.calcularEstado(ahora, true));

        cohorte.setFechaInicioDictado(ahora.minusDays(20));
        assertEquals(EstadoCohorte.EN_DICTADO, cohorte.calcularEstado(ahora, true));

        cohorte.setFechaFinDictado(ahora.minusDays(1));
        assertEquals(EstadoCohorte.DICTADO_FINALIZADO, cohorte.calcularEstado(ahora, true));
    }

    @Test
    @DisplayName("Terminado el dictado, la cohorte pasa a Cursada finalizada cuando ya no queda ninguna inscripción en juego")
    void estadoCursadaFinalizadaConDictado() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(200), ahora.minusDays(170), 10);
        cohorte.setFechaInicioDictado(ahora.minusDays(160));
        cohorte.setFechaFinDictado(ahora.minusDays(80));

        assertEquals(EstadoCohorte.DICTADO_FINALIZADO, cohorte.calcularEstado(ahora, true));
        assertEquals(EstadoCohorte.CURSADA_FINALIZADA, cohorte.calcularEstado(ahora, false));
    }

    @Test
    @DisplayName("Sin dictado: Inscripción cerrada mientras haya inscripciones en juego y Cursada finalizada cuando ya no queda ninguna")
    void estadoSinDictado() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(40), ahora.minusDays(10), 70);

        assertEquals(EstadoCohorte.CERRADA, cohorte.calcularEstado(ahora, true));
        assertEquals(EstadoCohorte.CURSADA_FINALIZADA, cohorte.calcularEstado(ahora, false));
    }

    @Test
    @DisplayName("Una inscripción sigue en juego si espera el pago, tiene un pago en curso o su acceso todavía no venció")
    void inscripcionEnJuego() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(40), ahora.minusDays(10), 70);
        Inscripcion habilitada = new Inscripcion(cohorte, null, ahora.plusDays(5));
        habilitada.setEstadoInscripcion(new EstadoInscripcion(EstadoInscripcion.HABILITADA));
        Inscripcion vencida = new Inscripcion(cohorte, null, ahora.minusDays(1));
        vencida.setEstadoInscripcion(new EstadoInscripcion(EstadoInscripcion.VENCIDA));
        Inscripcion cancelada = new Inscripcion(cohorte, null, ahora.plusDays(5));
        cancelada.setEstadoInscripcion(new EstadoInscripcion(EstadoInscripcion.CANCELADA));

        assertTrue(habilitada.sigueEnJuego(ahora));
        assertFalse(vencida.sigueEnJuego(ahora));
        assertFalse(cancelada.sigueEnJuego(ahora));
        habilitada.setFechaVencimientoAcceso(ahora.minusMinutes(1));
        assertFalse(habilitada.sigueEnJuego(ahora));
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
    @DisplayName("Una cohorte pendiente o cancelada no sigue el calendario: su estado es el guardado y no cuenta como en marcha")
    void estadoPendienteYCancelada() {
        Cohorte cohorte = new Cohorte(null, ahora.minusDays(5), ahora.plusDays(30), 10);
        assertTrue(cohorte.esActiva());
        assertEquals(EstadoCohorte.ABIERTA, cohorte.getEstado());
        assertTrue(cohorte.estaActiva());

        cohorte.setEstadoCohorte(new EstadoCohorte(EstadoCohorte.PENDIENTE));
        assertTrue(cohorte.esPendiente());
        assertEquals(EstadoCohorte.PENDIENTE, cohorte.getEstado());
        assertEquals(EstadoCohorte.PENDIENTE, cohorte.calcularEstado(ahora, false));
        assertFalse(cohorte.estaAbierta());
        assertFalse(cohorte.estaActiva());

        cohorte.setEstadoCohorte(new EstadoCohorte(EstadoCohorte.CANCELADA));
        assertTrue(cohorte.esCancelada());
        assertEquals(EstadoCohorte.CANCELADA, cohorte.getEstado());
        assertFalse(cohorte.estaAbierta());
        assertFalse(cohorte.estaActiva());

        cohorte.setEstadoCohorte(new EstadoCohorte(EstadoCohorte.ABIERTA));
        assertEquals(EstadoCohorte.ABIERTA, cohorte.getEstado());
        assertTrue(cohorte.estaActiva());

        cohorte.setEstadoCohorte(new EstadoCohorte(EstadoCohorte.CURSADA_FINALIZADA));
        assertFalse(cohorte.estaActiva());
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
