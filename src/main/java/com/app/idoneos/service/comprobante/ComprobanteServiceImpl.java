package com.app.idoneos.service.comprobante;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Comprobante;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Descuento;
import com.app.idoneos.model.Pago;
import com.app.idoneos.model.Programa;
import com.app.idoneos.repository.ComprobanteRepository;
import com.app.idoneos.service.CrudService;
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
    private static final Color AZUL = new Color(0x0B, 0x1B, 0x31);
    private static final Color DORADO = new Color(0xD4, 0xA0, 0x3D);

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
            agregarFila(tabla, "Número", comprobante.getNumero());
            agregarFila(tabla, "Fecha de emisión", comprobante.getFechaEmision().format(FORMATO_FECHA));
            agregarFila(tabla, "Pagador", comprobante.getNombrePagador() + " (DNI " + comprobante.getDniPagador() + ")");
            agregarFila(tabla, "Alumno", pago.getInscripcion().getAlumno().getUsuario().getNombreCompleto());
            agregarFila(tabla, "Curso", pago.getInscripcion().getCurso().getNombre());
            agregarFila(tabla, "Programa", pago.getInscripcion().getCohorte().getPrograma().getNombre());
            if (pago.getDescuento() != null) {
                agregarFila(tabla, "Descuento aplicado", pago.getDescuento().getNombre() + " ("
                        + String.format(Locale.of("es", "AR"), "%.0f", pago.getDescuento().getPorcentaje()) + "%)");
            }
            agregarFila(tabla, "Monto", "$ " + String.format(Locale.of("es", "AR"), "%,.2f", pago.getMonto()));
            agregarFila(tabla, "Medio de pago", pago.getMedioDePago());
            if (pago.getCodigoReferencia() != null) {
                agregarFila(tabla, "Referencia", pago.getCodigoReferencia());
            }
            if (pago.getNumeroComprobante() != null) {
                agregarFila(tabla, "N.º de comprobante", pago.getNumeroComprobante());
            }
            if (pago.getFechaAprobacion() != null) {
                agregarFila(tabla, "Fecha de aprobación", pago.getFechaAprobacion().format(FORMATO_FECHA));
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

    private static void agregarFila(PdfPTable tabla, String etiqueta, String valor) {
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

    private static String recortar(String texto, int maximo) {
        return texto == null ? "" : (texto.length() > maximo ? texto.substring(0, maximo) : texto);
    }
}
