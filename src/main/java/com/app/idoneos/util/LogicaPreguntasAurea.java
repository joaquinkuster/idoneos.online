package com.app.idoneos.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lógica para armar preguntas de evaluación a partir de oraciones del material, de la que usa el generador de pools.
 * Tiene tres partes: la selección de las oraciones que sirven y su prioridad por frecuencia, la comprobación de lo que escribe el modelo (la pregunta,
 * las variantes falsas y las paráfrasis) contra la oración de la que sale, y tres formatos que se arman sin el modelo: la definición de un término del
 * glosario, completar una oración con una palabra clave y elegir la oración que trata sobre una palabra clave.
 */
public final class LogicaPreguntasAurea {

    /** Tipos de pregunta, los mismos de AUREA. Una múltiple tiene exactamente dos opciones correctas, y una única, una sola. */
    public enum Tipo { MULTIPLE_CHOICE, UNICA_RESPUESTA, VERDADERO_FALSO }

    /** Opción de una pregunta, con su texto y si es correcta. */
    public record Opcion(String texto, boolean correcta) {
    }

    /** Pregunta armada o validada. */
    public record Pregunta(String enunciado, List<Opcion> opciones, Tipo tipo) {
    }

    /** Lo que se sabe del material para armar preguntas: sus oraciones, sus palabras clave y el glosario. */
    public static final class Contexto {
        private final List<String> sentencias;
        private final List<String> palabrasClave;
        /** Las 40 palabras más repetidas, para reconocer oraciones que tratan de lo mismo. */
        private final List<String> palabrasParaEmparejar;
        private final Map<String, String> formaOriginal;
        private final List<String[]> glosario;
        private final String materialNormalizado;
        /** Las oraciones que eligió el modelo como importantes: las preguntas se arman con ellas primero. */
        private final List<String> preferidas = new ArrayList<>();

        private Contexto(List<String> sentencias, List<String> palabrasClave, List<String> palabrasParaEmparejar, Map<String, String> formaOriginal,
                List<String[]> glosario, String materialNormalizado) {
            this.sentencias = sentencias;
            this.palabrasClave = palabrasClave;
            this.palabrasParaEmparejar = palabrasParaEmparejar;
            this.formaOriginal = formaOriginal;
            this.glosario = glosario;
            this.materialNormalizado = materialNormalizado;
        }

        /**
         * Arma el contexto de un material.
         * Las oraciones son las primeras 30, sin repetir, de entre 6 y 28 palabras, y las palabras clave las 12 más repetidas, como en AUREA.
         *
         * @param material El texto del material.
         * @param glosario Los términos del glosario, cada uno como «término: definición». Puede estar vacío.
         * @return El contexto.
         */
        public static Contexto desde(String material, List<String> glosario) {
            String limpio = limpiar(material);
            List<String> sentencias = new ArrayList<>(masImportantes(limpio, 60));
            Map<String, String> formas = new HashMap<>();
            Matcher buscador = Pattern.compile("\\p{L}{4,}").matcher(limpio);
            while (buscador.find()) {
                formas.putIfAbsent(normalizar(buscador.group()), buscador.group());
            }
            List<String[]> definiciones = new ArrayList<>();
            for (String linea : glosario == null ? List.<String>of() : glosario) {
                int corte = linea.indexOf(':');
                if (corte > 0 && corte < linea.length() - 1) {
                    definiciones.add(new String[] { linea.substring(0, corte).strip(), limpiar(linea.substring(corte + 1)) });
                }
            }
            return new Contexto(sentencias, PalabrasClaveUtil.extraer(limpio, 12), PalabrasClaveUtil.extraer(limpio, 40), formas, definiciones, normalizar(limpio));
        }

        /** El material sin tildes, en minúscula y con un solo espacio entre palabras. */
        public String materialNormalizado() {
            return materialNormalizado;
        }

        /** Las palabras clave, sin tildes y de la más repetida a la menos repetida. */
        public List<String> palabrasClave() {
            return palabrasClave;
        }

        /** Las oraciones del material que sirven para armar preguntas. */
        public List<String> oraciones() {
            return sentencias;
        }

        /**
         * Marca las oraciones que eligió el modelo como importantes. Pasan al principio de la lista de oraciones del contexto, y si son
         * suficientes, las preguntas se arman solo con ellas. Las demás quedan de respaldo.
         *
         * @param oraciones Las oraciones elegidas, que tienen que venir del material.
         */
        public void preferir(List<String> oraciones) {
            for (String oracion : oraciones) {
                String limpia = limpiar(oracion);
                if (!limpia.isEmpty() && !preferidas.contains(limpia)) {
                    preferidas.add(limpia);
                }
            }
            sentencias.removeAll(preferidas);
            sentencias.addAll(0, preferidas);
        }

            /** Las oraciones que eligió el modelo, en el orden en que las eligió. */
        public List<String> preferidas() {
            return new ArrayList<>(preferidas);
        }

        /** Las oraciones con las que se arman las preguntas: las del modelo si hay suficientes, o todas. */
        private List<String> base() {
            return preferidas.size() >= 4 ? preferidas : sentencias;
        }

        private String oracion(int indice) {
            List<String> base = base();
            return base.isEmpty() ? "" : base.get(Math.abs(indice) % base.size());
        }

        private String palabraClave(int indice) {
            return palabrasClave.isEmpty() ? "" : palabrasClave.get(Math.abs(indice) % palabrasClave.size());
        }
    }

    /** Largo máximo, en caracteres, de un enunciado o de una opción. */
    private static final int LARGO_MAXIMO_OPCION = 255;

    private static final Pattern NUMERO = Pattern.compile("\\b\\d+\\b");

    /** Las oraciones de un texto que sirven para armar una pregunta, en el orden del texto y sin repetir. Las elige {@link OracionesUtil}, sin listas de palabras. */
    private static List<String> oracionesCandidatas(String limpio, int maximo) {
        return OracionesUtil.candidatas(limpio, maximo);
    }

