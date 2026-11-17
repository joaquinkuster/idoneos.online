package com.app.idoneos.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Saca de un texto las oraciones que sirven para armar preguntas. No usa listas de palabras ni expresiones con palabras: sirve para cualquier documento
 * y cualquier tema. Decide con dos clases de señales:
 * <ul>
 * <li><b>Estructura:</b> mayúsculas, puntuación, números, códigos, símbolos de lista y largo.</li>
 * <li><b>Estadística del propio texto:</b> qué palabras se usan en minúscula y cuáles son cortas y frecuentes (los artículos y las preposiciones de cada
 * idioma), calculado sobre el documento que se está leyendo.</li>
 * </ul>
 */
public final class OracionesUtil {

    private OracionesUtil() {
    }

    /** Código de una ficha o de un requisito: letras en mayúscula, guion y número, como «AB-12» o «XYZ-A-03». */
    private static final Pattern CODIGO = Pattern.compile("\\b[A-Z]{1,6}(?:-[A-Z0-9]{1,5})*-\\d{1,3}\\b");

    /** Guion o barra suelta entre espacios, que en un texto sacado de una tabla separa celdas. */
    private static final Pattern SEPARADOR = Pattern.compile("\\s[-–—|/]\\s");

    /** Final de oración que es el encabezado de una tabla: una palabra con mayúscula y un número. */
    private static final Pattern FINAL_DE_TABLA = Pattern.compile("[A-ZÁÉÍÓÚ][a-záéíóúñ]+ \\d+[.]?$");

    /** Viñetas y símbolos de lista que el PDF deja en el texto. */
    private static final Pattern VINETA = Pattern.compile("[•●▪■□‣◦·\\uE000-\\uF8FF]");

    private static final String SEPARADOR_DE_VINETAS = "\\s*[•●▪■□‣◦\\uE000-\\uF8FF]\\s*";

    /** Número de sección o de figura con puntos («3.5.1»). No es el de miles («2.000»), que siempre tiene tres cifras después del punto. */
    private static final String NUMERO_DE_SECCION = "\\b\\d{1,2}(?:\\.\\d{1,2})+(?!\\d)";

    private static final Pattern REFERENCIA_DE_SECCION = Pattern.compile(NUMERO_DE_SECCION);

    /** Paréntesis que remite a otra parte del documento: lleva un número de sección adentro. */
    private static final Pattern PARENTESIS_CON_REFERENCIA = Pattern.compile("\\s*\\([^()]*" + NUMERO_DE_SECCION + "[^()]*\\)");

    /** Título seguido de una sigla entre paréntesis y pegado a la oración que sigue: «Algo de Algo (ABC) La oración…». */
    private static final Pattern TITULO_CON_SIGLA = Pattern.compile("^.{0,90}?\\([A-Z]{2,6}\\)\\s+(?=[A-ZÁÉÍÓÚ])");

    /** Numeración de lista al principio: «1. », «a) ». */
    private static final Pattern NUMERACION = Pattern.compile("^(?:\\d+|[a-dA-D])[.)]\\s+");

    private static final int PALABRAS_MINIMAS = 6;
    private static final int PALABRAS_MAXIMAS = 40;
    private static final int CARACTERES_MAXIMOS = 255;

    /**
     * Lo que se sabe del texto que se está leyendo: qué palabras escribe en minúscula (y por lo tanto no son nombres propios) y en cuántos trozos
     * aparece cada una. Con eso se reconocen las palabras cortas y frecuentes de la lengua (artículos, preposiciones, conjunciones) sin tener una lista.
     */
    static final class Estadistica {
        private final Map<String, Integer> enMinuscula = new HashMap<>();
        private final Map<String, Integer> trozosConLaPalabra = new HashMap<>();
        /** Cuántas veces aparecen juntas dos palabras con mayúscula («Idóneos Online»): si se repiten, son un nombre y no una etiqueta. */
        private final Map<String, Integer> paresConMayuscula = new HashMap<>();
        private final int trozos;

