package com.app.idoneos.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Lee un PDF respetando su estructura visual: separa los títulos de los párrafos, las celdas de una tabla entre sí y los ítems de una lista, y le
 * asigna a cada bloque la sección (el último título) en la que está. No usa palabras ni constantes de un documento: todo se decide comparando cada línea
 * con el resto del propio PDF.
 * <ul>
 * <li><b>Cuerpo:</b> el tamaño de letra de los párrafos de varias líneas.</li>
 * <li><b>Título:</b> una o dos líneas cortas, sin punto final, con letra más grande que el cuerpo o en negrita.</li>
 * <li><b>Párrafo:</b> líneas seguidas, del mismo tamaño y estilo, con el interlineado normal.</li>
 * <li><b>Celda de tabla:</b> en una línea, texto separado por un hueco horizontal grande.</li>
 * <li><b>Encabezado y pie de página:</b> una línea que se repite en muchas páginas, y los números de página sueltos.</li>
 * </ul>
 */
public final class EstructuraPdfUtil {

    private EstructuraPdfUtil() {
    }

    /** Clase de un bloque de texto. */
    public enum Tipo { TITULO, PARRAFO }

    /**
     * Un bloque de texto del PDF.
     *
     * @param tipo Si es un título o un párrafo (un párrafo puede ser también una celda de tabla o un ítem de lista).
     * @param texto El texto, con un solo espacio entre palabras.
     * @param seccion El último título que apareció antes, o vacío si no hubo ninguno.
     * @param pagina La página donde empieza el bloque (la primera es la 1).
     * @param campo La última etiqueta de ficha que apareció antes, dentro de la misma sección («Precondición»), o vacío si no hubo ninguna. Una etiqueta de
     *        ficha es un texto corto, sin punto final, que se repite varias veces en el documento.
     */
    public record Bloque(Tipo tipo, String texto, String seccion, String campo, int pagina) {
    }

    /** Una etiqueta de ficha tiene como mucho esta cantidad de palabras. */
    private static final int PALABRAS_DE_UNA_ETIQUETA = 3;

    /** Una línea de texto del PDF con lo que se sabe de ella. */
    private record Linea(String texto, List<String> celdas, float tamano, boolean negrita, float y, int pagina) {
    }

    /** Un hueco horizontal mayor que esta cantidad de veces el tamaño de la letra separa dos celdas de una tabla. */
    private static final float HUECO_DE_CELDA = 1.0f;

    /** Una línea que está a más de esta cantidad de veces el tamaño de la letra de la anterior empieza otro párrafo. */
    private static final float INTERLINEADO_MAXIMO = 1.7f;

    /** Un texto que parece un título pero aparece tantas veces o más es una etiqueta de ficha o de tabla, no un título. */
    private static final int VECES_QUE_SE_REPITE_UNA_ETIQUETA = 3;

    private static final Pattern SOLO_NUMERO = Pattern.compile("^\\d{1,4}$");

    /** Una línea que empieza con una viñeta o con una numeración de lista («1. », «a) ») es un ítem nuevo y no sigue al párrafo anterior. */
    private static final Pattern VINETA_AL_PRINCIPIO = Pattern.compile("^(?:[•●▪■□‣◦·\\uE000-\\uF8FF]|\\d{1,3}[.)]\\s|[a-zA-Z][.)]\\s)");

    private static final Pattern SIGNO_FINAL = Pattern.compile(".*[.!?:;,]$");

    /** Termina una oración. */
    private static final Pattern PUNTO_FINAL = Pattern.compile(".*[.!?]$");

    /** Saca las líneas del PDF con su tamaño, su estilo, su posición y sus celdas. */
    private static final class Lector extends PDFTextStripper {
        private final List<Linea> lineas = new ArrayList<>();

        Lector() throws IOException {
            super();
        }

        @Override
        protected void writeString(String texto, List<TextPosition> posiciones) {
            List<TextPosition> visibles = posiciones.stream().filter(p -> !p.getUnicode().isBlank()).toList();
            if (visibles.isEmpty()) {
                return;
            }
            TextPosition primera = visibles.get(0);
            float tamano = primera.getFontSizeInPt();
            // Las celdas: el texto se corta donde hay un hueco horizontal grande entre dos letras seguidas
            List<String> celdas = new ArrayList<>();
            StringBuilder celda = new StringBuilder();
            TextPosition anterior = null;
            for (TextPosition p : posiciones) {
                if (anterior != null && p.getXDirAdj() - (anterior.getXDirAdj() + anterior.getWidthDirAdj()) > HUECO_DE_CELDA * tamano
                        && !celda.toString().isBlank()) {
                    celdas.add(celda.toString().replaceAll("\\s+", " ").strip());
                    celda.setLength(0);
                }
                celda.append(p.getUnicode());
                anterior = p;
            }
            if (!celda.toString().isBlank()) {
                celdas.add(celda.toString().replaceAll("\\s+", " ").strip());
            }
            lineas.add(new Linea(String.join(" ", celdas), celdas, Math.round(tamano * 10) / 10f, esNegrita(primera), primera.getYDirAdj(), getCurrentPageNo()));
        }

