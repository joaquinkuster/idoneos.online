package com.app.idoneos.controller.modulo_reportes;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_reportes.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TRAZABILIDAD — Controller para el Módulo de Reportes y Estadísticas (MOD-NF-09).
 *
 * Mapea y conecta directamente las 3 pantallas de Reportes:
 *   CU-96 — Generar informe de alumnos de un curso   → GET /reportes/alumnos
 *   CU-97 — Generar informe de ingresos de un curso  → GET /reportes/ingresos
 *   CU-98 — Consultar estadísticas                   → GET /reportes/estadisticas
 */
@Controller
@RequestMapping("/reportes")
public class ReportesController {

    @Autowired private ReportesService reportesService;
    @Autowired private ReporteAlumnosPdfGenerator pdfAlumnos;
    @Autowired private ReporteIngresosPdfGenerator pdfIngresos;
    @Autowired private ReporteRepository reporteRepository;
    @Autowired private CursoService cursoService;

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    /**
     * CU-96 — Generar informe de alumnos de un curso.
     * Vista: cu-96-generar-informe-de-alumnos-de-un-curso.html
     */
    @GetMapping("/alumnos")
    public String informeAlumnosView(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                    Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("hace30Dias", LocalDate.now().minusDays(30));
        model.addAttribute("titulo", "CU-96 - Generar informe de alumnos de un curso | Idóneos Online");
        return "pages/reportes/cu-96-generar-informe-de-alumnos-de-un-curso";
    }

    @GetMapping("/alumnos/descargar")
    public ResponseEntity<byte[]> descargarInformeAlumnos(
            @RequestParam Integer cursoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        Usuario u = (auth != null && auth.getPrincipal() instanceof Usuario) ? (Usuario) auth.getPrincipal() : null;
        try {
            DatosInformeAlumnosDTO datos = reportesService.obtenerDatosInformeAlumnos(cursoId, desde, hasta);
            String nombreAdmin = (u != null) ? u.getNombreCompleto() : "Administrador";
            byte[] pdfBytes = pdfAlumnos.generar(datos, nombreAdmin);

            if (u != null) {
                reportesService.registrarReporteAlumnos(u.getId());
            }

            String filename = "informe_alumnos_curso_" + cursoId + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * CU-97 — Generar informe de ingresos de un curso.
     * Vista: cu-97-generar-informe-de-ingresos-de-un-curso.html
     */
    @GetMapping("/ingresos")
    public String informeIngresosView(@RequestParam(value = "cursoId", required = false) Integer cursoId,
                                     Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("cursos", cursoService.obtenerTodo());
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("hace30Dias", LocalDate.now().minusDays(30));
        model.addAttribute("titulo", "CU-97 - Generar informe de ingresos de un curso | Idóneos Online");
        return "pages/reportes/cu-97-generar-informe-de-ingresos-de-un-curso";
    }

    @GetMapping("/ingresos/descargar")
    public ResponseEntity<byte[]> descargarInformeIngresos(
            @RequestParam Integer cursoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        Usuario u = (auth != null && auth.getPrincipal() instanceof Usuario) ? (Usuario) auth.getPrincipal() : null;
        try {
            DatosInformeIngresosDTO datos = reportesService.obtenerDatosInformeIngresos(cursoId, desde, hasta);
            String nombreAdmin = (u != null) ? u.getNombreCompleto() : "Administrador";
            byte[] pdfBytes = pdfIngresos.generar(datos, nombreAdmin);

            if (u != null) {
                reportesService.registrarReporteIngresos(u.getId());
            }

            String filename = "informe_ingresos_curso_" + cursoId + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * CU-98 — Consultar estadísticas.
     * Vista: cu-98-consultar-estadisticas.html
     */
    @GetMapping("/estadisticas")
    public String consultarEstadisticas(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        EstadisticasDashboardDTO stats = reportesService.obtenerEstadisticasDashboard();
        List<Reporte> reportes = reporteRepository.findAll();

        model.addAttribute("stats", stats);
        model.addAttribute("reportes", reportes);
        model.addAttribute("titulo", "CU-98 - Consultar estadísticas | Idóneos Online");
        return "pages/reportes/cu-98-consultar-estadisticas";
    }
}
