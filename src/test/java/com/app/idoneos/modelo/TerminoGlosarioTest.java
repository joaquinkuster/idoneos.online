package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las reglas del glosario que se derivan del propio modelo: la letra del índice y la última actualización.
 */
class TerminoGlosarioTest {

    private TerminoGlosario termino(String texto) {
        return new TerminoGlosario(new Tema(), texto, "Definición");
    }

    @Test
    @DisplayName("La letra del índice es la inicial en mayúscula, sin tilde, y la Ñ cuenta como N")
    void letraInicial() {
        assertEquals("B", termino("BYMA").getLetraInicial());
        assertEquals("T", termino("tasa").getLetraInicial());
        assertEquals("A", termino("Árbol de decisión").getLetraInicial());
        assertEquals("N", termino("Ñandú").getLetraInicial());
    }

    @Test
    @DisplayName("Los términos que no empiezan con una letra de la A a la Z van al grupo Especial")
    void letraEspecial() {
        assertEquals(TerminoGlosario.LETRA_ESPECIAL, termino("401(k)").getLetraInicial());
        assertEquals(TerminoGlosario.LETRA_ESPECIAL, termino("$ dólar").getLetraInicial());
        assertEquals(TerminoGlosario.LETRA_ESPECIAL, termino("  ").getLetraInicial());
    }

    @Test
    @DisplayName("La última actualización es la modificación o, si nunca se modificó, la creación")
    void ultimaActualizacion() {
        TerminoGlosario termino = termino("TIR");
        assertEquals(termino.getFechaCreacion(), termino.getUltimaActualizacion());

        LocalDateTime modificado = LocalDateTime.now().plusDays(1);
        termino.setUltimaModificacion(modificado);
        assertEquals(modificado, termino.getUltimaActualizacion());
    }

    @Test
    @DisplayName("El tema sin términos no tiene fecha de última actualización del glosario")
    void glosarioSinTerminos() {
        assertNull(new Tema().getUltimaActualizacionGlosario());
    }
}