        Estadistica(List<String> trozosDelTexto) {
            this.trozos = Math.max(1, trozosDelTexto.size());
            for (String trozo : trozosDelTexto) {
                Set<String> vistas = new HashSet<>();
                String[] palabrasDelTrozo = trozo.split("\\s+");
                for (int i = 1; i < palabrasDelTrozo.length; i++) {
                    if (Character.isUpperCase(primeraLetra(palabrasDelTrozo[i - 1])) && Character.isUpperCase(primeraLetra(palabrasDelTrozo[i]))) {
                        paresConMayuscula.merge(LogicaPreguntasAurea.normalizar(palabrasDelTrozo[i - 1] + " " + palabrasDelTrozo[i]), 1, Integer::sum);
                    }
                }
                for (String palabra : palabrasDelTrozo) {
                    String normal = LogicaPreguntasAurea.normalizar(palabra);
                    if (normal.isEmpty()) {
                        continue;
                    }
                    if (Character.isLowerCase(primeraLetra(palabra))) {
                        enMinuscula.merge(normal, 1, Integer::sum);
                    }
                    vistas.add(normal);
                }
                vistas.forEach(palabra -> trozosConLaPalabra.merge(palabra, 1, Integer::sum));
            }
        }

        /** Las dos palabras con mayúscula aparecen juntas al menos dos veces en el texto: es un nombre y no una etiqueta. */
        boolean esNombreRepetido(String primera, String segunda) {
            return paresConMayuscula.getOrDefault(LogicaPreguntasAurea.normalizar(primera + " " + segunda), 0) >= 2;
        }

        /** El texto escribe la palabra en minúscula al menos dos veces: es una palabra de la lengua y no un nombre propio. */
        boolean esPalabraDeLaLengua(String palabra) {
            return enMinuscula.getOrDefault(LogicaPreguntasAurea.normalizar(palabra), 0) >= 2;
        }

        /** El texto escribe la palabra en minúscula al menos una vez: es una palabra de la lengua y no un nombre propio. */
        boolean escritaEnMinuscula(String palabra) {
            return enMinuscula.getOrDefault(LogicaPreguntasAurea.normalizar(palabra), 0) >= 1;
        }

        /** Palabra de hasta tres letras que se repite en al menos el 10 % de los trozos: un artículo, una preposición o una conjunción. */
        boolean esFuncional(String palabra) {
            String normal = LogicaPreguntasAurea.normalizar(palabra);
            return normal.length() <= 3 && esPalabraDeLaLengua(palabra) && trozosConLaPalabra.getOrDefault(normal, 0) * 10 >= trozos;
        }
    }

    private static char primeraLetra(String palabra) {
        for (int i = 0; i < palabra.length(); i++) {
            if (Character.isLetter(palabra.charAt(i))) {
                return palabra.charAt(i);
            }
        }
        return '0';
    }

    private static int contarPalabras(String texto) {
        return texto.isBlank() ? 0 : texto.trim().split("\\s+").length;
    }

    /**
     * Las oraciones de un texto que sirven para armar una pregunta, en el orden del texto y sin repetir.
     *
     * @param limpio El texto, con un solo espacio entre palabras.
     * @param maximo Cuántas como máximo.
     */
    public static List<String> candidatas(String limpio, int maximo) {
        List<String> trozos = Arrays.asList(limpio.split("(?<=[.!?])\\s+"));
        Estadistica estadistica = new Estadistica(trozos);
        List<String> candidatas = new ArrayList<>();
        for (String trozo : trozos) {
            for (String oracion : oracionesDeUnTrozo(trozo, estadistica)) {
                if (esOracionValida(oracion) && !candidatas.contains(oracion)) {
                    candidatas.add(oracion);
                }
            }
            if (candidatas.size() >= maximo) {
                break;
            }
        }
        return candidatas;
    }

    /** Una oración candidata con la sección del documento en la que está, la etiqueta de ficha que la precede («Precondición», o vacío si no hay) y su página. */
    public record OracionDeSeccion(String texto, String seccion, String campo, int pagina) {
    }

