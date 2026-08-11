package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Controller para la programación y transmisión de Clases en Vivo (CU-61 a CU-64).
 */
@Controller
@RequestMapping("/clase-vivo")
public class ClaseEnVivoController {

    @Autowired private ClaseEnVivoRepository claseEnVivoRepository;
    @Autowired private EstadoClaseEnVivoRepository estadoRepo;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;

    /**
     * CU-61 — Buscar y listar clases en vivo del docente.
     */
    @GetMapping("/docente")
    public String misClases(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepository.findById(usuario.getId()).orElse(null);
        if (docente == null) return "redirect:/docente";

        List<ClaseEnVivo> clases = claseEnVivoRepository.findByDocenteAndBajaFalseOrderByFechaHoraDesc(docente);
        model.addAttribute("usuario", usuario);
        model.addAttribute("clases", clases);
        model.addAttribute("titulo", "Mis Clases en Vivo | Idóneos Online");
        return "pages/docente/clases-en-vivo";
    }

    /**
     * CU-62 — Programar nueva clase en vivo.
     */
    @PostMapping("/programar")
    public String programar(@RequestParam Integer unidadId,
                            @RequestParam String titulo,
                            @RequestParam String fechaHora,
                            Authentication auth,
                            RedirectAttributes ra) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepository.findById(usuario.getId()).orElse(null);
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        EstadoClaseEnVivo estadoProgramada = estadoRepo.findByNombre("Programada").orElse(null);

        if (docente == null || unidad == null || estadoProgramada == null) {
            ra.addFlashAttribute("mensaje", "CU-62 Excepción paso 4: Datos incompletos al programar la clase.");
            return "redirect:/clase-vivo/docente";
        }

        LocalDateTime dt = LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        ClaseEnVivo clase = new ClaseEnVivo(titulo, dt, unidad, docente, estadoProgramada);
        claseEnVivoRepository.save(clase);
        ra.addFlashAttribute("mensaje", "Clase programada para " + dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return "redirect:/clase-vivo/docente";
    }

    /**
     * CU-63 — Modificar clase en vivo programada.
     */
    @PostMapping("/{claseId}/modificar")
    public String modificar(@PathVariable Integer claseId,
                            @RequestParam String titulo,
                            @RequestParam String fechaHora,
                            RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase == null) return "redirect:/clase-vivo/docente";

        LocalDateTime dt = LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        clase.setTitulo(titulo);
        clase.setFechaHora(dt);
        claseEnVivoRepository.save(clase);

        ra.addFlashAttribute("mensaje", "Clase modificada correctamente.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * CU-64 — Eliminar/Cancelar clase en vivo (baja lógica).
     */
    @PostMapping("/{claseId}/cancelar")
    public String cancelar(@PathVariable Integer claseId, RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase != null) {
            clase.setBaja(true);
            claseEnVivoRepository.save(clase);
        }
        ra.addFlashAttribute("mensaje", "Clase en vivo cancelada.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * CU-62 — Iniciar transmisión en vivo (Generación de RTMP + Stream Key).
     */
    @PostMapping("/{claseId}/iniciar")
    public String iniciar(@PathVariable Integer claseId, RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        EstadoClaseEnVivo estadoEnVivo = estadoRepo.findByNombre("En vivo").orElse(null);

        if (clase == null || estadoEnVivo == null) {
            ra.addFlashAttribute("mensaje", "No se pudo iniciar la clase.");
            return "redirect:/clase-vivo/docente";
        }

        String urlRtmp = "rtmp://live.idoneos.online/stream/" + claseId;
        String claveStream = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        clase.setEstado(estadoEnVivo);
        clase.setUrlRtmp(urlRtmp);
        clase.setClaveStream(claveStream);
        claseEnVivoRepository.save(clase);

        ra.addFlashAttribute("mensaje", "Clase iniciada. URL RTMP: " + urlRtmp + " | Clave: " + claveStream);
        return "redirect:/clase-vivo/docente";
    }

    /**
     * CU-62 — Finalizar transmisión en vivo y generar material de grabación en revisión.
     */
    @PostMapping("/{claseId}/finalizar")
    public String finalizar(@PathVariable Integer claseId, RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        EstadoClaseEnVivo estadoFinalizada = estadoRepo.findByNombre("Finalizada").orElse(null);
        TipoMaterial tipoGrabacion = tipoMaterialRepository.findByNombre("Grabación").orElse(null);

        if (clase == null || estadoFinalizada == null) {
            ra.addFlashAttribute("mensaje", "No se pudo finalizar la clase.");
            return "redirect:/clase-vivo/docente";
        }

        clase.setEstado(estadoFinalizada);

        if (tipoGrabacion != null) {
            String rutaGrabacion = "grabaciones/clase_" + claseId + "_" + System.currentTimeMillis() + ".mp4";
            String tituloMat = "Grabación: " + clase.getTitulo();
            if (tituloMat.length() > 250) tituloMat = tituloMat.substring(0, 247) + "...";
            Material grabacion = new Material(tipoGrabacion, tituloMat, rutaGrabacion, clase.getUnidad());
            grabacion.setPublicado(false);
            materialRepository.save(grabacion);
            clase.setMaterial(grabacion);
        }

        claseEnVivoRepository.save(clase);
        ra.addFlashAttribute("mensaje", "Clase finalizada. La grabación está disponible para revisión.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * CU-61 — Visualizar clase en vivo para alumnos inscriptos.
     */
    @GetMapping("/{claseId}/ver")
    public String verClase(@PathVariable Integer claseId, Model model, Authentication auth) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase == null) return "redirect:/cursos";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("clase", clase);
        model.addAttribute("titulo", "Clase en Vivo: " + clase.getTitulo());
        return "pages/alumno/ver-clase-vivo";
    }
}
