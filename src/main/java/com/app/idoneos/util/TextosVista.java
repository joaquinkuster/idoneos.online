package com.app.idoneos.util;

import org.springframework.stereotype.Component;

/**
 * Funciones de texto para las plantillas (se llaman como {@code @textosVista.cortar(texto, limite)}).
 */
@Component("textosVista")
public class TextosVista {

    /**
     * Corta un texto largo para mostrarlo en una celda: si cabe en el límite lo deja entero; si no, lo corta en el último espacio
     * antes del límite (sin partir una palabra) o, si no hay un espacio razonable, justo en el límite.
     *
     * @param texto  El texto completo (puede ser {@code null}).
     * @param limite La cantidad máxima de caracteres que se muestran.
     * @return El texto cortado, sin espacios al final ni signos de puntuación sueltos; el mismo texto si cabe.
     */
    public String cortar(String texto, int limite) {
        if (texto == null || texto.length() <= limite) {
            return texto;
        }
        int corte = limite;
        int espacio = texto.lastIndexOf(' ', limite);
        // Solo se corta por palabra si no se pierde más de la mitad del límite
        if (espacio >= limite / 2) {
            corte = espacio;
        }
        return texto.substring(0, corte).replaceAll("[\\s,.;:]+$", "");
    }
}
