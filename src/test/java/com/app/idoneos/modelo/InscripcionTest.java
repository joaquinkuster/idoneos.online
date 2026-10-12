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
 * Pruebas unitarias de las reglas del acceso al curso (CU-27) que calcula el modelo: habilitación secuencial de
 * unidades, semanas del cronograma, semana esperada y atraso del alumno.
 */
class InscripcionTest {

    private final LocalDateTime ahora = LocalDateTime.now();

    private Programa programa;
    private Cohorte cohorte;
    private Inscripcion inscripcion;
    private UnidadCronograma unidad1;
    private UnidadCronograma unidad2;
    private UnidadCronograma unidad3;

    /** Programa de 9 semanas (unidades de 2, 3 y 4 semanas) con una cohorte que empezó a dictarse hace 20 días. */
    @BeforeEach
    void armarPrograma() {
        programa = new Programa(null, "Programa 2026", "Objetivos", "Bibliografía");
        programa.setIdPrograma(1);
        unidad1 = agregarUnidad(1, 1, 2);
        unidad2 = agregarUnidad(2, 2, 3);
        unidad3 = agregarUnidad(3, 3, 4);
        cohorte = new Cohorte(programa, ahora.minusDays(60), ahora.minusDays(30), 12);
        cohorte.setFechaInicioDictado(ahora.minusDays(20));
        inscripcion = new Inscripcion(cohorte, null, ahora.plusWeeks(12));
    }

    private UnidadCronograma agregarUnidad(int id, int orden, int semanas) {
        Unidad unidad = new Unidad(null, "Unidad " + id, "Contenido");
        unidad.setIdUnidad(id);
        UnidadCronograma cronograma = new UnidadCronograma(programa, unidad, orden, semanas);
        programa.getUnidadesCronograma().add(cronograma);
        return cronograma;
    }

    private void completar(UnidadCronograma cronograma) {
        Progreso progreso = new Progreso(cronograma.getUnidad(), inscripcion);
        progreso.setCompletada(true);
        inscripcion.getProgresos().add(progreso);
    }

    @Test
    @DisplayName("Las unidades se habilitan de a una, según el avance secuencial del alumno")
    void habilitacionSecuencial() {
        assertTrue(inscripcion.estaUnidadHabilitada(unidad1));
        assertFalse(inscripcion.estaUnidadHabilitada(unidad2));
        assertFalse(inscripcion.estaUnidadHabilitada(unidad3));
        assertEquals("En curso", inscripcion.getEstadoUnidad(unidad1));
        assertEquals("Bloqueada", inscripcion.getEstadoUnidad(unidad2));
        assertEquals(unidad1, inscripcion.getUnidadEnCurso());

        completar(unidad1);

        assertTrue(inscripcion.estaUnidadCompletada(unidad1.getUnidad()));
        assertTrue(inscripcion.estaUnidadHabilitada(unidad2));
        assertFalse(inscripcion.estaUnidadHabilitada(unidad3));
        assertEquals("Completada", inscripcion.getEstadoUnidad(unidad1));
        assertEquals("En curso", inscripcion.getEstadoUnidad(unidad2));
        assertEquals(unidad2, inscripcion.getUnidadEnCurso());
    }

    @Test
    @DisplayName("Con todas las unidades completadas no queda ninguna en curso")
    void sinUnidadEnCurso() {
        completar(unidad1);
        completar(unidad2);
        completar(unidad3);

        assertNull(inscripcion.getUnidadEnCurso());
        assertEquals(3, inscripcion.getUltimaUnidadCompletada());
    }

    @Test
    @DisplayName("Las semanas de cada unidad se acumulan a lo largo del programa")
    void semanasDelCronograma() {
        assertEquals(9, programa.getDuracionTotalSemanas());
        assertEquals(1, unidad1.getSemanaDesde());
        assertEquals(2, unidad1.getSemanaHasta());
        assertEquals(3, unidad2.getSemanaDesde());
        assertEquals(5, unidad2.getSemanaHasta());
        assertEquals(6, unidad3.getSemanaDesde());
        assertEquals(9, unidad3.getSemanaHasta());
    }

    @Test
    @DisplayName("Un cronograma sin unidades vigentes ignora las unidades dadas de baja")
    void ignoraUnidadesDeBaja() {
        unidad3.marcarInactivo();

        assertEquals(2, programa.getCronogramaOrdenado().size());
        assertEquals(5, programa.getDuracionTotalSemanas());
    }

    @Test
    @DisplayName("La semana esperada se cuenta desde el inicio del dictado y no pasa de la duración del programa")
    void semanaEsperada() {
        assertEquals(3, cohorte.getSemanasTranscurridas());
        assertEquals(3, cohorte.getSemanaActual());

        cohorte.setFechaInicioDictado(ahora.minusDays(200));

        assertEquals(29, cohorte.getSemanasTranscurridas());
        assertEquals(9, cohorte.getSemanaActual());
    }

