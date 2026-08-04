package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.service.IA.OllamaService;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para disparar la generación de contenido académico con IA local (Ollama).
 * Ollama ejecuta el modelo Llama 3.1 (8B parámetros, 128K contexto) localmente,
 * sin enviar datos fuera de la red y sin costo por uso.
 */
@Controller
@RequestMapping("/docente/ia")
public class GeneracionContenidoIAController {

    @Autowired private OllamaService ollamaService;
    @Autowired private UnidadServiceImpl unidadService;

    /**
     * PA-9: Generar Banco de Preguntas.
     * El docente aporta bibliografía/glosario de la unidad y opcionalmente un prompt.
     * Ollama genera preguntas cerradas (opción múltiple y V/F) según la proporción configurada.
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
     * PA-8: Generar Resumen de Unidad.
     * El sistema recopila la bibliografía cargada de la unidad y la envía a Ollama,
     * que genera un resumen estructurado. No requiere Whisper ni transcripción de audio.
     */
    @PostMapping("/unidad/{unidadId}/generar-resumen")
    public String generarResumen(@PathVariable Integer unidadId, RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        ollamaService.generarResumenUnidad(unidad);
        ra.addFlashAttribute("mensaje", "¡Resumen de la unidad generado exitosamente con Ollama (Llama 3.1)! Disponible en estado Oculto para su revisión.");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }

    /**
     * PA-7: Generar Presentación de Clase (Diapositivas).
     * El docente redacta un guión mediante un prompt; Ollama devuelve la estructura
     * de contenidos (títulos, subtítulos, puntos clave) que el sistema formatea como presentación.
     */
    @PostMapping("/unidad/{unidadId}/generar-presentacion")
    public String generarPresentacion(@PathVariable Integer unidadId,
                                      @RequestParam String promptInput,
                                      RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        ollamaService.generarPresentacionClase(unidad, promptInput);
        ra.addFlashAttribute("mensaje", "¡Estructura de presentación generada con Ollama (Llama 3.1)! Disponible en estado Oculto para su revisión.");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }
}
