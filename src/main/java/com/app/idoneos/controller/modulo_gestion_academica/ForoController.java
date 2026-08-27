package com.app.idoneos.controller.modulo_gestion_academica;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Controller para Respuestas de Foro (CU-39 a CU-42).
 *
 * Mapea las 4 vistas del paquete foro:
 *   CU-39 — Buscar respuesta de foro      → GET /foro/consultas/{consultaId}/respuestas
 *   CU-40 — Registrar respuesta de foro   → GET /foro/consultas/{consultaId}/responder, POST /foro/consultas/{consultaId}/responder
 *   CU-41 — Modificar respuesta de foro   → GET /foro/respuestas/{id}/editar, POST /foro/respuestas/{id}/editar
 *   CU-42 — Dar de baja respuesta de foro → GET/POST /foro/respuestas/{id}/baja
 */
@Controller
@RequestMapping("/foro")
public class ForoController {

    @Autowired private ForoService foroService;
    @Autowired private ConsultaForoRepository consultaRepo;
    @Autowired private RespuestaForoRepository respuestaRepo;
    @Autowired private UnidadService unidadService;

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    /**
     * CU-39 — Buscar respuesta de foro.
     * Vista: cu-39-buscar-respuesta-de-foro.html
     */
    @GetMapping("/consultas/{consultaId}/respuestas")
    public String buscarRespuestas(@PathVariable Integer consultaId, Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        Optional<ConsultaForo> cOpt = foroService.buscarConsultaPorId(consultaId);
        if (cOpt.isEmpty()) return "redirect:/academico/foro";

        ConsultaForo consulta = cOpt.get();
        List<RespuestaForo> respuestas = foroService.obtenerRespuestasPorConsulta(consulta);

        model.addAttribute("consulta", consulta);
        model.addAttribute("respuestas", respuestas);
        model.addAttribute("titulo", "CU-39 - Respuestas de Foro | Idóneos Online");
        return "pages/foro/cu-39-buscar-respuesta-de-foro";
    }

    /**
     * CU-40 — Registrar respuesta de foro (GET).
     * Vista: cu-40-registrar-respuesta-de-foro.html
     */
    @GetMapping("/consultas/{consultaId}/responder")
    public String responderConsultaForm(@PathVariable Integer consultaId, Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        Optional<ConsultaForo> cOpt = foroService.buscarConsultaPorId(consultaId);
        if (cOpt.isEmpty()) return "redirect:/academico/foro";

        model.addAttribute("consulta", cOpt.get());
        model.addAttribute("titulo", "CU-40 - Responder consulta de foro | Idóneos Online");
        return "pages/foro/cu-40-registrar-respuesta-de-foro";
    }

    /**
     * CU-40 — Registrar respuesta de foro (POST).
     */
    @PostMapping("/consultas/{consultaId}/responder")
    public String guardarRespuesta(@PathVariable Integer consultaId,
                                   @RequestParam String texto,
                                   Authentication auth, RedirectAttributes ra) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";
        Usuario usuario = (Usuario) auth.getPrincipal();

        try {
            ConsultaForo consulta = foroService.buscarConsultaPorId(consultaId)
                    .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));
            foroService.crearRespuesta(texto, usuario, consulta);
            ra.addFlashAttribute("mensaje", "Respuesta publicada exitosamente.");
            return "redirect:/foro/consultas/" + consultaId + "/respuestas";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/foro/consultas/" + consultaId + "/responder";
        }
    }

    /**
     * CU-41 — Modificar respuesta de foro (GET).
     * Vista: cu-41-modificar-respuesta-de-foro.html
     */
    @GetMapping("/respuestas/{id}/editar")
    public String modificarRespuestaForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<RespuestaForo> rOpt = foroService.buscarRespuestaPorId(id);
        if (rOpt.isEmpty()) return "redirect:/academico/foro";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("respuesta", rOpt.get());
        model.addAttribute("titulo", "CU-41 - Modificar respuesta de foro | Idóneos Online");
        return "pages/foro/cu-41-modificar-respuesta-de-foro";
    }

    /**
     * CU-41 — Modificar respuesta de foro (POST).
     */
    @PostMapping("/respuestas/{id}/editar")
    public String actualizarRespuesta(@PathVariable Integer id,
                                      @RequestParam String texto,
                                      RedirectAttributes ra) {
        try {
            RespuestaForo r = foroService.buscarRespuestaPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Respuesta no encontrada"));
            r.setTexto(texto);
            foroService.modificarRespuesta(r);
            ra.addFlashAttribute("mensaje", "Respuesta modificada con éxito.");
            return "redirect:/foro/consultas/" + r.getConsulta().getId() + "/respuestas";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/foro/respuestas/" + id + "/editar";
        }
    }

    /**
     * CU-42 — Dar de baja respuesta de foro (GET/POST).
     * Vista: cu-42-dar-de-baja-respuesta-de-foro.html
     */
    @GetMapping("/respuestas/{id}/baja")
    public String darDeBajaRespuestaView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<RespuestaForo> rOpt = foroService.buscarRespuestaPorId(id);
        if (rOpt.isEmpty()) return "redirect:/academico/foro";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("respuesta", rOpt.get());
        model.addAttribute("titulo", "CU-42 - Dar de baja respuesta de foro | Idóneos Online");
        return "pages/foro/cu-42-dar-de-baja-respuesta-de-foro";
    }

    @PostMapping("/respuestas/{id}/baja")
    public String eliminarRespuesta(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            RespuestaForo r = foroService.buscarRespuestaPorId(id).orElse(null);
            Integer cId = (r != null && r.getConsulta() != null) ? r.getConsulta().getId() : null;
            foroService.darDeBajaRespuesta(id);
            ra.addFlashAttribute("mensaje", "Respuesta dada de baja correctamente.");
            return cId != null ? "redirect:/foro/consultas/" + cId + "/respuestas" : "redirect:/academico/foro";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/foro/respuestas/" + id + "/baja";
        }
    }
}
