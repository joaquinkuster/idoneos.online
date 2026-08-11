package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.Categoria.CategoriaServiceImpl;
import com.app.idoneos.service.Curso.CursoServiceImpl;
import com.app.idoneos.service.EmailService;
import com.app.idoneos.service.Usuario.UsuarioServiceImpl;
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
 * Controller para la Administración Central del Sistema (CU-01 a CU-09, CU-75 a CU-88).
 * Administra Cursos, Categorías, Usuarios, Docentes y la regla RN-07 (mínimo 1 admin activo) y RN-11 (docentes con cursos).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UsuarioServiceImpl usuarioService;
    @Autowired private CursoServiceImpl cursoService;
    @Autowired private CategoriaServiceImpl categoriaService;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private DictadoDocenteRepository dictadoDocenteRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private DictadoRepository dictadoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private ModalidadRepository modalidadRepository;
    @Autowired private EmailService emailService;

    /**
     * CU-89 — Dashboard principal de administración e indicadores generales.
     */
    @GetMapping
    public String verPanelAdmin(Model model, Authentication auth) {
        Double totalIngresos = pagoRepository.findAll().stream()
                .filter(p -> p.getEstadoPago() != null && "Acreditado".equals(p.getEstadoPago().getNombre()))
                .mapToDouble(Pago::getMonto)
                .sum();

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("totalCursos", cursoService.obtenerTodo().size());
        model.addAttribute("totalUsuarios", usuarioService.obtenerTodo().size());
        model.addAttribute("totalInscripciones", inscripcionRepository.count());
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("titulo", "Panel de Administración | Idóneos Online");
        return "pages/admin/dashboard";
    }

    /**
     * CU-76 — Buscar y listar usuarios registrados en el sistema.
     */
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("usuarios", usuarioService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Usuarios | Idóneos Online");
        return "pages/admin/usuarios";
    }

    /**
     * CU-77 / CU-82 — Formulario de alta de usuario por parte del Administrador.
     */
    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("roles", RolUsuario.values());
        model.addAttribute("titulo", "Nuevo Usuario | Idóneos Online");
        return "pages/admin/nuevo-usuario";
    }

    /**
     * CU-77 / CU-82 — Registrar usuario con rol por parte del Administrador.
     */
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@RequestParam String nombre,
                                 @RequestParam String apellido,
                                 @RequestParam String correo,
                                 @RequestParam(required = false) String contrasena,
                                 @RequestParam RolUsuario rol,
                                 RedirectAttributes redirectAttributes) {

        if (usuarioService.buscarPorCorreo(correo).isPresent()) {
            redirectAttributes.addFlashAttribute("mensaje", "CU-77 Excepción paso 5: El correo ya está registrado.");
            return "redirect:/admin/usuarios/nuevo";
        }

        Usuario nuevo = usuarioService.crearUsuarioConRol(nombre, apellido, correo, contrasena, rol);

        if (rol == RolUsuario.Docente && nuevo.getDocente() != null) {
            Docente d = nuevo.getDocente();
            d.setHabilitado(true);
            docenteRepository.save(d);
        }

        redirectAttributes.addFlashAttribute("mensaje", "Usuario " + nombre + " (" + rol + ") creado correctamente.");
        return "redirect:/admin/usuarios";
    }

    /**
     * CU-79 — Dar de baja un usuario (Baja Lógica).
     * Reglas de negocio:
     * - RN-07: Impide la baja del único administrador activo.
     * - RN-11: Impide la baja de un docente titular con cursos publicados.
     */
    @PostMapping("/usuarios/{id}/baja")
    public String darBajaUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> uOpt = usuarioService.buscarPorId(id);
        if (cOptEsVacio(uOpt)) return "redirect:/admin/usuarios";
        Usuario u = uOpt.get();

        if (u.getRol() == RolUsuario.Administrador && !u.getBaja()) {
            long adminsActivos = usuarioService.contarAdministradoresActivos();
            if (adminsActivos <= 1) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "RN-07 Excepción: No es posible dar de baja al único administrador activo del sistema.");
                return "redirect:/admin/usuarios";
            }
        }

        if (u.getRol() == RolUsuario.Docente && !u.getBaja() && u.getDocente() != null) {
            boolean tieneCursosPublicados = dictadoDocenteRepository.findByDocente(u.getDocente()).stream()
                    .anyMatch(dd -> !dd.isEsSupervisor() && dd.getDictado() != null 
                            && dd.getDictado().getPrograma() != null 
                            && dd.getDictado().getPrograma().getCurso() != null 
                            && Boolean.TRUE.equals(dd.getDictado().getPrograma().getCurso().getPublicado()));
            if (tieneCursosPublicados) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "RN-11 Excepción: No es posible dar de baja a un docente titular con cursos publicados.");
                return "redirect:/admin/usuarios";
            }
        }

        usuarioService.darDeBaja(id);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado.");
        return "redirect:/admin/usuarios";
    }

    private boolean cOptEsVacio(Optional<?> opt) { return opt.isEmpty(); }

    /**
     * CU-01 — Listar la totalidad de cursos del catálogo (publicados, no publicados y dados de baja).
     */
    @GetMapping("/cursos")
    public String listarCursos(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Cursos | Idóneos Online");
        return "pages/admin/cursos";
    }

    /**
     * CU-02 — Formulario de registro de nuevo curso desde el panel de administración.
     */
    @GetMapping("/cursos/nuevo")
    public String nuevoCursoForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("titulo", "Nuevo Curso | Idóneos Online");
        return "pages/admin/nuevo-curso";
    }

    /**
     * CU-02 — Registrar curso con asignación de docente titular y supervisor.
     */
    @PostMapping("/cursos/guardar")
    public String guardarCurso(@RequestParam String nombre,
                               @RequestParam String descripcion,
                               @RequestParam float precio,
                               @RequestParam Integer categoriaId,
                               @RequestParam Integer docenteTitularId,
                               @RequestParam(required = false) Integer docenteSupervisorId,
                               @RequestParam(defaultValue = "12") int mesesAcceso,
                               RedirectAttributes ra) {
        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);
        if (catOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "CU-02 Excepción paso 4: Categoría seleccionada inválida.");
            return "redirect:/admin/cursos/nuevo";
        }
        Optional<Docente> docenteTitularOpt = docenteRepository.findById(docenteTitularId);
        if (docenteTitularOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "CU-02 Excepción paso 4: Docente titular seleccionado inválido.");
            return "redirect:/admin/cursos/nuevo";
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get());
        curso.setMesesAcceso(mesesAcceso);
        Curso cursoDef = cursoService.guardar(curso);

        Programa prog = programaRepository.save(new Programa(nombre, descripcion, mesesAcceso, cursoDef));
        Dictado dictado = dictadoRepository.save(new Dictado(LocalDateTime.now(), LocalDateTime.now().plusMonths(6), 50, prog));

        dictadoDocenteRepository.save(new DictadoDocente(dictado, docenteTitularOpt.get(), false));
        if (docenteSupervisorId != null && !docenteSupervisorId.equals(docenteTitularId)) {
            docenteRepository.findById(docenteSupervisorId).ifPresent(sup ->
                    dictadoDocenteRepository.save(new DictadoDocente(dictado, sup, true)));
        }
        ra.addFlashAttribute("mensaje", "Curso '" + nombre + "' creado correctamente.");
        return "redirect:/admin/cursos";
    }

    /**
     * CU-03 — Formulario de modificación de curso.
     */
    @GetMapping("/cursos/{id}/editar")
    public String editarCursoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/admin/cursos";
        Curso curso = cOpt.get();
        
        List<DictadoDocente> asignaciones = dictadoDocenteRepository.findAll().stream()
                .filter(dd -> dd.getDictado() != null && dd.getDictado().getPrograma() != null && dd.getDictado().getPrograma().getCurso().getId() == curso.getId())
                .toList();

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("cursoEditar", curso);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("titulo", "Editar Curso | Idóneos Online");
        return "pages/admin/editar-curso";
    }

    /**
     * CU-03 / CU-04 — Guardar modificación de datos del curso.
     */
    @PostMapping("/cursos/{id}/editar")
    public String guardarEdicionCurso(@PathVariable Integer id,
                                      @RequestParam String nombre,
                                      @RequestParam String descripcion,
                                      @RequestParam float precio,
                                      @RequestParam Integer categoriaId,
                                      @RequestParam Integer docenteTitularId,
                                      @RequestParam(required = false) Integer docenteSupervisorId,
                                      @RequestParam(defaultValue = "12") int mesesAcceso,
                                      RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/admin/cursos";
        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);
        if (catOpt.isEmpty()) { ra.addFlashAttribute("mensaje", "CU-04 Excepción paso 4: Categoría inválida."); return "redirect:/admin/cursos/" + id + "/editar"; }

        Curso curso = cOpt.get();
        curso.setNombre(nombre);
        curso.setDescripcion(descripcion);
        curso.setPrecio(precio);
        curso.setCategoria(catOpt.get());
        curso.setMesesAcceso(mesesAcceso);
        cursoService.modificar(curso);

        List<Programa> progs = programaRepository.findByCurso(curso);
        Programa prog = progs.isEmpty() ? programaRepository.save(new Programa(nombre, descripcion, mesesAcceso, curso)) : progs.get(0);
        List<Dictado> dicts = dictadoRepository.findByPrograma(prog);
        Dictado dictado = dicts.isEmpty() ? dictadoRepository.save(new Dictado(LocalDateTime.now(), LocalDateTime.now().plusMonths(6), 50, prog)) : dicts.get(0);

        dictadoDocenteRepository.deleteByDictado(dictado);
        docenteRepository.findById(docenteTitularId).ifPresent(d ->
                dictadoDocenteRepository.save(new DictadoDocente(dictado, d, false)));
        if (docenteSupervisorId != null && !docenteSupervisorId.equals(docenteTitularId)) {
            docenteRepository.findById(docenteSupervisorId).ifPresent(sup ->
                    dictadoDocenteRepository.save(new DictadoDocente(dictado, sup, true)));
        }
        ra.addFlashAttribute("mensaje", "Curso actualizado correctamente.");
        return "redirect:/admin/cursos";
    }

    /**
     * CU-05 — Dar de baja un curso (Baja Lógica).
     */
    @PostMapping("/cursos/{id}/baja")
    public String darBajaCurso(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isPresent()) {
            Curso c = cOpt.get();
            c.setBaja(!c.getBaja());
            cursoService.modificar(c);
            ra.addFlashAttribute("mensaje", "Estado del curso actualizado.");
        }
        return "redirect:/admin/cursos";
    }

    /**
     * CU-03 — Cambiar estado de publicación del curso.
     */
    @PostMapping("/cursos/{id}/publicar")
    public String publicarCurso(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isPresent()) {
            Curso c = cOpt.get();
            c.setPublicado(!c.getPublicado());
            cursoService.modificar(c);
            ra.addFlashAttribute("mensaje", c.getPublicado() ? "Curso publicado." : "Curso despublicado.");
        }
        return "redirect:/admin/cursos";
    }

    /**
     * CU-06 — Listar categorías registradas.
     */
    @GetMapping("/categorias")
    public String listarCategorias(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "Categorías | Idóneos Online");
        return "pages/admin/categorias";
    }

    /**
     * CU-07 — Registrar nueva categoría.
     */
    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@RequestParam String nombre,
                                   @RequestParam String descripcion,
                                   RedirectAttributes ra) {
        Categoria c = new Categoria(nombre, descripcion);
        categoriaService.guardar(c);
        ra.addFlashAttribute("mensaje", "Categoría creada correctamente.");
        return "redirect:/admin/categorias";
    }
}
