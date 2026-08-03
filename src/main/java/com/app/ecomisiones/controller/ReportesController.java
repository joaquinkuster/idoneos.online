package com.app.ecomisiones.controller;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.repository.*;
import com.app.ecomisiones.service.Curso.CursoServiceImpl;
import com.app.ecomisiones.service.Usuario.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller para Reportes, Estadísticas y Registros de Auditoría (Admin).
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

    @GetMapping("/reportes")
    public String verReportes(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();

        List<Reporte> reportes = reporteRepository.findAll();
        List<TipoReporte> tipos = tipoReporteRepository.findAll();

        // Indicadores en vivo
        long totalAlumnos = usuarioService.obtenerTodo().stream().filter(Usuario::esAlumno).count();
        long totalCursos = cursoService.obtenerTodo().size();
        long totalInscripciones = inscripcionRepository.count();
        Double totalIngresos = pagoRepository.findAll().stream()
                .filter(p -> "Acreditado".equals(p.getEstadoPago().getNombre()))
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

    @GetMapping("/auditoria")
    public String verAuditoria(Model model, Authentication auth) {
        List<Auditoria> registros = auditoriaRepository.findAll();
        model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        model.addAttribute("registros", registros);
        model.addAttribute("titulo", "Log de Auditoría | Idóneos Online");
        return "pages/admin/auditoria";
    }
}