    /**
     * Las oraciones que sirven para armar una pregunta, sacadas de los bloques que lee {@link EstructuraPdfUtil}. Como los títulos, las celdas y los ítems
     * ya vienen separados, una oración no se pega con el título ni con la celda de al lado; y cada una conserva la sección donde está. Las mismas
     * reglas de {@link #candidatas} deciden qué oración sirve.
     *
     * @param bloques Los bloques del PDF.
     * @param maximo Cuántas como máximo.
     */
    public static List<OracionDeSeccion> candidatasPorBloques(List<EstructuraPdfUtil.Bloque> bloques, int maximo) {
        List<String[]> trozos = new ArrayList<>();
        for (EstructuraPdfUtil.Bloque bloque : bloques) {
            if (bloque.tipo() == EstructuraPdfUtil.Tipo.PARRAFO) {
                for (String trozo : bloque.texto().split("(?<=[.!?])\\s+")) {
                    trozos.add(new String[] { trozo, bloque.seccion(), bloque.campo(), String.valueOf(bloque.pagina()) });
                }
            }
        }
        // Las estadísticas se calculan solo con los trozos de prosa: las celdas y los ítems cortos de las tablas diluirían la frecuencia de las palabras
        Estadistica estadistica = new Estadistica(trozos.stream().map(t -> t[0]).filter(t -> contarPalabras(t) >= PALABRAS_MINIMAS).toList());
        List<OracionDeSeccion> candidatas = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        for (String[] trozo : trozos) {
            for (String oracion : oracionesDeUnTrozo(trozo[0], estadistica)) {
                if (esOracionValida(oracion) && vistas.add(oracion)) {
                    candidatas.add(new OracionDeSeccion(oracion, trozo[1], trozo[2], Integer.parseInt(trozo[3])));
                }
            }
            if (candidatas.size() >= maximo) {
                break;
            }
        }
        return candidatas;
    }

    /**
     * Saca de un trozo de texto (lo que hay entre dos puntos finales) las oraciones que contiene, escritas como oración. El PDF suele dejar pegados
     * títulos, etiquetas de tabla y listas con viñetas; se separan con tres reglas, ninguna de las cuales conoce una palabra en particular:
     * <ul>
     * <li>Cada ítem de una lista con viñetas es una oración.</li>
     * <li>Una oración de más de 40 palabras con punto y coma se parte en cada punto y coma.</li>
     * <li>Lo que va antes de un cambio brusco, una palabra de la lengua escrita con mayúscula en medio de la frase sin puntuación antes («…Sistema
     * externo Un fondo reúne…»), es un título o una etiqueta pegada, y se descarta.</li>
     * </ul>
     */
    private static List<String> oracionesDeUnTrozo(String trozo, Estadistica estadistica) {
        List<String> oraciones = new ArrayList<>();
        for (String item : trozo.split(SEPARADOR_DE_VINETAS)) {
            if (item.isBlank()) {
                continue;
            }
            List<String> piezas = contarPalabras(item) > PALABRAS_MAXIMAS && item.contains(";") ? Arrays.asList(item.split("\\s*;\\s*")) : List.of(item);
            for (String pieza : piezas) {
                String oracion = comoOracion(cortarEnUnaComa(sinRestos(pieza, estadistica)));
                // Una oración que sigue a otra («Además, el inversor puede…», «Y la Administración…») pierde el conector o se descarta
                if (contarPalabras(oracion) >= PALABRAS_MINIMAS) {
                    oracion = GramaticaUtil.sinConectorInicial(oracion);
                }
                if (oracion != null) {
                    oraciones.add(oracion);
                }
            }
        }
        return oraciones;
    }

    /**
     * Una oración que no entra en una pregunta o en una opción (más de 40 palabras o de 255 caracteres) se corta en la última coma que deje lo anterior
     * dentro de esos límites: la primera parte de una oración larga suele ser una oración completa. Se corta solo si queda al menos de 10 palabras y sin
     * un paréntesis abierto. Si no hay una coma así, la oración queda como está y se descarta más adelante por larga.
     */
    private static String cortarEnUnaComa(String texto) {
        if (contarPalabras(texto) <= PALABRAS_MAXIMAS && texto.length() <= CARACTERES_MAXIMOS) {
            return texto;
        }
        String ventana = texto.substring(0, Math.min(texto.length(), CARACTERES_MAXIMOS));
        for (int coma = ventana.lastIndexOf(','); coma > 0; coma = ventana.lastIndexOf(',', coma - 1)) {
            String primera = texto.substring(0, coma);
            long abiertos = primera.chars().filter(c -> c == '(').count();
            long cerrados = primera.chars().filter(c -> c == ')').count();
            if (contarPalabras(primera) >= 10 && contarPalabras(primera) <= PALABRAS_MAXIMAS && abiertos == cerrados) {
                return primera;
            }
        }
        return texto;
    }

