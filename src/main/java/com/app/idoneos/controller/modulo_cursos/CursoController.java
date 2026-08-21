package com.app.idoneos.controller.modulo_cursos;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;
import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;

import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;
import com.app.idoneos.service.modulo_configuracion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * TRAZABILIDAD — Controller para la navegación, búsqueda del catálogo de cursos e inscripción del alumno.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-01 — Buscar curso (vista alumno)  → GET /cursos (con parámetros busqueda / categoriaId)
 *   CU-02 — Ver mis cursos               → GET /cursos/mis-cursos
 *   CU-06 — Explorar catálogo de cursos → GET /cursos y GET /cursos/{id}
 *
 * MOD-F-02: Módulo de Gestión Académica
 *   CU-26 — Acceder curso (aula virtual) → GET /cursos/{id}/mi-cursada
 *
 * MOD-F-03: Módulo de Inscripciones
 *   CU-43 — Buscar inscripción (alumno)  → GET /cursos/mis-cursos (y GET /certificado/inscripcion/{id})
 *   CU-44 — Inscribir curso              → POST /cursos/{id}/inscribir (con cohorte y verificación de cupo/fechas)
 *   CU-45 — Dar de baja inscripción      → POST /cursos/inscripciones/{id}/baja
 *   CU-48 — Buscar progreso (alumno)     → GET /cursos/{id}/mi-cursada
 */
