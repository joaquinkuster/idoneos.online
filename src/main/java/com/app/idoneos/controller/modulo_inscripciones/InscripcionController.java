package com.app.idoneos.controller.modulo_inscripciones;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * TRAZABILIDAD — Controller para el Módulo de Inscripciones y Pagos (MOD-F-03).
 *
 * Mapea y conecta directamente las 10 pantallas de Inscripciones:
 *   CU-43 — Buscar inscripción               → GET /inscripciones
 *   CU-44 — Inscribir curso                  → GET /inscripciones/nueva, POST /inscripciones/guardar
 *   CU-45 — Dar de baja inscripción          → GET/POST /inscripciones/{id}/baja
 *   CU-46 — Buscar pago                      → GET /inscripciones/pagos
 *   CU-47 — Realizar pago                    → GET /inscripciones/pagos/nuevo, POST /inscripciones/pagos/procesar
 *   CU-48 — Buscar progreso                  → GET /inscripciones/progreso
 *   CU-49 — Buscar descuento                 → GET /inscripciones/descuentos
 *   CU-50 — Registrar descuento              → GET /inscripciones/descuentos/nuevo, POST /inscripciones/descuentos/guardar
 *   CU-51 — Modificar descuento              → GET /inscripciones/descuentos/{id}/editar, POST /inscripciones/descuentos/{id}/editar
 *   CU-52 — Dar de baja descuento            → GET/POST /inscripciones/descuentos/{id}/baja
 */
@Controller
@RequestMapping("/inscripciones")
public class InscripcionController {

    @Autowired private InscripcionService inscripcionService;
    @Autowired private PagoService pagoService;
    @Autowired private ProgresoService progresoService;
    @Autowired private DescuentoService descuentoService;
    @Autowired private CursoService cursoService;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private PagoRepository pagoRepository;

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-43, CU-44, CU-45: INSCRIPCIONES
    // ─────────────────────────────────────────────────────────────

    @GetMapping
    public String buscarInscripciones(@RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                      Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Cohorte> cohortes = cohorteRepository.findAll();
        Cohorte cohorte = (cohorteId != null) ? cohorteRepository.findById(cohorteId).orElse(null) : (cohortes.isEmpty() ? null : cohortes.get(0));

        List<Inscripcion> inscripciones = (cohorte != null) ? inscripcionService.obtenerPorCohorte(cohorte) : inscripcionService.obtenerTodo();
        model.addAttribute("cohortes", cohortes);
        model.addAttribute("cohorteSeleccionada", cohorte);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("titulo", "CU-43 - Buscar inscripción | Idóneos Online");
        return "pages/inscripciones/cu-43-buscar-inscripcion";
    }

    @GetMapping("/nueva")
    public String inscribirCursoForm(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                     Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cursos", cursoService.obtenerPublicados());
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("titulo", "CU-44 - Inscribir curso | Idóneos Online");
        return "pages/inscripciones/cu-44-inscribir-curso";
    }

