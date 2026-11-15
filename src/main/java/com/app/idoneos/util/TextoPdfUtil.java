package com.app.idoneos.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/** Utilidad para leer el texto de un PDF. */
public final class TextoPdfUtil {

    /**
     * Texto leído de un PDF.
     *
     * @param texto El texto, en un solo párrafo.
     * @param paginasLeidas Las páginas de las que se leyó texto.
     * @param paginasTotales Las páginas del documento.
     * @param incompleto Si, aun habiendo recorrido el documento, quedó texto sin leer por el reparto.
     */
    public record Lectura(String texto, int paginasLeidas, int paginasTotales, boolean incompleto) {

        /** Lectura de las primeras páginas, que está incompleta si no llegó al final del documento. */
        public Lectura(String texto, int paginasLeidas, int paginasTotales) {
            this(texto, paginasLeidas, paginasTotales, false);
        }

        /** Indica si quedó texto del documento sin leer. */
        public boolean recortado() {
            return incompleto || paginasLeidas < paginasTotales;
        }
    }

    /**
     * Texto de una sección de primer nivel del índice.
     *
     * @param titulo El título de la sección.
     * @param texto Lo que se leyó de ella: su arranque y el de cada una de sus subsecciones, dentro del reparto.
     */
    public record Seccion(String titulo, String texto) {
    }

    /**
     * Texto leído de un PDF según su índice.
     *
     * @param secciones Las secciones de primer nivel, en orden, con su texto.
     * @param paginasTotales Las páginas del documento.
     * @param recortado Si alguna parte quedó sin leer entera.
     */
    public record LecturaPorIndice(List<Seccion> secciones, int paginasTotales, boolean recortado) {
    }

    /** Nodo del índice con sus subsecciones. */
    private static final class Nodo {
        private final int posicion;
        private final List<Nodo> hijos = new ArrayList<>();

        private Nodo(int posicion) {
            this.posicion = posicion;
        }
    }

    /** Cantidad máxima de secciones de primer nivel que se leen. */
    private static final int MAXIMO_SECCIONES = 5;

    /** Caracteres mínimos que le tocan a cada sección para que su texto sirva. */
    private static final int MINIMO_POR_SECCION = 1600;

    /** Secciones que no explican el tema y no se leen: glosario, bibliografía, anexos y similares. */
    private static final Pattern AUXILIAR = Pattern.compile(
            "(?iu)^(?:[\\d.\\s]*)(glosario|bibliograf[ií]a|referencias|anexos?|ap[eé]ndices?|[ií]ndice|agradecimientos|pr[oó]logo|dedicatoria).*$");

    private TextoPdfUtil() {
    }

    private static boolean esAuxiliar(String titulo) {
        return AUXILIAR.matcher(titulo.strip()).matches();
    }

    /**
     * Lee un PDF según su índice, repartiendo el presupuesto de caracteres en partes iguales entre las secciones del primer nivel y,
     * dentro de cada una, entre su arranque y sus subsecciones. Lo que una parte no usa pasa a las que siguen.
     *
     * @param pdf La ruta del archivo.
     * @param indice El índice, en orden.
     * @param presupuesto Los caracteres que se pueden leer en total.
     * @return El texto de cada sección, o {@code null} si el índice no sirve (vacío o con páginas fuera del documento).
     * @throws IOException Si el archivo no se puede abrir como PDF.
     */
    public static LecturaPorIndice leerPorIndice(Path pdf, List<MarcadoresPdfUtil.Marcador> indice, int presupuesto) throws IOException {
        List<MarcadoresPdfUtil.Marcador> entradas = indice.stream().filter(e -> e.titulo() != null && !e.titulo().isBlank() && e.pagina() >= 1)
                .toList();
        if (entradas.isEmpty() || presupuesto <= 0) {
            return null;
        }
        int base = entradas.stream().mapToInt(MarcadoresPdfUtil.Marcador::nivel).min().orElse(1);
        try (PDDocument documento = Loader.loadPDF(pdf.toFile())) {
            int totales = documento.getNumberOfPages();
            Lector lector = new Lector(documento, totales, entradas);
            List<Nodo> raices = new ArrayList<>();
            List<Nodo> pila = new ArrayList<>();
            for (int i = 0; i < entradas.size(); i++) {
                Nodo nodo = new Nodo(i);
                int nivel = entradas.get(i).nivel();
                while (!pila.isEmpty() && entradas.get(pila.get(pila.size() - 1).posicion).nivel() >= nivel) {
                    pila.remove(pila.size() - 1);
                }
                if (nivel <= base || pila.isEmpty()) {
                    raices.add(nodo);
                } else {
                    pila.get(pila.size() - 1).hijos.add(nodo);
                }
                pila.add(nodo);
            }
            // Se descartan las secciones auxiliares y se toman solo las primeras, para que cada una tenga un largo útil
            int maximoSecciones = Math.max(1, Math.min(MAXIMO_SECCIONES, presupuesto / MINIMO_POR_SECCION));
            List<Nodo> elegidas = raices.stream().filter(n -> !esAuxiliar(entradas.get(n.posicion).titulo())).limit(maximoSecciones).toList();
            if (elegidas.isEmpty()) {
                return null;
            }
            if (elegidas.size() < raices.size()) {
                lector.recortado = true;
            }
            raices = new ArrayList<>(elegidas);
            List<Seccion> secciones = new ArrayList<>();
            int restante = presupuesto;
            for (int i = 0; i < raices.size(); i++) {
                int parte = restante / (raices.size() - i);
                String texto = lector.leer(raices.get(i), parte);
                restante -= texto.length() + 1;
                if (texto.length() >= 40) {
                    secciones.add(new Seccion(entradas.get(raices.get(i).posicion).titulo().trim(), texto));
                }
            }
            return secciones.isEmpty() ? null : new LecturaPorIndice(secciones, totales, lector.recortado);
        }
    }

