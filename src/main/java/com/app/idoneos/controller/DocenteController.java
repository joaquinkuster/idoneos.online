package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.Categoria.CategoriaServiceImpl;
import com.app.idoneos.service.Curso.CursoServiceImpl;
import com.app.idoneos.service.Evaluacion.EvaluacionService;
import com.app.idoneos.service.Material.MaterialServiceImpl;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller para la gestión del panel docente (CU-01 Buscar curso, CU-02 Registrar curso, CU-19 Buscar unidad a CU-32 Consultar glosario).
 * Restringe las operaciones a los cursos en los que el docente participa como titular o supervisor.
 */
@Controller
@RequestMapping("/docente")
public class DocenteController {

    @Autowired private CursoServiceImpl cursoService;
    @Autowired private CategoriaServiceImpl categoriaService;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private MaterialServiceImpl materialService;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private DictadoDocenteRepository dictadoDocenteRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private DictadoRepository dictadoRepository;
    @Autowired private TerminoGlosarioRepository terminoGlosarioRepository;
    @Autowired private AutoevaluacionRepository autoevaluacionRepository;
    @Autowired private PoolRepository poolRepository;
    @Autowired private EvaluacionService evaluacionService;

    private Docente getDocente(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return docenteRepository.findById(usuario.getId()).orElse(null);
    }

    private boolean docentePerteneceACurso(Docente docente, Curso curso) {
        if (docente == null || curso == null) return false;
        return dictadoDocenteRepository.findByDocente(docente).stream()
                .anyMatch(dd -> dd.getDictado() != null 
                        && dd.getDictado().getPrograma() != null 
                        && dd.getDictado().getPrograma().getCurso() != null 
                        && dd.getDictado().getPrograma().getCurso().getId() == curso.getId());
    }

    /**
     * CU-01 — Buscar curso.
     */
    @GetMapping
    public String panelDocente(Model model, Authentication auth) {
        Usuario docente = (Usuario) auth.getPrincipal();
        List<Curso> misCursos = cursoService.obtenerPorDocente(docente);

        model.addAttribute("usuario", docente);
        model.addAttribute("cursos", misCursos);
        model.addAttribute("titulo", "Panel del Docente | Idóneos Online");

        return "pages/docente/mis-cursos";
    }

    /**
     * CU-02 — Registrar curso.
     */
    @GetMapping("/curso/nuevo")
    public String nuevoCursoForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "Nuevo Curso | Idóneos Online");
        return "pages/docente/nuevo-curso";
    }

    /**
     * CU-02 — Registrar curso.
     * Reglas de negocio:
     * - RN-CU02-01: Precio mayor o igual a cero.
     * - Creación automática de programa y dictado asignado al docente.
     */
    @PostMapping("/curso/guardar")
    public String guardarCurso(@RequestParam("nombre") String nombre,
                               @RequestParam("descripcion") String descripcion,
                               @RequestParam("precio") float precio,
                               @RequestParam("categoriaId") Integer categoriaId,
                               Authentication auth, RedirectAttributes redirectAttributes) {

        Usuario usuarioAuth = (Usuario) auth.getPrincipal();
        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);

        if (catOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "EX-CU02-01: Categoría seleccionada inválida.");
            return "redirect:/docente/curso/nuevo";
        }

        Docente docente = docenteRepository.findById(usuarioAuth.getId()).orElse(null);
        if (docente == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: Perfil docente no encontrado.");
            return "redirect:/docente";
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get());
        Curso cursoDef = cursoService.guardar(curso);

        Programa prog = programaRepository.save(new Programa(nombre, descripcion, 12, cursoDef));
        Dictado dictado = dictadoRepository.save(new Dictado(java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMonths(6), 50, prog));

        dictadoDocenteRepository.save(new DictadoDocente(dictado, docente, false));

        redirectAttributes.addFlashAttribute("mensaje", "¡Curso creado correctamente!");
        return "redirect:/docente";
    }

    /**
     * CU-19 — Buscar unidad / CU-20 — Editar contenido de unidad.
     */
    @GetMapping("/curso/{cursoId}/gestionar")
    public String gestionarCurso(@PathVariable Integer cursoId, Model model, Authentication auth,
                                  RedirectAttributes redirectAttributes) {

        Optional<Curso> cursoOpt = cursoService.buscarPorId(cursoId);
        if (cursoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Curso no encontrado.");
            return "redirect:/docente";
        }

        Curso curso = cursoOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, curso)) {
            redirectAttributes.addFlashAttribute("mensaje", "No tenés permisos para gestionar este curso.");
            return "redirect:/docente";
        }

        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "Gestionar: " + curso.getNombre() + " | Idóneos Online");

        return "pages/docente/gestionar-curso";
    }

    /**
     * CU-32 — Consultar glosario.
     */
    @GetMapping("/unidad/{unidadId}/glosario")
    public String gestionarGlosario(@PathVariable Integer unidadId, Model model, Authentication auth,
                                     RedirectAttributes redirectAttributes) {

        Optional<Unidad> unidadOpt = unidadService.buscarPorId(unidadId);
        if (unidadOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Unidad no encontrada.");
            return "redirect:/docente";
        }

        Unidad unidad = unidadOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, unidad.getCurso())) {
            redirectAttributes.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        List<TerminoGlosario> terminos = terminoGlosarioRepository.findByUnidadAndBajaFalse(unidad);

        model.addAttribute("usuario", usuario);
        model.addAttribute("unidad", unidad);
        model.addAttribute("terminos", terminos);
        model.addAttribute("titulo", "Glosario: " + unidad.getTitulo() + " | Idóneos Online");

        return "pages/docente/glosario";
    }

    /**
     * CU-29 — Registrar término de glosario.
     */
    @PostMapping("/unidad/{unidadId}/glosario/guardar")
    public String guardarTerminoGlosario(@PathVariable Integer unidadId,
                                          @RequestParam("termino") String termino,
                                          @RequestParam("definicion") String definicion,
                                          Authentication auth, RedirectAttributes redirectAttributes) {

        Optional<Unidad> unidadOpt = unidadService.buscarPorId(unidadId);
        if (unidadOpt.isEmpty()) return "redirect:/docente";

        Unidad unidad = unidadOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, unidad.getCurso())) {
            redirectAttributes.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        TerminoGlosario nuevo = new TerminoGlosario(termino, definicion, unidad);
        terminoGlosarioRepository.save(nuevo);

        redirectAttributes.addFlashAttribute("mensaje", "Término agregado al glosario.");
        return "redirect:/docente/unidad/" + unidadId + "/glosario";
    }
}
