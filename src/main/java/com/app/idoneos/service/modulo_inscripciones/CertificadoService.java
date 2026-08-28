package com.app.idoneos.service.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TRAZABILIDAD — Servicio para la emision y verificacion de Certificados Digitales Academicos.
 *
 * MOD-F-03: Modulo de Inscripciones y Pagos
 *   CU-43 — Buscar inscripcion: incluye descarga del certificado digital.
 *   CU-63 — Realizar intento: emision del diploma digital al aprobar la ultima unidad.
 */
@Service
@Transactional
public class CertificadoService {

    private static final Logger log = LoggerFactory.getLogger(CertificadoService.class);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String FORMATO_NUMERO = "CERT-%d-%06d";

    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired @Lazy private EmailService emailService;

    @Value("${idoneos.app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * CU-63 paso 10 / CU-91 — Emite el certificado digital de aprobacion para la inscripcion.
     * Si el certificado ya fue emitido, no lo regenera.
     * Regla de negocio: Numero de certificado correlativo e infalsificable (CERT-YYYY-000000).
     */
    public Inscripcion emitirCertificado(Inscripcion inscripcion) {
        if (inscripcion == null) throw new ExcepcionValidacion("La inscripcion no puede ser nula.");

        // Idempotente: si ya tiene numero no regenerar
        if (inscripcion.getNumeroCertificado() != null && !inscripcion.getNumeroCertificado().isBlank()) {
            log.info("CertificadoService: certificado ya emitido para inscripcion #{}: {}", inscripcion.getId(), inscripcion.getNumeroCertificado());
            return inscripcion;
        }

        // Generar numero de certificado
        String numero = String.format(FORMATO_NUMERO, LocalDate.now().getYear(), inscripcion.getId());
        inscripcion.setNumeroCertificado(numero);
        inscripcion.setFechaEmisionCertificado(LocalDateTime.now());

        // Capturar datos del alumno en el momento de la emision
        if (inscripcion.getAlumno() != null && inscripcion.getAlumno().getUsuario() != null) {
            Usuario usuario = inscripcion.getAlumno().getUsuario();
            inscripcion.setNombreAlumno(usuario.getNombre() + " " + usuario.getApellido());
            inscripcion.setDniAlumno(usuario.getDni());
        }

        // Generar texto del certificado
        String nombreCurso = inscripcion.getCohorte() != null
                && inscripcion.getCohorte().getPrograma() != null
                && inscripcion.getCohorte().getPrograma().getCurso() != null
                ? inscripcion.getCohorte().getPrograma().getCurso().getNombre()
                : "Curso";

        inscripcion.setTextoCertificado(
                "Por medio del presente certificado, Idoneos Online acredita que el/la alumno/a "
                + (inscripcion.getNombreAlumno() != null ? inscripcion.getNombreAlumno() : "")
                + (inscripcion.getDniAlumno() != null ? " (DNI: " + inscripcion.getDniAlumno() + ")" : "")
                + " ha completado satisfactoriamente el curso de \"" + nombreCurso + "\""
                + ", habiendo aprobado la totalidad de las evaluaciones requeridas. "
                + "Numero de certificado: " + numero + ". "
                + "Fecha de emision: " + LocalDate.now().format(FORMATO_FECHA) + "."
        );

        inscripcion.setCertificadoEnviado(false);
        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        log.info("CertificadoService: certificado emitido #{} para inscripcion #{}", numero, inscripcion.getId());

        // Enviar email de notificacion al alumno de forma asincrona
        try {
            if (inscripcion.getAlumno() != null && inscripcion.getAlumno().getUsuario() != null) {
                enviarEmailCertificado(guardada);
                guardada.setCertificadoEnviado(true);
                guardada = inscripcionRepository.save(guardada);
            }
        } catch (Exception e) {
            log.error("CertificadoService: error al enviar email de certificado: {}", e.getMessage());
        }

        return guardada;
    }

    /**
     * CU-43 — Genera el PDF del certificado para descarga.
     * Retorna los bytes del PDF generado.
     */
    public byte[] generarPdf(Inscripcion inscripcion) {
        if (inscripcion == null || inscripcion.getNumeroCertificado() == null) {
            throw new ExcepcionValidacion("La inscripcion no tiene certificado emitido.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Fuentes
            Font fuenteTitulo = new Font(Font.HELVETICA, 28, Font.BOLD, new Color(0x1a, 0x2b, 0x5e));
            Font fuenteSubtitulo = new Font(Font.HELVETICA, 16, Font.NORMAL, new Color(0x4a, 0x5a, 0x8a));
            Font fuenteTexto = new Font(Font.HELVETICA, 13, Font.NORMAL, Color.DARK_GRAY);
            Font fuenteNegrita = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
            Font fuentePequena = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);

            // Titulo
            Paragraph titulo = new Paragraph("CERTIFICADO DE APROBACION", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingBefore(40);
            titulo.setSpacingAfter(8);
            doc.add(titulo);

            Paragraph subtitulo = new Paragraph("Idoneos Online - Plataforma de Formacion Profesional", fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(30);
            doc.add(subtitulo);

            // Linea separadora
            LineSeparator linea = new LineSeparator(1f, 80f, new Color(0x1a, 0x2b, 0x5e), Element.ALIGN_CENTER, -5);
            doc.add(linea);
            doc.add(Chunk.NEWLINE);

            // Cuerpo del certificado
            Paragraph intro = new Paragraph("Por medio del presente documento, se certifica que:", fuenteTexto);
            intro.setAlignment(Element.ALIGN_CENTER);
            intro.setSpacingBefore(20);
            intro.setSpacingAfter(15);
            doc.add(intro);

            String nombreAlumno = inscripcion.getNombreAlumno() != null ? inscripcion.getNombreAlumno() : "Alumno";
            Paragraph alumno = new Paragraph(nombreAlumno, fuenteTitulo);
            alumno.setAlignment(Element.ALIGN_CENTER);
            alumno.setSpacingAfter(10);
            doc.add(alumno);

            if (inscripcion.getDniAlumno() != null) {
                Paragraph dni = new Paragraph("DNI: " + inscripcion.getDniAlumno(), fuenteTexto);
                dni.setAlignment(Element.ALIGN_CENTER);
                dni.setSpacingAfter(15);
                doc.add(dni);
            }

            Paragraph aprobacion = new Paragraph("ha completado y aprobado satisfactoriamente el curso:", fuenteTexto);
            aprobacion.setAlignment(Element.ALIGN_CENTER);
            aprobacion.setSpacingAfter(10);
            doc.add(aprobacion);

            String nombreCurso = "Curso";
            if (inscripcion.getCohorte() != null
                    && inscripcion.getCohorte().getPrograma() != null
                    && inscripcion.getCohorte().getPrograma().getCurso() != null) {
                nombreCurso = inscripcion.getCohorte().getPrograma().getCurso().getNombre();
            }

            Paragraph curso = new Paragraph(nombreCurso, fuenteNegrita);
            curso.setAlignment(Element.ALIGN_CENTER);
            curso.setSpacingAfter(20);
            doc.add(curso);

            String fechaEmision = inscripcion.getFechaEmisionCertificado() != null
                    ? inscripcion.getFechaEmisionCertificado().toLocalDate().format(FORMATO_FECHA)
                    : LocalDate.now().format(FORMATO_FECHA);

            Paragraph fecha = new Paragraph("Buenos Aires, " + fechaEmision, fuenteTexto);
            fecha.setAlignment(Element.ALIGN_CENTER);
            fecha.setSpacingBefore(10);
            fecha.setSpacingAfter(30);
            doc.add(fecha);

            // Numero de certificado y link de verificacion
            doc.add(linea);
            doc.add(Chunk.NEWLINE);

            Paragraph numCert = new Paragraph("Numero de certificado: " + inscripcion.getNumeroCertificado(), fuentePequena);
            numCert.setAlignment(Element.ALIGN_CENTER);
            doc.add(numCert);

            Paragraph linkVerif = new Paragraph("Verificar en: " + baseUrl + "/certificado/validar/" + inscripcion.getNumeroCertificado(), fuentePequena);
            linkVerif.setAlignment(Element.ALIGN_CENTER);
            doc.add(linkVerif);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("CertificadoService: error al generar PDF del certificado: {}", e.getMessage());
            throw new ExcepcionNegocio("Error al generar el PDF del certificado: " + e.getMessage());
        }
    }

    /**
     * CU-43 — Verifica si un numero de certificado es valido.
     * Usado en el endpoint publico de verificacion.
     */
    @Transactional(readOnly = true)
    public Inscripcion verificarCertificado(String numeroCertificado) {
        return inscripcionRepository.findByNumeroCertificado(numeroCertificado)
                .orElse(null);
    }

    /** Envia el email de notificacion de certificado emitido al alumno. */
    private void enviarEmailCertificado(Inscripcion inscripcion) {
        Usuario usuario = inscripcion.getAlumno().getUsuario();
        String nombreCurso = inscripcion.getCohorte().getPrograma().getCurso().getNombre();

        String htmlBody = "<html><body style=\"font-family: Arial, sans-serif; background: #f8f9fa; padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: auto; background: white; border-radius: 12px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,.12);\">"
                + "<h2 style=\"color: #1a2b5e;\">&#127891; ¡Certificado emitido!</h2>"
                + "<p>Hola <strong>" + usuario.getNombre() + "</strong>,</p>"
                + "<p>Felicitaciones por completar el curso <strong>\"" + nombreCurso + "\"</strong>.</p>"
                + "<p>Tu certificado de aprobacion ha sido emitido con el numero:</p>"
                + "<p style=\"font-size: 20px; font-weight: bold; color: #1a2b5e; letter-spacing: 2px;\">"
                + inscripcion.getNumeroCertificado() + "</p>"
                + "<p>Podes descargarlo desde tu perfil o verificarlo en:</p>"
                + "<a href=\"" + baseUrl + "/certificado/validar/" + inscripcion.getNumeroCertificado() + "\" "
                + "style=\"background: #1a2b5e; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; display: inline-block; margin-top: 8px;\">Verificar Certificado</a>"
                + "<p style=\"margin-top: 24px; color: #666; font-size: 12px;\">Este es un correo automatico de Idoneos Online.</p>"
                + "</div></body></html>";

        emailService.enviar(usuario.getCorreo(), "Tu certificado de " + nombreCurso + " - Idoneos Online", htmlBody);
    }
}
