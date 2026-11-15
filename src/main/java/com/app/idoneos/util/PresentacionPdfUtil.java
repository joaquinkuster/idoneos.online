package com.app.idoneos.util;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

/** Utilidad que arma el PDF de una presentación, una diapositiva por página, a partir de la estructura que devuelve el modelo. */
public final class PresentacionPdfUtil {

    private static final float ANCHO = 960;
    private static final float ALTO = 540;
    private static final float MARGEN = 60;
    private static final float ALTO_FRANJA = 100;

    /** Presentación de un tema, con el formato que se le pide al modelo. */
    public record Presentacion(String titulo, @JsonProperty(required = true) List<Diapositiva> diapositivas) {

        /** Diapositiva de una presentación, con su título y sus puntos. */
        public record Diapositiva(@JsonProperty(required = true) String titulo, @JsonProperty(required = true) List<String> puntos) {
        }
    }

    private PresentacionPdfUtil() {
    }

    /** Dibuja el fondo de cada página: toda azul en la portada y una franja arriba en las diapositivas. */
    private static final class Fondo extends PdfPageEventHelper {

        private BaseFont numeros;
        private boolean cierre;

        @Override
        public void onStartPage(PdfWriter escritor, Document documento) {
            boolean portada = escritor.getPageNumber() == 1 || cierre;
            PdfContentByte fondo = escritor.getDirectContentUnder();
            fondo.setColorFill(PdfUtil.AZUL);
            if (portada) {
                fondo.rectangle(0, 0, ANCHO, ALTO);
                fondo.fill();
                fondo.setColorFill(PdfUtil.DORADO);
                fondo.rectangle(MARGEN, 250, 120, 4);
            } else {
                fondo.rectangle(0, ALTO - ALTO_FRANJA, ANCHO, ALTO_FRANJA);
                fondo.fill();
                fondo.setColorFill(PdfUtil.DORADO);
                fondo.rectangle(0, ALTO - ALTO_FRANJA - 4, ANCHO, 4);
            }
            fondo.fill();
        }

        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            if (escritor.getPageNumber() == 1 || cierre) {
                return;
            }
            try {
                if (numeros == null) {
                    numeros = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
                }
            } catch (DocumentException | IOException e) {
                return;
            }
            PdfContentByte lienzo = escritor.getDirectContent();
            lienzo.beginText();
            lienzo.setFontAndSize(numeros, 11);
            lienzo.setColorFill(PdfUtil.GRIS);
            lienzo.showTextAligned(Element.ALIGN_RIGHT, String.valueOf(escritor.getPageNumber() - 1), ANCHO - MARGEN, 24, 0);
            lienzo.endText();
        }
    }

    /**
     * Escribe el PDF de una presentación.
     *
     * @param tituloPorDefecto El título de la portada si la presentación no trae uno propio.
     * @param presentacion El contenido de la presentación.
     * @param lecturas Cuánto de la bibliografía se usó, que se aclara en la portada.
     * @param destino El archivo a escribir. Su carpeta debe existir.
     * @return La cantidad de páginas del PDF, con la portada y la página final de agradecimiento.
     * @throws IOException Si no se puede escribir el archivo.
     */
    public static int escribir(String tituloPorDefecto, Presentacion presentacion, String lecturas, Path destino) throws IOException {
        Font fuenteTituloPortada = new Font(Font.HELVETICA, 38, Font.BOLD, Color.WHITE);
        Font fuenteTituloDiapositiva = new Font(Font.HELVETICA, 28, Font.BOLD, Color.WHITE);
        Font fuentePunto = new Font(Font.HELVETICA, 24, Font.NORMAL, Color.BLACK);
        Font fuenteNota = new Font(Font.HELVETICA, 10, Font.ITALIC, new Color(0xCB, 0xD5, 0xE1));

        Document documento = new Document(new Rectangle(ANCHO, ALTO), MARGEN, MARGEN, ALTO_FRANJA + 36, 48);
        Fondo fondo = new Fondo();
        try (OutputStream salida = Files.newOutputStream(destino)) {
            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            escritor.setPageEvent(fondo);
            documento.open();
            // La primera página se inicia con un elemento mínimo, para que el fondo ya esté dibujado antes de escribir la portada
            documento.add(new Paragraph(" ", new Font(Font.HELVETICA, 1)));

            // Portada: el contenido se escribe en posición fija sobre el fondo azul
            String titulo = limpiar(presentacion.titulo());
            PdfContentByte lienzo = escritor.getDirectContent();
            tituloSobreLaLinea(lienzo, titulo.isEmpty() ? limpiar(tituloPorDefecto) : titulo, fuenteTituloPortada);
            texto(lienzo, "Presentación generada con inteligencia artificial a partir de la bibliografía del tema. " + limpiar(lecturas),
                    fuenteNota, MARGEN, 40, ANCHO - MARGEN, 215, 14);

            for (Presentacion.Diapositiva diapositiva : presentacion.diapositivas() == null
                    ? List.<Presentacion.Diapositiva>of() : presentacion.diapositivas()) {
                documento.newPage();
                // El título se centra en la franja, según las líneas que ocupa
                String tituloDiapositiva = limpiar(diapositiva.titulo());
                int lineas = Math.min(3, contarLineas(tituloDiapositiva, fuenteTituloDiapositiva));
                texto(escritor.getDirectContent(), tituloDiapositiva, fuenteTituloDiapositiva, MARGEN, ALTO - ALTO_FRANJA - 40, ANCHO - MARGEN,
                        ALTO - 26 + 17 * (lineas - 1), 34);
                for (String punto : diapositiva.puntos() == null ? List.<String>of() : diapositiva.puntos()) {
                    String contenido = limpiar(punto);
                    if (contenido.isEmpty()) {
                        continue;
                    }
                    Paragraph item = new Paragraph();
                    item.setLeading(31);
                    item.add(new Chunk("•  ", fuentePunto));
                    item.add(new Chunk(contenido, fuentePunto));
                    item.setIndentationLeft(26);
                    item.setFirstLineIndent(-26);
                    item.setSpacingAfter(16);
                    documento.add(item);
                }
            }
            // Última página, fija y sin contenido del modelo: el cierre, con el mismo fondo azul de la portada
            fondo.cierre = true;
            documento.newPage();
            documento.add(new Paragraph(" ", new Font(Font.HELVETICA, 1)));
            tituloSobreLaLinea(escritor.getDirectContent(), "Muchas gracias por su tiempo", fuenteTituloPortada);
            int paginas = escritor.getPageNumber();
            documento.close();
            return Math.max(1, paginas);
        } catch (DocumentException e) {
            throw new IOException("No se pudo armar el PDF de la presentación.", e);
        }
    }

    /**
     * Escribe el título de la portada o del cierre pegado a la línea dorada que tiene debajo: la última línea del título queda
     * a una distancia fija de la raya, sin importar cuántas líneas ocupe.
     */
    private static void tituloSobreLaLinea(PdfContentByte lienzo, String titulo, Font fuente) throws DocumentException {
        int lineas = Math.min(4, contarLineas(titulo, fuente));
        texto(lienzo, titulo, fuente, MARGEN, 120, ANCHO - MARGEN, 332 + 46 * (lineas - 1), 46);
    }

    /** Cuenta las líneas que ocupa un texto en el ancho útil de la página, cortando por palabras como lo hace el PDF. */
    private static int contarLineas(String contenido, Font fuente) {
        float ancho = ANCHO - 2 * MARGEN;
        int lineas = 1;
        StringBuilder actual = new StringBuilder();
        for (String palabra : contenido.split("\\s+")) {
            String candidata = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (actual.length() > 0 && fuente.getCalculatedBaseFont(false).getWidthPoint(candidata, fuente.getSize()) > ancho) {
                lineas++;
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(candidata);
            }
        }
        return lineas;
    }

    private static void texto(PdfContentByte lienzo, String contenido, Font fuente, float izquierda, float abajo, float derecha, float arriba,
            float interlineado) throws DocumentException {
        ColumnText columna = new ColumnText(lienzo);
        columna.setSimpleColumn(new Phrase(contenido, fuente), izquierda, abajo, derecha, arriba, interlineado, Element.ALIGN_LEFT);
        columna.go();
    }

    /** Quita el formato de Markdown y reemplaza lo que la tipografía del PDF no dibuja. */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replaceFirst("^\\s*[-•*]\\s+", "").replace("**", "").replace("__", "").replace("`", "")
                .replace("→", "->").replace("≈", "~").replace("≥", ">=").replace("≤", "<=")
                .replace(" ", " ").trim();
    }
}
