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
 * TRAZABILIDAD — Controller para la gestión de Pools de Preguntas, Autoevaluaciones e Intentos.
 *
 * MOD-F-04: Módulo de Evaluación y Progreso
 *   CU-53 — Buscar pool              → GET /docente/unidad/{unidadId}/pool
 *   CU-54 — Crear pool               → POST /docente/unidad/{unidadId}/pool/crear
 *             Incluye la carga de preguntas de opción múltiple/V-F (CU-54 paso 5-8).
 *             POST /docente/pool/{poolId}/pregunta/agregar
 *   CU-55 — Modificar pool           → no implementado. FALTANTE.
 *   CU-56 — Dar de baja pool         → POST /docente/pregunta/{preguntaId}/borrar
 *             NOTA: implementa eliminación de pregunta, no del pool completo. PARCIAL.
 *   CU-57 — Buscar autoevaluación    → no implementado como endpoint separado. FALTANTE.
 *             (La autoevaluación se muestra desde el gestor de unidad del docente.)
 *   CU-58 — Crear autoevaluación     → no implementado. FALTANTE.
 *   CU-59 — Modificar autoevaluación → no implementado. FALTANTE.
 *   CU-60 — Dar de baja autoevaluación → no implementado. FALTANTE.
 *   CU-61 — Buscar intento           → GET /evaluacion/{autoevaluacionId}/resultado
 *   CU-62 — Realizar intento         → GET /evaluacion/{autoevaluacionId}/rendir  (vista)
 *                                       POST /evaluacion/{autoevaluacionId}/enviar (envío y corrección)
 *
 * NOTAS DE COBERTURA:
 *   CU-54 paso 3 (RN-CU52-01): un pool por unidad; validación implementada en la creación del pool.
 *   CU-62 paso 4: sorteo aleatorio de 10 preguntas (implementado en IntentoService.sortearPreguntas).
 *   CU-62 paso 5 (RN-CU60-01): control del límite de intentos implementado en IntentoService.iniciarIntento.
 *   EX-CU62-01: si se supera el límite de intentos → se muestra error via model.addAttribute("error").
 *
 * Los CU-55, 57, 58, 59, 60 son FALTANTES y deben implementarse en una versión futura.
 */
@Controller
public class EvaluacionController {

    @Autowired private EvaluacionService evaluacionService;
    @Autowired private IntentoService intentoService;
    @Autowired private UnidadServiceImpl unidadService;

    // ─── DOCENTE — Pools de Preguntas (CU-53 a CU-56) ─────────────────────────

    /**
     * TRAZABILIDAD: CU-53 — Buscar pool.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La unidad existe.
     * Flujo paso 4: recupera el pool de preguntas de la unidad (si existe) con todas sus preguntas.
     * NOTA PARCIAL: CU-53 especifica filtro por nombre. No implementado.
     */
    @GetMapping("/docente/unidad/{unidadId}/pool")
    public String verPool(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", unidad);
        // CU-53 paso 4: recupera el pool de la unidad. Si no existe, devuelve null (la vista mostrará el formulario de creación).
        model.addAttribute("pool", evaluacionService.buscarPoolPorUnidad(unidad).orElse(null));
        model.addAttribute("titulo", "Pool de Preguntas | " + unidad.getTitulo());
        return "pages/docente/gestionar-pool";
    }

    /**
     * TRAZABILIDAD: CU-54 — Crear pool.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La unidad existe. No existe un pool previo para la unidad.
     * Flujo paso 3: valida que no exista ya un pool para la unidad (RN-CU52-01).
     * Flujo paso 4: registra el pool con el nombre dado.
     * Postcondición: pool creado y asociado a la unidad.
     * EX-CU54-01 (RN-CU52-01): ya existe un pool → redirect con mensaje de excepción.
     */
    @PostMapping("/docente/unidad/{unidadId}/pool/crear")
    public String crearPool(@PathVariable Integer unidadId,
                            @RequestParam String nombre,
                            RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/docente";

        // CU-54 paso 3: verificar unicidad del pool por unidad.
        if (evaluacionService.buscarPoolPorUnidad(unidad).isPresent()) {
            ra.addFlashAttribute("mensaje", "EX-CU54-01: Esta unidad ya cuenta con un pool de preguntas registrado.");
            return "redirect:/docente/unidad/" + unidadId + "/pool";
        }
        Pool pool = new Pool(nombre, unidad);
        evaluacionService.guardarPool(pool);
        ra.addFlashAttribute("mensaje", "Pool creado correctamente.");
        return "redirect:/docente/unidad/" + unidadId + "/pool";
    }