    @PostMapping("/guardar")
    public String inscribirCurso(@RequestParam Integer cursoId,
                                 @RequestParam(required = false) Integer cohorteId,
                                 Authentication auth, RedirectAttributes ra) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";
        Usuario usuario = (Usuario) auth.getPrincipal();
        try {
            Curso curso = cursoService.buscarPorId(cursoId).orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
            if (cohorteId != null) {
                Cohorte cohorte = cohorteRepository.findById(cohorteId).orElse(null);
                if (cohorte != null) {
                    inscripcionService.inscribirAlumnoACohorte(usuario, cohorte);
                } else {
                    inscripcionService.inscribirAlumno(usuario, curso);
                }
            } else {
                inscripcionService.inscribirAlumno(usuario, curso);
            }
            ra.addFlashAttribute("mensaje", "¡Inscripción realizada con éxito!");
            return "redirect:/cursos/" + cursoId + "/mi-cursada";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripciones/nueva?cursoId=" + cursoId;
        }
    }

    @GetMapping("/{id}/baja")
    public String darDeBajaInscripcionView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Inscripcion> iOpt = inscripcionService.buscarPorId(id);
        if (iOpt.isEmpty()) return "redirect:/inscripciones";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("inscripcion", iOpt.get());
        model.addAttribute("titulo", "CU-45 - Dar de baja inscripción | Idóneos Online");
        return "pages/inscripciones/cu-45-dar-de-baja-inscripcion";
    }

    @PostMapping("/{id}/baja")
    public String eliminarInscripcion(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            inscripcionService.darDeBajaInscripcion(id);
            ra.addFlashAttribute("mensaje", "Inscripción dada de baja correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inscripciones";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-46, CU-47, CU-48: PAGOS Y PROGRESO
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/pagos")
    public String buscarPagos(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("pagos", pagoRepository.findAll());
        model.addAttribute("titulo", "CU-46 - Buscar pago | Idóneos Online");
        return "pages/inscripciones/cu-46-buscar-pago";
    }

    @GetMapping({"/pagos/nuevo", "/pagar"})
    public String realizarPagoForm(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        Curso curso = (cursoId != null) ? cursoService.buscarPorId(cursoId).orElse(null) : null;
        model.addAttribute("curso", curso);
        model.addAttribute("cursos", cursoService.obtenerPublicados());
        model.addAttribute("titulo", "CU-47 - Realizar pago | Idóneos Online");
        return "pages/inscripciones/cu-47-realizar-pago";
    }

    @GetMapping("/progreso")
    public String buscarProgreso(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            Usuario u = (Usuario) auth.getPrincipal();
            model.addAttribute("inscripciones", inscripcionService.obtenerPorAlumno(u));
        }
        model.addAttribute("titulo", "CU-48 - Buscar progreso | Idóneos Online");
        return "pages/inscripciones/cu-48-buscar-progreso";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-49 a CU-52: DESCUENTOS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/descuentos")
    public String buscarDescuentos(@RequestParam(value = "codigo", required = false) String codigo,
                                   @RequestParam(value = "vigente", required = false) Boolean vigente,
                                   Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("descuentos", descuentoService.buscarDescuentosConFiltros(codigo, vigente));
        model.addAttribute("codigoBusqueda", codigo);
        model.addAttribute("titulo", "CU-49 - Buscar descuento | Idóneos Online");
        return "pages/inscripciones/cu-49-buscar-descuento";
    }

    @GetMapping("/descuentos/nuevo")
    public String registrarDescuentoForm(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("titulo", "CU-50 - Registrar descuento | Idóneos Online");
        return "pages/inscripciones/cu-50-registrar-descuento";
    }

    @PostMapping("/descuentos/guardar")
    public String guardarDescuento(@RequestParam String codigo,
                                   @RequestParam float porcentaje,
                                   @RequestParam String fechaInicio,
                                   @RequestParam String fechaFin,
                                   @RequestParam(required = false) Integer cursoId,
                                   RedirectAttributes ra) {
        try {
            descuentoService.registrarDescuento(codigo, porcentaje, fechaInicio, fechaFin, cursoId);
            ra.addFlashAttribute("mensaje", "Descuento '" + codigo + "' registrado con éxito.");
            return "redirect:/inscripciones/descuentos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripciones/descuentos/nuevo";
        }
    }

    @GetMapping("/descuentos/{id}/editar")
    public String modificarDescuentoForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Descuento> dOpt = descuentoService.buscarPorId(id);
        if (dOpt.isEmpty()) return "redirect:/inscripciones/descuentos";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("descuento", dOpt.get());
        model.addAttribute("titulo", "CU-51 - Modificar descuento | Idóneos Online");
        return "pages/inscripciones/cu-51-modificar-descuento";
    }

    @PostMapping("/descuentos/{id}/editar")
    public String actualizarDescuento(@PathVariable Integer id,
                                      @RequestParam String codigo,
                                      @RequestParam float porcentaje,
                                      @RequestParam String fechaInicio,
                                      @RequestParam String fechaFin,
                                      @RequestParam(required = false) Integer cursoId,
                                      RedirectAttributes ra) {
        try {
            descuentoService.modificarDescuento(id, codigo, porcentaje, fechaInicio, fechaFin, cursoId);
            ra.addFlashAttribute("mensaje", "Descuento modificado exitosamente.");
            return "redirect:/inscripciones/descuentos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripciones/descuentos/" + id + "/editar";
        }
    }

    @GetMapping("/descuentos/{id}/baja")
    public String darDeBajaDescuentoView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Descuento> dOpt = descuentoService.buscarPorId(id);
        if (dOpt.isEmpty()) return "redirect:/inscripciones/descuentos";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("descuento", dOpt.get());
        model.addAttribute("titulo", "CU-52 - Dar de baja descuento | Idóneos Online");
        return "pages/inscripciones/cu-52-dar-de-baja-descuento";
    }

    @PostMapping("/descuentos/{id}/baja")
    public String eliminarDescuento(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            descuentoService.darDeBajaDescuento(id);
            ra.addFlashAttribute("mensaje", "Descuento dado de baja correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inscripciones/descuentos";
    }
}
