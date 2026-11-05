package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.service.inscripcion.InscripcionService;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.model.PreguntaIntento;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.intentoautoevaluacion.IntentoAutoevaluacionService;
import com.app.idoneos.service.progreso.ProgresoService;
import com.app.idoneos.util.PaginacionUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Controlador de los intentos de autoevaluación (MOD-F-04).
 * <ul>
 * <li>El alumno rinde (CU-70): iniciar (POST /intento/iniciar/{autoevaluacionId}), el cuestionario (GET /intento/realizar/{id}),
 * guardar cada respuesta (POST /intento/responder/{id}), marcar para revisar (POST /intento/marcar/{id}), avanzar en un
 * cuestionario secuencial (POST /intento/avanzar/{id}), el resumen previo a la entrega (GET /intento/resumen/{id}), la
 * entrega (POST /intento/entregar/{id}) y la sincronización del tiempo (GET /intento/tiempo/{id}). El tiempo lo manda el
 * servidor: el navegador solo lo muestra y se resincroniza con cada respuesta.</li>
 * <li>El docente y el administrador hacen el seguimiento: CU-68 Buscar intentos (GET /intento/buscar), CU-71 Aprobar intento
 * (POST /intento/aprobar/{id}, docente) y CU-72 Dar de baja intento (POST /intento/darDeBaja/{id}, administrador).</li>
 * <li>Calificaciones (CU-69): el docente y el administrador ven una fila por alumno y autoevaluación (GET
 * /intento/buscarPorAutoevaluacion); el alumno ve las suyas (GET /intento/buscarPorInscripcion/{idInscripcion}).</li>
 * </ul>
 */
