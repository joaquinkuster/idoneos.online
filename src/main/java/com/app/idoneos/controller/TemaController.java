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

/** Controlador del tema. */
@Controller
@RequestMapping("/tema")
public class TemaController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private TemaService temaService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /tema/buscar: Lista los temas de un curso, con los programas que incluye cada uno. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "editar", required = false) Integer editar,
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "temaId", required = false) Integer temaId, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, (Usuario) authentication.getPrincipal());
            Curso curso = contexto.curso();
            boolean puedeGestionar = !contexto.esAdministrador();
            panel.cargarContexto(model, contexto, "temas");
            List<Tema> temas = temaService.buscarPorCurso(curso);
            model.addAttribute("temas", temas); // lista completa: la usa el asistente
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<Tema> pagina = Filtros.tabla.paginarMostrando(temas, request.getParameterMap(),
                    t -> "baja".equals(accion) && temaId != null && t.getIdTema() == temaId);
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
            // Desde la vista del curso: "Dar de baja" abre la confirmación de ese tema (el docente y el administrador pueden)
            boolean bajaDesdeElCurso = "baja".equals(accion) && temaId != null
                    && temas.stream().anyMatch(t -> t.getIdTema() == temaId);
            model.addAttribute("abrirModal", bajaDesdeElCurso ? "modalBajaTema" + temaId
                    : puedeGestionar && (nuevo != null || editar != null) ? "modalTemas" : null);
            model.addAttribute("temaAEditar", editar);
            model.addAttribute("titulo", "Temas | Idóneos Online");
            return "pages/tema/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /tema/guardar: Guarda de una vez los temas nuevos y los modificados de un curso (el formulario los arma en la ventana de registro, con la lista de temas del curso y el formulario de cada tema). */
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
                return panel.exitoConPasoSiguiente(mensaje, cursoId, usuario.getDocente());
            }
            return RespuestaFormularioUtil.armarRespuestaExitosa(mensaje);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /tema/darDeBaja/{id}: Da de baja un tema del curso (y lo quita del cronograma de los programas que lo incluyen). */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            temaService.darDeBajaPorCurso(id, docenteQueGestiona(authentication));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Tema dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /tema/darDeBajaMasiva: Da de baja varios temas del curso a la vez, todos o ninguno. */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication authentication) {
        try {
            temaService.darDeBajaVariasPorCurso(ids, docenteQueGestiona(authentication));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Temas dados de baja con éxito.");
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

        // ----------------------------------------------------------------------
        // Listado (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Tema> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Tema>("titulo", FiltroUtil.Tipo.TEXTO, Tema::getTitulo),
                new FiltroUtil.Columna<Tema>("descripcion", FiltroUtil.Tipo.TEXTO, Tema::getDescripcion),
                new FiltroUtil.Columna<Tema>("contenido", FiltroUtil.Tipo.TEXTO, Tema::getContenido),
                new FiltroUtil.Columna<Tema>("ultimaModificacion", FiltroUtil.Tipo.FECHA, t -> FiltroUtil.formatearFechaHora(t.getUltimaModificacion())),
                new FiltroUtil.Columna<Tema>("fechaCreacion", FiltroUtil.Tipo.FECHA, t -> FiltroUtil.formatearFechaHora(t.getFechaCreacion()))),
                t -> false);
    }
}
