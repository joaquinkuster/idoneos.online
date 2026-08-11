package com.app.idoneos.controller;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.service.Certificado.CertificadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller para la emisión y consulta de certificados digitales de aprobación (CU-91: Emitir certificado de aprobación).
 */
@Controller
public class CertificadoController {

    @Autowired private CertificadoService certificadoService;
    @Autowired private InscripcionRepository inscripcionRepository;

    /**
     * CU-91 — Emitir y visualizar certificado digital de aprobación.
     */
    @GetMapping("/certificado/inscripcion/{inscripcionId}")
    public String verCertificado(@PathVariable Integer inscripcionId,
                                 Model model,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId).orElse(null);
        if (inscripcion == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        Usuario alumnoUsuario = (inscripcion.getAlumno() != null) ? inscripcion.getAlumno().getUsuario() : null;
        if (alumnoUsuario != null && alumnoUsuario.getId() != usuario.getId() && !usuario.esAdmin()) {
            ra.addFlashAttribute("mensaje", "No tenés acceso a este certificado.");
            return "redirect:/perfil";
        }

        inscripcion = certificadoService.emitirCertificado(inscripcion);

        model.addAttribute("usuario", usuario);
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("curso", inscripcion.getCurso());
        model.addAttribute("titulo", "Constancia de Finalización | " + (inscripcion.getCurso() != null ? inscripcion.getCurso().getNombre() : "Curso"));
        return "pages/alumno/certificado-vista";
    }

    /**
     * CU-91 — Consultar mis certificados emitidos.
     */
    @GetMapping("/usuario/certificados")
    public String misCertificados(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        List<Inscripcion> inscripciones = (usuario.getAlumno() != null) ? inscripcionRepository.findByAlumno(usuario.getAlumno()) : List.of();

        model.addAttribute("usuario", usuario);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("titulo", "Mis Certificados | Idóneos Online");
        return "pages/perfil/certificados";
    }
}
