package com.app.idoneos.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BM25: puntúa qué tan relevante es cada oración de un documento respecto de una consulta (un conjunto de lemas). Es un TF-IDF con dos correcciones:
 * una palabra repetida en una oración pesa cada vez menos, y se descuenta el largo de la oración para que una larga no gane solo por tener más palabras.
 */
public final class Bm25Util {

    private Bm25Util() {
    }

    /** Cuánto se frena el peso de una palabra repetida. */
    private static final double K1 = 1.2;

    /** Cuánto pesa el largo de la oración (0 no lo tiene en cuenta y 1 lo descuenta por completo). */
    private static final double B = 0.75;

    /**
     * El puntaje de cada oración respecto de la consulta.
     *
     * @param lemasPorOracion Los lemas de contenido de cada oración del documento.
     * @param consulta Los lemas de lo que se busca.
     * @return El puntaje de cada oración, en el mismo orden; 0 si no tiene ningún lema de la consulta.
     */
    public static double[] puntajes(List<List<String>> lemasPorOracion, Set<String> consulta) {
        int n = lemasPorOracion.size();
        Map<String, Integer> df = new HashMap<>();
        double largoTotal = 0;
        for (List<String> lemas : lemasPorOracion) {
            largoTotal += lemas.size();
            new HashSet<>(lemas).forEach(lema -> df.merge(lema, 1, Integer::sum));
        }
        double largoMedio = n == 0 ? 0 : largoTotal / n;
        double[] puntajes = new double[n];
        for (int i = 0; i < n; i++) {
            Map<String, Integer> tf = new HashMap<>();
            lemasPorOracion.get(i).forEach(lema -> tf.merge(lema, 1, Integer::sum));
            double largo = lemasPorOracion.get(i).size();
            for (String lema : consulta) {
                Integer veces = tf.get(lema);
                if (veces != null) {
                    double idf = Math.log(1 + (n - df.get(lema) + 0.5) / (df.get(lema) + 0.5));
                    puntajes[i] += idf * (veces * (K1 + 1)) / (veces + K1 * (1 - B + B * largo / largoMedio));
                }
            }
        }
        return puntajes;
    }
}
