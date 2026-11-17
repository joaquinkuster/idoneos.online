package com.app.idoneos.service.correo;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.app.idoneos.model.Usuario;

import jakarta.mail.internet.MimeMessage;

/** Implementación de CorreoService. Los correos se envían en segundo plano para que la operación que los pide no espere al servidor de correo. */
@Service
public class CorreoServiceImpl implements CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoServiceImpl.class);

    /** Ruta con la que se valida el correo de una cuenta nueva. */
    private static final String RUTA_VALIDAR_CORREO = "/usuario/validarCorreo/";

    /** Ruta con la que se crea una contraseña nueva. */
    private static final String RUTA_RESTABLECER = "/recuperar/";

    @Autowired
    private JavaMailSender enviador;

    /** Dirección desde la que se envían los correos. */
    @Value("${spring.mail.username:}")
    private String remitente;

    /** Contraseña de la cuenta que envía. Si está vacía, los correos no se envían y el enlace se escribe en el registro. */
    @Value("${spring.mail.password:}")
    private String clave;

    /** Nombre que aparece como remitente. */
    @Value("${idoneos.mail.from.name:Idóneos Online}")
    private String nombreRemitente;

    /** Dirección pública de la aplicación, con la que se arman los enlaces. */
    @Value("${idoneos.url-base:http://localhost:8080}")
    private String urlBase;

    // ----------------------------------------------------------------------
    // Envíos
    // ----------------------------------------------------------------------

    /** Envía al usuario el enlace para validar su correo electrónico. */
    @Override
    @Async
    public void enviarValidacionCuenta(Usuario usuario, String token) {
        String enlace = urlBase + RUTA_VALIDAR_CORREO + token;
        enviar(usuario, "Validá tu correo en Idóneos Online",
                "Te damos la bienvenida a Idóneos Online",
                "Para activar tu cuenta, validá tu correo electrónico con el siguiente botón. El enlace vence en 24 horas.",
                "Validar mi correo", enlace);
    }

    /** Envía al usuario el enlace para crear una contraseña nueva. */
    @Override
    @Async
    public void enviarRecuperacionContrasena(Usuario usuario, String token) {
        String enlace = urlBase + RUTA_RESTABLECER + token;
        enviar(usuario, "Recuperá tu contraseña de Idóneos Online",
                "Recuperación de contraseña",
                "Recibimos un pedido para crear una contraseña nueva en tu cuenta. El enlace vence en 1 hora. "
                        + "Si no lo pediste vos, ignorá este correo.",
                "Crear contraseña nueva", enlace);
    }

    /** Envía al usuario creado por un administrador el enlace para que defina su contraseña. */
    @Override
    @Async
    public void enviarDefinicionContrasena(Usuario usuario, String token) {
        String enlace = urlBase + RUTA_RESTABLECER + token;
        enviar(usuario, "Definí tu contraseña en Idóneos Online",
                "Tu cuenta ya está creada",
                "Un administrador creó tu cuenta en Idóneos Online. Definí tu contraseña con el siguiente botón. "
                        + "El enlace vence en 72 horas.",
                "Definir mi contraseña", enlace);
    }

    /** Envía al usuario el correo de una notificación del sistema, esperando a que salga. */
    @Override
    public void enviarNotificacion(Usuario usuario, String asunto, String mensaje, String url) {
        enviar(usuario, asunto, asunto, mensaje, url == null ? null : "Ver en Idóneos Online", url == null ? null : urlBase + url);
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Arma y envía el correo. Sin contraseña de la cuenta emisora, escribe el enlace en el registro de la aplicación. */
    private void enviar(Usuario usuario, String asunto, String titulo, String texto, String boton, String enlace) {
        if (clave == null || clave.isBlank()) {
            log.warn("Correo sin enviar, no hay contraseña de correo configurada. Para {} ({}): {}", usuario.getCorreo(), asunto,
                    enlace != null ? enlace : texto);
            return;
        }
        try {
            MimeMessage mensaje = enviador.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            ayudante.setFrom(remitente, nombreRemitente);
            ayudante.setTo(usuario.getCorreo());
            ayudante.setSubject(asunto);
            ayudante.setText(armarHtml(usuario, titulo, texto, boton, enlace), true);
            enviador.send(mensaje);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}", usuario.getCorreo(), e);
            throw new IllegalStateException("Error! No se pudo enviar el correo. Intentá nuevamente en unos minutos.");
        }
    }

    /** Arma el cuerpo del correo, con el estilo de la institución. */
    private String armarHtml(Usuario usuario, String titulo, String texto, String boton, String enlace) {
        String nombre = HtmlUtils.htmlEscape(usuario.getNombre());
        return "<div style=\"background:#F1F5F9;padding:24px;font-family:Arial,Helvetica,sans-serif;color:#0F172A\">"
                + "<div style=\"max-width:520px;margin:0 auto;background:#FFFFFF;border:1px solid #E2E8F0;border-radius:14px;overflow:hidden\">"
                + "<div style=\"background:#0F233D;padding:18px 24px;color:#FFFFFF;font-size:18px\">Idóneos <span style=\"color:#D4A03D\">Online</span></div>"
                + "<div style=\"padding:24px\">"
                + "<h2 style=\"margin:0 0 12px;font-size:18px\">" + HtmlUtils.htmlEscape(titulo) + "</h2>"
                + "<p style=\"margin:0 0 12px;line-height:1.5\">Hola " + nombre + ",</p>"
                + "<p style=\"margin:0 0 20px;line-height:1.5\">" + HtmlUtils.htmlEscape(texto) + "</p>"
                + (enlace == null ? "" : "<p style=\"margin:0 0 20px\"><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"" + HtmlUtils.htmlEscape(enlace)
                + "\" style=\"display:inline-block;height:46px;line-height:46px;padding:0 26px;background:#D4A03D;border:1px solid #D4A03D;border-radius:8px;color:#081426;font-size:14px;font-weight:700;text-decoration:none\">"
                + HtmlUtils.htmlEscape(boton) + "</a></p>"
                + "<p style=\"margin:0;font-size:12px;color:#64748B;word-break:break-all\">Si el botón no funciona, copiá este enlace en tu navegador: "
                + HtmlUtils.htmlEscape(enlace) + "</p>")
                + "</div></div></div>";
    }
}
