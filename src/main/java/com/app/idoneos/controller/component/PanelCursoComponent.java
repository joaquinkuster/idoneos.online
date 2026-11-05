package com.app.idoneos.controller.component;

import com.app.idoneos.model.Programa;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Parametro;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.curso.CursoService;
import com.app.idoneos.service.inscripcion.InscripcionService;
import com.app.idoneos.service.parametro.ParametroService;

/**
 * Base de los controladores de las pantallas que viven dentro del panel de un curso (temas, programas, cronograma,
 * materiales, glosario, foros, participantes, cohortes y la página del curso). Resuelve con qué rol entra la persona,
 * con qué curso y programa trabaja y arma los datos de pantalla que todas comparten ({@code urlBase},
 * {@code urlMisCursos}, {@code esDocente}, {@code esAdministrador} y {@code menuActivo}). Las reglas de acceso las
 * resuelven los servicios; acá solo se arma lo que necesita la vista.
 */
@org.springframework.stereotype.Component
public class PanelCursoComponent {

    @Autowired
    private CursoService cursoService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private com.app.idoneos.service.tema.TemaService temaService;


    @Autowired
    private com.app.idoneos.service.cohorte.CohorteService cohorteService;

    /**
     * Resuelve el contexto de las pantallas que se abren desde el panel con {@code cursoId}: el administrador trabaja con
     * el programa vigente del curso y el docente, con el que tiene elegido.
     *
     * @param cursoId El identificador del curso (obligatorio).
     * @param usuario El usuario autenticado.
     * @return El contexto de la pantalla.
     * @throws IllegalArgumentException Si no se indicó el curso, no está activo o el docente no participa.
     */
    public ContextoCurso contextoDelPanel(Integer cursoId, Usuario usuario) {
        return cursoService.resolverContextoDelPanel(cursoId, usuario);
    }

