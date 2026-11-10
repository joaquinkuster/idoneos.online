package com.app.idoneos.util;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

/** Utilidad de documentos PDF. Reúne los colores, las fuentes, el logo y el encabezado que comparten el certificado y el comprobante. */
public final class PdfUtil {

    /** Azul de la marca. */
    public static final Color AZUL = new Color(0x0F, 0x23, 0x3D);

    /** Dorado de la marca. */
    public static final Color DORADO = new Color(0xD4, 0xA0, 0x3D);

    /** Gris de los textos de apoyo. */
    public static final Color GRIS = new Color(0x64, 0x74, 0x8B);

    /** Gris claro de los bordes. */
    public static final Color BORDE = new Color(0xCB, 0xD5, 0xE1);

    /** Áreas de la empresa, que van bajo el nombre en el encabezado. */
    public static final String AREAS = "FINANZAS • ECONOMÍA • MERCADO DE CAPITALES";

    private PdfUtil() {
    }

    /**
     * Carga la fuente Helvetica.
     *
     * @param negrita si es la variante en negrita
     * @return la fuente lista para dibujar
     */
    public static BaseFont helvetica(boolean negrita) {
        try {
            return BaseFont.createFont(negrita ? BaseFont.HELVETICA_BOLD : BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("No se pudo cargar la fuente del documento", e);
        }
    }

    /**
     * Carga la fuente especial de los títulos del certificado (Cinzel), que viaja dentro de la aplicación.
     *
     * @return la fuente embebida en el PDF
     */
    public static BaseFont cinzel() {
        try (InputStream entrada = PdfUtil.class.getResourceAsStream("/fonts/Cinzel.ttf")) {
            if (entrada == null) {
                return helvetica(true);
            }
            return BaseFont.createFont("Cinzel.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, entrada.readAllBytes(), null);
        } catch (DocumentException | IOException e) {
            return helvetica(true);
        }
    }

    /**
     * Carga el logo de la empresa.
     *
     * @return la imagen del logo, o {@code null} si no se puede leer
     */
    public static Image logo() {
        try (InputStream entrada = PdfUtil.class.getResourceAsStream("/static/img/logos/image.png")) {
            return entrada == null ? null : Image.getInstance(entrada.readAllBytes());
        } catch (IOException | DocumentException e) {
            return null;
        }
    }

    /**
     * Dibuja el encabezado de la empresa: el logo, el nombre y las áreas.
     *
     * @param lienzo donde se dibuja
     * @param izquierda posición horizontal del borde izquierdo
     * @param base posición vertical de la base del logo
     * @return el ancho que ocupó el encabezado
     */
    public static float dibujarEncabezado(PdfContentByte lienzo, float izquierda, float base) {
        float alto = 46;
        float x = izquierda;
        Image logo = logo();
        if (logo != null) {
            logo.scaleToFit(alto * 1.4f, alto);
            logo.setAbsolutePosition(x, base);
            try {
                lienzo.addImage(logo);
            } catch (DocumentException e) {
                // el logo es decorativo: si falla, el encabezado sigue con el texto
            }
            x += logo.getScaledWidth() + 10;
        }
        BaseFont negrita = helvetica(true);
        BaseFont normal = helvetica(false);
        float anchoIdoneos = negrita.getWidthPoint("IDÓNEOS ", 15);
        lienzo.beginText();
        lienzo.setFontAndSize(negrita, 15);
        lienzo.setColorFill(AZUL);
        lienzo.setTextMatrix(x, base + alto - 18);
        lienzo.showText("IDÓNEOS ");
        lienzo.setColorFill(DORADO);
        lienzo.showText("ONLINE");
        lienzo.setFontAndSize(normal, 7);
        lienzo.setColorFill(GRIS);
        lienzo.setCharacterSpacing(0.8f);
        lienzo.setTextMatrix(x, base + alto - 32);
        lienzo.showText(AREAS);
        lienzo.setCharacterSpacing(0);
        lienzo.endText();
        float anchoAreas = normal.getWidthPoint(AREAS, 7) + 0.8f * AREAS.length();
        return x - izquierda + Math.max(anchoIdoneos + negrita.getWidthPoint("ONLINE", 15), anchoAreas);
    }

    /**
     * Calcula el ancho de un texto con separación entre letras.
     *
     * @param fuente la fuente
     * @param texto el texto
     * @param tamanio el tamaño de la fuente
     * @param separacion la separación entre letras
     * @return el ancho total
     */
    public static float ancho(BaseFont fuente, String texto, float tamanio, float separacion) {
        return fuente.getWidthPoint(texto, tamanio) + separacion * Math.max(0, texto.length() - 1);
    }

    /**
     * Dibuja un texto centrado, con separación opcional entre letras.
     *
     * @param lienzo donde se dibuja
     * @param fuente la fuente
     * @param tamanio el tamaño de la fuente
     * @param color el color del texto
     * @param texto el texto
     * @param centroX posición horizontal del centro
     * @param y posición vertical de la línea base
     * @param separacion la separación entre letras
     */
    public static void centrado(PdfContentByte lienzo, BaseFont fuente, float tamanio, Color color, String texto, float centroX,
            float y, float separacion) {
        lienzo.beginText();
        lienzo.setFontAndSize(fuente, tamanio);
        lienzo.setColorFill(color);
        lienzo.setCharacterSpacing(separacion);
        lienzo.setTextMatrix(centroX - ancho(fuente, texto, tamanio, separacion) / 2, y);
        lienzo.showText(texto);
        lienzo.setCharacterSpacing(0);
        lienzo.endText();
    }
}
