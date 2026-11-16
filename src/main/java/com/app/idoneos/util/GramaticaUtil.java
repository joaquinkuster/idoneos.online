package com.app.idoneos.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

/**
 * Análisis gramatical del español con Apache OpenNLP (modelos entrenados con el corpus Universal Dependencies GSD): separa el texto en palabras, dice qué
 * categoría gramatical tiene cada una (verbo, sustantivo, pronombre, conjunción…) y la lleva a su forma base («cursos» → «curso»). Reemplaza a las listas
 * de palabras y a las raíces de cinco letras: lo que necesita conocimiento del idioma lo decide el etiquetador, no una lista nuestra.
 */
public final class GramaticaUtil {

    private GramaticaUtil() {
    }

    /** Nombres de los archivos de los modelos dentro de sus jars (cambian con la versión del modelo). */
    private static final String MODELO_DE_PALABRAS = "/opennlp-es-ud-gsd-tokens-1.3-2.5.4.bin";
    private static final String MODELO_DE_CATEGORIAS = "/opennlp-es-ud-gsd-pos-1.3-2.5.4.bin";
    private static final String MODELO_DE_LEMAS = "/opennlp-es-ud-gsd-lemmas-1.3-2.5.4.bin";

    /** Cuántos análisis se guardan para no repetir el trabajo con la misma oración. */
    private static final int ANALISIS_GUARDADOS = 4000;

    /**
     * El análisis de un texto.
     *
     * @param texto El texto analizado.
     * @param palabras Las palabras y los signos, tal como están en el texto.
     * @param inicios La posición de cada palabra en el texto.
     * @param categorias La categoría gramatical de cada una (VERB, AUX, NOUN, PROPN, ADJ, DET, ADP, PRON, ADV, CCONJ, SCONJ, PUNCT…).
     * @param lemas La forma base de cada una.
     */
    public record Analisis(String texto, String[] palabras, int[] inicios, String[] categorias, String[] lemas) {
    }

    private static TokenizerME tokenizador;
    private static POSTaggerME etiquetador;
    private static LemmatizerME lematizador;

