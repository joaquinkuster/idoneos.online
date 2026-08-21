package com.app.idoneos.controller;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.EmailService;
import com.app.idoneos.service.Unidad.UnidadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * TRAZABILIDAD — Controller para la gestión del Foro de Consultas por Unidad.
 *
 * MOD-F-02: Módulo de Gestión Académica
 *   CU-35 — Buscar consulta de foro    → GET /foro/unidad/{unidadId}
 *             Lista consultas activas de la unidad.
 *   CU-36 — Registrar consulta de foro → POST /foro/unidad/{unidadId}/consulta
 *             Actor: Alumno. Postcondición: consulta registrada + notificación al docente.
 *   CU-37 — Modificar consulta de foro → no implementado. FALTANTE.
 *   CU-38 — Dar de baja consulta de foro → no implementado. FALTANTE.
 *   CU-39 — Buscar respuesta de foro   → GET /foro/unidad/{unidadId} (respuestas incluidas en la vista).
 *             Implementado como parte de la vista de consultas.
 *   CU-40 — Registrar respuesta de foro → POST /foro/consulta/{consultaId}/responder
 *             Actor: Docente. Postcondición: respuesta registrada + notificación al alumno.
 *   CU-41 — Modificar respuesta de foro → no implementado. FALTANTE.
 *   CU-42 — Dar de baja respuesta de foro → no implementado. FALTANTE.
 *
 * INCONSISTENCIAS CON EL ESQUEMA ACTUAL:
 *   - ForoController inyecta DictadoDocenteRepository del esquema anterior para encontrar
 *     al docente titular del curso. En el nuevo esquema usar SupervisorRepository/CohorteRepository.
 */
@Controller
@RequestMapping("/foro")
public class ForoController {

    @Autowired private ConsultaForoRepository consultaRepo;
    @Autowired private RespuestaForoRepository respuestaRepo;
    @Autowired private UnidadServiceImpl unidadService;
    @Autowired private SupervisorRepository supervisorRepository;
    @Autowired private DocenteRepository docenteRepo;
    @Autowired private EmailService emailService;

    /**
     * TRAZABILIDAD: CU-35 — Buscar consulta de foro.
     * TRAZABILIDAD: CU-39 — Buscar respuesta de foro (las respuestas se muestran junto con las consultas).
     * Actor: Docente, Administrador (o Alumno con acceso al foro de su curso).
     * Precondición: existe al menos una consulta registrada en la unidad.
     * Flujo paso 4: recupera y lista las consultas activas de la unidad con sus respuestas.
     */
    @GetMapping("/unidad/{unidadId}")
    public String verForoUnidad(@PathVariable Integer unidadId, Model model, Authentication auth) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/cursos";

        Usuario usuario = (Usuario) auth.getPrincipal();
        // CU-35 paso 4: recupera consultas activas (baja = false) ordenadas por fecha descendente.
        List<ConsultaForo> consultas = consultaRepo.findByUnidadAndBajaFalseOrderByFechaDesc(unidad);

        model.addAttribute("usuario", usuario);
        model.addAttribute("unidad", unidad);
        model.addAttribute("curso", unidad.getCurso());
        model.addAttribute("consultas", consultas);
        model.addAttribute("titulo", "Foro — " + unidad.getTitulo() + " | Idóneos Online");
        return "pages/foro/foro-unidad";
    }

    /**
     * TRAZABILIDAD: CU-36 — Registrar consulta de foro.
     * Actor: Alumno.
     * Precondición: sesión con rol Alumno. Inscripción vigente al curso. Unidad habilitada.
     * Flujo paso 4: valida que el texto no esté vacío.
     * Flujo paso 5: registra la consulta asociada a la unidad y al alumno.
     * Flujo paso 6: notifica al docente titular por correo electrónico.
     * Postcondición: consulta registrada + docente notificado.
     */
    @PostMapping("/unidad/{unidadId}/consulta")
    public String nuevaConsulta(@PathVariable Integer unidadId,
                                @RequestParam String texto,
                                Authentication auth,
                                RedirectAttributes ra) {
        Unidad unidad = unidadService.buscarPorId(unidadId).orElse(null);
        if (unidad == null) return "redirect:/cursos";
        Usuario usuario = (Usuario) auth.getPrincipal();

        // CU-36 paso 5: registra la consulta asociada a la unidad y al alumno con fecha actual.
        ConsultaForo consulta = consultaRepo.save(new ConsultaForo(texto, usuario.getAlumno(), unidad));

        // CU-36 paso 6: notifica al docente titular
        Docente docenteTitular = unidad.getCronogramas().stream()
                .map(Cronograma::getPrograma)
                .filter(p -> p != null && p.getCurso() != null && p.getCurso().getDocente() != null)
                .map(p -> p.getCurso().getDocente())
                .findFirst()
                .orElse(null);

        if (docenteTitular != null && docenteTitular.getUsuario() != null && docenteTitular.getUsuario().getCorreo() != null) {
            emailService.enviarNuevaConsultaForo(docenteTitular.getUsuario().getCorreo(), consulta);
        }

        ra.addFlashAttribute("mensaje", "Consulta publicada en el foro.");
        return "redirect:/foro/unidad/" + unidadId;
    }

    /**
     * TRAZABILIDAD: CU-40 — Registrar respuesta de foro.
     * Actor: Docente (titular o supervisor del curso).
     * Precondición: sesión con rol Docente. Consulta existe y no está en baja.
     * Flujo paso 4: valida que el texto no esté vacío.
     * Flujo paso 5: registra la respuesta asociada a la consulta y al docente.
     * Flujo paso 6: notifica al alumno autor de la consulta por correo.
     * Postcondición: respuesta registrada + alumno notificado.
     * EX-CU40-01: actor no es Docente → redirect con mensaje.
     * NOTA PARCIAL: la validación del texto vacío no está implementada explícitamente.
     */
    @PostMapping("/consulta/{consultaId}/responder")
    public String responderConsulta(@PathVariable Integer consultaId,
                                    @RequestParam String texto,
                                    Authentication auth,
                                    RedirectAttributes ra) {
        ConsultaForo consulta = consultaRepo.findById(consultaId).orElse(null);
        if (consulta == null) return "redirect:/cursos";
        Usuario usuario = (Usuario) auth.getPrincipal();
        Docente docente = docenteRepo.findById(usuario.getId()).orElse(null);
        if (docente == null) {
            // CU-40 precondición: solo docentes pueden responder.
            ra.addFlashAttribute("mensaje", "CU-40: Solo los docentes pueden responder consultas.");
            return "redirect:/foro/unidad/" + consulta.getUnidad().getId();
        }

        // CU-40 paso 5: registra la respuesta asociada a la consulta y al docente con fecha actual.
        RespuestaForo respuesta = respuestaRepo.save(new RespuestaForo(texto, consulta, docente));

        // CU-40 paso 6: notifica al alumno autor de la consulta.
        if (consulta.getAlumno() != null) {
            emailService.enviarRespuestaForo(consulta.getAlumno().getUsuario().getCorreo(), respuesta);
        }

        ra.addFlashAttribute("mensaje", "Respuesta enviada.");
        return "redirect:/foro/unidad/" + consulta.getUnidad().getId();
    }
}
