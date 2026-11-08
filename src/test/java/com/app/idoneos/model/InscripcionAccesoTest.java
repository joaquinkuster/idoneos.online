package com.app.idoneos.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de los estados de la inscripción (CU-44) y de las fechas que calcula el modelo (CU-45, CU-47).
 */
class InscripcionAccesoTest {

    private final LocalDateTime ahora = LocalDateTime.now();

    private Programa programa;
    private Cohorte cohorte;
    private Inscripcion inscripcion;

    private static EstadoInscripcion estado(String nombre) {
        return new EstadoInscripcion(nombre);
    }

    /** Cohorte sin fechas de dictado, de 12 semanas de acceso, y una inscripción habilitada que vence en 12 semanas. */
    @BeforeEach
    void armar() {
        programa = new Programa(null, "Programa 2026");
        cohorte = new Cohorte(programa, ahora.minusDays(10), ahora.plusDays(20), 84);
        inscripcion = new Inscripcion(cohorte, null, ahora.plusWeeks(12));
        inscripcion.setEstadoInscripcion(estado(EstadoInscripcion.HABILITADA));
    }

    @Test
    @DisplayName("Acceso habilitado: tiene acceso mientras no venza y el dictado haya comenzado")
    void accesoHabilitado() {
        assertEquals(EstadoInscripcion.HABILITADA, inscripcion.getNombreEstado());
        assertTrue(inscripcion.tieneAcceso(ahora));
        assertTrue(inscripcion.puedeAccederAhora(ahora));
        assertFalse(inscripcion.esperaElDictado(ahora));
        assertTrue(inscripcion.fueHabilitada());
    }

    @Test
    @DisplayName("Pendiente de pago: no está habilitada")
    void pendienteDePago() {
        inscripcion.setEstadoInscripcion(estado(EstadoInscripcion.PENDIENTE_DE_PAGO));
        assertEquals(EstadoInscripcion.PENDIENTE_DE_PAGO, inscripcion.getNombreEstado());
        assertFalse(inscripcion.fueHabilitada());
        assertFalse(inscripcion.ocupaCupo());
        assertTrue(inscripcion.requierePago());
    }

    @Test
    @DisplayName("Pago en curso: sin acceso todavía, pero reserva el cupo")
    void pagoEnCurso() {
        inscripcion.setEstadoInscripcion(estado(EstadoInscripcion.PAGO_EN_CURSO));
        assertEquals(EstadoInscripcion.PAGO_EN_CURSO, inscripcion.getNombreEstado());
        assertFalse(inscripcion.tieneAcceso());
        assertTrue(inscripcion.ocupaCupo());
        assertTrue(inscripcion.requierePago());
    }

    @Test
    @DisplayName("Acceso suspendido: no tiene acceso, pero conserva su habilitación y su cupo")
    void suspendida() {
        inscripcion.setEstadoInscripcion(estado(EstadoInscripcion.SUSPENDIDA));
        assertEquals(EstadoInscripcion.SUSPENDIDA, inscripcion.getNombreEstado());
        assertFalse(inscripcion.tieneAcceso());
        assertTrue(inscripcion.fueHabilitada());
        assertTrue(inscripcion.ocupaCupo());
        assertFalse(inscripcion.requierePago());
    }

    @Test
    @DisplayName("Acceso vencido: ya no tiene acceso, no está habilitada, conserva el cupo y no se puede cobrar")
    void vencida() {
        inscripcion.setEstadoInscripcion(estado(EstadoInscripcion.VENCIDA));
        assertEquals(EstadoInscripcion.VENCIDA, inscripcion.getNombreEstado());
        assertTrue(inscripcion.estaVencida());
        assertFalse(inscripcion.tieneAcceso());
        assertTrue(inscripcion.fueHabilitada());
        assertTrue(inscripcion.ocupaCupo());
        assertFalse(inscripcion.requierePago());
    }

    @Test
    @DisplayName("Con la fecha de vencimiento pasada ya no tiene acceso, aunque el estado todavía no se haya actualizado")
    void sinAccesoConElVencimientoPasado() {
        inscripcion.setFechaVencimientoAcceso(ahora.minusDays(1));
        assertEquals(EstadoInscripcion.HABILITADA, inscripcion.getNombreEstado());
        assertTrue(inscripcion.vencimientoPasado(ahora));
        assertFalse(inscripcion.tieneAcceso(ahora));
        assertFalse(inscripcion.puedeAccederAhora(ahora));
    }

