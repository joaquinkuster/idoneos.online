package com.app.idoneos.controlador;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.PreguntaIntento;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.IntentoAutoevaluacion.IntentoAutoevaluacionServicio;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Controlador de los intentos de autoevaluación del alumno (MOD-F-04, CU-70 Realizar intento de autoevaluación):
 * iniciar (POST /intento/iniciar/{autoevaluacionId}), el cuestionario (GET /intento/realizar/{id}), guardar cada
 * respuesta (POST /intento/responder/{id}), marcar para revisar (POST /intento/marcar/{id}), avanzar en un cuestionario
 * secuencial (POST /intento/avanzar/{id}), el resumen previo a la entrega (GET /intento/resumen/{id}), la entrega
 * (POST /intento/entregar/{id}) y la sincronización del tiempo (GET /intento/tiempo/{id}).
 *
 * El tiempo lo manda el servidor: el navegador solo lo muestra y se resincroniza con cada respuesta.
 */
@Controller
@RequestMapping("/intento")
public class IntentoAutoevaluacionControlador {

    @Autowired
    private IntentoAutoevaluacionServicio intentoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-70: Muestra el cuestionario, de a una pregunta. Con navegación libre el alumno elige la pregunta; en un
     * cuestionario secuencial siempre se muestra la actual.
     *
     * @param id                 Identificador del intento.
     * @param pregunta           Número de la pregunta (solo con navegación libre).
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del cuestionario, o una redirección si el intento ya no está en curso.
     */
    @GetMapping("/realizar/{id}")
    public String realizar(@PathVariable("id") Integer id,
            @RequestParam(value = "pregunta", required = false) Integer pregunta, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            IntentoAutoevaluacion intento = intentoServicio.buscarPropio(id, alumnoDe(auth));
            Autoevaluacion autoevaluacion = intento.getAutoevaluacion();
            if (!intento.estaEnCurso()) {
                redirectAttributes.addFlashAttribute("mensaje", "El intento ya terminó.");
                return "redirect:/autoevaluacion/ver/" + autoevaluacion.getIdAutoevaluacion();
            }
            List<PreguntaIntento> preguntas = intento.getPreguntasOrdenadas();
            PreguntaIntento actual;
            if (autoevaluacion.getNavegacionLibre()) {
                int numero = Math.min(Math.max(pregunta == null ? 1 : pregunta, 1), preguntas.size());
                actual = preguntas.get(numero - 1);
            } else {
                actual = intento.getPreguntaActualSecuencial();
                if (actual == null) {
                    return "redirect:/intento/resumen/" + id; // ya pasó por todas: solo falta entregar
                }
                if (pregunta != null) {
                    return "redirect:/intento/realizar/" + id;
                }
            }
            double valorPregunta = autoevaluacion.getValorDeCadaPregunta();
            int correctas = Math.max(1, actual.getPregunta().getCantidadOpcionesCorrectas());
            cargarIntento(modelo, intento);
            modelo.addAttribute("actual", actual);
            modelo.addAttribute("preguntas", preguntas);
            modelo.addAttribute("elegidas", actual.getRespuestasIntento().stream()
                    .map(r -> r.getOpcionRespuesta().getIdOpcionRespuesta()).collect(Collectors.toSet()));
            modelo.addAttribute("valorPregunta", formatear(valorPregunta));
            modelo.addAttribute("valorOpcion", formatear(valorPregunta / correctas));
            modelo.addAttribute("hayAnterior", autoevaluacion.getNavegacionLibre() && actual.getOrden() > 1);
            modelo.addAttribute("esUltima", actual.getOrden() == preguntas.size());
            modelo.addAttribute("faltanPorPasar", preguntas.stream().filter(p -> !p.getPasada()).count());
            modelo.addAttribute("titulo", "Autoevaluación | Idóneos Online");
            return "pages/intento/realizar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }

    /**
     * CU-70 (paso 7): Muestra el resumen del intento: qué preguntas respondió, cuáles no y cuáles marcó.
     *
     * @param id                 Identificador del intento.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del resumen, o una redirección si el intento ya no está en curso.
     */
    @GetMapping("/resumen/{id}")
    public String resumen(@PathVariable("id") Integer id, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            IntentoAutoevaluacion intento = intentoServicio.buscarPropio(id, alumnoDe(auth));
            if (!intento.estaEnCurso()) {
                redirectAttributes.addFlashAttribute("mensaje", "El intento ya terminó.");
                return "redirect:/autoevaluacion/ver/" + intento.getAutoevaluacion().getIdAutoevaluacion();
            }
            List<PreguntaIntento> preguntas = intento.getPreguntasOrdenadas();
            cargarIntento(modelo, intento);
            modelo.addAttribute("preguntas", preguntas);
            modelo.addAttribute("sinResponder", preguntas.size() - intento.getCantidadRespondidas());
            // "Volver al intento": la primera sin responder (navegación libre) o la actual (secuencial)
            PreguntaIntento destino = intento.getAutoevaluacion().getNavegacionLibre()
                    ? preguntas.stream().filter(p -> !p.estaRespondida()).findFirst().orElse(preguntas.get(0))
                    : intento.getPreguntaActualSecuencial();
            modelo.addAttribute("volverA", destino);
            modelo.addAttribute("titulo", "Resumen del intento | Idóneos Online");
            return "pages/intento/resumen";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/misCursos";
        }
    }

