package com.app.idoneos.controlador;

import java.util.List;
import java.util.Map;

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

import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Tema.TemaServicio;
import com.app.idoneos.utilidades.DuracionUtilidad;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Controlador del ABM de temas (MOD-F-02): CU-19 Buscar tema (GET /tema/buscar), CU-20 Agregar tema
 * (POST /tema/registrar), CU-21 Modificar tema (POST /tema/modificar/{id}) y CU-22 Dar de baja tema (quitarla del
 * cronograma de un programa: POST /tema/darDeBaja/{id} y /tema/darDeBajaMasiva).
 *
 * El listado muestra el cronograma del programa por defecto del docente (o el vigente, si lo ve el administrador). El
 * docente (titular o ayudante) agrega y modifica temas de los cursos en los que participa; el administrador puede
 * buscar y dar de baja las de cualquier curso.
 */
@Controller
@RequestMapping("/tema")
public class TemaControlador extends PanelCursoControlador {

    @Autowired
    private TemaServicio temaServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-19: Lista los temas de un curso, con los programas que incluye cada uno. Con {@code nuevo} abre sola la ventana
     * para registrar temas; con {@code editar} abre esa ventana con el tema elegido para modificarlo.
     *
     * @param cursoId            Identificador del curso.
     * @param nuevo              Presente para abrir la ventana de registro (solo docente).
     * @param editar             Identificador de un tema para abrir la ventana con ese tema elegido (solo docente).
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarTemas(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "editar", required = false) Integer editar, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            Curso curso = contexto.curso();
            boolean puedeGestionar = !contexto.esAdministrador();
            cargarContexto(modelo, contexto, "temas");
            List<Tema> temas = temaServicio.buscarPorCurso(curso);
            modelo.addAttribute("temas", temas);
            // Si cada tema tiene todo su contenido (según las modalidades del curso)
            Map<Integer, Boolean> contenidoCompleto = new java.util.HashMap<>();
            temas.forEach(t -> contenidoCompleto.put(t.getIdTema(), disenoCursoServicio.contenidoDe(curso, t).getFaltantes().isEmpty()));
            modelo.addAttribute("contenidoCompleto", contenidoCompleto);
            modelo.addAttribute("puedeGestionar", puedeGestionar);
            modelo.addAttribute("abrirModal", puedeGestionar && (nuevo != null || editar != null) ? "modalTemas" : null);
            modelo.addAttribute("temaAEditar", editar);
            modelo.addAttribute("titulo", "Temas | Idóneos Online");
            return "pages/tema/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda de una vez los temas nuevos y los modificados de un curso (el formulario los arma en la ventana de registro,
     * con la lista de temas del curso y el formulario de cada tema). Responde en JSON.
     *
     * @param cursoId Identificador del curso.
     * @param cambios Los cambios, en JSON: una lista de {@code {id, titulo, descripcion, contenido}} ({@code id} nulo si es
     *                un tema nuevo).
     * @param auth    La autenticación actual.
     * @return Una respuesta con el mensaje de éxito (y, si el curso todavía necesita su programa, la alerta que lo propone)
     *         o el mensaje de error.
     */
    @PostMapping("/guardar")
    public ResponseEntity<Map<String, String>> guardarTemas(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "cambios", required = false) String cambios, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            List<TemaServicio.CambioDeTema> lista = new java.util.ArrayList<>();
            if (cambios != null && !cambios.isBlank()) {
                List<Map<String, Object>> filas = new com.fasterxml.jackson.databind.ObjectMapper().readValue(cambios,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() { });
                for (Map<String, Object> fila : filas) {
                    Object id = fila.get("id");
                    lista.add(new TemaServicio.CambioDeTema(id instanceof Number numero ? numero.intValue() : null,
                            texto(fila.get("titulo")), texto(fila.get("descripcion")), texto(fila.get("contenido"))));
                }
            }
            int cantidad = temaServicio.guardarCambios(usuario.getDocente(), cursoId, lista);
            String mensaje = cantidad == 1 ? "Tema guardado correctamente." : cantidad + " temas guardados correctamente.";
            boolean hayNuevos = lista.stream().anyMatch(c -> c.id() == null);
            Programa vigente = hayNuevos ? cursoServicio.buscarActivoPorId(cursoId).getProgramaVigente() : null;
            if (hayNuevos && (vigente == null || !vigente.estaDuracionAsignadaCompleta())) {
                // Alerta guía: con temas registrados, el paso siguiente es repartirles la duración del curso en un programa
                return RespuestaFormularioUtilidad.respuestaExitosaConSiguiente(mensaje, "¿Armás el programa?",
                        "Ya tenés temas: podés asignarles la duración del curso en el programa.", "Armar el programa",
                        "/programa/buscar?cursoId=" + cursoId + (vigente == null ? "&nuevo=" : ""));
            }
            return RespuestaFormularioUtilidad.respuestaExitosa(mensaje);
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    /**
     * CU-20: Agrega un tema (nueva o ya existente en otro programa del curso) al final del cronograma de un programa.
     * Responde en JSON.
     *
     * @param programaId        Identificador del programa.
     * @param temaExistenteId Tema de otro programa que se incorpora (opcional).
     * @param titulo            El título del tema nueva.
     * @param descripcion       La descripción del tema nueva (opcional).
     * @param contenido         El contenido del tema nueva (opcional).
     * @param duracion          La duración en el cronograma, en la unidad indicada (opcional: la del parámetro del sistema).
     * @param duracionUnidad    La unidad de la duración: "dias", "semanas" o "meses" (por defecto, días).
     * @param auth              La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> agregarTema(
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "temaExistenteId", required = false) Integer temaExistenteId,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "contenido", required = false) String contenido,
            @RequestParam(value = "duracion", required = false) Integer duracion,
            @RequestParam(value = "duracionUnidad", defaultValue = "dias") String duracionUnidad,
            Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Tema tema = temaServicio.agregarTema(usuario.getDocente(), programaId, temaExistenteId, titulo,
                    descripcion, contenido, dias(duracion, duracionUnidad));
            String accion = temaExistenteId != null ? "incorporada" : "registrada";
            return RespuestaFormularioUtilidad.respuestaExitosa("Tema '" + tema.getTitulo() + "' " + accion + " con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-21: Modifica un tema sin inscripciones activas asociadas. Responde en JSON.
     *
     * @param id          Identificador del tema.
     * @param titulo      El título.
     * @param descripcion La descripción (opcional).
     * @param contenido   El contenido.
     * @param programaId  Programa cuyo cronograma contiene el tema (opcional; necesario para cambiar la duración).
     * @param duracion    La duración en el cronograma de ese programa, en la unidad indicada (opcional).
     * @param duracionUnidad La unidad de la duración: "dias", "semanas" o "meses" (por defecto, días).
     * @param auth        La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarTema(@PathVariable("id") Integer id,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "contenido", required = false) String contenido,
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "duracion", required = false) Integer duracion,
            @RequestParam(value = "duracionUnidad", defaultValue = "dias") String duracionUnidad,
            Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            if (programaId != null) {
                temaServicio.modificarTema(id, programaId, usuario.getDocente(), titulo, descripcion, contenido,
                        dias(duracion, duracionUnidad));
            } else {
                temaServicio.modificarTema(id, usuario.getDocente(), titulo, descripcion, contenido);
            }
            return RespuestaFormularioUtilidad.respuestaExitosa("Tema modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Unidades de duración

    /** Convierte la dedicación docente recibida (en minutos u horas) a minutos; {@code null} si no se envió. */
    private static Integer minutos(Integer cantidad, String unidad) {
        if (cantidad == null) {
            return null;
        }
        try {
            return DuracionUtilidad.aMinutos(cantidad, unidad);
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE; // desborda: el servicio lo rechaza por superar el máximo
        }
    }

    /** Convierte la duración recibida (en días, semanas o meses) a días; {@code null} si no se envió. */
    private static Integer dias(Integer cantidad, String unidad) {
        if (cantidad == null) {
            return null;
        }
        try {
            return DuracionUtilidad.aDias(cantidad, unidad);
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE; // desborda: el servicio lo rechaza por superar el máximo
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-22: Da de baja un tema del cronograma de un programa. Responde en JSON.
     *
     * @param id         Identificador del tema.
     * @param programaId Identificador del programa.
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaTema(@PathVariable("id") Integer id,
            @RequestParam(value = "programaId", required = false) Integer programaId, Authentication auth) {
        try {
            if (programaId == null) {
                temaServicio.darDeBajaTemaDelCurso(id, docenteQueGestiona(auth));
            } else {
                temaServicio.darDeBajaTema(id, programaId, docenteQueGestiona(auth));
            }
            return RespuestaFormularioUtilidad.respuestaExitosa("Tema dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-22: Da de baja varios temas del cronograma de un programa, todas o ninguna. Responde en JSON.
     *
     * @param ids        Identificadores de los temas seleccionados.
     * @param programaId Identificador del programa.
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarias(
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "programaId", required = false) Integer programaId, Authentication auth) {
        try {
            temaServicio.darDeBajaVarias(ids, programaId, docenteQueGestiona(auth));
            return RespuestaFormularioUtilidad.respuestaExitosa("Temas dadas de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /** Obtiene el docente cuyas participaciones limitan la operación, o {@code null} si la hace el administrador. */
    private Docente docenteQueGestiona(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return usuario.esAdministradorActivo() ? null : usuario.getDocente();
    }
}
