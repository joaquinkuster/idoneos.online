package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las reglas del foro que se derivan del propio modelo: el resumen, las respuestas vigentes, el último
 * mensaje y el plazo de edición.
 */
class ConsultaForoTest {

    private ConsultaForo consulta(String texto) {
        ConsultaForo consulta = new ConsultaForo();
        consulta.setTexto(texto);
        return consulta;
    }

    private RespuestaForo respuesta(ConsultaForo consulta, LocalDateTime fecha, boolean baja) {
        RespuestaForo respuesta = new RespuestaForo();
        respuesta.setConsulta(consulta);
        respuesta.setTexto("Respuesta");
        respuesta.setFechaCreacion(fecha);
        respuesta.setBaja(baja);
        consulta.getRespuestasForo().add(respuesta);
        return respuesta;
    }

    @Test
    @DisplayName("El resumen muestra el comienzo del texto en una sola línea, con puntos suspensivos si es largo")
    void resumen() {
        assertEquals("¿Cómo se calcula?", consulta("¿Cómo se calcula?").getResumen());
        assertEquals("Dos líneas distintas", consulta("Dos   líneas\ndistintas").getResumen());
        String resumen = consulta("x".repeat(300)).getResumen();
        assertTrue(resumen.endsWith("…"));
        assertTrue(resumen.length() <= 91);
    }

    @Test
    @DisplayName("Las respuestas dadas de baja no cuentan: la consulta vuelve a estar sin responder")
    void respuestasVigentes() {
        ConsultaForo consulta = consulta("Consulta");
        assertTrue(consulta.estaSinResponder());

        respuesta(consulta, LocalDateTime.now(), true);
        assertTrue(consulta.estaSinResponder());

        respuesta(consulta, LocalDateTime.now(), false);
        assertFalse(consulta.estaSinResponder());
        assertEquals(1, consulta.getRespuestasVigentes().size());
    }

    @Test
    @DisplayName("El último mensaje es la última respuesta vigente o, sin respuestas, la propia consulta")
    void ultimoMensaje() {
        ConsultaForo consulta = consulta("Consulta");
        LocalDateTime base = LocalDateTime.of(2026, 10, 1, 10, 0);
        consulta.setFechaCreacion(base);
        assertEquals(base, consulta.getFechaUltimoMensaje());

        respuesta(consulta, base.plusDays(1), false);
        respuesta(consulta, base.plusDays(3), false);
        respuesta(consulta, base.plusDays(9), true); // de baja: no cuenta
        assertEquals(base.plusDays(3), consulta.getFechaUltimoMensaje());
        assertEquals(List.of(base.plusDays(1), base.plusDays(3)), consulta.getRespuestasVigentes().stream()
                .map(RespuestaForo::getFechaCreacion).toList());
    }

    @Test
    @DisplayName("Se puede modificar solo dentro del plazo de edición desde el registro")
    void plazoDeEdicion() {
        ConsultaForo consulta = consulta("Consulta");
        consulta.setFechaCreacion(LocalDateTime.now().minusMinutes(10));

        assertTrue(consulta.estaDentroDelPlazo(15));
        assertFalse(consulta.estaDentroDelPlazo(5));
    }
}
