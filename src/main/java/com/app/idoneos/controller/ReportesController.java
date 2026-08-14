package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.Curso.CursoServiceImpl;
import com.app.idoneos.service.Reportes.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller del Módulo de Reportes y Estadísticas.
 * Rutas bajo /admin/reportes y /admin/estadisticas.
 * Acceso restringido a Administrador por SecurityConfig (/admin/**).
 *
 * Implementa:
 *   CU-87 — Generar informe de alumnos de un curso
 *   CU-88 — Generar informe de ingresos de un curso
 *   CU-89 — Consultar estadísticas (panel ejecutivo)
 */
@Controller
@RequestMapping("/admin")
public class ReportesController {

    @Autowired private ReportesService reportesService;
    @Autowired private ReporteAlumnosPdfGenerator pdfAlumnos;
    @Autowired private ReporteIngresosPdfGenerator pdfIngresos;
    @Autowired private ReporteRepository reporteRepository;
    @Autowired private CursoServiceImpl cursoService;

    // =========================================================
    // CU-89 — Panel principal de Reportes + Estadísticas
    // GET /admin/reportes
    // =========================================================
    /**
     * CU-89 — Consultar estadísticas.
     * Muestra el panel ejecutivo con indicadores en tiempo real y el historial de reportes.
     * También expone el formulario de selección de curso + fechas para CU-87 y CU-88.
     */
    @GetMapping("/reportes")
    public String verReportes(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();

        // CU-89: Indicadores del panel ejecutivo
        EstadisticasDashboardDTO stats = reportesService.obtenerEstadisticasDashboard();

        // Historial de reportes generados (persiste en tabla Reporte)
        List<Reporte> reportes = reporteRepository.findAll();

        // Lista de cursos para el selector de informe
        List<Curso> cursos = cursoService.obtenerTodo();

        // Fechas por defecto: último mes
        LocalDate hoy = LocalDate.now();
        LocalDate hace30Dias = hoy.minusDays(30);

        model.addAttribute("usuario", usuario);
        model.addAttribute("stats", stats);
        model.addAttribute("reportes", reportes);
        model.addAttribute("cursos", cursos);
        model.addAttribute("fechaDesdeDefault", hace30Dias.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("fechaHastaDefault", hoy.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("titulo", "Reportes y Estadísticas | Idóneos Online");
        return "pages/admin/reportes";
    }

    // =========================================================
    // CU-87 — Generar informe de alumnos de un curso
    // GET /admin/reportes/alumnos?cursoId=X&desde=YYYY-MM-DD&hasta=YYYY-MM-DD
    // =========================================================
    /**
     * CU-87 — Generar informe de alumnos de un curso.
     * Genera el PDF con 3 vistas y lo registra en el historial.
     * Responde como descarga directa (application/pdf).
     */
    @GetMapping("/reportes/alumnos")
    public ResponseEntity<byte[]> generarInformeAlumnos(
            @RequestParam Integer cursoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();

            // Obtener datos estadísticos
            DatosInformeAlumnosDTO datos = reportesService.obtenerDatosInformeAlumnos(cursoId, desde, hasta);

            // Generar PDF
            String adminNombre = usuario.getNombre() + " " + usuario.getApellido();
            byte[] pdfBytes = pdfAlumnos.generar(datos, adminNombre);

            // CU-87 paso 5: Registrar el reporte en el historial
            reportesService.registrarReporteAlumnos(usuario.getId());

            // Nombre del archivo de descarga
            String nombreArchivo = "informe-alumnos-" + datos.getCurso().getNombre()
                    .replaceAll("[^a-zA-Z0-9\\-]", "_") + "-" + desde + "-" + hasta + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================================================
    // CU-88 — Generar informe de ingresos de un curso
    // GET /admin/reportes/ingresos?cursoId=X&desde=YYYY-MM-DD&hasta=YYYY-MM-DD
    // =========================================================
    /**
     * CU-88 — Generar informe de ingresos de un curso.
     * Genera el PDF con 4 vistas (solo pagos acreditados) y lo registra en el historial.
     * Responde como descarga directa (application/pdf).
     */
    @GetMapping("/reportes/ingresos")
    public ResponseEntity<byte[]> generarInformeIngresos(
            @RequestParam Integer cursoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();

            // Obtener datos estadísticos
            DatosInformeIngresosDTO datos = reportesService.obtenerDatosInformeIngresos(cursoId, desde, hasta);

            // Generar PDF
            String adminNombre = usuario.getNombre() + " " + usuario.getApellido();
            byte[] pdfBytes = pdfIngresos.generar(datos, adminNombre);

            // CU-88 paso 5: Registrar el reporte en el historial
            reportesService.registrarReporteIngresos(usuario.getId());

            String nombreArchivo = "informe-ingresos-" + datos.getCurso().getNombre()
                    .replaceAll("[^a-zA-Z0-9\\-]", "_") + "-" + desde + "-" + hasta + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =========================================================
    // CU-89 — Estadísticas en JSON (para gráficos asíncronos opcionales)
    // GET /admin/estadisticas
    // =========================================================
    /**
     * CU-89 — Consultar estadísticas.
     * Endpoint JSON auxiliar para consumir los indicadores desde el frontend.
     */
    @GetMapping("/estadisticas")
    @ResponseBody
    public EstadisticasDashboardDTO obtenerEstadisticas(Authentication auth) {
        return reportesService.obtenerEstadisticasDashboard();
    }
}