@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired private CursoServiceImpl cursoService;
    @Autowired private CategoriaServiceImpl categoriaService;
    @Autowired private InscripcionServiceImpl inscripcionService;
    @Autowired private ProgresoServiceImpl progresoService;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private CohorteRepository cohorteRepository;

    /**
     * TRAZABILIDAD: CU-06 — Explorar catálogo de cursos.
     * TRAZABILIDAD: CU-01 — Buscar curso (desde la perspectiva del alumno que navega el catálogo).
     * Actor: Alumno (con o sin sesión).
     * Precondición: existe al menos un curso publicado.
     * Flujo paso 2: lista cursos publicados con opción de filtrar por nombre o categoría.
     * Flujo paso 3-4: aplica filtros y lista resultados.
     * NOTA PARCIAL: CU-06 también especifica filtros por nivel, docente y modalidad. No implementados.
     * NOTA PARCIAL: CU-06 paso 4 muestra cohortes con inscripción abierta; la implementación
     *   muestra cursos publicados sin distinción de cohortes.
     */
    @GetMapping
    public String listarCursos(@RequestParam(value = "categoriaId", required = false) Integer categoriaId,
                               @RequestParam(value = "busqueda", required = false) String busqueda,
                               Model model, Authentication auth) {

        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }

        List<Curso> cursos;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            // CU-01 paso 4: búsqueda por nombre de curso.
            cursos = cursoService.buscarPorNombre(busqueda.trim());
        } else if (categoriaId != null) {
            // CU-01 paso 4: filtro por categoría.
            Optional<Categoria> cat = categoriaService.buscarPorId(categoriaId);
            cursos = cat.map(c -> cursoService.obtenerPorCategoria(c)).orElseGet(() -> cursoService.obtenerPublicados());
        } else {
            // CU-06 paso 2: lista todos los cursos publicados.
            cursos = cursoService.obtenerPublicados();
        }

        model.addAttribute("cursos", cursos);
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("titulo", "Catálogo de Cursos | Idóneos Online");

        return "pages/cursos/catalogo";
    }

    /**
     * TRAZABILIDAD: CU-06 — Explorar catálogo de cursos (ver detalle de un curso seleccionado).
     * Actor: Alumno (con o sin sesión).
     * Flujo paso 3-4: muestra detalle del curso seleccionado.
     * NOTA PARCIAL: la ficha no muestra cohortes con inscripción abierta ni cupo disponible.
     */
    @GetMapping("/{id:\\d+}")
    public String verDetalleCurso(@PathVariable("id") Integer id, Model model, Authentication auth) {
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);
        if (cursoOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cursoOpt.get();
        boolean yaInscripto = false;

        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            Usuario usuario = (Usuario) auth.getPrincipal();
            model.addAttribute("usuario", usuario);
            yaInscripto = inscripcionService.estaInscripto(usuario, curso);
        }

        model.addAttribute("curso", curso);
        model.addAttribute("unidades", unidadService.obtenerPorCurso(curso));
        model.addAttribute("yaInscripto", yaInscripto);
        model.addAttribute("titulo", curso.getNombre() + " | Idóneos Online");

        return "pages/cursos/detalle";
    }

    /**
     * TRAZABILIDAD: CU-02 — Ver mis cursos.
     * Actor: Alumno.
     * Precondición: sesión iniciada con rol Alumno. Posee al menos una inscripción.
     * Flujo paso 4-5: recupera y lista las inscripciones activas del alumno con los cursos correspondientes.
     * EX-CU02-01 (paso 4): si no posee inscripciones, la lista queda vacía (no se muestra mensaje explícito).
     * NOTA PARCIAL: CU-02 especifica filtros por nombre y estado. No implementados.
     * NOTA PARCIAL: CU-02 paso 5 debe mostrar el progreso general de cada curso. Parcialmente implementado.
     */
    @GetMapping("/mis-cursos")
    public String listarMisCursos(Authentication auth, Model model) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";

        Usuario usuario = (Usuario) auth.getPrincipal();
        List<Inscripcion> inscripciones = inscripcionService.obtenerPorAlumno(usuario);

        List<Curso> misCursos = new ArrayList<>();
        for (Inscripcion i : inscripciones) {
            if (!i.getBaja() && i.getCohorte() != null
                    && i.getCohorte().getPrograma() != null
                    && i.getCohorte().getPrograma().getCurso() != null) {
                misCursos.add(i.getCohorte().getPrograma().getCurso());
            }
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("misCursos", misCursos);
        model.addAttribute("titulo", "Mis Cursos | Idóneos Online");

        return "pages/cursos/mis-cursos";
    }

    /**
     * TRAZABILIDAD: CU-44 — Inscribir curso (con selección de cohorte y verificación de cupo/fechas).
     * Actor: Alumno.
     */
    @PostMapping("/{id}/inscribir")
    public String inscribirseACurso(@PathVariable("id") Integer id,
                                    @RequestParam(value = "cohorteId", required = false) Integer cohorteId,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";

        Usuario usuario = (Usuario) auth.getPrincipal();
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);
        if (cursoOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cursoOpt.get();

        if (inscripcionService.estaInscripto(usuario, curso)) {
            return "redirect:/cursos/" + id + "/mi-cursada";
        }

        // CU-44: Si el curso tiene costo y no fue abonado, redirige al flujo de pago
        if (curso.getPrecio() > 0) {
            return "redirect:/pago/checkout/" + id;
        }

        try {
            if (cohorteId != null) {
                Cohorte cohorte = cohorteRepository.findById(cohorteId).orElse(null);
                if (cohorte != null) {
                    inscripcionService.inscribirAlumnoACohorte(usuario, cohorte);
                } else {
                    inscripcionService.inscribirAlumno(usuario, curso);
                }
            } else {
                inscripcionService.inscribirAlumno(usuario, curso);
            }
            redirectAttributes.addFlashAttribute("mensaje", "¡Inscripción exitosa! Ya podés acceder a la cursada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
        }

        return "redirect:/cursos/" + id + "/mi-cursada";
    }

    /**
     * TRAZABILIDAD: CU-45 — Dar de baja inscripción.
     * Actor: Administrador / Alumno.
     */
    @PostMapping("/inscripciones/{inscripcionId}/baja")
    public String darBajaInscripcion(@PathVariable Integer inscripcionId,
                                     @RequestParam(required = false) String motivo,
                                     Authentication auth,
                                     RedirectAttributes ra) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";

        Optional<Inscripcion> iOpt = inscripcionService.buscarPorId(inscripcionId);
        if (iOpt.isEmpty()) return "redirect:/cursos/mis-cursos";

        Inscripcion inscripcion = iOpt.get();
        Usuario usuario = (Usuario) auth.getPrincipal();

        boolean esAlumno = inscripcion.getAlumno() != null && inscripcion.getAlumno().getUsuario().getId() == usuario.getId();
        if (!usuario.esAdmin() && !esAlumno) {
            ra.addFlashAttribute("mensaje", "No tenés permisos para cancelar esta inscripción.");
            return "redirect:/cursos/mis-cursos";
        }

        inscripcionService.darDeBajaInscripcion(inscripcionId);
        ra.addFlashAttribute("mensaje", "Inscripción dada de baja correctamente.");
        return "redirect:/cursos/mis-cursos";
    }

    /**
     * TRAZABILIDAD: CU-26 — Acceder curso (aula virtual del alumno).
     * TRAZABILIDAD: CU-48 — Buscar progreso (seguimiento de progreso por unidad).
     * Actor: Alumno.
     * Precondición: sesión iniciada con rol Alumno. El alumno posee inscripción vigente.
     * Flujo paso 2 (CU-26): el alumno accede al cronograma y contenido de sus unidades.
     * Flujo paso 4 (CU-48): muestra el progreso por unidad con porcentaje de avance.
     * EX-CU26-01: si el alumno no está inscripto, redirige a la ficha del curso con mensaje.
     * NOTA PARCIAL: CU-26 paso 5 verifica habilitación secuencial (unidad anterior aprobada).
     *   La verificación por unidad se hace en la vista, no aquí.
     */
    @GetMapping({"/{id}/mi-cursada", "/{id}/cursada"})
    public String verMiCursada(@PathVariable("id") Integer id, Authentication auth,
                                Model model, RedirectAttributes redirectAttributes) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/login";

        Usuario usuario = (Usuario) auth.getPrincipal();
        Optional<Curso> cursoOpt = cursoService.buscarPorId(id);
        if (cursoOpt.isEmpty()) return "redirect:/cursos";

        Curso curso = cursoOpt.get();
        Optional<Inscripcion> inscripcionOpt = inscripcionService.obtenerPorAlumnoYCurso(usuario, curso);

        if (inscripcionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes inscribirte para acceder a la cursada.");
            return "redirect:/cursos/" + id;
        }

        Inscripcion inscripcion = inscripcionOpt.get();
        List<Unidad> unidades = unidadService.obtenerPorCurso(curso);

        // CU-48 paso 4-5: recupera el progreso del alumno por unidad.
        Map<Integer, Boolean> progresoPorUnidad = new LinkedHashMap<>();
        for (Unidad u : unidades) {
            progresoPorUnidad.put(u.getId(), progresoService.unidadCompletada(inscripcion, u));
        }

        int completadas = progresoService.contarCompletadas(inscripcion);
        int totalUnidades = unidades.size();
        int porcentaje = totalUnidades > 0 ? (completadas * 100 / totalUnidades) : 0;

        model.addAttribute("usuario", usuario);
        model.addAttribute("curso", curso);
        model.addAttribute("inscripcion", inscripcion);
        model.addAttribute("unidades", unidades);
        model.addAttribute("progresoPorUnidad", progresoPorUnidad);
        model.addAttribute("completadas", completadas);
        model.addAttribute("totalUnidades", totalUnidades);
        model.addAttribute("porcentaje", porcentaje);
        model.addAttribute("titulo", "Cursada: " + curso.getNombre());

        return "pages/cursos/mi-cursada";
    }
}

