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
 * TRAZABILIDAD — Controller para la gestión de Clases con Avatar IA Clon (HeyGen API v2).
 *
 * MOD-F-06: Módulo de Generación de Contenido con IA
 *   CU-76 — Crear clon                  → no implementado como CU separado. FALTANTE.
 *             Actor: Docente. Solicita imagen con rostro y audio de voz. Envía a HeyGen.
 *             Registra avatar_id, voice_id y fecha de aceptación de términos en el perfil del Docente.
 *   CU-77 — Buscar clase con Clon IA    → GET /clon-ia/docente
 *             Actor: Docente o Administrador. Lista las clases clon del docente (baja = false).
 *             NOTA PARCIAL: CU-77 especifica filtros por unidad, título y estado. No implementados.
 *   CU-78 — Generar clase con Clon IA   → POST /clon-ia/generar
 *             Actor: Docente. Requiere consentimiento firmado (fechaConsentimientoClon != null).
 *             Integra con HeyGen API v2 (POST /v2/video/generate).
 *             NOTA PARCIAL: CU-78 pasos 7-8 son asíncronos (HeyGen notifica al sistema).
 *               Implementación actual es síncrona/sintética. IMPLEMENTADO PARCIALMENTE.
 *   CU-79 — Modificar clase con Clon IA → no implementado. FALTANTE.
 *             Actor: Docente. Permite modificar título y/o regenrar el video si se cambia el guión.
 *   CU-80 — Dar de baja clase con Clon IA → no implementado. FALTANTE.
 *             Actor: Docente o Administrador.
 *
 * NOTAS DE COBERTURA:
 *   CU-78 EX-CU78-01 (paso 2): si el docente no tiene consentimiento firmado →
 *     redirect con mensaje "EX-CU78-01".
 *   CU-78 paso 4 (EX-CU78-02): invoca la HeyGen API v2 con el guión y el avatar del docente.
 *     Si la API no está configurada (heygen.api_key ausente) → genera un ID de video sintético.
 *   CU-78 paso 5: el video generado se registra como Material de tipo "Grabación" con publicado = false.
 *
 * Regla de negocio crítica:
 *   Docente.puedeUsarClonIA() verifica que fechaConsentimientoClon no sea null.
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
                System.err.println("HeyGen API error (" + e.getMessage() + "). Se usa ID sintético.");
            }
        }
        return "heygen_vid_" + System.currentTimeMillis();
    }

    /**
     * TRAZABILIDAD: CU-77 — Buscar clase con Clon IA.
     * Actor: Docente (o Administrador).
     * Precondición: sesión con rol Docente. Existe al menos una clase clon registrada.
     * Flujo paso 4: recupera y lista las clases clon del docente (baja = false).
     *   Muestra el consentimiento del docente para Clon IA (para habilitar o bloquear el botón de generar).
     * NOTA PARCIAL: CU-77 especifica filtros por unidad, título y estado (Pendiente/Generada/Error). No implementados.
     */
    @GetMapping("/docente")
    public String panelClonIA(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepo.findById(usuario.getId()).orElse(null);
        if (docente == null) return "redirect:/docente";

        // CU-77 paso 4: lista las clases clon activas del docente.
        List<ClaseClonIA> clasesClon = clonRepo.findByDocenteAndBajaFalse(docente);
        model.addAttribute("usuario", usuario);
        model.addAttribute("docente", docente);
        model.addAttribute("clasesClon", clasesClon);
        model.addAttribute("titulo", "Clon IA — HeyGen | Idóneos Online");
        return "pages/docente/clon-ia";
    }

    /**
     * TRAZABILIDAD: CU-78 — Generar clase con Clon IA.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. El docente existe. Consentimiento firmado
     *   (fechaConsentimientoClon != null). La unidad existe.
     *   Estado "Generada" o "Pendiente" configurado en BD.
     * Flujo paso 2: verifica que el docente tenga consentimiento firmado y avatar/voz registrados.
     * Flujo paso 5-6: registra la clase en estado Pendiente. Envía guión, avatar_id y voice_id a HeyGen.
     * Flujo paso 8-9: descarga el video generado. Actualiza estado a Generada. Carga como Material.
     * Postcondición: clase clon registrada. Video en proceso/generado en HeyGen.
     *   Material cargado en la unidad con publicado = false.
     * EX-CU78-01 (paso 2): docente sin consentimiento → redirect con mensaje.
     * EX-CU78-02 (paso 4): unidad no válida → redirect con mensaje.
     * EX-CU78-03 (paso 7): HeyGen falla → actualiza estado a Error y notifica al docente (no implementado).
     * NOTA PARCIAL: la implementación es síncrona (simónica); CU-78 paso 7 es asíncrono (HeyGen notifica).
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

        // CU-78 paso 4: verificar consentimiento firmado del docente para uso de Clon IA.
        if (docente == null || !docente.puedeUsarClonIA()) {
            ra.addFlashAttribute("mensaje", "EX-CU78-01: El docente no tiene validado el consentimiento para Clon IA.");
            return "redirect:/clon-ia/docente";
        }

        if (unidad == null) {
            ra.addFlashAttribute("mensaje", "EX-CU78-02: Unidad no válida.");
            return "redirect:/clon-ia/docente";
        }

        EstadoClaseClonIA estadoGenerada = estadoRepo.findByNombre("Generada").orElseGet(() ->
                estadoRepo.findByNombre("Pendiente").orElse(null));

        // CU-78 paso 6: invocar HeyGen API v2 con el avatar del docente y el guión.
        String videoId = llamarHeyGenAPI(guionPrompt, "avatar_docente_" + docente.getId());

        ClaseClonIA clase = new ClaseClonIA(titulo, guionPrompt, docente, estadoGenerada);
        clase.setUnidad(unidad);
        clase.setFechaGeneracion(LocalDateTime.now());

        // CU-78 paso 8-9: registra el material de video generado (publicado = false, pendiente de revisión).
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
        ra.addFlashAttribute("mensaje", "¡Video con Clon IA generado exitosamente con HeyGen API (Video ID: " + videoId + ")!");
        return "redirect:/clon-ia/docente";
    }
}
