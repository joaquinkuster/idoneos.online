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
 * temas, días del cronograma, día esperado y atraso del alumno.
 */
class InscripcionTest {

    private final LocalDateTime ahora = LocalDateTime.now();

    private Programa programa;
    private Cohorte cohorte;
    private Inscripcion inscripcion;
    private TemaCronograma tema1;
    private TemaCronograma tema2;
    private TemaCronograma tema3;

    /** Programa de 63 días (temas de 14, 21 y 28 días) con una cohorte que empezó a dictarse hace 20 días. */
    @BeforeEach
    void armarPrograma() {
        programa = new Programa(null, "Programa 2026", "Objetivos", "Bibliografía");
        programa.setIdPrograma(1);
        tema1 = agregarTema(1, 1, 14);
        tema2 = agregarTema(2, 2, 21);
        tema3 = agregarTema(3, 3, 28);
        cohorte = new Cohorte(programa, ahora.minusDays(60), ahora.minusDays(30), 84);
        cohorte.setFechaInicioDictado(ahora.minusDays(20));
        inscripcion = new Inscripcion(cohorte, null, ahora.plusWeeks(12));
    }

    private TemaCronograma agregarTema(int id, int orden, int dias) {
        Tema tema = new Tema(null, "Tema " + id, "Contenido");
        tema.setIdTema(id);
        TemaCronograma cronograma = new TemaCronograma(programa, tema, orden, dias);
        programa.getTemasCronograma().add(cronograma);
        return cronograma;
    }

    private void completar(TemaCronograma cronograma) {
        Progreso progreso = new Progreso(cronograma.getTema(), inscripcion);
        progreso.setCompletada(true);
        inscripcion.getProgresos().add(progreso);
    }

    @Test
    @DisplayName("Los temas se habilitan de a una, según el avance secuencial del alumno")
    void habilitacionSecuencial() {
        assertTrue(inscripcion.estaTemaHabilitada(tema1));
        assertFalse(inscripcion.estaTemaHabilitada(tema2));
        assertFalse(inscripcion.estaTemaHabilitada(tema3));
        assertEquals("En curso", inscripcion.getEstadoTema(tema1));
        assertEquals("Bloqueada", inscripcion.getEstadoTema(tema2));
        assertEquals(tema1, inscripcion.getTemaEnCurso());

        completar(tema1);

        assertTrue(inscripcion.estaTemaCompletada(tema1.getTema()));
        assertTrue(inscripcion.estaTemaHabilitada(tema2));
        assertFalse(inscripcion.estaTemaHabilitada(tema3));
        assertEquals("Completada", inscripcion.getEstadoTema(tema1));
        assertEquals("En curso", inscripcion.getEstadoTema(tema2));
        assertEquals(tema2, inscripcion.getTemaEnCurso());
    }

    @Test
    @DisplayName("Con todos los temas completados no queda ninguna en curso")
    void sinTemaEnCurso() {
        completar(tema1);
        completar(tema2);
        completar(tema3);

        assertNull(inscripcion.getTemaEnCurso());
        assertEquals(3, inscripcion.getUltimaTemaCompletada());
    }

    @Test
    @DisplayName("Los días de cada tema se acumulan a lo largo del programa")
    void semanasDelCronograma() {
        assertEquals(63, programa.getDuracionTotalDias());
        assertEquals(1, tema1.getDiaDesde());
        assertEquals(14, tema1.getDiaHasta());
        assertEquals(15, tema2.getDiaDesde());
        assertEquals(35, tema2.getDiaHasta());
        assertEquals(36, tema3.getDiaDesde());
        assertEquals(63, tema3.getDiaHasta());
    }

    @Test
    @DisplayName("Un cronograma sin temas vigentes ignora los temas dados de baja")
    void ignoraTemasDeBaja() {
        tema3.marcarInactivo();

        assertEquals(2, programa.getCronogramaOrdenado().size());
        assertEquals(35, programa.getDuracionTotalDias());
    }

    @Test
    @DisplayName("El día esperado se cuenta desde el inicio del dictado y no pasa de la duración del programa")
    void semanaEsperada() {
        assertEquals(21, cohorte.getDiasTranscurridos());
        assertEquals(21, cohorte.getDiaActual());

        cohorte.setFechaInicioDictado(ahora.minusDays(200));

        assertEquals(201, cohorte.getDiasTranscurridos());
        assertEquals(63, cohorte.getDiaActual());
    }

