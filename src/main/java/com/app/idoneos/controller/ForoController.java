package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.RespuestaForoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Foro por unidad: alumnos consultan, docentes responden.
 */
@Controller
@RequestMapping("/foro")
public class ForoController {

    @Autowired private ConsultaForoRepository consultaRepo;
    @Autowired private RespuestaForoRepository respuestaRepo;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private DocenteRepository docenteRepository;

    @GetMapping("/unidad/{unidadId}")
    public String verForo(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/cursos";

        List<ConsultaForo> consultas = consultaRepo.findByUnidadAndBajaFalseOrderByFechaDesc(unidad);
        Usuario usuario = (Usuario) auth.getPrincipal();

        model.addAttribute("usuario", usuario);
        model.addAttribute("unidad", unidad);
        model.addAttribute("consultas", consultas);
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
        consultaRepo.save(new ConsultaForo(texto, unidad, usuario));
        ra.addFlashAttribute("mensaje", "Consulta publicada.");
        return "redirect:/foro/unidad/" + unidadId;
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
        if (docente == null) return "redirect:/docente";
        respuestaRepo.save(new RespuestaForo(texto, consulta, docente));
        ra.addFlashAttribute("mensaje", "Respuesta publicada.");
        return "redirect:/foro/unidad/" + consulta.getUnidad().getId();
    }

    @PostMapping("/consulta/{consultaId}/borrar")
    public String borrarConsulta(@PathVariable Integer consultaId, RedirectAttributes ra) {
        ConsultaForo c = consultaRepo.findById(consultaId).orElse(null);
        if (c != null) { c.setBaja(true); consultaRepo.save(c); }
        ra.addFlashAttribute("mensaje", "Consulta eliminada.");
        return "redirect:/foro/unidad/" + (c != null ? c.getUnidad().getId() : "");
    }
}
