package com.app.idoneos.controlador;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Docente.DocenteServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Nivel.NivelServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;
import com.app.idoneos.utilidades.PaginacionUtilidad;

/**
 * Controlador del catálogo público de cursos (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso:
 * CU-06 Explorar catálogo de cursos (GET /catalogo) y su ficha de curso (GET /catalogo/ficha/{id}).
 */
@Controller
@RequestMapping("/catalogo")
public class CatalogoControlador {

    /** Cantidad de cursos por página por defecto. El valor 0 significa "todos". */
    private static final int CURSOS_POR_PAGINA = 10;

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
     * CU-06: Explora el catálogo público de cursos con cohortes con inscripción abierta, con o sin sesión
     * iniciada.
     *
     * @param busqueda    Parte del nombre o la descripción del curso.
     * @param categoriaId Identificador de la categoría.
     * @param nivelId     Identificador del nivel.
     * @param docenteId   Identificador de un docente del equipo docente.
     * @param modalidadId Identificador de la modalidad de dictado.
     * @param porPagina   Cantidad de cursos por página (10, 25, 50 o 0 para ver todos).
     * @param page        Número de página (empieza en 0).
     * @param modelo      El modelo de la vista.
     * @return La vista del catálogo de cursos.
     */
    @GetMapping
    public String explorarCatalogo(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "docenteId", required = false) Integer docenteId,
            @RequestParam(value = "modalidadId", required = false) Integer modalidadId,
            @RequestParam(value = "porPagina", defaultValue = "" + CURSOS_POR_PAGINA) int porPagina,
            @RequestParam(value = "page", defaultValue = "0") int page, Model modelo) {
        List<Curso> cursos = cursoServicio.buscarEnCatalogo(busqueda, categoriaId, nivelId, docenteId, modalidadId);
        int tamanioPagina = porPagina > 0 ? porPagina : Math.max(cursos.size(), 1);
        int totalPaginas = PaginacionUtilidad.calcularTotalPaginas(cursos.size(), tamanioPagina);
        int pagina = PaginacionUtilidad.ajustarPagina(page, totalPaginas);
        List<Curso> cursosPagina = PaginacionUtilidad.obtenerPagina(cursos, pagina, tamanioPagina);

        // Cantidad de unidades temáticas de cada curso (según su primer programa activo)
        Map<Integer, Integer> cantidadUnidadesPorCurso = new HashMap<>();
        for (Curso curso : cursosPagina) {
            List<Programa> programas = programaServicio.buscarPorCurso(curso);
            cantidadUnidadesPorCurso.put(curso.getIdCurso(),
                    programas.isEmpty() ? 0 : programas.get(0).getCantidadUnidades());
        }

        modelo.addAttribute("cursos", cursosPagina);
        modelo.addAttribute("cantUnidadesPorCurso", cantidadUnidadesPorCurso);
        modelo.addAttribute("currentPage", pagina);
        modelo.addAttribute("totalPages", totalPaginas);
        modelo.addAttribute("totalCursos", cursos.size());
        modelo.addAttribute("desdeCurso", cursosPagina.isEmpty() ? 0 : pagina * tamanioPagina + 1);
        modelo.addAttribute("hastaCurso", cursosPagina.isEmpty() ? 0 : pagina * tamanioPagina + cursosPagina.size());
        modelo.addAttribute("porPagina", porPagina);
        modelo.addAttribute("categorias", categoriaServicio.obtenerTodo());
        modelo.addAttribute("niveles", nivelServicio.obtenerTodo());
        modelo.addAttribute("modalidades", modalidadServicio.obtenerTodo());
        modelo.addAttribute("docentes", docenteServicio.buscarHabilitados());
        modelo.addAttribute("busqueda", busqueda);
        modelo.addAttribute("categoriaSeleccionada", categoriaId);
        modelo.addAttribute("nivelSeleccionado", nivelId);
        modelo.addAttribute("docenteSeleccionado", docenteId);
        modelo.addAttribute("modalidadSeleccionada", modalidadId);
        modelo.addAttribute("titulo", "Catálogo de cursos | Idóneos Online");
        return "pages/catalogo";
    }

    /**
     * CU-06: Muestra la ficha de un curso: sus datos, la estructura de contenidos por unidad y las cohortes
     * con inscripción abierta.
     *
     * @param id                 Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de la ficha del curso, o una redirección al catálogo si el curso no está activo.
     */
    @GetMapping("/ficha/{id}")
    public String verFichaCurso(@PathVariable("id") Integer id, Model modelo, RedirectAttributes redirectAttributes) {
        Curso curso = cursoServicio.buscarPorId(id).orElse(null);
        if (curso == null) {
            redirectAttributes.addFlashAttribute("error", "Error! El curso no se encuentra disponible.");
            return "redirect:/catalogo";
        }
        modelo.addAttribute("cursoSeleccionado", curso);
        modelo.addAttribute("cohortesAbiertas", cursoServicio.buscarCohortesAbiertas(curso));
        List<Programa> programas = programaServicio.buscarPorCurso(curso);
        if (!programas.isEmpty()) {
            Programa programa = programas.get(0);
            modelo.addAttribute("programaSeleccionado", programa);
            modelo.addAttribute("cronogramas", programa.getUnidadesCronograma().stream()
                    .filter(cronograma -> !cronograma.getBaja())
                    .sorted(Comparator.comparingInt(cronograma -> cronograma.getNumeroOrden())).toList());
        }
        modelo.addAttribute("titulo", curso.getNombre() + " | Idóneos Online");
        return "pages/ficha";
    }
}
