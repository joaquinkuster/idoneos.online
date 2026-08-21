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
 * TRAZABILIDAD — Controller para el panel del docente.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-01 — Buscar curso (vista docente)  → GET /docente
 *             El sistema restringe los resultados a los cursos en los que el docente participa como titular o supervisor.
 *   CU-03 — Registrar curso (formulario + POST) → GET/POST /docente/curso/nuevo + /docente/curso/guardar
 *
 * MOD-F-02: Módulo de Gestión Académica
 *   CU-19 — Buscar unidad                → GET /docente/curso/{cursoId}/gestionar
 *             Lista las unidades del cronograma del programa vigente del curso.
 *   CU-20 — Agregar unidad               → no implementado como POST separado. FALTANTE.
 *   CU-21 — Modificar unidad             → no implementado. FALTANTE.
 *   CU-22 — Quitar unidad                → no implementado. FALTANTE.
 *   CU-23 — Buscar cronograma            → GET /docente/curso/{cursoId}/gestionar (junto con CU-19). PARCIAL.
 *   CU-24 — Modificar cronograma         → no implementado. FALTANTE.
 *   CU-25 — Ver participantes            → no implementado. FALTANTE.
 *   CU-27 — Buscar material              → GET /docente/curso/{cursoId}/gestionar (panel). PARCIAL.
 *   CU-28 — Subir material               → ver MaterialServiceImpl.
 *   CU-29 — Modificar material           → ver MaterialServiceImpl.
 *   CU-30 — Dar de baja material         → ver MaterialServiceImpl.
 *   CU-31 — Buscar término de glosario   → GET /docente/unidad/{unidadId}/glosario
 *   CU-32 — Registrar término de glosario → POST /docente/unidad/{unidadId}/glosario/guardar
 *   CU-33 — Modificar término de glosario → no implementado. FALTANTE.
 *   CU-34 — Dar de baja término          → no implementado. FALTANTE.
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
    @Autowired private SupervisorRepository supervisorRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private NivelRepository nivelRepository;
    @Autowired private TerminoGlosarioRepository terminoGlosarioRepository;
    @Autowired private AutoevaluacionRepository autoevaluacionRepository;
    @Autowired private PoolRepository poolRepository;
    @Autowired private EvaluacionService evaluacionService;

    /**
     * Obtiene el Docente vinculado al usuario autenticado.
     */
    private Docente getDocente(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return docenteRepository.findById(usuario.getId()).orElse(null);
    }

    /**
     * Verifica si un Docente pertenece a un Curso como titular o supervisor.
     */
    private boolean docentePerteneceACurso(Docente docente, Curso curso) {
        if (docente == null || curso == null) return false;
        // Titular
        if (curso.getDocente() != null && curso.getDocente().getId() == docente.getId()) {
            return true;
        }
        // Supervisor
        return supervisorRepository.findByDocente(docente).stream()
                .anyMatch(s -> s.getCurso() != null && s.getCurso().getId() == curso.getId());
    }

    /**
     * Verifica si un Docente pertenece a alguno de los Cursos vinculados a la Unidad.
     */
    private boolean docentePerteneceAUnidad(Docente docente, Unidad unidad) {
        if (docente == null || unidad == null || unidad.getCronogramas() == null) {
            return false;
        }
        return unidad.getCronogramas().stream()
                .map(Cronograma::getPrograma)
                .filter(p -> p != null && p.getCurso() != null)
                .map(Programa::getCurso)
                .anyMatch(curso -> docentePerteneceACurso(docente, curso));
    }

    /**
     * TRAZABILIDAD: CU-01 — Buscar curso (vista docente).
     * Actor: Docente.
     * Precondición: sesión iniciada con rol Docente. Existe al menos un curso asociado al docente.
     * Flujo paso 4: el sistema restringe el resultado a los cursos en los que participa como titular o supervisor.
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
     * TRAZABILIDAD: CU-03 — Registrar curso (formulario GET).
     * Actor: Docente / Administrador.
     */
    @GetMapping("/curso/nuevo")
    public String nuevoCursoForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("titulo", "Nuevo Curso | Idóneos Online");
        return "pages/docente/nuevo-curso";
    }

    /**
     * TRAZABILIDAD: CU-03 — Registrar curso (POST).
     * Actor: Docente.
     * Flujo paso 4-6: valida campos, registra el curso asignado al docente como titular.
     * Postcondición: curso registrado con el docente como titular.
     * EX-CU03-01: categoría inválida → redirect con mensaje.
     */
    @PostMapping("/curso/guardar")
    public String guardarCurso(@RequestParam("nombre") String nombre,
                               @RequestParam("descripcion") String descripcion,
                               @RequestParam("precio") float precio,
                               @RequestParam("categoriaId") Integer categoriaId,
                               @RequestParam(value = "nivelId", required = false) Integer nivelId,
                               Authentication auth, RedirectAttributes redirectAttributes) {

        if (precio < 0) {
            redirectAttributes.addFlashAttribute("mensaje", "EX-CU03-02: El precio no puede ser menor a cero.");
            return "redirect:/docente/curso/nuevo";
        }

        Usuario usuarioAuth = (Usuario) auth.getPrincipal();
        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);

        if (catOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "EX-CU03-01: Categoría seleccionada inválida.");
            return "redirect:/docente/curso/nuevo";
        }

        Docente docente = docenteRepository.findById(usuarioAuth.getId()).orElse(null);
        if (docente == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: Perfil docente no encontrado.");
            return "redirect:/docente";
        }

        Nivel nivel = (nivelId != null) ? nivelRepository.findById(nivelId).orElse(null) : null;
        if (nivel == null) {
            List<Nivel> niveles = nivelRepository.findAll();
            nivel = niveles.isEmpty() ? null : niveles.get(0);
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get(), nivel, docente);
        Curso cursoDef = cursoService.guardar(curso);

        // Registro de programa inicial
        programaRepository.save(new Programa(nombre, descripcion, "Objetivos generales del curso", "Bibliografía general", cursoDef));

        redirectAttributes.addFlashAttribute("mensaje", "¡Curso creado correctamente!");
        return "redirect:/docente";
    }

    /**
     * TRAZABILIDAD: CU-19 — Buscar unidad.
     * TRAZABILIDAD: CU-23 — Buscar cronograma (parcialmente, como parte de la gestión del curso).
     * TRAZABILIDAD: CU-27 — Buscar material (panel lateral del gestor de unidades).
     * Actor: Docente (o Administrador con permisos).
     * Precondición: sesión iniciada con rol Docente. El curso existe. El docente participa.
     * Flujo paso 2-3: lista las unidades del cronograma del programa vigente del curso.
     * Flujo paso 4: al seleccionar una unidad, despliega su contenido (material, glosario, pools, etc.).
     * EX-CU19-01: si el docente no pertenece al curso → redirect con mensaje.
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

        // CU-19 precondición: el docente participa en el curso como titular o supervisor.
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
     * TRAZABILIDAD: CU-31 — Buscar término de glosario.
     * TRAZABILIDAD: CU-32 — Registrar término de glosario (formulario).
     * TRAZABILIDAD: CU-33 — Modificar término de glosario (formulario). PARCIAL — solo muestra la lista.
     * TRAZABILIDAD: CU-34 — Dar de baja término de glosario. PARCIAL — no implementado aquí.
     * Actor: Docente (o Administrador).
     * Precondición: sesión con rol Docente. La unidad existe. El docente participa en el curso.
     * Flujo paso 4 (CU-31): recupera y lista los términos del glosario de la unidad.
     * NOTA PARCIAL: CU-33 (Modificar) y CU-34 (Dar de baja) no están implementados en esta vista.
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

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, unidad)) {
            redirectAttributes.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        // CU-31 paso 4: recupera todos los términos activos del glosario de la unidad.
        List<TerminoGlosario> terminos = terminoGlosarioRepository.findByUnidadAndBajaFalse(unidad);

        model.addAttribute("usuario", usuario);
        model.addAttribute("unidad", unidad);
        model.addAttribute("terminos", terminos);
        model.addAttribute("titulo", "Glosario: " + unidad.getTitulo() + " | Idóneos Online");

        return "pages/docente/glosario";
    }

    /**
     * TRAZABILIDAD: CU-32 — Registrar término de glosario.
     * Actor: Docente.
     * Precondición: sesión con rol Docente. La unidad existe y no está en baja. El docente participa.
     * Flujo paso 3-5: valida término y definición, registra el término asociado a la unidad.
     * Postcondición: término registrado y asociado a la unidad.
     * NOTA PARCIAL: CU-32 paso 4 valida que el término no esté ya registrado en la unidad. No implementado.
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

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, unidad)) {
            redirectAttributes.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        // CU-32 paso 5: registra el término de glosario asociado a la unidad.
        TerminoGlosario nuevo = new TerminoGlosario(termino, definicion, unidad);
        terminoGlosarioRepository.save(nuevo);

        redirectAttributes.addFlashAttribute("mensaje", "Término agregado al glosario.");
        return "redirect:/docente/unidad/" + unidadId + "/glosario";
    }
}
