package com.app.idoneos.controller;

import com.app.idoneos.util.PaginacionUtil;

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

import com.app.idoneos.model.Usuario;
import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.service.pago.PagoService;
import com.app.idoneos.service.descuento.DescuentoService;
import com.app.idoneos.service.categoria.CategoriaService;
import com.app.idoneos.service.curso.CursoService;
import com.app.idoneos.service.docente.DocenteService;
import com.app.idoneos.service.modalidad.ModalidadService;
import com.app.idoneos.service.nivel.NivelService;
import com.app.idoneos.service.programa.ProgramaService;

/**
 * Controlador del catálogo público de cursos (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso:
 * CU-06 Explorar catálogo de cursos (GET /catalogo) y su ficha de curso (GET /catalogo/ficha/{id}).
 */
@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    @Autowired
    private com.app.idoneos.service.recordatoriocohorte.RecordatorioCohorteService recordatorioService;

    @Autowired
    private com.app.idoneos.service.parametro.ParametroService parametroService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private DescuentoService descuentoService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private NivelService nivelService;

    @Autowired
    private ModalidadService modalidadService;

    @Autowired
    private DocenteService docenteService;

    @Autowired
    private ProgramaService programaService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-06: Explora el catálogo público de cursos con cohortes con inscripción abierta, con o sin sesión
     * iniciada. Entrega todos los cursos: los filtros y la paginación se aplican en el navegador.
     *
     * @param busqueda    Parte del nombre o la descripción del curso.
     * @param categoriaId Identificador de la categoría.
     * @param nivelId     Identificador del nivel.
     * @param docenteId   Identificador de un docente del equipo docente.
     * @param modalidadId Identificador de la modalidad de dictado.
     * @param model      El modelo de la vista.
     * @return La vista del catálogo de cursos.
     */
    @GetMapping
    public String explorar(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "docenteId", required = false) Integer docenteId,
            @RequestParam(value = "modalidadId", required = false) Integer modalidadId, Model model, Authentication authentication,
            jakarta.servlet.http.HttpServletRequest request) {
        List<Curso> todos = cursoService.buscarEnCatalogo(null, null, null, null, null);
        // El filtro (también el de la URL) y la página los resuelve el servidor: cards, 8 por página
        PaginacionUtil.Pagina<Curso> pagina = CursoController.Filtros.cards.paginar(todos, request.getParameterMap(), 8);
        PaginacionUtil.cargarEnModelo(model, pagina);
        List<Curso> cursos = pagina.filas();

        // Los cursos que todavía no tienen una cohorte con la inscripción abierta, solo próximas: la card dice «Próximamente»
        java.util.Set<Integer> cursosProximamente = new java.util.HashSet<>();
        cursos.stream().filter(curso -> cursoService.buscarCohortesAbiertas(curso).isEmpty())
                .forEach(curso -> cursosProximamente.add(curso.getIdCurso()));
        model.addAttribute("cursosProximamente", cursosProximamente);

        // Cantidad de temas de cada curso (según su primer programa activo)
        Map<Integer, Integer> cantidadTemasPorCurso = new HashMap<>();
        for (Curso curso : cursos) {
            List<Programa> programas = programaService.buscarPorCurso(curso);
            cantidadTemasPorCurso.put(curso.getIdCurso(),
                    programas.isEmpty() ? 0 : programas.get(0).getCantidadTemas());
        }

        // Descuentos vigentes: las cards de los cursos con costo avisan que hay descuentos
        model.addAttribute("descuentosVigentes", descuentoService.buscarVigentes());
        model.addAttribute("descuentoAplicable", pagoService.calcularDescuentoPara(alumnoDe(authentication)).orElse(null));
        model.addAttribute("cursos", cursos);
        model.addAttribute("cantTemasPorCurso", cantidadTemasPorCurso);
        model.addAttribute("totalCursos", todos.size());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/catalogo :: filas"; // solo las cards de la página pedida
        }
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("niveles", nivelService.obtenerTodo());
        model.addAttribute("modalidades", modalidadService.obtenerTodo());
        model.addAttribute("docentes", docenteService.buscarHabilitados());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("nivelSeleccionado", nivelId);
        model.addAttribute("docenteSeleccionado", docenteId);
        model.addAttribute("modalidadSeleccionada", modalidadId);
        model.addAttribute("titulo", "Catálogo de cursos | Idóneos Online");
        return "pages/catalogo";
    }

    /**
     * CU-06: Muestra la ficha de un curso: sus datos, la estructura de contenidos por tema y las cohortes
     * con inscripción abierta.
     *
     * @param id                 Identificador del curso.
     * @param model             El modelo de la vista.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de la ficha del curso, o una redirección al catálogo si el curso no está activo.
     */
    @GetMapping("/ficha/{id}")
    public String verFichaCurso(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes,
            Authentication authentication) {
        Curso curso = cursoService.buscarPorId(id).orElse(null);
        if (curso == null) {
            redirectAttributes.addFlashAttribute("error", "Error! El curso no se encuentra disponible.");
            return "redirect:/catalogo";
        }
        model.addAttribute("descuentoAplicable", pagoService.calcularDescuentoPara(alumnoDe(authentication)).orElse(null));
        model.addAttribute("descuentosVigentes", descuentoService.buscarVigentes());
        model.addAttribute("cursoSeleccionado", curso);
        List<com.app.idoneos.model.Cohorte> abiertas = cursoService.buscarCohortesAbiertas(curso);
        List<com.app.idoneos.model.Cohorte> proximas = cursoService.buscarCohortesProximas(curso);
        model.addAttribute("cohortesAbiertas", abiertas);
        // El select de la ficha ofrece las abiertas primero y las próximas después, cada una con su estado
        List<com.app.idoneos.model.Cohorte> elegibles = new java.util.ArrayList<>(abiertas);
        elegibles.addAll(proximas);
        model.addAttribute("cohortesElegibles", elegibles);
        model.addAttribute("cohortesRecordadas", recordatorioService.idsDeCohortesRecordadas(alumnoDe(authentication)));
        model.addAttribute("diasRecordatorio", Math.max(0, parametroService.obtenerEntero(
                com.app.idoneos.model.Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION,
                com.app.idoneos.model.Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION_POR_DEFECTO)));
        List<Programa> programas = programaService.buscarPorCurso(curso);
        if (!programas.isEmpty()) {
            Programa programa = programas.get(0);
            model.addAttribute("programaSeleccionado", programa);
            model.addAttribute("cronogramas", programa.getTemasCronograma().stream()
                    .filter(cronograma -> !cronograma.getBaja())
                    .sorted(Comparator.comparingInt(cronograma -> cronograma.getNumeroOrden())).toList());
        }
        model.addAttribute("titulo", curso.getNombre() + " | Idóneos Online");
        return "pages/curso/ficha";
    }

    /** El alumno que está mirando, o {@code null} si es un visitante o no es alumno. */
    private Alumno alumnoDe(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof Usuario usuario ? usuario.getAlumno() : null;
    }
}
