package com.app.idoneos.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

/** Utilidad que arma el PDF de un resumen a partir del texto que redactó el modelo de inteligencia artificial. */
public final class ResumenPdfUtil {

    /** Resumen de un material, dividido en bloques con un subtítulo opcional y uno o más párrafos. */
    public record Resumen(String titulo, List<Bloque> bloques) {

        /** Parte de un resumen, como la introducción, el desarrollo o las conclusiones, con su nivel en el índice (de 1 a 4). */
        public record Bloque(String subtitulo, List<String> parrafos, int nivel) {

            /** Un bloque de primer nivel. */
            public Bloque(String subtitulo, List<String> parrafos) {
                this(subtitulo, parrafos, 1);
            }
        }
    }

    private ResumenPdfUtil() {
    }

    /**
     * Escribe el PDF de un resumen.
     *
     * @param tituloPorDefecto El título que se muestra si el resumen no trae uno propio.
     * @param resumen El contenido del resumen.
     * @param lecturas Cuánto de la bibliografía se usó, que se aclara al pie.
     * @param destino El archivo a escribir. Su carpeta debe existir.
     * @return La cantidad de páginas del PDF.
     * @throws IOException Si no se puede escribir el archivo.
     */
    public static int escribir(String tituloPorDefecto, Resumen resumen, String lecturas, Path destino) throws IOException {
        Font fuenteTitulo = new Font(Font.HELVETICA, 20, Font.BOLD, PdfUtil.AZUL);
        Font fuenteSubtitulo = new Font(Font.HELVETICA, 13, Font.BOLD, PdfUtil.AZUL);
        Font fuenteTexto = new Font(Font.HELVETICA, 11, Font.NORMAL, java.awt.Color.BLACK);
        Font fuenteNota = new Font(Font.HELVETICA, 8, Font.ITALIC, PdfUtil.GRIS);

        Document documento = new Document(PageSize.A4, 56, 56, 64, 64);
        try (OutputStream salida = Files.newOutputStream(destino)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            String titulo = limpiar(resumen.titulo());
            documento.add(parrafo(titulo.isEmpty() ? limpiar(tituloPorDefecto) : titulo, fuenteTitulo, 14, 0));
            for (Resumen.Bloque bloque : resumen.bloques() == null ? List.<Resumen.Bloque>of() : resumen.bloques()) {
                String subtitulo = limpiar(bloque.subtitulo());
                int nivel = Math.max(1, Math.min(4, bloque.nivel()));
                if (!subtitulo.isEmpty()) {
                    // Cada nivel más abajo se escribe un poco más chico y más a la derecha
                    Font fuenteDelNivel = nivel == 1 ? fuenteSubtitulo
                            : new Font(Font.HELVETICA, 13 - nivel, nivel == 4 ? Font.BOLDITALIC : Font.BOLD, PdfUtil.AZUL);
                    Paragraph titular = parrafo(subtitulo, fuenteDelNivel, 6, nivel == 1 ? 14 : 8);
                    titular.setIndentationLeft((nivel - 1) * 14f);
                    documento.add(titular);
                }
                for (String texto : bloque.parrafos() == null ? List.<String>of() : bloque.parrafos()) {
                    String limpio = limpiar(texto);
                    if (!limpio.isEmpty()) {
                        Paragraph parrafo = parrafo(limpio, fuenteTexto, 8, 0);
                        parrafo.setAlignment(Element.ALIGN_JUSTIFIED);
                        documento.add(parrafo);
                    }
                }
            }
            documento.add(parrafo("Resumen generado con inteligencia artificial a partir del material bibliográfico del tema. " + limpiar(lecturas),
                    fuenteNota, 0, 24));
            int paginas = documento.getPageNumber();
            documento.close();
            return Math.max(1, paginas);
        } catch (DocumentException e) {
            throw new IOException("No se pudo armar el PDF del resumen.", e);
        }
    }

    private static Paragraph parrafo(String texto, Font fuente, float despues, float antes) {
        Paragraph parrafo = new Paragraph(texto, fuente);
        parrafo.setSpacingAfter(despues);
        parrafo.setSpacingBefore(antes);
        return parrafo;
    }

    /** Quita el formato de Markdown y reemplaza lo que la tipografía del PDF no dibuja. */
    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace("**", "").replace("__", "").replace("`", "")
                .replace("→", "->").replace("≈", "~").replace("≥", ">=").replace("≤", "<=")
                .replace(" ", " ").trim();
    }
}
