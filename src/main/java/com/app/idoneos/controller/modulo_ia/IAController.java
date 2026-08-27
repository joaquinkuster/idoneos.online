package com.app.idoneos.controller.modulo_ia;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_ia.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * TRAZABILIDAD — Controller para el Módulo de IA (MOD-F-06).
 *
 * Mapea y conecta directamente las 8 pantallas de Inteligencia Artificial y Clonación:
 *   CU-73 a CU-80
 */
@Controller
@RequestMapping("/ia")
public class IAController {

    @Autowired private OllamaService ollamaService;
    @Autowired private UnidadService unidadService;
    @Autowired private ClaseClonIARepository clonRepo;
    @Autowired private EstadoClaseClonIARepository estadoRepo;
    @Autowired private DocenteRepository docenteRepo;
    @Autowired private MaterialRepository materialRepo;
    @Autowired private TipoMaterialRepository tipoMaterialRepo;
    @Autowired private ConfiguracionRepository configRepo;

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-73, CU-74, CU-75: GENERACIÓN DE CONTENIDO ASISTIDO CON IA
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/banco-preguntas")
    public String generarBancoPreguntasView(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                           Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("titulo", "CU-73 - Generar banco de preguntas con IA | Idóneos Online");
        return "pages/ia_vivo/cu-73-generar-banco-de-preguntas";
    }

