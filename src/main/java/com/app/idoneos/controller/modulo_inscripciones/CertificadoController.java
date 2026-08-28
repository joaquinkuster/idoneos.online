package com.app.idoneos.controller.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller para la emisión, consulta, descarga y validación pública de certificados digitales de aprobación
 * (CU-43 / CU-91: Emitir certificado de aprobación).
 */
@Controller
public class CertificadoController {

    @Autowired
    private CertificadoService certificadoService;
    @Autowired
    private InscripcionRepository inscripcionRepository;

    /**
     * CU-91 — Emitir y visualizar certificado digital de aprobación.
     */
    @GetMapping("/certificado/inscripcion/{inscripcionId}")
    public String verCertificado(@PathVariable Integer inscripcionId,
            Model model,
            Authentication auth,
            RedirectAttributes ra) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId).orElse(null);
        if (inscripcion == null)
            return "redirect:/cursos";

        Usuario usuario = (auth != null && auth.getPrincipal() instanceof Usuario) ? (Usuario) auth.getPrincipal() : null;
        Usuario alumnoUsuario = (inscripcion.getAlumno() != null) ? inscripcion.getAlumno().getUsuario() : null;
        if (usuario != null && alumnoUsuario != null && alumnoUsuario.getId() != usuario.getId() && !usuario.esAdmin()) {
            ra.addFlashAttribute("mensaje", "No tenés acceso a este certificado.");
            return "redirect:/seguridad/perfil";
        }

        inscripcion = certificadoService.emitirCertificado(inscripcion);

        model.addAttribute("usuario", usuario);
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("curso", (inscripcion.getCohorte() != null && inscripcion.getCohorte().getPrograma() != null) 
                ? inscripcion.getCohorte().getPrograma().getCurso() : null);
        model.addAttribute("titulo", "Constancia de Finalización | "
                + (inscripcion.getCurso() != null ? inscripcion.getCurso().getNombre() : "Curso"));
        return "pages/alumno/certificado-vista";
    }

    /**
     * CU-43 / CU-91 — Descargar el certificado digital en PDF.
     */
    @GetMapping("/certificado/descargar/{inscripcionId}")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Integer inscripcionId, Authentication auth) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId).orElse(null);
        if (inscripcion == null || inscripcion.getNumeroCertificado() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = certificadoService.generarPdf(inscripcion);
        String filename = "Certificado-" + inscripcion.getNumeroCertificado() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * CU-91 / Endpoint Público — Validar autenticidad de un certificado emitido mediante su número.
     */
    @GetMapping("/certificado/validar/{numeroCertificado}")
    public String validarCertificadoPublico(@PathVariable String numeroCertificado, Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }

        Inscripcion inscripcion = certificadoService.verificarCertificado(numeroCertificado);
        model.addAttribute("numeroBuscado", numeroCertificado);
        model.addAttribute("valido", inscripcion != null);
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("titulo", "Validación de Certificado Académico | Idóneos Online");
        return "pages/inscripciones/certificado-detalle";
    }

    /**
     * CU-91 — Consultar mis certificados emitidos.
     */
    @GetMapping("/usuario/certificados")
    public String misCertificados(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        List<Inscripcion> inscripciones = (usuario.getAlumno() != null)
                ? inscripcionRepository.findByAlumno(usuario.getAlumno())
                : List.of();

        model.addAttribute("usuario", usuario);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("titulo", "Mis Certificados | Idóneos Online");
        return "pages/perfil/certificados";
    }
}
