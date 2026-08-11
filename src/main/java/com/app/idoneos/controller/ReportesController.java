package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.Curso.CursoServiceImpl;
import com.app.idoneos.service.Usuario.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller para la generación y visualización de reportes y estadísticas ejecutivas (CU-90: Generar reportes y estadísticas).
 */
@Controller
@RequestMapping("/admin")
public class ReportesController {

    @Autowired private ReporteRepository reporteRepository;
    @Autowired private TipoReporteRepository tipoReporteRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private UsuarioServiceImpl usuarioService;
    @Autowired private CursoServiceImpl cursoService;
    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private AdministradorRepository administradorRepository;

    /**
     * CU-90 — Ver panel de reportes e indicadores clave de rendimiento (KPIs).
     */
    @GetMapping("/reportes")
    public String verReportes(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();

        List<Reporte> reportes = reporteRepository.findAll();
        List<TipoReporte> tipos = tipoReporteRepository.findAll();

        // Indicadores ejecutivos
        long totalAlumnos = usuarioService.obtenerTodo().stream().filter(Usuario::esAlumno).count();
        long totalCursos = cursoService.obtenerTodo().size();
        long totalInscripciones = inscripcionRepository.count();
        Double totalIngresos = pagoRepository.findAll().stream()
                .filter(p -> p.getEstadoPago() != null && "Acreditado".equals(p.getEstadoPago().getNombre()))
                .mapToDouble(Pago::getMonto).sum();

        model.addAttribute("usuario", usuario);
        model.addAttribute("reportes", reportes);
        model.addAttribute("tiposReporte", tipos);
        model.addAttribute("totalAlumnos", totalAlumnos);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("totalInscripciones", totalInscripciones);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("titulo", "Reportes y Estadísticas | Idóneos Online");
        return "pages/admin/reportes";
    }

    /**
     * CU-90 — Generar reporte ejecutivo (inscripciones, recaudación, completitud).
     */
    @PostMapping("/reportes/generar")
    public String generarReporte(@RequestParam Integer tipoReporteId, Authentication auth, RedirectAttributes ra) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Administrador admin = administradorRepository.findById(usuario.getId()).orElse(null);
        TipoReporte tipo = tipoReporteRepository.findById(tipoReporteId).orElse(null);

        if (admin != null && tipo != null) {
            reporteRepository.save(new Reporte(tipo, admin));
            ra.addFlashAttribute("mensaje", "Reporte de " + tipo.getNombre() + " generado correctamente.");
        }
        return "redirect:/admin/reportes";
    }
}
