package com.app.idoneos.controlador;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.app.idoneos.modelo.CursoModalidad;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Docente.DocenteServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Nivel.NivelServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;
import com.app.idoneos.utilidades.Utilidades;

/**
 * Controlador de la gestión de cursos (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso:
 * CU-01 Buscar curso (GET /cursos), CU-03 Registrar curso (POST /cursos/guardar),
 * CU-04 Modificar curso (POST /cursos/{id}/editar) y CU-05 Dar de baja curso (POST /cursos/{id}/baja).
 * Los formularios de alta, modificación y baja se muestran como ventanas modales de la pantalla de búsqueda.
 */
@Controller
@RequestMapping("/cursos")
public class CursoControlador {

    private static final int TAMANIO_PAGINA = 8;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private NivelServicioImpl nivelServicio;

    @Autowired
    private ModalidadServicioImpl modalidadServicio;

    @Autowired
    private DocenteServicioImpl docenteServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    /**
     * CU-01: Busca cursos según nombre, categoría, nivel, equipo docente y modalidad.
     * Si el usuario es docente (y no administrador), el resultado se restringe a los cursos
     * en los que participa como titular o ayudante.
     *
     * @param busqueda          Parte del nombre o la descripción del curso.
     * @param categoriaId       Identificador de la categoría.
     * @param nivelId           Identificador del nivel.
     * @param docenteId         Identificador de un docente del equipo docente.
     * @param modalidadId       Identificador de la modalidad de dictado.
     * @param ordenBajasPrimero Si es true, los cursos dados de baja se listan primero.
     * @param page              Número de página (empieza en 0).
     * @param modelo            El modelo de la vista.
     * @param auth              La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de búsqueda de cursos.
     */
    @GetMapping
    public String buscarCursos(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "docenteId", required = false) Integer docenteId,
            @RequestParam(value = "modalidadId", required = false) Integer modalidadId,
            @RequestParam(value = "ordenBajasPrimero", defaultValue = "false") boolean ordenBajasPrimero,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model modelo, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Docente soloDeDocente = (usuario.esDocente() && !usuario.esAdmin()) ? usuario.getDocente() : null;

            List<Curso> cursos = cursoServicio.buscarConFiltros(busqueda, categoriaId, nivelId, docenteId,
                    modalidadId, ordenBajasPrimero, soloDeDocente);
            int totalPaginas = Utilidades.calcularTotalPaginas(cursos.size(), TAMANIO_PAGINA);
            int pagina = Utilidades.ajustarPagina(page, totalPaginas);
            List<Curso> cursosPagina = Utilidades.obtenerPagina(cursos, pagina, TAMANIO_PAGINA);

            // Inscripciones y programas activos por curso, para validar la baja y la modificación
            Map<Integer, List<Inscripcion>> inscripcionesPorCurso = new HashMap<>();
            Map<Integer, List<Programa>> programasPorCurso = new HashMap<>();
            Set<Integer> cursosConInscripcionAbierta = new HashSet<>();
            for (Curso curso : cursosPagina) {
                inscripcionesPorCurso.put(curso.getIdCurso(), cursoServicio.buscarInscripcionesActivas(curso));
                programasPorCurso.put(curso.getIdCurso(), programaServicio.buscarPorCurso(curso));
                if (!cursoServicio.buscarCohortesAbiertas(curso).isEmpty()) {
                    cursosConInscripcionAbierta.add(curso.getIdCurso());
                }
            }

