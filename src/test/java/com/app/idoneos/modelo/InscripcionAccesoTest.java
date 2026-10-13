package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del estado de acceso de la inscripción (CU-44) y de las fechas que calcula el modelo (CU-45, CU-47).
 */
class InscripcionAccesoTest {

    private final LocalDateTime ahora = LocalDateTime.now();

    private Programa programa;
    private Cohorte cohorte;
    private Inscripcion inscripcion;

    /** Cohorte sin fechas de dictado, de 12 semanas de acceso, y una inscripción habilitada que vence en 12 semanas. */
    @BeforeEach
    void armar() {
        programa = new Programa(null, "Programa 2026", "Objetivos", "Bibliografía");
        cohorte = new Cohorte(programa, ahora.minusDays(10), ahora.plusDays(20), 12);
        inscripcion = new Inscripcion(cohorte, null, ahora.plusWeeks(12));
        inscripcion.setHabilitado(true);
    }

    @Test
    @DisplayName("Vigente: habilitada, sin vencer y sin dictado pendiente")
    void vigente() {
        assertEquals(Inscripcion.ACCESO_VIGENTE, inscripcion.getEstadoAcceso(ahora));
    }

    @Test
    @DisplayName("Pendiente de pago: no está habilitada")
    void pendienteDePago() {
        inscripcion.setHabilitado(false);
        assertEquals(Inscripcion.ACCESO_PENDIENTE_DE_PAGO, inscripcion.getEstadoAcceso(ahora));
        assertFalse(inscripcion.estaHabilitada());
    }

    @Test
    @DisplayName("Una inscripción sin dato de habilitación se considera habilitada")
    void habilitacionSinDato() {
        inscripcion.setHabilitado(null);
        assertTrue(inscripcion.estaHabilitada());
        assertEquals(Inscripcion.ACCESO_VIGENTE, inscripcion.getEstadoAcceso(ahora));
    }

    @Test
    @DisplayName("Programada: habilitada, pero el dictado de la cohorte todavía no comenzó")
    void programada() {
        cohorte.setFechaInicioDictado(ahora.plusDays(25));
        assertEquals(Inscripcion.ACCESO_PROGRAMADA, inscripcion.getEstadoAcceso(ahora));
        cohorte.setFechaInicioDictado(ahora.minusDays(1));
        assertEquals(Inscripcion.ACCESO_VIGENTE, inscripcion.getEstadoAcceso(ahora));
    }

    @Test
    @DisplayName("Vencida: pasó la fecha de vencimiento, aunque falte pagar o comenzar el dictado")
    void vencida() {
        inscripcion.setFechaVencimientoAcceso(ahora.minusDays(1));
        assertEquals(Inscripcion.ACCESO_VENCIDA, inscripcion.getEstadoAcceso(ahora));
        inscripcion.setHabilitado(false);
        assertEquals(Inscripcion.ACCESO_VENCIDA, inscripcion.getEstadoAcceso(ahora));
    }

    @Test
    @DisplayName("Dada de baja: tiene prioridad sobre el resto de los estados")
    void dadaDeBaja() {
        inscripcion.marcarInactivo();
        assertEquals(Inscripcion.ACCESO_DADA_DE_BAJA, inscripcion.getEstadoAcceso(ahora));
    }

    @Test
    @DisplayName("CU-45: sin fechas de dictado, el acceso vence las semanas de acceso después de la inscripción")
    void vencimientoSinDictado() {
        assertEquals(ahora.plusWeeks(12), cohorte.calcularVencimientoAcceso(ahora));
        assertFalse(cohorte.dictadoPorComenzar(ahora));
    }

    @Test
    @DisplayName("CU-45: con fechas de dictado, el acceso vence las semanas de acceso después del inicio del dictado")
    void vencimientoConDictado() {
        cohorte.setFechaInicioDictado(ahora.plusDays(25));
        assertEquals(ahora.plusDays(25).plusWeeks(12), cohorte.calcularVencimientoAcceso(ahora));
        assertTrue(cohorte.dictadoPorComenzar(ahora));
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
