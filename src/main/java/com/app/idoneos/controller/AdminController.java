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
 *   CU-05 — Dar de baja curso   → POST /admin/cursos/{id}/baja (toggle baja)
 *             Nota parcial: CU-05 exige verificar programas activos antes de la baja;
 *             la implementación actual hace toggle directo sin esa verificación. IMPLEMENTADO PARCIALMENTE.
 *   CU-07 — Buscar categoría    → GET /admin/categorias
 *   CU-08 — Registrar categoría → POST /admin/categorias/guardar
 *             Nota: CU-08 exige validar unicidad de nombre; no implementado aquí. IMPLEMENTADO PARCIALMENTE.
 *   CU-09 — Modificar categoría → no implementado. FALTANTE.
 *   CU-10 — Dar de baja categoría → no implementado. FALTANTE.
 *
 * MOD-NF-01: Módulo de Usuarios y Notificaciones
 *   CU-82 — Buscar usuario      → GET /admin/usuarios
 *   CU-83 — Registrar usuario   → GET/POST /admin/usuarios/nuevo + /admin/usuarios/guardar
 *             Nota: CU-83 requiere también ejecutar CU-88 (Registrar docente) cuando el rol
 *             es Docente; la implementación actual solo crea el Usuario sin los datos del perfil
 *             docente requeridos (biografía, títulos, matrícula). IMPLEMENTADO PARCIALMENTE.
 *   CU-85 — Dar de baja usuario → POST /admin/usuarios/{id}/baja
 *             Regla RN-07 (único admin activo) implementada.
 *             Nota crítica: la verificación de docente con cohortes vigentes usa entidades
 *             obsoletas (DictadoDocente/Dictado) del esquema anterior; debería usar
 *             Supervisor/Cohorte. IMPLEMENTADO CON ENTIDADES OBSOLETAS.
 *   CU-88 — Registrar docente   → no implementado como CU separado. FALTANTE.
 *   CU-89 — Modificar docente   → no implementado. FALTANTE.
 *
 * MOD-NF-02: Módulo de Auditoría
 *   CU-95 — Consultar auditoría → GET /admin (dashboard con indicadores)
 *             Nota: la consulta completa de auditoría está en AuditoriaController. PARCIAL AQUÍ.
 *
 * INCONSISTENCIAS CON EL ESQUEMA ACTUAL (para corrección futura, MODEL NO SE TOCA):
 *   - AdminController inyecta DictadoDocenteRepository y DictadoRepository que corresponden
 *     al esquema anterior (Dictado/DictadoDocente). En el nuevo esquema estas relaciones
 *     se modelan con Cohorte → Cronograma → Supervisor. Requiere refactor del controller.
 *   - AdminController usa RolUsuario (enum) que fue reemplazado por la entidad Rol en el nuevo esquema.
 *
 * Aplica reglas de negocio: RN-07 (Al menos 1 admin activo), RN-11 (Docente titular con cursos publicados).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UsuarioServiceImpl usuarioService;
    @Autowired private CursoServiceImpl cursoService;
    @Autowired private CategoriaServiceImpl categoriaService;
    @Autowired private DocenteRepository docenteRepository;
    // NOTA: DictadoDocenteRepository y DictadoRepository corresponden al esquema anterior.
    // En el nuevo esquema usar SupervisorRepository y CohorteRepository.
    @Autowired private DictadoDocenteRepository dictadoDocenteRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private DictadoRepository dictadoRepository;
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
     * NOTA PARCIAL: la vista no solicita datos del perfil docente (biografía, títulos, matrícula)
     * requeridos por CU-88. Implementado parcialmente.
     */
    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioForm(Model model, Authentication auth) {
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        // NOTA: RolUsuario es un enum del esquema anterior; en el nuevo esquema Rol es una entidad.
        model.addAttribute("roles", RolUsuario.values());
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
     * NOTA PARCIAL: CU-88 requiere datos adicionales del perfil docente no capturados aquí.
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
     * Flujo paso 3: verifica que el docente no tenga cohortes vigentes.
     *   NOTA CRÍTICA: usa DictadoDocenteRepository/DictadoDocente del esquema ANTERIOR.
     *   En el esquema nuevo corresponde verificar con SupervisorRepository y CohorteRepository.
     *   Estado: IMPLEMENTADO CON ENTIDADES OBSOLETAS — requiere refactor.
     * Flujo paso 4: advierte al alumno con inscripciones vigentes (no implementado, ver paso 4 del CU).
     * Postcondición: cuenta en baja, sesiones cerradas (cierre de sesión no implementado).
     * EX-CU85-01 (RN-07): único admin activo → no permite baja.
     * EX-CU85-02 (RN-11): docente con cursos publicados → no permite baja.
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
                        "RN-07: No es posible dar de baja al único administrador activo del sistema.");
                return "redirect:/admin/usuarios";
            }
        }

        // NOTA: la verificación de docente con cursos publicados usa entidades obsoletas (Dictado/DictadoDocente).
        // Requiere refactor para usar Cohorte/Supervisor del nuevo esquema.
        if (u.getRol() == RolUsuario.Docente && !u.getBaja() && u.getDocente() != null) {
            boolean tieneCursosPublicados = dictadoDocenteRepository.findByDocente(u.getDocente()).stream()
                    .anyMatch(dd -> !dd.isEsSupervisor() && dd.getDictado() != null
                            && dd.getDictado().getPrograma() != null
                            && dd.getDictado().getPrograma().getCurso() != null
                            && Boolean.TRUE.equals(dd.getDictado().getPrograma().getCurso().getPublicado()));
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
     * Flujo paso 4-5: recupera y lista todos los cursos (sin filtro; criterios de búsqueda no implementados).
     * NOTA PARCIAL: CU-01 especifica filtros por nombre, categoría, nivel, equipo docente y modalidad.
     * La implementación lista todo sin filtros. IMPLEMENTADO PARCIALMENTE.
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
     * Flujo paso 2: el sistema solicita nombre, descripción, precio, categoría, docente titular/supervisor.
     * NOTA PARCIAL: CU-03 también solicita nivel, si emite certificado y modalidades. No implementado.
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
     * TRAZABILIDAD: CU-03 — Registrar curso (POST).
     * Actor: Administrador.
     * Flujo paso 4-6: valida campos obligatorios (nombre, descripción, precio, categoría, docente),
     *   registra el curso con Programa y Dictado (usando esquema anterior — ver nota).
     * Postcondición: curso registrado con su equipo docente.
     * EX-CU03-01: categoría inválida → redirect con mensaje.
     * EX-CU03-01: docente inválido → redirect con mensaje.
     * NOTA CRÍTICA: utiliza Dictado/DictadoDocente del esquema ANTERIOR.
     *   En el nuevo esquema corresponde crear Programa + Cohorte + Cronograma + Supervisor.
     *   Estado: IMPLEMENTADO CON ENTIDADES OBSOLETAS — requiere refactor.
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
            ra.addFlashAttribute("mensaje", "EX-CU03-01: Categoría seleccionada inválida.");
            return "redirect:/admin/cursos/nuevo";
        }
        Optional<Docente> docenteTitularOpt = docenteRepository.findById(docenteTitularId);
        if (docenteTitularOpt.isEmpty()) {
            ra.addFlashAttribute("mensaje", "EX-CU03-01: Docente titular seleccionado inválido.");
            return "redirect:/admin/cursos/nuevo";
        }

        Curso curso = new Curso(nombre, descripcion, precio, catOpt.get());
        curso.setMesesAcceso(mesesAcceso);
        Curso cursoDef = cursoService.guardar(curso);

        // NOTA: Programa + Dictado + DictadoDocente son del esquema anterior.
        // Nuevo esquema: Programa + Cohorte + Cronograma + Supervisor.
        Programa prog = programaRepository.save(new Programa(nombre, descripcion, mesesAcceso, cursoDef));
        Dictado dictado = dictadoRepository.save(new Dictado(java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMonths(6), 50, prog));

        dictadoDocenteRepository.save(new DictadoDocente(dictado, docenteTitularOpt.get(), false));
        if (docenteSupervisorId != null && !docenteSupervisorId.equals(docenteTitularId)) {
            docenteRepository.findById(docenteSupervisorId).ifPresent(sup ->
                    dictadoDocenteRepository.save(new DictadoDocente(dictado, sup, true)));
        }
        ra.addFlashAttribute("mensaje", "Curso '" + nombre + "' creado correctamente.");
        return "redirect:/admin/cursos";
    }

    /**
     * TRAZABILIDAD: CU-04 — Modificar curso (formulario GET).
     * Actor: Administrador.
     * Flujo paso 2: muestra datos actuales del curso con sus asignaciones docentes.
     * NOTA CRÍTICA: usa DictadoDocente del esquema anterior.
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
     * TRAZABILIDAD: CU-04 — Modificar curso (POST).
     * Actor: Administrador.
     * Flujo paso 4-7: valida campos, actualiza el curso con categoría y docentes.
     * Postcondición: curso actualizado con equipo docente.
     * EX-CU04-01: categoría inválida → redirect con mensaje.
     * NOTA PARCIAL: CU-04 verifica si hay cohortes con inscripción vigente para restringir
     *   qué campos pueden modificarse. Verificación no implementada.
     * NOTA CRÍTICA: usa Dictado/DictadoDocente del esquema anterior.
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
        if (catOpt.isEmpty()) { ra.addFlashAttribute("mensaje", "EX-CU04-01: Categoría inválida."); return "redirect:/admin/cursos/" + id + "/editar"; }

        Curso curso = cOpt.get();
        curso.setNombre(nombre);
        curso.setDescripcion(descripcion);
        curso.setPrecio(precio);
        curso.setCategoria(catOpt.get());
        curso.setMesesAcceso(mesesAcceso);
        cursoService.modificar(curso);

        // NOTA: usa Dictado/DictadoDocente del esquema anterior.
        List<Programa> progs = programaRepository.findByCurso(curso);
        Programa prog = progs.isEmpty() ? programaRepository.save(new Programa(nombre, descripcion, mesesAcceso, curso)) : progs.get(0);
        List<Dictado> dicts = dictadoRepository.findByPrograma(prog);
        Dictado dictado = dicts.isEmpty() ? dictadoRepository.save(new Dictado(java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMonths(6), 50, prog)) : dicts.get(0);

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
     * TRAZABILIDAD: CU-05 — Dar de baja curso.
     * Actor: Administrador.
     * Flujo paso 4: marca el curso como dado de baja (toggle).
     * NOTA PARCIAL: CU-05 exige verificar que no existan programas activos asociados antes de la baja.
     *   La implementación actual hace un toggle directo sin esa verificación. IMPLEMENTADO PARCIALMENTE.
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
     * Flujo paso 4-5: recupera y lista todas las categorías (sin filtro por nombre).
     * NOTA PARCIAL: CU-07 especifica búsqueda por nombre. Lista todo sin filtros. IMPLEMENTADO PARCIALMENTE.
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
     * Flujo paso 4-5: crea y guarda la categoría.
     * Postcondición: categoría registrada en estado activo.
     * NOTA PARCIAL: CU-08 exige validar unicidad de nombre (que no exista otra categoría activa
     *   con el mismo nombre). No implementado aquí. IMPLEMENTADO PARCIALMENTE.
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
