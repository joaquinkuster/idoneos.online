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

    /** Los primeros 5 caracteres de una palabra, sin tildes y en minúscula: sirve para reconocer singular y plural, y otras formas de una misma palabra. */
    private static String raizDe(String palabra) {
        String normal = normalizar(palabra);
        return normal.length() > 5 ? normal.substring(0, 5) : normal;
    }

    /** Largo máximo, en caracteres, de un enunciado o de una opción. */
    private static final int LARGO_MAXIMO_OPCION = 255;

    private static final Pattern NUMERO = Pattern.compile("\\b\\d+\\b");

    /**
     * Las oraciones de un texto que sirven para armar una pregunta, en el orden del texto y sin repetir: de entre 6 y 28 palabras, que entran enteras
     * en una pregunta o una opción, que se entienden solas y tienen verbo.
     */
    private static List<String> oracionesCandidatas(String limpio, int maximo) {
        List<String> candidatas = new ArrayList<>();
        for (String parte : limpio.split("(?<=[.!?])\\s+")) {
            String oracion = limpiarOracion(parte);
            int palabras = oracion.isEmpty() ? 0 : oracion.split("\\s+").length;
            // Solo oraciones que entran enteras en una pregunta o una opción (255 caracteres): cortarlas deja la idea a medias
            if (palabras >= 6 && palabras <= 40 && oracion.length() <= LARGO_MAXIMO_OPCION && esOracionDeContenido(oracion) && !candidatas.contains(oracion)) {
                candidatas.add(oracion);
            }
            if (candidatas.size() >= maximo) {
                break;
            }
        }
        return candidatas;
    }

    /**
     * Las oraciones más importantes de un texto, según el código, con TF-IDF. El peso de una palabra de contenido es su frecuencia en el texto (TF: cuántas
     * veces aparece) por su rareza entre las oraciones (IDF = ln(N / df), con N oraciones y df las que la contienen): una palabra que está en todas las
     * oraciones pesa cero, y una que es del tema pero está concentrada en pocas pesa mucho. La importancia de una oración es la suma de los pesos de
     * sus palabras, dividida por la raíz de la cantidad de palabras para no favorecer a las más largas. Se mira todo el texto, no solo el principio, y
     * para no concentrarse en un solo capítulo el texto se divide en tramos y de cada tramo se toman, como mucho, unas pocas oraciones. Si así no
     * alcanzan, se completa sin ese límite.
     *
     * @param texto El texto completo.
     * @param cantidad Cuántas oraciones devolver como máximo.
     * @return Las oraciones, de la más importante a la menos importante. Solo entran las que sirven para armar una pregunta.
     */
    public static List<String> masImportantes(String texto, int cantidad) {
        List<String> candidatas = oracionesCandidatas(limpiar(texto), Integer.MAX_VALUE);
        int n = candidatas.size();
        if (n == 0 || cantidad <= 0) {
            return List.of();
        }
        // TF: cuántas veces aparece cada palabra de contenido en las oraciones; DF: en cuántas oraciones aparece
        List<Set<String>> palabrasPorOracion = new ArrayList<>();
        Map<String, Integer> tf = new HashMap<>();
        Map<String, Integer> df = new HashMap<>();
        for (String candidata : candidatas) {
            Set<String> distintas = palabrasDeContenido(candidata);
            palabrasPorOracion.add(distintas);
            for (String palabra : normalizar(candidata).split("\\s+")) {
                if (distintas.contains(palabra)) {
                    tf.merge(palabra, 1, Integer::sum);
                }
            }
            distintas.forEach(palabra -> df.merge(palabra, 1, Integer::sum));
        }
        double[] puntaje = new double[n];
        Integer[] orden = new Integer[n];
        for (int i = 0; i < n; i++) {
            Set<String> palabras = palabrasPorOracion.get(i);
            double suma = 0;
            for (String palabra : palabras) {
                suma += tf.get(palabra) * Math.log((double) n / df.get(palabra));
            }
            puntaje[i] = suma / Math.sqrt(Math.max(1, palabras.size()));
            orden[i] = i;
        }
        // De mayor a menor importancia; si empatan, la que viene primero en el texto
        Arrays.sort(orden, (x, y) -> {
            int comparacion = Double.compare(puntaje[y], puntaje[x]);
            return comparacion != 0 ? comparacion : Integer.compare(x, y);
        });
        int tramos = Math.min(10, Math.max(1, n / 3));
        int maximoPorTramo = Math.max(1, (int) Math.ceil(cantidad * 1.5 / tramos));
        int[] porTramo = new int[tramos];
        List<String> elegidas = new ArrayList<>();
        Set<Integer> tomadas = new HashSet<>();
        for (int i : orden) {
            int tramo = Math.min(tramos - 1, (int) ((long) i * tramos / n));
            if (elegidas.size() < cantidad && porTramo[tramo] < maximoPorTramo) {
                porTramo[tramo]++;
                elegidas.add(candidatas.get(i));
                tomadas.add(i);
            }
        }
        for (int i : orden) {
            if (elegidas.size() < cantidad && !tomadas.contains(i)) {
                elegidas.add(candidatas.get(i));
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

    /**
     * Descarta lo que no es una afirmación del contenido, como encabezados de página, títulos y puntos de un índice.
     * Un título tiene casi todas las palabras con inicial en mayúscula, y un índice tiene muchos puntos seguidos o números.
     * Es un agregado a lo de AUREA, que toma las primeras oraciones tal como vienen.
     */
    private static boolean esOracionDeContenido(String oracion) {
        if (oracion.contains("....") || oracion.matches("^\\d+[.)].*") || dependeSinLista(oracion) || !VERBO.matcher(oracion).find()) {
            return false;
        }
        // Una oración cortada (sin punto final) o que anuncia una lista («…en dos razones:.») está incompleta
        if (!oracion.matches("(?s).*[.!?\"»”)]$") || oracion.matches("(?s).*:\\s*[.]?$")) {
            return false;
        }
        String[] palabras = oracion.split("\\s+");
        // Una fila de glosario o de tabla («OBS Sistema externo Software de…», «Programa Dominio Versión del…», «33 Propósito Permite…») no es una afirmación:
        // empieza con un número o tiene la segunda palabra con mayúscula. Las notas al pie («Nota metodológica: …») tampoco
        boolean empiezaConPreposicion = INICIOS_CON_NOMBRE.contains(palabras[0].toLowerCase(Locale.ROOT));
        if (palabras.length > 1 && (palabras[0].matches("\\d+") || (Character.isUpperCase(palabras[1].charAt(0)) && !empiezaConPreposicion))
                || ETIQUETA_DE_GLOSARIO.matcher(oracion).find() || oracion.matches("(?is).*\\bNota metodol[oó]gica\\b.*")) {
            return false;
        }
        long enMayuscula = Arrays.stream(palabras).filter(p -> !p.isEmpty() && Character.isUpperCase(p.charAt(0))).count();
        long digitos = oracion.chars().filter(Character::isDigit).count();
        return enMayuscula * 100 <= palabras.length * 40L && digitos * 4 < oracion.length();
    }

    /** Palabras con las que puede empezar una oración seguida de un nombre propio («Según Sebastián Bordato…»), que no es una fila de tabla. */
    private static final Set<String> INICIOS_CON_NOMBRE = Set.of("según", "para", "con", "sin", "desde", "hasta", "durante", "mediante", "tras", "entre",
            "sobre", "ante", "bajo");

    /** Etiqueta de categoría de un glosario seguida de la definición («…Dominio Conjunto de preguntas…», «…Sistema externo Pasarela de pagos…»). */
    private static final Pattern ETIQUETA_DE_GLOSARIO = Pattern.compile("\\b(Dominio|Sistema externo|Servicio externo)\\s+[A-ZÁÉÍÓÚ]");

    /** Código de requisito o de caso de uso, como MOD-F-01, RF-12 o ACT-01. */
    private static final Pattern CODIGO = Pattern.compile("\\b[A-Z]{1,6}(?:-[A-Z0-9]{1,5})*-\\d{1,3}\\b");

    /** Guion o barra suelta entre espacios, que en un texto sacado de una tabla separa celdas. */
    private static final Pattern SEPARADOR = Pattern.compile("\\s[-–—|/]\\s");

    /** Final de oración que es el encabezado de una tabla, como «Funciones 1»: una palabra con mayúscula y un número. */
    private static final Pattern FINAL_DE_TABLA = Pattern.compile("[A-ZÁÉÍÓÚ][a-záéíóúñ]+ \\d+[.]?$");

    /** Viñetas y símbolos de lista que el PDF deja en el texto: una oración que los tiene es un ítem de una lista o una celda, no una afirmación. */
    private static final Pattern VINETA = Pattern.compile("[•●▪■□‣◦·\\uE000-\\uF8FF]");

    /** Remite a lo que viene después o a otra parte del documento, y fuera de él no se entiende. */
    private static final Pattern REMISION = Pattern.compile("(?iu)\\b(a continuaci[oó]n|el siguiente|la siguiente|los siguientes|las siguientes|se listan|se enumeran"
            + "|se detallan|m[aá]s abajo|m[aá]s arriba|en este (contexto|caso|documento|texto|cap[ií]tulo|apartado|material|escenario|m[oó]dulo|flujo|punto|paso)|en esta (secci[oó]n|parte)"
            + "|este (cap[ií]tulo|documento|apartado)|esta secci[oó]n|el presente|la presente|en esta (?:lectura|unidad|clase)|lo anterior|lo siguiente|(?:frente a|ante|dado|debido a) (?:este|esta|esto|estos|estas))\\b");

    /**
     * Indica si una oración arrastra restos de una tabla del documento: códigos como MOD-F-01, encabezados de columnas, guiones sueltos
     * o tres palabras con mayúscula seguidas. Una oración así queda gramaticalmente incoherente.
     */
    public static boolean tieneRestosDeTabla(String oracion) {
        if (CODIGO.matcher(oracion).find() || SEPARADOR.matcher(oracion).find() || FINAL_DE_TABLA.matcher(oracion.strip()).find()) {
            return true;
        }
        String[] palabras = oracion.strip().split("\\s+");
        int racha = 0;
        for (int i = 1; i < palabras.length; i++) {
            boolean conMayuscula = palabras[i].length() > 2 && Character.isUpperCase(palabras[i].charAt(0));
            racha = conMayuscula ? racha + 1 : 0;
            if (racha >= 4) {
                return true;
            }
            if (palabras[i].endsWith(",")) {
                racha = 0;
            }
        }
        return false;
    }

    /** Palabras con las que empieza una oración que se entiende solo con la anterior: demostrativos, pronombres y conectores. */
    private static final Pattern INICIO_DEPENDIENTE = Pattern.compile("(?iu)^(este|esta|esto|estos|estas|ese|esa|eso|esos|esas|aquel|aquella|aquellos|aquellas"
            + "|ambos|ambas|dicho|dicha|dichos|dichas|mencionad[oa]s?|ello|ellos|ellas|ella|él|además|asimismo|también|sin embargo|por ello|por esto"
            + "|por lo tanto|en consecuencia|así|pero|y|o|ni|sino|aunque|luego|entonces|finalmente|por otro lado|por último|en cambio|los mismos"
            + "|las mismas|el mismo|la misma|lo anterior|lo siguiente|cada uno|cada una|dicho|a esto|a ello|no son|no es|no se)\\b");

    /** Remite a otra parte del documento, como «sección 4.4» o «figura 2», y sin esa parte no se entiende. */
    private static final Pattern REFERENCIA_INTERNA = Pattern.compile("(?iu)\\b(secci[oó]n|cap[ií]tulo|apartado|figura|tabla|diagrama|anexo|punto)\\s+\\d");

    /** Un verbo en infinitivo al principio, que es el arranque de un ítem de una lista («Gestionar cursos (alta, baja…)») y no una oración. */
    private static final Pattern ARRANQUE_DE_LISTA = Pattern.compile("^[A-ZÁÉÍÓÚ][a-záéíóúñ]{3,}(?:ar|er|ir)\\b");

    /**
     * Indica si una oración depende de otra para entenderse: empieza con un demostrativo o un conector o remite a otra parte del documento. Una
     * pregunta armada con ella queda sin contexto. Un ítem de lista en infinitivo («Gestionar pagos…») sí es una oración válida: es un objetivo.
     */
    private static boolean dependeSinLista(String oracion) {
        String limpia = oracion.strip();
        return INICIO_DEPENDIENTE.matcher(limpia).find() || REFERENCIA_INTERNA.matcher(limpia).find() || REMISION.matcher(limpia).find()
                || VINETA.matcher(limpia).find() || tieneRestosDeTabla(limpia);
    }

    /** Igual que {@link #dependeSinLista}, y además un ítem de lista en infinitivo: en una pregunta o una opción que escribe el modelo no sirve. */
    private static boolean dependeDeOtraOracion(String oracion) {
        String limpia = oracion.strip();
        return INICIO_DEPENDIENTE.matcher(limpia).find() || REFERENCIA_INTERNA.matcher(limpia).find() || ARRANQUE_DE_LISTA.matcher(limpia).find()
                || REMISION.matcher(limpia).find() || VINETA.matcher(limpia).find() || tieneRestosDeTabla(limpia);
    }

    /** Formas de verbos frecuentes y terminaciones de pasado y futuro. Una oración sin ninguna es casi siempre un título o una descripción corta. */
    private static final Pattern VERBO = Pattern.compile("(?iu)\\b(es|son|era|eran|fue|fueron|ser[aá]n?|est[aá]n?|tiene|tienen|ten[ií]a|permite|permiten|debe|deben"
            + "|puede|pueden|incluye|incluyen|define|definen|describe|describen|expresa|expresan|especifica|especifican|busca|buscan|ofrece|ofrecen"
            + "|cubre|cubren|contempla|contemplan|hay|existe|existen|comprende|comprenden|consiste|consisten|implica|implican|requiere|requieren"
            + "|presenta|presentan|identifica|identifican|gestiona|gestionan|genera|generan|realiza|realizan|utiliza|utilizan|representa|representan"
            + "|garantiza|garantizan|organiza|organizan|propone|proponen|resulta|resultan|surge|surgen|forma|forman|corresponde|corresponden"
            + "|depende|dependen|determina|determinan|establece|establecen|proporciona|proporcionan|brinda|brindan|facilita|facilitan|mejora|mejoran"
            + "|reduce|reducen|aumenta|aumentan|compone|componen|se[ñn]ala|se[ñn]alan|indica|indican|muestra|muestran|abarca|abarcan|constituye"
            + "|constituyen|asigna|asignan|almacena|almacenan|registra|registran|ejecuta|ejecutan|accede|acceden|publica|publican|cursa|cursan)\\b"
            + "|\\bse\\s+\\p{L}{4,}|\\b\\p{L}+(aron|ieron|ar[aá]n?|er[aá]n?|ir[aá]n?|aba|aban|amos|emos)\\b");

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

    /** Título seguido de una sigla entre paréntesis y pegado a la oración que sigue, como «Requisitos de Información (RQI) Los requisitos…». */
    private static final Pattern TITULO_PEGADO = Pattern.compile("^.{0,90}?\\([A-Z]{2,6}\\)\\s+(?=[A-ZÁÉÍÓÚ])");

    /** Palabras con las que empieza una pregunta que el modelo escribió sin signos. */
    private static final Pattern INICIO_DE_PREGUNTA = Pattern.compile("(?iu)^(qu[eé]|cu[aá]l|cu[aá]les|c[oó]mo|cu[aá]ndo|d[oó]nde|qui[eé]n|qui[eé]nes|por qu[eé]|cu[aá]nto|cu[aá]ntos|cu[aá]ntas|para qu[eé]|en qu[eé])(?!\\p{L})");

    /** Habla de la oración o del texto como un objeto: una pregunta así no se entiende sin verlos. */
    private static final Pattern HABLA_DEL_TEXTO = Pattern.compile("(?iu)\\b(la oraci[oó]n|el fragmento|la frase|el p[aá]rrafo|el enunciado|palabras? clave|el documento|el material)\\b");

    /** Dos preguntas en una («¿Qué motivó a los socios y qué obstáculos enfrentaban?»): un modelo chico las parafrasea enteras y regalan la respuesta. */
    private static final Pattern DOBLE_PREGUNTA = Pattern.compile("(?iu)\\b(y|e)\\s+(qu[eé]|cu[aá]l|cu[aá]les|c[oó]mo|cu[aá]ndo|d[oó]nde|qui[eé]n|qui[eé]nes|por qu[eé]|cu[aá]nto|cu[aá]ntos)(?!\\p{L})");

    /** Cierra los signos de una pregunta: «¿…?». Devuelve vacío si lo que escribió el modelo no es una pregunta. */
    private static String cerrarPregunta(String texto) {
        String limpio = limpiar(texto);
        String cuerpo = limpio.replaceAll("^[¿?\\s]+", "").replaceAll("[?\\s.;:,]+$", "");
        if (cuerpo.isEmpty()) {
            return "";
        }
        if (limpio.startsWith("¿") || limpio.endsWith("?") || INICIO_DE_PREGUNTA.matcher(cuerpo).find()) {
            return "¿" + cuerpo + "?";
        }
        return "";
    }

    /** Las raíces (los primeros 5 caracteres) de las palabras de contenido de un texto: «exista» y «existe» cuentan como la misma. */
    private static Set<String> raicesDeContenido(String texto) {
        Set<String> raices = new HashSet<>();
        for (String palabra : palabrasDeContenido(texto)) {
            raices.add(palabra.length() > 5 ? palabra.substring(0, 5) : palabra);
        }
        return raices;
    }

    /** Las palabras de 4 letras o más de un texto, sin tildes y sin las comunes. */
    private static Set<String> palabrasDeContenido(String texto) {
        Set<String> palabras = new HashSet<>();
        for (String palabra : normalizar(texto).split(" ")) {
            if (palabra.length() >= 4 && !PalabrasClaveUtil.esComun(palabra)) {
                palabras.add(palabra);
            }
        }
        return palabras;
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
        // Con dos preguntas en una («¿Qué significa mantener el software y qué tipo de actividad es?») se queda la primera, si tiene sentido sola
        java.util.regex.Matcher doble = DOBLE_PREGUNTA.matcher(cuerpo);
        if (doble.find()) {
            cuerpo = cuerpo.substring(0, doble.start()).replaceAll("[\\s,;]+$", "");
            if (cuerpo.split("\\s+").length < 4) {
                return null;
            }
            enunciado = "¿" + cuerpo + "?";
        }
        // Una pregunta que repite casi toda la oración («¿Qué significa que exista al menos una inscripción registrada?») ya dice la respuesta
        Set<String> deLaPregunta = raicesDeContenido(cuerpo);
        Set<String> deLaOracion = raicesDeContenido(fuentes);
        if (deLaPregunta.size() >= 4 && deLaPregunta.stream().filter(deLaOracion::contains).count() * 10 >= deLaPregunta.size() * 8
                && deLaPregunta.size() * 100 >= deLaOracion.size() * 45) {
            return null;
        }
        // Una pregunta que pide un dato suelto («¿En qué año…?», «¿Cuántos…?») no tiene como respuesta una oración entera
        if (cuerpo.matches("(?iu)^\\s*(en )?(qu[eé] a[ñn]o|cu[aá]nt[oa]s?|cu[aá]ndo)(?!\\p{L}).*")) {
            return null;
        }
        if (dependeDeOtraOracion(cuerpo) || HABLA_DEL_TEXTO.matcher(cuerpo).find() || DOBLE_PREGUNTA.matcher(cuerpo).find()
                || coberturaSinGenericas(cuerpo, fuentes) < 0.4 || !numerosYNombresEstanEn(cuerpo, fuentes + " " + contexto)) {
            return null;
        }
        return enunciado;
    }

    private static double coberturaSinGenericas(String frase, String fuente) {
        return FidelidadTextoUtil.cobertura(frase.replaceAll(PALABRAS_GENERICAS_DE_PREGUNTA, " "), fuente);
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

    /** Palabras que una pregunta suele traer aunque la oración no las tenga: «número», «total», «tipo»… No cuentan al comprobar que se apoya en ella. */
    private static final String PALABRAS_GENERICAS_DE_PREGUNTA = "(?iu)\\b(n[uú]mero|cantidad|total|tipo|tipos|nombre|forma|manera|principal|principales|caracter[ií]sticas?|propiedades|funci[oó]n)\\b";

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

    /** Título en mayúsculas pegado a la oración que sigue, como «Objetivos del Sistema El sistema se propone…»: palabras con mayúscula y, enseguida, un artículo con mayúscula. */
    private static final Pattern TITULO_CAPITALIZADO = Pattern.compile("^((?:[A-ZÁÉÍÓÚ][\\p{L}/-]*\\s+|(?:de|del|la|las|los|el|y|en|para|con)\\s+)+?)(?=(?:El|La|Los|Las|Se|Cada|Este|Esta|Un|Una|Dado)\\s)");

    /** Remisión entre paréntesis a otra parte del documento, como «(sección 2.4)», que se puede quitar sin que la oración pierda sentido. */
    private static final Pattern REFERENCIA_ENTRE_PARENTESIS = Pattern.compile("\\s*\\((?:ver\\s+)?(?:secci[oó]n|cap[ií]tulo|apartado|figura|tabla)\\s+[\\d.]+\\)");

    /** Quita el título que el PDF dejó pegado al principio de una oración, y las remisiones entre paréntesis a otras partes del documento. */
    private static String sinTituloPegado(String oracion) {
        String limpia = TITULO_PEGADO.matcher(oracion).replaceFirst("");
        Matcher titulo = TITULO_CAPITALIZADO.matcher(limpia);
        if (titulo.find()) {
            int palabras = titulo.group(1).strip().split("\\s+").length;
            if (palabras >= 2 && palabras <= 8) {
                limpia = limpia.substring(titulo.end());
            }
        }
        // Título con palabras en minúscula («Qué es la renta fija Un instrumento…», «Mercado primario y mercado secundario En el mercado primario…»):
        // sin puntuación, de 2 a 9 palabras, seguido de un artículo o preposición con mayúscula, y sus palabras vuelven a aparecer enseguida en la oración
        Matcher conMinusculas = TITULO_CON_MINUSCULAS.matcher(limpia);
        if (conMinusculas.find()) {
            String encabezado = conMinusculas.group(1).strip();
            String resto = limpia.substring(conMinusculas.end());
            int palabras = encabezado.split("\\s+").length;
            String primeras = String.join(" ", Arrays.asList(normalizar(resto).split(" ")).subList(0, Math.min(15, normalizar(resto).split(" ").length)));
            if (palabras >= 2 && palabras <= 9 && raicesDeContenido(encabezado).stream().anyMatch(raiz -> raicesDeContenido(primeras).contains(raiz))) {
                limpia = resto;
            }
        }
        return REFERENCIA_ENTRE_PARENTESIS.matcher(limpia).replaceAll("");
    }

    /** Título sin puntuación al principio, seguido de un artículo o preposición con mayúscula que empieza la oración de verdad. */
    private static final Pattern TITULO_CON_MINUSCULAS = Pattern.compile("^([A-ZÁÉÍÓÚ][^.,;:!?()\"]*?)\\s+(?=(?:El|La|Los|Las|Un|Una|Unos|Unas|En|Se|Cada|Para|Con)\\s)");

    /** Etiqueta de una ficha o tabla pegada al principio («Descripción • Administrar…», «Postcondición ● La inscripción queda…», «33 Propósito Permite…»). */
    private static final Pattern ETIQUETA_DE_TABLA = Pattern.compile("^(?:\\d{1,3}\\s+)?(?:(?:Descripci[oó]n|Prop[oó]sito|Precondici[oó]n|Postcondici[oó]n|Entrada|Salida|Actores?"
            + "|Alcance|Comentarios|Observaciones|Resultado)\\s+)*(?:[•●▪■□‣◦·\\uE000-\\uF8FF]\\s*|(?=[A-ZÁÉÍÓÚ]))");

    /** Remisión a otra parte del documento al final de una oración («…específicos definidos en la sección 3.3»), que se puede quitar sin que pierda sentido. */
    private static final Pattern REMISION_AL_FINAL = Pattern.compile("\\s*,?\\s*(?:definid|descript|presentad|detallad|desarrollad|especificad)[oa]s?\\s+en\\s+(?:la|el)\\s+"
            + "(?:secci[oó]n|cap[ií]tulo|apartado)\\s+[\\d.]+(?=[.!?]?$)");

    /**
     * Deja una oración del texto lista para usarse: le quita la numeración de lista, la etiqueta de tabla o la viñeta del principio, el título pegado,
     * las remisiones a otras partes del documento («(sección 2.4)», «definidos en la sección 3.3») y el número suelto del final.
     */
    private static String limpiarOracion(String parte) {
        String oracion = sinNumeracion(parte);
        // Las etiquetas de tabla solo se quitan si después queda una oración con al menos 4 palabras
        String sinEtiqueta = ETIQUETA_DE_TABLA.matcher(oracion).replaceFirst("").strip();
        if (!sinEtiqueta.equals(oracion) && sinEtiqueta.split("\\s+").length >= 4) {
            oracion = sinEtiqueta;
        }
        oracion = REMISION_AL_FINAL.matcher(sinTituloPegado(oracion)).replaceFirst("");
        // Un número suelto al final («…el sistema 1.») es un resto de página o de tabla, no parte de la oración
        return oracion.replaceFirst("\\s+P[áa]g\\.?$", ".").replaceFirst("\\s+\\d{1,3}\\s*([.!?])?$", "$1");
    }

    /** Quita la numeración de lista del principio, como «1. » o «a) », que se copia del PDF y no es parte del texto. */
    private static String sinNumeracion(String texto) {
        return limpiar(texto).replaceFirst("^(?:\\d+|[a-dA-D])[.)]\\s+", "");
    }

    /**
     * Corta un texto al largo indicado, sin puntos suspensivos y sin dejar la idea a medias: si se pasa, corta en la última coma, punto y coma
     * o dos puntos que haya en su segunda mitad, y si no hay, en una palabra entera quitando lo que quedaría colgando al final,
     * como un conector («de», «y», «con») o un gerundio («contemplando»).
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
        return corte.replaceAll("(?iu)(\\s+(y|o|e|de|del|la|el|los|las|con|en|a|que|para|por|un|una|se|su|al|como|sin|sobre|entre|desde|hasta"
                + "|\\p{L}+(?:ando|iendo|yendo)))+$", "").replaceAll("[,;:\\s]+$", "");
    }

    private static String capitalizar(String texto) {
        return texto == null || texto.isBlank() ? "" : texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }

    private static Set<String> palabrasRelevantes(String texto) {
        Set<String> palabras = new HashSet<>();
        for (String palabra : normalizar(texto).split("\\s+")) {
            // «solo» se ignora porque es lo que agrega la restricción indebida, y sin eso una opción así parecería distinta de la original
            if (palabra.length() >= 4 && !PalabrasClaveUtil.esComun(palabra) && !palabra.equals("solo")) {
                palabras.add(palabra);
            }
        }
        return palabras;
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

    /**
     * Elige, entre las otras oraciones del material, las que sirven como respuestas incorrectas de una pregunta: tratan de lo mismo (comparten palabras
     * con la correcta), tienen un largo parecido, no se parecen entre sí y no responden también a la pregunta. Son frases reales del material.
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
        Set<String> deLaCorrecta = palabrasDeContenido(correcta);
        Set<String> deLaPregunta = palabrasDeContenido(enunciado);
        record Puntuada(String texto, int comunes, boolean mismoInicio) {
        }
        String inicioDeLaCorrecta = normalizar(correcta).split(" ")[0];
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
            // Una oración que también responde a la pregunta no puede ser una opción incorrecta
            long enLaPregunta = palabras.stream().filter(deLaPregunta::contains).count();
            if (!deLaPregunta.isEmpty() && enLaPregunta * 5 > deLaPregunta.size() * 3) {
                continue;
            }
            // Sin ninguna palabra de contenido en común con la correcta ni con la pregunta, la opción no tiene relación con ella: no sirve
            int comunes = (int) palabras.stream().filter(deLaCorrecta::contains).count();
            if (comunes == 0 && enLaPregunta == 0) {
                continue;
            }
            candidatas.add(new Puntuada(texto, comunes, normalizar(texto).split(" ")[0].equals(inicioDeLaCorrecta)));
        }
        // Primero las que empiezan igual que la correcta («Se muestra…», «Existe…»), para que las cuatro opciones parezcan del mismo tipo, y después las que
        // más palabras comparten con ella; las empatadas, al azar
        Collections.shuffle(candidatas);
        candidatas.sort(java.util.Comparator.comparing(Puntuada::mismoInicio).reversed().thenComparing(java.util.Comparator.comparingInt(Puntuada::comunes).reversed()));
        List<String> elegidas = new ArrayList<>();
        for (Puntuada candidata : candidatas) {
            if (elegidas.size() < cantidad && elegidas.stream().noneMatch(e -> esDemasiadoParecida(e, candidata.texto()))) {
                elegidas.add(candidata.texto());
            }
        }
        return elegidas.size() == cantidad ? elegidas : null;
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
        if (malas == null || !usados.add(normalizar(enunciado + " " + buena))) {
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
        if (malas == null || malas.stream().anyMatch(m -> esDemasiadoParecida(m, segunda)) || !usados.add(normalizar(enunciado + " " + primera))) {
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
        boolean sirve = texto.length() >= 12 && texto.length() <= LARGO_MAXIMO_OPCION && !dependeDeOtraOracion(texto)
                && !HABLA_DEL_TEXTO.matcher(texto).find() && !INICIO_DE_PREGUNTA.matcher(texto).find() && coberturaSinGenericas(texto, oracion) >= 0.6
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
                if (oracion.length() > 125 || !Pattern.compile("(?iu)\\b" + Pattern.quote(original) + "\\b").matcher(oracion).find()) {
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
                // Sin dos distractores de la misma raíz entre sí ni con la correcta («curso» y «cursos»)
                Set<String> raices = new HashSet<>(List.of(raizDe(original)));
                for (String distractor : parecidas) {
                    if (opciones.size() < 4 && raices.add(raizDe(distractor))) {
                        opciones.add(new Opcion(distractor, false));
                    }
                }
                if (opciones.size() >= 3) {
                    Collections.shuffle(opciones, azar);
                    return new Pregunta(enunciado, opciones, Tipo.UNICA_RESPUESTA);
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Respaldo final con plantillas
    // ----------------------------------------------------------------------

}