    private static final Map<String, Analisis> GUARDADOS = new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Analisis> eldest) {
            return size() > ANALISIS_GUARDADOS;
        }
    };

    private static void cargarModelos() {
        if (tokenizador != null) {
            return;
        }
        try (InputStream palabras = GramaticaUtil.class.getResourceAsStream(MODELO_DE_PALABRAS);
                InputStream categorias = GramaticaUtil.class.getResourceAsStream(MODELO_DE_CATEGORIAS);
                InputStream lemas = GramaticaUtil.class.getResourceAsStream(MODELO_DE_LEMAS)) {
            if (palabras == null || categorias == null || lemas == null) {
                throw new IllegalStateException("Faltan los modelos de OpenNLP para español en el classpath.");
            }
            tokenizador = new TokenizerME(new TokenizerModel(palabras));
            etiquetador = new POSTaggerME(new POSModel(categorias));
            lematizador = new LemmatizerME(new LemmatizerModel(lemas));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron cargar los modelos de OpenNLP.", e);
        }
    }

    /**
     * Analiza un texto. Los modelos no admiten usarse desde dos hilos a la vez, así que el análisis es exclusivo; el resultado se guarda para la próxima.
     */
    public static synchronized Analisis analizar(String texto) {
        Analisis guardado = GUARDADOS.get(texto);
        if (guardado != null) {
            return guardado;
        }
        cargarModelos();
        Span[] posiciones = tokenizador.tokenizePos(texto);
        String[] palabras = new String[posiciones.length];
        int[] inicios = new int[posiciones.length];
        for (int i = 0; i < posiciones.length; i++) {
            palabras[i] = texto.substring(posiciones[i].getStart(), posiciones[i].getEnd());
            inicios[i] = posiciones[i].getStart();
        }
        String[] categorias = etiquetador.tag(palabras);
        String[] lemas = lematizador.lemmatize(palabras, categorias);
        for (int i = 0; i < lemas.length; i++) {
            // Si el lematizador no sabe la forma base, queda la palabra tal cual
            if (lemas[i] == null || lemas[i].isBlank() || lemas[i].equals("O") || lemas[i].equals("_")) {
                lemas[i] = palabras[i];
            }
        }
        Analisis analisis = new Analisis(texto, palabras, inicios, categorias, lemas);
        GUARDADOS.put(texto, analisis);
        return analisis;
    }

    /** Indica si el texto tiene un verbo o un auxiliar. Una oración sin ninguno es un título, una celda o un fragmento. */
    public static boolean tieneVerbo(String texto) {
        for (String categoria : analizar(texto).categorias()) {
            if (categoria.equals("VERB") || categoria.equals("AUX")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indica si un texto empieza con un verbo, o sea si es una pregunta de sí o no («¿Existe al menos un curso?», «¿Ha iniciado sesión el Docente?»). Una
     * pregunta así se responde con «sí» o «no», no con la oración del material.
     */
    public static boolean empiezaConVerbo(String texto) {
        Analisis a = analizar(texto);
        for (int i = 0; i < a.palabras().length; i++) {
            if (!a.categorias()[i].equals("PUNCT")) {
                return a.categorias()[i].equals("VERB") || a.categorias()[i].equals("AUX");
            }
        }
        return false;
    }

    /**
     * Los lemas de las palabras que dicen de qué trata un texto (sustantivos, nombres propios, verbos y adjetivos), sin tildes y en minúscula y con
     * repeticiones. Los artículos, las preposiciones y las conjunciones no entran, porque no son de ninguna de esas categorías.
     */
    public static List<String> lemasDeContenido(String texto) {
        Analisis analisis = analizar(texto);
        List<String> lemas = new ArrayList<>();
        for (int i = 0; i < analisis.palabras().length; i++) {
            String categoria = analisis.categorias()[i];
            if (categoria.equals("NOUN") || categoria.equals("PROPN") || categoria.equals("VERB") || categoria.equals("ADJ")) {
                String lema = LogicaPreguntasAurea.normalizar(analisis.lemas()[i]);
                if (lema.length() >= 2) {
                    lemas.add(lema);
                }
            }
        }
        return lemas;
    }

    /** Igual que {@link #lemasDeContenido(String)}, pero sin repeticiones. */
    public static Set<String> conjuntoDeLemas(String texto) {
        return new HashSet<>(lemasDeContenido(texto));
    }

    /**
     * El IDF de cada lema: ln(N / df), con N oraciones y df las que lo contienen. Un lema que está en todas las oraciones pesa cero. Sirve para medir
     * cuánto se parecen dos oraciones.
     */
    public static Map<String, Double> idfDeLemas(Collection<String> oraciones) {
        Map<String, Integer> oracionesConElLema = new HashMap<>();
        for (String oracion : oraciones) {
            conjuntoDeLemas(oracion).forEach(lema -> oracionesConElLema.merge(lema, 1, Integer::sum));
        }
        Map<String, Double> idf = new HashMap<>();
        double n = Math.max(1, oraciones.size());
        oracionesConElLema.forEach((lema, cantidad) -> idf.put(lema, Math.log(n / cantidad)));
        return idf;
    }

    /**
     * La similitud coseno entre dos textos: cada uno es un vector con el peso (frecuencia por IDF) de los lemas de sus palabras de contenido. Vale 0 si no
     * comparten nada y 1 si son iguales en lo que dicen.
     *
     * @param idf El peso de cada lema en el documento. Un lema que no está se toma como el más raro.
     */
    public static double coseno(String a, String b, Map<String, Double> idf) {
        Map<String, Double> va = vector(a, idf);
        Map<String, Double> vb = vector(b, idf);
        double producto = 0;
        for (Map.Entry<String, Double> e : va.entrySet()) {
            producto += e.getValue() * vb.getOrDefault(e.getKey(), 0.0);
        }
        double normas = norma(va) * norma(vb);
        return normas == 0 ? 0 : producto / normas;
    }

    private static Map<String, Double> vector(String texto, Map<String, Double> idf) {
        double masRaro = idf.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        Map<String, Double> vector = new HashMap<>();
        for (String lema : lemasDeContenido(texto)) {
            vector.merge(lema, idf.getOrDefault(lema, masRaro), Double::sum);
        }
        return vector;
    }

    private static double norma(Map<String, Double> vector) {
        return Math.sqrt(vector.values().stream().mapToDouble(v -> v * v).sum());
    }

    /**
     * Quita el conector con el que empieza una oración que sigue a otra, o descarta la oración si sin la anterior no se entiende:
     * <ul>
     * <li>Empieza con un adverbio o una conjunción («Además, el inversor puede…», «Y la Administración…»): si lo que sigue arranca con un sujeto, se queda
     * con eso («El inversor puede…»); si no («Asimismo, debe sincronizar…», «Tampoco están integradas…»), se descarta.</li>
     * <li>Empieza con un verbo auxiliar sin sujeto («Es una relación de…»): se descarta.</li>
     * <li>Empieza con un pronombre de más de dos letras («Esto provoca…»), salvo que sea parte de un grupo con una preposición («Los de renta fija…»):
     * se descarta. Los pronombres de dos letras («Se muestra…») son reflexivos y la oración se entiende sola.</li>
     * </ul>
     *
     * @return La oración, con o sin el conector, o nulo si hay que descartarla.
     */
    public static String sinConectorInicial(String oracion) {
        Analisis a = analizar(oracion);
        int primera = 0;
        while (primera < a.palabras().length && a.categorias()[primera].equals("PUNCT")) {
            primera++;
        }
        if (primera >= a.palabras().length) {
            return oracion;
        }
        String categoria = a.categorias()[primera];
        if (categoria.equals("ADV") || categoria.equals("CCONJ") || categoria.equals("SCONJ")) {
            int sujeto = primera + 1;
            while (sujeto < a.palabras().length && a.categorias()[sujeto].equals("PUNCT")) {
                sujeto++;
            }
            if (sujeto < a.palabras().length && sujeto - primera <= 3 && (a.categorias()[sujeto].equals("DET") || a.categorias()[sujeto].equals("NOUN")
                    || a.categorias()[sujeto].equals("PROPN"))) {
                String resto = oracion.substring(a.inicios()[sujeto]);
                return Character.toUpperCase(resto.charAt(0)) + resto.substring(1);
            }
            return null;
        }
        if (categoria.equals("AUX")) {
            return null;
        }
        if (categoria.equals("PRON") && a.palabras()[primera].length() > 2
                && !(primera + 1 < a.palabras().length && a.categorias()[primera + 1].equals("ADP"))) {
            return null;
        }
        return oracion;
    }
}
