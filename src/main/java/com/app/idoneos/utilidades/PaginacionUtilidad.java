package com.app.idoneos.utilidades;

import java.util.Collections;
import java.util.List;

/**
 * Métodos auxiliares para paginar listados en memoria.
 */
public class PaginacionUtilidad {

    /**
     * Calcula la cantidad de páginas necesarias para mostrar una cantidad de elementos.
     * Siempre devuelve al menos una página.
     *
     * @param totalElementos La cantidad total de elementos.
     * @param tamanioPagina  La cantidad de elementos por página.
     * @return La cantidad de páginas.
     */
    public static int calcularTotalPaginas(int totalElementos, int tamanioPagina) {
        return Math.max(1, (int) Math.ceil((double) totalElementos / tamanioPagina));
    }

    /**
     * Ajusta un número de página al rango válido (de 0 a la última página).
     *
     * @param pagina       El número de página solicitado (empieza en 0).
     * @param totalPaginas La cantidad total de páginas.
     * @return El número de página ajustado.
     */
    public static int ajustarPagina(int pagina, int totalPaginas) {
        return Math.min(Math.max(pagina, 0), totalPaginas - 1);
    }

    /**
     * Obtiene los elementos de una página de una lista.
     *
     * @param lista         La lista completa.
     * @param pagina        El número de página (empieza en 0).
     * @param tamanioPagina La cantidad de elementos por página.
     * @param <T>           El tipo de los elementos.
     * @return La sublista correspondiente a la página.
     */
    public static <T> List<T> obtenerPagina(List<T> lista, int pagina, int tamanioPagina) {
        int desde = pagina * tamanioPagina;
        if (desde >= lista.size()) {
            return Collections.emptyList();
        }
        return lista.subList(desde, Math.min(desde + tamanioPagina, lista.size()));
    }
}
