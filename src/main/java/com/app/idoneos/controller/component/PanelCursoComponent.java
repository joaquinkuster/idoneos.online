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

/** Componente base de los controladores de las pantallas del panel de un curso. */
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

    /** Resuelve el contexto de las pantallas que se abren desde el panel con cursoId. */
    public ContextoCurso contextoDelPanel(Integer cursoId, Usuario usuario) {
        return cursoService.resolverContextoDelPanel(cursoId, usuario);
    }

    /** Resuelve el contexto de las pantallas que se abren desde un tema (foro, glosario, material). */
    public ContextoCurso contextoDeLaTema(Tema tema, Usuario usuario) {
        return cursoService.resolverContextoDeLaTema(tema, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    /** Vuelca al modelo los datos que comparten todas las pantallas del panel de un curso. */
    public void cargarContexto(Model model, ContextoCurso contexto, String menuActivo) {
        model.addAttribute("curso", contexto.curso());
        model.addAttribute("programa", contexto.programa());
        model.addAttribute("esAdministrador", contexto.esAdministrador());
        model.addAttribute("esDocente", contexto.esDocente());
        model.addAttribute("urlBase", contexto.urlBase());
        model.addAttribute("urlMisCursos", contexto.urlMisCursos());
        model.addAttribute("menuActivo", menuActivo);
        // El estado del diseño es para quien gestiona el curso. Al docente el menú se le habilita de a poco, según el paso del
        // diseño en que está su cohorte de trabajo. El administrador ve todo
        if (contexto.esDocente() || contexto.esAdministrador()) {
            com.app.idoneos.model.Cohorte cohorte = contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo();
            CursoService.EstadoDiseno estado = cursoService.calcularEstadoDiseno(contexto.curso(), cohorte);
            model.addAttribute("estadoDiseno", estado);
            if (contexto.esDocente()) {
                recordarDisenoCompleto(contexto.curso().getIdCurso(), estado.getPasoActual() >= 4);
                recordarPaso(contexto.curso().getIdCurso(), estado.getPasoActual());
            }
            model.addAttribute("nivelMenu", contexto.esDocente() ? estado.getNivelDeMenu() : 5);
            // "Esta cohorte" (participantes, progreso, intentos y calificaciones) se habilita cuando deja de estar pendiente
            model.addAttribute("cohortePendiente", contexto.esDocente() && (cohorte == null || cohorte.esPendiente()));
        }
    }

    /** Arma la respuesta de una operación exitosa del diseño del curso. */
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> exitoConPasoSiguiente(String mensaje,
            Integer cursoId, com.app.idoneos.model.Docente docente) {
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
        switch (estado.getPasoActual()) {
            case 2: {
                // Temas y contenido: primero los temas hasta el mínimo y después el contenido de cada uno
                if (estado.cantidadTemas() < estado.minimoTemas()) {
                    int faltan = Math.max(1, estado.minimoTemas() - estado.cantidadTemas());
                    return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                            "¿Registrás los temas?", "El curso necesita al menos " + estado.minimoTemas()
                                    + (estado.minimoTemas() == 1 ? " tema" : " temas") + ": " + (faltan == 1 ? "falta 1." : "faltan " + faltan + "."),
                            "Registrar temas", "/tema/buscar" + id + "&nuevo=");
                }
                return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                        "¿Completás el contenido?", contenidoPendiente(estado), "Completar contenido",
                        "/curso/acceder/" + cursoId + "?completar=contenido");
            }
            case 3: {
                boolean puedeAsignar = cohorte != null && cohorte.esPendiente() && cohorte.getCantidadInscriptos() == 0;
                if (!curso.getProgramasActivos().isEmpty() && puedeAsignar) {
                    java.util.Map<String, String> cuerpo = new java.util.LinkedHashMap<>(
                            com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                                    "¿Cómo seguís con el programa?",
                                    "Ahora la cohorte necesita su programa: asigná uno de los que ya tiene el curso o registrá una versión nueva a partir de los temas creados.",
                                    "Asignar programa", "/cohorte/buscar" + id + "&asignar=" + cohorte.getIdCohorte() + "&volver=curso").getBody());
                    cuerpo.put("siguienteAlternativa", "Nueva versión");
                    cuerpo.put("siguienteAlternativaUrl", "/programa/buscar" + id + "&nuevo=");
                    return org.springframework.http.ResponseEntity.ok(cuerpo);
                }
                return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                        "¿Registrás el programa?", "Ahora la cohorte necesita su programa: registrá el primero.",
                        "Registrar programa", "/programa/buscar" + id + "&nuevo=");
            }
            default:
                // Con el programa completo y la cohorte todavía pendiente, publicarla es decisión del docente: se le pregunta
                if (cohorte != null && cohorte.esPendiente() && cohorte.tienePrograma() && estado.isProgramaCompleto()) {
                    java.util.Map<String, String> cuerpo = new java.util.LinkedHashMap<>(
                            com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(mensaje,
                                    com.app.idoneos.controller.CohorteController.TITULO_FINALIZAR_DISENO,
                                    com.app.idoneos.controller.CohorteController.textoFinalizarDiseno(cohorte), "Finalizar diseño",
                                    "/curso/acceder/" + cursoId).getBody());
                    cuerpo.put("siguientePost", "/cohorte/finalizarDiseno/" + cohorte.getIdCohorte());
                    cuerpo.put("siguienteTipo", "advertencia");
                    return org.springframework.http.ResponseEntity.ok(cuerpo);
                }
                if (!disenoRecienCompleto(cursoId)) {
                    return com.app.idoneos.util.RespuestaFormularioUtil.armarRespuestaExitosa(mensaje);
                }
                // El diseño completo se avisa como un éxito (no como una pregunta informativa)
                java.util.Map<String, String> completo = new java.util.LinkedHashMap<>();
                completo.put("mensaje", mensaje);
                completo.put("siguienteTipo", "exito");
                completo.put("siguienteTitulo", TITULO_DISENO_COMPLETO);
                completo.put("siguienteTexto", TEXTO_DISENO_COMPLETO);
                completo.put("siguienteConfirmar", "Ir al curso");
                completo.put("siguienteUrl", "/curso/acceder/" + cursoId);
                return org.springframework.http.ResponseEntity.ok(completo);
        }
    }

    /** Describe lo que falta del contenido de los temas, para la alerta guía. */
    private static String contenidoPendiente(CursoService.EstadoDiseno estado) {
        int faltan = Math.max(1, estado.getTemasQueFaltan());
        return "Todavía " + (faltan == 1 ? "falta completar el contenido de 1 tema" : "faltan completar el contenido de " + faltan + " temas")
                + ".";
    }

    private static final String TITULO_DISENO_COMPLETO = "¡Completaste el diseño del curso!";
    private static final String TEXTO_DISENO_COMPLETO = "El curso ya tiene todo lo necesario para publicar la cohorte.";

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

    /** Recuerda en la sesión el paso del diseño en que estaba el curso cuando se mostró la pantalla (para detectar cuándo se pasa al siguiente). */
    private void recordarPaso(Integer cursoId, int paso) {
        var atributos = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (atributos != null) {
            atributos.setAttribute("pasoDiseno." + cursoId, paso, org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION);
        }
    }

    /** Obtiene el paso del diseño en que estaba el curso la última vez que se mostró una pantalla, o 0 si no se sabe. */
    private int pasoAnterior(Integer cursoId, int pasoActual) {
        var atributos = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (atributos == null) {
            return 0;
        }
        Object antes = atributos.getAttribute("pasoDiseno." + cursoId, org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION);
        recordarPaso(cursoId, pasoActual);
        return antes instanceof Integer anterior ? anterior : 0;
    }

    /** Indica si el diseño pasó a estar completo con esta operación. */
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

    /** Arma la respuesta de un recurso o actividad registrado (material, pool, autoevaluación). */
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
            int pasoAntes = pasoAnterior(cursoId, estado.getPasoActual());
            if (estado.getPasoActual() == 3 && pasoAntes == 2) {
                // Se completó el contenido de los temas: sigue el programa de la cohorte
                String id = "?cursoId=" + cursoId;
                boolean puedeAsignar = cohorte != null && cohorte.esPendiente() && cohorte.getCantidadInscriptos() == 0;
                if (!curso.getProgramasActivos().isEmpty() && puedeAsignar) {
                    cuerpo.put("siguienteTitulo", "¿Asignás un programa?");
                    cuerpo.put("siguienteTexto", "Ya completaste el contenido de los temas. Ahora la cohorte necesita su programa: elegí uno de los que ya tiene el curso.");
                    cuerpo.put("siguienteConfirmar", "Asignar programa");
                    cuerpo.put("siguienteUrl", "/cohorte/buscar" + id + "&asignar=" + cohorte.getIdCohorte() + "&volver=curso");
                } else {
                    cuerpo.put("siguienteTitulo", "¿Registrás el programa?");
                    cuerpo.put("siguienteTexto", "Ya completaste el contenido de los temas. Ahora la cohorte necesita su programa: registrá el primero.");
                    cuerpo.put("siguienteConfirmar", "Registrar programa");
                    cuerpo.put("siguienteUrl", "/programa/buscar" + id + "&nuevo=");
                }
            } else if (estado.getPasoActual() == 2 && estado.cantidadTemas() >= estado.minimoTemas()) {
                cuerpo.put("siguienteTitulo", "¿Seguís completando el contenido?");
                cuerpo.put("siguienteTexto", contenidoPendiente(estado));
                cuerpo.put("siguienteConfirmar", "Completar contenido");
                cuerpo.put("siguienteUrl", "/curso/acceder/" + cursoId + "?completar=contenido");
            } else if (estado.getPasoActual() >= 4 && disenoRecienCompleto(cursoId)) {
                cuerpo.put("siguienteTipo", "exito");
                cuerpo.put("siguienteTitulo", TITULO_DISENO_COMPLETO);
                cuerpo.put("siguienteTexto", TEXTO_DISENO_COMPLETO);
                cuerpo.put("siguienteConfirmar", "Ir al curso");
                cuerpo.put("siguienteUrl", "/curso/acceder/" + cursoId);
            }
        } catch (RuntimeException e) {
            // El recurso ya se registró: sin alerta guía, solo el mensaje
        }
        return org.springframework.http.ResponseEntity.ok(cuerpo);
    }

    /** Obtiene los temas que se ofrecen en los selectores de los ABM del contenido. */
    public java.util.List<com.app.idoneos.model.TemaCronograma> temasDelCursoEnOrden(ContextoCurso contexto) {
        Programa programa = contexto.programa();
        java.util.List<com.app.idoneos.model.TemaCronograma> lista = new java.util.ArrayList<>(
                programa == null ? java.util.List.of() : programa.getCronogramaOrdenado());
        if (!contexto.esDocente() && !contexto.esAdministrador()) {
            return lista;
        }
        java.util.Set<Integer> enPrograma = lista.stream().map(c -> c.getTema().getIdTema()).collect(java.util.stream.Collectors.toSet());
        int orden = lista.size();
        for (com.app.idoneos.model.Tema tema : contexto.curso().getTemas().stream()
                .filter(t -> !t.getBaja() && !enPrograma.contains(t.getIdTema()))
                .sorted(java.util.Comparator.comparingInt(com.app.idoneos.model.Tema::getIdTema)).toList()) {
            lista.add(new com.app.idoneos.model.TemaCronograma(programa, tema, ++orden, 0));
        }
        return lista;
    }

    /** Obtiene los días que ocupa un tema nuevo en el cronograma cuando no se indica otra duración (parámetro del sistema). */
    public int diasPorTemaNuevo() {
        return Math.max(1, parametroService.obtenerEntero(Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO,
                Parametro.CRONOGRAMA_DIAS_POR_TEMA_NUEVO_POR_DEFECTO));
    }

    /** Vuelca al modelo el máximo de días que puede sumar un cronograma del curso: la duración del curso. */
    public void cargarDiasMaximosDelCronograma(Model model, Curso curso) {
        model.addAttribute("diasNuevoTema", diasPorTemaNuevo());
        model.addAttribute("diasMaximos", curso.getDuracionDias());
    }

    /** Vuelve al listado de cursos mostrando el motivo por el que no se pudo abrir la pantalla. */
    public String volverAMisCursos(RedirectAttributes redirectAttributes, IllegalArgumentException error) {
        redirectAttributes.addFlashAttribute("error", error.getMessage());
        return "redirect:/curso/buscar";
    }

    /** Avisa que un curso sin programas no tiene temas ni cronograma. */
    public String avisarCursoSinProgramas(ContextoCurso contexto, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error",
                "Error! El curso todavía no tiene programas: registrá uno para poder cargar temas.");
        return contexto.esAdministrador() ? "redirect:/curso/buscar"
                : "redirect:/programa/buscar?cursoId=" + contexto.curso().getIdCurso() + "&nuevo";
    }

    /** Carga en el modelo lo que comparten las vistas del foro: el tema, el curso, el rol de quien mira. */
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
