package com.app.idoneos.servicio.Comprobante;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.app.idoneos.modelo.Comprobante;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.repositorio.ComprobanteRepositorio;
import com.app.idoneos.servicio.CrudServicio;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Comprobante}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ComprobanteServicio}.
 */
@Service
public class ComprobanteServicioImpl implements ComprobanteServicio, CrudServicio<Comprobante> {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ComprobanteServicioImpl.class);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color AZUL = new Color(0x0B, 0x1B, 0x31);
    private static final Color DORADO = new Color(0xD4, 0xA0, 0x3D);

    @Autowired
    private ComprobanteRepositorio comprobanteRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el comprobante por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Comprobante> buscarPorId(Integer id) {
        return comprobanteRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Comprobante> obtenerTodo() {
        return comprobanteRepositorio.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return comprobanteRepositorio.existsById(id);
    }

    /**
     * Busca los registros de comprobante asociados a pago.
     *
     * @param pago el registro de Pago asociado
     * @return una lista de Comprobante asociados al Pago indicado
     */
    @Override
    public List<Comprobante> buscarPorPago(Pago pago) {
        return comprobanteRepositorio.findByPago(pago);
    }

    /**
     * Busca el comprobante por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Comprobante> buscarPorNumero(String numero) {
        return comprobanteRepositorio.findByNumero(numero);
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

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el comprobante en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Comprobante guardar(Comprobante entidad) {
        return comprobanteRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public Comprobante emitirComprobante(Pago pago, String nombrePagador, String dniPagador) {
        LocalDateTime ahora = LocalDateTime.now();
        Comprobante comprobante = comprobanteRepositorio.save(new Comprobante(pago,
                Comprobante.generarNumero(pago.getIdPago(), ahora), ahora,
                recortar(nombrePagador, Comprobante.LONGITUD_NOMBRE_PAGADOR),
                recortar(dniPagador, Comprobante.LONGITUD_DNI_PAGADOR)));
        pago.getComprobantes().add(comprobante);
        // El envío por correo electrónico es parte de MOD-NF-01: por ahora queda registrado y sin enviar
        REGISTRO.info("[CORREO SIMULADO] Comprobante {} para {} (se enviará cuando esté el correo electrónico)",
                comprobante.getNumero(), pago.getInscripcion().getAlumno().getUsuario().getCorreo());
        return comprobante;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Comprobante modificar(Comprobante entidad) {
        return comprobanteRepositorio.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Comprobante entidad) {
        comprobanteRepositorio.delete(entidad);
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
