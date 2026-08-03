package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.repository.*;
import com.app.ecomisiones.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestión de Clases generadas con Avatar Clon IA (HeyGen API).
 * Permite redactar el guion (prompt input) y simular la generación asincrónica del video.
 */
@Controller
@RequestMapping("/clon-ia")
public class ClaseClonIAController {

    @Autowired private ClaseClonIARepository clonRepo;
    @Autowired private EstadoClaseClonIARepository estadoRepo;
    @Autowired private DocenteRepository docenteRepo;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private MaterialRepository materialRepo;
    @Autowired private TipoMaterialRepository tipoMaterialRepo;

    @GetMapping("/docente")
    public String panelClonIA(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepo.findById(usuario.getId()).orElse(null);
        if (docente == null) return "redirect:/docente";

        List<ClaseClonIA> clasesClon = clonRepo.findByDocenteAndBajaFalse(docente);
        model.addAttribute("usuario", usuario);
        model.addAttribute("docente", docente);
        model.addAttribute("clasesClon", clasesClon);
        model.addAttribute("titulo", "Clon IA — HeyGen | Idóneos Online");
        return "pages/docente/clon-ia";
    }

    @PostMapping("/generar")
    public String generarClase(@RequestParam Integer unidadId,
                               @RequestParam String titulo,
                               @RequestParam String guionPrompt,
                               Authentication auth,
                               RedirectAttributes ra) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepo.findById(usuario.getId()).orElse(null);
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);

        if (docente == null || !docente.puedeUsarClonIA()) {
            ra.addFlashAttribute("mensaje", "Error: El docente no tiene validado el consentimiento para Clon IA en HeyGen.");
            return "redirect:/clon-ia/docente";
        }

        if (unidad == null) {
            ra.addFlashAttribute("mensaje", "Unidad no válida.");
            return "redirect:/clon-ia/docente";
        }

        EstadoClaseClonIA estadoGenerada = estadoRepo.findByNombre("Generada").orElseGet(() ->
                estadoRepo.findByNombre("Pendiente").orElse(null));

        ClaseClonIA clase = new ClaseClonIA(titulo, unidad, docente, estadoGenerada);
        clase.setFechaGeneracion(LocalDate.now());

        // Generar material de tipo Grabación (HeyGen avatar result)
        TipoMaterial tipoGrabacion = tipoMaterialRepo.findByNombre("Grabación").orElse(null);
        if (tipoGrabacion != null) {
            String urlVideoHeyGen = "videos/heygen_avatar_" + System.currentTimeMillis() + ".mp4";
            Material m = new Material(tipoGrabacion, "Clon IA: " + titulo, urlVideoHeyGen, unidad);
            m.setGeneradoPorIA(true);
            m.setPublicado(false); // Oculto por defecto para revisión
            materialRepo.save(m);
            clase.setMaterial(m);
        }

        clonRepo.save(clase);
        ra.addFlashAttribute("mensaje", "¡Video con Clon IA generado exitosamente en HeyGen! Disponible en estado Oculto para tu revisión.");
        return "redirect:/clon-ia/docente";
    }
}
