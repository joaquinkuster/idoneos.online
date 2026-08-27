package com.app.idoneos.controller.modulo_gestion_academica;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * TRAZABILIDAD — Controller para el Módulo de Gestión Académica (MOD-F-02).
 *
 * Mapea y conecta directamente las 25 pantallas académicas:
 *   CU-15 a CU-38
 */
@Controller
@RequestMapping("/academico")
public class AcademicoController {

    @Autowired private ProgramaService programaService;
    @Autowired private CursoService cursoService;
    @Autowired private UnidadService unidadService;
    @Autowired private MaterialService materialService;
    @Autowired private GlosarioService glosarioService;
    @Autowired private ForoService foroService;
    @Autowired private InscripcionService inscripcionService;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private DocenteRepository docenteRepository;

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-15 a CU-18: PROGRAMAS ACADÉMICOS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/programas")
    public String buscarProgramas(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                  @RequestParam(value = "busqueda", required = false) String busqueda,
                                  Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("programas", programaService.buscarProgramasConFiltros(cursoId, busqueda, false));
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("cursoSeleccionado", cursoId);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("titulo", "CU-15 - Buscar programa | Idóneos Online");
        return "pages/academico/cu-15-buscar-programa";
    }

    @GetMapping("/programas/nuevo")
    public String registrarProgramaForm(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                        Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("titulo", "CU-16 - Registrar programa | Idóneos Online");
        return "pages/academico/cu-16-registrar-programa";
    }

    @PostMapping("/programas/guardar")
    public String guardarPrograma(@RequestParam Integer cursoId,
                                  @RequestParam String nombre,
                                  @RequestParam(required = false) String descripcion,
                                  RedirectAttributes ra) {
        try {
            programaService.registrarPrograma(cursoId, nombre, descripcion, "1.0");
            ra.addFlashAttribute("mensaje", "Programa registrado con éxito.");
            return "redirect:/academico/programas?cursoId=" + cursoId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/programas/nuevo?cursoId=" + cursoId;
        }
    }