        private static boolean esNegrita(TextPosition posicion) {
            try {
                PDFontDescriptor descriptor = posicion.getFont().getFontDescriptor();
                return descriptor != null && (descriptor.isForceBold() || descriptor.getFontWeight() >= 700
                        || (descriptor.getFontName() != null && descriptor.getFontName().toLowerCase().contains("bold")));
            } catch (RuntimeException e) {
                return false;
            }
        }
    }

    /** Un grupo de líneas seguidas, del mismo tamaño y estilo y con el interlineado normal. */
    private static final class Racha {
        private final List<Linea> lineas = new ArrayList<>();

        String texto() {
            StringBuilder sb = new StringBuilder();
            for (Linea l : lineas) {
                sb.append(sb.length() == 0 ? "" : " ").append(l.texto());
            }
            return sb.toString().replaceAll("\\s+", " ").strip();
        }

        Linea primera() {
            return lineas.get(0);
        }
    }

    /**
     * Lee un PDF y devuelve sus bloques de texto, en orden.
     *
     * @param pdf El archivo.
     * @param maximoDeCaracteres Cuánto texto leer como máximo.
     * @return Los bloques, o una lista vacía si el PDF no tiene texto.
     */
    public static List<Bloque> leer(Path pdf, int maximoDeCaracteres) throws IOException {
        List<Linea> lineas;
        int paginas;
        try (PDDocument documento = Loader.loadPDF(pdf.toFile())) {
            Lector lector = new Lector();
            lector.getText(documento);
            lineas = lector.lineas;
            paginas = documento.getNumberOfPages();
        }
        List<Racha> rachas = agrupar(lineas);
        float cuerpo = tamanoDelCuerpo(rachas);
        Set<String> repetidas = lineasQueSeRepiten(rachas, paginas);
        // Un título de verdad aparece una vez: lo que parece un título pero se repite varias veces es la etiqueta de una ficha o de una tabla
        Map<String, Integer> vecesQueAparece = new HashMap<>();
        for (Racha racha : rachas) {
            if (racha.primera().celdas().size() < 2 && esTitulo(racha, cuerpo)) {
                vecesQueAparece.merge(clave(racha.texto()), 1, Integer::sum);
            }
        }
        // Las etiquetas de una ficha («Actores», «Precondición») son textos cortos sin punto final que se repiten en todas las fichas
        Map<String, Integer> vecesQueSeRepiteUnTextoCorto = new HashMap<>();
        for (Racha racha : rachas) {
            if (racha.primera().celdas().size() < 2 && esEtiqueta(racha.texto())) {
                vecesQueSeRepiteUnTextoCorto.merge(clave(racha.texto()), 1, Integer::sum);
            }
        }
        List<Bloque> bloques = new ArrayList<>();
        String seccion = "";
        String campo = "";
        int caracteres = 0;
        for (Racha racha : rachas) {
            String texto = racha.texto();
            if (texto.isBlank() || SOLO_NUMERO.matcher(texto).matches() || repetidas.contains(clave(texto))) {
                continue;
            }
            Linea primera = racha.primera();
            if (primera.celdas().size() >= 2) {
                // Una fila de tabla: cada celda es un bloque aparte
                for (String celda : primera.celdas()) {
                    bloques.add(new Bloque(Tipo.PARRAFO, celda, seccion, campo, primera.pagina()));
                }
            } else if (esTitulo(racha, cuerpo) && vecesQueAparece.getOrDefault(clave(texto), 0) < VECES_QUE_SE_REPITE_UNA_ETIQUETA) {
                seccion = texto;
                campo = "";
                bloques.add(new Bloque(Tipo.TITULO, texto, seccion, campo, primera.pagina()));
            } else if (esEtiqueta(texto) && vecesQueSeRepiteUnTextoCorto.getOrDefault(clave(texto), 0) >= VECES_QUE_SE_REPITE_UNA_ETIQUETA) {
                campo = texto;
                bloques.add(new Bloque(Tipo.PARRAFO, texto, seccion, "", primera.pagina()));
            } else {
                bloques.add(new Bloque(Tipo.PARRAFO, texto, seccion, campo, primera.pagina()));
            }
            caracteres += texto.length();
            if (caracteres >= maximoDeCaracteres) {
                break;
            }
        }
        return bloques;
    }