    @Test
    @DisplayName("Sin fecha de dictado, el día esperado se cuenta desde el inicio de la inscripción")
    void semanaEsperadaDesdeLaInscripcion() {
        cohorte.setFechaInicioDictado(null);

        assertEquals(cohorte.getFechaInicioInscripcion(), cohorte.getFechaBaseCronograma());
        assertEquals(61, cohorte.getDiasTranscurridos());
    }

    @Test
    @DisplayName("El alumno que no completó lo que ya debería estar terminado va atrasado")
    void alumnoAtrasado() {
        assertEquals(1, inscripcion.getTemasEsperadasTerminadas());
        assertEquals(0, inscripcion.getUltimaTemaCompletada());
        assertTrue(inscripcion.estaAtrasada());
        assertEquals("Atrasada", inscripcion.getEstadoCronograma(tema1));
        assertEquals("Actual", inscripcion.getEstadoCronograma(tema2));
        assertEquals("Próxima", inscripcion.getEstadoCronograma(tema3));
    }

    @Test
    @DisplayName("El alumno que completó lo esperado va al día")
    void alumnoAlDia() {
        completar(tema1);

        assertFalse(inscripcion.estaAtrasada());
        assertEquals("Completada", inscripcion.getEstadoCronograma(tema1));
    }

    @Test
    @DisplayName("Los días de atraso son el día actual menos el último día del primer tema sin completar")
    void diasDeAtraso() {
        // día 21 y sigue en el tema 1 (que terminaba el día 14): 7 días de atraso
        assertEquals(7, inscripcion.getDiasDeAtraso());

        cohorte.setFechaInicioDictado(ahora.minusDays(40));
        // día 41 y sigue en el tema 1: 27 días de atraso
        assertEquals(27, inscripcion.getDiasDeAtraso());

        completar(tema1);
        // día 41 con el tema 2 (hasta el día 35) sin completar: 6 días de atraso
        assertEquals(6, inscripcion.getDiasDeAtraso());

        completar(tema2);
        completar(tema3);
        assertEquals(0, inscripcion.getDiasDeAtraso());
    }

    @Test
    @DisplayName("Quien está al día no tiene días de atraso")
    void sinAtraso() {
        completar(tema1);

        assertEquals(0, inscripcion.getDiasDeAtraso());
    }

    @Test
    @DisplayName("El estado resumido del alumno es Completada, En curso o Pendiente según su avance")
    void estadoResumidoDelAlumno() {
        assertEquals("En curso", inscripcion.getEstadoTemaResumido(tema1));
        assertEquals("Pendiente", inscripcion.getEstadoTemaResumido(tema2));

        completar(tema1);

        assertEquals("Completada", inscripcion.getEstadoTemaResumido(tema1));
        assertEquals("En curso", inscripcion.getEstadoTemaResumido(tema2));
        assertEquals("Pendiente", inscripcion.getEstadoTemaResumido(tema3));
    }

    @Test
    @DisplayName("El estado resumido del calendario de la cohorte depende del día actual")
    void estadoResumidoDelCalendario() {
        // día 21: el tema 1 ya pasó, el 2 está en curso y el 3 falta
        assertEquals("Completada", cohorte.getEstadoTemaResumido(tema1));
        assertEquals("En curso", cohorte.getEstadoTemaResumido(tema2));
        assertEquals("Pendiente", cohorte.getEstadoTemaResumido(tema3));
    }

    @Test
    @DisplayName("Un cronograma que todavía no comenzó deja todos los temas pendientes")
    void cronogramaSinComenzar() {
        cohorte.setFechaInicioDictado(ahora.plusDays(10));

        assertFalse(cohorte.haComenzadoCronograma());
        assertFalse(cohorte.haFinalizadoCronograma());
        assertEquals("Pendiente", cohorte.getEstadoTemaResumido(tema1));
        assertEquals("Pendiente", cohorte.getEstadoTemaResumido(tema2));
    }

    @Test
    @DisplayName("El cronograma comenzó o finalizó según los días transcurridos")
    void cronogramaEnCursoYFinalizado() {
        assertTrue(cohorte.haComenzadoCronograma());
        assertFalse(cohorte.haFinalizadoCronograma());

        cohorte.setFechaInicioDictado(ahora.minusDays(200));

        assertTrue(cohorte.haFinalizadoCronograma());
        assertEquals("Completada", cohorte.getEstadoTemaResumido(tema3));
    }
}
