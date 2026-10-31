package com.app.idoneos.controlador;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.app.idoneos.servicio.Tema.TemaServicio;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Programa.ProgramaServicio;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Controlador del ABM de programas (MOD-F-02): CU-15 Buscar programa (GET /programa/buscar), CU-16 Registrar programa
 * (POST /programa/registrar), CU-17 Modificar programa (POST /programa/modificar/{id}) y CU-18 Dar de baja programa
 * (POST /programa/darDeBaja/{id}).
 *
 * El docente (titular o ayudante) gestiona los programas de los cursos en los que participa; el administrador puede
 * buscar y dar de baja los de cualquier curso.
 */
@Controller
@RequestMapping("/programa")
public class ProgramaControlador extends PanelCursoControlador {

    @Autowired
    private ProgramaServicio programaServicio;

    @Autowired
    private TemaServicio temaServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-15: Busca los programas de un curso por nombre y estado (Vigente, Anterior o Dado de baja).
     *
     * @param cursoId            Identificador del curso.
     * @param nombre             Parte del nombre (opcional).
     * @param estado             Estado del programa (opcional).
     * @param nuevo              Presente para abrir el formulario de alta (solo docente): es el destino del botón
     *                           "Registrar programa" de un curso que todavía no tiene programas.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarProgramas(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "editar", required = false) Integer editar, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            boolean esAdministrador = contexto.esAdministrador();
            Curso curso = contexto.curso();
            List<Programa> programas = programaServicio.buscarConFiltros(curso, null, null); // listado completo: los filtros se aplican en la pantalla

            cargarContexto(modelo, contexto, "programas");
            modelo.addAttribute("programas", programas);
            modelo.addAttribute("programasActivos", curso.getProgramasActivos());
            // Si cada programa está completo y, por tema del curso, el contenido que tiene (para el asistente de programas)
            Map<Integer, Boolean> completos = new HashMap<>();
            programas.forEach(p -> completos.put(p.getIdPrograma(), disenoCursoServicio.estaCompleto(curso, p)));
            modelo.addAttribute("completos", completos);
            List<Tema> temasDelCurso = temaServicio.buscarPorCurso(curso);
            modelo.addAttribute("temasDelCurso", temasDelCurso);
            Map<Integer, Map<String, Boolean>> contenido = new LinkedHashMap<>();
            temasDelCurso.forEach(t -> contenido.put(t.getIdTema(), disenoCursoServicio.contenidoDe(curso, t).celdas()));
            modelo.addAttribute("contenidoPorTema", contenido);
            modelo.addAttribute("columnasContenido", disenoCursoServicio.columnasDeContenido(curso));
            // Lo que necesita el asistente de programas (se lee en la página como JSON)
            List<Map<String, Object>> temasJson = new java.util.ArrayList<>();
            temasDelCurso.forEach(t -> temasJson.add(Map.of("id", t.getIdTema(), "titulo", t.getTitulo(),
                    "contenido", contenido.get(t.getIdTema()))));
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("cursoId", curso.getIdCurso());
            datos.put("duracionDias", curso.getDuracionDias());
            datos.put("temas", temasJson);
            datos.put("columnas", disenoCursoServicio.columnasDeContenido(curso));
            modelo.addAttribute("datos", datos);
            modelo.addAttribute("abrirModal", !esAdministrador && (nuevo != null || editar != null) ? "modalPrograma" : null);
            modelo.addAttribute("programaAEditar", editar);
            modelo.addAttribute("nombreBusqueda", nombre);
            modelo.addAttribute("estadoSeleccionado", estado);
            modelo.addAttribute("puedeGestionar", !esAdministrador);
            modelo.addAttribute("titulo", "Programas | Idóneos Online");
            return "pages/programa/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-16: Registra un programa para un curso, opcionalmente a partir de uno anterior. Responde en JSON.
     *
     * @param cursoId          Identificador del curso.
     * @param programaOrigenId Programa del que se parte (opcional).
     * @param nombre           El nombre (opcional: si no se indica, es "Versión N").
     * @param descripcion      Qué cambia respecto de la versión anterior (opcional).
     * @param auth             La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarPrograma(
            @RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "programaOrigenId", required = false) Integer programaOrigenId,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "conCronograma", required = false) Boolean conCronograma,
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "dias", required = false) List<String> dias, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Programa programa = programaServicio.registrarPrograma(usuario.getDocente(), cursoId, programaOrigenId,
                    nombre, descripcion, cronogramaRecibido(conCronograma, ids), dias);
            return RespuestaFormularioUtilidad.respuestaExitosa("Programa '" + programa.getNombre() + "' registrado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-17: Modifica un programa sin inscripciones activas. Responde en JSON.
     *
     * @param id           Identificador del programa.
     * @param nombre       El nombre.
     * @param descripcion  Qué cambia respecto de la versión anterior (opcional).
     * @param auth         La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarPrograma(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "conCronograma", required = false) Boolean conCronograma,
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "dias", required = false) List<String> dias, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            programaServicio.modificarPrograma(id, usuario.getDocente(), nombre, descripcion,
                    cronogramaRecibido(conCronograma, ids), dias);
            return RespuestaFormularioUtilidad.respuestaExitosa("Programa modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Los temas del cronograma que envió el asistente: con {@code conCronograma} y sin temas, un cronograma vacío (no
     * se parte del programa de origen); sin {@code conCronograma}, el cronograma no se toca.
     */
    private static List<Integer> cronogramaRecibido(Boolean conCronograma, List<Integer> ids) {
        if (!Boolean.TRUE.equals(conCronograma)) {
            return ids;
        }
        return ids == null ? List.of() : ids;
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-18: Da de baja un programa sin cohortes activas. Responde en JSON.
     *
     * @param id   Identificador del programa.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaPrograma(@PathVariable("id") Integer id, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            // El administrador puede dar de baja los programas de cualquier curso
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            programaServicio.darDeBajaPrograma(id, docente);
            return RespuestaFormularioUtilidad.respuestaExitosa("Programa dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Da de baja varios programas a la vez, todos o ninguno. Responde en JSON.
     *
     * @param ids  Identificadores de los programas seleccionados.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Docente docente = usuario.esAdministradorActivo() ? null : usuario.getDocente();
            programaServicio.darDeBajaVarios(ids, docente);
            return RespuestaFormularioUtilidad.respuestaExitosa("Programas dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }
}
