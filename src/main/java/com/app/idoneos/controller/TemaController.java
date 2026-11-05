package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.service.curso.CursoService;

import com.app.idoneos.util.PaginacionUtil;
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

import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.tema.TemaService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/**
 * Controlador del ABM de temas (MOD-F-02): CU-19 Buscar tema (GET /tema/buscar), CU-20 y CU-21 Registrar y modificar temas
 * (POST /tema/guardar, que guarda de una vez los temas nuevos y los modificados de un curso) y CU-22 Dar de baja tema
 * (POST /tema/darDeBaja/{id}: del curso, o solo del cronograma de un programa si se indica).
 *
 * Los temas son del curso, no de un programa: el listado muestra todos los del curso con los programas que los incluyen. El
 * docente (titular o ayudante) registra y modifica los temas de los cursos en los que participa; el administrador puede
 * buscar y dar de baja los de cualquier curso.
 */
@Controller
@RequestMapping("/tema")
public class TemaController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private TemaService temaService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-19: Lista los temas de un curso, con los programas que incluye cada uno. Con {@code nuevo} abre sola la ventana
     * para registrar temas; con {@code editar} abre esa ventana con el tema elegido para modificarlo.
     *
     * @param cursoId            Identificador del curso.
     * @param nuevo              Presente para abrir la ventana de registro (solo docente).
     * @param editar             Identificador de un tema para abrir la ventana con ese tema elegido (solo docente).
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "editar", required = false) Integer editar, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, (Usuario) authentication.getPrincipal());
            Curso curso = contexto.curso();
            boolean puedeGestionar = !contexto.esAdministrador();
            panel.cargarContexto(model, contexto, "temas");
            List<Tema> temas = temaService.buscarPorCurso(curso);
            model.addAttribute("temas", temas); // lista completa: la usa el asistente
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<Tema> pagina = Filtros.tabla.paginar(temas, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            model.addAttribute("temasPagina", pagina.filas());
            // Si cada tema tiene todo su contenido (según las modalidades del curso)
            Map<Integer, Boolean> contenidoCompleto = new java.util.HashMap<>();
            temas.forEach(t -> contenidoCompleto.put(t.getIdTema(), cursoService.buscarContenidoDeTema(curso, t).getFaltantes().isEmpty()));
            model.addAttribute("contenidoCompleto", contenidoCompleto);
            if (PaginacionUtil.pideSoloFilas(request)) {
                model.addAttribute("puedeGestionar", puedeGestionar);
                return "pages/tema/buscar :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("puedeGestionar", puedeGestionar);
            model.addAttribute("abrirModal", puedeGestionar && (nuevo != null || editar != null) ? "modalTemas" : null);
            model.addAttribute("temaAEditar", editar);
            model.addAttribute("titulo", "Temas | Idóneos Online");
            return "pages/tema/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
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
     * @param authentication    La autenticación actual.
     * @return Una respuesta con el mensaje de éxito (y, si el curso todavía necesita su programa, la alerta que lo propone)
     *         o el mensaje de error.
     */
    @PostMapping("/guardar")
    public ResponseEntity<Map<String, String>> guardar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "cambios", required = false) String cambios, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            List<TemaService.CambioDeTema> lista = new java.util.ArrayList<>();
            if (cambios != null && !cambios.isBlank()) {
                List<Map<String, Object>> filas = new com.fasterxml.jackson.databind.ObjectMapper().readValue(cambios,
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() { });
                for (Map<String, Object> fila : filas) {
                    Object id = fila.get("id");
                    lista.add(new TemaService.CambioDeTema(id instanceof Number numero ? numero.intValue() : null,
                            texto(fila.get("titulo")), texto(fila.get("descripcion")), texto(fila.get("contenido"))));
                }
            }
            int cantidad = temaService.guardarCambios(usuario.getDocente(), cursoId, lista);
            String mensaje = cantidad == 1 ? "Tema guardado correctamente." : cantidad + " temas guardados correctamente.";
            boolean hayNuevos = lista.stream().anyMatch(c -> c.id() == null);
            if (hayNuevos) {
                // Alerta guía: con el mínimo de temas, el paso siguiente es armar el cronograma (o el que corresponda)
                return panel.exitoConPasoSiguiente(mensaje, cursoId, usuario.getDocente(), true);
            }
            return RespuestaFormularioUtil.armarRespuestaExitosa(mensaje);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-22: Da de baja un tema del cronograma de un programa. Responde en JSON.
     *
     * @param id         Identificador del tema.
     * @param programaId Identificador del programa.
     * @param authentication       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id,
            @RequestParam(value = "programaId", required = false) Integer programaId, Authentication authentication) {
        try {
            if (programaId == null) {
                temaService.darDeBajaPorCurso(id, docenteQueGestiona(authentication));
            } else {
                temaService.darDeBaja(id, programaId, docenteQueGestiona(authentication));
            }
            return RespuestaFormularioUtil.armarRespuestaExitosa("Tema dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Obtiene el docente cuyas participaciones limitan la operación, o {@code null} si la hace el administrador. */
    private Docente docenteQueGestiona(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return usuario.esAdministradorActivo() ? null : usuario.getDocente();
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado (motor común)


        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<Tema> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Tema>("titulo", FiltroUtil.Tipo.TEXTO, Tema::getTitulo),
                new FiltroUtil.Columna<Tema>("descripcion", FiltroUtil.Tipo.TEXTO, Tema::getDescripcion),
                new FiltroUtil.Columna<Tema>("contenido", FiltroUtil.Tipo.TEXTO, Tema::getContenido),
                new FiltroUtil.Columna<Tema>("ultimaModificacion", FiltroUtil.Tipo.FECHA, t -> FiltroUtil.formatearFechaHora(t.getUltimaModificacion())),
                new FiltroUtil.Columna<Tema>("fechaCreacion", FiltroUtil.Tipo.FECHA, t -> FiltroUtil.formatearFechaHora(t.getFechaCreacion()))),
                t -> false);
    }
}
