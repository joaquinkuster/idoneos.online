package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.service.ClaseClonIa.OllamaService;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para disparar la generación de contenido académico asistido por IA.
 * Implementa: CU-68 — Generar banco de preguntas, CU-69 — Generar resumen de unidad,
 * CU-70 — Generar presentación de unidad.
 * Integra con Ollama local (Llama 3.1 8B).
 */
@Controller
@RequestMapping("/docente/ia")
public class GeneracionContenidoIAController {

    @Autowired private OllamaService ollamaService;
    @Autowired private UnidadServiceImpl unidadService;

    /**
     * CU-68 — Generar banco de preguntas.
     * Genera preguntas cerradas (opción múltiple y verdadero/falso) para la evaluación.
     */
    @PostMapping("/unidad/{unidadId}/generar-banco")
    public String generarBanco(@PathVariable Integer unidadId,
                               @RequestParam(required = false) String promptInput,
                               RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        ollamaService.generarBancoPreguntas(unidad, promptInput);
        ra.addFlashAttribute("mensaje", "¡Banco de preguntas generado exitosamente con Ollama (Llama 3.1)!");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    /**
     * CU-69 — Generar resumen de unidad.
     */
    @PostMapping("/unidad/{unidadId}/generar-resumen")
    public String generarResumen(@PathVariable Integer unidadId, RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        ollamaService.generarResumenUnidad(unidad);
        ra.addFlashAttribute("mensaje", "¡Resumen de la unidad generado exitosamente con Ollama (Llama 3.1)!");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }

    /**
     * CU-70 — Generar presentación de unidad.
     */
    @PostMapping("/unidad/{unidadId}/generar-presentacion")
    public String generarPresentacion(@PathVariable Integer unidadId,
                                      @RequestParam String promptInput,
                                      RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        ollamaService.generarPresentacionClase(unidad, promptInput);
        ra.addFlashAttribute("mensaje", "¡Estructura de presentación generada con Ollama (Llama 3.1)!");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }
}