    @Test
    @DisplayName("Sin fecha de dictado, la semana esperada se cuenta desde el inicio de la inscripción")
    void semanaEsperadaDesdeLaInscripcion() {
        cohorte.setFechaInicioDictado(null);

        assertEquals(cohorte.getFechaInicioInscripcion(), cohorte.getFechaBaseCronograma());
        assertEquals(9, cohorte.getSemanasTranscurridas());
    }

    @Test
    @DisplayName("El alumno que no completó lo que ya debería estar terminado va atrasado")
    void alumnoAtrasado() {
        assertEquals(1, inscripcion.getUnidadesEsperadasTerminadas());
        assertEquals(0, inscripcion.getUltimaUnidadCompletada());
        assertTrue(inscripcion.estaAtrasada());
        assertEquals("Atrasada", inscripcion.getEstadoCronograma(unidad1));
        assertEquals("Semana actual", inscripcion.getEstadoCronograma(unidad2));
        assertEquals("Próxima", inscripcion.getEstadoCronograma(unidad3));
    }

    @Test
    @DisplayName("El alumno que completó lo esperado va al día")
    void alumnoAlDia() {
        completar(unidad1);

        assertFalse(inscripcion.estaAtrasada());
        assertEquals("Completada", inscripcion.getEstadoCronograma(unidad1));
    }

    @Test
    @DisplayName("Las semanas de atraso son la semana actual menos la última semana de la primera unidad sin completar")
    void semanasDeAtraso() {
        // semana 3 y sigue en la unidad 1 (que terminaba en la semana 2): 1 semana de atraso
        assertEquals(1, inscripcion.getSemanasDeAtraso());

        cohorte.setFechaInicioDictado(ahora.minusDays(40));
        // semana 6 y sigue en la unidad 1: 4 semanas de atraso
        assertEquals(4, inscripcion.getSemanasDeAtraso());

        completar(unidad1);
        // semana 6 con la unidad 2 (hasta la semana 5) sin completar: 1 semana de atraso
        assertEquals(1, inscripcion.getSemanasDeAtraso());

        completar(unidad2);
        completar(unidad3);
        assertEquals(0, inscripcion.getSemanasDeAtraso());
    }

    @Test
    @DisplayName("Quien está al día no tiene semanas de atraso")
    void sinAtraso() {
        completar(unidad1);

        assertEquals(0, inscripcion.getSemanasDeAtraso());
    }

    @Test
    @DisplayName("El estado resumido del alumno es Completada, En curso o Pendiente según su avance")
    void estadoResumidoDelAlumno() {
        assertEquals("En curso", inscripcion.getEstadoUnidadResumido(unidad1));
        assertEquals("Pendiente", inscripcion.getEstadoUnidadResumido(unidad2));

        completar(unidad1);

        assertEquals("Completada", inscripcion.getEstadoUnidadResumido(unidad1));
        assertEquals("En curso", inscripcion.getEstadoUnidadResumido(unidad2));
        assertEquals("Pendiente", inscripcion.getEstadoUnidadResumido(unidad3));
    }

    @Test
    @DisplayName("El estado resumido del calendario de la cohorte depende de la semana actual")
    void estadoResumidoDelCalendario() {
        // semana 3: la unidad 1 ya pasó, la 2 está en curso y la 3 falta
        assertEquals("Completada", cohorte.getEstadoUnidadResumido(unidad1));
        assertEquals("En curso", cohorte.getEstadoUnidadResumido(unidad2));
        assertEquals("Pendiente", cohorte.getEstadoUnidadResumido(unidad3));
    }

    @Test
    @DisplayName("Un cronograma que todavía no comenzó deja todas las unidades pendientes")
    void cronogramaSinComenzar() {
        cohorte.setFechaInicioDictado(ahora.plusDays(10));

        assertFalse(cohorte.haComenzadoCronograma());
        assertFalse(cohorte.haFinalizadoCronograma());
        assertEquals("Pendiente", cohorte.getEstadoUnidadResumido(unidad1));
        assertEquals("Pendiente", cohorte.getEstadoUnidadResumido(unidad2));
    }

    @Test
    @DisplayName("El cronograma comenzó o finalizó según las semanas transcurridas")
    void cronogramaEnCursoYFinalizado() {
        assertTrue(cohorte.haComenzadoCronograma());
        assertFalse(cohorte.haFinalizadoCronograma());

        cohorte.setFechaInicioDictado(ahora.minusDays(200));

        assertTrue(cohorte.haFinalizadoCronograma());
        assertEquals("Completada", cohorte.getEstadoUnidadResumido(unidad3));
    }
}
