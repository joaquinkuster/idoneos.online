package com.app.idoneos.utilidades;

import java.text.Normalizer;

/**
 * Métodos auxiliares para comparar y ordenar textos.
 */
public class TextoUtilidad {

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
}