    /**
     * Informa los segundos que le quedan al intento según el reloj del servidor: el navegador lo usa para
     * resincronizar su temporizador (al volver a la pestaña y cada tanto). Responde en JSON.
     *
     * @param id   Identificador del intento.
     * @param auth La autenticación actual.
     * @return Los segundos restantes (vacío si no hay tiempo límite) y si el intento sigue en curso.
     */
    @GetMapping("/tiempo/{id}")
    public ResponseEntity<Map<String, Object>> tiempo(@PathVariable("id") Integer id, Authentication auth) {
        try {
            return RespuestaFormularioUtilidad.respuestaConDatos(estado(intentoServicio.buscarPropio(id, alumnoDe(auth))));
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConErrorEnDatos(e);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-70 (pasos 2 a 4): Inicia un intento (o continúa el que está en curso) y abre el cuestionario.
     *
     * @param idAutoevaluacion   Identificador de la autoevaluación.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al cuestionario o, si no puede rendir, a la ficha de la autoevaluación con el motivo.
     */
    @PostMapping("/iniciar/{idAutoevaluacion}")
    public String iniciar(@PathVariable("idAutoevaluacion") Integer idAutoevaluacion, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            IntentoAutoevaluacion intento = intentoServicio.iniciarIntento(alumnoDe(auth), idAutoevaluacion);
            return "redirect:/intento/realizar/" + intento.getIdIntentoAutoevaluacion();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/autoevaluacion/ver/" + idAutoevaluacion;
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-70 (paso 6): Guarda la elección del alumno para una pregunta (sin opciones, la deja sin responder). Responde en
     * JSON con el tiempo que queda según el servidor.
     *
     * @param id                Identificador del intento.
     * @param preguntaIntentoId Identificador de la pregunta del intento.
     * @param opcionIds         Las opciones elegidas.
     * @param auth              La autenticación actual.
     * @return El estado del intento o el mensaje de error.
     */
    @PostMapping("/responder/{id}")
    public ResponseEntity<Map<String, Object>> responder(@PathVariable("id") Integer id,
            @RequestParam(value = "preguntaIntentoId", required = false) Integer preguntaIntentoId,
            @RequestParam(value = "opcionIds", required = false) List<Integer> opcionIds, Authentication auth) {
        try {
            return RespuestaFormularioUtilidad.respuestaConDatos(estado(
                    intentoServicio.responderPregunta(id, alumnoDe(auth), preguntaIntentoId, opcionIds)));
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConErrorEnDatos(e);
        }
    }

    /**
     * Marca o desmarca una pregunta para revisarla antes de entregar (solo con navegación libre). Responde en JSON.
     *
     * @param id                Identificador del intento.
     * @param preguntaIntentoId Identificador de la pregunta del intento.
     * @param marcada           Si queda marcada.
     * @param auth              La autenticación actual.
     * @return El estado del intento o el mensaje de error.
     */
    @PostMapping("/marcar/{id}")
    public ResponseEntity<Map<String, Object>> marcar(@PathVariable("id") Integer id,
            @RequestParam(value = "preguntaIntentoId", required = false) Integer preguntaIntentoId,
            @RequestParam(value = "marcada", defaultValue = "false") boolean marcada, Authentication auth) {
        try {
            return RespuestaFormularioUtilidad.respuestaConDatos(estado(
                    intentoServicio.marcarPregunta(id, alumnoDe(auth), preguntaIntentoId, marcada)));
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConErrorEnDatos(e);
        }
    }

    /**
     * En un cuestionario secuencial, pasa a la pregunta siguiente. Responde en JSON con el destino (la pregunta siguiente o,
     * si no quedan, el resumen).
     *
     * @param id   Identificador del intento.
     * @param auth La autenticación actual.
     * @return El estado del intento con el destino, o el mensaje de error.
     */
    @PostMapping("/avanzar/{id}")
    public ResponseEntity<Map<String, Object>> avanzar(@PathVariable("id") Integer id, Authentication auth) {
        try {
            IntentoAutoevaluacion intento = intentoServicio.avanzarPregunta(id, alumnoDe(auth));
            Map<String, Object> datos = estado(intento);
            datos.put("destino", intento.getPreguntaActualSecuencial() == null ? "/intento/resumen/" + id
                    : "/intento/realizar/" + id);
            return RespuestaFormularioUtilidad.respuestaConDatos(datos);
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConErrorEnDatos(e);
        }
    }

    /**
     * CU-70 (pasos 8 a 12): Entrega el intento, lo corrige y devuelve la nota y el resultado. Responde en JSON con el
     * mensaje y el destino (la ficha de la autoevaluación).
     *
     * @param id            Identificador del intento.
     * @param tiempoAgotado Si el navegador entrega porque se agotó el tiempo (solo cambia el mensaje).
     * @param auth          La autenticación actual.
     * @return El mensaje con la nota, el resultado y el destino, o el mensaje de error.
     */
    @PostMapping("/entregar/{id}")
    public ResponseEntity<Map<String, Object>> entregar(@PathVariable("id") Integer id,
            @RequestParam(value = "tiempoAgotado", defaultValue = "false") boolean tiempoAgotado, Authentication auth) {
        try {
            IntentoAutoevaluacion intento = intentoServicio.entregarIntento(id, alumnoDe(auth));
            String nota = formatear(intento.getNota() == null ? 0 : intento.getNota());
            String resultado = intento.getAprobado()
                    ? "Tu nota fue " + nota + " / 10: aprobaste la autoevaluación."
                    : "Tu nota fue " + nota + " / 10: no alcanzaste la nota mínima. Debés reintentar el cuestionario completo.";
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("aprobado", intento.getAprobado());
            datos.put("titulo", tiempoAgotado ? "Se agotó el tiempo" : "Intento entregado");
            datos.put("mensaje", (tiempoAgotado ? "Tu intento se corrigió con las respuestas guardadas. " : "") + resultado);
            datos.put("destino", "/autoevaluacion/ver/" + intento.getAutoevaluacion().getIdAutoevaluacion());
            return RespuestaFormularioUtilidad.respuestaConDatos(datos);
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConErrorEnDatos(e);
        }
    }

    // ---------------------------------------------------------------- Auxiliares

    private Alumno alumnoDe(Authentication auth) {
        Alumno alumno = ((Usuario) auth.getPrincipal()).getAlumno();
        if (alumno == null) {
            throw new IllegalArgumentException("Error! Solo los alumnos pueden realizar autoevaluaciones.");
        }
        return alumno;
    }

    /** Lo que comparten el cuestionario y el resumen: el intento, su autoevaluación, el curso, y el tiempo. */
    private void cargarIntento(Model modelo, IntentoAutoevaluacion intento) {
        Autoevaluacion autoevaluacion = intento.getAutoevaluacion();
        modelo.addAttribute("intento", intento);
        modelo.addAttribute("autoevaluacion", autoevaluacion);
        modelo.addAttribute("curso", autoevaluacion.getUnidad().getCurso());
        modelo.addAttribute("unidad", autoevaluacion.getUnidad());
        modelo.addAttribute("libre", autoevaluacion.getNavegacionLibre());
        modelo.addAttribute("segundosRestantes", intento.getSegundosRestantes(java.time.LocalDateTime.now()));
        modelo.addAttribute("total", intento.getPreguntasIntento().size());
        modelo.addAttribute("respondidas", intento.getCantidadRespondidas());
    }

    /** El estado del intento para el navegador: tiempo restante según el servidor y cuántas preguntas lleva. */
    private Map<String, Object> estado(IntentoAutoevaluacion intento) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("enCurso", intento.estaEnCurso());
        datos.put("segundosRestantes", intento.getSegundosRestantes(java.time.LocalDateTime.now()));
        datos.put("respondidas", intento.getCantidadRespondidas());
        datos.put("marcadas", intento.getCantidadMarcadas());
        Set<Integer> respondidas = intento.getPreguntasIntento().stream().filter(PreguntaIntento::estaRespondida)
                .map(PreguntaIntento::getOrden).collect(Collectors.toSet());
        datos.put("ordenesRespondidas", respondidas);
        return datos;
    }

    /** Un número con coma decimal y dos decimales, como se muestran las notas ("7,50"). */
    private String formatear(double valor) {
        return String.format(Locale.forLanguageTag("es-AR"), "%.2f", valor);
    }
}
