package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.ConfiguracionRepository;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.RespuestaForoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.service.EmailService;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

/**
 * Módulo de Cursos — Foros por Unidad (CU-21, CU-22).
 * Permite a alumnos realizar consultas y a docentes responder.
 * Respeta el límite de tiempo de edición/eliminación configurable (CU-85: 'foro.tiempo_limite_edicion_minutos').
 */
@Controller
@RequestMapping("/foro")
public class ForoController {

    @Autowired private ConsultaForoRepository consultaRepo;
    @Autowired private RespuestaForoRepository respuestaRepo;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private DocenteCursoRepository docenteCursoRepository;
    @Autowired private ConfiguracionRepository configRepo;
    @Autowired private EmailService emailService;

    private int getTiempoLimiteEdicionMinutos() {
        return configRepo.findByClave("foro.tiempo_limite_edicion_minutos")
                .map(c -> {
                    try { return Integer.parseInt(c.getValor()); }
                    catch (NumberFormatException e) { return 30; }
                })
                .orElse(30);
    }

    @GetMapping("/unidad/{unidadId}")
    public String verForo(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/cursos";

        List<ConsultaForo> consultas = consultaRepo.findByUnidadAndBajaFalseOrderByFechaDesc(unidad);
        Usuario usuario = (Usuario) auth.getPrincipal();

        model.addAttribute("usuario", usuario);
        model.addAttribute("unidad", unidad);
        model.addAttribute("consultas", consultas);
        model.addAttribute("tiempoEdicionMinutos", getTiempoLimiteEdicionMinutos());
        model.addAttribute("titulo", "Foro — " + unidad.getTitulo());
        return "pages/alumno/foro-unidad";
    }

    @PostMapping("/unidad/{unidadId}/consulta/nueva")
    public String nuevaConsulta(@PathVariable Integer unidadId,
                                @RequestParam String texto,
                                Authentication auth,
                                RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/cursos";
        Usuario usuario = (Usuario) auth.getPrincipal();
        ConsultaForo consulta = consultaRepo.save(new ConsultaForo(texto, unidad, usuario));

        // CU-24: Notificar al docente titular del curso
        docenteCursoRepository.findByCurso(unidad.getCurso()).stream()
                .filter(dc -> !dc.isEsSupervisor())
                .findFirst()
                .ifPresent(dc -> emailService.enviarNuevaConsultaForo(
                        dc.getDocente().getUsuario().getCorreo(), consulta));

        ra.addFlashAttribute("mensaje", "Consulta publicada en el foro.");
        return "redirect:/foro/unidad/" + unidadId;
    }

    @PostMapping("/consulta/{consultaId}/editar")
    public String editarConsulta(@PathVariable Integer consultaId,
                                 @RequestParam String texto,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        ConsultaForo c = consultaRepo.findById(consultaId).orElse(null);
        if (c == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        if (c.getUsuario().getId() != usuario.getId() && !usuario.esAdmin()) {
            ra.addFlashAttribute("mensaje", "No estás autorizado a editar esta consulta.");
            return "redirect:/foro/unidad/" + c.getUnidad().getId();
        }

        int limiteMinutos = getTiempoLimiteEdicionMinutos();
        long minutosTranscurridos = Duration.between(c.getFecha(), LocalDateTime.now()).toMinutes();
        if (minutosTranscurridos > limiteMinutos && !usuario.esAdmin()) {
            ra.addFlashAttribute("mensaje", "El tiempo límite para editar la consulta (" + limiteMinutos + " minutos) ha expirado.");
            return "redirect:/foro/unidad/" + c.getUnidad().getId();
        }

        c.setTexto(texto);
        consultaRepo.save(c);
        ra.addFlashAttribute("mensaje", "Consulta actualizada correctamente.");
        return "redirect:/foro/unidad/" + c.getUnidad().getId();
    }

    @PostMapping("/consulta/{consultaId}/responder")
    public String responder(@PathVariable Integer consultaId,
                            @RequestParam String texto,
                            Authentication auth,
                            RedirectAttributes ra) {
        ConsultaForo consulta = consultaRepo.findById(consultaId).orElse(null);
        if (consulta == null) return "redirect:/docente";
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepository.findById(usuario.getId()).orElse(null);
        if (docente == null && !usuario.esAdmin()) return "redirect:/docente";

        RespuestaForo respuesta = respuestaRepo.save(new RespuestaForo(texto, consulta, docente));

        // CU-28: Notificar al alumno que publicó la consulta
        emailService.enviarNuevaRespuestaForo(respuesta);

        ra.addFlashAttribute("mensaje", "Respuesta publicada.");
        return "redirect:/foro/unidad/" + consulta.getUnidad().getId();
    }

    @PostMapping("/respuesta/{respuestaId}/editar")
    public String editarRespuesta(@PathVariable Integer respuestaId,
                                  @RequestParam String texto,
                                  Authentication auth,
                                  RedirectAttributes ra) {
        RespuestaForo r = respuestaRepo.findById(respuestaId).orElse(null);
        if (r == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        if (r.getDocente() != null && r.getDocente().getId() != usuario.getId() && !usuario.esAdmin()) {
            ra.addFlashAttribute("mensaje", "No estás autorizado a editar esta respuesta.");
            return "redirect:/foro/unidad/" + r.getConsulta().getUnidad().getId();
        }

        int limiteMinutos = getTiempoLimiteEdicionMinutos();
        long minutosTranscurridos = Duration.between(r.getFecha(), LocalDateTime.now()).toMinutes();
        if (minutosTranscurridos > limiteMinutos && !usuario.esAdmin()) {
            ra.addFlashAttribute("mensaje", "El tiempo límite para editar la respuesta (" + limiteMinutos + " minutos) ha expirado.");
            return "redirect:/foro/unidad/" + r.getConsulta().getUnidad().getId();
        }

        r.setTexto(texto);
        respuestaRepo.save(r);
        ra.addFlashAttribute("mensaje", "Respuesta actualizada correctamente.");
        return "redirect:/foro/unidad/" + r.getConsulta().getUnidad().getId();
    }

    @PostMapping("/consulta/{consultaId}/borrar")
    public String borrarConsulta(@PathVariable Integer consultaId, RedirectAttributes ra) {
        ConsultaForo c = consultaRepo.findById(consultaId).orElse(null);
        if (c != null) {
            c.setBaja(true);
            consultaRepo.save(c);
        }
        ra.addFlashAttribute("mensaje", "Consulta eliminada.");
        return "redirect:/foro/unidad/" + (c != null ? c.getUnidad().getId() : "");
    }

    @PostMapping("/respuesta/{respuestaId}/borrar")
    public String borrarRespuesta(@PathVariable Integer respuestaId, RedirectAttributes ra) {
        RespuestaForo r = respuestaRepo.findById(respuestaId).orElse(null);
        if (r != null) {
            r.setBaja(true);
            respuestaRepo.save(r);
        }
        ra.addFlashAttribute("mensaje", "Respuesta eliminada.");
        return "redirect:/foro/unidad/" + (r != null ? r.getConsulta().getUnidad().getId() : "");
    }
}