    /**
     * TRAZABILIDAD: CU-54 — Crear pool (Cargar preguntas de opción múltiple / Verdadero-Falso).
     * Actor: Docente.
     * Precondición: el pool existe. El docente accede al formulario de carga de preguntas.
     * Flujo paso 5-8: registra la pregunta con su texto, tipo (opción múltiple/V-F) y opciones de respuesta,
     *   marcando la opción correcta.
     * Postcondición: pregunta registrada con sus opciones.
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
     * TRAZABILIDAD: CU-56 — Dar de baja pool (parcial — elimina una pregunta individual del pool).
     * Actor: Docente.
     * Precondición: el pool existe. La pregunta pertenece al pool.
     * Flujo paso 4: elimina la pregunta del pool.
     * Postcondición: pregunta eliminada del pool.
     * NOTA PARCIAL: CU-56 especifica dar de baja el pool completo. Esta acción solo elimina
     *   una pregunta individual. La eliminación del pool completo no está implementada.
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

    // ─── ALUMNO — Rendición e Intentos de Autoevaluación (CU-61 y CU-62) ────

    /**
     * TRAZABILIDAD: CU-62 — Realizar intento de autoevaluación (vista del examen).
     * Actor: Alumno.
     * Precondición: sesión con rol Alumno. La autoevaluación existe y está activa.
     * Flujo paso 3: valida el límite de intentos (RN-CU60-01). Error si se superó el límite.
     * Flujo paso 4: sortea aleatoriamente 10 preguntas del pool de la autoevaluación.
     * Flujo paso 5: muestra las preguntas al alumno para que elija sus respuestas.
     * EX-CU62-01: si se superó el límite de intentos → se muestra error en la vista.
     */
    @GetMapping("/evaluacion/{autoevaluacionId}/rendir")
    public String verExamen(@PathVariable Integer autoevaluacionId, Model model, Authentication auth) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();

        try {
            // CU-62 paso 3: valida el límite de intentos (RN-CU60-01).
            intentoService.iniciarIntento(ae, usuario);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        // CU-62 paso 4: sorteo aleatorio de 10 preguntas del pool.
        List<Pregunta> preguntas = intentoService.sortearPreguntas(ae);

        model.addAttribute("usuario", usuario);
        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("preguntas", preguntas);
        model.addAttribute("titulo", "Autoevaluación: " + ae.getNombre());
        return "pages/alumno/rendir-examen";
    }

    /**
     * TRAZABILIDAD: CU-62 — Realizar intento de autoevaluación (Enviar respuestas y calcular nota).
     * Actor: Alumno.
     * Precondición: el alumno completó la selección de respuestas.
     * Flujo paso 6: recibe las respuestas del formulario (pregunta_N → opcionId).
     * Flujo paso 7: corrige las respuestas y calcula la nota (IntentoService.corregirYGuardar).
     * Flujo paso 8: guarda el intento con nota y estado (Aprobado/Desaprobado).
     * Postcondición: intento registrado con nota calculada.
     */
    @PostMapping("/evaluacion/{autoevaluacionId}/enviar")
    public String enviarExamen(@PathVariable Integer autoevaluacionId,
                               @RequestParam Map<String, String> form,
                               Authentication auth,
                               RedirectAttributes ra) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(ae);

        // CU-62 paso 6: parsea las respuestas del formulario (pregunta_N=opcionId).
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

        // CU-62 paso 7-8: corrige respuestas, calcula nota y guarda el intento.
        IntentoAutoevaluacion resultado = intentoService.corregirYGuardar(intento, respuestas);
        ra.addFlashAttribute("intentoId", resultado.getId());
        ra.addFlashAttribute("nota", resultado.getNota());
        ra.addFlashAttribute("aprobado", intentoService.estaAprobado(resultado));
        return "redirect:/evaluacion/" + autoevaluacionId + "/resultado";
    }

    /**
     * TRAZABILIDAD: CU-61 — Buscar intento de autoevaluación.
     * Actor: Alumno.
     * Precondición: sesión con rol Alumno. Existe al menos un intento previo.
     * Flujo paso 4-5: recupera y muestra el historial de intentos del alumno para la autoevaluación.
     * Incluye nota de cada intento y estado (Aprobado/Desaprobado).
     * NOTA PARCIAL: CU-61 especifica filtros por nota y fecha. No implementados.
     */
    @GetMapping("/evaluacion/{autoevaluacionId}/resultado")
    public String verResultado(@PathVariable Integer autoevaluacionId, Model model, Authentication auth) {
        Autoevaluacion ae = evaluacionService.buscarAutoevaluacionPorId(autoevaluacionId).orElse(null);
        if (ae == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        // CU-61 paso 4: recupera el historial de intentos del alumno para esta autoevaluación.
        List<IntentoAutoevaluacion> historial = intentoService.historialPorAlumno(ae, usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("autoevaluacion", ae);
        model.addAttribute("historial", historial);
        model.addAttribute("titulo", "Resultado | " + ae.getNombre());
        return "pages/alumno/resultado-examen";
    }
}
