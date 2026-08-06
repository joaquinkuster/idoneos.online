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
 * Panel del Docente — gestión de cursos propios, unidades, materiales,
 * glosario y autoevaluaciones.
 *
 * Decisión de diseño: El docente solo puede gestionar cursos en los que participa
 * (como titular o supervisor), verificado en cada acción vía DocenteCursoRepository.
 * El administrador tiene acceso sin restricciones desde AdminController.
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
    @Autowired private DocenteCursoRepository docenteCursoRepository;
    @Autowired private TerminoGlosarioRepository terminoGlosarioRepository;
    @Autowired private AutoevaluacionRepository autoevaluacionRepository;
    @Autowired private PoolRepository poolRepository;
    @Autowired private EvaluacionService evaluacionService;

    // ─── Helper: verificar que el docente pertenece al curso ──────────────────

    private Docente getDocente(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return docenteRepository.findById(usuario.getId()).orElse(null);
    }

    private boolean docentePerteneceACurso(Docente docente, Curso curso) {
        if (docente == null || curso == null) return false;
        return docenteCursoRepository.findByDocenteAndCurso(docente, curso).isPresent();
    }

    // ─── Panel Principal Docente ───────────────────────────────────────────────

    @GetMapping
    public String panelDocente(Model model, Authentication auth) {
        Usuario docente = (Usuario) auth.getPrincipal();
        List<Curso> misCursos = cursoService.obtenerPorDocente(docente);

        model.addAttribute("usuario", docente);
        model.addAttribute("cursos", misCursos);
        model.addAttribute("titulo", "Panel del Docente | Idóneos Online");

        return "pages/docente/mis-cursos";
    }

    // ─── Crear Curso ───────────────────────────────────────────────────────────

    @GetMapping("/curso/nuevo")
    public String nuevoCursoForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "Nuevo Curso | Idóneos Online");
        return "pages/docente/nuevo-curso";
    }

    @PostMapping("/curso/guardar")
    public String guardarCurso(@RequestParam("nombre") String nombre,
                               @RequestParam("descripcion") String descripcion,
                               @RequestParam("precio") float precio,
                               @RequestParam("categoriaId") Integer categoriaId,
                               Authentication auth, RedirectAttributes redirectAttributes) {

        Usuario usuarioAuth = (Usuario) auth.getPrincipal();
        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);

        if (catOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Categoría no válida.");
            return "redirect:/docente/curso/nuevo";
        }

        Docente docente = docenteRepository.findById(usuarioAuth.getId()).orElse(null);
        if (docente == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: perfil de docente no encontrado.");
            return "redirect:/docente";
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get());
        Curso cursoDef = cursoService.guardar(curso);
        docenteCursoRepository.save(new DocenteCurso(docente, cursoDef, false));

        redirectAttributes.addFlashAttribute("mensaje", "¡Curso creado correctamente!");
        return "redirect:/docente";
    }

    // ─── Gestión de Unidades y Materiales de un Curso ─────────────────────────

    @GetMapping("/curso/{cursoId}/gestionar")
    public String gestionarCurso(@PathVariable Integer cursoId, Model model, Authentication auth,
                                  RedirectAttributes redirectAttributes) {

        Optional<Curso> cursoOpt = cursoService.buscarPorId(cursoId);
        if (cursoOpt.isEmpty()) return "redirect:/docente";

        Curso curso = cursoOpt.get();

        // Verificar pertenencia del docente al curso
        Docente docente = getDocente(auth);
        if (docente != null && !docentePerteneceACurso(docente, curso)) {
            redirectAttributes.addFlashAttribute("mensaje", "No tenés acceso a ese curso.");
            return "redirect:/docente";
        }

        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);
        List<Pool> pools = poolRepository.findByCurso(curso);

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("curso", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("pools", pools);
        model.addAttribute("autoevaluaciones", autoevaluacionRepository.findByCurso(curso));
        model.addAttribute("titulo", "Gestionar: " + curso.getNombre());

        return "pages/docente/gestionar-curso";
    }

    // ─── Nueva Unidad ──────────────────────────────────────────────────────────

    @GetMapping("/curso/{cursoId}/unidad/nueva")
    public String nuevaUnidadForm(@PathVariable Integer cursoId, Model model, Authentication auth) {
        Optional<Curso> cursoOpt = cursoService.buscarPorId(cursoId);
        if (cursoOpt.isEmpty()) return "redirect:/docente";

        int siguiente = unidadService.contarUnidadesPorCurso(cursoOpt.get()) + 1;

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("curso", cursoOpt.get());
        model.addAttribute("numeroOrdenSugerido", siguiente);
        model.addAttribute("titulo", "Nueva Unidad | Idóneos Online");

        return "pages/docente/nueva-unidad";
    }

    @PostMapping("/curso/{cursoId}/unidad/guardar")
    public String guardarUnidad(@PathVariable Integer cursoId,
                                @RequestParam("titulo") String titulo,
                                @RequestParam("descripcion") String descripcion,
                                @RequestParam("numeroOrden") int numeroOrden,
                                RedirectAttributes redirectAttributes) {

        Optional<Curso> cursoOpt = cursoService.buscarPorId(cursoId);
        if (cursoOpt.isEmpty()) return "redirect:/docente";

        Unidad unidad = new Unidad(titulo, descripcion, numeroOrden, cursoOpt.get());
        unidadService.guardar(unidad);

        redirectAttributes.addFlashAttribute("mensaje", "¡Unidad creada correctamente!");
        return "redirect:/docente/curso/" + cursoId + "/gestionar";
    }

    /** CU-12: Editar unidad. */
    @GetMapping("/unidad/{unidadId}/editar")
    public String editarUnidadForm(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(unidadId);
        if (uOpt.isEmpty()) return "redirect:/docente";
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", uOpt.get());
        model.addAttribute("titulo", "Editar Unidad | Idóneos Online");
        return "pages/docente/editar-unidad";
    }

    @PostMapping("/unidad/{unidadId}/editar")
    public String guardarEdicionUnidad(@PathVariable Integer unidadId,
                                       @RequestParam String titulo,
                                       @RequestParam String descripcion,
                                       @RequestParam int numeroOrden,
                                       RedirectAttributes ra) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(unidadId);
        if (uOpt.isEmpty()) return "redirect:/docente";
        Unidad unidad = uOpt.get();
        unidad.setTitulo(titulo);
        unidad.setDescripcion(descripcion);
        unidad.setNumeroOrden(numeroOrden);
        unidadService.guardar(unidad);
        ra.addFlashAttribute("mensaje", "Unidad actualizada correctamente.");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }

    @PostMapping("/unidad/{unidadId}/eliminar")
    public String eliminarUnidad(@PathVariable Integer unidadId, @RequestParam("cursoId") Integer cursoId,
                                  RedirectAttributes redirectAttributes) {
        unidadService.buscarPorId(unidadId).ifPresent(unidadService::borrar);
        redirectAttributes.addFlashAttribute("mensaje", "Unidad eliminada.");
        return "redirect:/docente/curso/" + cursoId + "/gestionar";
    }

    // ─── Nuevo Material ────────────────────────────────────────────────────────

    @GetMapping("/unidad/{unidadId}/material/nuevo")
    public String nuevoMaterialForm(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Optional<Unidad> unidadOpt = unidadService.buscarPorId(unidadId);
        if (unidadOpt.isEmpty()) return "redirect:/docente";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", unidadOpt.get());
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "Nuevo Material | Idóneos Online");

        return "pages/docente/nuevo-material";
    }

    @PostMapping("/unidad/{unidadId}/material/guardar")
    public String guardarMaterial(@PathVariable Integer unidadId,
                                   @RequestParam("tipoId") Integer tipoId,
                                   @RequestParam("titulo") String titulo,
                                   @RequestParam("rutaArchivo") String rutaArchivo,
                                   RedirectAttributes redirectAttributes) {

        Optional<Unidad> unidadOpt = unidadService.buscarPorId(unidadId);
        if (unidadOpt.isEmpty()) return "redirect:/docente";

        Unidad unidad = unidadOpt.get();
        TipoMaterial tipo = tipoMaterialRepository.findById(tipoId).orElse(null);
        if (tipo == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Tipo de material no válido.");
            return "redirect:/docente/unidad/" + unidadId + "/material/nuevo";
        }
        Material material = new Material(tipo, titulo, rutaArchivo, unidad);
        materialService.guardar(material);

        redirectAttributes.addFlashAttribute("mensaje", "¡Material cargado correctamente!");
        return "redirect:/docente/curso/" + unidad.getCurso().getId() + "/gestionar";
    }

    /** CU-17: Editar material (incluyendo toggle de publicación). */
    @GetMapping("/material/{materialId}/editar")
    public String editarMaterialForm(@PathVariable Integer materialId, Model model, Authentication auth) {
        Optional<Material> mOpt = materialService.buscarPorId(materialId);
        if (mOpt.isEmpty()) return "redirect:/docente";
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("material", mOpt.get());
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "Editar Material | Idóneos Online");
        return "pages/docente/editar-material";
    }

    @PostMapping("/material/{materialId}/editar")
    public String guardarEdicionMaterial(@PathVariable Integer materialId,
                                          @RequestParam String titulo,
                                          @RequestParam String rutaArchivo,
                                          @RequestParam Integer tipoId,
                                          @RequestParam(defaultValue = "false") boolean publicado,
                                          RedirectAttributes ra) {
        Optional<Material> mOpt = materialService.buscarPorId(materialId);
        if (mOpt.isEmpty()) return "redirect:/docente";
        Material material = mOpt.get();
        material.setTitulo(titulo);
        material.setRutaArchivo(rutaArchivo);
        material.setPublicado(publicado);
        tipoMaterialRepository.findById(tipoId).ifPresent(material::setTipo);
        materialService.guardar(material);
        ra.addFlashAttribute("mensaje", "Material actualizado correctamente.");
        return "redirect:/docente/curso/" + material.getUnidad().getCurso().getId() + "/gestionar";
    }

    /** Toggle publicación de material (publicado/oculto). */
    @PostMapping("/material/{materialId}/toggle-publicado")
    public String togglePublicadoMaterial(@PathVariable Integer materialId, RedirectAttributes ra) {
        Optional<Material> mOpt = materialService.buscarPorId(materialId);
        if (mOpt.isPresent()) {
            Material m = mOpt.get();
            m.setPublicado(!m.isPublicado());
            materialService.guardar(m);
            ra.addFlashAttribute("mensaje", m.isPublicado() ? "Material publicado." : "Material ocultado.");
            return "redirect:/docente/curso/" + m.getUnidad().getCurso().getId() + "/gestionar";
        }
        return "redirect:/docente";
    }

    @PostMapping("/material/{materialId}/eliminar")
    public String eliminarMaterial(@PathVariable Integer materialId, @RequestParam("cursoId") Integer cursoId,
                                    RedirectAttributes redirectAttributes) {
        materialService.buscarPorId(materialId).ifPresent(materialService::borrar);
        redirectAttributes.addFlashAttribute("mensaje", "Material eliminado.");
        return "redirect:/docente/curso/" + cursoId + "/gestionar";
    }

    // ─── Glosario (CU-19 a CU-22) ─────────────────────────────────────────────

    @GetMapping("/unidad/{unidadId}/glosario")
    public String verGlosario(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(unidadId);
        if (uOpt.isEmpty()) return "redirect:/docente";
        Unidad unidad = uOpt.get();
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("unidad", unidad);
        model.addAttribute("terminos", terminoGlosarioRepository.findByUnidadAndBajaFalse(unidad));
        model.addAttribute("titulo", "Glosario — " + unidad.getTitulo());
        return "pages/docente/glosario-unidad";
    }

    /** CU-19: Alta de término de glosario. */
    @PostMapping("/unidad/{unidadId}/glosario/agregar")
    public String agregarTermino(@PathVariable Integer unidadId,
                                  @RequestParam String termino,
                                  @RequestParam String definicion,
                                  RedirectAttributes ra) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(unidadId);
        if (uOpt.isEmpty()) return "redirect:/docente";
        TerminoGlosario tg = new TerminoGlosario(termino, definicion, uOpt.get());
        terminoGlosarioRepository.save(tg);
        ra.addFlashAttribute("mensaje", "Término '" + termino + "' agregado al glosario.");
        return "redirect:/docente/unidad/" + unidadId + "/glosario";
    }

    /** CU-20: Modificar término de glosario. */
    @PostMapping("/termino/{terminoId}/editar")
    public String editarTermino(@PathVariable Integer terminoId,
                                @RequestParam String termino,
                                @RequestParam String definicion,
                                RedirectAttributes ra) {
        Optional<TerminoGlosario> tOpt = terminoGlosarioRepository.findById(terminoId);
        if (tOpt.isEmpty()) return "redirect:/docente";
        TerminoGlosario tg = tOpt.get();
        tg.setTermino(termino);
        tg.setDefinicion(definicion);
        terminoGlosarioRepository.save(tg);
        ra.addFlashAttribute("mensaje", "Término actualizado.");
        return "redirect:/docente/unidad/" + tg.getUnidad().getId() + "/glosario";
    }

    /** CU-21: Baja de término de glosario. */
    @PostMapping("/termino/{terminoId}/borrar")
    public String borrarTermino(@PathVariable Integer terminoId, RedirectAttributes ra) {
        Optional<TerminoGlosario> tOpt = terminoGlosarioRepository.findById(terminoId);
        if (tOpt.isPresent()) {
            TerminoGlosario tg = tOpt.get();
            tg.setBaja(true);
            terminoGlosarioRepository.save(tg);
            ra.addFlashAttribute("mensaje", "Término eliminado del glosario.");
            return "redirect:/docente/unidad/" + tg.getUnidad().getId() + "/glosario";
        }
        return "redirect:/docente";
    }

    // ─── Autoevaluaciones (CU-46 a CU-49) ────────────────────────────────────

    /** CU-46: Consultar autoevaluaciones del curso. */
    @GetMapping("/curso/{cursoId}/autoevaluaciones")
    public String verAutoevaluaciones(@PathVariable Integer cursoId, Model model, Authentication auth) {
        Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
        if (cOpt.isEmpty()) return "redirect:/docente";
        Curso curso = cOpt.get();
        List<Pool> pools = poolRepository.findByCurso(curso);
        List<Autoevaluacion> autos = autoevaluacionRepository.findByCurso(curso);
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("curso", curso);
        model.addAttribute("pools", pools);
        model.addAttribute("autoevaluaciones", autos);
        model.addAttribute("titulo", "Autoevaluaciones — " + curso.getNombre());
        return "pages/docente/gestionar-autoevaluacion";
    }

    /** CU-47: Alta de autoevaluación. */
    @PostMapping("/curso/{cursoId}/autoevaluacion/crear")
    public String crearAutoevaluacion(@PathVariable Integer cursoId,
                                       @RequestParam String nombre,
                                       @RequestParam List<Integer> poolIds,
                                       RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(cursoId);
        if (cOpt.isEmpty()) return "redirect:/docente";

        Autoevaluacion ae = new Autoevaluacion(nombre, null, 3);
        List<Pool> pools = poolRepository.findAllById(poolIds);
        ae.setPools(pools);
        autoevaluacionRepository.save(ae);
        ra.addFlashAttribute("mensaje", "Autoevaluación '" + nombre + "' creada correctamente.");
        return "redirect:/docente/curso/" + cursoId + "/autoevaluaciones";
    }

    /** CU-48: Modificar autoevaluación. */
    @PostMapping("/autoevaluacion/{autoId}/editar")
    public String editarAutoevaluacion(@PathVariable Integer autoId,
                                        @RequestParam String nombre,
                                        @RequestParam List<Integer> poolIds,
                                        RedirectAttributes ra) {
        Optional<Autoevaluacion> aOpt = autoevaluacionRepository.findById(autoId);
        if (aOpt.isEmpty()) return "redirect:/docente";
        Autoevaluacion ae = aOpt.get();
        ae.setNombre(nombre);
        ae.setPools(poolRepository.findAllById(poolIds));
        autoevaluacionRepository.save(ae);
        ra.addFlashAttribute("mensaje", "Autoevaluación actualizada.");
        // Redirigir al primer curso del primer pool
        if (!ae.getPools().isEmpty() && ae.getPools().get(0).getUnidad() != null) {
            return "redirect:/docente/curso/" + ae.getPools().get(0).getUnidad().getCurso().getId() + "/autoevaluaciones";
        }
        return "redirect:/docente";
    }

    /** CU-49: Baja de autoevaluación. */
    @PostMapping("/autoevaluacion/{autoId}/borrar")
    public String borrarAutoevaluacion(@PathVariable Integer autoId,
                                        @RequestParam Integer cursoId,
                                        RedirectAttributes ra) {
        autoevaluacionRepository.findById(autoId).ifPresent(ae -> {
            ae.setBaja(true);
            autoevaluacionRepository.save(ae);
        });
        ra.addFlashAttribute("mensaje", "Autoevaluación eliminada.");
        return "redirect:/docente/curso/" + cursoId + "/autoevaluaciones";
    }
}
