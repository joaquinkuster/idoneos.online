package com.app.idoneos.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Utilidad que saca las palabras clave de un texto, como lo hace AUREA: cuenta cuántas veces aparece cada palabra de 4 letras o más,
 * sin las comunes, y se queda con las más repetidas. No usa TF-IDF ni reduce las palabras a su raíz.
 */
public final class PalabrasClaveUtil {

    /** Palabras comunes que no dicen de qué trata un texto, sin tildes y en minúscula. */
    private static final Set<String> COMUNES = new HashSet<>(Arrays.asList(
            "de", "la", "que", "el", "en", "y", "a", "los", "del", "se", "las", "por", "un", "para", "con", "no", "una", "su", "al", "lo",
            "como", "mas", "pero", "sus", "le", "ya", "o", "este", "si", "porque", "esta", "entre", "cuando", "muy", "sin", "sobre", "tambien",
            "me", "hasta", "hay", "donde", "quien", "desde", "todo", "nos", "durante", "todos", "uno", "les", "ni", "contra", "otros", "ese",
            "eso", "ante", "ellos", "e", "esto", "antes", "algunos", "unos", "otro", "otras", "otra", "tanto", "esa", "estos", "mucho",
            "quienes", "nada", "muchos", "cual", "poco", "ella", "estar", "estas", "algunas", "algo", "nosotros", "mi", "mis", "tu", "tus",
            "ellas", "nosotras", "vosotros", "vosotras", "os", "mio", "mia", "mios", "mias", "tuyo", "tuya", "tuyos", "tuyas", "suyo", "suya",
            "suyos", "suyas", "nuestro", "nuestra", "nuestros", "nuestras", "vuestro", "vuestra", "vuestros", "vuestras", "es", "son", "ser",
            "fue", "fueron", "ha", "han", "hace", "hacer", "cada", "material", "tema", "concepto", "aspecto", "sistema", "proceso", "procesos",
            "funcion", "funciones", "dato", "curioso", "curiosa", "informacion", "elemento", "rol", "descripcion", "nombre", "tabla", "campo"));

    private PalabrasClaveUtil() {
    }

    /** Indica si una palabra, ya sin tildes y en minúscula, es de las comunes que no dicen de qué trata un texto. */
    public static boolean esComun(String palabra) {
        return COMUNES.contains(palabra);
    }

    /**
     * Cuenta cuántas veces aparece cada palabra de 4 letras o más de un texto, sin las comunes.
     *
     * @param texto El texto.
     * @return Las palabras, sin tildes y en minúscula, con sus apariciones.
     */
    public static Map<String, Integer> frecuencias(String texto) {
        Map<String, Integer> frecuencia = new HashMap<>();
        if (texto == null || texto.isBlank()) {
            return frecuencia;
        }
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT);
        for (String palabra : normalizado.split("[^a-z0-9]+")) {
            if (palabra.length() >= 4 && !COMUNES.contains(palabra)) {
                frecuencia.merge(palabra, 1, Integer::sum);
            }
        }
        return frecuencia;
    }

    /**
     * Saca las palabras clave de un texto.
     *
     * @param texto El texto.
     * @param cantidad Cuántas palabras devolver como máximo.
     * @return Las palabras, sin tildes y en minúscula, de la más repetida a la menos repetida.
     */
    public static List<String> extraer(String texto, int cantidad) {
        List<Map.Entry<String, Integer>> ordenadas = new ArrayList<>(frecuencias(texto).entrySet());
        ordenadas.sort(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed());
        return ordenadas.stream().limit(cantidad).map(Map.Entry::getKey).toList();
    }
}