    @PostMapping("/banco-preguntas/generar")
    public String procesarBancoPreguntas(@RequestParam Integer unidadId,
                                         @RequestParam(required = false) String promptInput,
                                         RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            ollamaService.generarBancoPreguntas(unidad, promptInput);
            ra.addFlashAttribute("mensaje", "¡Banco de preguntas generado exitosamente con IA!");
            return "redirect:/evaluaciones/pools?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ia/banco-preguntas?unidadId=" + unidadId;
        }
    }

    @GetMapping("/resumen-unidad")
    public String generarResumenView(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                    Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("titulo", "CU-74 - Generar resumen de unidad con IA | Idóneos Online");
        return "pages/ia_vivo/cu-74-generar-resumen-de-unidad";
    }

    @PostMapping("/resumen-unidad/generar")
    public String procesarResumen(@RequestParam Integer unidadId, RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            ollamaService.generarResumenUnidad(unidad);
            ra.addFlashAttribute("mensaje", "¡Resumen conceptual de la unidad generado con IA!");
            return "redirect:/academico/materiales?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ia/resumen-unidad?unidadId=" + unidadId;
        }
    }

    @GetMapping("/presentacion-unidad")
    public String generarPresentacionView(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                          Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Unidad> unidades = unidadService.obtenerTodo();
        Unidad unidad = (unidadId != null) ? unidadService.buscarPorId(unidadId).orElse(null) : (unidades.isEmpty() ? null : unidades.get(0));

        model.addAttribute("unidades", unidades);
        model.addAttribute("unidadSeleccionada", unidad);
        model.addAttribute("titulo", "CU-75 - Generar presentación de unidad con IA | Idóneos Online");
        return "pages/ia_vivo/cu-75-generar-presentacion-de-unidad";
    }

    @PostMapping("/presentacion-unidad/generar")
    public String procesarPresentacion(@RequestParam Integer unidadId, RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            ollamaService.generarPresentacionClase(unidad, "Presentación de la unidad: " + unidad.getTitulo());
            ra.addFlashAttribute("mensaje", "¡Presentación estructurada generada con éxito con IA!");
            return "redirect:/academico/materiales?unidadId=" + unidadId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ia/presentacion-unidad?unidadId=" + unidadId;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-76 a CU-80: CLONACIÓN Y CLASES CON CLON IA (HEYGEN)
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/clon/nuevo")
    public String crearClonForm(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("titulo", "CU-76 - Crear clon de voz y avatar IA | Idóneos Online");
        return "pages/ia_vivo/cu-76-crear-clon";
    }

    @PostMapping("/clon/crear")
    public String procesarCrearClon(@RequestParam String nombreAvatar,
                                    Authentication auth, RedirectAttributes ra) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";
        Usuario u = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepo.findById(u.getId()).orElse(null);
        if (docente != null) {
            docente.setAvatarId("avatar_" + UUID.randomUUID().toString().substring(0, 8));
            docente.setFechaAceptacionTycClon(LocalDateTime.now());
            docenteRepo.save(docente);
            ra.addFlashAttribute("mensaje", "¡Consentimiento biométrico registrado y Clon IA generado exitosamente!");
        }
        return "redirect:/ia/clon/clases";
    }

    @GetMapping("/clon/clases")
    public String buscarClasesConClon(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clases", clonRepo.findAll().stream().filter(c -> !c.getBaja()).toList());
        model.addAttribute("titulo", "CU-77 - Buscar clase con clon IA | Idóneos Online");
        return "pages/ia_vivo/cu-77-buscar-clase-con-clon";
    }

    @GetMapping("/clon/clases/nueva")
    public String generarClaseConClonForm(@RequestParam(value = "unidadId", required = false) Integer unidadId,
                                          Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("unidades", unidadService.obtenerTodo());
        model.addAttribute("unidadId", unidadId);
        model.addAttribute("titulo", "CU-78 - Generar clase con clon IA | Idóneos Online");
        return "pages/ia_vivo/cu-78-generar-clase-con-clon";
    }

    @PostMapping("/clon/clases/generar")
    public String procesarGenerarClaseConClon(@RequestParam Integer unidadId,
                                              @RequestParam String titulo,
                                              @RequestParam String guionPrompt,
                                              Authentication auth, RedirectAttributes ra) {
        try {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
            Docente docente = null;
            if (auth != null && auth.getPrincipal() instanceof Usuario) {
                Usuario u = (Usuario) auth.getPrincipal();
                docente = docenteRepo.findById(u.getId()).orElse(null);
            }
            if (docente == null) {
                List<Docente> docentes = docenteRepo.findAll();
                docente = docentes.isEmpty() ? null : docentes.get(0);
            }

            EstadoClaseClonIA estado = estadoRepo.findByNombre("Generada").orElseGet(() -> estadoRepo.findAll().get(0));
            ClaseClonIA clase = new ClaseClonIA(titulo, guionPrompt, docente, estado);
            clonRepo.save(clase);

            ra.addFlashAttribute("mensaje", "¡Clase audiovisual con Clon IA generada correctamente!");
            return "redirect:/ia/clon/clases";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ia/clon/clases/nueva?unidadId=" + unidadId;
        }
    }

    @GetMapping("/clon/clases/{id}/editar")
    public String modificarClaseConClonForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ClaseClonIA> cOpt = clonRepo.findById(id);
        if (cOpt.isEmpty()) return "redirect:/ia/clon/clases";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clase", cOpt.get());
        model.addAttribute("titulo", "CU-79 - Modificar clase con clon IA | Idóneos Online");
        return "pages/ia_vivo/cu-79-modificar-clase-con-clon";
    }

    @PostMapping("/clon/clases/{id}/editar")
    public String actualizarClaseConClon(@PathVariable Integer id,
                                         @RequestParam String titulo,
                                         @RequestParam String guionPrompt,
                                         RedirectAttributes ra) {
        try {
            ClaseClonIA c = clonRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
            c.setTitulo(titulo);
            c.setGuion(guionPrompt);
            clonRepo.save(c);
            ra.addFlashAttribute("mensaje", "Clase modificada con éxito.");
            return "redirect:/ia/clon/clases";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ia/clon/clases/" + id + "/editar";
        }
    }

    @GetMapping("/clon/clases/{id}/baja")
    public String darDeBajaClaseConClonView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ClaseClonIA> cOpt = clonRepo.findById(id);
        if (cOpt.isEmpty()) return "redirect:/ia/clon/clases";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clase", cOpt.get());
        model.addAttribute("titulo", "CU-80 - Dar de baja clase con clon IA | Idóneos Online");
        return "pages/ia_vivo/cu-80-dar-de-baja-clase-con-clon";
    }

    @PostMapping("/clon/clases/{id}/baja")
    public String eliminarClaseConClon(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            ClaseClonIA c = clonRepo.findById(id).orElse(null);
            if (c != null) {
                c.setBaja(true);
                clonRepo.save(c);
            }
            ra.addFlashAttribute("mensaje", "Clase con Clon IA dada de baja exitosamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ia/clon/clases";
    }
}
