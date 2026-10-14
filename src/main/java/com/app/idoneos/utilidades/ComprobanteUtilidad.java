package com.app.idoneos.utilidades;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.app.idoneos.modelo.Comprobante;
import com.app.idoneos.modelo.Pago;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Genera el archivo PDF de un comprobante de pago, para que el alumno o el administrador lo descarguen.
 */
public class ComprobanteUtilidad {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color AZUL = new Color(0x0B, 0x1B, 0x31);
    private static final Color DORADO = new Color(0xD4, 0xA0, 0x3D);

    private ComprobanteUtilidad() {
    }

    /**
     * Arma el PDF de un comprobante.
     *
     * @param comprobante El comprobante emitido por un pago aprobado.
     * @return El contenido del archivo PDF.
     */
    public static byte[] generarPdf(Comprobante comprobante) {
        Pago pago = comprobante.getPago();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 56, 56, 56, 56);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Paragraph marca = new Paragraph("Idóneos Online", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, AZUL));
            marca.setSpacingAfter(2);
            documento.add(marca);
            Paragraph titulo = new Paragraph("Comprobante de pago", FontFactory.getFont(FontFactory.HELVETICA, 13, DORADO));
            titulo.setSpacingAfter(18);
            documento.add(titulo);

            PdfPTable tabla = new PdfPTable(new float[] { 36, 64 });
            tabla.setWidthPercentage(100);
            fila(tabla, "Número", comprobante.getNumero());
            fila(tabla, "Fecha de emisión", comprobante.getFechaEmision().format(FORMATO_FECHA));
            fila(tabla, "Pagador", comprobante.getNombrePagador() + " (DNI " + comprobante.getDniPagador() + ")");
            fila(tabla, "Alumno", pago.getInscripcion().getAlumno().getUsuario().getNombreCompleto());
            fila(tabla, "Curso", pago.getInscripcion().getCurso().getNombre());
            fila(tabla, "Programa", pago.getInscripcion().getCohorte().getPrograma().getNombre());
            if (pago.getDescuento() != null) {
                fila(tabla, "Descuento aplicado", pago.getDescuento().getNombre() + " ("
                        + String.format(Locale.of("es", "AR"), "%.0f", pago.getDescuento().getPorcentaje()) + "%)");
            }
            fila(tabla, "Monto", "$ " + String.format(Locale.of("es", "AR"), "%,.2f", pago.getMonto()));
            fila(tabla, "Medio de pago", pago.getMedioDePago());
            if (pago.getCodigoReferencia() != null) {
                fila(tabla, "Referencia", pago.getCodigoReferencia());
            }
            if (pago.getFechaAprobacion() != null) {
                fila(tabla, "Fecha de aprobación", pago.getFechaAprobacion().format(FORMATO_FECHA));
            }
            documento.add(tabla);

            Paragraph pie = new Paragraph("Este comprobante acredita el pago de la inscripción. Las bajas de inscripción no generan reembolso.",
                    FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY));
            pie.setSpacingBefore(24);
            documento.add(pie);
            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el comprobante en PDF", e);
        }
        return salida.toByteArray();
    }

    private static void fila(PdfPTable tabla, String etiqueta, String valor) {
        tabla.addCell(celda(etiqueta, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, AZUL)));
        tabla.addCell(celda(valor, FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK)));
    }

    private static PdfPCell celda(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(new Color(0xDD, 0xDD, 0xDD));
        celda.setPadding(7);
        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
        return celda;
    }
}
