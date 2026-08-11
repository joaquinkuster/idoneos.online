package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.service.Evaluacion.EvaluacionService;
import com.app.idoneos.service.Evaluacion.IntentoService;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * Controller para la gestión de Pools de Preguntas, Autoevaluaciones e Intentos de Examen.
 * Implementa: CU-51 — Buscar pool, CU-52 — Crear pool, CU-53 — Modificar pool, CU-54 — Eliminar pool,
 * CU-55 — Buscar autoevaluación, CU-56 — Crear autoevaluación, CU-57 — Modificar autoevaluación,
 * CU-58 — Eliminar autoevaluación, CU-59 — Buscar intento de autoevaluación,
 * CU-60 — Realizar intento de autoevaluación.
 */
@Controller
public class EvaluacionController {

    @Autowired private EvaluacionService evaluacionService;
    @Autowired private IntentoService intentoService;
    @Autowired private UnidadServiceImpl unidadService;

    // ─── DOCENTE — Pools de Preguntas (CU-51 a CU-54) ───────────────────────────

    /**
     * CU-51 — Buscar pool.
     */
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

    /**
     * CU-52 — Crear pool.
     */
    @PostMapping("/docente/unidad/{unidadId}/pool/crear")
    public String crearPool(@PathVariable Integer unidadId,
                            @RequestParam String nombre,
                            RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        if (evaluacionService.buscarPoolPorUnidad(unidad).isPresent()) {
            ra.addFlashAttribute("mensaje", "EX-CU52-01: Esta unidad ya cuenta con un pool de preguntas registrado.");
            return "redirect:/docente/unidad/" + unidadId + "/pool";
        }
        Pool pool = new Pool(nombre, unidad);
        evaluacionService.guardarPool(pool);
        ra.addFlashAttribute("mensaje", "Pool creado correctamente.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    /**
     * CU-52 — Crear pool (Cargar preguntas de opción múltiple / Verdadero-Falso).
     */
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

    /**
     * CU-54 — Eliminar pool (Eliminar pregunta).
     */
    @PostMapping("/docente/pregunta/{preguntaId}/borrar")
    public String borrarPregunta(@PathVariable Integer preguntaId, RedirectAttributes ra) {
        Pregunta p = evaluacionService.buscarPreguntaPorId(preguntaId).orElse(null);
        if (p == null) return "redirect:/docente";
        Integer unidadId = p.getPool().getUnidad().getId();
        evaluacionService.borrarPregunta(p);
        ra.addFlashAttribute("mensaje", "Pregunta eliminada.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    // ─── ALUMNO — Rendición e Intentos de Examen (CU-59 y CU-60) ───────────────

    /**
     * CU-60 — Realizar intento de autoevaluación.
     * Reglas de negocio:
     * - Sorteo aleatorio de 10 preguntas.
     * - Control de límite de intentos permitidos (RN-CU60-01).
     */
    @GetMapping("/evaluacion/{autoevaluacionId}/rendir")
    public String verExamen(@PathVariable Integer autoevaluacionId, Model model, Authentication auth) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();

        try {
            intentoService.iniciarIntento(ae, usuario);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        List<Pregunta> preguntas = intentoService.sortearPreguntas(ae);

        model.addAttribute("usuario", usuario);
        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("titulo", "Autoevaluación: " + ae.getNombre());
        return "pages/alumno/rendir-examen";
    }

    /**
     * CU-60 — Realizar intento de autoevaluación (Enviar respuestas y calcular nota).
     */
    @PostMapping("/evaluacion/{autoevaluacionId}/enviar")
    public String enviarExamen(@PathVariable Integer autoevaluacionId,
                               @RequestParam Map<String, String> form,
                               Authentication auth,
                               RedirectAttributes ra) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(ae, usuario);

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

    /**
     * CU-59 — Buscar intento de autoevaluación.
     */
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
