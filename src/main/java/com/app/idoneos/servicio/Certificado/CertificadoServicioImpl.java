package com.app.idoneos.servicio.Certificado;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import com.app.idoneos.modelo.Alumno;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.CertificadoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Certificado}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CertificadoServicio}.
 */
@Service
public class CertificadoServicioImpl implements CertificadoServicio, CrudServicio<Certificado> {

    /** Longitud máxima del nombre (la de la columna). */
    private static final int LONGITUD_NOMBRE = 100;

    private static final Logger REGISTRO = LoggerFactory.getLogger(CertificadoServicioImpl.class);

    @Autowired
    private CertificadoRepositorio certificadoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el certificado por su identificador. Incluye los registros dados de baja o anulados, para poder consultarlos.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Certificado> buscarPorId(Integer id) {
        return certificadoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Solo devuelve certificados vigentes (no anulados).
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Certificado> obtenerTodo() {
        return certificadoRepositorio.findByAnuladoFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return certificadoRepositorio.existsById(id);
    }

    /**
     * Busca los registros vigentes de certificado asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Certificado asociados al Inscripcion indicado
     */
    @Override
    public List<Certificado> buscarPorInscripcion(Inscripcion inscripcion) {
        return certificadoRepositorio.findByInscripcionAndAnuladoFalse(inscripcion);
    }

    /**
     * Busca el certificado por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Certificado> buscarPorNumero(String numero) {
        return certificadoRepositorio.findByNumero(numero);
    }

    @Override
    public List<Certificado> buscarVigentesDelAlumno(Alumno alumno) {
        return certificadoRepositorio.findByInscripcionAlumnoAndAnuladoFalseOrderByFechaEmisionDesc(alumno);
    }

    @Override
    public Certificado buscarVisible(Integer idCertificado, Alumno solicitante) {
        return certificadoRepositorio.findById(idCertificado == null ? -1 : idCertificado)
                .filter(c -> !c.getAnulado())
                .filter(c -> solicitante == null
                        || c.getInscripcion().getAlumno().getIdAlumno() == solicitante.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! El certificado no se encuentra disponible."));
    }

    @Override
    public byte[] generarPdf(Certificado certificado) {
        Color azul = new Color(0x0B, 0x1B, 0x31);
        Color dorado = new Color(0xD4, 0xA0, 0x3D);
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "AR"));
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 72, 72, 60, 60);
        try {
            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            documento.open();

            // Marco doble de la hoja
            PdfContentByte lienzo = escritor.getDirectContentUnder();
            lienzo.setColorStroke(dorado);
            lienzo.setLineWidth(2.5f);
            lienzo.rectangle(28, 28, PageSize.A4.rotate().getWidth() - 56, PageSize.A4.rotate().getHeight() - 56);
            lienzo.stroke();
            lienzo.setLineWidth(0.8f);
            lienzo.rectangle(36, 36, PageSize.A4.rotate().getWidth() - 72, PageSize.A4.rotate().getHeight() - 72);
            lienzo.stroke();

            documento.add(centrado("Idóneos Online", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, azul), 6));
            documento.add(centrado("CERTIFICADO DE FINALIZACIÓN", FontFactory.getFont(FontFactory.HELVETICA, 13, dorado), 34));
            documento.add(centrado("Se certifica que", FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY), 10));
            documento.add(centrado(certificado.getNombreAlumno(), FontFactory.getFont(FontFactory.TIMES_BOLD, 30, azul), 4));
            documento.add(centrado("DNI " + certificado.getDniAlumno(), FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY), 22));
            documento.add(centrado("finalizó con éxito el curso", FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY), 10));
            documento.add(centrado(certificado.getInscripcion().getCurso().getNombre(), FontFactory.getFont(FontFactory.TIMES_BOLD, 22, azul), 4));
            documento.add(centrado(certificado.getInscripcion().getCohorte().getPrograma().getNombre(),
                    FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY), 34));
            documento.add(centrado("Emitido el " + certificado.getFechaEmision().format(formatoFecha),
                    FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK), 4));
            documento.add(centrado("N.º " + certificado.getNumero(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, azul), 0));
            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el certificado en PDF", e);
        }
        return salida.toByteArray();
    }

    private static Paragraph centrado(String texto, com.lowagie.text.Font fuente, float espacioDespues) {
        Paragraph parrafo = new Paragraph(texto, fuente);
        parrafo.setAlignment(Element.ALIGN_CENTER);
        parrafo.setSpacingAfter(espacioDespues);
        return parrafo;
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el certificado en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Certificado guardar(Certificado entidad) {
        return certificadoRepositorio.save(entidad);
    }

    /**
     * Emite el certificado de finalización de una inscripción. Si ya tiene uno vigente, lo devuelve.
     */
    @Override
    @Transactional
    public Certificado emitirCertificado(Inscripcion inscripcion) {
        Certificado vigente = inscripcion.getCertificadoVigente();
        if (vigente != null) {
            return vigente;
        }
        LocalDateTime ahora = LocalDateTime.now();
        long secuencia = certificadoRepositorio.count() + 1;
        String numero = String.format("CERT-%d-%06d", ahora.getYear(), secuencia);
        while (certificadoRepositorio.findByNumero(numero).isPresent()) {
            secuencia++;
            numero = String.format("CERT-%d-%06d", ahora.getYear(), secuencia);
        }
        var usuario = inscripcion.getAlumno().getUsuario();
        Certificado certificado = new Certificado(inscripcion, numero, ahora,
                "Certificado de finalización del curso " + inscripcion.getCurso().getNombre(), usuario.getNombreCompleto(),
                usuario.getDni());
        certificado = certificadoRepositorio.save(certificado);
        inscripcion.getCertificados().add(certificado);
        // El correo real llega con MOD-NF-01: por ahora el envío se registra en el log y el certificado queda sin enviar
        REGISTRO.info("Certificado {} emitido para {} (envío por correo simulado)", numero, usuario.getCorreo());
        return certificado;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Certificado modificar(Certificado entidad) {
        return certificadoRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public Certificado corregirDatos(Integer idCertificado, String nombre, String dni) {
        Certificado certificado = certificadoRepositorio.findById(idCertificado)
                .filter(c -> !c.estaAnulado() && !c.getInscripcion().esInactivo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error! El certificado no se encuentra vigente o su inscripción está dada de baja."));
        if (nombre == null || nombre.isBlank() || dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("Error! El nombre y el DNI del alumno son obligatorios.");
        }
        String nombreLimpio = nombre.trim();
        String dniLimpio = dni.trim();
        if (nombreLimpio.length() > LONGITUD_NOMBRE) {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre", MensajeErrorUtilidad.demasiadoLargo("El nombre", LONGITUD_NOMBRE, "caracteres"));
        }
        if (!dniLimpio.matches("\\d{7,8}")) {
            throw RespuestaFormularioUtilidad.campoInvalido("dni", MensajeErrorUtilidad.formatoInvalido("El DNI", "solo números, 7 u 8 dígitos"));
        }
        certificado.setNombreAlumno(nombreLimpio);
        certificado.setDniAlumno(dniLimpio);
        certificado.setUltimaModificacion(LocalDateTime.now());
        return certificadoRepositorio.save(certificado);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Anula el certificado, en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Certificado entidad) {
        entidad.anular(); // Anulación en lugar de borrado
        certificadoRepositorio.save(entidad);
    }

    /**
     * Anula los certificados vigentes de una inscripción.
     */
    @Override
    @Transactional
    public boolean anularCertificados(Inscripcion inscripcion) {
        List<Certificado> vigentes = certificadoRepositorio.findByInscripcionAndAnuladoFalse(inscripcion);
        for (Certificado certificado : vigentes) {
            certificado.anular();
            certificado.setUltimaModificacion(LocalDateTime.now());
            certificadoRepositorio.save(certificado);
        }
        return !vigentes.isEmpty();
    }
}
