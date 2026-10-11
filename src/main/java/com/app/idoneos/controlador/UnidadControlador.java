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

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Unidad.UnidadServicio;
import com.app.idoneos.utilidades.RespuestaUtilidad;

/**
 * Controlador del ABM de unidades (MOD-F-02): CU-19 Buscar unidad (GET /unidad/buscar), CU-20 Agregar unidad
 * (POST /unidad/registrar), CU-21 Modificar unidad (POST /unidad/modificar/{id}) y CU-22 Dar de baja unidad (quitarla del
 * cronograma de un programa: POST /unidad/darDeBaja/{id} y /unidad/darDeBajaMasiva).
 *
 * El listado muestra el cronograma del programa por defecto del docente (o el vigente, si lo ve el administrador). El
 * docente (titular o ayudante) agrega y modifica unidades de los cursos en los que participa; el administrador puede
 * buscar y dar de baja las de cualquier curso.
 */
@Controller
@RequestMapping("/unidad")
public class UnidadControlador {

    @Autowired
    private UnidadServicio unidadServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    /**
     * CU-19: Lista las unidades del cronograma del programa de trabajo de un curso. Si se indica {@code nuevo}, abre
     * sola la ventana para agregar una unidad; con {@code accion} y {@code unidadId} abre la ventana de modificar o de
     * dar de baja esa unidad (son los destinos del botón "Registrar unidad" y del menú de cada unidad de la vista del
     * curso).
     *
     * @param cursoId            Identificador del curso.
     * @param titulo             Parte del título (opcional).
     * @param nuevo              Presente para abrir el formulario de alta (solo docente).
     * @param accion             "editar" o "baja": abre sola la ventana de esa unidad.
     * @param unidadId           Identificador de la unidad de la acción (opcional).
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del listado, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarUnidades(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "unidadId", required = false) Integer unidadId, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            boolean esAdministrador = usuario.esAdministradorActivo();
            if (cursoId == null) {
                throw new IllegalArgumentException("Error! Debe seleccionar un curso.");
            }
            Curso curso;
            Programa programa;
            if (esAdministrador) {
                curso = cursoServicio.buscarPorId(cursoId)
                        .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
                programa = curso.getProgramaVigente();
            } else {
                ParticipacionDocente participacion = cursoServicio.validarAccesoDocente(cursoId, usuario.getDocente());
                curso = participacion.getCurso();
                programa = participacion.getProgramaDeTrabajo();
            }
            if (programa == null) {
                throw new IllegalArgumentException("Error! El curso no tiene un programa vigente.");
            }
            List<UnidadCronograma> unidades = unidadServicio.buscarConFiltros(programa, titulo);

            String abrirModal = null;
            if (!esAdministrador && nuevo != null) {
                abrirModal = "modalNuevaUnidad";
            } else if (accion != null && unidadId != null && programa.getCronogramaOrdenado().stream()
                    .anyMatch(c -> c.getUnidad().getIdUnidad() == unidadId)) {
                if ("baja".equals(accion)) {
                    abrirModal = "modalBajaUnidad" + unidadId;
                } else if ("editar".equals(accion) && !esAdministrador) {
                    abrirModal = "modalEditarUnidad" + unidadId;
                }
            }

            List<Unidad> unidadesDelCurso = unidadServicio.buscarPorCurso(curso).stream()
                    .filter(u -> !u.getProgramasQueLaIncluyen().isEmpty()).toList();
            modelo.addAttribute("curso", curso);
            modelo.addAttribute("programa", programa);
            modelo.addAttribute("unidades", unidades);
            modelo.addAttribute("programasActivos", curso.getProgramasActivos());
            modelo.addAttribute("unidadesDelCurso", unidadesDelCurso);
            modelo.addAttribute("tituloBusqueda", titulo);
            modelo.addAttribute("abrirModal", abrirModal);
            modelo.addAttribute("esDocente", !esAdministrador);
            modelo.addAttribute("puedeGestionar", !esAdministrador);
            modelo.addAttribute("urlBase", "/curso/acceder/" + cursoId);
            modelo.addAttribute("urlMisCursos", "/curso/buscar");
            modelo.addAttribute("titulo", "Unidades | Idóneos Online");
            modelo.addAttribute("menuActivo", "unidades");
            return "pages/unidad/buscar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/curso/buscar";
        }
    }

    /**
     * CU-20: Agrega una unidad (nueva o ya existente en otro programa del curso) al final del cronograma de un programa.
     * Responde en JSON.
     *
     * @param programaId        Identificador del programa.
     * @param unidadExistenteId Unidad de otro programa que se incorpora (opcional).
     * @param titulo            El título de la unidad nueva.
     * @param descripcion       La descripción de la unidad nueva (opcional).
     * @param contenido         El contenido de la unidad nueva.
     * @param auth              La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> agregarUnidad(
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "unidadExistenteId", required = false) Integer unidadExistenteId,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "contenido", required = false) String contenido, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Unidad unidad = unidadServicio.agregarUnidad(usuario.getDocente(), programaId, unidadExistenteId, titulo,
                    descripcion, contenido);
            String accion = unidadExistenteId != null ? "incorporada" : "registrada";
            return RespuestaUtilidad.respuestaExitosa("Unidad '" + unidad.getTitulo() + "' " + accion + " con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-21: Modifica una unidad sin inscripciones activas asociadas. Responde en JSON.
     *
     * @param id          Identificador de la unidad.
     * @param titulo      El título.
     * @param descripcion La descripción (opcional).
     * @param contenido   El contenido.
     * @param auth        La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarUnidad(@PathVariable("id") Integer id,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "contenido", required = false) String contenido, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            unidadServicio.modificarUnidad(id, usuario.getDocente(), titulo, descripcion, contenido);
            return RespuestaUtilidad.respuestaExitosa("Unidad modificada correctamente.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-22: Da de baja una unidad del cronograma de un programa. Responde en JSON.
     *
     * @param id         Identificador de la unidad.
     * @param programaId Identificador del programa.
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaUnidad(@PathVariable("id") Integer id,
            @RequestParam(value = "programaId", required = false) Integer programaId, Authentication auth) {
        try {
            unidadServicio.darDeBajaUnidad(id, programaId, docenteQueGestiona(auth));
            return RespuestaUtilidad.respuestaExitosa("Unidad dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-22: Da de baja varias unidades del cronograma de un programa, todas o ninguna. Responde en JSON.
     *
     * @param ids        Identificadores de las unidades seleccionadas.
     * @param programaId Identificador del programa.
     * @param auth       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarias(
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "programaId", required = false) Integer programaId, Authentication auth) {
        try {
            unidadServicio.darDeBajaVarias(ids, programaId, docenteQueGestiona(auth));
            return RespuestaUtilidad.respuestaExitosa("Unidades dadas de baja con éxito.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /** Obtiene el docente cuyas participaciones limitan la operación, o {@code null} si la hace el administrador. */
    private Docente docenteQueGestiona(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return usuario.esAdministradorActivo() ? null : usuario.getDocente();
    }
}
