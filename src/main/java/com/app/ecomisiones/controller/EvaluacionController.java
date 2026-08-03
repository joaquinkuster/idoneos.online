package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.service.Evaluacion.EvaluacionService;
import com.app.ecomisiones.service.Evaluacion.IntentoService;
import com.app.ecomisiones.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * Controlador para Pools, Preguntas, Opciones, Autoevaluaciones e Intentos.
 * - Rutas /docente/** → solo DOCENTE
 * - Rutas /evaluacion/** → ALUMNO (rendir examen)
 */
@Controller
public class EvaluacionController {

    @Autowired private EvaluacionService evaluacionService;
    @Autowired private IntentoService intentoService;
    @Autowired private UnidadServiceImpl unidadService;

    // ══════════════════════════════════════════════════════════════════════════
    // DOCENTE — Gestionar Pool y Preguntas
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/docente/unidad/{unidadId}/pool")
    public String verPool(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", unidad);
        model.addAttribute("pool", evaluacionService.buscarPoolPorUnidad(unidad).orElse(null));
        model.addAttribute("titulo", "Pool de Preguntas | " + unidad.getTitulo());
        return "pages/docente/gestionar-pool";
    }

    @PostMapping("/docente/unidad/{unidadId}/pool/crear")
    public String crearPool(@PathVariable Integer unidadId,
                            @RequestParam String nombre,
                            RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        if (evaluacionService.buscarPoolPorUnidad(unidad).isPresent()) {
            ra.addFlashAttribute("mensaje", "Esta unidad ya tiene un pool de preguntas.");
            return "redirect:/docente/unidad/" + unidadId + "/pool";
        }
        Pool pool = new Pool(nombre, unidad);
        evaluacionService.guardarPool(pool);
        ra.addFlashAttribute("mensaje", "Pool creado correctamente.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    @PostMapping("/docente/pool/{poolId}/pregunta/agregar")
    public String agregarPregunta(@PathVariable Integer poolId,
                                  @RequestParam String texto,
                                  @RequestParam(defaultValue = "true") Boolean esOpcionMultiple,
                                  @RequestParam List<String> opciones,
                                  @RequestParam Integer correcta,
                                  RedirectAttributes ra) {
        Pool pool = evaluacionService.buscarPoolPorId(poolId).orElse(null);
        if (pool == null) return "redirect:/docente";

        Pregunta pregunta = new Pregunta(texto, esOpcionMultiple, pool);
        pregunta = evaluacionService.guardarPregunta(pregunta);

        for (int i = 0; i < opciones.size(); i++) {
            OpcionRespuesta opcion = new OpcionRespuesta(opciones.get(i), i == correcta, pregunta);
            evaluacionService.guardarOpcion(opcion);
        }

        ra.addFlashAttribute("mensaje", "Pregunta agregada.");
        return "redirect:/docente/unidad/" + pool.getUnidad().getId() + "/pool";
    }

    @PostMapping("/docente/pregunta/{preguntaId}/borrar")
    public String borrarPregunta(@PathVariable Integer preguntaId, RedirectAttributes ra) {
        Pregunta p = evaluacionService.buscarPreguntaPorId(preguntaId).orElse(null);
        if (p == null) return "redirect:/docente";
        Integer unidadId = p.getPool().getUnidad().getId();
        evaluacionService.borrarPregunta(p);
        ra.addFlashAttribute("mensaje", "Pregunta eliminada.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ALUMNO — Rendir Examen
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/evaluacion/{autoevaluacionId}/rendir")
    public String verExamen(@PathVariable Integer autoevaluacionId, Model model, Authentication auth) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();

        try {
            intentoService.iniciarIntento(ae, usuario); // valida límite de intentos
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }

        List<Pregunta> preguntas = intentoService.sortearPreguntas(ae);

        model.addAttribute("usuario", usuario);
        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("titulo", "Autoevaluación: " + ae.getNombre());
        return "pages/alumno/rendir-examen";
    }

    @PostMapping("/evaluacion/{autoevaluacionId}/enviar")
    public String enviarExamen(@PathVariable Integer autoevaluacionId,
                               @RequestParam Map<String, String> form,
                               Authentication auth,
                               RedirectAttributes ra) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(ae, usuario);

        // Parsear respuestas: prefijo "pregunta_" + preguntaId → opcionId
        Map<Integer, Integer> respuestas = new HashMap<>();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (entry.getKey().startsWith("pregunta_")) {
                try {
                    Integer preguntaId = Integer.parseInt(entry.getKey().replace("pregunta_", ""));
                    Integer opcionId = Integer.parseInt(entry.getValue());
                    respuestas.put(preguntaId, opcionId);
                } catch (NumberFormatException ignored) {}
            }
        }

        IntentoAutoevaluacion resultado = intentoService.corregirYGuardar(intento, respuestas);
        ra.addFlashAttribute("intentoId", resultado.getId());
        ra.addFlashAttribute("nota", resultado.getNota());
        ra.addFlashAttribute("aprobado", intentoService.estaAprobado(resultado));
        return "redirect:/evaluacion/" + autoevaluacionId + "/resultado";
    }

    @GetMapping("/evaluacion/{autoevaluacionId}/resultado")
    public String verResultado(@PathVariable Integer autoevaluacionId, Model model, Authentication auth) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        List<IntentoAutoevaluacion> historial = intentoService.historialPorAlumno(ae, usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("historial", historial);
        model.addAttribute("titulo", "Resultado | " + ae.getNombre());
        return "pages/alumno/resultado-examen";
    }
}
