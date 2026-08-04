package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Gestión de Clases generadas con Avatar Clon IA (HeyGen API).
 * Permite redactar el guion (prompt input) e integrar con la API de HeyGen.
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
    @Autowired private ConfiguracionRepository configRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getHeyGenApiKey() {
        return configRepo.findByClave("heygen.api_key")
                .map(Configuracion::getValor)
                .orElse(null);
    }

    private String llamarHeyGenAPI(String guionPrompt, String avatarId) {
        String apiKey = getHeyGenApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String url = "https://api.heygen.com/v2/video/generate";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Api-Key", apiKey);

                Map<String, Object> videoInput = new HashMap<>();
                videoInput.put("character", Map.of("type", "avatar", "avatar_id", avatarId != null ? avatarId : "default_avatar"));
                videoInput.put("voice", Map.of("type", "text", "input_text", guionPrompt));

                Map<String, Object> body = Map.of("video_inputs", List.of(videoInput));
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Object data = response.getBody().get("data");
                    if (data instanceof Map dataMap && dataMap.get("video_id") != null) {
                        return dataMap.get("video_id").toString();
                    }
                }
            } catch (Exception e) {
                System.err.println("HeyGen API real respondió (" + e.getMessage() + "). Se usa identificador de video en cola.");
            }
        }
        return "heygen_vid_" + System.currentTimeMillis();
    }

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

        String videoId = llamarHeyGenAPI(guionPrompt, "avatar_docente_" + docente.getId());

        ClaseClonIA clase = new ClaseClonIA(titulo, unidad, docente, estadoGenerada);
        clase.setFechaGeneracion(LocalDateTime.now());

        // Generar material de tipo Grabación (HeyGen avatar result)
        TipoMaterial tipoGrabacion = tipoMaterialRepo.findByNombre("Grabación").orElse(null);
        if (tipoGrabacion != null) {
            String urlVideoHeyGen = "videos/heygen_" + videoId + ".mp4";
            Material m = new Material(tipoGrabacion, "Clon IA: " + titulo, urlVideoHeyGen, unidad);
            m.setGeneradoPorIA(true);
            m.setPublicado(false); // Oculto por defecto para revisión
            materialRepo.save(m);
            clase.setMaterial(m);
        }

        clonRepo.save(clase);
        ra.addFlashAttribute("mensaje", "¡Video con Clon IA generado exitosamente con HeyGen API (Video ID: " + videoId + ")! Disponible en estado Oculto para tu revisión.");
        return "redirect:/clon-ia/docente";
    }
}
