package com.app.idoneos.utils;

import java.util.List;

/**
 * Métodos auxiliares genéricos.
 */
public class Utilidades {

    public static <T> List<T> limitar(List<T> lista, int max) {
        return lista.size() > max ? lista.subList(0, max) : lista;
    }
}
