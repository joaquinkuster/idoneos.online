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
 * Panel de Administración — Idóneos Online.
 * Cubre CU-01 a CU-08 (Cursos), CU-09 a CU-11 (Categorías),
 * CU-68 a CU-78 (Usuarios, Docentes, Administradores).
 * RN-07: Al menos 1 admin activo. RN-11: Docente titular con cursos publicados no puede darse de baja.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UsuarioServiceImpl usuarioService;
    @Autowired private CursoServiceImpl cursoService;
    @Autowired private CategoriaServiceImpl categoriaService;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private DocenteCursoRepository docenteCursoRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private ModalidadRepository modalidadRepository;
    @Autowired private EmailService emailService;

    // ─── Panel Principal ───────────────────────────────────────────────────────

    @GetMapping
    public String verPanelAdmin(Model model, Authentication auth) {
        Double totalIngresos = pagoRepository.findAll().stream()
                .filter(p -> "Acreditado".equals(p.getEstadoPago().getNombre()))
                .mapToDouble(Pago::getMonto).sum();

        long totalAlumnos = usuarioService.obtenerTodo().stream().filter(Usuario::esAlumno).count();
        long totalDocentes = usuarioService.obtenerTodo().stream().filter(u -> u.getRol() == RolUsuario.Docente).count();
        long totalInscripciones = inscripcionRepository.count();

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("totalUsuarios", usuarioService.obtenerTodo().size());
        model.addAttribute("totalAlumnos", totalAlumnos);
        model.addAttribute("totalDocentes", totalDocentes);
        model.addAttribute("totalCursos", cursoService.obtenerTodo().size());
        model.addAttribute("totalCategorias", categoriaService.obtenerTodo().size());
        model.addAttribute("totalInscripciones", totalInscripciones);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("titulo", "Panel de Administración | Idóneos Online");
        return "pages/admin/panel";
    }

    // ─── Gestión de Usuarios ───────────────────────────────────────────────────

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("usuarios", usuarioService.obtenerTodo());
        model.addAttribute("roles", RolUsuario.values());
        model.addAttribute("titulo", "Gestión de Usuarios | Idóneos Online");
        return "pages/admin/usuarios";
    }

    /** CU-69: Alta de usuario (alumno o administrador) por parte del admin. */
    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("roles", new RolUsuario[]{RolUsuario.Alumno, RolUsuario.Administrador});
        model.addAttribute("titulo", "Nuevo Usuario | Idóneos Online");
        return "pages/admin/nuevo-usuario";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarNuevoUsuario(@RequestParam String nombre,
                                      @RequestParam String apellido,
                                      @RequestParam String correo,
                                      @RequestParam String contrasena,
                                      @RequestParam RolUsuario rol,
                                      RedirectAttributes ra) {
        try {
            if (rol == RolUsuario.Administrador) {
                usuarioService.registrarAdministrador(nombre, apellido, correo, contrasena);
            } else {
                usuarioService.registrarAlumno(nombre, apellido, correo, contrasena);
            }
            ra.addFlashAttribute("mensaje", "Usuario '" + correo + "' creado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /** CU-70: Modificar datos de un usuario. */
    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuarioForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Usuario> uOpt = usuarioService.buscarPorId(id);
        if (uOpt.isEmpty()) return "redirect:/admin/usuarios";
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("usuarioEditar", uOpt.get());
        model.addAttribute("titulo", "Editar Usuario | Idóneos Online");
        return "pages/admin/editar-usuario";
    }

    @PostMapping("/usuarios/{id}/editar")
    public String guardarEdicionUsuario(@PathVariable Integer id,
                                        @RequestParam String nombre,
                                        @RequestParam String apellido,
                                        @RequestParam(required = false) String telefono,
                                        RedirectAttributes ra) {
        Optional<Usuario> uOpt = usuarioService.buscarPorId(id);
        if (uOpt.isEmpty()) return "redirect:/admin/usuarios";
        Usuario u = uOpt.get();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setTelefono(telefono);
        usuarioService.modificar(u);
        ra.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
        return "redirect:/admin/usuarios";
    }

    /**
     * CU-71: Dar de baja (toggle) a un usuario.
     * RN-07: Si es el último admin activo, no se puede dar de baja.
     * RN-11: Si es docente titular con cursos publicados, no se puede dar de baja.
     */
    @PostMapping("/usuarios/{id}/toggle-baja")
    public String toggleBaja(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> uOpt = usuarioService.buscarPorId(id);
        if (uOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado.");
            return "redirect:/admin/usuarios";
        }
        Usuario u = uOpt.get();

        // RN-07: validar unicidad de administrador
        if (u.getRol() == RolUsuario.Administrador && !u.getBaja()) {
            if (usuarioService.contarAdministradoresActivos() <= 1) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "No es posible dar de baja al único administrador activo del sistema (RN-07).");
                return "redirect:/admin/usuarios";
            }
        }

        // RN-11: docente titular con cursos publicados
        if (u.getRol() == RolUsuario.Docente && !u.getBaja() && u.getDocente() != null) {
            boolean tieneCursosPublicados = docenteCursoRepository
                    .existsByDocenteAndEsSupervisorFalseAndCurso_PublicadoTrue(u.getDocente());
            if (tieneCursosPublicados) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "No es posible dar de baja a un docente titular con cursos publicados (RN-11). "
                                + "Despublicá sus cursos primero.");
                return "redirect:/admin/usuarios";
            }
        }

        u.setBaja(!u.getBaja());
        usuarioService.modificar(u);
        redirectAttributes.addFlashAttribute("mensaje", "Estado del usuario actualizado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/cambiar-rol")
    public String cambiarRol(@PathVariable Integer id,
                              @RequestParam("rol") RolUsuario rol,
                              RedirectAttributes redirectAttributes) {
        usuarioService.buscarPorId(id).ifPresent(u -> {
            u.setRol(rol);
            usuarioService.modificar(u);
        });
        redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle-clon-ia")
    public String toggleClonIA(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> uOpt = usuarioService.buscarPorId(id);
        uOpt.ifPresent(u -> {
            if (u.getDocente() != null) {
                Docente d = u.getDocente();
                if (d.getFechaConsentimientoClon() != null) {
                    d.setFechaConsentimientoClon(null);
                } else {
                    d.setFechaConsentimientoClon(LocalDateTime.now());
                }
                usuarioService.modificar(u);
            }
        });
        redirectAttributes.addFlashAttribute("mensaje", "Estado de Clon IA actualizado.");
        return "redirect:/admin/usuarios";
    }

    // ─── Gestión de Docentes ───────────────────────────────────────────────────

    /** CU-72: Consultar docentes. */
    @GetMapping("/docentes")
    public String listarDocentes(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("titulo", "Gestión de Docentes | Idóneos Online");
        return "pages/admin/usuarios";  // reutiliza la vista de usuarios con filtro docentes
    }

    /** CU-74: Alta de docente por parte del administrador. */
    @GetMapping("/docentes/nuevo")
    public String nuevoDocenteForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("titulo", "Nuevo Docente | Idóneos Online");
        return "pages/admin/nuevo-docente";
    }

    @PostMapping("/docentes/guardar")
    public String guardarNuevoDocente(@RequestParam String nombre,
                                      @RequestParam String apellido,
                                      @RequestParam String correo,
                                      @RequestParam(required = false) String telefono,
                                      @RequestParam(required = false) String biografia,
                                      @RequestParam(required = false) Integer aniosExperiencia,
                                      RedirectAttributes ra) {
        try {
            Docente docente = usuarioService.registrarDocente(nombre, apellido, correo, telefono, biografia, aniosExperiencia);
            // CU-74: enviar bienvenida con link para definir contraseña
            String urlLoginTmp = "http://localhost:8080/login";
            emailService.enviarBienvenidaDocente(docente.getUsuario(), urlLoginTmp);
            ra.addFlashAttribute("mensaje", "Docente '" + nombre + " " + apellido + "' registrado. Se envió un email de bienvenida.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", "Error al registrar docente: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /** CU-75: Modificar datos del docente. */
    @GetMapping("/docentes/{id}/editar")
    public String editarDocenteForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Docente> dOpt = docenteRepository.findById(id);
        if (dOpt.isEmpty()) return "redirect:/admin/usuarios";
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("docenteEditar", dOpt.get());
        model.addAttribute("titulo", "Editar Docente | Idóneos Online");
        return "pages/admin/editar-docente";
    }

    @PostMapping("/docentes/{id}/editar")
    public String guardarEdicionDocente(@PathVariable Integer id,
                                        @RequestParam String nombre,
                                        @RequestParam String apellido,
                                        @RequestParam(required = false) String telefono,
                                        @RequestParam(required = false) String biografia,
                                        @RequestParam(required = false) Integer aniosExperiencia,
                                        RedirectAttributes ra) {
        Optional<Docente> dOpt = docenteRepository.findById(id);
        if (dOpt.isEmpty()) return "redirect:/admin/usuarios";
        Docente docente = dOpt.get();
        Usuario u = docente.getUsuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setTelefono(telefono);
        docente.setBiografia(biografia);
        if (aniosExperiencia != null) docente.setAniosExperiencia(aniosExperiencia);
        docenteRepository.save(docente);
        usuarioService.modificar(u);
        ra.addFlashAttribute("mensaje", "Datos del docente actualizados correctamente.");
        return "redirect:/admin/usuarios";
    }

    // ─── Gestión de Cursos ─────────────────────────────────────────────────────

    @GetMapping("/cursos")
    public String listarCursos(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Cursos | Idóneos Online");
        return "pages/admin/cursos";
    }

    /** CU-01: Alta de curso desde el panel del admin. */
    @GetMapping("/cursos/nuevo")
    public String nuevoCursoAdminForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("modalidades", modalidadRepository.findAll());
        model.addAttribute("titulo", "Nuevo Curso | Idóneos Online");
        return "pages/admin/nuevo-curso";
    }

    @PostMapping("/cursos/guardar")
    public String guardarNuevoCurso(@RequestParam String nombre,
                                    @RequestParam String descripcion,
                                    @RequestParam float precio,
                                    @RequestParam Integer categoriaId,
                                    @RequestParam Integer docenteTitularId,
                                    @RequestParam(required = false) Integer docenteSupervisorId,
                                    @RequestParam(defaultValue = "12") int mesesAcceso,
                                    RedirectAttributes ra) {
        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);
        if (catOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Categoría no válida.");
            return "redirect:/admin/cursos/nuevo";
        }
        Optional<Docente> docenteTitularOpt = docenteRepository.findById(docenteTitularId);
        if (docenteTitularOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "Docente titular no válido.");
            return "redirect:/admin/cursos/nuevo";
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get());
        curso.setMesesAcceso(mesesAcceso);
        Curso cursoDef = cursoService.guardar(curso);

        docenteCursoRepository.save(new DocenteCurso(docenteTitularOpt.get(), cursoDef, false));
        if (docenteSupervisorId != null && !docenteSupervisorId.equals(docenteTitularId)) {
            docenteRepository.findById(docenteSupervisorId).ifPresent(sup ->
                    docenteCursoRepository.save(new DocenteCurso(sup, cursoDef, true)));
        }
        ra.addFlashAttribute("mensaje", "Curso '" + nombre + "' creado correctamente.");
        return "redirect:/admin/cursos";
    }

    /** CU-02: Modificar curso. */
    @GetMapping("/cursos/{id}/editar")
    public String editarCursoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/admin/cursos";
        Curso curso = cOpt.get();
        List<DocenteCurso> asignaciones = docenteCursoRepository.findByCurso(curso);

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("cursoEditar", curso);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("asignaciones", asignaciones);
        model.addAttribute("titulo", "Editar Curso | Idóneos Online");
        return "pages/admin/editar-curso";
    }

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
        if (catOpt.isEmpty()) { ra.addFlashAttribute("mensaje", "Categoría no válida."); return "redirect:/admin/cursos/" + id + "/editar"; }

        Curso curso = cOpt.get();
        curso.setNombre(nombre);
        curso.setDescripcion(descripcion);
        curso.setPrecio(precio);
        curso.setCategoria(catOpt.get());
        curso.setMesesAcceso(mesesAcceso);
        cursoService.modificar(curso);

        // Reasignar docentes
        docenteCursoRepository.deleteByCurso(curso);
        docenteRepository.findById(docenteTitularId).ifPresent(d ->
                docenteCursoRepository.save(new DocenteCurso(d, curso, false)));
        if (docenteSupervisorId != null && !docenteSupervisorId.equals(docenteTitularId)) {
            docenteRepository.findById(docenteSupervisorId).ifPresent(sup ->
                    docenteCursoRepository.save(new DocenteCurso(sup, curso, true)));
        }
        ra.addFlashAttribute("mensaje", "Curso actualizado correctamente.");
        return "redirect:/admin/cursos";
    }

    /**
     * CU-03: Toggle publicación de curso.
     * RN-02: Al menos 10 unidades. RN-03: Al menos 1 material publicado.
     */
    @PostMapping("/cursos/{id}/toggle-publicado")
    public String togglePublicado(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) { redirectAttributes.addFlashAttribute("mensaje", "Curso no encontrado."); return "redirect:/admin/cursos"; }
        Curso c = cOpt.get();
        if (!c.getPublicado()) {
            // Validar RN-02: mínimo 10 unidades
            int unidades = c.getUnidades() != null ? c.getUnidades().size() : 0;
            if (unidades < 10) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "No se puede publicar el curso: tiene " + unidades + " unidades. Mínimo requerido: 10 (RN-02).");
                return "redirect:/admin/cursos";
            }
        }
        c.setPublicado(!c.getPublicado());
        cursoService.modificar(c);
        redirectAttributes.addFlashAttribute("mensaje", "Estado de publicación actualizado.");
        return "redirect:/admin/cursos";
    }

    /**
     * CU-04: Baja de curso.
     * Valida que no tenga inscripciones vigentes antes de dar de baja.
     */
    @PostMapping("/cursos/{id}/eliminar")
    public String eliminarCurso(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) { redirectAttributes.addFlashAttribute("mensaje", "Curso no encontrado."); return "redirect:/admin/cursos"; }
        Curso c = cOpt.get();
        if (inscripcionRepository.existsByCursoAndBajaFalse(c)) {
            redirectAttributes.addFlashAttribute("mensaje",
                    "No es posible dar de baja un curso con inscripciones vigentes (RN-04). Deshabilite las inscripciones primero.");
            return "redirect:/admin/cursos";
        }
        cursoService.borrar(c);
        redirectAttributes.addFlashAttribute("mensaje", "Curso eliminado del sistema.");
        return "redirect:/admin/cursos";
    }

    // ─── Gestión de Categorías ─────────────────────────────────────────────────

    @GetMapping("/categorias")
    public String listarCategorias(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Categorías | Idóneos Online");
        return "pages/admin/categorias";
    }

    @PostMapping("/categorias/crear")
    public String crearCategoria(@RequestParam("nombre") String nombre,
                                 @RequestParam("descripcion") String descripcion,
                                 RedirectAttributes redirectAttributes) {
        Categoria cat = new Categoria(nombre, descripcion);
        categoriaService.guardar(cat);
        redirectAttributes.addFlashAttribute("mensaje", "Categoría creada con éxito.");
        return "redirect:/admin/categorias";
    }

    /** CU-06: Modificar categoría. */
    @GetMapping("/categorias/{id}/editar")
    public String editarCategoriaForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Categoria> cOpt = categoriaService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/admin/categorias";
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categoriaEditar", cOpt.get());
        model.addAttribute("titulo", "Editar Categoría | Idóneos Online");
        return "pages/admin/editar-categoria";
    }

    @PostMapping("/categorias/{id}/editar")
    public String guardarEdicionCategoria(@PathVariable Integer id,
                                          @RequestParam String nombre,
                                          @RequestParam String descripcion,
                                          RedirectAttributes ra) {
        Optional<Categoria> cOpt = categoriaService.buscarPorId(id);
        if (cOpt.isEmpty()) { ra.addFlashAttribute("mensaje", "Categoría no encontrada."); return "redirect:/admin/categorias"; }
        Categoria cat = cOpt.get();
        cat.setNombre(nombre);
        cat.setDescripcion(descripcion);
        categoriaService.modificar(cat);
        ra.addFlashAttribute("mensaje", "Categoría actualizada correctamente.");
        return "redirect:/admin/categorias";
    }

    @PostMapping("/categorias/{id}/eliminar")
    public String eliminarCategoria(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        categoriaService.buscarPorId(id).ifPresent(c -> {
            c.setBaja(true);
            categoriaService.modificar(c);
        });
        redirectAttributes.addFlashAttribute("mensaje", "Categoría eliminada.");
        return "redirect:/admin/categorias";
    }
}
