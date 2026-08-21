package com.app.idoneos.controller.modulo_usuarios;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_usuarios.*;

import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.Evaluacion.EvaluacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Controller para el panel del docente.
 *
 * MOD-F-01: Módulo de Cursos
 * CU-01 — Buscar curso (vista docente) → GET /docente
 * CU-03 — Registrar curso → GET/POST /docente/curso/nuevo +
 * /docente/curso/guardar
 *
 * MOD-F-02: Módulo de Gestión Académica
 * CU-15 — Buscar programa → GET /docente/curso/{cursoId}/programas
 * CU-16 — Registrar programa → POST /docente/curso/{cursoId}/programas/guardar
 * CU-17 — Modificar programa → POST /docente/programas/{id}/modificar
 * CU-18 — Dar de baja programa → POST /docente/programas/{id}/baja
 * CU-19 — Buscar unidad → GET /docente/programas/{programaId}/unidades y GET
 * /docente/curso/{cursoId}/gestionar
 * CU-20 — Agregar unidad → POST
 * /docente/programas/{programaId}/unidades/agregar
 * CU-21 — Modificar unidad → POST /docente/unidades/{id}/modificar
 * CU-22 — Quitar unidad → POST
 * /docente/programas/{programaId}/unidades/{unidadId}/quitar
 * CU-23 — Buscar cronograma → GET /docente/programas/{programaId}/cronograma
 * CU-24 — Modificar cronograma → POST
 * /docente/programas/{programaId}/cronograma/modificar
 * CU-25 — Ver participantes → GET /docente/curso/{cursoId}/participantes
 * CU-27 — Buscar material → GET /docente/unidad/{unidadId}/materiales
 * CU-28 — Subir material → POST /docente/unidad/{unidadId}/material/guardar
 * CU-29 — Modificar material → POST /docente/material/{id}/modificar
 * CU-30 — Dar de baja material → POST /docente/material/{id}/baja
 * CU-31 — Buscar término de glosario → GET /docente/unidad/{unidadId}/glosario
 * CU-32 — Registrar término de glosario → POST
 * /docente/unidad/{unidadId}/glosario/guardar
 * CU-33 — Modificar término de glosario → POST /docente/glosario/{id}/modificar
 * CU-34 — Dar de baja término de glosario → POST /docente/glosario/{id}/baja
 *
 * MOD-F-03: Módulo de Inscripciones
 * CU-48 — Buscar progreso (docente/admin) → GET
 * /docente/curso/{cursoId}/participantes
 */
@Controller
@RequestMapping("/docente")
public class DocenteController {

