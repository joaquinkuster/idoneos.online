package com.app.idoneos.util;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidad que comprueba si una frase se apoya en un texto fuente.
 * Es una verificación léxica barata: no entiende significado, pero detecta las frases que traen palabras que la fuente no tiene.
 */
public final class FidelidadTextoUtil {

    private static final Pattern PALABRA = Pattern.compile("\\p{L}{5,}");

    /** Letras que se comparan de cada palabra, para tolerar plurales y conjugaciones. */
    private static final int RAIZ = 6;

    private FidelidadTextoUtil() {
    }

    /**
     * Calcula qué parte de las palabras de una frase aparecen en el texto fuente.
     *
     * @param frase La frase a comprobar.
     * @param fuente El texto del que debería salir.
     * @return Un valor entre 0 y 1. Una frase sin palabras de 5 letras o más devuelve 1, porque no hay nada que comprobar.
     */
    public static double cobertura(String frase, String fuente) {
        Set<String> enLaFuente = raices(fuente);
        Set<String> enLaFrase = raices(frase);
        if (enLaFrase.isEmpty()) {
            return 1;
        }
        long presentes = enLaFrase.stream().filter(enLaFuente::contains).count();
        return (double) presentes / enLaFrase.size();
    }

    /**
     * Indica si una respuesta breve aparece en un texto, aunque sea una sigla o un nombre corto.
     * Si la respuesta tiene palabras de 5 letras o más, alcanza con que la mayoría esté en el texto. Si no, tiene que aparecer tal cual.
     *
     * @param respuesta La respuesta a buscar.
     * @param texto El texto en el que debería estar.
     * @return Verdadero si la respuesta aparece.
     */
    public static boolean aparece(String respuesta, String texto) {
        String buscada = respuesta == null ? "" : respuesta.toLowerCase(Locale.ROOT).trim();
        String donde = texto == null ? "" : texto.toLowerCase(Locale.ROOT);
        if (buscada.isEmpty()) {
            return false;
        }
        if (donde.contains(buscada)) {
            return true;
        }
        return !raices(buscada).isEmpty() && cobertura(buscada, donde) >= 0.8;
    }

    /**
     * Calcula qué tanto se parecen dos frases por las palabras que comparten.
     *
     * @param a La primera frase.
     * @param b La segunda frase.
     * @return Un valor entre 0 y 1: las palabras que comparten sobre las que suman entre las dos.
     */
    public static double similitud(String a, String b) {
        Set<String> primera = raices(a);
        Set<String> segunda = raices(b);
        Set<String> union = new HashSet<>(primera);
        union.addAll(segunda);
        if (union.isEmpty()) {
            return 0;
        }
        long comunes = primera.stream().filter(segunda::contains).count();
        return (double) comunes / union.size();
    }

    private static Set<String> raices(String texto) {
        Set<String> raices = new HashSet<>();
        Matcher buscador = PALABRA.matcher(texto == null ? "" : texto.toLowerCase(Locale.ROOT));
        while (buscador.find()) {
            String palabra = buscador.group();
            raices.add(palabra.length() > RAIZ ? palabra.substring(0, RAIZ) : palabra);
        }
        return raices;
    }
}
