package com.app.idoneos.controller;

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

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.service.videoclon.VideoClonService;
import com.app.idoneos.util.FiltroUtil;
import com.app.idoneos.util.PaginacionUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

import jakarta.servlet.http.HttpServletRequest;

/** Controlador de los videos con clon: el docente pide videos en los que su clon dice un guion, y el listado muestra su estado hasta que HeyGen los termina. */
@Controller
@RequestMapping("/videoClon")
public class VideoClonController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private VideoClonService videoClonService;

    @Autowired
    private com.app.idoneos.repository.DocenteRepository docenteRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /videoClon/buscar: Lista los videos con clon de un curso, con su tema y su estado. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "temaId", required = false) Integer temaId, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
            boolean esAdministrador = contexto.esAdministrador();
            Docente docente = esAdministrador ? null : usuario.getDocente();
            List<TemaRecurso> videos = videoClonService.buscarPorCurso(contexto.curso(), docente);
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<TemaRecurso> pagina = Filtros.tabla.paginar(videos, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            model.addAttribute("videos", pagina.filas());
            model.addAttribute("puedeGestionar", !esAdministrador);
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/videoClon/buscar :: filas"; // solo las filas de la página pedida
            }
            panel.cargarContexto(model, contexto, "videosClon");
            model.addAttribute("temas", esAdministrador ? List.of() : panel.temasDelCursoEnOrden(contexto));
            model.addAttribute("temaSeleccionada", temaId);
            // El docente de la sesión puede estar desactualizado (el clon se crea desde el perfil): se lee de la base
            Docente actual = docente == null ? null : docenteRepository.findById(docente.getIdDocente()).orElse(docente);
            model.addAttribute("tieneClon", actual != null && actual.getAvatarId() != null && actual.getVoiceId() != null);
            // Desde "Completar contenido" y el selector de recursos se llega con ?nuevo&temaId=: se abre sola la ventana de alta
            model.addAttribute("abrirModal", !esAdministrador && nuevo != null ? "modalGenerarVideoClon" : null);
            model.addAttribute("titulo", "Videos con clon | Idóneos Online");
            return "pages/videoClon/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /videoClon/generar: Pide el video con el clon del docente. Queda pendiente hasta que HeyGen lo termina. */
    @PostMapping("/generar")
    public ResponseEntity<Map<String, String>> generar(@RequestParam(value = "temaId", required = false) Integer temaId,
            @RequestParam(value = "titulo", required = false) String titulo, @RequestParam(value = "guion", required = false) String guion,
            Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            TemaRecurso video = videoClonService.solicitar(usuario.getDocente(), temaId, titulo, guion);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Video pedido con éxito. Queda pendiente mientras HeyGen lo genera: te avisamos cuando esté listo.",
                    "/videoClon/buscar?cursoId=" + video.getTema().getCurso().getIdCurso());
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
        public static final FiltroUtil<TemaRecurso> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<TemaRecurso>("titulo", FiltroUtil.Tipo.TEXTO, m -> m.getRecurso().getTitulo()),
                new FiltroUtil.Columna<TemaRecurso>("tema", FiltroUtil.Tipo.LISTA, m -> m.getTema().getTitulo()),
                new FiltroUtil.Columna<TemaRecurso>("estado", FiltroUtil.Tipo.LISTA, m -> m.getRecurso().getVideoClon().getEstadoGeneracionIa().getNombre()),
                new FiltroUtil.Columna<TemaRecurso>("docente", FiltroUtil.Tipo.TEXTO, m -> m.getRecurso().getDocente().getUsuario().getNombreCompleto()),
                new FiltroUtil.Columna<TemaRecurso>("fechaCreacion", FiltroUtil.Tipo.FECHA, m -> FiltroUtil.formatearFechaHora(m.getRecurso().getFechaCreacion()))),
                m -> false);
    }
}
