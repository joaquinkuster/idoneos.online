package com.app.idoneos.util;

import java.text.Normalizer;
import org.springframework.stereotype.Component;

/** Utilidad de textos. Sirve para comparar, ordenar y cortar textos. */
@Component("textoUtil")
public class TextoUtil {

    /**
     * Pasa un texto a minúsculas y sin tildes, para compararlo o buscarlo sin distinguirlas.
     *
     * @param texto El texto (puede ser {@code null}).
     * @return El texto normalizado, o una cadena vacía si es {@code null}.
     */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }

    /** Corta un texto largo para mostrarlo en una celda, sin partir una palabra. */
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
        return texto.substring(0, corte).replaceAll("[\s,.;:]+$", "");
    }
}
