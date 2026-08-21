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
 *             El sistema restringe los resultados a los cursos en los que el docente participa.
 *   CU-03 — Registrar curso (formulario + POST) → GET/POST /docente/curso/nuevo + /docente/curso/guardar
 *             NOTA CRÍTICA: usa Dictado/DictadoDocente del esquema ANTERIOR. Requiere refactor.
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
 *
 * INCONSISTENCIAS CON EL ESQUEMA ACTUAL:
 *   - DocenteController inyecta DictadoDocenteRepository y DictadoRepository del esquema anterior.
 *   - La verificación de pertenencia del docente al curso (docentePerteneceACurso) usa
 *     DictadoDocente/Dictado; en el nuevo esquema debe usar Supervisor/Cohorte/Cronograma.
 *   - CU-03 (registrar curso) crea Programa + Dictado + DictadoDocente en lugar de
 *     Programa + Cronograma + Cohorte + Supervisor del nuevo esquema.
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
    // NOTA: DictadoDocenteRepository y DictadoRepository son del esquema anterior.
    // En el nuevo esquema usar SupervisorRepository y CohorteRepository.
    @Autowired private DictadoDocenteRepository dictadoDocenteRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private DictadoRepository dictadoRepository;
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
     * NOTA: usa DictadoDocente del esquema anterior. En el nuevo esquema verificar con Supervisor/Cohorte.
     */
    private boolean docentePerteneceACurso(Docente docente, Curso curso) {
        if (docente == null || curso == null) return false;
        return dictadoDocenteRepository.findByDocente(docente).stream()
                .anyMatch(dd -> dd.getDictado() != null
                        && dd.getDictado().getPrograma() != null
                        && dd.getDictado().getPrograma().getCurso() != null
                        && dd.getDictado().getPrograma().getCurso().getId() == curso.getId());
    }

    /**
     * TRAZABILIDAD: CU-01 — Buscar curso (vista docente).
     * Actor: Docente.
     * Precondición: sesión iniciada con rol Docente. Existe al menos un curso asociado al docente.
     * Flujo paso 4: el sistema restringe el resultado a los cursos en los que participa como titular o supervisor.
     * NOTA PARCIAL: CU-01 especifica criterios de búsqueda (nombre, categoría, nivel, etc.). No implementados.
     *   La implementación lista todos los cursos del docente sin filtros.
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
     * Actor: Docente (en esta implementación el docente puede crear cursos; según la spec, es rol Administrador).
     * NOTA DE ACTOR: CU-03 especifica que el actor es el Administrador. Esta ruta para Docente
     *   podría corresponder a CU-16 (Registrar programa) o ser un requisito adicional no documentado.
     *   INCONSISTENCIA DE ACTOR — pendiente de aclaración.
     */
    @GetMapping("/curso/nuevo")
    public String nuevoCursoForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "Nuevo Curso | Idóneos Online");
        return "pages/docente/nuevo-curso";
    }

    /**
     * TRAZABILIDAD: CU-03 — Registrar curso (POST) / posiblemente CU-16 — Registrar programa.
     * Actor: Docente.
     * Flujo paso 4-6: valida campos, registra el curso con Programa y Dictado asignado al docente.
     * Postcondición: curso registrado con el docente como titular.
     * EX-CU03-01: categoría inválida → redirect con mensaje.
     * NOTA CRÍTICA: usa Dictado/DictadoDocente del esquema ANTERIOR.
     *   En el nuevo esquema: Programa + Cronograma + Cohorte (por Admin) + Supervisor.
     *   Estado: IMPLEMENTADO CON ENTIDADES OBSOLETAS — requiere refactor.
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
            redirectAttributes.addFlashAttribute("mensaje", "EX-CU03-01: Categoría seleccionada inválida.");
            return "redirect:/docente/curso/nuevo";
        }

        Docente docente = docenteRepository.findById(usuarioAuth.getId()).orElse(null);
        if (docente == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: Perfil docente no encontrado.");
            return "redirect:/docente";
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get());
        Curso cursoDef = cursoService.guardar(curso);

        // NOTA: usa Dictado/DictadoDocente del esquema anterior.
        // Nuevo esquema: Programa + Cronograma + Cohorte + Supervisor.
        Programa prog = programaRepository.save(new Programa(nombre, descripcion, 12, cursoDef));
        Dictado dictado = dictadoRepository.save(new Dictado(java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMonths(6), 50, prog));

        dictadoDocenteRepository.save(new DictadoDocente(dictado, docente, false));

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

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, unidad.getCurso())) {
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

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, unidad.getCurso())) {
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