    /** Quita de una pieza de texto lo que no es la oración: numeración, códigos y números sueltos del principio, remisiones a otras secciones y el título pegado. */
    private static String sinRestos(String pieza, Estadistica estadistica) {
        String texto = NUMERACION.matcher(pieza.strip()).replaceFirst("");
        texto = PARENTESIS_CON_REFERENCIA.matcher(texto).replaceAll("");
        texto = TITULO_CON_SIGLA.matcher(texto).replaceFirst("");
        List<String> palabras = new ArrayList<>(Arrays.asList(texto.strip().split("\\s+")));
        // Códigos y números sueltos al principio («OB-08 …», «33 …»)
        while (palabras.size() > 1 && (CODIGO.matcher(palabras.get(0)).matches() || palabras.get(0).matches("\\d{1,3}"))) {
            palabras.remove(0);
        }
        palabras = sinEtiquetaAlPrincipio(palabras, estadistica);
        palabras = sinCadenaDeEtiquetas(palabras, estadistica);
        // El último cambio brusco: lo anterior es un título o una etiqueta
        int inicio = 0;
        for (int i = 1; i < palabras.size(); i++) {
            if (esInicioDeOracionPegada(palabras.get(i - 1), palabras.get(i), estadistica) && palabras.size() - i >= 5 && !dentroDeParentesis(palabras, i)) {
                inicio = i;
            }
        }
        palabras = sinCadenaDeEtiquetas(new ArrayList<>(palabras.subList(inicio, palabras.size())), estadistica);
        texto = String.join(" ", palabras);
        // Un número suelto al final es un resto de página o de tabla, no parte de la oración
        return texto.replaceFirst("\\s+\\d{1,3}\\s*([.!?])?$", "$1");
    }

    /**
     * Dentro de las primeras seis palabras, otra palabra con mayúscula que el propio texto también escribe en minúscula (una palabra de la lengua, no un
     * nombre) empieza la oración, y lo anterior es una etiqueta o un título pegado: «Quiénes participan Participan los emisores…», «Descripción Permite
     * generar…», «MODO Sistema externo Pasarela de pagos…». No corta después de una palabra funcional («el Alumno»), ni después de un punto, ni dentro de
     * un paréntesis, ni deja menos de cinco palabras. Se repite hasta que no corta más.
     */
    private static List<String> sinEtiquetaAlPrincipio(List<String> palabras, Estadistica estadistica) {
        boolean corto = true;
        while (corto) {
            corto = false;
            for (int i = 1; i <= 5 && i < palabras.size(); i++) {
                String palabra = palabras.get(i);
                String anterior = palabras.get(i - 1);
                if (Character.isUpperCase(primeraLetra(palabra)) && Character.isLetter(palabra.charAt(0)) && estadistica.escritaEnMinuscula(palabra)
                        && !estadistica.esFuncional(anterior) && !anterior.matches(".*[.;!?]$") && palabras.size() - i >= 5
                        && !dentroDeParentesis(palabras, i)) {
                    palabras = new ArrayList<>(palabras.subList(i, palabras.size()));
                    corto = true;
                    break;
                }
            }
        }
        return palabras;
    }

    /** Indica si la palabra de la posición dada está dentro de un paréntesis que todavía no se cerró. */
    private static boolean dentroDeParentesis(List<String> palabras, int posicion) {
        int abiertos = 0;
        for (int i = 0; i < posicion; i++) {
            abiertos += palabras.get(i).chars().filter(c -> c == '(').count() - palabras.get(i).chars().filter(c -> c == ')').count();
        }
        return abiertos > 0;
    }

    /**
     * Una cadena de dos o más palabras con mayúscula al principio, seguida de una en minúscula, son etiquetas de una tabla («Dominio Conjunto de
     * preguntas…», «Actores Docente Propósito Permite al…»): la oración empieza en la última, porque una oración común tiene una sola palabra con
     * mayúscula al empezar. No se corta si la primera palabra es funcional («El Docente…») ni si son dos palabras que el texto repite juntas («Idóneos
     * Online es…»), que son un nombre.
     */
    private static List<String> sinCadenaDeEtiquetas(List<String> palabras, Estadistica estadistica) {
        int cadena = 0;
        while (cadena < palabras.size() && Character.isUpperCase(primeraLetra(palabras.get(cadena)))) {
            cadena++;
        }
        if (cadena < 2 || cadena >= palabras.size() || estadistica.esFuncional(palabras.get(0))) {
            return palabras;
        }
        if (cadena == 2 && estadistica.esNombreRepetido(palabras.get(0), palabras.get(1))) {
            return palabras;
        }
        return new ArrayList<>(palabras.subList(cadena - 1, palabras.size()));
    }