    /** Lee el texto de las páginas de cada entrada y arma el de un nodo con sus subsecciones. */
    private static final class Lector {
        private final PDDocument documento;
        private final int totales;
        private final List<MarcadoresPdfUtil.Marcador> entradas;
        private final String[] paginas;
        private final String encabezado;
        private boolean recortado;

        private Lector(PDDocument documento, int totales, List<MarcadoresPdfUtil.Marcador> entradas) throws IOException {
            this.documento = documento;
            this.totales = totales;
            this.entradas = entradas;
            this.paginas = new String[totales + 1];
            this.encabezado = detectarEncabezado(documento);
        }

        private String pagina(int numero) throws IOException {
            if (paginas[numero] == null) {
                PDFTextStripper lector = new PDFTextStripper();
                lector.setStartPage(numero);
                lector.setEndPage(numero);
                paginas[numero] = quitarEncabezado(normalizar(lector.getText(documento)), encabezado);
            }
            return paginas[numero];
        }

        /** El texto propio de una entrada: desde su título hasta el de la entrada que sigue. */
        private String propio(int posicion) throws IOException {
            MarcadoresPdfUtil.Marcador actual = entradas.get(posicion);
            if (actual.pagina() > totales) {
                return "";
            }
            MarcadoresPdfUtil.Marcador siguiente = posicion + 1 < entradas.size() ? entradas.get(posicion + 1) : null;
            int fin = totales;
            if (siguiente != null && siguiente.pagina() >= actual.pagina()) {
                fin = siguiente.pagina() > actual.pagina() ? siguiente.pagina() - 1 : actual.pagina();
            }
            StringBuilder texto = new StringBuilder();
            for (int numero = actual.pagina(); numero <= Math.min(fin, totales); numero++) {
                texto.append(pagina(numero)).append(' ');
            }
            String junto = texto.toString().trim();
            int desde = junto.toLowerCase(Locale.ROOT).indexOf(actual.titulo().trim().toLowerCase(Locale.ROOT));
            if (desde >= 0) {
                junto = junto.substring(desde + actual.titulo().trim().length()).trim();
            }
            if (siguiente != null && siguiente.pagina() == actual.pagina()) {
                int hasta = junto.toLowerCase(Locale.ROOT).indexOf(siguiente.titulo().trim().toLowerCase(Locale.ROOT));
                if (hasta > 0) {
                    junto = junto.substring(0, hasta).trim();
                }
            }
            return junto;
        }

        private String leer(Nodo nodo, int presupuesto) throws IOException {
            String propio = propio(nodo.posicion);
            if (nodo.hijos.isEmpty()) {
                return recortar(propio, presupuesto);
            }
            int partes = nodo.hijos.size() + (propio.length() >= 40 ? 1 : 0);
            int restante = presupuesto;
            StringBuilder texto = new StringBuilder();
            if (propio.length() >= 40) {
                String arranque = recortar(propio, restante / partes);
                texto.append(arranque);
                restante -= arranque.length() + 1;
                partes--;
            }
            for (Nodo hijo : nodo.hijos) {
                String subtexto = leer(hijo, Math.max(0, restante / Math.max(1, partes)));
                partes--;
                if (subtexto.length() >= 40) {
                    // Solo el texto: los títulos de las subsecciones no se copian, para que el modelo no los repita en lo que escribe
                    texto.append(texto.length() > 0 ? " " : "").append(subtexto);
                    restante -= subtexto.length() + 1;
                }
            }
            return texto.toString();
        }

        private String recortar(String texto, int maximo) {
            if (texto.length() > maximo) {
                recortado = true;
                return cortar(texto, Math.max(0, maximo));
            }
            return texto;
        }
    }

    /**
     * Lee el principio del texto de un PDF con texto reconocido, página por página, hasta juntar el largo indicado.
     *
     * @param pdf La ruta del archivo.
     * @param maximoCaracteres El largo máximo del texto que se devuelve. Lo que sigue no se lee.
     * @return El texto leído, con los espacios normalizados y las palabras cortadas por renglón unidas, y cuántas páginas cubre.
     * @throws IOException Si el archivo no se puede abrir como PDF.
     */
    public static Lectura leerInicio(Path pdf, int maximoCaracteres) throws IOException {
        try (PDDocument documento = Loader.loadPDF(pdf.toFile())) {
            int paginas = documento.getNumberOfPages();
            PDFTextStripper lector = new PDFTextStripper();
            StringBuilder texto = new StringBuilder();
            int leidas = 0;
            String encabezado = detectarEncabezado(documento);
            for (int pagina = 1; pagina <= paginas && texto.length() < maximoCaracteres; pagina++) {
                lector.setStartPage(pagina);
                lector.setEndPage(pagina);
                texto.append(quitarEncabezado(normalizar(lector.getText(documento)), encabezado)).append(' ');
                leidas++;
            }
            String limpio = texto.toString().trim();
            return new Lectura(limpio.length() <= maximoCaracteres ? limpio : cortar(limpio, maximoCaracteres), leidas, paginas);
        }
    }