@Controller
@RequestMapping("/intento")
public class IntentoController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private IntentoAutoevaluacionService intentoService;

    @Autowired
    private ProgresoService progresoService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-70: Muestra el cuestionario, de a una pregunta. Con navegación libre el alumno elige la pregunta; en un
     * cuestionario secuencial siempre se muestra la actual.
     *
     * @param id                 Identificador del intento.
     * @param pregunta           Número de la pregunta (solo con navegación libre).
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del cuestionario, o una redirección si el intento ya no está en curso.
     */
    @GetMapping("/realizar/{id}")
    public String realizar(@PathVariable("id") Integer id,
            @RequestParam(value = "pregunta", required = false) Integer pregunta, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            IntentoAutoevaluacion intento = intentoService.buscarPropio(id, alumnoDe(authentication));
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
            cargar(model, intento);
            model.addAttribute("actual", actual);
            model.addAttribute("preguntas", preguntas);
            model.addAttribute("elegidas", actual.getRespuestasIntento().stream()
                    .map(r -> r.getOpcionRespuesta().getIdOpcionRespuesta()).collect(Collectors.toSet()));
            model.addAttribute("valorPregunta", formatear(valorPregunta));
            model.addAttribute("valorOpcion", formatear(valorPregunta / correctas));
            model.addAttribute("hayAnterior", autoevaluacion.getNavegacionLibre() && actual.getOrden() > 1);
            model.addAttribute("esUltima", actual.getOrden() == preguntas.size());
            model.addAttribute("faltanPorPasar", preguntas.stream().filter(p -> !p.getPasada()).count());
            model.addAttribute("titulo", "Autoevaluación | Idóneos Online");
            return "pages/intento/realizar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/buscar";
        }
    }

    /**
     * CU-70 (paso 7): Muestra el resumen del intento: qué preguntas respondió, cuáles no y cuáles marcó.
     *
     * @param id                 Identificador del intento.
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del resumen, o una redirección si el intento ya no está en curso.
     */
    @GetMapping("/resumen/{id}")
    public String resumen(@PathVariable("id") Integer id, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            IntentoAutoevaluacion intento = intentoService.buscarPropio(id, alumnoDe(authentication));
            if (!intento.estaEnCurso()) {
                redirectAttributes.addFlashAttribute("mensaje", "El intento ya terminó.");
                return "redirect:/autoevaluacion/ver/" + intento.getAutoevaluacion().getIdAutoevaluacion();
            }
            List<PreguntaIntento> preguntas = intento.getPreguntasOrdenadas();
            cargar(model, intento);
            model.addAttribute("preguntas", preguntas);
            model.addAttribute("sinResponder", preguntas.size() - intento.getCantidadRespondidas());
            // "Volver al intento": la primera sin responder (navegación libre) o la actual (secuencial)
            PreguntaIntento destino = intento.getAutoevaluacion().getNavegacionLibre()
                    ? preguntas.stream().filter(p -> !p.estaRespondida()).findFirst().orElse(preguntas.get(0))
                    : intento.getPreguntaActualSecuencial();
            model.addAttribute("volverA", destino);
            model.addAttribute("titulo", "Resumen del intento | Idóneos Online");
            return "pages/intento/resumen";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/buscar";
        }
    }

    /**
     * Informa los segundos que le quedan al intento según el reloj del servidor: el navegador lo usa para
     * resincronizar su temporizador (al volver a la pestaña y cada tanto). Responde en JSON.
     *
     * @param id   Identificador del intento.
     * @param authentication La autenticación actual.
     * @return Los segundos restantes (vacío si no hay tiempo límite) y si el intento sigue en curso.
     */
    @GetMapping("/tiempo/{id}")
    public ResponseEntity<Map<String, Object>> tiempo(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            return RespuestaFormularioUtil.armarRespuestaConDatos(estado(intentoService.buscarPropio(id, alumnoDe(authentication))));
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-70 (pasos 2 a 4): Inicia un intento (o continúa el que está en curso) y abre el cuestionario.
     *
     * @param idAutoevaluacion   Identificador de la autoevaluación.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al cuestionario o, si no puede rendir, a la ficha de la autoevaluación con el motivo.
     */
    @PostMapping("/iniciar/{idAutoevaluacion}")
    public String iniciar(@PathVariable("idAutoevaluacion") Integer idAutoevaluacion, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            IntentoAutoevaluacion intento = intentoService.iniciar(alumnoDe(authentication), idAutoevaluacion);
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
     * @param authentication              La autenticación actual.
     * @return El estado del intento o el mensaje de error.
     */
    @PostMapping("/responder/{id}")
    public ResponseEntity<Map<String, Object>> responder(@PathVariable("id") Integer id,
            @RequestParam(value = "preguntaIntentoId", required = false) Integer preguntaIntentoId,
            @RequestParam(value = "opcionIds", required = false) List<Integer> opcionIds, Authentication authentication) {
        try {
            return RespuestaFormularioUtil.armarRespuestaConDatos(estado(
                    intentoService.responderPregunta(id, alumnoDe(authentication), preguntaIntentoId, opcionIds)));
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    /**
     * Marca o desmarca una pregunta para revisarla antes de entregar (solo con navegación libre). Responde en JSON.
     *
     * @param id                Identificador del intento.
     * @param preguntaIntentoId Identificador de la pregunta del intento.
     * @param marcada           Si queda marcada.
     * @param authentication              La autenticación actual.
     * @return El estado del intento o el mensaje de error.
     */
    @PostMapping("/marcar/{id}")
    public ResponseEntity<Map<String, Object>> marcar(@PathVariable("id") Integer id,
            @RequestParam(value = "preguntaIntentoId", required = false) Integer preguntaIntentoId,
            @RequestParam(value = "marcada", defaultValue = "false") boolean marcada, Authentication authentication) {
        try {
            return RespuestaFormularioUtil.armarRespuestaConDatos(estado(
                    intentoService.marcarPregunta(id, alumnoDe(authentication), preguntaIntentoId, marcada)));
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    /**
     * En un cuestionario secuencial, pasa a la pregunta siguiente. Responde en JSON con el destino (la pregunta siguiente o,
     * si no quedan, el resumen).
     *
     * @param id   Identificador del intento.
     * @param authentication La autenticación actual.
     * @return El estado del intento con el destino, o el mensaje de error.
     */
    @PostMapping("/avanzar/{id}")
    public ResponseEntity<Map<String, Object>> avanzar(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            IntentoAutoevaluacion intento = intentoService.avanzarPregunta(id, alumnoDe(authentication));
            Map<String, Object> datos = estado(intento);
            datos.put("destino", intento.getPreguntaActualSecuencial() == null ? "/intento/resumen/" + id
                    : "/intento/realizar/" + id);
            return RespuestaFormularioUtil.armarRespuestaConDatos(datos);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    /**
     * CU-70 (pasos 8 a 12): Entrega el intento, lo corrige y devuelve la nota y el resultado. Responde en JSON con el
     * mensaje y el destino (la ficha de la autoevaluación).
     *
     * @param id            Identificador del intento.
     * @param tiempoAgotado Si el navegador entrega porque se agotó el tiempo (solo cambia el mensaje).
     * @param authentication          La autenticación actual.
     * @return El mensaje con la nota, el resultado y el destino, o el mensaje de error.
     */
    @PostMapping("/entregar/{id}")
    public ResponseEntity<Map<String, Object>> entregar(@PathVariable("id") Integer id,
            @RequestParam(value = "tiempoAgotado", defaultValue = "false") boolean tiempoAgotado, Authentication authentication) {
        try {
            IntentoAutoevaluacion intento = intentoService.entregar(id, alumnoDe(authentication));
            String nota = formatear(intento.getNota() == null ? 0 : intento.getNota());
            String resultado = intento.getAprobado()
                    ? "Tu nota fue " + nota + " / 10: aprobaste la autoevaluación."
                    : "Tu nota fue " + nota + " / 10: no alcanzaste la nota mínima. Debés reintentar el cuestionario completo.";
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("aprobado", intento.getAprobado());
            datos.put("titulo", tiempoAgotado ? "Se agotó el tiempo" : "Intento entregado");
            datos.put("mensaje", (tiempoAgotado ? "Tu intento se corrigió con las respuestas guardadas. " : "") + resultado);
            // Si con este intento se completó el último contenido del curso, se emitió el certificado: la ficha lo avisa
            Certificado certificado = intento.getInscripcion().getCertificadoVigente();
            boolean cursoFinalizado = certificado != null && !certificado.getFechaCreacion().isBefore(intento.getFechaCreacion());
            datos.put("cursoFinalizado", cursoFinalizado);
            datos.put("destino", "/autoevaluacion/ver/" + intento.getAutoevaluacion().getIdAutoevaluacion()
                    + (cursoFinalizado ? "?cursoFinalizado" : ""));
            return RespuestaFormularioUtil.armarRespuestaConDatos(datos);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConErrorEnDatos(e);
        }
    }

    // ---------------------------------------------------------------- Auxiliares

    private Alumno alumnoDe(Authentication authentication) {
        Alumno alumno = ((Usuario) authentication.getPrincipal()).getAlumno();
        if (alumno == null) {
            throw new IllegalArgumentException("Error! Solo los alumnos pueden realizar autoevaluaciones.");
        }
        return alumno;
    }

    /** Lo que comparten el cuestionario y el resumen: el intento, su autoevaluación, el curso, y el tiempo. */
    private void cargar(Model model, IntentoAutoevaluacion intento) {
        Autoevaluacion autoevaluacion = intento.getAutoevaluacion();
        model.addAttribute("intento", intento);
        model.addAttribute("autoevaluacion", autoevaluacion);
        model.addAttribute("curso", autoevaluacion.getTema().getCurso());
        model.addAttribute("tema", autoevaluacion.getTema());
        model.addAttribute("libre", autoevaluacion.getNavegacionLibre());
        model.addAttribute("segundosRestantes", intento.getSegundosRestantes(java.time.LocalDateTime.now()));
        model.addAttribute("total", intento.getPreguntasIntento().size());
        model.addAttribute("respondidas", intento.getCantidadRespondidas());
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

    /**
     * Un intento del listado con su número entre los del mismo alumno en la misma autoevaluación.
     *
     * @param intento el intento
     * @param numero  su número de orden (1 es el primero)
     */
    public record FilaIntento(IntentoAutoevaluacion intento, int numero) {
    }

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-68: Lista los intentos de autoevaluación de los alumnos del curso, de todas sus cohortes. Los filtros se aplican
     * en la pantalla; el docente entra con el filtro en su cohorte de trabajo.
     *
     * @param cursoId            Identificador del curso.
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a la lista de cursos si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
            List<IntentoAutoevaluacion> intentos = intentoService.buscarDelCurso(cursoId,
                    contexto.esAdministrador() ? null : usuario.getDocente());

            // Número de cada intento entre los del mismo alumno en la misma autoevaluación (del más reciente al más antiguo)
            Map<String, Integer> contadores = new HashMap<>();
            List<FilaIntento> filas = new ArrayList<>();
            for (IntentoAutoevaluacion intento : intentos) {
                String clave = intento.getInscripcion().getIdInscripcion() + "-"
                        + intento.getAutoevaluacion().getIdAutoevaluacion();
                filas.add(new FilaIntento(intento, contadores.merge(clave, 1, Integer::sum)));
            }
            filas = filas.reversed();

            Integer cohorteSeleccionada = contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte();
            // Entra con el filtro en la cohorte de trabajo; si el navegador pide otra página o cambia el filtro, manda su propia elección
            java.util.Map<String, String[]> parametros = new java.util.HashMap<>(request.getParameterMap());
            if (!PaginacionUtil.pideSoloFilas(request) && !parametros.containsKey("cohorte") && cohorteSeleccionada != null) {
                parametros.put("cohorte", new String[] { String.valueOf(cohorteSeleccionada) });
            }
            // El filtro, el orden por columna y la página los resuelve el servidor
            PaginacionUtil.Pagina<FilaIntento> pagina = Filtros.tabla.paginar(filas, parametros);
            PaginacionUtil.cargarEnModelo(model, pagina);

            panel.cargarContexto(model, contexto, "intentos");
            model.addAttribute("filas", pagina.filas());
            model.addAttribute("puedeAprobar", !contexto.esAdministrador());
            model.addAttribute("puedeDarDeBaja", contexto.esAdministrador());
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/intento/buscar :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("cohorteSeleccionada", cohorteSeleccionada);
            model.addAttribute("cohortes", contexto.curso().getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            model.addAttribute("puedeAprobar", !contexto.esAdministrador());
            model.addAttribute("puedeDarDeBaja", contexto.esAdministrador());
            model.addAttribute("titulo", "Intentos | Idóneos Online");
            return "pages/intento/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-71: El docente aprueba un intento entregado que no alcanzó la nota mínima. Responde en JSON.
     *
     * @param id   Identificador del intento.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/aprobar/{id}")
    public ResponseEntity<Map<String, String>> aprobar(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            intentoService.aprobarPorDocente(id, usuario.getDocente());
            return RespuestaFormularioUtil.armarRespuestaExitosa("Intento aprobado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-72: El administrador da de baja un intento. Responde en JSON.
     *
     * @param id Identificador del intento.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            boolean revirtio = intentoService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa(revirtio
                    ? "Intento dado de baja con éxito. El alumno ya no aprueba el tema: volvió a estar pendiente y se anuló su certificado, si tenía."
                    : "Intento dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * Una fila del listado: la calificación de un alumno en una autoevaluación.
     *
     * @param inscripcion   la inscripción del alumno
     * @param autoevaluacion la autoevaluación
     * @param numeroTema  el número del tema en el cronograma del programa
     * @param habilitada    si el alumno ya puede acceder al tema
     */
    public record FilaCalificacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion, int numeroTema, boolean habilitada) {
    }

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-69: Listado de calificaciones del curso para el docente y el administrador: una fila por alumno inscripto y
     * autoevaluación. El docente entra con el filtro en su cohorte de trabajo; el administrador ve todas.
     *
     * @param cursoId            Identificador del curso.
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a la lista de cursos si no se puede acceder.
     */
    @GetMapping("/buscarPorAutoevaluacion")
    public String buscarPorAutoevaluacion(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
            List<FilaCalificacion> filas = new ArrayList<>();
            for (Inscripcion inscripcion : progresoService.buscarConFiltros(contexto.curso().getIdCurso(), null)) {
                filas.addAll(filasDe(inscripcion));
            }

            Integer cohorteSeleccionada = contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte();
            // Entra con el filtro en la cohorte de trabajo; si el navegador pide otra página o cambia el filtro, manda su propia elección
            java.util.Map<String, String[]> parametros = new java.util.HashMap<>(request.getParameterMap());
            if (!PaginacionUtil.pideSoloFilas(request) && !parametros.containsKey("cohorte") && cohorteSeleccionada != null) {
                parametros.put("cohorte", new String[] { String.valueOf(cohorteSeleccionada) });
            }
            // El filtro, el orden por columna y la página los resuelve el servidor
            PaginacionUtil.Pagina<FilaCalificacion> pagina = Filtros.tablaCalificaciones.paginar(filas, parametros);
            PaginacionUtil.cargarEnModelo(model, pagina);

            panel.cargarContexto(model, contexto, "calificaciones");
            model.addAttribute("filas", pagina.filas());
            model.addAttribute("verAlumnos", true);
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/intento/listado :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("urlListado", "/intento/buscarPorAutoevaluacion?cursoId=" + contexto.curso().getIdCurso());
            model.addAttribute("cohorteSeleccionada", cohorteSeleccionada);
            model.addAttribute("cohortes", contexto.curso().getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            model.addAttribute("titulo", "Calificaciones | Idóneos Online");
            return "pages/intento/listado";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * CU-69: Calificaciones del alumno en un curso: para cada autoevaluación visible de su programa, sus intentos, su mejor
     * nota y si la aprobó. Solo ve las suyas.
     *
     * @param idInscripcion      Identificador de la inscripción del alumno.
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de las calificaciones, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscarPorInscripcion/{idInscripcion}")
    public String buscarPorInscripcion(@PathVariable("idInscripcion") int idInscripcion, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            Inscripcion inscripcion = inscripcionService.validarAcceso(idInscripcion, alumno);
            panel.cargarContexto(model, ContextoCurso.deAlumno(inscripcion), "calificaciones");
            List<FilaCalificacion> todas = filasDe(inscripcion);
            PaginacionUtil.Pagina<FilaCalificacion> pagina = Filtros.tablaCalificaciones.paginar(todas, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            model.addAttribute("filas", pagina.filas());
            model.addAttribute("verAlumnos", false);
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/intento/listado :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("urlListado", "/intento/buscarPorInscripcion/" + inscripcion.getIdInscripcion());
            model.addAttribute("cohortes", List.of());
            model.addAttribute("titulo", "Calificaciones | Idóneos Online");
            return "pages/intento/listado";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/buscar";
        }
    }

    // ---------------------------------------------------------------- Auxiliares

    /** Las filas de una inscripción: sus autoevaluaciones visibles, tema por tema, en el orden del cronograma. */
    private List<FilaCalificacion> filasDe(Inscripcion inscripcion) {
        List<FilaCalificacion> filas = new ArrayList<>();
        for (TemaCronograma cronograma : inscripcion.getCohorte().getPrograma().getCronogramaOrdenado()) {
            boolean habilitada = inscripcion.estaTemaHabilitada(cronograma);
            for (Autoevaluacion autoevaluacion : cronograma.getTema().getAutoevaluacionesPublicadas()) {
                filas.add(new FilaCalificacion(inscripcion, autoevaluacion, cronograma.getNumeroOrden(), habilitada));
            }
        }
        return filas;
    }

    // ---------------------------------------------------------------- Listado (filtros y página en el servidor)



    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado (filtros y página en el servidor)


        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<FilaIntento> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<FilaIntento>("alumno", FiltroUtil.Tipo.TEXTO,
                        f -> f.intento().getInscripcion().getAlumno().getUsuario().getNombreCompleto() + " "
                                + f.intento().getInscripcion().getAlumno().getUsuario().getDni() + " "
                                + f.intento().getInscripcion().getAlumno().getUsuario().getCorreo()),
                new FiltroUtil.Columna<FilaIntento>("cohorte", FiltroUtil.Tipo.LISTA,
                        f -> String.valueOf(f.intento().getInscripcion().getCohorte().getIdCohorte())),
                new FiltroUtil.Columna<FilaIntento>("autoevaluacion", FiltroUtil.Tipo.LISTA,
                        f -> f.intento().getAutoevaluacion().getNombre()),
                new FiltroUtil.Columna<FilaIntento>("numero", FiltroUtil.Tipo.NUMERO, f -> String.valueOf(f.numero())),
                new FiltroUtil.Columna<FilaIntento>("inicio", FiltroUtil.Tipo.FECHA, f -> FiltroUtil.formatearFechaHora(f.intento().getFechaCreacion())),
                new FiltroUtil.Columna<FilaIntento>("entrega", FiltroUtil.Tipo.FECHA, f -> FiltroUtil.formatearFechaHora(f.intento().getFechaEntrega())),
                new FiltroUtil.Columna<FilaIntento>("duracion", FiltroUtil.Tipo.NUMERO,
                        f -> java.util.Objects.toString(f.intento().getDuracionEnMinutos(), "")),
                new FiltroUtil.Columna<FilaIntento>("nota", FiltroUtil.Tipo.NUMERO,
                        f -> java.util.Objects.toString(f.intento().getNota(), "")),
                new FiltroUtil.Columna<FilaIntento>("resultado", FiltroUtil.Tipo.LISTA, f -> f.intento().getResultado())),
                f -> false);

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<FilaCalificacion> tablaCalificaciones = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<FilaCalificacion>("alumno", FiltroUtil.Tipo.TEXTO,
                        f -> f.inscripcion().getAlumno().getUsuario().getNombreCompleto() + " "
                                + f.inscripcion().getAlumno().getUsuario().getDni() + " " + f.inscripcion().getAlumno().getUsuario().getCorreo()),
                new FiltroUtil.Columna<FilaCalificacion>("cohorte", FiltroUtil.Tipo.LISTA,
                        f -> String.valueOf(f.inscripcion().getCohorte().getIdCohorte())),
                new FiltroUtil.Columna<FilaCalificacion>("tema", FiltroUtil.Tipo.LISTA, f -> f.autoevaluacion().getTema().getTitulo()),
                new FiltroUtil.Columna<FilaCalificacion>("autoevaluacion", FiltroUtil.Tipo.LISTA, f -> f.autoevaluacion().getNombre()),
                new FiltroUtil.Columna<FilaCalificacion>("intentos", FiltroUtil.Tipo.NUMERO,
                        f -> String.valueOf(f.autoevaluacion().intentosDe(f.inscripcion()).size())),
                new FiltroUtil.Columna<FilaCalificacion>("mejorNota", FiltroUtil.Tipo.NUMERO,
                        f -> java.util.Objects.toString(f.autoevaluacion().mejorNota(f.inscripcion()), "")),
                new FiltroUtil.Columna<FilaCalificacion>("resultado", FiltroUtil.Tipo.LISTA, Filtros::resultado)),
                f -> false);

        private static String resultado(FilaCalificacion f) {
            Float nota = f.autoevaluacion().mejorNota(f.inscripcion());
            return f.autoevaluacion().estaAprobadaPor(f.inscripcion()) ? "Aprobada"
                    : (nota != null ? "No aprobada" : (f.habilitada() ? "Sin rendir" : "Tema bloqueada"));
        }
    }
}