    /**
     * Indica si una palabra con mayúscula, en medio de un trozo y sin puntuación antes, empieza una oración que quedó pegada a un título. Tiene que ser
     * una palabra de la lengua (el texto también la escribe en minúscula) y no puede venir después de una palabra funcional, que es lo que pasa con un
     * nombre compuesto («el Alumno», «y Obligaciones»). Si lo anterior termina en minúscula, alcanza con eso; si es otra palabra con mayúscula, tiene
     * que ser además una palabra funcional («Descripción El sistema…»), porque una secuencia de palabras con mayúscula suele ser un nombre.
     */
    private static boolean esInicioDeOracionPegada(String anterior, String palabra, Estadistica estadistica) {
        if (!Character.isUpperCase(primeraLetra(palabra)) || !Character.isLetter(palabra.charAt(0)) || anterior.matches(".*[.,;:!?)»”\"'\\]]$")) {
            return false;
        }
        if (!estadistica.esPalabraDeLaLengua(palabra) || estadistica.esFuncional(anterior)) {
            return false;
        }
        return Character.isLowerCase(primeraLetra(anterior)) || estadistica.esFuncional(palabra);
    }

    /** Escribe un trozo como oración: con mayúscula inicial y punto final. Devuelve vacío si termina en algo que la deja incompleta (dos puntos, coma, paréntesis abierto). */
    static String comoOracion(String texto) {
        String limpio = texto.strip().replaceAll("^[•●▪■□‣◦\\uE000-\\uF8FF\\-–—\\s]+", "").strip();
        if (limpio.isEmpty() || limpio.matches("(?s).*[:,;(]$")) {
            return "";
        }
        limpio = Character.toUpperCase(limpio.charAt(0)) + limpio.substring(1);
        return limpio.matches("(?s).*[.!?\"»”)]$") ? limpio : limpio + ".";
    }

    /**
     * Indica si un texto sirve como oración para una pregunta: de la estructura mira el largo, la puntuación, los símbolos, los códigos, las mayúsculas
     * y los números, y de la gramática, que tenga un verbo (un título o una celda de tabla no lo tiene).
     */
    public static boolean esOracionValida(String oracion) {
        if (oracion.isEmpty() || oracion.length() > CARACTERES_MAXIMOS) {
            return false;
        }
        String[] palabras = oracion.split("\\s+");
        if (palabras.length < PALABRAS_MINIMAS || palabras.length > PALABRAS_MAXIMAS) {
            return false;
        }
        if (oracion.contains("....") || oracion.matches("^\\d+[.)].*") || oracion.matches("(?s).*[:]\\s*[.]?$") || !oracion.matches("(?s).*[.!?\"»”)]$")
                || VINETA.matcher(oracion).find() || REFERENCIA_DE_SECCION.matcher(oracion).find() || tieneRestosDeTabla(oracion)) {
            return false;
        }
        long enMayuscula = Arrays.stream(palabras).filter(p -> Character.isUpperCase(primeraLetra(p))).count();
        long digitos = oracion.chars().filter(Character::isDigit).count();
        if (enMayuscula * 100 > palabras.length * 40L || digitos * 4 >= oracion.length()) {
            return false;
        }
        // Una oración tiene un verbo, según el etiquetador de OpenNLP. Un ítem en infinitivo («Gestionar pagos…») también cuenta: el etiquetador no ve
        // como verbo a un infinitivo que empieza con mayúscula
        return GramaticaUtil.tieneVerbo(oracion) || LogicaPreguntasAurea.esItemDeLista(oracion);
    }

    /**
     * Indica si una oración arrastra restos de una tabla: códigos como «AB-12», guiones sueltos, un encabezado con número al final, o cuatro palabras
     * con mayúscula seguidas (encabezados de columnas).
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
}
