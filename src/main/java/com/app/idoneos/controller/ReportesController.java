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
 * TRAZABILIDAD — Controller del Módulo de Reportes y Estadísticas.
 *
 * MOD-NF-03: Módulo de Reportes y Estadísticas
 *   CU-96 — Generar informe de alumnos de un curso → GET /admin/reportes/alumnos
 *             Actor: Administrador.
 *             Parámetros: cursoId, desde (YYYY-MM-DD), hasta (YYYY-MM-DD).
 *             Genera PDF con listado de alumnos inscriptos, aprobados y notas del período.
 *             Postcondición: PDF generado, registrado en historial y enviado como descarga.
 *   CU-97 — Generar informe de ingresos de un curso → GET /admin/reportes/ingresos
 *             Actor: Administrador.
 *             Parámetros: cursoId, desde (YYYY-MM-DD), hasta (YYYY-MM-DD).
 *             Genera PDF con pagos acreditados del período.
 *             Postcondición: PDF generado, registrado en historial y enviado como descarga.
 *   CU-98 — Consultar estadísticas (panel ejecutivo) → GET /admin/reportes
 *                                                       GET /admin/estadisticas (JSON)
 *             Actor: Administrador.
 *             Muestra KPIs en tiempo real: cursos, alumnos, ingresos, inscripciones.
 *             El endpoint /admin/estadisticas retorna JSON para consumo asíncrono desde el frontend.
 *
 * NOTAS DE COBERTURA:
 *   CU-96 paso 5: el reporte generado se registra en la tabla Reporte con fecha y usuario responsable.
 *   CU-97 paso 5: ídem CU-96.
 *   CU-98 paso 4: los indicadores se calculan en tiempo real vía ReportesService.obtenerEstadisticasDashboard().
 *   CU-96/CU-97 EX-01: si el curso no existe o no hay datos → ResponseEntity.badRequest().
 *
 * El acceso a /admin/** está restringido a Administrador por SecurityConfig.
 */
@Controller
@RequestMapping("/admin")
public class ReportesController {

    @Autowired private ReportesService reportesService;
    @Autowired private ReporteAlumnosPdfGenerator pdfAlumnos;
    @Autowired private ReporteIngresosPdfGenerator pdfIngresos;
    @Autowired private ReporteRepository reporteRepository;
    @Autowired private CursoServiceImpl cursoService;

