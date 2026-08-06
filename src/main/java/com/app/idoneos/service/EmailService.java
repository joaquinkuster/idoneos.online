package com.app.idoneos.service;

import com.app.idoneos.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Servicio centralizado de notificaciones por correo electrónico (MOD-NF-01).
 * Cada método corresponde a un evento del sistema definido en los casos de uso.
 * El envío es asincrónico (@Async) para no bloquear el flujo principal.
 * Si el mail no está configurado, registra el evento en el log (fail-safe).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:notificaciones@idoneos.online}")
    private String fromAddress;

    @Value("${idoneos.mail.from.name:Idóneos Online}")
    private String fromName;

    // ─────────────────────────────────────────────────────────────────────────
    // Método interno de envío
    // ─────────────────────────────────────────────────────────────────────────

    @Async
    public void enviar(String para, String asunto, String htmlBody) {
        if (mailSender == null || fromAddress == null || fromAddress.isBlank()) {
            log.info("[EMAIL-SIMULADO] Para: {} | Asunto: {}", para, asunto);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(para);
            helper.setSubject(asunto);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("[EMAIL-ENVIADO] Para: {} | Asunto: {}", para, asunto);
        } catch (Exception e) {
            log.error("[EMAIL-ERROR] Para: {} | Asunto: {} | Error: {}", para, asunto, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-67: Validación de cuenta al registrarse
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarValidacionCuenta(Usuario usuario, String urlValidacion) {
        String asunto = "Validá tu cuenta en Idóneos Online";
        String html = html(
                "Bienvenido/a a Idóneos Online, " + usuario.getNombre() + "!",
                "Para activar tu cuenta hacé clic en el siguiente botón:",
                urlValidacion,
                "Validar mi cuenta",
                "El enlace tiene validez de 24 horas. Si no lo solicitaste, ignorá este mensaje."
        );
        enviar(usuario.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-35 / PA-2: Confirmación de pago y habilitación de acceso
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarConfirmacionPago(Usuario alumno, Curso curso, Pago pago) {
        String asunto = "Pago acreditado — " + curso.getNombre();
        String html = html(
                "Tu pago fue acreditado",
                "Hola " + alumno.getNombre() + ", tu inscripción al curso <strong>" + curso.getNombre()
                        + "</strong> está activa. Monto abonado: <strong>$"
                        + String.format("%.2f", pago.getMonto()) + "</strong>.",
                "/cursos/" + curso.getId() + "/mi-cursada",
                "Ir a mi cursada",
                "Número de pago: " + pago.getPaymentId()
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-35: Pago rechazado
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarPagoRechazado(Usuario alumno, Curso curso) {
        String asunto = "Pago rechazado — " + curso.getNombre();
        String html = html(
                "Tu pago no fue aprobado",
                "Hola " + alumno.getNombre() + ", lamentablemente tu pago por el curso <strong>"
                        + curso.getNombre() + "</strong> fue rechazado por la pasarela de pagos.",
                "/pago/checkout/" + curso.getId(),
                "Intentar nuevamente",
                "Si el problema persiste, contactanos en contacto@idoneos.online."
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PA-2: Pago pendiente (sin confirmación dentro del plazo)
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarPagoPendiente(Usuario alumno, Curso curso) {
        String asunto = "Tu pago está pendiente de confirmación — " + curso.getNombre();
        String html = html(
                "Pago en proceso",
                "Hola " + alumno.getNombre() + ", tu pago por el curso <strong>" + curso.getNombre()
                        + "</strong> está siendo procesado. Te notificaremos cuando se confirme.",
                "/cursos",
                "Ver catálogo",
                "Si tenés dudas, contactanos en contacto@idoneos.online."
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-36: Comprobante de pago
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarComprobante(Usuario alumno, Comprobante comprobante, Curso curso) {
        String asunto = "Comprobante de pago — " + curso.getNombre();
        String html = html(
                "Tu comprobante de pago",
                "Hola " + alumno.getNombre() + ", adjuntamos el comprobante N° <strong>"
                        + comprobante.getNumero() + "</strong> por tu inscripción al curso <strong>"
                        + curso.getNombre() + "</strong>.",
                "/cursos/" + curso.getId() + "/mi-cursada",
                "Ir a mi cursada",
                "Fecha de emisión: " + comprobante.getFechaEmision()
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PA-6 / CU-53: Certificado de finalización emitido
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarCertificado(Usuario alumno, Certificado certificado, Curso curso) {
        String asunto = "Certificado de finalización — " + curso.getNombre();
        String html = html(
                "Felicitaciones, completaste el curso",
                "Hola " + alumno.getNombre() + ", aprobaste el curso <strong>" + curso.getNombre()
                        + "</strong>. Tu certificado N° <strong>" + certificado.getNumero() + "</strong> ya está disponible.",
                "/perfil/certificados",
                "Ver mi certificado",
                "Segui aprendiendo en Idóneos Online."
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PA-4: Grabación de clase en vivo disponible
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarGrabacionDisponible(Usuario alumno, ClaseEnVivo clase) {
        String asunto = "Grabación disponible — " + clase.getTitulo();
        String html = html(
                "La grabación ya está disponible",
                "Hola " + alumno.getNombre() + ", la grabación de la clase <strong>"
                        + clase.getTitulo() + "</strong> ya está disponible en tu cursada.",
                "/cursos/" + clase.getUnidad().getCurso().getId() + "/mi-cursada",
                "Ver grabación",
                "Disponible por tiempo limitado."
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PA-4: Aviso de vencimiento próximo de grabación
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarAvisoVencimientoGrabacion(Usuario alumno, ClaseEnVivo clase, int diasRestantes) {
        String asunto = "Grabación por vencer — " + clase.getTitulo();
        String html = html(
                "Tu grabación vence pronto",
                "Hola " + alumno.getNombre() + ", la grabación de <strong>" + clase.getTitulo()
                        + "</strong> vence en <strong>" + diasRestantes + " días</strong>. Descargala antes que expire.",
                "/cursos/" + clase.getUnidad().getCurso().getId() + "/mi-cursada",
                "Ver grabación",
                ""
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-58: Clase en vivo programada (notificación a alumnos)
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarClaseProgramada(Usuario alumno, ClaseEnVivo clase) {
        String asunto = "Nueva clase en vivo — " + clase.getTitulo();
        String html = html(
                "Se programó una nueva clase en vivo",
                "Hola " + alumno.getNombre() + ", el docente programó la clase <strong>"
                        + clase.getTitulo() + "</strong> para el <strong>"
                        + clase.getFechaHora().toLocalDate() + "</strong> a las <strong>"
                        + clase.getFechaHora().toLocalTime() + "</strong>.",
                "/cursos/" + clase.getUnidad().getCurso().getId() + "/mi-cursada",
                "Ver mi cursada",
                ""
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-24: Nueva consulta en foro (notificación al docente)
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarNuevaConsultaForo(String emailDocente, ConsultaForo consulta) {
        String asunto = "Nueva consulta en el foro — " + consulta.getUnidad().getTitulo();
        String html = html(
                "Nueva consulta de alumno",
                "<strong>" + consulta.getUsuario().getNombre() + " " + consulta.getUsuario().getApellido()
                        + "</strong> publicó una consulta en el foro de la unidad <strong>"
                        + consulta.getUnidad().getTitulo() + "</strong>:<br><em>"
                        + consulta.getTexto() + "</em>",
                "/foro/unidad/" + consulta.getUnidad().getId(),
                "Ver foro",
                ""
        );
        enviar(emailDocente, asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-28: Nueva respuesta en foro (notificación al alumno)
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarNuevaRespuestaForo(RespuestaForo respuesta) {
        ConsultaForo consulta = respuesta.getConsulta();
        Usuario alumno = consulta.getUsuario();
        String asunto = "El docente respondió tu consulta — " + consulta.getUnidad().getTitulo();
        String html = html(
                "Respuesta a tu consulta",
                "Hola " + alumno.getNombre() + ", el docente respondió tu consulta en el foro de <strong>"
                        + consulta.getUnidad().getTitulo() + "</strong>:<br><em>"
                        + respuesta.getTexto() + "</em>",
                "/foro/unidad/" + consulta.getUnidad().getId(),
                "Ver foro",
                ""
        );
        enviar(alumno.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-56 / PA-5: Clase Clon IA generada
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarClaseClonIAGenerada(Usuario docente, ClaseClonIA clase) {
        String asunto = "Tu clase con Clon IA fue generada — " + clase.getTitulo();
        String html = html(
                "Clase con Clon IA lista",
                "Hola " + docente.getNombre() + ", la clase <strong>" + clase.getTitulo()
                        + "</strong> fue generada correctamente. Está disponible en estado oculto para tu revisión.",
                "/docente",
                "Ir a mi panel",
                "Publicala cuando esté lista."
        );
        enviar(docente.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-74: Bienvenida a docente recién registrado
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarBienvenidaDocente(Usuario docente, String urlDefinirContrasena) {
        String asunto = "Bienvenido a Idóneos Online — Activá tu cuenta docente";
        String html = html(
                "Tu cuenta docente fue creada",
                "Hola " + docente.getNombre() + " " + docente.getApellido() + ", el equipo de Idóneos Online creó tu cuenta como docente. "
                        + "Hacé clic en el botón para definir tu contraseña y acceder a la plataforma.",
                urlDefinirContrasena,
                "Definir mi contraseña",
                "Si tenés dudas, contactanos en sistemas@idoneos.online."
        );
        enviar(docente.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CU-75: Notificación de suspensión de habilitación
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarSuspensionDocente(Usuario docente) {
        String asunto = "Aviso: tu habilitación como docente fue suspendida temporalmente";
        String html = html(
                "Suspensión temporal de habilitación",
                "Hola " + docente.getNombre() + ", la administración de Idóneos Online ha suspendido temporalmente "
                        + "tu habilitación para dictar clases. Tu cuenta y tu historial permanecen intactos.",
                null,
                null,
                "Para más información, contactate con la administración en sistemas@idoneos.online."
        );
        enviar(docente.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PA-7/8/9: Contenido IA generado (resumen, presentación, banco)
    // ─────────────────────────────────────────────────────────────────────────

    public void enviarContenidoIAGenerado(Usuario docente, String tipoContenido, String tituloUnidad) {
        String asunto = "Contenido IA generado — " + tipoContenido + " - " + tituloUnidad;
        String html = html(
                tipoContenido + " generado con IA",
                "Hola " + docente.getNombre() + ", el " + tipoContenido.toLowerCase()
                        + " para la unidad <strong>" + tituloUnidad
                        + "</strong> fue generado exitosamente. Está en estado oculto para tu revisión.",
                "/docente",
                "Ir a mi panel",
                "Publicalo cuando esté listo."
        );
        enviar(docente.getCorreo(), asunto, html);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Plantilla HTML base para todos los emails
    // ─────────────────────────────────────────────────────────────────────────

    private String html(String titulo, String cuerpo, String urlBoton, String textoBoton, String nota) {
        String botonHtml = (urlBoton != null && textoBoton != null)
                ? "<a href=\"" + urlBoton + "\" style=\"display:inline-block;margin-top:20px;padding:12px 28px;"
                + "background:#1a56db;color:#fff;text-decoration:none;border-radius:8px;font-weight:600;"
                + "font-family:Arial,sans-serif;font-size:15px;\">" + textoBoton + "</a>"
                : "";

        String notaHtml = (nota != null && !nota.isBlank())
                ? "<p style=\"margin-top:24px;font-size:13px;color:#6b7280;\">" + nota + "</p>"
                : "";

        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif;\">"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f3f4f6;padding:40px 20px;\"><tr><td align=\"center\">"
                + "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
                + "<tr><td style=\"background:linear-gradient(135deg,#1a56db 0%,#1e40af 100%);padding:32px 40px;text-align:center;\">"
                + "<h1 style=\"margin:0;color:#fff;font-size:24px;font-weight:700;\">Idóneos Online</h1>"
                + "<p style=\"margin:6px 0 0;color:#bfdbfe;font-size:13px;\">Plataforma de Cursos de Finanzas y Economía</p></td></tr>"
                + "<tr><td style=\"padding:36px 40px;\">"
                + "<h2 style=\"margin:0 0 16px;color:#111827;font-size:20px;font-weight:700;\">" + titulo + "</h2>"
                + "<p style=\"margin:0;color:#374151;font-size:15px;line-height:1.7;\">" + cuerpo + "</p>"
                + botonHtml + notaHtml + "</td></tr>"
                + "<tr><td style=\"background:#f9fafb;border-top:1px solid #e5e7eb;padding:20px 40px;text-align:center;\">"
                + "<p style=\"margin:0;color:#9ca3af;font-size:12px;\">"
                + "© 2026 Idóneos Online S.A.S. · Apóstoles, Misiones, Argentina<br>"
                + "Este correo fue generado automáticamente.</p></td></tr>"
                + "</table></td></tr></table></body></html>";
    }
}