            modelo.addAttribute("cursos", cursosPagina);
            modelo.addAttribute("inscripcionesPorCurso", inscripcionesPorCurso);
            modelo.addAttribute("programasPorCurso", programasPorCurso);
            modelo.addAttribute("cursosConInscripcionAbierta", cursosConInscripcionAbierta);
            modelo.addAttribute("totalCursos", cursos.size());
            modelo.addAttribute("currentPage", pagina);
            modelo.addAttribute("totalPages", totalPaginas);
            modelo.addAttribute("categorias", categoriaServicio.obtenerTodo());
            modelo.addAttribute("niveles", nivelServicio.obtenerTodo());
            modelo.addAttribute("modalidades", modalidadServicio.obtenerTodo());
            modelo.addAttribute("docentes", docenteServicio.buscarHabilitados());
            modelo.addAttribute("busqueda", busqueda);
            modelo.addAttribute("categoriaSeleccionada", categoriaId);
            modelo.addAttribute("nivelSeleccionado", nivelId);
            modelo.addAttribute("docenteSeleccionado", docenteId);
            modelo.addAttribute("modalidadSeleccionada", modalidadId);
            modelo.addAttribute("ordenBajasPrimero", ordenBajasPrimero);
            modelo.addAttribute("titulo", "CU-01 - Buscar curso | Idóneos Online");
            return "pages/cursos/cu-01-buscar-curso";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inicio";
        }
    }

    /**
     * CU-03: Registra un curso con sus modalidades de dictado y su equipo docente.
     *
     * @param nombre             El nombre del curso.
     * @param descripcion        La descripción del curso.
     * @param precio             El precio del curso.
     * @param imagen             La ruta de la imagen de portada (opcional).
     * @param categoriaId        Identificador de la categoría.
     * @param nivelId            Identificador del nivel.
     * @param emiteCertificado   Si el curso emite certificado al finalizar.
     * @param idsModalidades     Identificadores de las modalidades de dictado.
     * @param docenteTitularId   Identificador del docente titular.
     * @param docenteAyudanteId  Identificador del docente ayudante (opcional).
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al listado de cursos, o al formulario si hubo un error.
     */
    @PostMapping("/guardar")
    public String registrarCurso(@RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "precio", required = false) Float precio,
            @RequestParam(value = "imagen", required = false) String imagen,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "emiteCertificado", defaultValue = "false") boolean emiteCertificado,
            @RequestParam(value = "idsModalidades", required = false) List<Integer> idsModalidades,
            @RequestParam(value = "docenteTitularId", required = false) Integer docenteTitularId,
            @RequestParam(value = "docenteAyudanteId", required = false) Integer docenteAyudanteId,
            RedirectAttributes redirectAttributes) {
        try {
            Curso curso = cursoServicio.registrarCurso(nombre, descripcion, precio, imagen, categoriaId, nivelId,
                    emiteCertificado, idsModalidades, docenteTitularId, docenteAyudanteId);
            redirectAttributes.addFlashAttribute("mensaje", "Curso '" + curso.getNombre() + "' registrado con éxito.");
            return "redirect:/cursos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos";
        }
    }

    /**
     * CU-04: Modifica un curso activo.
     *
     * @param id                 Identificador del curso.
     * @param nombre             El nombre del curso.
     * @param descripcion        La descripción del curso.
     * @param precio             El precio del curso.
     * @param imagen             La ruta de la imagen de portada (opcional).
     * @param categoriaId        Identificador de la categoría.
     * @param nivelId            Identificador del nivel.
     * @param emiteCertificado   Si el curso emite certificado al finalizar.
     * @param idsModalidades     Identificadores de las modalidades de dictado.
     * @param docenteTitularId   Identificador del docente titular.
     * @param docenteAyudanteId  Identificador del docente ayudante (opcional).
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al listado de cursos, o al formulario si hubo un error.
     */
    @PostMapping("/{id}/editar")
    public String modificarCurso(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "precio", required = false) Float precio,
            @RequestParam(value = "imagen", required = false) String imagen,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "emiteCertificado", defaultValue = "false") boolean emiteCertificado,
            @RequestParam(value = "idsModalidades", required = false) List<Integer> idsModalidades,
            @RequestParam(value = "docenteTitularId", required = false) Integer docenteTitularId,
            @RequestParam(value = "docenteAyudanteId", required = false) Integer docenteAyudanteId,
            RedirectAttributes redirectAttributes) {
        try {
            cursoServicio.modificarCurso(id, nombre, descripcion, precio, imagen, categoriaId, nivelId,
                    emiteCertificado, idsModalidades, docenteTitularId, docenteAyudanteId);
            redirectAttributes.addFlashAttribute("mensaje", "Curso modificado correctamente.");
            return "redirect:/cursos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cursos";
        }
    }

    /**
     * CU-05: Da de baja un curso que no tenga programas ni unidades activas asociadas.
     *
     * @param id                 Identificador del curso.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return Una redirección al listado de cursos.
     */
    @PostMapping("/{id}/baja")
    public String darDeBajaCurso(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            cursoServicio.darDeBajaCurso(id);
            redirectAttributes.addFlashAttribute("mensaje", "Curso dado de baja exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos";
    }
}