    /**
     * Resuelve el contexto de las pantallas que se abren desde un tema (foro, glosario, material): el administrador
     * y los docentes del curso entran como tales; el alumno, por su inscripción con acceso al tema. Quien las
     * llame debe haber comprobado antes que la persona puede ver el contenido.
     *
     * @param tema  El tema de la pantalla.
     * @param usuario El usuario autenticado.
     * @return El contexto de la pantalla.
     * @throws ResponseStatusException 403 si no es de la gestión del curso ni tiene una inscripción con acceso.
     */
    public ContextoCurso contextoDeLaTema(Tema tema, Usuario usuario) {
        return cursoService.resolverContextoDeLaTema(tema, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    /**
     * Vuelca al modelo los datos que comparten todas las pantallas del panel de un curso.
     *
     * @param model     El modelo de la vista.
     * @param contexto   El contexto de la pantalla.
     * @param menuActivo La opción del menú lateral que se marca como activa.
     */
    public void cargarContexto(Model model, ContextoCurso contexto, String menuActivo) {
        model.addAttribute("curso", contexto.curso());
        model.addAttribute("programa", contexto.programa());
        model.addAttribute("esAdministrador", contexto.esAdministrador());
        model.addAttribute("esDocente", contexto.esDocente());
        model.addAttribute("urlBase", contexto.urlBase());
        model.addAttribute("urlMisCursos", contexto.urlMisCursos());
        model.addAttribute("menuActivo", menuActivo);
        // El estado del diseño es para quien gestiona el curso. Al docente el menú se le habilita de a poco, según el paso del
        // diseño en que está su cohorte de trabajo; el administrador ve todo
        if (contexto.esDocente() || contexto.esAdministrador()) {
            com.app.idoneos.model.Cohorte cohorte = contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo();
            // Si el docente acaba de completar el diseño, la cohorte pasa de Pendiente a Activa ya, sin esperar a la tarea periódica
            if (cohorte != null && cohorte.esPendiente()) {
                cohorteService.activarSiEstaCompleta(cohorte.getIdCohorte());
            }
            CursoService.EstadoDiseno estado = cursoService.calcularEstadoDiseno(contexto.curso(), cohorte);
            model.addAttribute("estadoDiseno", estado);
            if (contexto.esDocente()) {
                recordarDisenoCompleto(contexto.curso().getIdCurso(), estado.getPasoActual() >= 6);
            }
            model.addAttribute("nivelMenu", contexto.esDocente() ? estado.getNivelDeMenu() : 5);
            // "Esta cohorte" (participantes, progreso, intentos y calificaciones) se habilita cuando deja de estar pendiente
            model.addAttribute("cohortePendiente", contexto.esDocente() && (cohorte == null || cohorte.esPendiente()));
        }
    }

    /**
     * Arma la respuesta de una operación exitosa del diseño del curso (datos, programa, temas, cronograma) con la alerta guía que
     * propone el paso que sigue, según el estado del diseño de la cohorte de trabajo del docente: registrar o asignar el programa,
     * registrar los temas que faltan hasta el mínimo, armar el cronograma o completar el contenido de los temas pendientes. Con el
     * diseño completo (o si faltan los datos del curso) responde solo con el mensaje.
     *
     * @param mensaje El mensaje de éxito.
     * @param cursoId El curso en que se trabaja.
     * @param docente El docente que hizo la operación.
     * @return La respuesta JSON, con la alerta del paso siguiente si corresponde.
     */
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> exitoConPasoSiguiente(String mensaje,
            Integer cursoId, com.app.idoneos.model.Docente docente) {
        return exitoConPasoSiguiente(mensaje, cursoId, docente, false);
    }

    /**
     * Igual que {@link #exitoConPasoSiguiente(String, Integer, com.app.idoneos.model.Docente)}, y además, si
     * {@code avisarTemasSinCronograma}, propone armar el cronograma cuando el curso tiene temas que el programa de la cohorte todavía no
     * incluye (por ejemplo, un tema nuevo registrado con el diseño ya completo: no entra solo al cronograma).
     *
     * @param mensaje                  El mensaje de éxito.
     * @param cursoId                  El curso en que se trabaja.
     * @param docente                  El docente que hizo la operación.
     * @param avisarTemasSinCronograma Si se avisa de los temas del curso que no están en el cronograma del programa.
     * @return La respuesta JSON, con la alerta del paso siguiente si corresponde.
     */
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> exitoConPasoSiguiente(String mensaje,
            Integer cursoId, com.app.idoneos.model.Docente docente, boolean avisarTemasSinCronograma) {
        com.app.idoneos.model.Curso curso;
        com.app.idoneos.model.Cohorte cohorte;
        try {
            curso = cursoService.buscarActivoPorId(cursoId);
            cohorte = cursoService.validarAccesoDocente(cursoId, docente).getCohorteDeTrabajo();
        } catch (RuntimeException e) {
            // La operación ya se hizo: si no se puede calcular el paso siguiente, se responde solo con el mensaje
            return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosa(mensaje);
        }
        CursoService.EstadoDiseno estado = cursoService.calcularEstadoDiseno(curso, cohorte);
        String id = "?cursoId=" + cursoId;
        if (avisarTemasSinCronograma && estado.getPasoActual() >= 5 && estado.programaVigente() != null) {
            // Con el diseño en marcha (programa con toda la duración asignada), un tema nuevo no está en el cronograma
            java.util.Set<Integer> enCronograma = estado.programaVigente().getCronogramaOrdenado().stream()
                    .map(u -> u.getTema().getIdTema()).collect(java.util.stream.Collectors.toSet());
            long sinCronograma = curso.getTemas().stream()
                    .filter(t -> !t.getBaja() && !enCronograma.contains(t.getIdTema())).count();
            if (sinCronograma > 0) {
                return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                        sinCronograma == 1 ? "¿Sumás el tema al cronograma?" : "¿Sumás los temas al cronograma?",
                        sinCronograma == 1
                                ? "El curso tiene 1 tema que todavía no está en el cronograma: asignale su duración."
                                : "El curso tiene " + sinCronograma + " temas que todavía no están en el cronograma: asignales su duración.",
                        "Armar el cronograma", "/cronograma/buscar" + id);
            }
        }
        switch (estado.getPasoActual()) {
            case 2: {
                boolean puedeAsignar = cohorte != null && cohorte.esPendiente() && cohorte.getCantidadInscriptos() == 0;
                if (!curso.getProgramasActivos().isEmpty() && puedeAsignar) {
                    return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                            "¿Asignás un programa?", "Ahora la cohorte necesita su programa: elegí uno de los que ya tiene el curso.",
                            "Asignar programa", "/cohorte/buscar" + id + "&asignar=" + cohorte.getIdCohorte());
                }
                return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                        "¿Registrás el programa?", "Ahora la cohorte necesita su programa: registrá el primero.",
                        "Registrar programa", "/programa/buscar" + id + "&nuevo=");
            }
            case 3: {
                int faltan = Math.max(1, estado.minimoTemas() - estado.cantidadTemas());
                return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                        "¿Registrás los temas?", "El curso necesita al menos " + estado.minimoTemas()
                                + (estado.minimoTemas() == 1 ? " tema" : " temas") + ": " + (faltan == 1 ? "falta 1." : "faltan " + faltan + "."),
                        "Registrar temas", "/tema/buscar" + id + "&nuevo=");
            }
            case 4:
                return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                        "¿Armás el cronograma?", "Asigná la duración del curso a los temas del programa.",
                        "Armar el cronograma", "/cronograma/buscar" + id);
            case 5:
                return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                        "¿Completás el contenido?", "Todavía hay temas a los que les falta contenido: " + estado.getTemasIncompletos().size()
                                + (estado.getTemasIncompletos().size() == 1 ? " pendiente." : " pendientes."),
                        "Completar contenido", "/curso/acceder/" + cursoId + "?completar=contenido");
            default:
                if (!disenoRecienCompleto(cursoId)) {
                    return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosa(mensaje);
                }
                // El diseño completo se avisa como un éxito (no como una pregunta informativa)
                java.util.Map<String, String> completo = new java.util.LinkedHashMap<>();
                completo.put("mensaje", mensaje);
                completo.put("siguienteTipo", "exito");
                completo.put("siguienteTitulo", TITULO_DISENO_COMPLETO);
                completo.put("siguienteTexto", TEXTO_DISENO_COMPLETO);
                completo.put("siguienteConfirmar", "Ver el curso");
                completo.put("siguienteUrl", "/curso/acceder/" + cursoId);
                return org.springframework.http.ResponseEntity.ok(completo);
        }
    }

    private static final String TITULO_DISENO_COMPLETO = "¡Completaste el diseño del curso!";
    private static final String TEXTO_DISENO_COMPLETO = "El curso ya tiene todo lo necesario para que la cohorte pase a estar activa.";

    private static String claveDisenoCompleto(Integer cursoId) {
        return "disenoCompleto." + cursoId;
    }

    /** Recuerda en la sesión si el diseño del curso estaba completo cuando se mostró la pantalla (para detectar el momento en que se completa). */
    private void recordarDisenoCompleto(Integer cursoId, boolean completo) {
        var atributos = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (atributos != null) {
            atributos.setAttribute(claveDisenoCompleto(cursoId), completo,
                    org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION);
        }
    }

    /** Indica si el diseño pasó a estar completo con esta operación (y deja registrado que ya lo está, para no repetir el aviso). */
    private boolean disenoRecienCompleto(Integer cursoId) {
        var atributos = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (atributos == null) {
            return false;
        }
        Object antes = atributos.getAttribute(claveDisenoCompleto(cursoId),
                org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION);
        recordarDisenoCompleto(cursoId, true);
        return Boolean.FALSE.equals(antes);
    }

    /**
     * Arma la respuesta de un recurso o actividad registrado (material, pool, autoevaluación): si al curso todavía le falta contenido
     * por completar, agrega la alerta guía para seguir con el que sigue; si no, responde solo con el mensaje (y el destino, si hay).
     *
     * @param mensaje El mensaje de éxito.
     * @param destino A dónde se va después, o {@code null} para recargar la pantalla.
     * @param cursoId El curso en que se trabaja.
     * @param docente El docente que registró el recurso.
     * @return La respuesta JSON.
     */
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> exitoConContenidoPendiente(String mensaje,
            String destino, Integer cursoId, com.app.idoneos.model.Docente docente) {
        java.util.Map<String, String> cuerpo = new java.util.LinkedHashMap<>();
        cuerpo.put("mensaje", mensaje);
        if (destino != null) {
            cuerpo.put("destino", destino);
        }
        try {
            com.app.idoneos.model.Curso curso = cursoService.buscarActivoPorId(cursoId);
            com.app.idoneos.model.Cohorte cohorte = cursoService.validarAccesoDocente(cursoId, docente).getCohorteDeTrabajo();
            CursoService.EstadoDiseno estado = cursoService.calcularEstadoDiseno(curso, cohorte);
            if (estado.getPasoActual() == 5) {
                int pendientes = estado.getTemasIncompletos().size();
                cuerpo.put("siguienteTitulo", "¿Seguís completando el contenido?");
                cuerpo.put("siguienteTexto", "Todavía hay " + pendientes + (pendientes == 1 ? " tema" : " temas") + " con contenido pendiente.");
                cuerpo.put("siguienteConfirmar", "Completar contenido");
                cuerpo.put("siguienteUrl", "/curso/acceder/" + cursoId + "?completar=contenido");
            } else if (estado.getPasoActual() >= 6 && disenoRecienCompleto(cursoId)) {
                cuerpo.put("siguienteTipo", "exito");
                cuerpo.put("siguienteTitulo", TITULO_DISENO_COMPLETO);
                cuerpo.put("siguienteTexto", TEXTO_DISENO_COMPLETO);
                cuerpo.put("siguienteConfirmar", "Ver el curso");
                cuerpo.put("siguienteUrl", "/curso/acceder/" + cursoId);
            }
        } catch (RuntimeException e) {
            // El recurso ya se registró: sin alerta guía, solo el mensaje
        }
        return org.springframework.http.ResponseEntity.ok(cuerpo);
    }

    /**
     * Obtiene los días que ocupa un tema nuevo en el cronograma cuando no se indica otra duración (parámetro del sistema).
     *
     * @return Los días de un tema nuevo.
     */
    public int diasPorTemaNuevo() {
        return Math.max(1, parametroService.obtenerEntero(Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO,
                Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO_POR_DEFECTO));
    }

    /**
     * Vuelca al modelo el máximo de días que puede sumar un cronograma del curso: la duración del curso.
     *
     * @param model El modelo de la vista.
     * @param curso  El curso del cronograma.
     */
    public void cargarDiasMaximosDelCronograma(Model model, Curso curso) {
        model.addAttribute("diasNuevoTema", diasPorTemaNuevo());
        model.addAttribute("diasMaximos", curso.getDuracionDias());
    }

    /**
     * Vuelve al listado de cursos mostrando el motivo por el que no se pudo abrir la pantalla.
     *
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @param error              El error que impidió abrir la pantalla.
     * @return La redirección al listado de cursos.
     */
    public String volverAMisCursos(RedirectAttributes redirectAttributes, IllegalArgumentException error) {
        redirectAttributes.addFlashAttribute("error", error.getMessage());
        return "redirect:/curso/buscar";
    }

    /**
     * Un curso sin programas no tiene temas ni cronograma: el docente va a registrar el primero y el administrador
     * vuelve al listado de cursos.
     *
     * @param contexto           El contexto de la pantalla.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La redirección que corresponde al rol.
     */
    public String avisarCursoSinProgramas(ContextoCurso contexto, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error",
                "Error! El curso todavía no tiene programas: registrá uno para poder cargar temas.");
        return contexto.esAdministrador() ? "redirect:/curso/buscar"
                : "redirect:/programa/buscar?cursoId=" + contexto.curso().getIdCurso() + "&nuevo";
    }

    /**
     * Carga en el modelo lo que comparten las vistas del foro: el tema, el curso, el rol de quien mira, el plazo de
     * edición y los enlaces del menú. Verifica que el usuario pueda ver el foro del tema.
     */
    public void cargarContextoDelForo(Tema tema, Usuario usuario, Model model) {
        if (!temaService.puedeVerContenido(tema, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        ContextoCurso contexto = contextoDeLaTema(tema, usuario);
        boolean esAdministrador = contexto.esAdministrador();
        boolean esDocente = contexto.esDocente();
        Programa programa = contexto.programa();
        int numeroOrden = programa == null ? 0 : programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> cronograma.getTema().getIdTema() == tema.getIdTema())
                .mapToInt(TemaCronograma::getNumeroOrden).findFirst().orElse(0);

        cargarContexto(model, contexto, "foros");
        model.addAttribute("tema", tema);
        model.addAttribute("numeroOrden", numeroOrden);
        model.addAttribute("esAlumno", contexto.esAlumno());
        model.addAttribute("alumnoActual", contexto.esAlumno() ? usuario.getAlumno() : null);
        model.addAttribute("docenteActual", esDocente ? usuario.getDocente() : null);
        model.addAttribute("plazoEdicion", parametroService.obtenerEntero(Parametro.PLAZO_EDICION_FORO,
                Parametro.PLAZO_EDICION_FORO_POR_DEFECTO));
    }
}
