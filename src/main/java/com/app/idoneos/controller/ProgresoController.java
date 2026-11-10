package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.util.PaginacionUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.progreso.ProgresoService.DetalleProgreso;
import com.app.idoneos.service.progreso.ProgresoService;

/** Controlador del progreso. */
@Controller
@RequestMapping("/progreso")
public class ProgresoController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private ProgresoService progresoService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /progreso/buscar: Lista los alumnos inscriptos en el curso, de todas sus cohortes, con su avance y el detalle por tema de cada uno. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, (Usuario) authentication.getPrincipal());
            panel.cargarContexto(model, contexto, "progreso");
            List<Inscripcion> inscripciones = progresoService.buscarConFiltros(contexto.curso().getIdCurso(), null);
            // El docente entra con el filtro en su cohorte de trabajo. El administrador ve todas
            Integer cohorteSeleccionada = contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte();
            // Entra con el filtro en la cohorte de trabajo. Si el navegador pide otra página o cambia el filtro, manda su propia elección
            java.util.Map<String, String[]> parametros = new java.util.HashMap<>(request.getParameterMap());
            if (!PaginacionUtil.pideSoloFilas(request) && !parametros.containsKey("cohorte") && cohorteSeleccionada != null) {
                parametros.put("cohorte", new String[] { String.valueOf(cohorteSeleccionada) });
            }
            // El filtro, el orden por columna y la página los resuelve el servidor
            PaginacionUtil.Pagina<Inscripcion> pagina = Filtros.tabla.paginar(inscripciones, parametros);
            PaginacionUtil.cargarEnModelo(model, pagina);
            // El detalle por tema se calcula solo para las filas de la página
            Map<Integer, DetalleProgreso> detalles = new LinkedHashMap<>();
            for (Inscripcion inscripcion : pagina.filas()) {
                detalles.put(inscripcion.getIdInscripcion(),
                        progresoService.buscarDetalle(inscripcion.getIdInscripcion()));
            }
            model.addAttribute("inscripciones", pagina.filas());
            model.addAttribute("detalles", detalles);
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/progreso/buscar :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("cohorteSeleccionada", cohorteSeleccionada);
            model.addAttribute("cohortes", contexto.curso().getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            model.addAttribute("titulo", "Progreso | Idóneos Online");
            return "pages/progreso/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ----------------------------------------------------------------------
        // Listado (filtros y página en el servidor)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Inscripcion> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Inscripcion>("alumno", FiltroUtil.Tipo.TEXTO,
                        i -> i.getAlumno().getUsuario().getNombreCompleto() + " " + i.getAlumno().getUsuario().getDni() + " "
                                + i.getAlumno().getUsuario().getCorreo()),
                new FiltroUtil.Columna<Inscripcion>("cohorte", FiltroUtil.Tipo.LISTA, i -> String.valueOf(i.getCohorte().getIdCohorte())),
                new FiltroUtil.Columna<Inscripcion>("temasCompletadas", FiltroUtil.Tipo.NUMERO, i -> String.valueOf(i.getTemasCompletadas())),
                new FiltroUtil.Columna<Inscripcion>("progreso", FiltroUtil.Tipo.NUMERO, i -> String.valueOf(i.getPorcentajeProgreso())),
                new FiltroUtil.Columna<Inscripcion>("estado", FiltroUtil.Tipo.LISTA, Inscripcion::getEstado),
                new FiltroUtil.Columna<Inscripcion>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        i -> FiltroUtil.formatearFechaHora(i.getFechaCreacion()))),
                i -> false);
    }
}