    /** Un texto corto sin punto final ni viñeta: puede ser la etiqueta de una ficha. */
    private static boolean esEtiqueta(String texto) {
        return !texto.isBlank() && texto.split("\\s+").length <= PALABRAS_DE_UNA_ETIQUETA && !PUNTO_FINAL.matcher(texto).matches()
                && !VINETA_AL_PRINCIPIO.matcher(texto).find();
    }

    private static List<Racha> agrupar(List<Linea> lineas) {
        List<Racha> rachas = new ArrayList<>();
        Racha actual = null;
        Linea anterior = null;
        for (Linea linea : lineas) {
            // Un cambio de negrita corta el párrafo, salvo que la línea anterior sea larga y no haya terminado una oración: es una etiqueta en negrita
            // («Nota: …») que sigue en la línea de abajo con letra normal
            boolean mismoEstilo = anterior != null && (linea.negrita() == anterior.negrita()
                    || (anterior.texto().split("\\s+").length >= 8 && !PUNTO_FINAL.matcher(anterior.texto()).matches()));
            boolean continua = actual != null && anterior != null && linea.celdas().size() < 2 && anterior.celdas().size() < 2
                    && linea.pagina() == anterior.pagina() && mismoEstilo && Math.abs(linea.tamano() - anterior.tamano()) < 0.5f
                    && linea.y() > anterior.y() && linea.y() - anterior.y() <= INTERLINEADO_MAXIMO * linea.tamano()
                    && !VINETA_AL_PRINCIPIO.matcher(linea.texto()).find();
            if (!continua) {
                actual = new Racha();
                rachas.add(actual);
            }
            actual.lineas.add(linea);
            anterior = linea;
        }
        return rachas;
    }

    /** El tamaño de letra del cuerpo: el que más caracteres tiene en los párrafos de tres líneas o más. Si no hay ninguno, el más frecuente en general. */
    private static float tamanoDelCuerpo(List<Racha> rachas) {
        Map<Float, Integer> porTamano = new HashMap<>();
        Map<Float, Integer> general = new HashMap<>();
        for (Racha r : rachas) {
            general.merge(r.primera().tamano(), r.texto().length(), Integer::sum);
            if (r.lineas.size() >= 3) {
                porTamano.merge(r.primera().tamano(), r.texto().length(), Integer::sum);
            }
        }
        Map<Float, Integer> usar = porTamano.isEmpty() ? general : porTamano;
        return usar.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(12f);
    }

    /**
     * Un título es una racha de una o dos líneas, corta, sin signo de puntuación al final, que se distingue del cuerpo: letra más grande o en negrita.
     */
    private static boolean esTitulo(Racha racha, float cuerpo) {
        String texto = racha.texto();
        int palabras = texto.split("\\s+").length;
        Linea l = racha.primera();
        boolean destaca = l.tamano() >= cuerpo * 1.1f || (l.negrita() && l.tamano() >= cuerpo * 0.95f);
        return racha.lineas.size() <= 2 && palabras <= 14 && !SIGNO_FINAL.matcher(texto).matches() && destaca;
    }

    /** Las líneas (sin dígitos) que aparecen en al menos tres páginas y en el 30 % de ellas: encabezados y pies de página. */
    private static Set<String> lineasQueSeRepiten(List<Racha> rachas, int paginas) {
        Map<String, Set<Integer>> paginasPorLinea = new HashMap<>();
        for (Racha r : rachas) {
            if (r.lineas.size() == 1) {
                paginasPorLinea.computeIfAbsent(clave(r.texto()), k -> new HashSet<>()).add(r.primera().pagina());
            }
        }
        Set<String> repetidas = new HashSet<>();
        paginasPorLinea.forEach((clave, ps) -> {
            if (!clave.isBlank() && ps.size() >= 3 && ps.size() * 10 >= paginas * 3) {
                repetidas.add(clave);
            }
        });
        return repetidas;
    }

    private static String clave(String texto) {
        return texto.replaceAll("\\d+", "").replaceAll("\\s+", " ").strip().toLowerCase(java.util.Locale.ROOT);
    }
}