    @Autowired
    private CursoServiceImpl cursoService;
    @Autowired
    private CategoriaServiceImpl categoriaService;
    @Autowired
    private UnidadServiceImpl unidadService;
    @Autowired
    private MaterialServiceImpl materialService;
    @Autowired
    private TipoMaterialRepository tipoMaterialRepository;
    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private SupervisorRepository supervisorRepository;
    @Autowired
    private ProgramaRepository programaRepository;
    @Autowired
    private NivelRepository nivelRepository;
    @Autowired
    private TerminoGlosarioRepository terminoGlosarioRepository;
    @Autowired
    private CohorteRepository cohorteRepository;
    @Autowired
    private CronogramaRepository cronogramaRepository;
    @Autowired
    private InscripcionRepository inscripcionRepository;
    @Autowired
    private AutoevaluacionRepository autoevaluacionRepository;
    @Autowired
    private PoolRepository poolRepository;
    @Autowired
    private EvaluacionService evaluacionService;

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
        if (docente == null || curso == null)
            return false;
        // Titular
        if (curso.getDocente() != null && curso.getDocente().getId() == docente.getId()) {
            return true;
        }
        // Supervisor
        return supervisorRepository.findByDocente(docente).stream()
                .anyMatch(s -> s.getCurso() != null && s.getCurso().getId() == curso.getId());
    }

    /**
     * Verifica si un Docente pertenece a alguno de los Cursos vinculados a la
     * Unidad.
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
     * Precondición: sesión iniciada con rol Docente. Existe al menos un curso
     * asociado al docente.
     * Flujo paso 4: el sistema restringe el resultado a los cursos en los que
     * participa como titular o supervisor.
     */
    @GetMapping
    public String panelDocente(Model model, Authentication auth) {
        Usuario docente = (Usuario) auth.getPrincipal();
        List<Curso> misCursos = cursoService.obtenerPorDocente(docente);

        model.addAttribute("usuario", docente);
        model.addAttribute("cursos", misCursos);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("titulo", "Panel del Docente | Idóneos Online");

        return "pages/docente/mis-cursos";
    }

    /**
     * TRAZABILIDAD: CU-03 — Registrar curso (formulario / modal).
     * Actor: Docente / Administrador.
     */
    @GetMapping("/curso/nuevo")
    public String nuevoCursoForm(Model model, Authentication auth) {
        return "redirect:/docente";
    }

    /**
     * TRAZABILIDAD: CU-03 — Registrar curso (POST).
     * Actor: Docente.
     * Flujo paso 4-6: valida campos, registra el curso asignado al docente como
     * titular.
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

        if (nombre != null && nombre.trim().length() > 50) {
            nombre = nombre.trim().substring(0, 50);
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get(), nivel, docente);
        Curso cursoDef = cursoService.guardar(curso);

        // Registro de programa inicial respetando constraints
        String descProg = (descripcion != null && descripcion.length() > 150) ? descripcion.substring(0, 150) : descripcion;
        programaRepository.save(
                new Programa(nombre, descProg, "Objetivos generales del curso", "Bibliografía general", cursoDef));

        redirectAttributes.addFlashAttribute("mensaje", "¡Curso creado correctamente!");
        return "redirect:/docente";
    }

    /**
     * TRAZABILIDAD: CU-19 — Buscar unidad.
     * TRAZABILIDAD: CU-23 — Buscar cronograma (parcialmente, como parte de la
     * gestión del curso).
     * TRAZABILIDAD: CU-27 — Buscar material (panel lateral del gestor de unidades).
     * Actor: Docente (o Administrador con permisos).
     * Precondición: sesión iniciada con rol Docente. El curso existe. El docente
     * participa.
     * Flujo paso 2-3: lista las unidades del cronograma del programa vigente del
     * curso.
     * Flujo paso 4: al seleccionar una unidad, despliega su contenido (material,
     * glosario, pools, etc.).
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

        // CU-19 precondición: el docente participa en el curso como titular o
        // supervisor.
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
     * TRAZABILIDAD: CU-33 — Modificar término de glosario (formulario). PARCIAL —
     * solo muestra la lista.
     * TRAZABILIDAD: CU-34 — Dar de baja término de glosario. PARCIAL — no
     * implementado aquí.
     * Actor: Docente (o Administrador).
     * Precondición: sesión con rol Docente. La unidad existe. El docente participa
     * en el curso.
     * Flujo paso 4 (CU-31): recupera y lista los términos del glosario de la
     * unidad.
     * NOTA PARCIAL: CU-33 (Modificar) y CU-34 (Dar de baja) no están implementados
     * en esta vista.
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
     * Precondición: sesión con rol Docente. La unidad existe y no está en baja. El
     * docente participa.
     * Flujo paso 3-5: valida término y definición, registra el término asociado a
     * la unidad.
     * Postcondición: término registrado y asociado a la unidad.
     * NOTA PARCIAL: CU-32 paso 4 valida que el término no esté ya registrado en la
     * unidad. No implementado.
     */
    @PostMapping("/unidad/{unidadId}/glosario/guardar")
    public String guardarTerminoGlosario(@PathVariable Integer unidadId,
            @RequestParam("termino") String termino,
            @RequestParam("definicion") String definicion,
            Authentication auth, RedirectAttributes redirectAttributes) {

        Optional<Unidad> unidadOpt = unidadService.buscarPorId(unidadId);
        if (unidadOpt.isEmpty())
            return "redirect:/docente";

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

    /**
     * TRAZABILIDAD: CU-33 — Modificar término de glosario.
     * Actor: Docente.
     */
    @PostMapping("/glosario/{id}/modificar")
    public String modificarTerminoGlosario(@PathVariable Integer id,
            @RequestParam String termino,
            @RequestParam String definicion,
            Authentication auth, RedirectAttributes ra) {
        Optional<TerminoGlosario> tgOpt = terminoGlosarioRepository.findById(id);
        if (tgOpt.isEmpty())
            return "redirect:/docente";

        TerminoGlosario tg = tgOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, tg.getUnidad())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        tg.setTermino(termino.trim());
        tg.setDefinicion(definicion.trim());
        terminoGlosarioRepository.save(tg);

        ra.addFlashAttribute("mensaje", "Término modificado correctamente.");
        return "redirect:/docente/unidad/" + tg.getUnidad().getId() + "/glosario";
    }

    /**
     * TRAZABILIDAD: CU-34 — Dar de baja término de glosario.
     * Actor: Docente / Administrador.
     */
    @PostMapping("/glosario/{id}/baja")
    public String darBajaTerminoGlosario(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        Optional<TerminoGlosario> tgOpt = terminoGlosarioRepository.findById(id);
        if (tgOpt.isEmpty())
            return "redirect:/docente";

        TerminoGlosario tg = tgOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, tg.getUnidad())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        Integer unidadId = tg.getUnidad().getId();
        tg.setBaja(true);
        terminoGlosarioRepository.save(tg);

        ra.addFlashAttribute("mensaje", "Término dado de baja correctamente.");
        return "redirect:/docente/unidad/" + unidadId + "/glosario";
    }

    // ─────────────────────────────────────────────────────────────
    // PROGRAMAS (CU-15 a CU-18)
    // ─────────────────────────────────────────────────────────────

    /**
     * TRAZABILIDAD: CU-15 — Buscar programa.
     * Actor: Docente / Administrador.
     */
    @GetMapping("/curso/{cursoId}/programas")
    public String listarProgramas(@PathVariable Integer cursoId, Model model, Authentication auth,
            RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
        if (cOpt.isEmpty())
            return "redirect:/docente";

        Curso curso = cOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, curso)) {
            ra.addFlashAttribute("mensaje", "No tenés permisos para gestionar este curso.");
            return "redirect:/docente";
        }

        List<Programa> programas = programaRepository.findByCursoAndBajaFalse(curso);

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("programas", programas);
        model.addAttribute("titulo", "Programas de " + curso.getNombre() + " | Idóneos Online");
        return "pages/docente/programas";
    }

    /**
     * TRAZABILIDAD: CU-16 — Registrar programa.
     * Actor: Docente.
     */
    @PostMapping("/curso/{cursoId}/programas/guardar")
    public String registrarPrograma(@PathVariable Integer cursoId,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String objetivos,
            @RequestParam String bibliografia,
            @RequestParam(required = false) Integer cargaHorariaTotal,
            @RequestParam(required = false) Integer programaAnteriorId,
            Authentication auth, RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
        if (cOpt.isEmpty())
            return "redirect:/docente";

        Curso curso = cOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, curso)) {
            ra.addFlashAttribute("mensaje", "No tenés permisos para este curso.");
            return "redirect:/docente";
        }

        try {
            Programa nuevo = new Programa(nombre, descripcion, objetivos, bibliografia, curso);
            nuevo.setCargaHorariaTotal(cargaHorariaTotal != null ? cargaHorariaTotal : 40);
            Programa guardado = programaRepository.save(nuevo);

            // Si se seleccionó partir de un programa anterior, clonar su cronograma
            if (programaAnteriorId != null) {
                programaRepository.findById(programaAnteriorId).ifPresent(ant -> {
                    if (ant.getCronogramas() != null) {
                        for (Cronograma cr : ant.getCronogramas()) {
                            Cronograma clon = new Cronograma(cr.getNumeroOrden(), cr.getSemanasDuracion(), guardado,
                                    cr.getUnidad());
                            cronogramaRepository.save(clon);
                        }
                    }
                });
            }

            ra.addFlashAttribute("mensaje", "Programa '" + nombre + "' registrado exitosamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al registrar el programa: " + e.getMessage());
        }

        return "redirect:/docente/curso/" + cursoId + "/programas";
    }

    /**
     * TRAZABILIDAD: CU-17 — Modificar programa.
     * Actor: Docente.
     */
    @PostMapping("/programas/{id}/modificar")
    public String modificarPrograma(@PathVariable Integer id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String objetivos,
            @RequestParam String bibliografia,
            @RequestParam(required = false) Integer cargaHorariaTotal,
            Authentication auth, RedirectAttributes ra) {
        Optional<Programa> pOpt = programaRepository.findById(id);
        if (pOpt.isEmpty())
            return "redirect:/docente";

        Programa p = pOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, p.getCurso())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        try {
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setObjetivos(objetivos);
            p.setBibliografia(bibliografia);
            if (cargaHorariaTotal != null)
                p.setCargaHorariaTotal(cargaHorariaTotal);
            p.setUltimaModificacion(LocalDateTime.now());
            programaRepository.save(p);
            ra.addFlashAttribute("mensaje", "Programa modificado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al modificar: " + e.getMessage());
        }

        return "redirect:/docente/curso/" + p.getCurso().getId() + "/programas";
    }

    /**
     * TRAZABILIDAD: CU-18 — Dar de baja programa.
     * Actor: Docente / Administrador.
     */
    @PostMapping("/programas/{id}/baja")
    public String darBajaPrograma(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        Optional<Programa> pOpt = programaRepository.findById(id);
        if (pOpt.isEmpty())
            return "redirect:/docente";

        Programa p = pOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, p.getCurso())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        // CU-18: Validar que no tenga cohortes asociadas
        List<Cohorte> cohortes = cohorteRepository.findByPrograma(p);
        if (!cohortes.isEmpty()) {
            ra.addFlashAttribute("mensaje", "EX-CU18-01: No se puede dar de baja un programa con cohortes asociadas.");
            return "redirect:/docente/curso/" + p.getCurso().getId() + "/programas";
        }

        p.setBaja(true);
        p.setUltimaModificacion(LocalDateTime.now());
        programaRepository.save(p);
        ra.addFlashAttribute("mensaje", "Programa dado de baja correctamente.");
        return "redirect:/docente/curso/" + p.getCurso().getId() + "/programas";
    }

    // ─────────────────────────────────────────────────────────────
    // UNIDADES Y CRONOGRAMA (CU-19 a CU-22)
    // ─────────────────────────────────────────────────────────────

    /**
     * TRAZABILIDAD: CU-19 — Buscar unidad (gestionar contenido de unidades del
     * programa).
     * Actor: Docente / Administrador.
     */
    @GetMapping("/programas/{programaId}/unidades")
    public String listarUnidadesPrograma(@PathVariable Integer programaId, Model model, Authentication auth,
            RedirectAttributes ra) {
        Optional<Programa> pOpt = programaRepository.findById(programaId);
        if (pOpt.isEmpty())
            return "redirect:/docente";

        Programa p = pOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, p.getCurso())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        List<Cronograma> cronogramas = cronogramaRepository.findByProgramaOrderByNumeroOrden(p);

        // Unidades reutilizables de otros programas del mismo curso
        List<Unidad> todasUnidades = unidadService.obtenerTodo();
        List<Integer> idsEnEstePrograma = cronogramas.stream().map(c -> c.getUnidad().getId()).toList();
        List<Unidad> unidadesReutilizables = todasUnidades.stream()
                .filter(u -> !u.getBaja() && !idsEnEstePrograma.contains(u.getId()))
                .toList();

        model.addAttribute("usuario", usuario);
        model.addAttribute("programa", p);
        model.addAttribute("cronogramas", cronogramas);
        model.addAttribute("unidadesReutilizables", unidadesReutilizables);
        model.addAttribute("titulo", "Unidades del Programa | " + p.getNombre());
        return "pages/docente/unidades";
    }

    /**
     * TRAZABILIDAD: CU-20 — Agregar unidad (Caso A: Nueva unidad / Caso B:
     * Reutilizar existente).
     * Actor: Docente.
     */
    @PostMapping("/programas/{programaId}/unidades/agregar")
    public String agregarUnidad(@PathVariable Integer programaId,
            @RequestParam(required = false) Integer unidadExistenteId,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String contenido,
            @RequestParam(defaultValue = "1") int semanasDuracion,
            Authentication auth, RedirectAttributes ra) {
        Optional<Programa> pOpt = programaRepository.findById(programaId);
        if (pOpt.isEmpty())
            return "redirect:/docente";

        Programa programa = pOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, programa.getCurso())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        List<Cronograma> cronogramas = cronogramaRepository.findByProgramaOrderByNumeroOrden(programa);
        int siguienteOrden = cronogramas.isEmpty() ? 1 : cronogramas.get(cronogramas.size() - 1).getNumeroOrden() + 1;

        Unidad unidad;
        if (unidadExistenteId != null) {
            // Caso B: Reutilizar unidad existente
            Optional<Unidad> uOpt = unidadService.buscarPorId(unidadExistenteId);
            if (uOpt.isEmpty()) {
                ra.addFlashAttribute("mensaje", "Unidad seleccionada no encontrada.");
                return "redirect:/docente/programas/" + programaId + "/unidades";
            }
            unidad = uOpt.get();
        } else {
            // Caso A: Crear unidad nueva
            if (titulo == null || titulo.isBlank() || contenido == null || contenido.isBlank()) {
                ra.addFlashAttribute("mensaje", "EX-CU20-01: El título y contenido son obligatorios.");
                return "redirect:/docente/programas/" + programaId + "/unidades";
            }
            unidad = new Unidad(titulo.trim(), descripcion, contenido.trim());
            unidad = unidadService.guardar(unidad);
        }

        Cronograma nuevoCronograma = new Cronograma(siguienteOrden, semanasDuracion > 0 ? semanasDuracion : 1, programa,
                unidad);
        cronogramaRepository.save(nuevoCronograma);

        ra.addFlashAttribute("mensaje", "Unidad agregada al programa exitosamente.");
        return "redirect:/docente/programas/" + programaId + "/unidades";
    }

    /**
     * TRAZABILIDAD: CU-21 — Modificar unidad.
     * Actor: Docente.
     */
    @PostMapping("/unidades/{id}/modificar")
    public String modificarUnidad(@PathVariable Integer id,
            @RequestParam String titulo,
            @RequestParam(required = false) String descripcion,
            @RequestParam String contenido,
            @RequestParam(required = false) Integer programaId,
            Authentication auth, RedirectAttributes ra) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(id);
        if (uOpt.isEmpty())
            return "redirect:/docente";

        Unidad u = uOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, u)) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        if (titulo == null || titulo.isBlank() || contenido == null || contenido.isBlank()) {
            ra.addFlashAttribute("mensaje", "EX-CU21-01: El título y contenido no pueden quedar vacíos.");
            return "redirect:/docente";
        }

        u.setTitulo(titulo.trim());
        u.setDescripcion(descripcion);
        u.setContenido(contenido.trim());
        u.setUltimaModificacion(LocalDateTime.now());
        unidadService.guardar(u);

        ra.addFlashAttribute("mensaje", "Unidad modificada correctamente.");
        if (programaId != null) {
            return "redirect:/docente/programas/" + programaId + "/unidades";
        }
        return "redirect:/docente";
    }

    /**
     * TRAZABILIDAD: CU-22 — Quitar unidad del programa.
     * Actor: Docente / Administrador.
     */
    @PostMapping("/programas/{programaId}/unidades/{unidadId}/quitar")
    public String quitarUnidad(@PathVariable Integer programaId,
            @PathVariable Integer unidadId,
            Authentication auth, RedirectAttributes ra) {
        Optional<Programa> pOpt = programaRepository.findById(programaId);
        Optional<Unidad> uOpt = unidadService.buscarPorId(unidadId);
        if (pOpt.isEmpty() || uOpt.isEmpty())
            return "redirect:/docente";

        Programa p = pOpt.get();
        Unidad u = uOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, p.getCurso())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        // Buscar entrada en Cronograma
        List<Cronograma> cronogramas = cronogramaRepository.findByProgramaOrderByNumeroOrden(p);
        Optional<Cronograma> entrada = cronogramas.stream().filter(c -> c.getUnidad().getId() == unidadId).findFirst();

        if (entrada.isPresent()) {
            cronogramaRepository.delete(entrada.get());

            // Si ya no pertenece a ningún otro programa, dar de baja lógica a la unidad
            List<Cronograma> restantes = cronogramaRepository.findByUnidad(u);
            if (restantes.isEmpty()) {
                u.setBaja(true);
                u.setUltimaModificacion(LocalDateTime.now());
                unidadService.guardar(u);
            }

            ra.addFlashAttribute("mensaje", "Unidad quitada del programa correctamente.");
        }

        return "redirect:/docente/programas/" + programaId + "/unidades";
    }

    // ─────────────────────────────────────────────────────────────
    // CRONOGRAMA Y PARTICIPANTES (CU-23, CU-24, CU-25)
    // ─────────────────────────────────────────────────────────────

    /**
     * TRAZABILIDAD: CU-23 — Buscar cronograma.
     * Actor: Administrador / Docente.
     */
    @GetMapping("/programas/{programaId}/cronograma")
    public String verCronograma(@PathVariable Integer programaId, Model model, Authentication auth,
            RedirectAttributes ra) {
        Optional<Programa> pOpt = programaRepository.findById(programaId);
        if (pOpt.isEmpty())
            return "redirect:/docente";

        Programa programa = pOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, programa.getCurso())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        List<Cronograma> cronogramas = cronogramaRepository.findByProgramaOrderByNumeroOrden(programa);

        model.addAttribute("usuario", usuario);
        model.addAttribute("programa", programa);
        model.addAttribute("cronogramas", cronogramas);
        model.addAttribute("titulo", "Cronograma | " + programa.getNombre());
        return "pages/docente/cronograma";
    }

    /**
     * TRAZABILIDAD: CU-24 — Modificar cronograma (orden y duración en semanas de
     * cada unidad).
     * Actor: Docente.
     */
    @PostMapping("/programas/{programaId}/cronograma/modificar")
    public String modificarCronograma(@PathVariable Integer programaId,
            @RequestParam List<Integer> cronogramaId,
            @RequestParam List<Integer> numeroOrden,
            @RequestParam List<Integer> semanasDuracion,
            Authentication auth, RedirectAttributes ra) {
        Optional<Programa> pOpt = programaRepository.findById(programaId);
        if (pOpt.isEmpty())
            return "redirect:/docente";

        Programa programa = pOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, programa.getCurso())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        try {
            for (int i = 0; i < cronogramaId.size(); i++) {
                Integer cId = cronogramaId.get(i);
                int orden = numeroOrden.get(i);
                int duracion = semanasDuracion.get(i);

                Optional<Cronograma> cOpt = cronogramaRepository.findById(cId);
                if (cOpt.isPresent()) {
                    Cronograma cr = cOpt.get();
                    cr.setNumeroOrden(orden);
                    cr.setSemanasDuracion(duracion > 0 ? duracion : 1);
                    cronogramaRepository.save(cr);
                }
            }
            ra.addFlashAttribute("mensaje", "Cronograma actualizado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al actualizar cronograma: " + e.getMessage());
        }

        return "redirect:/docente/programas/" + programaId + "/cronograma";
    }

    /**
     * TRAZABILIDAD: CU-25 — Ver participantes de un curso.
     * Actor: Docente / Administrador.
     */
    @GetMapping("/curso/{cursoId}/participantes")
    public String verParticipantes(@PathVariable Integer cursoId,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String rol,
            Model model, Authentication auth, RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
        if (cOpt.isEmpty())
            return "redirect:/docente";

        Curso curso = cOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceACurso(docente, curso)) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        // Equipo docente
        List<Docente> docentesEquipo = new java.util.ArrayList<>();
        if (curso.getDocente() != null)
            docentesEquipo.add(curso.getDocente());
        supervisorRepository.findByCurso(curso).forEach(s -> {
            if (s.getDocente() != null && !docentesEquipo.contains(s.getDocente())) {
                docentesEquipo.add(s.getDocente());
            }
        });

        // Alumnos inscriptos en todas las cohortes del curso
        List<Inscripcion> inscripciones = inscripcionRepository.findByCursoAndBajaFalse(curso);

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("docentesEquipo", docentesEquipo);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("titulo", "Participantes | " + curso.getNombre());
        return "pages/docente/participantes";
    }

    // ─────────────────────────────────────────────────────────────
    // MATERIALES DE ESTUDIO (CU-27 a CU-30)
    // ─────────────────────────────────────────────────────────────

    /**
     * TRAZABILIDAD: CU-28 — Subir material (grabación, bibliografía, presentación,
     * etc.).
     * Actor: Docente.
     */
    @PostMapping("/unidad/{unidadId}/material/guardar")
    public String subirMaterial(@PathVariable Integer unidadId,
            @RequestParam String titulo,
            @RequestParam Integer tipoMaterialId,
            @RequestParam(required = false) String contenido,
            @RequestParam(required = false) String rutaArchivo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) Integer duracion,
            @RequestParam(defaultValue = "true") boolean publicado,
            Authentication auth, RedirectAttributes ra) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(unidadId);
        if (uOpt.isEmpty())
            return "redirect:/docente";

        Unidad u = uOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, u)) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        Optional<TipoMaterial> tmOpt = tipoMaterialRepository.findById(tipoMaterialId);
        if (tmOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Tipo de material inválido.");
            return "redirect:/docente";
        }

        try {
            Material m = new Material(titulo, docente, tmOpt.get(), u);
            m.setRutaArchivo(rutaArchivo);
            m.setContenido(contenido);
            m.setAutor(autor);
            m.setDuracion(duracion);
            m.setPublicado(publicado);
            materialService.guardar(m);
            ra.addFlashAttribute("mensaje", "Material '" + titulo + "' subido correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al subir material: " + e.getMessage());
        }

        return "redirect:/docente/unidad/" + unidadId + "/materiales";
    }

    /**
     * TRAZABILIDAD: CU-27 — Buscar materiales de una unidad.
     * Actor: Docente / Administrador.
     */
    @GetMapping("/unidad/{unidadId}/materiales")
    public String listarMaterialesUnidad(@PathVariable Integer unidadId, Model model, Authentication auth,
            RedirectAttributes ra) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(unidadId);
        if (uOpt.isEmpty())
            return "redirect:/docente";

        Unidad u = uOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, u)) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        List<Material> materiales = materialService.obtenerTodosPorUnidad(u);

        model.addAttribute("usuario", usuario);
        model.addAttribute("unidad", u);
        model.addAttribute("materiales", materiales);
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "Materiales | " + u.getTitulo());
        return "pages/docente/materiales";
    }

    /**
     * TRAZABILIDAD: CU-29 — Modificar material.
     * Actor: Docente.
     */
    @PostMapping("/material/{id}/modificar")
    public String modificarMaterial(@PathVariable Integer id,
            @RequestParam String titulo,
            @RequestParam(required = false) String contenido,
            @RequestParam(required = false) String rutaArchivo,
            @RequestParam(required = false) Integer tipoMaterialId,
            @RequestParam(defaultValue = "true") boolean publicado,
            Authentication auth, RedirectAttributes ra) {
        Optional<Material> mOpt = materialService.buscarPorId(id);
        if (mOpt.isEmpty())
            return "redirect:/docente";

        Material m = mOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, m.getUnidad())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        try {
            m.setTitulo(titulo);
            m.setContenido(contenido);
            m.setRutaArchivo(rutaArchivo);
            m.setPublicado(publicado);
            if (tipoMaterialId != null) {
                tipoMaterialRepository.findById(tipoMaterialId).ifPresent(m::setTipoMaterial);
            }
            materialService.modificar(m);
            ra.addFlashAttribute("mensaje", "Material modificado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al modificar material: " + e.getMessage());
        }

        return "redirect:/docente/unidad/" + m.getUnidad().getId() + "/materiales";
    }

    /**
     * TRAZABILIDAD: CU-30 — Dar de baja material.
     * Actor: Docente / Administrador.
     */
    @PostMapping("/material/{id}/baja")
    public String darBajaMaterial(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        Optional<Material> mOpt = materialService.buscarPorId(id);
        if (mOpt.isEmpty())
            return "redirect:/docente";

        Material m = mOpt.get();
        Docente docente = getDocente(auth);
        Usuario usuario = (Usuario) auth.getPrincipal();

        if (!usuario.esAdmin() && !docentePerteneceAUnidad(docente, m.getUnidad())) {
            ra.addFlashAttribute("mensaje", "No tenés permisos.");
            return "redirect:/docente";
        }

        Integer unidadId = m.getUnidad().getId();
        materialService.darDeBajaMaterial(id);
        ra.addFlashAttribute("mensaje", "Material dado de baja correctamente.");
        return "redirect:/docente/unidad/" + unidadId + "/materiales";
    }
}
