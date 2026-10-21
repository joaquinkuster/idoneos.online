package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la corrección de los intentos de autoevaluación (DSD corregir) y de las reglas derivadas del modelo de
 * evaluaciones: tiempo, resultado y unidad aprobada. No usan la base de datos.
 */
class IntentoAutoevaluacionTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 1, 10, 0);
    private int siguienteId = 1;

    private Autoevaluacion autoevaluacion(int cantidadPreguntas, boolean descuentoPorError, float notaMinima) {
        Autoevaluacion autoevaluacion = new Autoevaluacion(new Unidad(), "Autoevaluación", cantidadPreguntas, notaMinima, INICIO);
        autoevaluacion.setIdAutoevaluacion(siguienteId++);
        autoevaluacion.setDescuentoPorError(descuentoPorError);
        return autoevaluacion;
    }

    /** Crea una pregunta; las opciones correctas llevan un asterisco inicial. */
    private Pregunta pregunta(boolean opcionMultiple, String... opciones) {
        Pregunta pregunta = new Pregunta(new Pool(), "Pregunta " + siguienteId, opcionMultiple);
        pregunta.setIdPregunta(siguienteId++);
        for (String texto : opciones) {
            boolean correcta = texto.startsWith("*");
            OpcionRespuesta opcion = new OpcionRespuesta(pregunta, correcta ? texto.substring(1) : texto, correcta);
            opcion.setIdOpcionRespuesta(siguienteId++);
            pregunta.getOpcionesRespuesta().add(opcion);
        }
        return pregunta;
    }

    private OpcionRespuesta opcion(Pregunta pregunta, boolean correcta) {
        return pregunta.getOpcionesActivas().stream().filter(o -> o.getEsCorrecta() == correcta).findFirst().orElseThrow();
    }

    private IntentoAutoevaluacion intento(Autoevaluacion autoevaluacion) {
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(new Inscripcion(), autoevaluacion);
        intento.setIdIntentoAutoevaluacion(siguienteId++);
        intento.setFechaCreacion(INICIO);
        autoevaluacion.getIntentosAutoevaluacion().add(intento);
        return intento;
    }

    private PreguntaIntento sortear(IntentoAutoevaluacion intento, Pregunta pregunta, OpcionRespuesta... elegidas) {
        PreguntaIntento preguntaIntento = new PreguntaIntento(intento, pregunta, intento.getPreguntasIntento().size() + 1);
        for (OpcionRespuesta elegida : elegidas) {
            preguntaIntento.getRespuestasIntento().add(new RespuestaIntento(preguntaIntento, elegida));
        }
        intento.getPreguntasIntento().add(preguntaIntento);
        return preguntaIntento;
    }

    /** Intento de 5 preguntas de opción única: 3 bien, 1 mal y 1 sin responder. */
    private IntentoAutoevaluacion intentoConTresBienUnaMalUnaVacia(Autoevaluacion autoevaluacion) {
        IntentoAutoevaluacion intento = intento(autoevaluacion);
        for (int i = 0; i < 5; i++) {
            Pregunta pregunta = pregunta(false, "*Correcta", "Incorrecta", "Otra");
            if (i < 3) {
                sortear(intento, pregunta, opcion(pregunta, true));
            } else if (i == 3) {
                sortear(intento, pregunta, opcion(pregunta, false));
            } else {
                sortear(intento, pregunta);
            }
        }
        return intento;
    }

    @Test
    @DisplayName("Sin descuento por error, cada correcta suma 1/N y las incorrectas y las vacías no restan")
    void correccionSinDescuento() {
        Autoevaluacion autoevaluacion = autoevaluacion(5, false, 6f);
        IntentoAutoevaluacion intento = intentoConTresBienUnaMalUnaVacia(autoevaluacion);

        intento.corregir();

        assertEquals(6f, intento.getNota());
        assertTrue(intento.getAprobado());
    }

    @Test
    @DisplayName("Con descuento por error, cada incorrecta resta lo que vale una correcta de su pregunta")
    void correccionConDescuento() {
        Autoevaluacion autoevaluacion = autoevaluacion(5, true, 6f);
        IntentoAutoevaluacion intento = intentoConTresBienUnaMalUnaVacia(autoevaluacion);

        intento.corregir();

        assertEquals(4f, intento.getNota()); // (3 - 1) / 5 * 10
        assertFalse(intento.getAprobado());
    }

    @Test
    @DisplayName("En las preguntas de opción múltiple el valor de la pregunta se reparte entre sus opciones correctas")
    void correccionDeOpcionMultiple() {
        Autoevaluacion autoevaluacion = autoevaluacion(2, true, 5f);
        Pregunta multiple = pregunta(true, "*Correcta A", "*Correcta B", "Incorrecta A", "Incorrecta B");
        Pregunta unica = pregunta(false, "*Correcta", "Incorrecta");

        IntentoAutoevaluacion unaCorrecta = intento(autoevaluacion);
        sortear(unaCorrecta, multiple, opcion(multiple, true));
        sortear(unaCorrecta, unica);
        unaCorrecta.corregir();
        assertEquals(2.5f, unaCorrecta.getNota()); // 1 de las 2 correctas: la mitad de lo que vale la pregunta (5)

        IntentoAutoevaluacion correctaEIncorrecta = intento(autoevaluacion);
        sortear(correctaEIncorrecta, multiple, opcion(multiple, true), opcion(multiple, false));
        sortear(correctaEIncorrecta, unica);
        correctaEIncorrecta.corregir();
        assertEquals(0f, correctaEIncorrecta.getNota()); // lo que suma la correcta lo resta la incorrecta

        IntentoAutoevaluacion todasLasCorrectas = intento(autoevaluacion);
        PreguntaIntento p1 = sortear(todasLasCorrectas, multiple);
        multiple.getOpcionesActivas().stream().filter(OpcionRespuesta::getEsCorrecta)
                .forEach(o -> p1.getRespuestasIntento().add(new RespuestaIntento(p1, o)));
        sortear(todasLasCorrectas, unica, opcion(unica, true));
        todasLasCorrectas.corregir();
        assertEquals(10f, todasLasCorrectas.getNota());
    }

    @Test
    @DisplayName("La nota nunca es menor a cero y un intento sin respuestas obtiene cero")
    void notaMinimaCero() {
        Autoevaluacion autoevaluacion = autoevaluacion(2, true, 4f);
        IntentoAutoevaluacion todasMal = intento(autoevaluacion);
        for (int i = 0; i < 2; i++) {
            Pregunta pregunta = pregunta(false, "*Correcta", "Incorrecta");
            sortear(todasMal, pregunta, opcion(pregunta, false));
        }
        todasMal.corregir();
        assertEquals(0f, todasMal.getNota());
        assertFalse(todasMal.getAprobado());

        IntentoAutoevaluacion sinResponder = intento(autoevaluacion);
        sortear(sinResponder, pregunta(false, "*Correcta", "Incorrecta"));
        sinResponder.corregir();
        assertEquals(0f, sinResponder.getNota());
    }

    @Test
    @DisplayName("La nota se expresa con dos decimales")
    void notaConDosDecimales() {
        Autoevaluacion autoevaluacion = autoevaluacion(3, false, 3f);
        IntentoAutoevaluacion intento = intento(autoevaluacion);
        Pregunta pregunta = pregunta(false, "*Correcta", "Incorrecta");
        sortear(intento, pregunta, opcion(pregunta, true));

        intento.corregir();

        assertEquals(3.33f, intento.getNota());
        assertTrue(intento.getAprobado());
    }

    @Test
    @DisplayName("Una pregunta es válida si tiene al menos dos opciones y las correctas que pide su tipo")
    void validezDeLasPreguntas() {
        assertTrue(pregunta(false, "*Verdadero", "Falso").esValida());
        assertTrue(pregunta(true, "*A", "*B", "C").esValida());
        assertFalse(pregunta(false, "*A", "*B", "C").esValida()); // opción única con dos correctas
        assertFalse(pregunta(true, "A", "B").esValida()); // sin correctas
        assertFalse(pregunta(false, "*Sola").esValida()); // menos de dos opciones
        Pregunta conBaja = pregunta(false, "*A", "B", "C");
        conBaja.getOpcionesRespuesta().stream().filter(o -> !o.getEsCorrecta()).forEach(OpcionRespuesta::marcarInactivo);
        assertFalse(conBaja.esValida()); // las opciones dadas de baja no cuentan
    }

    @Test
    @DisplayName("El resultado distingue en curso, aprobado, aprobado por el docente y no aprobado")
    void resultadoDelIntento() {
        Autoevaluacion autoevaluacion = autoevaluacion(5, false, 6f);
        IntentoAutoevaluacion intento = intento(autoevaluacion);
        assertTrue(intento.estaEnCurso());
        assertEquals(IntentoAutoevaluacion.RESULTADO_EN_CURSO, intento.getResultado());

        intento.setFechaEntrega(INICIO.plusMinutes(20));
        intento.setNota(4f);
        assertFalse(intento.estaEnCurso());
        assertEquals(IntentoAutoevaluacion.RESULTADO_NO_APROBADO, intento.getResultado());

        intento.setAprobado(true); // aprobación manual: aprobado con una nota menor a la mínima
        assertTrue(intento.esAprobadoPorDocente());
        assertEquals(IntentoAutoevaluacion.RESULTADO_APROBADO_POR_DOCENTE, intento.getResultado());

        intento.setNota(8f);
        assertFalse(intento.esAprobadoPorDocente());
        assertEquals(IntentoAutoevaluacion.RESULTADO_APROBADO, intento.getResultado());

        intento.marcarInactivo();
        assertFalse(intento.estaEnCurso());
    }

    @Test
    @DisplayName("El tiempo corre desde el inicio y se admite una tolerancia para las entregas con demora")
    void tiempoDelIntento() {
        Autoevaluacion autoevaluacion = autoevaluacion(5, false, 6f);
        autoevaluacion.setTiempoLimite(30);
        IntentoAutoevaluacion intento = intento(autoevaluacion);

        assertEquals(INICIO.plusMinutes(30), intento.getVencimiento());
        assertEquals(1200L, intento.getSegundosRestantes(INICIO.plusMinutes(10)));
        assertEquals(0L, intento.getSegundosRestantes(INICIO.plusMinutes(45)));
        assertFalse(intento.haVencido(INICIO.plusMinutes(30).plusSeconds(15), 15));
        assertTrue(intento.haVencido(INICIO.plusMinutes(30).plusSeconds(16), 15));

        autoevaluacion.setTiempoLimite(null);
        assertNull(intento.getVencimiento());
        assertNull(intento.getSegundosRestantes(INICIO));
        assertFalse(intento.haVencido(INICIO.plusYears(1), 15));
    }

    @Test
    @DisplayName("La unidad se aprueba cuando el alumno aprobó todas sus autoevaluaciones visibles")
    void unidadAprobada() {
        Unidad unidad = new Unidad();
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setIdInscripcion(1);
        Autoevaluacion primera = autoevaluacion(5, false, 6f);
        Autoevaluacion segunda = autoevaluacion(5, false, 6f);
        unidad.getAutoevaluaciones().add(primera);
        unidad.getAutoevaluaciones().add(segunda);
        assertFalse(unidad.estaAprobadaPor(inscripcion));

        aprobar(primera, inscripcion);
        assertFalse(unidad.estaAprobadaPor(inscripcion)); // falta la segunda

        segunda.setOculto(true);
        assertTrue(unidad.estaAprobadaPor(inscripcion)); // la oculta no se exige

        segunda.setOculto(false);
        aprobar(segunda, inscripcion);
        assertTrue(unidad.estaAprobadaPor(inscripcion));

        primera.marcarInactivo();
        segunda.marcarInactivo();
        assertFalse(unidad.estaAprobadaPor(inscripcion)); // sin autoevaluaciones visibles no hay nada que aprobar
    }

    private void aprobar(Autoevaluacion autoevaluacion, Inscripcion inscripcion) {
        IntentoAutoevaluacion intento = intento(autoevaluacion);
        intento.setInscripcion(inscripcion);
        intento.setNota(10f);
        intento.setAprobado(true);
        intento.setFechaEntrega(INICIO.plusMinutes(10));
    }
}
