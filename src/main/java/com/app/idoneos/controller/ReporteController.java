package com.app.idoneos.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Reporte;
import com.app.idoneos.model.TipoReporte;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.curso.CursoService;
import com.app.idoneos.service.reporte.ReporteService;
import com.app.idoneos.util.FiltroUtil;
import com.app.idoneos.util.PaginacionUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

import jakarta.servlet.http.HttpServletRequest;

/** Controlador del reporte: el administrador genera los informes de alumnos y de ingresos de un curso y consulta el historial de generaciones. */
@Controller
@RequestMapping("/reporte")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private CursoService cursoService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /reporte/buscar: Lista el historial de informes generados. */
    @GetMapping("/buscar")
    public String buscar(Model model, HttpServletRequest request) {
        List<Reporte> reportes = reporteService.buscarRecientes();
        // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
        PaginacionUtil.Pagina<Reporte> pagina = Filtros.tabla.paginar(reportes, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("reportes", pagina.filas());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/reporte/buscar :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("cursos", cursoService.buscarConFiltros(null, null, null, null, null, "nombre", null));
        model.addAttribute("tipoAlumnos", TipoReporte.INFORME_ALUMNOS);
        model.addAttribute("tipoIngresos", TipoReporte.INFORME_INGRESOS);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("inicioMes", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("titulo", "Reportes | Idóneos Online");
        model.addAttribute("menuActivo", "reportes");
        return "pages/reporte/buscar";
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /reporte/generar: Genera el informe de un curso, lo registra en el historial y lo devuelve para descargar. */
    @PostMapping("/generar")
    public ResponseEntity<?> generar(@RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "cursoId", required = false) Integer idCurso,
            @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta, Authentication authentication) {
        try {
            if (hasta != null && hasta.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Error! La fecha hasta no puede ser posterior a hoy.");
            }
            Usuario actor = (Usuario) authentication.getPrincipal();
            byte[] pdf = reporteService.generarInforme(actor.getAdministrador(), tipo, idCurso, desde, hasta);
            Curso curso = cursoService.buscarPorId(idCurso).orElseThrow();
            String nombre = (TipoReporte.INFORME_ALUMNOS.equals(tipo) ? "informe-alumnos-" : "informe-ingresos-")
                    + curso.getNombre().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "") + "-" + LocalDate.now() + ".pdf";
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre, StandardCharsets.UTF_8).build().toString())
                    .body(pdf);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Listado (motor común)
    // ----------------------------------------------------------------------

    /** Columnas de filtro, orden y página del listado (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Reporte> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Reporte>("fecha", FiltroUtil.Tipo.FECHA, r -> FiltroUtil.formatearFechaHora(r.getFechaGeneracion())),
                new FiltroUtil.Columna<Reporte>("tipo", FiltroUtil.Tipo.LISTA, r -> r.getTipoReporte().getNombre()),
                new FiltroUtil.Columna<Reporte>("curso", FiltroUtil.Tipo.TEXTO, r -> r.getCurso().getNombre()),
                new FiltroUtil.Columna<Reporte>("administrador", FiltroUtil.Tipo.TEXTO,
                        r -> r.getAdministrador().getUsuario().getNombreCompleto() + " " + r.getAdministrador().getUsuario().getCorreo())),
                r -> false);
    }
}