    /**
     * Las oraciones más importantes de un texto, según el código. La importancia de cada oración es su relevancia (BM25) respecto de las palabras clave del
     * documento, y se eligen de a una con MMR: cada elegida le resta puntaje a las que se le parecen, así que no entran diez versiones de la misma ficha
     * ni de la misma idea. Las oraciones sin un verbo conjugado (objetivos en infinitivo, fragmentos) van después de las que sí lo tienen.
     *
     * @param texto El texto completo.
     * @param cantidad Cuántas oraciones devolver como máximo.
     * @return Las oraciones, de la más importante a la menos importante. Solo entran las que sirven para armar una pregunta.
     */
    public static List<String> masImportantes(String texto, int cantidad) {
        return masImportantes(oracionesCandidatas(limpiar(texto), Integer.MAX_VALUE), cantidad);
    }

    /** Cuántas palabras clave del documento forman la consulta con la que se mide la relevancia de cada oración. */
    private static final int PALABRAS_CLAVE_DE_LA_CONSULTA = 8;

    /** MMR: 1 elige solo por relevancia y 0 solo por diversidad. */
    private static final double LAMBDA_DE_MMR = 0.5;

    /**
     * Igual que {@link #masImportantes(String, int)}, pero con las oraciones ya sacadas del documento (por ejemplo, con la estructura del PDF).
     *
     * @param candidatas Las oraciones del documento que sirven para armar una pregunta, en el orden del texto.
     * @param cantidad Cuántas devolver como máximo.
     */
    public static List<String> masImportantes(List<String> candidatas, int cantidad) {
        int n = candidatas.size();
        if (n == 0 || cantidad <= 0) {
            return List.of();
        }
        // La consulta: los lemas de las palabras clave del documento
        Set<String> consulta = new HashSet<>();
        PalabrasClaveUtil.extraer(String.join(" ", candidatas), PALABRAS_CLAVE_DE_LA_CONSULTA).forEach(clave -> consulta.addAll(GramaticaUtil.conjuntoDeLemas(clave)));
        // Los lemas de cada oración y su relevancia (BM25) respecto de la consulta
        List<List<String>> lemasPorOracion = new ArrayList<>();
        for (String candidata : candidatas) {
            lemasPorOracion.add(GramaticaUtil.lemasDeContenido(candidata));
        }
        double[] relevancia = Bm25Util.puntajes(lemasPorOracion, consulta);
        double maxima = Arrays.stream(relevancia).max().orElse(0);
        // Sin ninguna coincidencia con la consulta, todas valen lo mismo y decide la diversidad
        for (int i = 0; i < n; i++) {
            relevancia[i] = maxima == 0 ? 1 : relevancia[i] / maxima;
        }
        // Los vectores de lemas, armados una vez, para medir qué tanto se parecen dos oraciones
        Map<String, Double> idfDelDocumento = GramaticaUtil.idfDeLemas(candidatas);
        List<Map<String, Double>> vectores = new ArrayList<>();
        for (String candidata : candidatas) {
            vectores.add(GramaticaUtil.vectorDe(candidata, idfDelDocumento));
        }
        // Las que tienen un verbo conjugado se eligen primero; las demás, solo si hacen falta
        List<Integer> conVerbo = new ArrayList<>();
        List<Integer> sinVerbo = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            (GramaticaUtil.tieneVerboConjugado(candidatas.get(i)) ? conVerbo : sinVerbo).add(i);
        }
        double[] parecidaALaElegida = new double[n];
        List<String> elegidas = new ArrayList<>();
        for (List<Integer> grupo : List.of(conVerbo, sinVerbo)) {
            Set<Integer> pendientes = new HashSet<>(grupo);
            while (elegidas.size() < cantidad && !pendientes.isEmpty()) {
                int mejor = -1;
                double mejorPuntaje = Double.NEGATIVE_INFINITY;
                for (int i : grupo) {
                    if (!pendientes.contains(i)) {
                        continue;
                    }
                    // MMR: la relevancia menos lo que se parece a lo ya elegido. Si empatan, la que viene primero en el texto
                    double puntaje = LAMBDA_DE_MMR * relevancia[i] - (1 - LAMBDA_DE_MMR) * parecidaALaElegida[i];
                    if (puntaje > mejorPuntaje) {
                        mejorPuntaje = puntaje;
                        mejor = i;
                    }
                }
                pendientes.remove(mejor);
                elegidas.add(candidatas.get(mejor));
                for (int i = 0; i < n; i++) {
                    parecidaALaElegida[i] = Math.max(parecidaALaElegida[i], GramaticaUtil.coseno(vectores.get(i), vectores.get(mejor)));
                }
            }
        }
        return elegidas;
    }

    /**
     * Las oraciones de un texto entre las que puede elegir el modelo para armar preguntas.
     *
     * @param texto El texto.
     * @param maximo Cuántas como máximo.
     * @return Las oraciones, en el orden del texto.
     */
    public static List<String> candidatas(String texto, int maximo) {
        return oracionesCandidatas(limpiar(texto), maximo);
    }

    /** Un verbo en infinitivo al principio, que es el arranque de un ítem de una lista («Gestionar cursos (alta, baja…)») y no una oración. */
    private static final Pattern ARRANQUE_DE_LISTA = Pattern.compile("^[A-ZÁÉÍÓÚ][a-záéíóúñ]{3,}(?:ar|er|ir)\\b");

    /** Indica si una oración arrastra restos de una tabla: códigos, guiones sueltos, encabezados con número o columnas con mayúscula. */
    public static boolean tieneRestosDeTabla(String oracion) {
        return OracionesUtil.tieneRestosDeTabla(oracion);
    }

    private LogicaPreguntasAurea() {
    }

    // ----------------------------------------------------------------------
    // Texto
    // ----------------------------------------------------------------------

    /** Quita tildes y símbolos, pasa a minúscula y deja un solo espacio entre palabras. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replaceAll("\\s+", " ").trim();
    }

    /**
     * Cierra los signos de una pregunta: «¿…?». Si el modelo escribió más de una pregunta, se queda con la primera. Devuelve vacío si lo que escribió no
     * es una pregunta, o sea, si no tiene ningún signo de interrogación.
     */
    private static String cerrarPregunta(String texto) {
        String limpio = limpiar(texto).replaceAll("^[¿?\\s]+", "");
        if (!texto.contains("?") && !texto.contains("¿")) {
            return "";
        }
        int fin = limpio.indexOf('?');
        String cuerpo = (fin >= 0 ? limpio.substring(0, fin) : limpio).replace("¿", "").replaceAll("[?\\s.;:,]+$", "");
        return cuerpo.isEmpty() ? "" : "¿" + cuerpo + "?";
    }

    /** Etiqueta con la que el pedido al modelo marca la oración: una pregunta que la nombra está copiando el pedido. */
    public static final String ETIQUETA_DE_ORACION = "Oración";

    /** Etiqueta con la que el pedido al modelo marca la sección del documento en la que está la oración. */
    public static final String ETIQUETA_DE_SECCION = "Sección";

    /**
     * Indica si un texto repite las etiquetas del pedido que se le hizo al modelo: la de la oración y las de la línea de contexto («Documento: ….
     * Palabras clave: …»), que son lo que va antes de cada dos puntos. Una pregunta que dice «las palabras clave» o «la oración» no habla del contenido.
     */
    private static boolean repiteEtiquetas(String texto, String contexto) {
        List<String> etiquetas = new ArrayList<>(List.of(ETIQUETA_DE_ORACION));
        Matcher encontradas = Pattern.compile("(?:^|\\.\\s)([\\p{L} ]{3,30}):").matcher(contexto == null ? "" : contexto);
        while (encontradas.find()) {
            etiquetas.add(encontradas.group(1));
        }
        String normal = " " + normalizar(texto) + " ";
        return etiquetas.stream().map(LogicaPreguntasAurea::normalizar).anyMatch(etiqueta -> !etiqueta.isEmpty() && normal.contains(" " + etiqueta + " "));
    }

    /**
     * Los lemas de las palabras que dicen de qué trata un texto (sustantivos, nombres propios, verbos y adjetivos), sin tildes y en minúscula: «curso» y
     * «cursos» cuentan como uno. Los reconoce el etiquetador de OpenNLP, sin listas de palabras.
     */
    private static Set<String> palabrasDeContenido(String texto) {
        return GramaticaUtil.conjuntoDeLemas(texto);
    }

    /** El lema de una palabra suelta, sin tildes y en minúscula. */
    private static String lemaDe(String palabra) {
        GramaticaUtil.Analisis analisis = GramaticaUtil.analizar(palabra);
        return analisis.lemas().length == 0 ? normalizar(palabra) : normalizar(analisis.lemas()[0]);
    }

    /** Indica si una oración es un ítem de lista en infinitivo («Gestionar pagos mediante la API de MODO»): un objetivo, no una afirmación verdadera o falsa. */
    public static boolean esItemDeLista(String oracion) {
        return ARRANQUE_DE_LISTA.matcher(oracion.strip()).find();
    }

    /** Pasa una oración a su forma de opción: con la primera letra en mayúscula y un punto final. */
    private static String comoOracion(String texto) {
        String limpio = limpiar(texto == null ? "" : texto).replaceAll("^[¿\\s]+", "").replaceAll("[?\\s.;:,]+$", "");
        return limpio.isEmpty() ? "" : capitalizar(limpio) + ".";
    }

    /**
     * Comprueba la pregunta que escribió el modelo para una oración (o dos): tiene que ser una pregunta, entenderse sola, apoyarse en lo que dicen las
     * oraciones y no traer datos que no están en ellas, como una sigla inventada.
     *
     * @param fuentes La oración, o las dos oraciones juntas, de las que sale la pregunta.
     * @param pregunta La pregunta que escribió el modelo.
     * @return La pregunta con sus signos cerrados, o nulo si no sirve.
     */
    public static String enunciadoValido(String fuentes, String pregunta) {
        return enunciadoValido(fuentes, "", pregunta);
    }

    /**
     * Igual que {@link #enunciadoValido(String, String)}, pero los nombres propios y siglas de la pregunta también pueden salir del contexto breve del
     * material («Documento: Requisitos del Sistema - Idóneos Online…») que se le da al modelo con cada oración.
     *
     * @param fuentes La oración, o las dos oraciones juntas, de las que sale la pregunta.
     * @param contexto El contexto breve del material que recibió el modelo.
     * @param pregunta La pregunta que escribió el modelo.
     * @return La pregunta con sus signos cerrados, o nulo si no sirve.
     */
    public static String enunciadoValido(String fuentes, String contexto, String pregunta) {
        String enunciado = cerrarPregunta(pregunta == null ? "" : pregunta);
        if (enunciado.isEmpty() || enunciado.length() > LARGO_MAXIMO_OPCION) {
            return null;
        }
        String cuerpo = enunciado.replaceAll("[¿?]", "");
        // Una pregunta que empieza con un verbo es de sí o no: su respuesta no es la oración del material
        if (cuerpo.split("\\s+").length < 3 || repiteEtiquetas(cuerpo, contexto) || GramaticaUtil.empiezaConVerbo(cuerpo)) {
            return null;
        }
        // Una pregunta que repite casi toda la oración («¿Qué significa que exista al menos una inscripción registrada?») ya dice la respuesta
        Set<String> deLaPregunta = palabrasDeContenido(cuerpo);
        Set<String> deLaOracion = palabrasDeContenido(fuentes);
        if (deLaPregunta.size() >= 4 && deLaPregunta.stream().filter(deLaOracion::contains).count() * 10 >= deLaPregunta.size() * 8
                && deLaPregunta.size() * 100 >= deLaOracion.size() * 45) {
            return null;
        }
        if (FidelidadTextoUtil.cobertura(cuerpo, fuentes) < 0.4 || !numerosYNombresEstanEn(cuerpo, fuentes + " " + contexto)) {
            return null;
        }
        return enunciado;
    }

    /**
     * Cuántas palabras de contenido (lemas de sustantivos, nombres propios, verbos y adjetivos) tiene una pregunta que no están en la oración de la que
     * sale ni en su contexto (el título del documento, las palabras clave y la sección). Es lo que el modelo agregó por su cuenta.
     */
    public static int palabrasAjenas(String fuentes, String contexto, String enunciado) {
        Set<String> propias = palabrasDeContenido(fuentes + " " + (contexto == null ? "" : contexto));
        return (int) palabrasDeContenido(enunciado).stream().filter(lema -> !propias.contains(lema)).count();
    }

    /** La similitud coseno mínima entre una pregunta y su oración: con menos, la pregunta no nació de esa oración. */
    private static final double COSENO_MINIMO_DE_LA_PREGUNTA = 0.30;

    /**
     * De las versiones de la pregunta que escribió el modelo, elige la mejor: la que pasa las comprobaciones de {@link #enunciadoValido}, tiene una
     * similitud coseno con la oración de al menos 0,30 y es la más parecida a ella. Con un modelo chico, la misma oración da preguntas muy distintas, y
     * algunas inventan o hablan de otra cosa.
     *
     * @param fuentes La oración de la que sale la pregunta.
     * @param contexto El contexto breve del material y la sección.
     * @param preguntas Las versiones que escribió el modelo.
     * @param idf El peso de cada lema en el documento, para medir la similitud.
     * @return La mejor pregunta, con sus signos cerrados, o nulo si ninguna sirve.
     */
    public static String mejorEnunciado(String fuentes, String contexto, List<String> preguntas, Map<String, Double> idf) {
        String mejor = null;
        double mejorPuntaje = Double.NEGATIVE_INFINITY;
        for (String pregunta : preguntas) {
            String valida = enunciadoValido(fuentes, contexto, pregunta);
            if (valida == null) {
                continue;
            }
            double coseno = GramaticaUtil.coseno(valida, fuentes, idf);
            if (coseno < COSENO_MINIMO_DE_LA_PREGUNTA) {
                continue;
            }
            // Entre las que pasan, la más parecida a la oración, con un descuento por cada palabra de contenido que agrega por su cuenta
            double puntaje = coseno - 0.05 * palabrasAjenas(fuentes, contexto, valida);
            if (puntaje > mejorPuntaje) {
                mejor = valida;
                mejorPuntaje = puntaje;
            }
        }
        return mejor;
    }

    /**
     * Arma preguntas con los dos formatos que hace el código, sin el modelo: la definición de un término del glosario y completar una oración
     * tapando una palabra clave.
     *
     * @param cantidad Cuántas armar como máximo.
     * @param usados Los enunciados ya usados, normalizados. Se completa con los de las preguntas que arma.
     * @return Las preguntas armadas, que pueden ser menos que las pedidas.
     */
    public static List<Pregunta> armarFormatosDeReglas(Contexto contexto, int cantidad, Set<String> usados) {
        Random azar = new Random();
        List<Pregunta> armadas = new ArrayList<>();
        for (int intento = 0; armadas.size() < cantidad && intento < Math.max(cantidad * 10, 20); intento++) {
            int sorteado = azar.nextInt(1_000_000);
            Pregunta pregunta = azar.nextBoolean() ? deGlosario(contexto, sorteado, usados, azar) : completarConPalabraClave(contexto, sorteado, usados, azar);
            if (pregunta != null) {
                armadas.add(pregunta);
            }
        }
        return armadas;
    }

    /**
     * Comprueba que todos los números y los nombres propios (palabras con mayúscula que no abren la frase) de un texto estén en la oración de la que
     * sale: lo que el modelo no puede cambiar al parafrasear.
     */
    private static boolean numerosYNombresEstanEn(String texto, String oracion) {
        String fuente = " " + normalizar(oracion) + " ";
        Matcher numeros = Pattern.compile("\\d+").matcher(texto);
        while (numeros.find()) {
            if (!fuente.contains(" " + numeros.group() + " ")) {
                return false;
            }
        }
        String[] palabras = texto.strip().split("\\s+");
        for (int i = 1; i < palabras.length; i++) {
            String palabra = palabras[i].replaceAll("[^\\p{L}]", "");
            // Nombres propios y siglas (palabras todas en mayúscula, como «POO»): el modelo no puede traer los que no están en la oración
            boolean sigla = palabra.length() >= 2 && palabra.equals(palabra.toUpperCase(Locale.ROOT));
            if (((palabra.length() >= 4 && Character.isUpperCase(palabra.charAt(0))) || sigla) && !fuente.contains(" " + normalizar(palabra) + " ")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Corta un texto al largo indicado, sin puntos suspensivos y sin dejar la idea a medias: si se pasa, corta en la última coma, punto y coma
     * o dos puntos que haya en su segunda mitad, y si no hay, en una palabra entera quitando lo que quedaría colgando al final,
     * como una palabra corta (artículo, preposición o conjunción) o un gerundio («contemplando»).
     */
    public static String limitar(String texto, int maximo) {
        String limpio = limpiar(texto);
        if (limpio.length() <= maximo) {
            return limpio;
        }
        String corte = TextoPdfUtil.cortar(limpio, maximo);
        int borde = Math.max(corte.lastIndexOf(','), Math.max(corte.lastIndexOf(';'), corte.lastIndexOf(':')));
        if (borde > corte.length() / 2) {
            corte = corte.substring(0, borde);
        }
        // Lo que quedaría colgando al final: palabras de hasta tres letras (artículos, preposiciones, conjunciones) y gerundios
        return corte.replaceAll("(?iu)(\\s+(\\p{L}{1,3}|\\p{L}+(?:ando|iendo|yendo)))+$", "").replaceAll("[,;:\\s]+$", "");
    }

    private static String capitalizar(String texto) {
        return texto == null || texto.isBlank() ? "" : texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }

    private static Set<String> palabrasRelevantes(String texto) {
        return palabrasDeContenido(texto);
    }

    private static int contarPalabras(String texto) {
        String limpio = limpiar(texto);
        return limpio.isEmpty() ? 0 : limpio.split("\\s+").length;
    }

    private static boolean esDemasiadoParecida(String base, String candidata) {
        if (base == null || base.isBlank() || candidata == null || candidata.isBlank()) {
            return false;
        }
        Set<String> deLaBase = palabrasRelevantes(base);
        Set<String> deLaCandidata = palabrasRelevantes(candidata);
        if (deLaBase.isEmpty() || deLaCandidata.isEmpty()) {
            return false;
        }
        Set<String> comunes = new HashSet<>(deLaBase);
        comunes.retainAll(deLaCandidata);
        return comunes.size() / (double) deLaBase.size() >= 0.85 && comunes.size() / (double) deLaCandidata.size() >= 0.85
                && Math.abs(contarPalabras(base) - contarPalabras(candidata)) <= 2;
    }

    private static boolean tieneSolapamiento(String a, String b) {
        if (a == null || a.isBlank() || b == null || b.isBlank()) {
            return false;
        }
        Set<String> comunes = palabrasRelevantes(a);
        comunes.retainAll(palabrasRelevantes(b));
        return comunes.size() >= 2;
    }

    // ----------------------------------------------------------------------
    // Preguntas armadas por el código: el modelo solo redacta
    // ----------------------------------------------------------------------

    /** Una oración con una similitud coseno igual o mayor que esta con la correcta casi dice lo mismo: no sirve como incorrecta. */
    private static final double SIMILITUD_MAXIMA_DE_UNA_INCORRECTA = 0.9;

    /**
     * Elige, entre las otras oraciones del material, las que sirven como respuestas incorrectas de una pregunta: son las más parecidas a la correcta por
     * similitud coseno, tienen un largo parecido, no se parecen entre sí y no responden también a la pregunta. Son frases reales del material.
     *
     * @param correcta La respuesta correcta, ya como oración.
     * @param original La oración del material de la que sale la correcta, que no puede elegirse.
     * @param deDefinicion Si la correcta es una definición («Término: definición»): entonces las incorrectas son las definiciones de otros términos.
     *        Nulo es cualquier oración.
     * @param palabraProhibida Palabra normalizada que no puede tener ninguna incorrecta, o vacío.
     * @return Las incorrectas pedidas, o nulo si no hay suficientes.
     */
    private static List<String> elegirDistractores(String correcta, String original, Boolean deDefinicion, String enunciado, List<String> otras,
            int cantidad, String palabraProhibida) {
        // Dos oraciones tratan de lo mismo según el peso (IDF) de los lemas que comparten: las que están en todas las oraciones del documento pesan cero
        Map<String, Double> idf = GramaticaUtil.idfDeLemas(otras == null ? List.<String>of() : otras);
        Set<String> deLaPregunta = palabrasDeContenido(enunciado);
        // Las palabras de la pregunta que salen de la oración correcta: lo que una opción tendría que tener para responder igual de bien
        Set<String> deLaPropiaPregunta = new HashSet<>(deLaPregunta);
        deLaPropiaPregunta.retainAll(palabrasDeContenido(correcta));
        record Puntuada(String texto, double similitud) {
        }
        List<Puntuada> candidatas = new ArrayList<>();
        Set<String> vistos = new HashSet<>(List.of(normalizar(correcta), normalizar(original)));
        for (String otra : otras == null ? List.<String>of() : otras) {
            Definicion definicion = comoDefinicion(otra);
            if ((deDefinicion != null && deDefinicion != (definicion != null)) || esItemDeLista(otra)) {
                continue;
            }
            String texto = comoOracion(deDefinicion != null && deDefinicion && definicion != null ? definicion.texto() : otra);
            double razon = correcta.isEmpty() ? 0 : (double) texto.length() / correcta.length();
            // Como en Aurea: las opciones tienen al menos 6 palabras y un largo parecido, para que la correcta no destaque por larga o por corta
            if (texto.isEmpty() || texto.length() > LARGO_MAXIMO_OPCION || razon < 0.6 || razon > 1.5 || texto.split("\\s+").length < 6 || !vistos.add(normalizar(texto))
                    || esDemasiadoParecida(correcta, texto) || (!palabraProhibida.isEmpty() && (" " + normalizar(texto) + " ").contains(" " + palabraProhibida + " "))) {
                continue;
            }
            Set<String> palabras = palabrasDeContenido(texto);
            // Una oración que también responde a la pregunta no puede ser una opción incorrecta: si tiene todas las palabras de la pregunta que salen de la
            // oración correcta, no se distingue de ella («¿Qué se hace al iniciar sesión?» y «El Docente ha iniciado sesión…» y «El Administrador ha iniciado
            // sesión…»)
            long enLaPregunta = palabras.stream().filter(deLaPregunta::contains).count();
            if (!deLaPropiaPregunta.isEmpty() && palabras.containsAll(deLaPropiaPregunta)) {
                continue;
            }
            // La similitud coseno con la correcta (vectores de TF-IDF de lemas): sin nada en común con ella ni con la pregunta, la opción no tiene relación y
            // no sirve; casi igual a ella, confunde
            double similitud = GramaticaUtil.coseno(correcta, texto, idf);
            if ((similitud <= 0 && enLaPregunta == 0) || similitud >= SIMILITUD_MAXIMA_DE_UNA_INCORRECTA) {
                continue;
            }
            candidatas.add(new Puntuada(texto, similitud));
        }
        // Las más parecidas a la correcta primero, así tiene que leerlas todas; las empatadas, al azar
        Collections.shuffle(candidatas);
        candidatas.sort(java.util.Comparator.comparingDouble(Puntuada::similitud).reversed());
        List<String> elegidas = new ArrayList<>();
        for (Puntuada candidata : candidatas) {
            if (elegidas.size() < cantidad && elegidas.stream().noneMatch(e -> esDemasiadoParecida(e, candidata.texto()))) {
                elegidas.add(candidata.texto());
            }
        }
        return elegidas.size() == cantidad ? elegidas : null;
    }

    /**
     * Arma una pregunta de opción única donde las incorrectas son la misma oración con un dato cambiado (un número o un nombre de otra parte del documento),
     * y las que falten salen de otras oraciones parecidas del documento. Con el dato cambiado la opción tiene el mismo estilo y la misma estructura que la
     * correcta, y solo se distingue por el dato.
     *
     * @param oracion La oración del material, que es la respuesta correcta.
     * @param enunciado La pregunta, ya comprobada.
     * @param cambiadas Las oraciones con el dato cambiado (hasta tres).
     * @param otras Las otras oraciones del material, de donde salen las que falten.
     * @param usados Los enunciados ya usados, normalizados. Si la pregunta sirve, se agrega el suyo.
     * @return La pregunta lista, o nulo si no se pueden armar tres incorrectas o la pregunta ya se usó.
     */
    public static Pregunta unicaConCambios(String oracion, String enunciado, List<String> cambiadas, List<String> otras, Set<String> usados) {
        String buena = comoOracion(oracion);
        Set<String> vistos = new HashSet<>(List.of(normalizar(buena)));
        List<String> malas = new ArrayList<>();
        for (String cambiada : cambiadas) {
            String texto = comoOracion(cambiada);
            if (!texto.isEmpty() && texto.length() <= LARGO_MAXIMO_OPCION && vistos.add(normalizar(texto)) && malas.size() < 3) {
                malas.add(texto);
            }
        }
        if (malas.size() < 3) {
            List<String> parecidas = elegirDistractores(buena, oracion, null, enunciado, otras, 3 - malas.size(), "");
            if (parecidas == null) {
                return null;
            }
            for (String parecida : parecidas) {
                if (vistos.add(normalizar(parecida))) {
                    malas.add(parecida);
                }
            }
        }
        if (buena.isEmpty() || malas.size() < 3 || !usados.add(normalizar(enunciado))) {
            return null;
        }
        List<Opcion> opciones = new ArrayList<>(List.of(new Opcion(buena, true)));
        malas.forEach(texto -> opciones.add(new Opcion(texto, false)));
        Collections.shuffle(opciones, new Random());
        return new Pregunta(enunciado, opciones, Tipo.UNICA_RESPUESTA);
    }

    /**
     * Arma una pregunta de opción única. La pregunta la redacta el modelo; la correcta es la oración del material tal cual (o la definición, si la oración
     * tiene la forma «Término: definición»), y las tres incorrectas las elige el código entre las otras oraciones del material.
     *
     * @param oracion La oración del material.
     * @param enunciado La pregunta, ya comprobada con {@link #enunciadoValido}.
     * @param otras Las otras oraciones del material, de donde salen las incorrectas.
     * @param usados Los enunciados ya usados, normalizados. Si la pregunta sirve, se agrega el suyo.
     * @return La pregunta lista, o nulo si no hay tres incorrectas buenas o la pregunta ya se usó.
     */
    public static Pregunta unica(String oracion, String enunciado, List<String> otras, Set<String> usados) {
        Definicion definicion = comoDefinicion(oracion);
        String buena = comoOracion(definicion == null ? oracion : definicion.texto());
        List<String> malas = buena.isEmpty() ? null : elegirDistractores(buena, oracion, definicion != null, enunciado, otras, 3, "");
        if (malas == null || !usados.add(normalizar(enunciado))) {
            return null;
        }
        List<Opcion> opciones = new ArrayList<>(List.of(new Opcion(buena, true)));
        malas.forEach(texto -> opciones.add(new Opcion(texto, false)));
        Collections.shuffle(opciones, new Random());
        return new Pregunta(enunciado, opciones, Tipo.UNICA_RESPUESTA);
    }

    /**
     * Arma una pregunta de opción múltiple: las dos correctas son las dos oraciones del material tal cual, y las dos incorrectas son otras oraciones del
     * material que no mencionan la palabra clave de la consigna (si la mencionaran, también serían correctas).
     *
     * @param consignaClave La palabra clave de la consigna, normalizada.
     */
    public static Pregunta multiple(String oracionA, String oracionB, String enunciado, String consignaClave, List<String> otras, Set<String> usados) {
        String primera = comoOracion(oracionA);
        String segunda = comoOracion(oracionB);
        List<String> sinLasDos = new ArrayList<>(otras == null ? List.<String>of() : otras);
        sinLasDos.removeIf(o -> normalizar(o).equals(normalizar(oracionB)));
        List<String> malas = primera.isEmpty() || segunda.isEmpty() ? null
                : elegirDistractores(primera, oracionA, null, enunciado, sinLasDos, 2, consignaClave);
        if (malas == null || malas.stream().anyMatch(m -> esDemasiadoParecida(m, segunda)) || !usados.add(normalizar(enunciado))) {
            return null;
        }
        List<Opcion> opciones = new ArrayList<>(List.of(new Opcion(primera, true), new Opcion(segunda, true)));
        malas.forEach(texto -> opciones.add(new Opcion(texto, false)));
        Collections.shuffle(opciones, new Random());
        return new Pregunta(enunciado, opciones, Tipo.MULTIPLE_CHOICE);
    }

    /** La palabra clave de la consigna de una pregunta múltiple, normalizada: la que comparten las dos oraciones. */
    public static String claveDeLaConsigna(String a, String b, Contexto contexto) {
        return palabraClaveCompartida(a, b, contexto);
    }

    /**
     * Arma un verdadero o falso con la afirmación verdadera: la paráfrasis que escribió el modelo, si dice lo mismo que la oración (sus palabras,
     * números y nombres están en ella), o la oración tal cual.
     *
     * @param parafrasis La paráfrasis que escribió el modelo, o nulo.
     */
    public static Pregunta verdaderoFalso(String oracion, String parafrasis, Set<String> usados) {
        String texto = comoOracion(parafrasis);
        boolean sirve = texto.length() >= 12 && texto.length() <= LARGO_MAXIMO_OPCION && !texto.contains("?")
                && !repiteEtiquetas(texto, "") && FidelidadTextoUtil.cobertura(texto, oracion) >= 0.6
                && numerosYNombresEstanEn(texto, oracion);
        String enunciado = sirve ? texto : comoOracion(oracion);
        if (enunciado.isBlank() || !usados.add(normalizar(enunciado))) {
            return null;
        }
        return new Pregunta(enunciado, opcionesVerdaderoFalso(true), Tipo.VERDADERO_FALSO);
    }

    /**
     * Arma una afirmación falsa a partir de una oración con forma de definición, poniéndole al término la definición de otro término del material:
     * «Pruebas de software: se ocupa de los fundamentos de ingeniería…». La alteración la hace el código, con frases reales del texto.
     *
     * @return La afirmación falsa, o nulo si la oración no es una definición o no hay otra definición con qué intercambiarla.
     */
    public static String falsaDeDefinicion(String oracion, List<String> otras) {
        Definicion definicion = comoDefinicion(oracion);
        if (definicion == null) {
            return null;
        }
        String propia = comoOracion(definicion.texto());
        List<String> candidatas = new ArrayList<>();
        for (String otra : otras == null ? List.<String>of() : otras) {
            Definicion deLaOtra = comoDefinicion(otra);
            if (deLaOtra == null || normalizar(deLaOtra.termino()).equals(normalizar(definicion.termino())) || esItemDeLista(otra)) {
                continue;
            }
            String texto = deLaOtra.texto().replaceAll("[.\\s]+$", "");
            double razon = (double) texto.length() / propia.length();
            if (razon >= 0.5 && razon <= 1.8 && definicion.termino().length() + texto.length() + 3 <= LARGO_MAXIMO_OPCION
                    && !esDemasiadoParecida(propia, comoOracion(texto))) {
                candidatas.add(texto);
            }
        }
        if (candidatas.isEmpty()) {
            return null;
        }
        return definicion.termino() + ": " + candidatas.get(new Random().nextInt(candidatas.size())) + ".";
    }

    /** Arma un verdadero o falso con una afirmación falsa ya armada. */
    public static Pregunta verdaderoFalsoFalso(String afirmacion, Set<String> usados) {
        if (afirmacion == null || afirmacion.isBlank() || !usados.add(normalizar(afirmacion))) {
            return null;
        }
        return new Pregunta(afirmacion, opcionesVerdaderoFalso(false), Tipo.VERDADERO_FALSO);
    }

    // ----------------------------------------------------------------------
    // Opciones
    // ----------------------------------------------------------------------

    private static List<Opcion> opcionesVerdaderoFalso(boolean verdadera) {
        return List.of(new Opcion("Verdadero", verdadera), new Opcion("Falso", !verdadera));
    }

    // ----------------------------------------------------------------------
    // Validación de lo que devuelve el modelo
    // ----------------------------------------------------------------------

        // ----------------------------------------------------------------------
    // Reglas: preguntas armadas desde el texto
    // ----------------------------------------------------------------------

    /**
     * Busca, entre otras oraciones, una que comparta al menos dos palabras clave con la dada y no se le parezca demasiado: la pareja para armar una
     * pregunta de opción múltiple. Con una sola palabra en común (una genérica, como «docente») se juntan oraciones que no tienen relación.
     *
     * @return La oración, o nulo si ninguna sirve.
     */
    public static String parejaDe(String oracion, List<String> otras, Contexto contexto) {
        for (String otra : otras) {
            if (!normalizar(otra).equals(normalizar(oracion)) && !tieneSolapamiento(oracion, otra) && palabrasClaveCompartidas(oracion, otra, contexto) >= 2) {
                return otra;
            }
        }
        return null;
    }

    /** Cuántas palabras clave del material aparecen en las dos oraciones. */
    private static int palabrasClaveCompartidas(String a, String b, Contexto contexto) {
        String enA = " " + normalizar(a) + " ";
        String enB = " " + normalizar(b) + " ";
        int compartidas = 0;
        for (String clave : contexto.palabrasParaEmparejar) {
            if (enA.contains(" " + clave + " ") && enB.contains(" " + clave + " ")) {
                compartidas++;
            }
        }
        return compartidas;
    }

    /**
     * La consigna de una pregunta de opción múltiple sobre dos oraciones, armada por el código con la palabra clave que comparten: «¿Cuáles de estas
     * afirmaciones sobre «X» son correctas?». No se la pide al modelo, porque con un pedido de dos oraciones copiaba el ejemplo del prompt.
     *
     * @return La consigna, o nulo si las oraciones no comparten ninguna palabra clave.
     */
    public static String consignaMultiple(String a, String b, Contexto contexto) {
        String clave = palabraClaveCompartida(a, b, contexto);
        return clave.isBlank() ? null : "¿Cuáles de estas afirmaciones sobre «" + contexto.formaOriginal.getOrDefault(clave, clave) + "» son correctas?";
    }

    /** Una oración con forma de definición («Pruebas de software: verificación dinámica de que…»): el término y lo que dice de él. */
    public record Definicion(String termino, String texto) {
    }

    private static final Pattern FORMA_DE_DEFINICION = Pattern.compile("^([^:]{3,60}):\\s+(\\S.{24,})$");

    /**
     * Separa una oración con forma de definición en el término y su definición. A un modelo de 1B le va mucho mejor variando solo la definición, y la
     * pregunta se arma con el término.
     *
     * @return El término y la definición, o nulo si la oración no tiene esa forma.
     */
    public static Definicion comoDefinicion(String oracion) {
        java.util.regex.Matcher encontrado = FORMA_DE_DEFINICION.matcher(oracion == null ? "" : oracion.strip());
        if (!encontrado.matches() || encontrado.group(1).split("\\s+").length > 6 || encontrado.group(1).matches(".*[.;?!].*")) {
            return null;
        }
        return new Definicion(encontrado.group(1).strip(), encontrado.group(2).strip());
    }

    /** La consigna de una pregunta sobre la definición de un término: «¿Cuál de estas afirmaciones sobre «X» es correcta?». */
    public static String consignaDeDefinicion(String termino) {
        return "¿Cuál de estas afirmaciones sobre «" + termino + "» es correcta?";
    }

    /** La palabra clave más repetida que aparece en las dos oraciones, o vacío si no comparten ninguna. */
    private static String palabraClaveCompartida(String a, String b, Contexto contexto) {
        String enA = " " + normalizar(a) + " ";
        String enB = " " + normalizar(b) + " ";
        for (String clave : contexto.palabrasParaEmparejar) {
            if (enA.contains(" " + clave + " ") && enB.contains(" " + clave + " ")) {
                return clave;
            }
        }
        return "";
    }

    private static Pregunta deGlosario(Contexto contexto, int indice, Set<String> usados, Random azar) {
        if (contexto.glosario.size() < 4) {
            return null;
        }
        String[] entrada = contexto.glosario.get(Math.abs(indice) % contexto.glosario.size());
        String definicion = limitar(entrada[1], 110);
        String enunciado = "Según el glosario, ¿cuál es la definición de «" + entrada[0] + "»?";
        if (definicion.isBlank() || enunciado.length() > LARGO_MAXIMO_OPCION || !usados.add(normalizar(enunciado))) {
            return null;
        }
        List<Opcion> opciones = new ArrayList<>(List.of(new Opcion(definicion, true)));
        for (String[] otra : contexto.glosario) {
            if (opciones.size() < 4 && !otra[0].equals(entrada[0])) {
                opciones.add(new Opcion(limitar(otra[1], 110), false));
            }
        }
        Collections.shuffle(opciones, azar);
        return new Pregunta(enunciado, opciones, Tipo.UNICA_RESPUESTA);
    }

    private static Pregunta completarConPalabraClave(Contexto contexto, int indice, Set<String> usados, Random azar) {
        // La primera palabra clave, la más repetida, que aparece en alguna oración corta
        for (int i = 0; i < contexto.palabrasClave.size(); i++) {
            String clave = contexto.palabraClave(indice + i);
            String original = contexto.formaOriginal.getOrDefault(clave, clave);
            for (String oracion : contexto.sentencias) {
                // Una oración de la que ya salió otra pregunta (el modelo la deja en «usados», normalizada) no se usa de nuevo
                if (oracion.length() > 125 || usados.contains(normalizar(oracion)) || !Pattern.compile("(?iu)\\b" + Pattern.quote(original) + "\\b").matcher(oracion).find()) {
                    continue;
                }
                String enunciado = "Completá la oración: " + oracion.replaceFirst("(?iu)\\b" + Pattern.quote(original) + "\\b", "_____");
                if (!usados.add(normalizar(enunciado))) {
                    continue;
                }
                List<Opcion> opciones = new ArrayList<>(List.of(new Opcion(original, true)));
                // Los distractores son otras palabras clave que se parezcan a la tapada: de la misma clase (verbo en infinitivo o no), con mayúscula
                // o sin ella, y de preferencia con la misma terminación, para que concuerden con el resto de la oración
                char terminacion = Character.toLowerCase(original.charAt(original.length() - 1));
                boolean infinitivo = original.matches("(?iu).*(ar|er|ir)$");
                boolean conMayuscula = Character.isUpperCase(original.charAt(0));
                List<String> parecidas = new ArrayList<>();
                List<String> otras = new ArrayList<>();
                for (String otra : contexto.palabrasClave) {
                    String otraOriginal = contexto.formaOriginal.getOrDefault(otra, otra);
                    if (otra.equals(clave) || normalizar(oracion).contains(otra) || otraOriginal.matches("(?iu).*(ar|er|ir)$") != infinitivo
                            || Character.isUpperCase(otraOriginal.charAt(0)) != conMayuscula) {
                        continue;
                    }
                    (Character.toLowerCase(otraOriginal.charAt(otraOriginal.length() - 1)) == terminacion ? parecidas : otras).add(otraOriginal);
                }
                parecidas.addAll(otras);
                // Sin dos distractores del mismo lema entre sí ni con la correcta («curso» y «cursos»)
                Set<String> raices = new HashSet<>(List.of(lemaDe(original)));
                for (String distractor : parecidas) {
                    if (opciones.size() < 4 && raices.add(lemaDe(distractor))) {
                        opciones.add(new Opcion(distractor, false));
                    }
                }
                if (opciones.size() >= 3) {
                    // La oración queda usada: no se arma otra pregunta tapando una palabra distinta de la misma
                    usados.add(normalizar(oracion));
                    Collections.shuffle(opciones, azar);
                    return new Pregunta(enunciado, opciones, Tipo.UNICA_RESPUESTA);
                }
            }
        }
        return null;
    }
}
