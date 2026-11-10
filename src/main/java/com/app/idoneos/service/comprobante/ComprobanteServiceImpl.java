package com.app.idoneos.service.comprobante;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Comprobante;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Pago;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.repository.ComprobanteRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.util.PdfUtil;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de ComprobanteService. */
@Service
public class ComprobanteServiceImpl implements ComprobanteService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ComprobanteServiceImpl.class);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el comprobante por su identificador. */
    @Override
    public Optional<Comprobante> buscarPorId(Integer id) {
        return comprobanteRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Comprobante> obtenerTodo() {
        return comprobanteRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return comprobanteRepository.existsById(id);
    }

    /** Busca el comprobante por su atributo único 'numero'. */
    @Override
    public Optional<Comprobante> buscarPorNumero(String numero) {
        return comprobanteRepository.findByNumero(numero);
    }

    @Override
    public byte[] generarPdf(Comprobante comprobante) {
        Pago pago = comprobante.getPago();
        Rectangle pagina = PageSize.A4;
        float ancho = pagina.getWidth();
        float alto = pagina.getHeight();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(pagina, 0, 0, 0, 0);
        try {
            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            PaginacionDelPie pie = new PaginacionDelPie();
            escritor.setPageEvent(pie);
            documento.open();
            PdfContentByte lienzo = escritor.getDirectContent();
            BaseFont negrita = PdfUtil.helvetica(true);
            BaseFont normal = PdfUtil.helvetica(false);

            // Encabezado: logo, nombre y áreas, y el distintivo gris rectangular
            PdfUtil.dibujarEncabezado(lienzo, 48, alto - 94);
            String distintivo = "COMPROBANTE";
            float anchoDistintivo = PdfUtil.ancho(negrita, distintivo, 9, 0.8f) + 24;
            lienzo.setColorFill(new Color(0xF1, 0xF5, 0xF9));
            lienzo.setColorStroke(PdfUtil.BORDE);
            lienzo.setLineWidth(0.8f);
            lienzo.rectangle(ancho - 48 - anchoDistintivo, alto - 76, anchoDistintivo, 24);
            lienzo.fillStroke();
            PdfUtil.centrado(lienzo, negrita, 9, new Color(0x47, 0x55, 0x69), distintivo, ancho - 48 - anchoDistintivo / 2, alto - 68, 0.8f);

            // Bajo el logo: el número de comprobante y la fecha de emisión, en ese orden
            lineaDelEncabezado(lienzo, alto - 114, "N.\u00ba de comprobante: ", comprobante.getNumero());
            lineaDelEncabezado(lienzo, alto - 136, "Fecha de emisi\u00f3n: ", comprobante.getFechaEmision().format(FORMATO_FECHA));
            lienzo.setColorStroke(PdfUtil.DORADO);
            lienzo.setLineWidth(2f);
            lienzo.moveTo(48, alto - 152);
            lienzo.lineTo(ancho - 48, alto - 152);
            lienzo.stroke();

            // Datos en filas horizontales: etiqueta a la izquierda y valor a su lado
            Inscripcion inscripcion = pago.getInscripcion();
            float y = alto - 190;
            y = fila(lienzo, y, "Curso", inscripcion.getCurso().getNombre() + " (Cohorte " + inscripcion.getCohorte().getMesAnio() + ")");
            y = fila(lienzo, y, "Monto", "$ " + String.format(Locale.of("es", "AR"), "%,.2f", pago.getMonto()) + " ARS");
            y -= 16;
            lienzo.beginText();
            lienzo.setFontAndSize(negrita, 9);
            lienzo.setColorFill(PdfUtil.GRIS);
            lienzo.setCharacterSpacing(0.8f);
            lienzo.setTextMatrix(48, y);
            lienzo.showText("DATOS DEL PAGADOR");
            lienzo.setCharacterSpacing(0);
            lienzo.endText();
            y -= 34;
            y = fila(lienzo, y, "Apellido y nombre", comprobante.getNombrePagador());
            y = fila(lienzo, y, "D.N.I. / CUIT", comprobante.getDniPagador());
            fila(lienzo, y, "Medio de pago", pago.getMedioDePago());

            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el comprobante en PDF", e);
        }
        return salida.toByteArray();
    }

    /** Dibuja una fila con la etiqueta a la izquierda y el valor a su lado, y devuelve la posición de la fila siguiente. */
    /** Dibuja una línea del encabezado con la etiqueta en gris y el valor en negrita. */
    private static void lineaDelEncabezado(PdfContentByte lienzo, float y, String etiqueta, String valor) {
        BaseFont negrita = PdfUtil.helvetica(true);
        BaseFont normal = PdfUtil.helvetica(false);
        lienzo.beginText();
        lienzo.setFontAndSize(normal, 10);
        lienzo.setColorFill(PdfUtil.GRIS);
        lienzo.setTextMatrix(48, y);
        lienzo.showText(etiqueta);
        lienzo.setFontAndSize(negrita, 10);
        lienzo.setColorFill(PdfUtil.AZUL);
        lienzo.showText(valor);
        lienzo.endText();
    }

    /** Dibuja una fila con la etiqueta a la izquierda y el valor a su lado, y devuelve la posición de la fila siguiente. */
    private static float fila(PdfContentByte lienzo, float y, String etiqueta, String valor) throws DocumentException {
        BaseFont negrita = PdfUtil.helvetica(true);
        BaseFont normal = PdfUtil.helvetica(false);
        float tamanio = 11;
        lienzo.beginText();
        lienzo.setFontAndSize(normal, 10);
        lienzo.setColorFill(PdfUtil.GRIS);
        lienzo.setTextMatrix(48, y);
        lienzo.showText(etiqueta + ":");
        lienzo.endText();
        ColumnText texto = new ColumnText(lienzo);
        Font fuente = new Font(negrita, tamanio, Font.NORMAL, PdfUtil.AZUL);
        texto.setSimpleColumn(new Phrase(valor, fuente), 190, y - 30, 547, y + tamanio, tamanio + 3, Element.ALIGN_LEFT);
        texto.go();
        int lineas = Math.max(1, (int) Math.ceil(negrita.getWidthPoint(valor, tamanio) / 357));
        float siguiente = y - 14 - (lineas - 1) * (tamanio + 3);
        lienzo.setColorStroke(new Color(0xE2, 0xE8, 0xF0));
        lienzo.setLineWidth(0.6f);
        lienzo.moveTo(48, siguiente + 2);
        lienzo.lineTo(547, siguiente + 2);
        lienzo.stroke();
        return siguiente - 22;
    }

    /** Dibuja en el pie de cada página los datos de la empresa y la paginación, por ejemplo Página 1 de 1. */
    private static final class PaginacionDelPie extends PdfPageEventHelper {

        private PdfTemplate total;

        @Override
        public void onOpenDocument(PdfWriter escritor, Document documento) {
            total = escritor.getDirectContent().createTemplate(40, 12);
        }

        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            PdfContentByte lienzo = escritor.getDirectContent();
            BaseFont normal = PdfUtil.helvetica(false);
            float ancho = documento.getPageSize().getWidth();
            lienzo.setColorStroke(new Color(0xE2, 0xE8, 0xF0));
            lienzo.setLineWidth(0.6f);
            lienzo.moveTo(48, 62);
            lienzo.lineTo(ancho - 48, 62);
            lienzo.stroke();
            lienzo.beginText();
            lienzo.setFontAndSize(normal, 8);
            lienzo.setColorFill(PdfUtil.GRIS);
            lienzo.setTextMatrix(48, 48);
            lienzo.showText("Idóneos Online S.A.S. \u00b7 CUIT 30-71884920-4");
            String texto = "Página " + escritor.getPageNumber() + " de ";
            float anchoTexto = normal.getWidthPoint(texto, 8);
            lienzo.setTextMatrix(ancho - 48 - anchoTexto - 6, 48);
            lienzo.showText(texto);
            lienzo.endText();
            lienzo.addTemplate(total, ancho - 48 - 6, 48);
        }

        @Override
        public void onCloseDocument(PdfWriter escritor, Document documento) {
            total.beginText();
            total.setFontAndSize(PdfUtil.helvetica(false), 8);
            total.setColorFill(PdfUtil.GRIS);
            total.setTextMatrix(0, 0);
            total.showText(String.valueOf(escritor.getPageNumber() - 1));
            total.endText();
        }
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el comprobante en la base de datos. */
    @Override
    public Comprobante guardar(Comprobante entidad) {
        return comprobanteRepository.save(entidad);
    }

    @Override
    @Transactional
    public Comprobante emitir(Pago pago, String nombrePagador, String dniPagador) {
        LocalDateTime ahora = LocalDateTime.now();
        Comprobante comprobante = comprobanteRepository.save(new Comprobante(pago,
                Comprobante.generarNumero(pago.getIdPago(), ahora), ahora,
                recortar(nombrePagador, Comprobante.LONGITUD_NOMBRE_PAGADOR),
                recortar(dniPagador, Comprobante.LONGITUD_DNI_PAGADOR)));
        pago.getComprobantes().add(comprobante);
        // El envío por correo electrónico es parte de MOD-NF-01: por ahora queda registrado y sin enviar
        REGISTRO.info("[CORREO SIMULADO] Comprobante {} para {} (se enviará cuando esté el correo electrónico)",
                comprobante.getNumero(), pago.getInscripcion().getAlumno().getUsuario().getCorreo());
        return comprobante;
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Comprobante modificar(Comprobante entidad) {
        return comprobanteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Comprobante entidad) {
        comprobanteRepository.delete(entidad);
    }

    private static String recortar(String texto, int maximo) {
        return texto == null ? "" : (texto.length() > maximo ? texto.substring(0, maximo) : texto);
    }
}