    /**
     * Busca en qué página de un PDF está cada título, recorriendo las páginas en orden: cada título se busca desde la página donde se encontró
     * el anterior. Si no lo encuentra, queda en esa misma página.
     *
     * @param pdf La ruta del archivo.
     * @param titulos Los títulos, en el orden en que aparecen en el documento.
     * @return La página de cada título (la primera es la 1).
     * @throws IOException Si el archivo no se puede abrir como PDF.
     */
    public static int[] ubicarTitulos(Path pdf, List<String> titulos) throws IOException {
        try (PDDocument documento = Loader.loadPDF(pdf.toFile())) {
            int paginas = documento.getNumberOfPages();
            PDFTextStripper lector = new PDFTextStripper();
            String[] textos = new String[paginas + 1];
            int[] ubicadas = new int[titulos.size()];
            int actual = 1;
            for (int i = 0; i < titulos.size(); i++) {
                String buscado = titulos.get(i).replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
                for (int pagina = actual; pagina <= paginas; pagina++) {
                    if (textos[pagina] == null) {
                        lector.setStartPage(pagina);
                        lector.setEndPage(pagina);
                        textos[pagina] = lector.getText(documento).replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
                    }
                    if (textos[pagina].contains(buscado)) {
                        actual = pagina;
                        break;
                    }
                }
                ubicadas[i] = Math.max(1, Math.min(actual, Math.max(1, paginas)));
            }
            return ubicadas;
        }
    }

    /**
     * Detecta el encabezado que se repite al principio de las páginas (el autor, el título del documento, el número de página), que al extraer el
     * texto queda pegado a la primera oración de cada página. Compara el principio de las páginas 2 a 8, sin el número de página: lo que tienen
     * en común, si es largo y se repite, es el encabezado.
     *
     * @return El encabezado, o vacío si el documento no tiene uno que se repita.
     */
    private static String detectarEncabezado(PDDocument documento) throws IOException {
        int paginas = documento.getNumberOfPages();
        if (paginas < 4) {
            return "";
        }
        PDFTextStripper lector = new PDFTextStripper();
        List<String> muestras = new ArrayList<>();
        for (int pagina = 2; pagina <= Math.min(paginas, 8); pagina++) {
            lector.setStartPage(pagina);
            lector.setEndPage(pagina);
            muestras.add(normalizar(lector.getText(documento)).replaceFirst("^\\d+\\s+", ""));
        }
        Map<String, Integer> repeticiones = new HashMap<>();
        for (int i = 0; i < muestras.size(); i++) {
            for (int j = i + 1; j < muestras.size(); j++) {
                String comun = prefijoComun(muestras.get(i), muestras.get(j));
                if (comun.length() >= 25) {
                    repeticiones.merge(comun, 1, Integer::sum);
                }
            }
        }
        return repeticiones.entrySet().stream().filter(e -> e.getValue() >= 2)
                .max(java.util.Comparator.comparingInt((Map.Entry<String, Integer> e) -> e.getValue()).thenComparingInt(e -> e.getKey().length()))
                .map(Map.Entry::getKey).orElse("");
    }

    /** El principio que comparten dos textos, palabra por palabra. */
    private static String prefijoComun(String a, String b) {
        String[] x = a.split(" ");
        String[] y = b.split(" ");
        int n = 0;
        while (n < x.length && n < y.length && x[n].equals(y[n])) {
            n++;
        }
        return String.join(" ", java.util.Arrays.copyOf(x, n));
    }

    /** Quita de una página el número y el encabezado repetido que tiene al principio. */
    private static String quitarEncabezado(String pagina, String encabezado) {
        if (encabezado.isEmpty()) {
            return pagina;
        }
        String sinNumero = pagina.replaceFirst("^\\d+\\s+", "");
        return sinNumero.startsWith(encabezado) ? sinNumero.substring(encabezado.length()).strip() : pagina;
    }

    /** Corta un texto al largo indicado, en una palabra entera. */
    public static String cortar(String texto, int maximo) {
        if (texto.length() <= maximo) {
            return texto;
        }
        int corte = texto.lastIndexOf(' ', maximo);
        return (corte > maximo / 2 ? texto.substring(0, corte) : texto.substring(0, maximo)).trim();
    }

    /** Une las palabras cortadas por renglón y deja los espacios en uno solo. */
    private static String normalizar(String crudo) {
        return crudo.replace("\r", "")
                .replaceAll("(\\p{L})-\\n(\\p{L})", "$1$2")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
