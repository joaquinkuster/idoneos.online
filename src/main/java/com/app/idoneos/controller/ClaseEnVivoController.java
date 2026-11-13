package com.app.idoneos.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.claseenvivo.ChatClaseService;
import com.app.idoneos.service.claseenvivo.ClaseEnVivoService;
import com.app.idoneos.service.claseenvivo.ClaseEnVivoService.RolEnSala;
import com.app.idoneos.service.claseenvivo.ClaseEnVivoService.SolicitudClaseEnVivo;
import com.app.idoneos.service.streaming.StreamingService;
import com.app.idoneos.service.streaming.StreamingService.DatosDeConexion;
import com.app.idoneos.service.streaming.StreamingService.EstadoDeSenal;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.util.FiltroUtil;
import com.app.idoneos.util.PaginacionUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Controlador de la clase en vivo. */
@Controller
@RequestMapping("/claseEnVivo")
public class ClaseEnVivoController {

    /** Estados de la cohorte en los que se pueden programar clases en vivo. */
    private static final List<String> ESTADOS_CON_CLASES = List.of(com.app.idoneos.model.EstadoCohorte.PROXIMA,
            com.app.idoneos.model.EstadoCohorte.ABIERTA, com.app.idoneos.model.EstadoCohorte.PENDIENTE,
            com.app.idoneos.model.EstadoCohorte.EN_DICTADO);

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private ClaseEnVivoService claseEnVivoService;

    @Autowired
    private ChatClaseService chatService;

