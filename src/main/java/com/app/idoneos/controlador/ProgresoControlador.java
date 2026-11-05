package com.app.idoneos.controlador;

import com.app.idoneos.utilidades.FiltroUtilidad;
import com.app.idoneos.utilidades.PaginacionUtilidad;
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

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.servicio.Progreso.DetalleProgreso;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;

/**
 * Controlador del progreso de los alumnos de un curso (MOD-F-03).
 *
 * Mapea la pantalla de CU-55 Buscar progreso (GET /progreso/buscar), para el docente y el administrador. El detalle de
 * cada alumno por tema se despliega debajo de su fila.
 */
@Controller
@RequestMapping("/progreso")
public class ProgresoControlador extends PanelCursoControlador {

    @Autowired
    private ProgresoServicio progresoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-55: Lista los alumnos inscriptos en el curso, de todas sus cohortes, con su avance y, desplegable debajo de
     * cada uno, el detalle por tema. El filtro por alumno se aplica en la pantalla.
     *
     * @param cursoId            Identificador del curso.
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del progreso, o una redirección a la lista de cursos si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarProgresos(@RequestParam(value = "cursoId", required = false) Integer cursoId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest solicitud) {
        try {
            ContextoCurso contexto = contextoDelPanel(cursoId, (Usuario) auth.getPrincipal());
            cargarContexto(modelo, contexto, "progreso");
            List<Inscripcion> inscripciones = progresoServicio.buscarConFiltros(contexto.curso().getIdCurso(), null);
            // El docente entra con el filtro en su cohorte de trabajo; el administrador ve todas
            Integer cohorteSeleccionada = contexto.participacion() == null ? null
                    : contexto.participacion().getCohorteDeTrabajo() == null ? null
                            : contexto.participacion().getCohorteDeTrabajo().getIdCohorte();
            // Entra con el filtro en la cohorte de trabajo; si el navegador pide otra página o cambia el filtro, manda su propia elección
            java.util.Map<String, String[]> parametros = new java.util.HashMap<>(solicitud.getParameterMap());
            if (!PaginacionUtilidad.pideSoloFilas(solicitud) && !parametros.containsKey("cohorte") && cohorteSeleccionada != null) {
                parametros.put("cohorte", new String[] { String.valueOf(cohorteSeleccionada) });
            }
            // El filtro, el orden por columna y la página los resuelve el servidor
            List<Inscripcion> filtradas = FILTROS.filtrar(inscripciones, parametros);
            FILTROS.ordenar(filtradas, parametros);
            PaginacionUtilidad.Pagina<Inscripcion> pagina = PaginacionUtilidad.paginar(filtradas, parametros, FILTROS.opciones(inscripciones));
            PaginacionUtilidad.aModelo(modelo, pagina);
            // El detalle por tema se calcula solo para las filas de la página
            Map<Integer, DetalleProgreso> detalles = new LinkedHashMap<>();
            for (Inscripcion inscripcion : pagina.filas()) {
                detalles.put(inscripcion.getIdInscripcion(),
                        progresoServicio.buscarDetalle(inscripcion.getIdInscripcion()));
            }
            modelo.addAttribute("inscripciones", pagina.filas());
            modelo.addAttribute("detalles", detalles);
            if (PaginacionUtilidad.pideSoloFilas(solicitud)) {
                return "pages/progreso/buscar :: filas"; // solo las filas de la página pedida
            }
            modelo.addAttribute("cohorteSeleccionada", cohorteSeleccionada);
            modelo.addAttribute("cohortes", contexto.curso().getProgramasActivos().stream()
                    .flatMap(programa -> programa.getCohortesActivas().stream()).toList());
            modelo.addAttribute("titulo", "Progreso | Idóneos Online");
            return "pages/progreso/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Listado (filtros y página en el servidor)

    /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
    private static final FiltroUtilidad<Inscripcion> FILTROS = new FiltroUtilidad<>(List.of(
            new FiltroUtilidad.Columna<Inscripcion>("alumno", FiltroUtilidad.Tipo.TEXTO,
                    i -> i.getAlumno().getUsuario().getNombreCompleto() + " " + i.getAlumno().getUsuario().getDni() + " "
                            + i.getAlumno().getUsuario().getCorreo()),
            new FiltroUtilidad.Columna<Inscripcion>("cohorte", FiltroUtilidad.Tipo.LISTA, i -> String.valueOf(i.getCohorte().getIdCohorte())),
            new FiltroUtilidad.Columna<Inscripcion>("temasCompletadas", FiltroUtilidad.Tipo.NUMERO, i -> String.valueOf(i.getTemasCompletadas())),
            new FiltroUtilidad.Columna<Inscripcion>("progreso", FiltroUtilidad.Tipo.NUMERO, i -> String.valueOf(i.getPorcentajeProgreso())),
            new FiltroUtilidad.Columna<Inscripcion>("estado", FiltroUtilidad.Tipo.LISTA, Inscripcion::getEstado),
            new FiltroUtilidad.Columna<Inscripcion>("fechaCreacion", FiltroUtilidad.Tipo.FECHA,
                    i -> i.getFechaCreacion() == null ? "" : i.getFechaCreacion().toString().replace('T', ' '))),
            i -> false);
}
