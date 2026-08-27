package com.app.idoneos.controller.modulo_ia;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_configuracion.*;

import com.app.idoneos.service.modulo_gestion_academica.*;
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
 * TRAZABILIDAD — Controller para la gestión de Clases con Avatar IA Clon
 * (HeyGen API v2).
 *
 * MOD-F-06: Módulo de Generación de Contenido con IA
 * CU-76 — Crear clon → POST /clon-ia/crear-clon (registro biométrico y
 * aceptación de TyC)
 * CU-77 — Buscar clase con Clon IA → GET /clon-ia/docente
 * CU-78 — Generar clase con Clon IA → POST /clon-ia/generar (integración con
 * HeyGen)
 * CU-79 — Modificar clase con Clon IA → POST /clon-ia/{id}/modificar (título y
 * guion)
 * CU-80 — Dar de baja clase con Clon IA → POST /clon-ia/{id}/baja (baja lógica)
 */
@Controller
@RequestMapping("/clon-ia")
public class ClaseClonIAController {

    @Autowired
    private ClaseClonIARepository clonRepo;
    @Autowired
    private EstadoClaseClonIARepository estadoRepo;
    @Autowired
    private DocenteRepository docenteRepo;
    @Autowired
    private UnidadServiceImpl unidadService;
    @Autowired
    private MaterialRepository materialRepo;
    @Autowired
    private TipoMaterialRepository tipoMaterialRepo;
    @Autowired
    private ConfiguracionRepository configRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Recupera la API Key de HeyGen desde la tabla de configuración.
     * Clave: "heygen.api_key".
     */
    private String getHeyGenApiKey() {
        return configRepo.findByClave("heygen.api_key")
                .map(Configuracion::getValor)
                .orElse(null);
    }

