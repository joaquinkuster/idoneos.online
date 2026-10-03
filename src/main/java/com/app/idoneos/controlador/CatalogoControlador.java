package com.app.idoneos.controlador;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Docente.DocenteServicioImpl;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Nivel.NivelServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;
import com.app.idoneos.utilidades.Utilidades;

/**
 * Controlador del catálogo de cursos (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso:
 * CU-06 Explorar catálogo de cursos (GET /cursos/catalogo) y
 * CU-02 Ver mis cursos (GET /cursos/mis-cursos).
 */
@Controller
@RequestMapping("/cursos")
public class CatalogoControlador {

    private static final int TAMANIO_PAGINA = 4;

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

    @Autowired
    private InscripcionServicioImpl inscripcionServicio;

    /**
     * CU-06: Explora el catálogo público de cursos con cohortes con inscripción abierta, con o sin sesión
     * iniciada, y muestra la ficha del curso seleccionado (o del primero de la página, por defecto).
     *
     * @param busqueda    Parte del nombre o la descripción del curso.
     * @param categoriaId Identificador de la categoría.
     * @param nivelId     Identificador del nivel.
     * @param docenteId   Identificador de un docente del equipo docente.
     * @param modalidadId Identificador de la modalidad de dictado.
     * @param cursoId     Identificador del curso del que se muestra la ficha.
     * @param page        Número de página (empieza en 0).
     * @param modelo      El modelo de la vista.
     * @return La vista del catálogo de cursos.
     */
    @GetMapping("/catalogo")
    public String explorarCatalogo(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "docenteId", required = false) Integer docenteId,
            @RequestParam(value = "modalidadId", required = false) Integer modalidadId,
            @RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "page", defaultValue = "0") int page, Model modelo) {
        List<Curso> cursos = cursoServicio.buscarEnCatalogo(busqueda, categoriaId, nivelId, docenteId, modalidadId);
        int totalPaginas = Utilidades.calcularTotalPaginas(cursos.size(), TAMANIO_PAGINA);
        int pagina = Utilidades.ajustarPagina(page, totalPaginas);
        List<Curso> cursosPagina = Utilidades.obtenerPagina(cursos, pagina, TAMANIO_PAGINA);

        // Cantidad de unidades temáticas de cada curso (según su primer programa activo)
        Map<Integer, Integer> cantidadUnidadesPorCurso = new HashMap<>();
        for (Curso curso : cursos) {
            List<Programa> programas = programaServicio.buscarPorCurso(curso);
            cantidadUnidadesPorCurso.put(curso.getIdCurso(),
                    programas.isEmpty() ? 0 : programas.get(0).getCantidadUnidades());
        }

        // Curso seleccionado: el indicado, o el primero de la página
        Curso seleccionado = cursoId == null ? null
                : cursos.stream().filter(curso -> curso.getIdCurso() == cursoId).findFirst().orElse(null);
        if (seleccionado == null && !cursosPagina.isEmpty()) {
            seleccionado = cursosPagina.get(0);
        }
        if (seleccionado != null) {
            modelo.addAttribute("cursoSeleccionado", seleccionado);
            modelo.addAttribute("cohortesAbiertas", cursoServicio.buscarCohortesAbiertas(seleccionado));
            List<Programa> programas = programaServicio.buscarPorCurso(seleccionado);
            if (!programas.isEmpty()) {
                Programa programa = programas.get(0);
                modelo.addAttribute("programaSeleccionado", programa);
                modelo.addAttribute("cronogramas", programa.getUnidadesCronograma().stream()
                        .filter(cronograma -> !cronograma.getBaja())
                        .sorted(Comparator.comparingInt(cronograma -> cronograma.getNumeroOrden())).toList());
            }
        }

        modelo.addAttribute("cursos", cursosPagina);
        modelo.addAttribute("cantUnidadesPorCurso", cantidadUnidadesPorCurso);
        modelo.addAttribute("currentPage", pagina);
        modelo.addAttribute("totalPages", totalPaginas);
        modelo.addAttribute("totalCursos", cursos.size());
        modelo.addAttribute("categorias", categoriaServicio.obtenerTodo());
        modelo.addAttribute("niveles", nivelServicio.obtenerTodo());
        modelo.addAttribute("modalidades", modalidadServicio.obtenerTodo());
        modelo.addAttribute("docentes", docenteServicio.buscarHabilitados());
        modelo.addAttribute("busqueda", busqueda);
        modelo.addAttribute("categoriaSeleccionada", categoriaId);
        modelo.addAttribute("nivelSeleccionado", nivelId);
        modelo.addAttribute("docenteSeleccionado", docenteId);
        modelo.addAttribute("modalidadSeleccionada", modalidadId);
        modelo.addAttribute("titulo", "CU-06 - Catálogo de Cursos | Idóneos Online");
        return "pages/cursos/cu-06-explorar-catalogo-de-cursos";
    }

    /**
     * Redirige a la ficha de un curso dentro del catálogo.
     *
     * @param id Identificador del curso.
     * @return Una redirección al catálogo con el curso seleccionado.
     */
    @GetMapping("/{id}/ficha")
    public String verFichaCurso(@PathVariable("id") Integer id) {
        return "redirect:/cursos/catalogo?cursoId=" + id;
    }

    /**
     * CU-02: Lista los cursos en los que el alumno está inscripto, con su progreso general,
     * filtrando por nombre del curso y estado de la inscripción (Pendiente, En Progreso o Finalizado).
     *
     * @param busqueda           Parte del nombre del curso.
     * @param estado             Estado de la inscripción.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Ver mis cursos".
     */
    @GetMapping("/mis-cursos")
    public String verMisCursos(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "estado", required = false) String estado, Model modelo, Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Alumno alumno = usuario.getAlumno();
            if (alumno == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
            }
            List<Inscripcion> inscripciones = inscripcionServicio.buscarMisCursos(alumno, busqueda, estado);
            modelo.addAttribute("inscripciones", inscripciones);
            modelo.addAttribute("busqueda", busqueda);
            modelo.addAttribute("estadoSeleccionado", estado);
            modelo.addAttribute("titulo", "CU-02 - Mis Cursos | Idóneos Online");
            return "pages/cursos/cu-02-ver-mis-cursos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inicio";
        }
    }
}
