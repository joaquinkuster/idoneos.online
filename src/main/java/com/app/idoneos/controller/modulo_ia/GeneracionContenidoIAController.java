package com.app.idoneos.controller.modulo_ia;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_ia.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * TRAZABILIDAD — Controller para la generación de contenido académico asistido
 * por IA (Ollama / Llama 3.1).
 *
 * MOD-F-06: Módulo de Generación de Contenido con IA
 * CU-73 — Generar banco de preguntas (Ollama) → POST
 * /docente/ia/unidad/{unidadId}/generar-banco
 * Actor: Docente. Genera preguntas cerradas (opción múltiple y V-F) para el
 * pool de la unidad.
 * La unidad debe poseer al menos un material de tipo Bibliografía o un término
 * de glosario.
 * Integra con Ollama local (Llama 3.1 8B).
 * CU-74 — Generar resumen de unidad (Ollama) → POST
 * /docente/ia/unidad/{unidadId}/generar-resumen
 * Actor: Docente. Genera un resumen del contenido de la unidad.
 * La unidad debe poseer al menos un material de tipo Bibliografía.
 * Integra con Ollama local (Llama 3.1 8B).
 * CU-75 — Generar presentación de unidad (Ollama) → POST
 * /docente/ia/unidad/{unidadId}/generar-presentacion
 * Actor: Docente. Genera la estructura de una presentación para la clase.
 * La unidad debe poseer al menos un material de tipo Bibliografía.
 * Integra con Ollama local (Llama 3.1 8B).
 *
 * NOTAS DE COBERTURA:
 * CU-73 paso 2: el docente puede proporcionar un promptInput adicional para
 * personalizar las preguntas.
 * CU-73 EX-CU73-01 (paso 6): si Ollama no está disponible o el formato es
 * inválido → la excepción
 * se maneja en OllamaService.
 * CU-74 paso 5: el resumen se guarda como Material de la unidad con tipo
 * "Resumen" (implementado en OllamaService).
 * CU-75 paso 5: la presentación se guarda como Material de la unidad con tipo
 * "Presentación" (OllamaService).
 * CU-73/74/75: la verificación de que el docente pertenece al curso no está
 * implementada aquí. FALTANTE.
 * CU-73/74/75: la notificación al docente de la disponibilidad del material
 * generado no está implementada. FALTANTE.
 *
 * Integración tecnológica: Ollama local → Llama 3.1 8B.
 */
@Controller
@RequestMapping("/docente/ia")
public class GeneracionContenidoIAController {

    @Autowired
    private OllamaService ollamaService;
    @Autowired
    private UnidadServiceImpl unidadService;

    /**
     * TRAZABILIDAD: CU-73 — Generar banco de preguntas con IA.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La unidad existe y posee al menos un
     * material de tipo
     * Bibliografía o un término de glosario cargado.
     * Flujo paso 2: el docente provee opcionalmente un prompt personalizado.
     * Flujo paso 4-5: OllamaService envía bibliografía + glosario a Llama 3.1 y
     * genera preguntas.
     * Flujo paso 6-7: el sistema valida el formato y registra el pool generado
     * asociado a la unidad.
     * Postcondición: pool de la unidad registrado con las preguntas generadas
     * (pendiente de revisión).
     * EX-CU73-01 (paso 6): formato inválido o Ollama no disponible → manejo de
     * excepción en OllamaService.
     * NOTA PARCIAL: la notificación al docente de disponibilidad del pool (paso 8)
     * no está implementada. FALTANTE.
     * NOTA PARCIAL: la verificación de pertenencia del docente al curso no está
     * implementada aquí. FALTANTE.
     */
    @PostMapping("/unidad/{unidadId}/generar-banco")
    public String generarBanco(@PathVariable Integer unidadId,
            @RequestParam(required = false) String promptInput,
            RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null)
            return "redirect:/docente";

        // CU-73 paso 4-7: delega en OllamaService la generación y registro del pool de
        // preguntas.
        ollamaService.generarBancoPreguntas(unidad, promptInput);
        ra.addFlashAttribute("mensaje", "¡Banco de preguntas generado exitosamente con Ollama (Llama 3.1)!");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-74 — Generar resumen de unidad con IA.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La unidad existe y posee al menos un
     * material de tipo Bibliografía.
     * Flujo paso 3: OllamaService envía la bibliografía al modelo y genera el
     * resumen.
     * Flujo paso 5: el resumen generado se guarda como Material de tipo "Resumen"
     * en la unidad (publicado = false).
     * Postcondición: resumen disponible como material sin publicar, pendiente de
     * revisión.
     * EX-CU74-01 (paso 4): si Ollama no está disponible → manejo en OllamaService.
     * NOTA PARCIAL: CU-74 paso 6 notifica al docente. No implementado. FALTANTE.
     */
    @PostMapping("/unidad/{unidadId}/generar-resumen")
    public String generarResumen(@PathVariable Integer unidadId, RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null)
            return "redirect:/docente";

        // CU-74 paso 3-5: OllamaService genera el resumen y lo persiste como Material
        // de la unidad.
        ollamaService.generarResumenUnidad(unidad);
        ra.addFlashAttribute("mensaje", "¡Resumen de la unidad generado exitosamente con Ollama (Llama 3.1)!");
        return "redirect:/docente";
    }

    /**
     * TRAZABILIDAD: CU-75 — Generar presentación de unidad con IA.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La unidad existe y posee al menos un
     * material de tipo Bibliografía.
     * Flujo paso 3: OllamaService envía la bibliografía al modelo y genera la
     * estructura de la presentación.
     * Flujo paso 5: la presentación se registra como Material de tipo
     * "Presentación" en la unidad (publicado = false).
     * Postcondición: presentación disponible como material sin publicar, pendiente
     * de revisión.
     * EX-CU75-01 (paso 4): si Ollama no está disponible → manejo en OllamaService.
     * NOTA PARCIAL: CU-75 paso 6 notifica al docente. No implementado. FALTANTE.
     */
    @PostMapping("/unidad/{unidadId}/generar-presentacion")
    public String generarPresentacion(@PathVariable Integer unidadId,
            @RequestParam String promptInput,
            RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null)
            return "redirect:/docente";

        // CU-72 paso 3-4: OllamaService genera la presentación y la persiste como
        // Material de la unidad.
        ollamaService.generarPresentacionClase(unidad, promptInput);
        ra.addFlashAttribute("mensaje", "¡Estructura de presentación generada con Ollama (Llama 3.1)!");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }
}
