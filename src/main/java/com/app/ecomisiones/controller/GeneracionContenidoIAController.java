package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.service.IA.GeminiService;
import com.app.ecomisiones.service.Material.MaterialServiceImpl;
import com.app.ecomisiones.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller para disparar la generación de contenido académico con IA (Gemini).
 */
@Controller
@RequestMapping("/docente/ia")
public class GeneracionContenidoIAController {

    @Autowired private GeminiService geminiService;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private MaterialServiceImpl materialService;

    // PA-9: Generar Banco de Preguntas
    @PostMapping("/unidad/{unidadId}/generar-banco")
    public String generarBanco(@PathVariable Integer unidadId,
                               @RequestParam(required = false) String promptInput,
                               RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        geminiService.generarBancoPreguntas(unidad, promptInput);
        ra.addFlashAttribute("mensaje", "¡Banco de preguntas generado exitosamente con Gemini IA!");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    // PA-8: Generar Resumen de Clase Grabada (via Whisper + Gemini)
    @PostMapping("/material/{materialId}/generar-resumen")
    public String generarResumen(@PathVariable Integer materialId, RedirectAttributes ra) {
        Optional<Material> grabacionOpt = materialService.buscarPorId(materialId);
        if (grabacionOpt.isEmpty()) return "redirect:/docente";

        Material grabacion = grabacionOpt.get();
        geminiService.generarResumenClase(grabacion.getUnidad(), grabacion);

        ra.addFlashAttribute("mensaje", "¡Resumen de la clase generado exitosamente con Whisper + Gemini IA! Disponible en estado Oculto.");
        return "redirect:/docente/curso/" + grabacion.getUnidad().getCurso().getId() + "/gestionar";
    }

    // PA-7: Generar Presentación de Clase (Diapositivas)
    @PostMapping("/unidad/{unidadId}/generar-presentacion")
    public String generarPresentacion(@PathVariable Integer unidadId,
                                      @RequestParam String promptInput,
                                      RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        geminiService.generarPresentacionClase(unidad, promptInput);
        ra.addFlashAttribute("mensaje", "¡Estructura de presentación generada con Gemini IA! Disponible en estado Oculto.");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }
}
