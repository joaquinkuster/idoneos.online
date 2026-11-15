package com.app.idoneos.util;

import java.util.ArrayDeque;
import java.util.Deque;

/** Utilidad para recuperar lo que llegó completo de un JSON que el modelo dejó cortado al quedarse sin espacio para responder. */
public final class JsonTruncadoUtil {

    private JsonTruncadoUtil() {
    }

    /**
     * Cierra un JSON cortado a partir del último objeto que quedó completo.
     * Lo que venía después, que está a medias, se descarta, y se cierran los corchetes y llaves que quedaron abiertos.
     *
     * @param json El texto recibido.
     * @return El JSON reparado. Si el texto ya estaba cerrado, o no tiene ningún objeto completo, se devuelve igual.
     */
    public static String reparar(String json) {
        Deque<Character> abiertos = new ArrayDeque<>();
        boolean enTexto = false;
        boolean escapado = false;
        int ultimoCompleto = -1;
        String cierre = "";
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (enTexto) {
                if (escapado) {
                    escapado = false;
                } else if (c == '\\') {
                    escapado = true;
                } else if (c == '"') {
                    enTexto = false;
                }
                continue;
            }
            if (c == '"') {
                enTexto = true;
            } else if (c == '{' || c == '[') {
                abiertos.push(c);
            } else if ((c == '}' || c == ']') && !abiertos.isEmpty()) {
                abiertos.pop();
                if (c == '}') {
                    ultimoCompleto = i + 1;
                    cierre = cerrar(abiertos);
                }
            }
        }
        if (!enTexto && abiertos.isEmpty()) {
            return json;
        }
        return ultimoCompleto < 0 ? json : json.substring(0, ultimoCompleto) + cierre;
    }

    private static String cerrar(Deque<Character> abiertos) {
        StringBuilder cierre = new StringBuilder();
        for (char abierto : abiertos) {
            cierre.append(abierto == '{' ? '}' : ']');
        }
        return cierre.toString();
    }
}
