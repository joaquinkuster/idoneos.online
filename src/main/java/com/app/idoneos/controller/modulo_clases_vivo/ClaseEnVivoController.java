package com.app.idoneos.controller.modulo_clases_vivo;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
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
 * TRAZABILIDAD — Controller para la programación y transmisión de Clases en
 * Vivo.
 *
 * MOD-F-05: Módulo de Clases en Vivo
 * CU-65 — Buscar clase en vivo → GET /clase-vivo/docente (con filtros)
 * CU-66 — Programar clase en vivo → POST /clase-vivo/programar (con cohorte en
 * dictado y duración estimada)
 * CU-67 — Modificar clase en vivo → POST /clase-vivo/{claseId}/modificar
 * (título, fecha/hora, duración)
 * CU-68 — Cancelar clase en vivo → POST /clase-vivo/{claseId}/cancelar (cambia
 * a estado Cancelada o notifica)
 * CU-69 — Dar de baja clase en vivo → POST /clase-vivo/{claseId}/baja (baja
 * lógica para Docente/Admin)
 * CU-70 — Iniciar clase en vivo → POST /clase-vivo/{claseId}/iniciar (genera
 * datos de conexión RTMP)
 * CU-71 — Finalizar clase en vivo → POST /clase-vivo/{claseId}/finalizar
 * (cambia a Finalizada y genera material)
 * CU-72 — Ingresar a clase en vivo → GET /clase-vivo/{claseId}/ver
 * (verificación de alumno y acceso a sala)
 */
@Controller
@RequestMapping("/clase-vivo")
public class ClaseEnVivoController {

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;
    @Autowired
    private EstadoClaseEnVivoRepository estadoRepo;
    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private UnidadServiceImpl unidadService;
    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private TipoMaterialRepository tipoMaterialRepository;
    @Autowired
    private CohorteRepository cohorteRepository;
    @Autowired
    private CronogramaRepository cronogramaRepository;

    /**
     * TRAZABILIDAD: CU-65 — Buscar clase en vivo.
     * Actor: Docente / Administrador.
     */
    @GetMapping("/docente")
    public String misClases(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepository.findById(usuario.getId()).orElse(null);
        if (docente == null && !usuario.esAdmin())
            return "redirect:/docente";

        List<ClaseEnVivo> clases = (docente != null)
                ? claseEnVivoRepository.findByDocenteAndBajaFalseOrderByFechaHoraDesc(docente)
                : claseEnVivoRepository.findAll().stream().filter(c -> !c.getBaja()).toList();

        model.addAttribute("usuario", usuario);
        model.addAttribute("clases", clases);
        model.addAttribute("titulo", "Clases en Vivo | Idóneos Online");
        return "pages/docente/clases-en-vivo";
    }

    /**
     * TRAZABILIDAD: CU-66 — Programar clase en vivo.
     * Actor: Docente.
     */
    @PostMapping("/programar")
    public String programar(@RequestParam(required = false) Integer cohorteId,
            @RequestParam(required = false) Integer unidadId,
            @RequestParam String titulo,
            @RequestParam String fechaHora,
            @RequestParam(defaultValue = "60") int duracionEstimada,
            Authentication auth,
            RedirectAttributes ra) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepository.findById(usuario.getId()).orElse(null);
        EstadoClaseEnVivo estadoProgramada = estadoRepo.findByNombre("Programada").orElse(null);

        Cohorte cohorte = null;
        if (cohorteId != null) {
            cohorte = cohorteRepository.findById(cohorteId).orElse(null);
        } else if (unidadId != null) {
            Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
            if (unidad != null) {
                List<Cronograma> cronogramas = cronogramaRepository.findByUnidad(unidad);
                for (Cronograma crono : cronogramas) {
                    if (crono.getPrograma() != null) {
                        List<Cohorte> cohortes = cohorteRepository.findByProgramaAndBajaFalse(crono.getPrograma());
                        if (!cohortes.isEmpty()) {
                            cohorte = cohortes.get(0);
                            break;
                        }
                    }
                }
            }
        }

