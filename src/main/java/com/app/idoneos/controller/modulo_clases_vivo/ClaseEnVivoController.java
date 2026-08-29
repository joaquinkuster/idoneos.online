package com.app.idoneos.controller.modulo_clases_vivo;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * TRAZABILIDAD — Controller para el Módulo de Clases en Vivo (MOD-F-05).
 *
 * Mapea y conecta directamente las 8 pantallas de Clases en Vivo:
 *   CU-65 a CU-72
 */
@Controller
@RequestMapping("/clases-vivo")
public class ClaseEnVivoController {

    @Autowired private ClaseEnVivoRepository claseEnVivoRepository;
    @Autowired private EstadoClaseEnVivoRepository estadoRepo;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private CohorteRepository cohorteRepository;
    @Autowired private UnidadService unidadService;
    @Autowired private CursoService cursoService;
    @Autowired private UsuarioRepository usuarioRepository;

    private Usuario obtenerUsuarioAutenticado(Authentication auth) {
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        String email = auth.getName();
        if (email != null && usuarioRepository != null) {
            return usuarioRepository.findByCorreo(email).orElse(null);
        }
        return null;
    }

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        Usuario u = obtenerUsuarioAutenticado(auth);
        if (u != null) {
            model.addAttribute("usuario", u);
        }
    }

    /**
     * CU-65 — Buscar clase en vivo.
     * Vista: cu-65-buscar-clase-en-vivo.html
     */
    @GetMapping
    public String buscarClasesEnVivo(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                     @RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                     Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        Curso curso = (cursoId != null) ? cursoService.buscarPorId(cursoId).orElse(null) : null;
        List<Cohorte> cohortes = cohorteRepository.findAll();
        Cohorte cohorte = (cohorteId != null) ? cohorteRepository.findById(cohorteId).orElse(null) : null;

        if (curso == null && cohorte != null && cohorte.getPrograma() != null && cohorte.getPrograma().getCurso() != null) {
            curso = cohorte.getPrograma().getCurso();
        }
        if (curso == null) {
            List<Curso> todos = cursoService.obtenerTodo();
            if (!todos.isEmpty()) curso = todos.get(0);
        }

        final Curso cursoFinal = curso;
        List<ClaseEnVivo> todas = claseEnVivoRepository.findAll().stream().filter(c -> !c.getBaja()).toList();
        List<ClaseEnVivo> clases = (cohorte != null)
                ? todas.stream().filter(c -> c.getCohorte() != null && c.getCohorte().getId() == cohorte.getId()).toList()
                : ((cursoFinal != null)
                    ? todas.stream().filter(c -> c.getCohorte() != null && c.getCohorte().getPrograma() != null && cursoFinal.equals(c.getCohorte().getPrograma().getCurso())).toList()
                    : todas);

        model.addAttribute("curso", cursoFinal);
        model.addAttribute("cursoSeleccionado", cursoFinal);
        model.addAttribute("clases", clases);
        model.addAttribute("cohortes", cohortes);
        model.addAttribute("cohorteSeleccionada", cohorte);
        model.addAttribute("titulo", "CU-65 - Buscar clase en vivo | Idóneos Online");
        return "pages/ia_vivo/cu-65-buscar-clase-en-vivo";
    }

    /**
     * CU-66 — Programar clase en vivo (GET).
     * Vista: cu-66-programar-clase-en-vivo.html
     */
    @GetMapping("/nueva")
    public String programarClaseForm(@RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                     Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cohortes", cohorteRepository.findAll());
        model.addAttribute("cohorteId", cohorteId);
        model.addAttribute("titulo", "CU-66 - Programar clase en vivo | Idóneos Online");
        return "pages/ia_vivo/cu-66-programar-clase-en-vivo";
    }

    /**
     * CU-66 — Programar clase en vivo (POST).
     */
    @PostMapping("/guardar")
    public String guardarClase(@RequestParam Integer cohorteId,
                               @RequestParam String titulo,
                               @RequestParam String fechaHora,
                               @RequestParam(defaultValue = "60") int duracionEstimada,
                               Authentication auth, RedirectAttributes ra) {
        try {
            Cohorte cohorte = cohorteRepository.findById(cohorteId).orElseThrow(() -> new IllegalArgumentException("Cohorte no encontrada"));
            Docente docente = null;
            Usuario u = obtenerUsuarioAutenticado(auth);
            if (u != null) {
                if (u.getDocente() != null) {
                    docente = u.getDocente();
                } else {
                    docente = docenteRepository.findById(u.getId()).orElse(null);
                }
            }
            if (docente == null) {
                List<Docente> docentes = docenteRepository.findAll();
                docente = docentes.isEmpty() ? null : docentes.get(0);
            }

            EstadoClaseEnVivo estado = estadoRepo.findByNombre("Programada").orElseGet(() -> estadoRepo.findAll().get(0));
            LocalDateTime fHora = LocalDateTime.parse(fechaHora.contains("T") ? fechaHora : fechaHora + "T18:00:00");
            LocalDateTime fFin = fHora.plusMinutes(duracionEstimada);

            if (docente != null && claseEnVivoRepository.existsByDocenteAndFechaHoraBetweenAndBajaFalse(docente, fHora.minusMinutes(30), fFin)) {
                ra.addFlashAttribute("error", "El docente seleccionado ya tiene una clase programada en ese rango horario.");
                return "redirect:/clases-vivo/nueva?cohorteId=" + cohorteId;
            }

            ClaseEnVivo clase = new ClaseEnVivo(titulo, fHora, duracionEstimada, "rtmp://live.idoneos.online/live", UUID.randomUUID().toString(), docente, estado, cohorte);
            claseEnVivoRepository.save(clase);
            ra.addFlashAttribute("mensaje", "Clase en vivo programada exitosamente.");
            return "redirect:/clases-vivo";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/clases-vivo/nueva";
        }
    }

    /**
     * CU-67 — Modificar clase en vivo (GET).
     * Vista: cu-67-modificar-clase-en-vivo.html
     */
    @GetMapping("/{id}/editar")
    public String modificarClaseForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ClaseEnVivo> cOpt = claseEnVivoRepository.findById(id);
        if (cOpt.isEmpty()) return "redirect:/clases-vivo";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clase", cOpt.get());
        model.addAttribute("titulo", "CU-67 - Modificar clase en vivo | Idóneos Online");
        return "pages/ia_vivo/cu-67-modificar-clase-en-vivo";
    }

    @PostMapping("/{id}/editar")
    public String actualizarClase(@PathVariable Integer id,
                                  @RequestParam String titulo,
                                  @RequestParam String fechaHora,
                                  @RequestParam int duracionEstimada,
                                  RedirectAttributes ra) {
        try {
            ClaseEnVivo c = claseEnVivoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
            c.setTitulo(titulo);
            c.setFechaHora(LocalDateTime.parse(fechaHora.contains("T") ? fechaHora : fechaHora + "T18:00:00"));
            c.setDuracionEstimada(duracionEstimada);
            claseEnVivoRepository.save(c);
            ra.addFlashAttribute("mensaje", "Clase en vivo actualizada con éxito.");
            return "redirect:/clases-vivo";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/clases-vivo/" + id + "/editar";
        }
    }

    /**
     * CU-68 — Cancelar clase en vivo (GET/POST).
     * Vista: cu-68-cancelar-clase-en-vivo.html
     */
    @GetMapping("/{id}/cancelar")
    public String cancelarClaseView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ClaseEnVivo> cOpt = claseEnVivoRepository.findById(id);
        if (cOpt.isEmpty()) return "redirect:/clases-vivo";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clase", cOpt.get());
        model.addAttribute("titulo", "CU-68 - Cancelar clase en vivo | Idóneos Online");
        return "pages/ia_vivo/cu-68-cancelar-clase-en-vivo";
    }

    @PostMapping("/{id}/cancelar")
    public String procesarCancelarClase(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            ClaseEnVivo c = claseEnVivoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
            EstadoClaseEnVivo cancelada = estadoRepo.findByNombre("Cancelada").orElse(null);
            if (cancelada != null) {
                c.setEstado(cancelada);
                claseEnVivoRepository.save(c);
            }
            ra.addFlashAttribute("mensaje", "Clase cancelada exitosamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clases-vivo";
    }

    /**
     * CU-69 — Dar de baja clase en vivo (GET/POST).
     * Vista: cu-69-dar-de-baja-clase-en-vivo.html
     */
    @GetMapping("/{id}/baja")
    public String darDeBajaClaseView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ClaseEnVivo> cOpt = claseEnVivoRepository.findById(id);
        if (cOpt.isEmpty()) return "redirect:/clases-vivo";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clase", cOpt.get());
        model.addAttribute("titulo", "CU-69 - Dar de baja clase en vivo | Idóneos Online");
        return "pages/ia_vivo/cu-69-dar-de-baja-clase-en-vivo";
    }

    @PostMapping("/{id}/baja")
    public String eliminarClase(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            ClaseEnVivo c = claseEnVivoRepository.findById(id).orElse(null);
            if (c != null) {
                c.setBaja(true);
                claseEnVivoRepository.save(c);
            }
            ra.addFlashAttribute("mensaje", "Clase dada de baja correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clases-vivo";
    }

    /**
     * CU-70 — Iniciar clase en vivo (GET/POST).
     * Vista: cu-70-iniciar-clase-en-vivo.html
     */
    @GetMapping("/{id}/iniciar")
    public String iniciarClaseView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ClaseEnVivo> cOpt = claseEnVivoRepository.findById(id);
        if (cOpt.isEmpty()) return "redirect:/clases-vivo";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clase", cOpt.get());
        model.addAttribute("titulo", "CU-70 - Iniciar clase en vivo | Idóneos Online");
        return "pages/ia_vivo/cu-70-iniciar-clase-en-vivo";
    }

    @PostMapping("/{id}/iniciar")
    public String transmitirClase(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            ClaseEnVivo c = claseEnVivoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
            EstadoClaseEnVivo enVivo = estadoRepo.findByNombre("En Vivo").orElse(null);
            if (enVivo != null) {
                c.setEstado(enVivo);
                claseEnVivoRepository.save(c);
            }
            ra.addFlashAttribute("mensaje", "¡Transmisión iniciada en directo!");
            return "redirect:/clases-vivo/" + id + "/sala";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/clases-vivo/" + id + "/iniciar";
        }
    }

    /**
     * CU-71 — Finalizar clase en vivo (GET/POST).
     * Vista: cu-71-finalizar-clase-en-vivo.html
     */
    @GetMapping("/{id}/finalizar")
    public String finalizarClaseView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ClaseEnVivo> cOpt = claseEnVivoRepository.findById(id);
        if (cOpt.isEmpty()) return "redirect:/clases-vivo";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clase", cOpt.get());
        model.addAttribute("titulo", "CU-71 - Finalizar clase en vivo | Idóneos Online");
        return "pages/ia_vivo/cu-71-finalizar-clase-en-vivo";
    }

    @PostMapping("/{id}/finalizar")
    public String concluirClase(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            ClaseEnVivo c = claseEnVivoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Clase no encontrada"));
            EstadoClaseEnVivo fin = estadoRepo.findByNombre("Finalizada").orElse(null);
            if (fin != null) {
                c.setEstado(fin);
                claseEnVivoRepository.save(c);
            }
            ra.addFlashAttribute("mensaje", "Clase en vivo finalizada y grabada con éxito.");
            return "redirect:/clases-vivo";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/clases-vivo/" + id + "/finalizar";
        }
    }

    /**
     * CU-72 — Ingresar a clase en vivo (Sala del alumno).
     * Vista: cu-72-ingresar-a-clase-en-vivo.html
     */
    @GetMapping("/{id}/sala")
    public String ingresarSalaClase(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<ClaseEnVivo> cOpt = claseEnVivoRepository.findById(id);
        if (cOpt.isEmpty()) return "redirect:/clases-vivo";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("clase", cOpt.get());
        model.addAttribute("titulo", "CU-72 - Sala de Transmisión: " + cOpt.get().getTitulo() + " | Idóneos Online");
        return "pages/ia_vivo/cu-72-ingresar-a-clase-en-vivo";
    }
}