    /**
     * TRAZABILIDAD: CU-98 — Consultar estadísticas (Panel principal de Reportes).
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador.
     * Flujo paso 2-4: recupera los KPIs del sistema (cursos, alumnos, ingresos, inscripciones)
     *   y el historial de reportes generados.
     * Flujo paso 5: presenta también los formularios para generar informes (CU-96 y CU-97).
     * Postcondición: panel ejecutivo visible con estadísticas en tiempo real e historial de reportes.
     * NOTA: los KPIs se calculan en ReportesService (no aquí) para mantener la separación de responsabilidades.
     */
    @GetMapping("/reportes")
    public String verReportes(Model model, Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();

        // CU-98 paso 4: KPIs del panel ejecutivo en tiempo real.
        EstadisticasDashboardDTO stats = reportesService.obtenerEstadisticasDashboard();

        // Historial de reportes generados (CU-96 paso 5 + CU-97 paso 5).
        List<Reporte> reportes = reporteRepository.findAll();

        // Lista de cursos para el selector de informe (CU-96 paso 2 + CU-97 paso 2).
        List<Curso> cursos = cursoService.obtenerTodo();

        // Fechas por defecto: último mes.
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

    /**
     * TRAZABILIDAD: CU-96 — Generar informe de alumnos de un curso.
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador. El curso existe. Rango de fechas válido.
     * Flujo paso 2-3: el administrador selecciona curso y rango de fechas desde el panel.
     * Flujo paso 4: el sistema genera el PDF con las 3 vistas (listado, aprobados, notas).
     * Flujo paso 5: registra el reporte en el historial de reportes (tabla Reporte).
     * Postcondición: PDF generado y descargado. Reporte registrado en el historial.
     * EX-CU96-01: curso no encontrado o sin datos en el período → HTTP 400 Bad Request.
     * EX-CU96-02: error interno al generar el PDF → HTTP 500 Internal Server Error.
     */
    @GetMapping("/reportes/alumnos")
    public ResponseEntity<byte[]> generarInformeAlumnos(
            @RequestParam Integer cursoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();

            // CU-96 paso 4: obtener datos estadísticos del período.
            DatosInformeAlumnosDTO datos = reportesService.obtenerDatosInformeAlumnos(cursoId, desde, hasta);

            // CU-96 paso 4: generar PDF con 3 vistas (listado, aprobados, notas).
            String adminNombre = usuario.getNombre() + " " + usuario.getApellido();
            byte[] pdfBytes = pdfAlumnos.generar(datos, adminNombre);

            // CU-96 paso 5: registrar el reporte en el historial.
            reportesService.registrarReporteAlumnos(usuario.getId());

            String nombreArchivo = "informe-alumnos-" + datos.getCurso().getNombre()
                    .replaceAll("[^a-zA-Z0-9\\-]", "_") + "-" + desde + "-" + hasta + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            // EX-CU96-01: curso no encontrado o sin datos.
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // EX-CU96-02: error interno.
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * TRAZABILIDAD: CU-97 — Generar informe de ingresos de un curso.
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador. El curso existe. Rango de fechas válido.
     * Flujo paso 2-3: el administrador selecciona curso y rango de fechas desde el panel.
     * Flujo paso 4: el sistema genera el PDF con 4 vistas (solo pagos acreditados).
     * Flujo paso 5: registra el reporte en el historial de reportes.
     * Postcondición: PDF generado y descargado. Reporte registrado en el historial.
     * EX-CU97-01: curso no encontrado o sin pagos en el período → HTTP 400 Bad Request.
     * EX-CU97-02: error interno al generar el PDF → HTTP 500 Internal Server Error.
     */
    @GetMapping("/reportes/ingresos")
    public ResponseEntity<byte[]> generarInformeIngresos(
            @RequestParam Integer cursoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();

            // CU-97 paso 4: obtener datos estadísticos de ingresos del período.
            DatosInformeIngresosDTO datos = reportesService.obtenerDatosInformeIngresos(cursoId, desde, hasta);

            // CU-97 paso 4: generar PDF con 4 vistas (solo pagos en estado "Acreditado").
            String adminNombre = usuario.getNombre() + " " + usuario.getApellido();
            byte[] pdfBytes = pdfIngresos.generar(datos, adminNombre);

            // CU-97 paso 5: registrar el reporte en el historial.
            reportesService.registrarReporteIngresos(usuario.getId());

            String nombreArchivo = "informe-ingresos-" + datos.getCurso().getNombre()
                    .replaceAll("[^a-zA-Z0-9\\-]", "_") + "-" + desde + "-" + hasta + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (IllegalArgumentException e) {
            // EX-CU97-01: curso no encontrado o sin pagos.
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // EX-CU97-02: error interno.
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * TRAZABILIDAD: CU-98 — Consultar estadísticas (endpoint JSON para consumo asíncrono).
     * Actor: Administrador.
     * Precondición: sesión con rol Administrador.
     * Flujo paso 4: retorna los KPIs del sistema en formato JSON para consumo desde el frontend.
     * Postcondición: respuesta JSON con estadísticas actualizadas en tiempo real.
     */
    @GetMapping("/estadisticas")
    @ResponseBody
    public EstadisticasDashboardDTO obtenerEstadisticas(Authentication auth) {
        // CU-98 paso 4: KPIs en tiempo real para consumo asíncrono (fetch/AJAX desde la vista).
        return reportesService.obtenerEstadisticasDashboard();
    }
}