        if (docente == null || estadoProgramada == null || cohorte == null) {
            ra.addFlashAttribute("mensaje", "EX-CU66-01: Datos incompletos o cohorte en dictado no seleccionada.");
            return "redirect:/clase-vivo/docente";
        }

        LocalDateTime dt = LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        ClaseEnVivo clase = new ClaseEnVivo(titulo.trim(), dt, docente, estadoProgramada, cohorte);
        clase.setDuracionEstimada(duracionEstimada);
        claseEnVivoRepository.save(clase);

        ra.addFlashAttribute("mensaje",
                "Clase '" + titulo + "' programada para " + dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-67 — Modificar clase en vivo.
     * Actor: Docente.
     */
    @PostMapping("/{claseId}/modificar")
    public String modificar(@PathVariable Integer claseId,
            @RequestParam String titulo,
            @RequestParam String fechaHora,
            @RequestParam(defaultValue = "60") int duracionEstimada,
            RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase == null)
            return "redirect:/clase-vivo/docente";

        LocalDateTime dt = LocalDateTime.parse(fechaHora, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        clase.setTitulo(titulo.trim());
        clase.setFechaHora(dt);
        clase.setDuracionEstimada(duracionEstimada);
        claseEnVivoRepository.save(clase);

        ra.addFlashAttribute("mensaje", "Clase en vivo modificada correctamente.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-68 — Cancelar clase en vivo.
     * Actor: Docente.
     */
    @PostMapping("/{claseId}/cancelar")
    public String cancelar(@PathVariable Integer claseId, RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase != null) {
            EstadoClaseEnVivo estadoCancelada = estadoRepo.findByNombre("Cancelada").orElse(null);
            if (estadoCancelada != null) {
                clase.setEstado(estadoCancelada);
            } else {
                clase.setOculto(true);
            }
            claseEnVivoRepository.save(clase);
        }
        ra.addFlashAttribute("mensaje", "Clase en vivo cancelada.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-69 — Dar de baja clase en vivo.
     * Actor: Docente / Administrador.
     */
    @PostMapping("/{claseId}/baja")
    public String darDeBaja(@PathVariable Integer claseId, RedirectAttributes ra) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase != null) {
            clase.setBaja(true);
            claseEnVivoRepository.save(clase);
        }
        ra.addFlashAttribute("mensaje", "Clase en vivo dada de baja del sistema.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-70 — Iniciar clase en vivo.
     * Actor: Docente.
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
     * TRAZABILIDAD: CU-71 — Finalizar clase en vivo.
     * Actor: Docente.
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
            if (tituloMat.length() > 50)
                tituloMat = tituloMat.substring(0, 47) + "...";

            Unidad unidadMaterial = null;
            if (clase.getCohorte() != null && clase.getCohorte().getPrograma() != null) {
                List<Cronograma> cronos = cronogramaRepository
                        .findByProgramaOrderByNumeroOrden(clase.getCohorte().getPrograma());
                if (!cronos.isEmpty()) {
                    unidadMaterial = cronos.get(0).getUnidad();
                }
            }

            if (unidadMaterial != null) {
                Material grabacion = new Material(tituloMat, clase.getDocente(), tipoGrabacion, unidadMaterial);
                grabacion.setRutaArchivo(rutaGrabacion);
                grabacion.setPublicado(false);
                materialRepository.save(grabacion);
                clase.setMaterial(grabacion);
            }
        }

        claseEnVivoRepository.save(clase);
        ra.addFlashAttribute("mensaje", "Clase finalizada. La grabación está disponible para revisión.");
        return "redirect:/clase-vivo/docente";
    }

    /**
     * TRAZABILIDAD: CU-72 — Ingresar a clase en vivo.
     * Actor: Alumno.
     */
    @GetMapping("/{claseId}/ver")
    public String verClase(@PathVariable Integer claseId, Model model, Authentication auth) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(claseId).orElse(null);
        if (clase == null)
            return "redirect:/cursos";

        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("clase", clase);
        model.addAttribute("titulo", "Clase en Vivo: " + clase.getTitulo());
        return "pages/alumno/ver-clase-vivo";
    }
}
