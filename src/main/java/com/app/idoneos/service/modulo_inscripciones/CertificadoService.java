package com.app.idoneos.service.modulo_inscripciones;

import com.app.idoneos.exception.ExcepcionNegocio;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_inscripciones.InscripcionRepository;
import com.app.idoneos.service.modulo_usuarios.EmailService;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * TRAZABILIDAD — Servicio para la emisión y verificación de Certificados Digitales Académicos.
 *
 * MOD-F-03: Módulo de Inscripciones y Pagos
 *   CU-43 — Buscar inscripción: incluye descarga del certificado digital oficial en alta fidelidad.
 *   CU-63 — Realizar intento: emisión del diploma digital al aprobar la última unidad.
 */
@Service
@Transactional
public class CertificadoService {

    private static final Logger log = LoggerFactory.getLogger(CertificadoService.class);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_TEXTO = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "AR"));
    private static final String FORMATO_NUMERO = "CERT-%d-%06d";

    // Paleta de colores oficial del sistema Idóneos Online (Figma Design System)
    private static final Color COLOR_NAVY_DARK = new Color(8, 20, 38);      // #081426
    private static final Color COLOR_NAVY_MID = new Color(15, 35, 61);      // #0F233D
    private static final Color COLOR_GOLD = new Color(212, 160, 61);        // #D4A03D
    private static final Color COLOR_GOLD_LIGHT = new Color(243, 218, 163);  // #F3DAA3
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // #64748B
    private static final Color COLOR_TEXT_MAIN = new Color(51, 65, 85);     // #334155
    private static final Color COLOR_BG_CERT = new Color(255, 255, 255);
    private static final Color COLOR_BORDER_OUTER = new Color(212, 160, 61);
    private static final Color COLOR_BORDER_INNER = new Color(8, 20, 38);

    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired @Lazy private EmailService emailService;

    @Value("${idoneos.app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * CU-63 paso 10 / CU-91 — Emite el certificado digital de aprobación para la inscripción.
     * Si el certificado ya fue emitido, no lo regenera.
     * Regla de negocio: Número de certificado correlativo e infalsificable (CERT-YYYY-000000).
     */
    public Inscripcion emitirCertificado(Inscripcion inscripcion) {
        if (inscripcion == null) throw new ExcepcionValidacion("La inscripción no puede ser nula.");

        // Idempotente: si ya tiene número no regenerar
        if (inscripcion.getNumeroCertificado() != null && !inscripcion.getNumeroCertificado().isBlank()) {
            log.info("CertificadoService: certificado ya emitido para inscripción #{}: {}", inscripcion.getId(), inscripcion.getNumeroCertificado());
            return inscripcion;
        }

        // Generar número de certificado correlativo
        String numero = String.format(FORMATO_NUMERO, LocalDate.now().getYear(), inscripcion.getId());
        inscripcion.setNumeroCertificado(numero);
        inscripcion.setFechaEmisionCertificado(LocalDateTime.now());

        // Capturar datos del alumno en el momento de la emisión
        if (inscripcion.getAlumno() != null && inscripcion.getAlumno().getUsuario() != null) {
            Usuario usuario = inscripcion.getAlumno().getUsuario();
            inscripcion.setNombreAlumno(usuario.getNombre() + " " + usuario.getApellido());
            inscripcion.setDniAlumno(usuario.getDni());
        }

        // Generar texto del certificado
        String nombreCurso = (inscripcion.getCurso() != null)
                ? inscripcion.getCurso().getNombre()
                : (inscripcion.getCohorte() != null && inscripcion.getCohorte().getPrograma() != null && inscripcion.getCohorte().getPrograma().getCurso() != null
                    ? inscripcion.getCohorte().getPrograma().getCurso().getNombre()
                    : "Curso de Especialización");

        inscripcion.setTextoCertificado(
                "Por medio del presente certificado, Idóneos Online acredita que "
                + (inscripcion.getNombreAlumno() != null ? inscripcion.getNombreAlumno() : "el/la alumno/a")
                + (inscripcion.getDniAlumno() != null ? " (DNI: " + inscripcion.getDniAlumno() + ")" : "")
                + " ha completado y aprobado satisfactoriamente las exigencias académicas y evaluaciones del curso \""
                + nombreCurso + "\". "
                + "Número de certificado: " + numero + ". "
                + "Fecha de emisión: " + LocalDate.now().format(FORMATO_FECHA) + "."
        );

        inscripcion.setCertificadoEnviado(false);
        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        log.info("CertificadoService: certificado emitido #{} para inscripción #{}", numero, inscripcion.getId());

        // Enviar email de notificación al alumno de forma asíncrona
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
     * CU-43 / CU-91 — Genera el PDF del certificado para descarga con el diseño oficial Landscape (Figma Design System).
     */
    public byte[] generarPdf(Inscripcion inscripcion) {
        if (inscripcion == null || inscripcion.getNumeroCertificado() == null) {
            throw new ExcepcionValidacion("La inscripción no tiene certificado emitido.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Tamaño Landscape A4 (842 x 595 pt) con márgenes armónicos
            Document doc = new Document(PageSize.A4.rotate(), 54, 54, 46, 42);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);

            // Marco ornamental y fondo oficial
            writer.setPageEvent(new CertificadoBackgroundEvent());

            doc.open();

            // 1. ENCABEZADO: Logos institucionales, marca Idóneos Online y Avales FCEQyN UNaM / CNV
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3.2f, 1.8f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell cellBrand = new PdfPCell();
            cellBrand.setBorder(Rectangle.NO_BORDER);
            cellBrand.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPTable brandInner = new PdfPTable(2);
            brandInner.setWidthPercentage(100);
            brandInner.setWidths(new float[]{0.65f, 3.35f});
            brandInner.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            com.lowagie.text.Image logoImg = null;
            try {
                ClassPathResource r1 = new ClassPathResource("static/img/logos/image.png");
                logoImg = com.lowagie.text.Image.getInstance(r1.getURL());
            } catch (Exception ignored) {
                try {
                    ClassPathResource r2 = new ClassPathResource("logo.png");
                    logoImg = com.lowagie.text.Image.getInstance(r2.getURL());
                } catch (Exception ignored2) {}
            }

            PdfPCell cellLogoImg = new PdfPCell();
            cellLogoImg.setBorder(Rectangle.NO_BORDER);
            cellLogoImg.setVerticalAlignment(Element.ALIGN_MIDDLE);
            if (logoImg != null) {
                logoImg.scaleToFit(50, 42);
                cellLogoImg.addElement(logoImg);
            }
            brandInner.addCell(cellLogoImg);

            PdfPCell cellBrandText = new PdfPCell();
            cellBrandText.setBorder(Rectangle.NO_BORDER);
            cellBrandText.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Font fontBrandMain = new Font(Font.HELVETICA, 15, Font.BOLD, COLOR_NAVY_DARK);
            Font fontBrandTag = new Font(Font.HELVETICA, 7f, Font.BOLD, COLOR_TEXT_MUTED);

            Paragraph pBrand = new Paragraph();
            pBrand.add(new Chunk("IDÓNEOS ", fontBrandMain));
            Font fontGold = new Font(Font.HELVETICA, 15, Font.BOLD, COLOR_GOLD);
            pBrand.add(new Chunk("ONLINE", fontGold));
            cellBrandText.addElement(pBrand);

            Paragraph pTag = new Paragraph("EDUCACIÓN FINANCIERA & MERCADO DE CAPITALES", fontBrandTag);
            pTag.setSpacingBefore(1);
            cellBrandText.addElement(pTag);

            brandInner.addCell(cellBrandText);
            cellBrand.addElement(brandInner);
            headerTable.addCell(cellBrand);

            // Avales institucionales FCEQyN UNaM y CNV
            PdfPCell cellEndorsers = new PdfPCell();
            cellEndorsers.setBorder(Rectangle.NO_BORDER);
            cellEndorsers.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellEndorsers.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Font fontEndorser = new Font(Font.HELVETICA, 8f, Font.BOLD, COLOR_NAVY_MID);
            Paragraph pEndorsers = new Paragraph("FCEQyN — UNaM  •  CNV RÉGIMEN IDÓNEOS", fontEndorser);
            pEndorsers.setAlignment(Element.ALIGN_RIGHT);
            cellEndorsers.addElement(pEndorsers);

            Font fontEndorserSub = new Font(Font.HELVETICA, 7f, Font.NORMAL, COLOR_TEXT_MUTED);
            Paragraph pEndorsersSub = new Paragraph("Validez Nacional • Resolución CNV N° 19.340", fontEndorserSub);
            pEndorsersSub.setAlignment(Element.ALIGN_RIGHT);
            pEndorsersSub.setSpacingBefore(2);
            cellEndorsers.addElement(pEndorsersSub);

            headerTable.addCell(cellEndorsers);
            doc.add(headerTable);

            // Espaciador vertical moderado
            Paragraph sp1 = new Paragraph(" ");
            sp1.setSpacingBefore(18);
            doc.add(sp1);

            // 2. CUERPO CENTRAL: Título "CERTIFICADO" y otorgamiento
            Font fontIntro = new Font(Font.HELVETICA, 9.5f, Font.BOLD, COLOR_TEXT_MUTED);
            Paragraph pIntro = new Paragraph("IDÓNEOS ONLINE S.A.S. OTORGA EL PRESENTE", fontIntro);
            pIntro.setAlignment(Element.ALIGN_CENTER);
            pIntro.setSpacingAfter(4);
            doc.add(pIntro);

            Font fontMainTitle = new Font(Font.TIMES_ROMAN, 32, Font.BOLD, COLOR_NAVY_DARK);
            Paragraph pMainTitle = new Paragraph("CERTIFICADO", fontMainTitle);
            pMainTitle.setAlignment(Element.ALIGN_CENTER);
            pMainTitle.setSpacingAfter(14);
            doc.add(pMainTitle);

            // Nombre del Alumno con DNI
            String nombreEstudiante = (inscripcion.getNombreAlumno() != null && !inscripcion.getNombreAlumno().isBlank())
                    ? inscripcion.getNombreAlumno()
                    : (inscripcion.getAlumno() != null && inscripcion.getAlumno().getUsuario() != null
                        ? inscripcion.getAlumno().getUsuario().getNombreCompleto()
                        : "Alumno/a Certificado/a");

            String dniEstudiante = (inscripcion.getDniAlumno() != null && !inscripcion.getDniAlumno().isBlank())
                    ? inscripcion.getDniAlumno()
                    : (inscripcion.getAlumno() != null && inscripcion.getAlumno().getUsuario() != null
                        ? inscripcion.getAlumno().getUsuario().getDni()
                        : "N/A");

            Font fontA = new Font(Font.HELVETICA, 12, Font.NORMAL, COLOR_TEXT_MUTED);
            Font fontStudentName = new Font(Font.TIMES_ROMAN, 22, Font.BOLD, COLOR_NAVY_DARK);
            Font fontDni = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_TEXT_MAIN);

            Paragraph pStudent = new Paragraph();
            pStudent.setAlignment(Element.ALIGN_CENTER);
            pStudent.add(new Chunk("A:   ", fontA));
            pStudent.add(new Chunk(nombreEstudiante.toUpperCase(), fontStudentName));
            if (dniEstudiante != null && !dniEstudiante.isBlank() && !dniEstudiante.equals("N/A")) {
                pStudent.add(new Chunk("      D.N.I.: " + dniEstudiante, fontDni));
            }
            pStudent.setSpacingAfter(18);
            doc.add(pStudent);

            // Texto de acreditación formal
            String nombreCurso = (inscripcion.getCurso() != null)
                    ? inscripcion.getCurso().getNombre()
                    : (inscripcion.getCohorte() != null && inscripcion.getCohorte().getPrograma() != null && inscripcion.getCohorte().getPrograma().getCurso() != null
                        ? inscripcion.getCohorte().getPrograma().getCurso().getNombre()
                        : "Especialización en Idoneidad Bursátil");

            Font fontBody = new Font(Font.HELVETICA, 11f, Font.NORMAL, COLOR_TEXT_MAIN);
            Font fontBodyBold = new Font(Font.HELVETICA, 11f, Font.BOLD, COLOR_NAVY_DARK);

            Paragraph pBody = new Paragraph();
            pBody.setAlignment(Element.ALIGN_CENTER);
            pBody.setLeading(18);
            pBody.add(new Chunk("Por haber completado y aprobado satisfactoriamente la totalidad de las exigencias académicas y evaluaciones de la ", fontBody));
            pBody.add(new Chunk("\"" + nombreCurso + "\"", fontBodyBold));
            pBody.add(new Chunk(", cumpliendo los estándares y regulaciones del Régimen de Idóneos en Mercado de Capitales, con una carga horaria total de ", fontBody));
            pBody.add(new Chunk("120 horas cátedra", fontBodyBold));
            pBody.add(new Chunk(".", fontBody));
            pBody.setSpacingAfter(36);
            doc.add(pBody);

            // 3. FIRMAS FORMALES DE AUTORIDADES
            PdfPTable signaturesTable = new PdfPTable(2);
            signaturesTable.setWidthPercentage(80);
            signaturesTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            signaturesTable.setWidths(new float[]{1f, 1f});
            signaturesTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Firma 1: Docente Titular
            PdfPCell cellSig1 = new PdfPCell();
            cellSig1.setBorder(Rectangle.NO_BORDER);
            cellSig1.setHorizontalAlignment(Element.ALIGN_CENTER);

            Font fontSigSim = new Font(Font.TIMES_ROMAN, 18, Font.ITALIC | Font.BOLD, COLOR_NAVY_MID);
            Paragraph pSigName1Sim = new Paragraph("Fausto Spotorno", fontSigSim);
            pSigName1Sim.setAlignment(Element.ALIGN_CENTER);
            cellSig1.addElement(pSigName1Sim);

            Paragraph pSigLine1 = new Paragraph("____________________________________", new Font(Font.HELVETICA, 7.5f, Font.NORMAL, COLOR_NAVY_DARK));
            pSigLine1.setAlignment(Element.ALIGN_CENTER);
            cellSig1.addElement(pSigLine1);

            Paragraph pSigRole1 = new Paragraph("Lic. Fausto Spotorno\nDocente Titular Responsable • Matrícula CNV 4821", new Font(Font.HELVETICA, 8f, Font.BOLD, COLOR_NAVY_DARK));
            pSigRole1.setAlignment(Element.ALIGN_CENTER);
            pSigRole1.setSpacingBefore(3);
            cellSig1.addElement(pSigRole1);

            signaturesTable.addCell(cellSig1);

            // Firma 2: Director Académico
            PdfPCell cellSig2 = new PdfPCell();
            cellSig2.setBorder(Rectangle.NO_BORDER);
            cellSig2.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph pSigName2Sim = new Paragraph("Lazaro Martinez", fontSigSim);
            pSigName2Sim.setAlignment(Element.ALIGN_CENTER);
            cellSig2.addElement(pSigName2Sim);

            Paragraph pSigLine2 = new Paragraph("____________________________________", new Font(Font.HELVETICA, 7.5f, Font.NORMAL, COLOR_NAVY_DARK));
            pSigLine2.setAlignment(Element.ALIGN_CENTER);
            cellSig2.addElement(pSigLine2);

            Paragraph pSigRole2 = new Paragraph("Lic. Lazaro Martinez\nDirector Académico General • Idóneos Online", new Font(Font.HELVETICA, 8f, Font.BOLD, COLOR_NAVY_DARK));
            pSigRole2.setAlignment(Element.ALIGN_CENTER);
            pSigRole2.setSpacingBefore(3);
            cellSig2.addElement(pSigRole2);

            signaturesTable.addCell(cellSig2);

            doc.add(signaturesTable);

            // 4. PIE DE PÁGINA METADATOS Y VALIDACIÓN QR / CÓDIGO
            PdfPTable footerTable = new PdfPTable(3);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{1.4f, 1.8f, 2.3f});
            footerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            footerTable.setSpacingBefore(34);

            LocalDate fechaEmision = (inscripcion.getFechaEmisionCertificado() != null)
                    ? inscripcion.getFechaEmisionCertificado().toLocalDate()
                    : LocalDate.now();

            // Bloque de Registro Digital y Código Correlativo
            PdfPCell f1 = new PdfPCell();
            f1.setBorder(Rectangle.NO_BORDER);
            f1.setVerticalAlignment(Element.ALIGN_MIDDLE);

            PdfPTable codeBadge = new PdfPTable(1);
            codeBadge.setWidthPercentage(100);
            PdfPCell badgeCell = new PdfPCell();
            badgeCell.setBackgroundColor(new Color(248, 250, 252));
            badgeCell.setBorderColor(COLOR_GOLD);
            badgeCell.setBorderWidth(0.75f);
            badgeCell.setPadding(5);

            Paragraph pBadgeTitle = new Paragraph("REGISTRO DIGITAL OFICIAL", new Font(Font.HELVETICA, 6f, Font.BOLD, COLOR_TEXT_MUTED));
            Paragraph pBadgeNum = new Paragraph(inscripcion.getNumeroCertificado(), new Font(Font.COURIER, 9f, Font.BOLD, COLOR_NAVY_DARK));
            badgeCell.addElement(pBadgeTitle);
            badgeCell.addElement(pBadgeNum);
            codeBadge.addCell(badgeCell);
            f1.addElement(codeBadge);

            footerTable.addCell(f1);

            // Ubicación y fecha
            PdfPCell f2 = new PdfPCell();
            f2.setBorder(Rectangle.NO_BORDER);
            f2.setHorizontalAlignment(Element.ALIGN_CENTER);
            f2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph pLoc = new Paragraph("Posadas, Misiones, Argentina\n" + fechaEmision.format(FORMATO_FECHA_TEXTO), new Font(Font.HELVETICA, 7.5f, Font.NORMAL, COLOR_TEXT_MUTED));
            pLoc.setAlignment(Element.ALIGN_CENTER);
            f2.addElement(pLoc);
            footerTable.addCell(f2);

            // Verificación online
            PdfPCell f3 = new PdfPCell();
            f3.setBorder(Rectangle.NO_BORDER);
            f3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            f3.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph pVerif = new Paragraph("Verificación de Autenticidad:\n" + baseUrl + "/certificado/validar/" + inscripcion.getNumeroCertificado(), new Font(Font.HELVETICA, 7f, Font.NORMAL, COLOR_NAVY_DARK));
            pVerif.setAlignment(Element.ALIGN_RIGHT);
            f3.addElement(pVerif);
            footerTable.addCell(f3);

            doc.add(footerTable);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("CertificadoService: error al generar PDF del certificado: {}", e.getMessage(), e);
            throw new ExcepcionNegocio("Error al generar el PDF del certificado: " + e.getMessage());
        }
    }

    /**
     * Evento de página para dibujar marco doble perimetral Dorado y Navy y detalles ornamentales.
     */
    static class CertificadoBackgroundEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();
            float width = PageSize.A4.rotate().getWidth();
            float height = PageSize.A4.rotate().getHeight();

            // Fondo blanco nítido
            cb.setColorFill(COLOR_BG_CERT);
            cb.rectangle(0, 0, width, height);
            cb.fill();

            // Franja dorada superior delgada
            cb.setColorFill(COLOR_GOLD);
            cb.rectangle(0, height - 6, width, 6);
            cb.fill();

            // Marco exterior dorado (2pt)
            cb.setColorStroke(COLOR_BORDER_OUTER);
            cb.setLineWidth(2f);
            cb.rectangle(24, 24, width - 48, height - 48);
            cb.stroke();

            // Marco interior navy (0.75pt)
            cb.setColorStroke(COLOR_BORDER_INNER);
            cb.setLineWidth(0.75f);
            cb.rectangle(29, 29, width - 58, height - 58);
            cb.stroke();

            // Esquinas ornamentales cuadradas doradas
            float sz = 8;
            cb.setColorFill(COLOR_GOLD);
            // Sup Izq
            cb.rectangle(24, height - 24 - sz, sz, sz);
            // Sup Der
            cb.rectangle(width - 24 - sz, height - 24 - sz, sz, sz);
            // Inf Izq
            cb.rectangle(24, 24, sz, sz);
            // Inf Der
            cb.rectangle(width - 24 - sz, 24, sz, sz);
            cb.fill();
        }
    }

    /**
     * CU-43 — Verifica si un número de certificado es válido.
     * Usado en el endpoint público de verificación.
     */
    @Transactional(readOnly = true)
    public Inscripcion verificarCertificado(String numeroCertificado) {
        return inscripcionRepository.findByNumeroCertificado(numeroCertificado)
                .orElse(null);
    }

    /** Envía el email de notificación de certificado emitido al alumno. */
    private void enviarEmailCertificado(Inscripcion inscripcion) {
        Usuario usuario = (inscripcion.getAlumno() != null) ? inscripcion.getAlumno().getUsuario() : null;
        if (usuario == null) return;
        String nombreCurso = (inscripcion.getCurso() != null)
                ? inscripcion.getCurso().getNombre()
                : "Curso de Especialización";

        String htmlBody = "<html><body style=\"font-family: Arial, sans-serif; background: #f8f9fa; padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: auto; background: white; border-radius: 12px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,.12);\">"
                + "<h2 style=\"color: #081426;\">&#127891; ¡Certificado oficial emitido!</h2>"
                + "<p>Hola <strong>" + usuario.getNombre() + "</strong>,</p>"
                + "<p>Felicitaciones por completar y aprobar satisfactoriamente el curso <strong>\"" + nombreCurso + "\"</strong>.</p>"
                + "<p>Tu certificado digital oficial ha sido emitido con el número de registro:</p>"
                + "<p style=\"font-size: 20px; font-weight: bold; color: #D4A03D; letter-spacing: 2px;\">"
                + inscripcion.getNumeroCertificado() + "</p>"
                + "<p>Podés descargarlo desde la plataforma o verificar su autenticidad pública en:</p>"
                + "<a href=\"" + baseUrl + "/certificado/validar/" + inscripcion.getNumeroCertificado() + "\" "
                + "style=\"background: #081426; color: #D4A03D; padding: 12px 24px; border-radius: 6px; text-decoration: none; font-weight: bold; display: inline-block; margin-top: 8px;\">Verificar Certificado Digital</a>"
                + "<p style=\"margin-top: 24px; color: #64748B; font-size: 12px;\">Idóneos Online S.A.S. • Educación Financiera y Mercado de Capitales.</p>"
                + "</div></body></html>";

        emailService.enviar(usuario.getCorreo(), "Tu Certificado Oficial de " + nombreCurso + " - Idóneos Online", htmlBody);
    }
}