    @GetMapping("/programas/{id}/editar")
    public String modificarProgramaForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Programa> pOpt = programaService.buscarPorId(id);
        if (pOpt.isEmpty()) return "redirect:/academico/programas";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("programa", pOpt.get());
        model.addAttribute("titulo", "CU-17 - Modificar programa | Idóneos Online");
        return "pages/academico/cu-17-modificar-programa";
    }

    @PostMapping("/programas/{id}/editar")
    public String actualizarPrograma(@PathVariable Integer id,
                                     @RequestParam String nombre,
                                     @RequestParam(required = false) String descripcion,
                                     RedirectAttributes ra) {
        try {
            Programa p = programaService.modificarPrograma(id, nombre, descripcion, "1.0");
            ra.addFlashAttribute("mensaje", "Programa modificado con éxito.");
            return "redirect:/academico/programas?cursoId=" + p.getCurso().getIdCurso();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/programas/" + id + "/editar";
        }
    }

    @GetMapping("/programas/{id}/baja")
    public String darDeBajaProgramaView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Programa> pOpt = programaService.buscarPorId(id);
        if (pOpt.isEmpty()) return "redirect:/academico/programas";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("programa", pOpt.get());
        model.addAttribute("titulo", "CU-18 - Dar de baja programa | Idóneos Online");
        return "pages/academico/cu-18-dar-de-baja-programa";
    }

    @PostMapping("/programas/{id}/baja")
    public String eliminarPrograma(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Programa p = programaService.buscarPorId(id).orElse(null);
            Integer cId = (p != null && p.getCurso() != null) ? p.getCurso().getIdCurso() : null;
            programaService.darDeBajaPrograma(id);
            ra.addFlashAttribute("mensaje", "Programa dado de baja correctamente.");
            return cId != null ? "redirect:/academico/programas?cursoId=" + cId : "redirect:/academico/programas";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/programas/" + id + "/baja";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-19 a CU-22: UNIDADES TEMÁTICAS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/unidades")
    public String buscarUnidades(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                 Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Curso> cursos = cursoService.obtenerTodo();
        Curso curso = (cursoId != null) ? cursoService.buscarPorId(cursoId).orElse(null) : (cursos.isEmpty() ? null : cursos.get(0));

        List<Unidad> unidades = (curso != null) ? unidadService.obtenerPorCurso(curso) : List.of();
        model.addAttribute("cursos", cursos);
        model.addAttribute("cursoSeleccionado", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("titulo", "CU-19 - Buscar unidad | Idóneos Online");
        return "pages/academico/cu-19-buscar-unidad";
    }

    @GetMapping("/unidades/nueva")
    public String agregarUnidadForm(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                    Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("titulo", "CU-20 - Agregar unidad | Idóneos Online");
        return "pages/academico/cu-20-agregar-unidad";
    }

    @PostMapping("/unidades/guardar")
    public String guardarUnidad(@RequestParam Integer cursoId,
                                @RequestParam String titulo,
                                @RequestParam(required = false) String descripcion,
                                @RequestParam(required = false) String contenido,
                                RedirectAttributes ra) {
        try {
            Unidad u = new Unidad(titulo, descripcion, (contenido != null && !contenido.isBlank()) ? contenido : "Contenido de la unidad");
            unidadService.guardar(u);
            ra.addFlashAttribute("mensaje", "Unidad agregada correctamente.");
            return "redirect:/academico/unidades?cursoId=" + cursoId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/unidades/nueva?cursoId=" + cursoId;
        }
    }

    @GetMapping("/unidades/{id}/editar")
    public String modificarUnidadForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(id);
        if (uOpt.isEmpty()) return "redirect:/academico/unidades";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidad", uOpt.get());
        model.addAttribute("titulo", "CU-21 - Modificar unidad | Idóneos Online");
        return "pages/academico/cu-21-modificar-unidad";
    }

    @PostMapping("/unidades/{id}/editar")
    public String actualizarUnidad(@PathVariable Integer id,
                                   @RequestParam String titulo,
                                   @RequestParam(required = false) String descripcion,
                                   @RequestParam(required = false) String contenido,
                                   RedirectAttributes ra) {
        try {
            Unidad u = unidadService.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            u.setTitulo(titulo);
            u.setDescripcion(descripcion);
            if (contenido != null && !contenido.isBlank()) {
                u.setContenido(contenido);
            }
            unidadService.modificar(u);
            ra.addFlashAttribute("mensaje", "Unidad actualizada con éxito.");
            Integer cId = (u.getCurso() != null) ? u.getCurso().getIdCurso() : null;
            return cId != null ? "redirect:/academico/unidades?cursoId=" + cId : "redirect:/academico/unidades";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/unidades/" + id + "/editar";
        }
    }

    @GetMapping("/unidades/{id}/quitar")
    public String quitarUnidadView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Unidad> uOpt = unidadService.buscarPorId(id);
        if (uOpt.isEmpty()) return "redirect:/academico/unidades";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidad", uOpt.get());
        model.addAttribute("titulo", "CU-22 - Quitar unidad | Idóneos Online");
        return "pages/academico/cu-22-quitar-unidad";
    }

    @PostMapping("/unidades/{id}/quitar")
    public String eliminarUnidad(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Unidad u = unidadService.buscarPorId(id).orElse(null);
            Integer cId = (u != null && u.getCurso() != null) ? u.getCurso().getIdCurso() : null;
            unidadService.darDeBaja(id);
            ra.addFlashAttribute("mensaje", "Unidad eliminada con éxito.");
            return cId != null ? "redirect:/academico/unidades?cursoId=" + cId : "redirect:/academico/unidades";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/unidades/" + id + "/quitar";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-23, CU-24, CU-25: CRONOGRAMA Y PARTICIPANTES
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/cronogramas")
    public String buscarCronograma(@RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Cohorte> cohortes = cohorteRepository.findAll();
        Cohorte cohorte = (cohorteId != null) ? cohorteRepository.findById(cohorteId).orElse(null) : (cohortes.isEmpty() ? null : cohortes.get(0));

        model.addAttribute("cohortes", cohortes);
        model.addAttribute("cohorteSeleccionada", cohorte);
        if (cohorte != null && cohorte.getPrograma() != null && cohorte.getPrograma().getCurso() != null) {
            model.addAttribute("unidades", unidadService.obtenerPorCurso(cohorte.getPrograma().getCurso()));
        }
        model.addAttribute("titulo", "CU-23 - Buscar cronograma | Idóneos Online");
        return "pages/academico/cu-23-buscar-cronograma";
    }

    @GetMapping("/cronogramas/{id}/editar")
    public String modificarCronogramaForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Cohorte> cOpt = cohorteRepository.findById(id);
        if (cOpt.isEmpty()) return "redirect:/academico/cronogramas";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cohorte", cOpt.get());
        model.addAttribute("titulo", "CU-24 - Modificar cronograma | Idóneos Online");
        return "pages/academico/cu-24-modificar-cronograma";
    }

    @GetMapping("/participantes")
    public String verParticipantes(@RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Cohorte> cohortes = cohorteRepository.findAll();
        Cohorte cohorte = (cohorteId != null) ? cohorteRepository.findById(cohorteId).orElse(null) : (cohortes.isEmpty() ? null : cohortes.get(0));

        List<Inscripcion> inscripciones = (cohorte != null) ? inscripcionService.obtenerPorCohorte(cohorte) : List.of();
        model.addAttribute("cohortes", cohortes);
        model.addAttribute("cohorteSeleccionada", cohorte);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("titulo", "CU-25 - Ver participantes | Idóneos Online");
        return "pages/academico/cu-25-ver-participantes";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-26 & CU-26b: AULA VIRTUAL Y MODO EDICIÓN
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/curso/{id}/aula")
    public String accederCurso(@PathVariable Integer id, Model model, Authentication auth, RedirectAttributes ra) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";
        Usuario usuario = (Usuario) auth.getPrincipal();

        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cOpt.get();
        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("titulo", "CU-26 - Aula Virtual: " + curso.getNombre() + " | Idóneos Online");
        return "pages/academico/cu-26-acceder-curso";
    }

    @GetMapping("/curso/{id}/edicion")
    public String accederCursoModoEdicion(@PathVariable Integer id, Model model, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";
        Usuario usuario = (Usuario) auth.getPrincipal();

        Optional<Curso> cOpt = cursoService.buscarPorId(id);
        if (cOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cOpt.get();
        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("unidades", unidades);
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "CU-26b - Modo Edición: " + curso.getNombre() + " | Idóneos Online");
        return "pages/academico/cu-26b-acceder-curso-modo-edicion-docente-administrador";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-27 a CU-30: MATERIALES EDUCATIVOS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/materiales")
    public String buscarMateriales(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        List<Material> materiales = (unidad != null) ? materialService.obtenerPorUnidad(unidad) : List.of();
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("materiales", materiales);
        model.addAttribute("titulo", "CU-27 - Buscar material | Idóneos Online");
        return "pages/academico/cu-27-buscar-material";
    }

    @GetMapping("/materiales/nuevo")
    public String subirMaterialForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                    Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-28 - Subir material | Idóneos Online");
        return "pages/academico/cu-28-subir-material";
    }

    @PostMapping("/materiales/guardar")
    public String guardarMaterial(@RequestParam Integer unidadId,
                                  @RequestParam String nombre,
                                  @RequestParam(required = false) String descripcion,
                                  @RequestParam String url,
                                  @RequestParam Integer tipoMaterialId,
                                  Authentication auth,
                                  RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad inválida"));
            TipoMaterial tipo = tipoMaterialRepository.findById(tipoMaterialId).orElse(null);
            Docente docente = null;
            if (auth != null && auth.getPrincipal() instanceof Usuario) {
                Usuario u = (Usuario) auth.getPrincipal();
                docente = docenteRepository.findById(u.getId()).orElse(null);
            }
            if (docente == null) {
                List<Docente> docentes = docenteRepository.findAll();
                docente = docentes.isEmpty() ? null : docentes.get(0);
            }

            Material m = new Material(nombre, docente, tipo, unidad);
            m.setRutaArchivo(url);
            m.setContenido(descripcion);
            materialService.guardar(m);
            ra.addFlashAttribute("mensaje", "Material subido exitosamente.");
            return "redirect:/academico/materiales?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/materiales/nuevo?unidadId=" + unidadId;
        }
    }

    @GetMapping("/materiales/{id}/editar")
    public String modificarMaterialForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Material> mOpt = materialService.buscarPorId(id);
        if (mOpt.isEmpty()) return "redirect:/academico/materiales";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("material", mOpt.get());
        model.addAttribute("tiposMaterial", tipoMaterialRepository.findAll());
        model.addAttribute("titulo", "CU-29 - Modificar material | Idóneos Online");
        return "pages/academico/cu-29-modificar-material";
    }

    @PostMapping("/materiales/{id}/editar")
    public String actualizarMaterial(@PathVariable Integer id,
                                     @RequestParam String nombre,
                                     @RequestParam(required = false) String descripcion,
                                     @RequestParam String url,
                                     RedirectAttributes ra) {
        try {
            Material m = materialService.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Material no encontrado"));
            m.setTitulo(nombre);
            m.setContenido(descripcion);
            m.setRutaArchivo(url);
            materialService.modificar(m);
            ra.addFlashAttribute("mensaje", "Material actualizado con éxito.");
            return "redirect:/academico/materiales?unidadId=" + m.getUnidad().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/materiales/" + id + "/editar";
        }
    }

    @GetMapping("/materiales/{id}/baja")
    public String darDeBajaMaterialView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Material> mOpt = materialService.buscarPorId(id);
        if (mOpt.isEmpty()) return "redirect:/academico/materiales";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("material", mOpt.get());
        model.addAttribute("titulo", "CU-30 - Dar de baja material | Idóneos Online");
        return "pages/academico/cu-30-dar-de-baja-material";
    }

    @PostMapping("/materiales/{id}/baja")
    public String eliminarMaterial(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Material m = materialService.buscarPorId(id).orElse(null);
            Integer uId = (m != null && m.getUnidad() != null) ? m.getUnidad().getId() : null;
            materialService.darDeBaja(id);
            ra.addFlashAttribute("mensaje", "Material dado de baja correctamente.");
            return uId != null ? "redirect:/academico/materiales?unidadId=" + uId : "redirect:/academico/materiales";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/materiales/" + id + "/baja";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-31 a CU-34: GLOSARIO
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/glosario")
    public String buscarGlosario(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                 Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        List<TerminoGlosario> terminos = (unidad != null) ? glosarioService.obtenerPorUnidad(unidad) : List.of();
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("terminos", terminos);
        model.addAttribute("titulo", "CU-31 - Buscar término de glosario | Idóneos Online");
        return "pages/academico/cu-31-buscar-termino-de-glosario";
    }

    @GetMapping("/glosario/nuevo")
    public String registrarTerminoForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                       Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-32 - Registrar término de glosario | Idóneos Online");
        return "pages/academico/cu-32-registrar-termino-de-glosario";
    }

    @PostMapping("/glosario/guardar")
    public String guardarTermino(@RequestParam Integer unidadId,
                                 @RequestParam String termino,
                                 @RequestParam String definicion,
                                 RedirectAttributes ra) {
        try {
            Unidad u = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            glosarioService.registrarTermino(new TerminoGlosario(termino, definicion, u));
            ra.addFlashAttribute("mensaje", "Término registrado con éxito.");
            return "redirect:/academico/glosario?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/glosario/nuevo?unidadId=" + unidadId;
        }
    }

    @GetMapping("/glosario/{id}/editar")
    public String modificarTerminoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<TerminoGlosario> tOpt = glosarioService.buscarPorId(id);
        if (tOpt.isEmpty()) return "redirect:/academico/glosario";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("termino", tOpt.get());
        model.addAttribute("titulo", "CU-33 - Modificar término de glosario | Idóneos Online");
        return "pages/academico/cu-33-modificar-termino-de-glosario";
    }

    @PostMapping("/glosario/{id}/editar")
    public String actualizarTermino(@PathVariable Integer id,
                                    @RequestParam String termino,
                                    @RequestParam String definicion,
                                    RedirectAttributes ra) {
        try {
            TerminoGlosario tg = glosarioService.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Término no encontrado"));
            tg.setTermino(termino);
            tg.setDefinicion(definicion);
            glosarioService.modificarTermino(tg);
            ra.addFlashAttribute("mensaje", "Término actualizado correctamente.");
            return "redirect:/academico/glosario?unidadId=" + tg.getUnidad().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/glosario/" + id + "/editar";
        }
    }

    @GetMapping("/glosario/{id}/baja")
    public String darDeBajaTerminoView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<TerminoGlosario> tOpt = glosarioService.buscarPorId(id);
        if (tOpt.isEmpty()) return "redirect:/academico/glosario";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("termino", tOpt.get());
        model.addAttribute("titulo", "CU-34 - Dar de baja término de glosario | Idóneos Online");
        return "pages/academico/cu-34-dar-de-baja-termino-de-glosario";
    }

    @PostMapping("/glosario/{id}/baja")
    public String eliminarTermino(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            TerminoGlosario tg = glosarioService.buscarPorId(id).orElse(null);
            Integer uId = (tg != null && tg.getUnidad() != null) ? tg.getUnidad().getId() : null;
            glosarioService.darDeBaja(id);
            ra.addFlashAttribute("mensaje", "Término dado de baja con éxito.");
            return uId != null ? "redirect:/academico/glosario?unidadId=" + uId : "redirect:/academico/glosario";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/glosario/" + id + "/baja";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-35 a CU-38: FORO DE CONSULTAS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/foro")
    public String buscarForo(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                             Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        List<ConsultaForo> consultas = (unidad != null) ? foroService.obtenerConsultasPorUnidad(unidad) : List.of();
        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("consultas", consultas);
        model.addAttribute("titulo", "CU-35 - Buscar consulta de foro | Idóneos Online");
        return "pages/academico/cu-35-buscar-consulta-de-foro";
    }

    @GetMapping("/foro/nueva")
    public String registrarConsultaForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                        Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-36 - Registrar consulta de foro | Idóneos Online");
        return "pages/academico/cu-36-registrar-consulta-de-foro";
    }

    @PostMapping("/foro/guardar")
    public String guardarConsulta(@RequestParam Integer unidadId,
                                  @RequestParam String texto,
                                  Authentication auth, RedirectAttributes ra) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";
        Usuario u = (Usuario) auth.getPrincipal();
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            foroService.crearConsulta(texto, u, unidad);
            ra.addFlashAttribute("mensaje", "Consulta publicada en el foro.");
            return "redirect:/academico/foro?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/foro/nueva?unidadId=" + unidadId;
        }
    }

    @GetMapping("/foro/{id}/editar")
    public String modificarConsultaForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ConsultaForo> cOpt = foroService.buscarConsultaPorId(id);
        if (cOpt.isEmpty()) return "redirect:/academico/foro";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("consulta", cOpt.get());
        model.addAttribute("titulo", "CU-37 - Modificar consulta de foro | Idóneos Online");
        return "pages/academico/cu-37-modificar-consulta-de-foro";
    }

    @PostMapping("/foro/{id}/editar")
    public String actualizarConsulta(@PathVariable Integer id,
                                     @RequestParam String texto,
                                     RedirectAttributes ra) {
        try {
            ConsultaForo c = foroService.buscarConsultaPorId(id).orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));
            c.setTexto(texto);
            foroService.modificarConsulta(c);
            ra.addFlashAttribute("mensaje", "Consulta modificada con éxito.");
            return "redirect:/academico/foro?unidadId=" + c.getUnidad().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/foro/" + id + "/editar";
        }
    }

    @GetMapping("/foro/{id}/baja")
    public String darDeBajaConsultaView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ConsultaForo> cOpt = foroService.buscarConsultaPorId(id);
        if (cOpt.isEmpty()) return "redirect:/academico/foro";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("consulta", cOpt.get());
        model.addAttribute("titulo", "CU-38 - Dar de baja consulta de foro | Idóneos Online");
        return "pages/academico/cu-38-dar-de-baja-consulta-de-foro";
    }

    @PostMapping("/foro/{id}/baja")
    public String eliminarConsulta(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            ConsultaForo c = foroService.buscarConsultaPorId(id).orElse(null);
            Integer uId = (c != null && c.getUnidad() != null) ? c.getUnidad().getId() : null;
            foroService.darDeBajaConsulta(id);
            ra.addFlashAttribute("mensaje", "Consulta dada de baja correctamente.");
            return uId != null ? "redirect:/academico/foro?unidadId=" + uId : "redirect:/academico/foro";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/academico/foro/" + id + "/baja";
        }
    }
}