    @Autowired
    private StreamingService streamingService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /claseEnVivo/buscar: Lista las clases en vivo de un curso con su cohorte, su tema, su estado y su grabación. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "claseId", required = false) Integer claseId,
            @RequestParam(value = "nuevo", required = false) String nuevo, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
            boolean esAdministrador = contexto.esAdministrador();
            Docente docente = esAdministrador ? null : usuario.getDocente();
            // Listado completo: los filtros se aplican en la pantalla
            List<ClaseEnVivo> clases = claseEnVivoService.buscarConFiltros(cursoId, false, docente);
            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<ClaseEnVivo> pagina = Filtros.tabla.paginarMostrando(clases, request.getParameterMap(),
                    c -> accion != null && claseId != null && c.getIdClaseEnVivo() == claseId);
            PaginacionUtil.cargarEnModelo(model, pagina);
            model.addAttribute("clases", pagina.filas());
            model.addAttribute("puedeGestionar", !esAdministrador);
            model.addAttribute("docenteActual", docente);
            model.addAttribute("anticipacion", claseEnVivoService.minutosDeAnticipacion());
            if (PaginacionUtil.pideSoloFilas(request)) {
                return "pages/claseEnVivo/buscar :: filas"; // solo las filas de la página pedida
            }
            String abrirModal = null;
            boolean hayAccion = accion != null && claseId != null && "editar".equals(accion) && !esAdministrador
                    && clases.stream().anyMatch(c -> c.getIdClaseEnVivo() == claseId && c.esProgramada() && c.esDeDocente(docente));
            if (hayAccion) {
                abrirModal = "modalModificarClase" + claseId;
            } else if (!esAdministrador && nuevo != null) {
                abrirModal = "modalNuevaClase";
            }
            panel.cargarContexto(model, contexto, "clases");
            cargarOpcionesDelFormulario(model, contexto);
            model.addAttribute("abrirModal", abrirModal);
            model.addAttribute("titulo", "Clases en vivo | Idóneos Online");
            return "pages/claseEnVivo/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    /** Carga en el modelo las cohortes y los temas que ofrece el formulario. */
    private void cargarOpcionesDelFormulario(Model model, ContextoCurso contexto) {
        List<Cohorte> cohortes = contexto.curso().getCohortes().stream()
                .filter(c -> !c.getBaja() && c.getFechaInicioDictado() != null && ESTADOS_CON_CLASES.contains(c.getEstado()))
                .sorted(java.util.Comparator.comparing(Cohorte::getFechaInicioDictado).reversed()).toList();
        List<TemaCronograma> temas = panel.temasDelCursoEnOrden(contexto);
        model.addAttribute("cohortes", cohortes);
        model.addAttribute("temas", temas);
    }

    /** GET /claseEnVivo/modificar/{id}: Ventana de modificación de una clase en vivo. */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        ClaseEnVivo clase = claseEnVivoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! La clase en vivo no se encuentra activa."));
        panel.contextoDelPanel(clase.getCohorte().getCurso().getIdCurso(), usuario);
        model.addAttribute("c", clase);
        return "pages/claseEnVivo/modificar :: modal(c=${c})";
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /claseEnVivo/registrar: Programa una clase en vivo para una cohorte. */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(@RequestParam(value = "idCohorte", required = false) Integer idCohorte,
            @RequestParam(value = "idTema", required = false) Integer idTema,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "fechaHoraInicio", required = false) String fechaHoraInicio,
            @RequestParam(value = "fechaHoraCierre", required = false) String fechaHoraCierre, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ClaseEnVivo clase = claseEnVivoService.registrar(usuario.getDocente(),
                    new SolicitudClaseEnVivo(idCohorte, idTema, titulo, fechaHoraInicio, fechaHoraCierre));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Clase en vivo '" + clase.getTitulo() + "' programada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /claseEnVivo/modificar/{id}: Modifica el título, el inicio y el cierre de una clase programada. */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "fechaHoraInicio", required = false) String fechaHoraInicio,
            @RequestParam(value = "fechaHoraCierre", required = false) String fechaHoraCierre, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            claseEnVivoService.modificar(id, usuario.getDocente(),
                    new SolicitudClaseEnVivo(null, null, titulo, fechaHoraInicio, fechaHoraCierre));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Clase en vivo modificada correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /claseEnVivo/cancelar/{id}: Cancela una clase programada que todavía no se transmitió. */
    @PostMapping("/cancelar/{id}")
    public ResponseEntity<Map<String, String>> cancelar(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            claseEnVivoService.cancelar(id, usuario.getDocente());
            return RespuestaFormularioUtil.armarRespuestaExitosa("Clase en vivo cancelada con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /claseEnVivo/cancelarMasiva: Cancela varias clases programadas a la vez, todas o ninguna. */
    @PostMapping("/cancelarMasiva")
    public ResponseEntity<Map<String, String>> cancelarVarias(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            claseEnVivoService.cancelarVarias(ids, usuario.getDocente());
            return RespuestaFormularioUtil.armarRespuestaExitosa("Clases en vivo canceladas con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /claseEnVivo/darDeBaja/{id}: Da de baja una clase, junto con su grabación. Solo el administrador. */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            exigirAdministrador(authentication);
            claseEnVivoService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Clase en vivo dada de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /claseEnVivo/darDeBajaMasiva: Da de baja varias clases a la vez, todas o ninguna. Solo el administrador. */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarias(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication authentication) {
        try {
            exigirAdministrador(authentication);
            claseEnVivoService.darDeBajaVarias(ids);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Clases en vivo dadas de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Transmisión del docente
    // ----------------------------------------------------------------------

    /** Clave con que se guardan en la sesión del docente los datos de conexión de una clase. */
    private static String claveDeConexion(Integer id) {
        return "conexionClase." + id;
    }

    /** POST /claseEnVivo/iniciar/{id}: Inicia la transmisión de una clase programada y lleva al estudio con los datos de conexión. */
    @PostMapping("/iniciar/{id}")
    public ResponseEntity<Map<String, String>> iniciar(@PathVariable("id") Integer id, Authentication authentication,
            HttpSession sesion) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            DatosDeConexion datos = claseEnVivoService.iniciar(id, usuario.getDocente());
            sesion.setAttribute(claveDeConexion(id), datos);
            return RespuestaFormularioUtil.armarRespuestaDeRedireccion("/claseEnVivo/estudio/" + id);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /claseEnVivo/regenerarClave/{id}: Genera una clave de transmisión nueva, y la anterior deja de servir. */
    @PostMapping("/regenerarClave/{id}")
    public ResponseEntity<Map<String, String>> regenerarClave(@PathVariable("id") Integer id, Authentication authentication,
            HttpSession sesion) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            DatosDeConexion datos = claseEnVivoService.regenerarClave(id, usuario.getDocente());
            sesion.setAttribute(claveDeConexion(id), datos);
            return RespuestaFormularioUtil.armarRespuestaDeRedireccion("/claseEnVivo/estudio/" + id);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /claseEnVivo/finalizar/{id}: Finaliza la transmisión y publica la grabación en el tema de la clase. */
    @PostMapping("/finalizar/{id}")
    public ResponseEntity<Map<String, String>> finalizar(@PathVariable("id") Integer id, Authentication authentication,
            HttpSession sesion) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ClaseEnVivo clase = claseEnVivoService.finalizar(id, usuario.getDocente());
            sesion.removeAttribute(claveDeConexion(id));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Clase finalizada. La grabación se está procesando y se va a "
                    + "publicar en el tema '" + clase.getTema().getTitulo() + "' en unos minutos.",
                    "/claseEnVivo/buscar?cursoId=" + clase.getCohorte().getCurso().getIdCurso());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** GET /claseEnVivo/estudio/{id}: Estudio del docente, con la vista previa de la señal, los datos de conexión y el chat. */
    @GetMapping("/estudio/{id}")
    public String estudio(@PathVariable("id") Integer id, Model model, Authentication authentication, HttpSession sesion,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ClaseEnVivo clase = claseEnVivoService.buscarEnVivoDelDocente(id, usuario.getDocente());
            cargarSala(model, clase, usuario, "docente");
            model.addAttribute("minutosSinSenal", claseEnVivoService.minutosDeEsperaSinSenal());
            model.addAttribute("conexion", sesion.getAttribute(claveDeConexion(id)));
            return "pages/claseEnVivo/estudio";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return claseEnVivoService.buscarPorId(id)
                    .map(c -> "redirect:/claseEnVivo/buscar?cursoId=" + c.getCohorte().getCurso().getIdCurso())
                    .orElse("redirect:/curso/buscar");
        }
    }

    // ----------------------------------------------------------------------
    // Sala del alumno
    // ----------------------------------------------------------------------

    /** GET /claseEnVivo/sala/{id}: Sala de la clase en vivo para el alumno, con el video y el chat. */
    @GetMapping("/sala/{id}")
    public String sala(@PathVariable("id") Integer id, Model model, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ClaseEnVivo clase = claseEnVivoService.autorizarIngreso(id, usuario.getAlumno());
            cargarSala(model, clase, usuario, "alumno");
            return "pages/claseEnVivo/sala";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscripcion/buscar";
        }
    }

    /** Carga en el modelo lo que comparten el estudio del docente y la sala del alumno. */
    private void cargarSala(Model model, ClaseEnVivo clase, Usuario usuario, String rol) {
        // La sala se muestra dentro del panel del curso: menú lateral, navbar y migas, como el resto de las pantallas
        ContextoCurso contexto = panel.contextoDeLaTema(clase.getTema(), usuario);
        panel.cargarContexto(model, contexto, "clases");
        model.addAttribute("urlClases", "docente".equals(rol) ? "/claseEnVivo/buscar?cursoId=" + clase.getCohorte().getCurso().getIdCurso()
                : contexto.urlBase());
        model.addAttribute("c", clase);
        model.addAttribute("rol", rol);
        model.addAttribute("urlReproduccion", streamingService.urlDeReproduccion(clase));
        model.addAttribute("tokenReproduccion", streamingService.firmarAcceso(clase, usuario.getIdUsuario()));
        model.addAttribute("chatHabilitado", chatService.estaHabilitado(clase.getIdClaseEnVivo()));
        model.addAttribute("titulo", clase.getTitulo() + " | Idóneos Online");
    }

    /** GET /claseEnVivo/estado/{id}: Informa el estado de la clase y de la señal, para que la sala se actualice sola. */
    @GetMapping("/estado/{id}")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<Map<String, Object>> estado(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            ClaseEnVivo clase = claseDeLaSala(id, authentication);
            RolEnSala rol = rolEnLaSala(clase, authentication);
            EstadoDeSenal senal = clase.esEnVivo() ? streamingService.consultarSenal(clase).orElse(null) : null;
            if (rol == RolEnSala.ALUMNO && clase.esEnVivo()) {
                chatService.registrarPresencia(id, ((Usuario) authentication.getPrincipal()).getIdUsuario());
            }
            Map<String, Object> cuerpo = new java.util.LinkedHashMap<>();
            cuerpo.put("estado", clase.getEstadoClaseEnVivo().getNombre());
            cuerpo.put("enVivo", clase.esEnVivo());
            cuerpo.put("transmitiendo", senal != null && senal.transmitiendo());
            // Se cuentan solo los alumnos que tienen la sala abierta, no los reproductores del docente
            cuerpo.put("conectados", chatService.conectados(id));
            return ResponseEntity.ok(cuerpo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
    }

    // ----------------------------------------------------------------------
    // Chat
    // ----------------------------------------------------------------------

    /** GET /claseEnVivo/chat/{id}/mensajes: Obtiene los últimos mensajes del chat de la clase. */
    @GetMapping("/chat/{id}/mensajes")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<List<ChatClaseService.Mensaje>> mensajes(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            ClaseEnVivo clase = claseDeLaSala(id, authentication);
            rolEnLaSala(clase, authentication);
            return ResponseEntity.ok(chatService.historial(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
    }

    /** GET /claseEnVivo/chat/{id}/eventos: Mantiene abierta la conexión por la que llegan los mensajes nuevos del chat. */
    @GetMapping(value = "/chat/{id}/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter eventos(@PathVariable("id") Integer id, Authentication authentication) {
        ClaseEnVivo clase = claseDeLaSala(id, authentication);
        rolEnLaSala(clase, authentication);
        if (!clase.esEnVivo()) {
            throw new IllegalArgumentException("Error! La clase no se está transmitiendo.");
        }
        return chatService.suscribir(id);
    }

    /** POST /claseEnVivo/chat/{id}/mensajes: Envía un mensaje al chat de la clase. */
    @PostMapping("/chat/{id}/mensajes")
    public ResponseEntity<Map<String, String>> enviarMensaje(@PathVariable("id") Integer id,
            @RequestParam(value = "texto", required = false) String texto, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ClaseEnVivo clase = claseDeLaSala(id, authentication);
            RolEnSala rol = rolEnLaSala(clase, authentication);
            if (!clase.esEnVivo()) {
                throw new IllegalArgumentException("Error! La clase no se está transmitiendo.");
            }
            chatService.enviar(id, new ChatClaseService.Autor(usuario.getIdUsuario(), nombreEnElChat(usuario), usuario.getUrlImagen(),
                    usuario.getIniciales(), rol == RolEnSala.DOCENTE), texto);
            return ResponseEntity.ok(Map.of());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /claseEnVivo/chat/{id}/eliminar/{idMensaje}: Elimina un mensaje del chat. Solo el docente de la clase. */
    @PostMapping("/chat/{id}/eliminar/{idMensaje}")
    public ResponseEntity<Map<String, String>> eliminarMensaje(@PathVariable("id") Integer id,
            @PathVariable("idMensaje") Long idMensaje, Authentication authentication) {
        try {
            exigirDocenteDeLaSala(id, authentication);
            chatService.eliminar(id, idMensaje);
            return ResponseEntity.ok(Map.of());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /claseEnVivo/chat/{id}/habilitado: Habilita o deshabilita el chat. Solo el docente de la clase. */
    @PostMapping("/chat/{id}/habilitado")
    public ResponseEntity<Map<String, String>> habilitarChat(@PathVariable("id") Integer id,
            @RequestParam(value = "valor", defaultValue = "true") boolean valor, Authentication authentication) {
        try {
            exigirDocenteDeLaSala(id, authentication);
            chatService.habilitar(id, valor);
            return ResponseEntity.ok(Map.of());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Busca la clase de una sala. */
    private ClaseEnVivo claseDeLaSala(Integer id, Authentication authentication) {
        return claseEnVivoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! La clase en vivo no se encuentra disponible."));
    }

    /** Obtiene el papel del usuario en la sala, o falla si no puede participar. */
    private RolEnSala rolEnLaSala(ClaseEnVivo clase, Authentication authentication) {
        return claseEnVivoService.rolEnSala(clase, (Usuario) authentication.getPrincipal())
                .orElseThrow(() -> new IllegalArgumentException("Error! No podés participar en esta clase."));
    }

    private void exigirDocenteDeLaSala(Integer id, Authentication authentication) {
        if (rolEnLaSala(claseDeLaSala(id, authentication), authentication) != RolEnSala.DOCENTE) {
            throw new IllegalArgumentException("Error! Solo el docente de la clase puede moderar el chat.");
        }
    }

    /** Arma el nombre con que se muestra a un usuario en el chat: su nombre y la inicial de su apellido. */
    private String nombreEnElChat(Usuario usuario) {
        String apellido = usuario.getApellido() == null || usuario.getApellido().isBlank() ? "" : " " + usuario.getApellido().trim().charAt(0) + ".";
        return usuario.getNombre().trim() + apellido;
    }

    /** Verifica que quien opera sea el administrador. */
    private void exigirAdministrador(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (!usuario.esAdministradorActivo()) {
            throw new IllegalArgumentException("Error! Solo el administrador puede dar de baja una clase en vivo.");
        }
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ----------------------------------------------------------------------
        // Listado (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<ClaseEnVivo> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<ClaseEnVivo>("titulo", FiltroUtil.Tipo.TEXTO, ClaseEnVivo::getTitulo),
                new FiltroUtil.Columna<ClaseEnVivo>("tema", FiltroUtil.Tipo.LISTA, c -> c.getTema().getTitulo()),
                new FiltroUtil.Columna<ClaseEnVivo>("cohorte", FiltroUtil.Tipo.LISTA, c -> c.getCohorte().getMesAnio()),
                new FiltroUtil.Columna<ClaseEnVivo>("fechaHora", FiltroUtil.Tipo.FECHA, c -> FiltroUtil.formatearFechaHora(c.getFechaHoraInicio())),
                new FiltroUtil.Columna<ClaseEnVivo>("docente", FiltroUtil.Tipo.LISTA,
                        c -> c.getDocenteCurso().getDocente().getUsuario().getNombreCompleto()),
                new FiltroUtil.Columna<ClaseEnVivo>("estado", FiltroUtil.Tipo.LISTA,
                        c -> c.getBaja() ? "Dada de baja" : c.getEstadoClaseEnVivo().getNombre()),
                new FiltroUtil.Columna<ClaseEnVivo>("vencimiento", FiltroUtil.Tipo.FECHA,
                        c -> c.getFechaVencimientoGrabacion() == null ? "" : c.getFechaVencimientoGrabacion().toString()),
                new FiltroUtil.Columna<ClaseEnVivo>("fechaCreacion", FiltroUtil.Tipo.FECHA, c -> FiltroUtil.formatearFechaHora(c.getFechaCreacion()))),
                ClaseEnVivo::getBaja);
    }
}
