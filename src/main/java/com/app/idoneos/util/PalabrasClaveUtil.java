package com.app.idoneos.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Saca las palabras clave de un texto con TF-IDF, sin listas de palabras comunes: solo cuentan las categorías gramaticales que dicen de qué trata. Cada oración es un documento: el peso de una palabra es cuántas
 * veces aparece en el texto (TF) por ln(N / df), con N oraciones y df las que la contienen (IDF). Una palabra que está en todas las oraciones pesa
 * cero, así que los artículos, las preposiciones y las palabras genéricas del tema quedan abajo sin que haya que nombrarlas.
 */
public final class PalabrasClaveUtil {

    /** Largo mínimo de una palabra de contenido. */
    private static final int LARGO_MINIMO = 4;

    /** Una palabra es frecuente si aparece en al menos este porcentaje de las oraciones del texto (y en tres como mínimo). */
    private static final int PORCENTAJE_FRECUENTE = 10;

    private PalabrasClaveUtil() {
    }

    private static List<String> palabras(String texto) {
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT);
        List<String> palabras = new ArrayList<>();
        for (String palabra : normalizado.split("[^a-z0-9]+")) {
            if (palabra.length() >= LARGO_MINIMO) {
                palabras.add(palabra);
            }
        }
        return palabras;
    }

    /**
     * Las palabras de una oración que dicen de qué trata (sustantivos, nombres propios, verbos y adjetivos, según el etiquetador de OpenNLP), sin tildes y
     * en minúscula, tal como están escritas. Los artículos, las preposiciones y las conjunciones no entran.
     */
    private static List<String> palabrasDeContenido(String oracion) {
        GramaticaUtil.Analisis analisis = GramaticaUtil.analizar(oracion);
        List<String> palabras = new ArrayList<>();
        for (int i = 0; i < analisis.palabras().length; i++) {
            String categoria = analisis.categorias()[i];
            String palabra = LogicaPreguntasAurea.normalizar(analisis.palabras()[i]);
            if ((categoria.equals("NOUN") || categoria.equals("PROPN") || categoria.equals("VERB") || categoria.equals("ADJ")) && palabra.length() >= 3
                    && palabra.matches("[a-z0-9]+")) {
                palabras.add(palabra);
            }
        }
        return palabras;
    }

    /**
     * Las palabras de 4 letras o más que aparecen en muchas de las oraciones dadas: las que no dicen de qué trata cada una, como las preposiciones, los
     * conectores o las palabras que están en todo el documento. Se calcula sobre las propias oraciones, no con una lista.
     *
     * @param oraciones Las oraciones del texto.
     * @return Las palabras frecuentes, sin tildes y en minúscula.
     */
    public static Set<String> frecuentes(Collection<String> oraciones) {
        Map<String, Integer> oracionesConLaPalabra = new HashMap<>();
        for (String oracion : oraciones) {
            new HashSet<>(palabras(oracion)).forEach(palabra -> oracionesConLaPalabra.merge(palabra, 1, Integer::sum));
        }
        Set<String> frecuentes = new HashSet<>();
        int minimo = Math.max(3, (int) Math.ceil(oraciones.size() * PORCENTAJE_FRECUENTE / 100.0));
        oracionesConLaPalabra.forEach((palabra, cantidad) -> {
            if (cantidad >= minimo) {
                frecuentes.add(palabra);
            }
        });
        return frecuentes;
    }

    /**
     * Saca las palabras clave de un texto.
     *
     * @param texto El texto.
     * @param cantidad Cuántas palabras devolver como máximo.
     * @return Las palabras, sin tildes y en minúscula, de la de mayor peso TF-IDF a la de menor.
     */
    public static List<String> extraer(String texto, int cantidad) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        String[] oraciones = texto.split("(?<=[.!?])\\s+");
        Map<String, Integer> tf = new LinkedHashMap<>();
        Map<String, Integer> df = new HashMap<>();
        for (String oracion : oraciones) {
            List<String> palabras = palabrasDeContenido(oracion);
            palabras.forEach(palabra -> tf.merge(palabra, 1, Integer::sum));
            new HashSet<>(palabras).forEach(palabra -> df.merge(palabra, 1, Integer::sum));
        }
        double n = oraciones.length;
        Map<String, Double> peso = new HashMap<>();
        // Con una sola oración todo el IDF vale cero: se ordena por frecuencia
        tf.forEach((palabra, veces) -> peso.put(palabra, n > 1 ? veces * Math.log(n / df.get(palabra)) : veces));
        List<String> ordenadas = new ArrayList<>(tf.keySet());
        // De mayor a menor peso; si empatan, la que más se repite y, si sigue el empate, la que aparece primero (la ordenación es estable)
        ordenadas.sort((a, b) -> {
            int comparacion = Double.compare(peso.get(b), peso.get(a));
            return comparacion != 0 ? comparacion : Integer.compare(tf.get(b), tf.get(a));
        });
        return ordenadas.stream().limit(cantidad).toList();
    }
}
