package com.app.idoneos.controlador;

import com.app.idoneos.utilidades.FiltroUtilidad;
import com.app.idoneos.utilidades.PaginacionUtilidad;
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
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Tema.TemaServicio;
import com.app.idoneos.utilidades.DuracionUtilidad;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

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
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest solicitud) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            Curso curso = contexto.curso();
            boolean puedeGestionar = !contexto.esAdministrador();
            cargarContexto(modelo, contexto, "temas");
            List<Tema> temas = temaServicio.buscarPorCurso(curso);
            modelo.addAttribute("temas", temas); // lista completa: la usa el asistente
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            List<Tema> paginaFiltradas = TABLA.filtrar(temas, solicitud.getParameterMap());
            TABLA.ordenar(paginaFiltradas, solicitud.getParameterMap());
            PaginacionUtilidad.Pagina<Tema> pagina = PaginacionUtilidad.paginar(paginaFiltradas, solicitud.getParameterMap(), TABLA.opciones(temas));
            PaginacionUtilidad.aModelo(modelo, pagina);
            modelo.addAttribute("temasPagina", pagina.filas());
            // Si cada tema tiene todo su contenido (según las modalidades del curso)
            Map<Integer, Boolean> contenidoCompleto = new java.util.HashMap<>();
            temas.forEach(t -> contenidoCompleto.put(t.getIdTema(), disenoCursoServicio.contenidoDe(curso, t).getFaltantes().isEmpty()));
            modelo.addAttribute("contenidoCompleto", contenidoCompleto);
            if (PaginacionUtilidad.pideSoloFilas(solicitud)) {
                modelo.addAttribute("puedeGestionar", puedeGestionar);
                return "pages/tema/buscar :: filas"; // solo las filas de la página pedida
            }
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
            if (hayNuevos) {
                // Alerta guía: con el mínimo de temas, el paso siguiente es armar el cronograma (o el que corresponda)
                return exitoConPasoSiguiente(mensaje, cursoId, usuario.getDocente());
            }
            return RespuestaFormularioUtilidad.respuestaExitosa(mensaje);
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
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

    /** Obtiene el docente cuyas participaciones limitan la operación, o {@code null} si la hace el administrador. */
    private Docente docenteQueGestiona(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return usuario.esAdministradorActivo() ? null : usuario.getDocente();
    }

    // ---------------------------------------------------------------- Listado (motor común)

    private static String dia(java.time.LocalDateTime fecha) {
        return fecha == null ? "" : fecha.toString().replace('T', ' ');
    }

    /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
    private static final FiltroUtilidad<Tema> TABLA = new FiltroUtilidad<>(List.of(
            new FiltroUtilidad.Columna<Tema>("titulo", FiltroUtilidad.Tipo.TEXTO, Tema::getTitulo),
            new FiltroUtilidad.Columna<Tema>("descripcion", FiltroUtilidad.Tipo.TEXTO, Tema::getDescripcion),
            new FiltroUtilidad.Columna<Tema>("contenido", FiltroUtilidad.Tipo.TEXTO, Tema::getContenido),
            new FiltroUtilidad.Columna<Tema>("ultimaModificacion", FiltroUtilidad.Tipo.FECHA, t -> dia(t.getUltimaModificacion())),
            new FiltroUtilidad.Columna<Tema>("fechaCreacion", FiltroUtilidad.Tipo.FECHA, t -> dia(t.getFechaCreacion()))),
            t -> false);
}
