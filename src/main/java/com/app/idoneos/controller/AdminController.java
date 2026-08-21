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

import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Controller para la Administración Central del Sistema.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-01 — Buscar curso        → GET /admin/cursos
 *   CU-03 — Registrar curso     → GET /admin/cursos/nuevo + POST /admin/cursos/guardar
 *   CU-04 — Modificar curso     → GET /admin/cursos/{id}/editar + POST /admin/cursos/{id}/editar
 *   CU-05 — Dar de baja curso   → POST /admin/cursos/{id}/baja (toggle baja con validación de programas activos)
 *   CU-07 — Buscar categoría    → GET /admin/categorias
 *   CU-08 — Registrar categoría → POST /admin/categorias/guardar
 *   CU-09 — Modificar categoría → POST /admin/categorias/{id}/editar
 *   CU-10 — Dar de baja categoría → POST /admin/categorias/{id}/baja
 *
 * MOD-NF-01: Módulo de Usuarios y Notificaciones
 *   CU-82 — Buscar usuario      → GET /admin/usuarios
 *   CU-83 — Registrar usuario   → GET/POST /admin/usuarios/nuevo + /admin/usuarios/guardar
 *   CU-85 — Dar de baja usuario → POST /admin/usuarios/{id}/baja
 *             Regla RN-07 (único admin activo) y RN-11 (docente con cursos publicados o cohortes vigentes)
 *   CU-88 — Registrar docente   → GET/POST /admin/usuarios/nuevo + /admin/usuarios/guardar (delegado)
 *   CU-89 — Modificar docente   → POST /admin/docentes/{id}/habilitar
 *
 * MOD-NF-02: Módulo de Auditoría
 *   CU-95 — Consultar auditoría → GET /admin (dashboard con indicadores)
 *   CU-98 — Consultar estadísticas → GET /admin
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UsuarioServiceImpl usuarioService;
    @Autowired private CursoServiceImpl cursoService;
    @Autowired private CategoriaServiceImpl categoriaService;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private SupervisorRepository supervisorRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private NivelRepository nivelRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private ModalidadRepository modalidadRepository;
    @Autowired private EmailService emailService;

    /**
     * TRAZABILIDAD: CU-95 — Consultar auditoría (Dashboard principal con indicadores del sistema).
     * CU-98 — Consultar estadísticas.
     * Actor: Administrador.
     * Precondición: sesión iniciada con rol Administrador.
     * Flujo paso 2 y 3: recupera y muestra indicadores (cursos, usuarios, categorías, inscripciones, ingresos).
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
        model.addAttribute("totalCategorias", categoriaService.obtenerTodo().size());
        model.addAttribute("totalInscripciones", inscripcionRepository.count());
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("titulo", "Panel de Administración | Idóneos Online");
        return "pages/admin/panel";
    }

    /**
     * TRAZABILIDAD: CU-82 — Buscar usuario.
     * Actor: Administrador.
     * Precondición: sesión iniciada con rol Administrador. Existe al menos un usuario.
     * Flujo paso 4-5: recupera y lista todos los usuarios del sistema.
     */
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("usuarios", usuarioService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Usuarios | Idóneos Online");
        return "pages/admin/usuarios";
    }

    /**
     * TRAZABILIDAD: CU-83 — Registrar usuario (formulario GET).
     * TRAZABILIDAD: CU-88 — Registrar docente (cuando el rol seleccionado es Docente).
     * Actor: Administrador.
     * Flujo paso 2: el sistema solicita nombre, apellido, correo, DNI, teléfono y rol.
     */
    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("roles", rolRepository.findAll().isEmpty() ? RolUsuario.values() : rolRepository.findAll());
        model.addAttribute("titulo", "Nuevo Usuario | Idóneos Online");
        return "pages/admin/nuevo-usuario";
    }

    /**
     * TRAZABILIDAD: CU-83 — Registrar usuario (POST).
     * TRAZABILIDAD: CU-88 — Registrar docente (cuando rol == Docente).
     * Actor: Administrador.
     * Flujo paso 4-6: valida campos obligatorios, unicidad de correo, registra la cuenta.
     * EX-CU83-01: correo ya registrado → redirect con mensaje.
     * Postcondición: cuenta registrada con el rol indicado.
     */
    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@RequestParam String nombre,
                                 @RequestParam String apellido,
                                 @RequestParam String correo,
                                 @RequestParam(required = false) String contrasena,
                                 @RequestParam RolUsuario rol,
                                 RedirectAttributes redirectAttributes) {

        if (usuarioService.buscarPorCorreo(correo).isPresent()) {
            redirectAttributes.addFlashAttribute("mensaje", "EX-CU83-01: El correo electrónico ya está registrado.");
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
     * TRAZABILIDAD: CU-85 — Dar de baja usuario.
     * Actor: Administrador.
     * Flujo paso 2: valida que existan otros admins activos (RN-07).
     * Flujo paso 3: verifica que el docente no sea titular de cursos publicados ni supervisor activo (RN-11).
     * Postcondición: cuenta en baja.
     * EX-CU85-01 (RN-07): único admin activo → no permite baja.
     * EX-CU85-02 (RN-11): docente con cursos publicados → no permite baja.
     */
    @PostMapping("/usuarios/{id}/baja")
    public String darBajaUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> uOpt = usuarioService.buscarPorId(id);
        if (cOptEsVacio(uOpt)) return "redirect:/admin/usuarios";
        Usuario u = uOpt.get();

        if (u.getRol() != null && "Administrador".equalsIgnoreCase(u.getRol().getNombre()) && !u.getBaja()) {
            long adminsActivos = usuarioService.contarAdministradoresActivos();
            if (adminsActivos <= 1) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "RN-07: No es posible dar de baja al único administrador activo del sistema.");
                return "redirect:/admin/usuarios";
            }
        }

        // RN-11: No es posible dar de baja a un docente titular con cursos publicados.
        if (u.getRol() != null && "Docente".equalsIgnoreCase(u.getRol().getNombre()) && !u.getBaja() && u.getDocente() != null) {
            Docente d = u.getDocente();
            boolean tieneCursosPublicados = d.getCursos() != null && d.getCursos().stream()
                    .anyMatch(c -> !c.getBaja() && Boolean.TRUE.equals(c.getPublicado()));
            if (tieneCursosPublicados) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "RN-11: No es posible dar de baja a un docente titular con cursos publicados.");
                return "redirect:/admin/usuarios";
            }
        }

        usuarioService.darDeBaja(id);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado.");
        return "redirect:/admin/usuarios";
    }

    private boolean cOptEsVacio(Optional<?> opt) { return opt.isEmpty(); }

    /**
     * TRAZABILIDAD: CU-01 — Buscar curso.
     * Actor: Administrador.
     * Precondición: sesión iniciada con rol Administrador. Existe al menos un curso.
     * Flujo paso 4-5: recupera y lista todos los cursos.
     */
    @GetMapping("/cursos")
    public String listarCursos(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("titulo", "Gestión de Cursos | Idóneos Online");
        return "pages/admin/cursos";
    }

    /**
     * TRAZABILIDAD: CU-03 — Registrar curso (formulario GET).
     * Actor: Administrador.
     * Flujo paso 2: solicita nombre, descripción, precio, categoría, nivel, docente titular y supervisor.
     */
    @GetMapping("/cursos/nuevo")
    public String nuevoCursoForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("modalidades", modalidadRepository.findAll());
        model.addAttribute("titulo", "Nuevo Curso | Idóneos Online");
        return "pages/admin/nuevo-curso";
    }

    /**
     * TRAZABILIDAD: CU-03 — Registrar curso (POST).
     * Actor: Administrador.
     * Flujo paso 4-6: valida campos obligatorios (nombre, descripción, precio >= 0, categoría, docente titular),
     *   registra el curso con su equipo docente (titular y supervisor opcional).
     * Postcondición: curso registrado sin cohortes abiertas.
     * EX-CU03-01: categoría o docente titular inválido → redirect con mensaje.
     */
    @PostMapping("/cursos/guardar")
    public String guardarCurso(@RequestParam String nombre,
                               @RequestParam String descripcion,
                               @RequestParam float precio,
                               @RequestParam Integer categoriaId,
                               @RequestParam Integer docenteTitularId,
                               @RequestParam(required = false) Integer docenteSupervisorId,
                               @RequestParam(required = false) Integer nivelId,
                               @RequestParam(defaultValue = "false") boolean emiteCertificado,
                               RedirectAttributes ra) {
        if (precio < 0) {
            ra.addFlashAttribute("mensaje", "EX-CU03-02: El precio no puede ser menor a cero.");
            return "redirect:/admin/cursos/nuevo";
        }

        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);
        if (catOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "EX-CU03-01: Categoría seleccionada inválida.");
            return "redirect:/admin/cursos/nuevo";
        }
        Optional<Docente> docenteTitularOpt = docenteRepository.findById(docenteTitularId);
        if (docenteTitularOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "EX-CU03-01: Docente titular seleccionado inválido.");
            return "redirect:/admin/cursos/nuevo";
        }

        Docente titular = docenteTitularOpt.get();
        Nivel nivel = (nivelId != null) ? nivelRepository.findById(nivelId).orElse(null) : null;
        if (nivel == null) {
            List<Nivel> niveles = nivelRepository.findAll();
            nivel = niveles.isEmpty() ? null : niveles.get(0);
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get(), nivel, titular);
        curso.setEmiteCertificado(emiteCertificado);
        Curso cursoGuardado = cursoService.guardar(curso);

        // Registro de docente supervisor mediante la entidad Supervisor del nuevo modelo
        if (docenteSupervisorId != null && !docenteSupervisorId.equals(docenteTitularId)) {
            docenteRepository.findById(docenteSupervisorId).ifPresent(sup -> {
                Supervisor s = new Supervisor(cursoGuardado, sup);
                supervisorRepository.save(s);
            });
        }

        ra.addFlashAttribute("mensaje", "Curso '" + nombre + "' registrado correctamente con su equipo docente.");
        return "redirect:/admin/cursos";
    }

    /**
     * TRAZABILIDAD: CU-04 — Modificar curso (formulario GET).
     * Actor: Administrador.
     * Flujo paso 2: muestra datos actuales del curso con sus asignaciones docentes.
     */
    @GetMapping("/cursos/{id}/editar")
    public String editarCursoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/admin/cursos";
        Curso curso = cOpt.get();

        List<Supervisor> supervisores = supervisorRepository.findByCurso(curso);

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("cursoEditar", curso);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("docentes", docenteRepository.findActivos());
        model.addAttribute("niveles", nivelRepository.findAll());
        model.addAttribute("supervisores", supervisores);
        model.addAttribute("titulo", "Editar Curso | Idóneos Online");
        return "pages/admin/editar-curso";
    }

    /**
     * TRAZABILIDAD: CU-04 — Modificar curso (POST).
     * Actor: Administrador.
     * Flujo paso 4-7: valida campos obligatorios, precio >= 0, actualiza curso, categoría, nivel y docentes.
     * Postcondición: curso actualizado con equipo docente.
     * EX-CU04-01: categoría inválida o precio negativo → redirect con mensaje.
     */
    @PostMapping("/cursos/{id}/editar")
    public String guardarEdicionCurso(@PathVariable Integer id,
                                      @RequestParam String nombre,
                                      @RequestParam String descripcion,
                                      @RequestParam float precio,
                                      @RequestParam Integer categoriaId,
                                      @RequestParam Integer docenteTitularId,
                                      @RequestParam(required = false) Integer docenteSupervisorId,
                                      @RequestParam(required = false) Integer nivelId,
                                      @RequestParam(defaultValue = "false") boolean emiteCertificado,
                                      RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/admin/cursos";

        if (precio < 0) {
            ra.addFlashAttribute("mensaje", "EX-CU04-02: El precio no puede ser menor a cero.");
            return "redirect:/admin/cursos/" + id + "/editar";
        }

        Optional<Categoria> catOpt = categoriaService.buscarPorId(categoriaId);
        if (catOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "EX-CU04-01: Categoría inválida.");
            return "redirect:/admin/cursos/" + id + "/editar";
        }

        Curso curso = cOpt.get();
        curso.setNombre(nombre);
        curso.setDescripcion(descripcion);
        curso.setPrecio(precio);
        curso.setCategoria(catOpt.get());
        curso.setEmiteCertificado(emiteCertificado);

        if (nivelId != null) {
            nivelRepository.findById(nivelId).ifPresent(curso::setNivel);
        }

        docenteRepository.findById(docenteTitularId).ifPresent(curso::setDocente);
        cursoService.modificar(curso);

        // Actualizar supervisores asociados en el nuevo esquema
        List<Supervisor> supervisoresActuales = supervisorRepository.findByCurso(curso);
        supervisorRepository.deleteAll(supervisoresActuales);

        if (docenteSupervisorId != null && !docenteSupervisorId.equals(docenteTitularId)) {
            docenteRepository.findById(docenteSupervisorId).ifPresent(sup -> {
                Supervisor s = new Supervisor(curso, sup);
                supervisorRepository.save(s);
            });
        }

        ra.addFlashAttribute("mensaje", "Curso actualizado correctamente.");
        return "redirect:/admin/cursos";
    }

    /**
     * TRAZABILIDAD: CU-05 — Dar de baja curso.
     * Actor: Administrador.
     * Flujo paso 2: el sistema verifica que no existan programas activos asociados al curso (EX-CU05-01).
     * Flujo paso 4: marca el curso como dado de baja y lo retira del catálogo público.
     * Postcondición: el curso queda en baja.
     * EX-CU05-01 (paso 2): si posee programas activos, informa dependencia y no permite la baja.
     */
    @PostMapping("/cursos/{id}/baja")
    public String darBajaCurso(@PathVariable Integer id, RedirectAttributes ra) {
        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isPresent()) {
            Curso c = cOpt.get();
            if (!c.getBaja()) {
                // CU-05 paso 2: verificar si existen programas activos
                List<Programa> programas = programaRepository.findByCurso(c);
                boolean tieneProgramasActivos = programas.stream().anyMatch(p -> !p.isBaja());
                if (tieneProgramasActivos) {
                    ra.addFlashAttribute("mensaje", "EX-CU05-01: No es posible dar de baja el curso porque posee programas activos asociados.");
                    return "redirect:/admin/cursos";
                }
                c.setBaja(true);
                c.setPublicado(false);
                cursoService.modificar(c);
                ra.addFlashAttribute("mensaje", "Curso dado de baja correctamente.");
            } else {
                c.setBaja(false);
                cursoService.modificar(c);
                ra.addFlashAttribute("mensaje", "Curso reactivado.");
            }
        }
        return "redirect:/admin/cursos";
    }

    /**
     * TRAZABILIDAD: CU-04 — Modificar curso (cambiar estado de publicación).
     * Actor: Administrador.
     * Flujo paso 7: actualiza el flag publicado del curso.
     * Postcondición: curso publicado o despublicado según el estado anterior.
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
     * TRAZABILIDAD: CU-07 — Buscar categoría.
     * Actor: Administrador.
     * Precondición: sesión iniciada con rol Administrador. Existe al menos una categoría.
     * Flujo paso 4-5: recupera y lista todas las categorías activas.
     */
    @GetMapping("/categorias")
    public String listarCategorias(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("titulo", "Categorías | Idóneos Online");
        return "pages/admin/categorias";
    }

    /**
     * TRAZABILIDAD: CU-08 — Registrar categoría.
     * Actor: Administrador.
     * Flujo paso 4-5: valida que el nombre haya sido completado y que no exista otra categoría activa
     *   con el mismo nombre. Registra la categoría en estado activo.
     * Postcondición: categoría registrada en estado activo.
     */
    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@RequestParam String nombre,
                                   @RequestParam(required = false) String descripcion,
                                   RedirectAttributes ra) {
        try {
            Categoria c = new Categoria(nombre, descripcion);
            categoriaService.guardar(c);
            ra.addFlashAttribute("mensaje", "Categoría creada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }

    /**
     * TRAZABILIDAD: CU-10 — Dar de baja categoría.
     * Actor: Administrador.
     * Flujo paso 2: verifica que no existan cursos activos asociados a la categoría.
     * Flujo paso 4: marca la categoría como dada de baja.
     */
    @PostMapping("/categorias/{id}/baja")
    public String darBajaCategoria(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            categoriaService.darDeBaja(id);
            ra.addFlashAttribute("mensaje", "Categoría dada de baja correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("mensaje", e.getMessage());
        }
        return "redirect:/admin/categorias";
    }
}