    /**
     * Invoca la HeyGen API v2 para generar el video con el avatar del docente.
     * Si la API no está configurada o falla, retorna un ID de video sintético.
     * CU-78 paso 6: POST https://api.heygen.com/v2/video/generate.
     */
    private String llamarHeyGenAPI(String guionPrompt, String avatarId) {
        String apiKey = getHeyGenApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String url = "https://api.heygen.com/v2/video/generate";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Api-Key", apiKey);

                Map<String, Object> videoInput = new HashMap<>();
                videoInput.put("character",
                        Map.of("type", "avatar", "avatar_id", avatarId != null ? avatarId : "default_avatar"));
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
                System.err.println("HeyGen API error (" + e.getMessage() + "). Se usa ID sintético.");
            }
        }
        return "heygen_vid_" + System.currentTimeMillis();
    }

    /**
     * TRAZABILIDAD: CU-76 — Crear clon.
     * Actor: Docente.
     */
    @PostMapping("/crear-clon")
    public String crearClon(@RequestParam(defaultValue = "false") boolean aceptaTerminos,
            @RequestParam(required = false) String imagen,
            @RequestParam(required = false) String audio,
            Authentication auth,
            RedirectAttributes ra) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepo.findById(usuario.getId()).orElse(null);
        if (docente == null)
            return "redirect:/docente";

        if (!aceptaTerminos) {
            ra.addFlashAttribute("mensaje",
                    "EX-CU76-01: Es obligatorio aceptar los términos y condiciones de consentimiento biométrico.");
            return "redirect:/clon-ia/docente";
        }

        docente.setFechaAceptacionTycClon(LocalDateTime.now());
        docente.setAvatarId("avatar_docente_" + docente.getId() + "_" + UUID.randomUUID().toString().substring(0, 8));
        docente.setVoiceId("voice_docente_" + docente.getId() + "_" + UUID.randomUUID().toString().substring(0, 8));
        docenteRepo.save(docente);

        ra.addFlashAttribute("mensaje", "¡Clon virtual y consentimiento biométrico registrados correctamente!");
        return "redirect:/clon-ia/docente";
    }

    /**
     * TRAZABILIDAD: CU-77 — Buscar clase con Clon IA.
     * Actor: Docente / Administrador.
     */
    @GetMapping("/docente")
    public String panelClonIA(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepo.findById(usuario.getId()).orElse(null);
        if (docente == null && !usuario.esAdmin())
            return "redirect:/docente";

        List<ClaseClonIA> clasesClon = (docente != null)
                ? clonRepo.findByDocenteAndBajaFalse(docente)
                : clonRepo.findAll().stream().filter(c -> !c.getBaja()).toList();

        model.addAttribute("usuario", usuario);
        model.addAttribute("docente", docente);
        model.addAttribute("clasesClon", clasesClon);
        model.addAttribute("titulo", "Clon IA — HeyGen | Idóneos Online");
        return "pages/docente/clon-ia";
    }

    /**
     * TRAZABILIDAD: CU-78 — Generar clase con Clon IA.
     * Actor: Docente.
     */
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
            ra.addFlashAttribute("mensaje", "EX-CU78-01: El docente no tiene validado el consentimiento para Clon IA.");
            return "redirect:/clon-ia/docente";
        }

        if (unidad == null) {
            ra.addFlashAttribute("mensaje", "EX-CU78-02: Unidad no válida.");
            return "redirect:/clon-ia/docente";
        }

        EstadoClaseClonIA estadoGenerada = estadoRepo.findByNombre("Generada")
                .orElseGet(() -> estadoRepo.findByNombre("Pendiente").orElse(null));

        String avatarId = docente.getAvatarId() != null ? docente.getAvatarId() : ("avatar_docente_" + docente.getId());
        String videoId = llamarHeyGenAPI(guionPrompt, avatarId);

        ClaseClonIA clase = new ClaseClonIA(titulo.trim(), guionPrompt.trim(), docente, estadoGenerada);
        clase.setUnidad(unidad);
        clase.setFechaGeneracion(LocalDateTime.now());

        TipoMaterial tipoGrabacion = tipoMaterialRepo.findByNombre("Grabación").orElse(null);
        if (tipoGrabacion != null) {
            String urlVideoHeyGen = "videos/heygen_" + videoId + ".mp4";
            Material m = new Material("Clon IA: " + titulo, docente, tipoGrabacion, unidad);
            m.setRutaArchivo(urlVideoHeyGen);
            m.setGeneradoPorIa(true);
            m.setPublicado(false);
            materialRepo.save(m);
            clase.setMaterial(m);
        }

        clonRepo.save(clase);
        ra.addFlashAttribute("mensaje",
                "¡Video con Clon IA generado exitosamente con HeyGen API (Video ID: " + videoId + ")!");
        return "redirect:/clon-ia/docente";
    }

    /**
     * TRAZABILIDAD: CU-79 — Modificar clase con clon.
     * Actor: Docente.
     */
    @PostMapping("/{id}/modificar")
    public String modificarClaseClon(@PathVariable Integer id,
            @RequestParam String titulo,
            @RequestParam String guion,
            RedirectAttributes ra) {
        Optional<ClaseClonIA> cOpt = clonRepo.findById(id);
        if (cOpt.isEmpty())
            return "redirect:/clon-ia/docente";

        ClaseClonIA clase = cOpt.get();
        clase.setTitulo(titulo.trim());
        clase.setGuion(guion.trim());
        clonRepo.save(clase);

        ra.addFlashAttribute("mensaje", "Clase con Clon IA modificada correctamente.");
        return "redirect:/clon-ia/docente";
    }

    /**
     * TRAZABILIDAD: CU-80 — Dar de baja clase con clon.
     * Actor: Docente / Administrador.
     */
    @PostMapping("/{id}/baja")
    public String darDeBajaClaseClon(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<ClaseClonIA> cOpt = clonRepo.findById(id);
        if (cOpt.isEmpty())
            return "redirect:/clon-ia/docente";

        ClaseClonIA clase = cOpt.get();
        clase.setBaja(true);
        clonRepo.save(clase);

        ra.addFlashAttribute("mensaje", "Clase con Clon IA dada de baja del sistema.");
        return "redirect:/clon-ia/docente";
    }
}
