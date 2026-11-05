package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.tema.TemaService;
import com.app.idoneos.service.temacronograma.TemaCronogramaService;
import com.app.idoneos.util.RespuestaFormularioUtil;

/**
 * Controlador del cronograma (MOD-F-02): CU-23 Buscar cronograma (GET /cronograma/buscar) y CU-24 Modificar cronograma
 * (POST /cronograma/modificar).
 *
 * La página muestra el cronograma del programa por defecto del docente (o el vigente, si lo ve el administrador). El
 * docente (titular o ayudante) lo modifica mientras el programa no tenga inscripciones activas; en ese caso, y para el
 * administrador, se muestra en solo lectura. El alumno ve el cronograma desde la vista de su curso.
 */
@Controller
@RequestMapping("/cronograma")
public class CronogramaController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private com.app.idoneos.service.parametro.ParametroService parametroService;


    /** Mínimo de temas con material que se usa si el parámetro no está cargado. */
    private static final int MINIMO_POR_DEFECTO = 1;

    @Autowired
    private TemaCronogramaService cronogramaService;

    @Autowired
    private TemaService temaService;

    @Autowired
    private com.app.idoneos.repository.ProgramaRepository programaRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-23: Muestra el cronograma del programa de trabajo de un curso, con sus temas, el orden, los días de cada
     * una y el material cargado. Si el docente puede modificarlo, la misma página permite hacerlo (CU-24).
     *
     * @param cursoId            Identificador del curso.
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista del cronograma, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "programaId", required = false) Integer programaId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, (Usuario) authentication.getPrincipal());
            boolean esAdministrador = contexto.esAdministrador();
            Curso curso = contexto.curso();
            // Por defecto, el programa de la cohorte de trabajo; con el selector se mira el de cualquier otro programa del curso.
            // Si la cohorte no tiene programa, el cronograma se muestra vacío
            List<Programa> programasDelCurso = curso.getProgramasActivos();
            Programa programa = programasDelCurso.stream().filter(p -> p.getIdPrograma() == (programaId == null ? -1 : programaId))
                    .findFirst().orElse(contexto.programa());
            Cohorte cohorte = esAdministrador ? null : contexto.participacion().getCohorteDeTrabajo();
            boolean conInscripciones = programa != null && programa.getCantidadInscripcionesActivas() > 0;
            // Todos los temas activos del curso: se pueden poner en este cronograma, estén o no en otro programa
            List<Tema> temasDelCurso = temaService.buscarPorCurso(curso);

            panel.cargarContexto(model, contexto, "cronograma");
            panel.cargarDiasMaximosDelCronograma(model, curso);
            model.addAttribute("programa", programa);
            model.addAttribute("programasDelCurso", programasDelCurso);
            model.addAttribute("cohorte", cohorte);
            model.addAttribute("inscripcion", null);
            model.addAttribute("puedeGestionar", false); // esta vista no lleva el selector de cohorte por defecto
            model.addAttribute("cronograma", programa == null ? List.of() : programa.getCronogramaOrdenado());
            model.addAttribute("temasDelCurso", temasDelCurso);
            model.addAttribute("puedeEditar", !esAdministrador && programa != null && !conInscripciones);
            model.addAttribute("soloLecturaPorInscripciones", !esAdministrador && conInscripciones);
            model.addAttribute("minimoTemasConMaterial", parametroService
                    .obtenerEntero(Parametro.MINIMO_TEMAS_CON_MATERIAL, MINIMO_POR_DEFECTO));
            model.addAttribute("titulo", "Cronograma | Idóneos Online");
            return "pages/cronograma/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-24: Modifica el cronograma de un programa: los temas que lo componen, su orden y sus días. Responde en
     * JSON.
     *
     * @param programaId Identificador del programa.
     * @param ids        Identificadores de los temas, en el orden del cronograma.
     * @param dias       Días de cada tema, en el mismo orden.
     * @param authentication       La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar")
    public ResponseEntity<Map<String, String>> modificar(
            @RequestParam(value = "programaId", required = false) Integer programaId,
            @RequestParam(value = "ids", required = false) List<Integer> ids,
            @RequestParam(value = "dias", required = false) List<String> dias, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            cronogramaService.modificar(usuario.getDocente(), programaId, ids, dias);
            // Alerta guía: con el cronograma completo, lo que sigue es completar el contenido de los temas pendientes
            Integer cursoId = programaRepository.findById(programaId == null ? -1 : programaId).map(p -> p.getCurso().getIdCurso()).orElse(null);
            return cursoId == null ? RespuestaFormularioUtil.armarRespuestaExitosa("Cronograma modificado correctamente.")
                    : panel.exitoConPasoSiguiente("Cronograma modificado correctamente.", cursoId, usuario.getDocente());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }
}