    @Test
    @DisplayName("Cancelada por falta de pago: no tiene acceso ni ocupa cupo, no se le puede cobrar y es un estado final")
    void cancelada() {
        inscripcion.setEstadoInscripcion(estado(EstadoInscripcion.CANCELADA));
        assertEquals(EstadoInscripcion.CANCELADA, inscripcion.getNombreEstado());
        assertFalse(inscripcion.tieneAcceso());
        assertFalse(inscripcion.ocupaCupo());
        assertFalse(inscripcion.requierePago());
        assertTrue(inscripcion.esInmutable());
    }

    @Test
    @DisplayName("Una inscripción sin dato de habilitación se considera habilitada")
    void habilitacionSinDato() {
        inscripcion.setEstadoInscripcion(null);
        assertTrue(inscripcion.fueHabilitada());
        assertTrue(inscripcion.puedeAccederAhora(ahora));
        assertEquals(EstadoInscripcion.HABILITADA, inscripcion.getNombreEstado());
    }

    @Test
    @DisplayName("Espera el dictado: tiene el acceso habilitado, pero el dictado de la cohorte todavía no comenzó")
    void esperaElDictado() {
        cohorte.setFechaInicioDictado(ahora.plusDays(25));
        assertTrue(inscripcion.esperaElDictado(ahora));
        assertFalse(inscripcion.puedeAccederAhora(ahora));
        cohorte.setFechaInicioDictado(ahora.minusDays(1));
        assertTrue(inscripcion.puedeAccederAhora(ahora));
    }

    @Test
    @DisplayName("Dada de baja: tiene prioridad sobre el resto de los estados y es un estado final")
    void dadaDeBaja() {
        inscripcion.marcarInactivo();
        assertEquals(EstadoInscripcion.DADA_DE_BAJA, inscripcion.getNombreEstado());
        assertTrue(inscripcion.estaDadaDeBaja());
        assertTrue(inscripcion.esInmutable());
    }

    @Test
    @DisplayName("Se puede cancelar por falta de pago la pendiente sin pago en curso, aunque tenga un pago parcial aprobado")
    void seCancelaConUnPagoParcial() {
        inscripcion.setEstadoInscripcion(estado(EstadoInscripcion.PENDIENTE_DE_PAGO));
        assertTrue(inscripcion.sePuedeCancelarPorFaltaDePago());
        inscripcion.setEstadoInscripcion(estado(EstadoInscripcion.PAGO_EN_CURSO));
        assertFalse(inscripcion.sePuedeCancelarPorFaltaDePago());
    }

    @Test
    @DisplayName("CU-45: sin fechas de dictado, el acceso vence las semanas de acceso después de la inscripción")
    void vencimientoSinDictado() {
        assertEquals(ahora.plusWeeks(12), cohorte.calcularVencimientoAcceso(ahora));
        assertFalse(cohorte.dictadoPorComenzar(ahora));
    }

    @Test
    @DisplayName("CU-45: con fechas de dictado, el acceso vence los días de acceso después del inicio del dictado")
    void vencimientoConDictado() {
        cohorte.setFechaInicioDictado(ahora.plusDays(25));
        cohorte.setFechaFinDictado(ahora.plusDays(25).plusWeeks(8));
        assertEquals(ahora.plusDays(25).plusWeeks(12), cohorte.calcularVencimientoAcceso(ahora));
        assertTrue(cohorte.dictadoPorComenzar(ahora));
    }

    @Test
    @DisplayName("CU-45: quien se habilita con el dictado ya terminado tiene las semanas de acceso completas desde su habilitación")
    void vencimientoConDictadoTerminado() {
        cohorte.setFechaInicioDictado(ahora.minusWeeks(10));
        cohorte.setFechaFinDictado(ahora.minusWeeks(2));
        assertEquals(ahora.plusWeeks(12), cohorte.calcularVencimientoAcceso(ahora));
    }

    @Test
    @DisplayName("Sin certificado emitido, el certificado vigente es nulo; uno anulado no cuenta")
    void certificadoVigente() {
        assertNull(inscripcion.getCertificadoVigente());
        Certificado anulado = new Certificado(inscripcion, "CERT-1", ahora, "Texto", "Ana Pérez", "30111222");
        anulado.anular();
        inscripcion.getCertificados().add(anulado);
        assertNull(inscripcion.getCertificadoVigente());
        Certificado vigente = new Certificado(inscripcion, "CERT-2", ahora, "Texto", "Ana Pérez", "30111222");
        inscripcion.getCertificados().add(vigente);
        assertEquals(vigente, inscripcion.getCertificadoVigente());
    }
}
