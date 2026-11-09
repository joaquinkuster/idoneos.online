package com.app.idoneos.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.recurso.RecursoService;
import com.app.idoneos.service.indice.IndiceRecursoService;
import com.app.idoneos.service.ocr.OcrService;
import com.app.idoneos.service.recurso.RecursoService.TipoRecurso;
import com.app.idoneos.util.FiltroUtil;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.util.PaginacionUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador de «Mi biblioteca» del docente: los recursos que cargó, con o sin tema, y que puede compartir con otros docentes. Mapea
 * «Mis recursos»: Buscar (GET /biblioteca/buscar), Registrar (POST /biblioteca/registrar), Modificar (POST /biblioteca/modificar/{id}) y
 * Dar de baja (POST /biblioteca/darDeBaja/{id}).
 */
@Controller
@RequestMapping("/biblioteca")
public class BibliotecaController {

    @Autowired
    private IndiceRecursoService indiceService;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private RecursoService recursoService;

    @Autowired
    private com.app.idoneos.service.curso.CursoService cursoService;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    // ---------------------------------------------------------------- Lecturas

    /**
     * «Biblioteca digital»: un solo listado con los recursos que ve el usuario, agrupados por tipo. El docente ve primero los suyos (públicos
     * y privados) y después los públicos de otros docentes; el administrador ve todos. El filtro, el orden y la página los resuelve el
     * servidor.
     *
     * @param model          El modelo de la vista.
     * @param authentication La autenticación actual.
     * @param request        La solicitud HTTP.
     * @return La vista del listado, o solo sus filas si las pide la propia pantalla.
     */
    @GetMapping("/buscar")
    public String buscar(Model model, Authentication authentication, HttpServletRequest request) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        boolean esAdministrador = usuario.esAdministradorActivo();
        Docente docente = esAdministrador ? null : usuario.getDocente();
        List<Recurso> recursos = esAdministrador ? recursoService.buscarTodosLosRecursos() : recursoService.buscarRecursosVisibles(docente);
        Map<String, String[]> params = request.getParameterMap();
        // Agrupado por tipo salvo que se ordene por otra columna; dentro de cada tipo van primero los del docente
        String campoOrden = request.getParameter("ordenCampo");
        boolean agrupar = campoOrden == null || campoOrden.isBlank() || "titulo".equals(campoOrden) || "tipo".equals(campoOrden);
        Comparator<Recurso> porTipo = Comparator.comparing(Recurso::getTipo, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(recurso -> !esDelDocente(recurso, docente));
        PaginacionUtil.Pagina<Recurso> pagina = Filtros.tabla.paginar(recursos, params, agrupar && !"tipo".equals(campoOrden) ? porTipo : null);
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("agrupar", agrupar);
        // Chips de tipo bajo los filtros y cantidad de recursos de cada tipo en todo el listado filtrado (no solo en la página)
        model.addAttribute("chipsTipos", Filtros.tabla.armarChips(recursos, params, "tipo", Recurso::getTipo, Recurso::getTipo));
        if (agrupar) {
            model.addAttribute("recursosPorTipo", Filtros.tabla.contar(recursos, params, Recurso::getTipo));
        }
        // Qué recursos de la página son del docente (los únicos que modifica) y cuántos temas usan los que puede dar de baja
        Set<Integer> propios = new HashSet<>();
        Map<Integer, Long> usosPorRecurso = new HashMap<>();
        for (Recurso recurso : pagina.filas()) {
            boolean propio = esDelDocente(recurso, docente);
            if (propio) {
                propios.add(recurso.getIdRecurso());
            }
            if (propio || esAdministrador) {
                usosPorRecurso.put(recurso.getIdRecurso(), recursoService.contarUsos(recurso));
            }
        }
        model.addAttribute("recursos", pagina.filas());
        model.addAttribute("propios", propios);
        model.addAttribute("usosPorRecurso", usosPorRecurso);
        model.addAttribute("esAdministrador", esAdministrador);
        // Cursos activos del docente, para «Registrar en un tema» (el administrador no tiene esa acción)
        model.addAttribute("cursosDelDocente", esAdministrador ? List.<com.app.idoneos.model.Curso>of()
                : cursoService.buscarConFiltros(null, null, null, null, null, null, docente).stream().filter(c -> !c.getBaja()).toList());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/biblioteca/buscar :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("tiposDeSubida", TipoRecurso.deSubidaManual());
        model.addAttribute("menuActivo", "biblioteca");
        model.addAttribute("titulo", "Biblioteca digital | Idóneos Online");
        return "pages/biblioteca/buscar";
    }

    /** Indica si el recurso es del docente. */
    private static boolean esDelDocente(Recurso recurso, Docente docente) {
        return docente != null && recurso.getDocente().getIdDocente() == docente.getIdDocente();
    }

    /** El listado de «Explorar biblioteca» se unificó con el de «Mi biblioteca»: se mantiene la dirección y se redirige. */
    @GetMapping("/explorar")
    public String explorar() {
        return "redirect:/biblioteca/buscar";
    }

    /**
     * Muestra un recurso en el visor de la plataforma: el reproductor de una grabación o el visor de páginas de un PDF. No hay forma de
     * descargarlo: se ve únicamente desde la plataforma. Un enlace externo se abre directamente. Lo ve su dueño, cualquier docente si es
     * público y el administrador.
     *
     * @param id             Identificador del recurso.
     * @param model          El modelo de la vista.
     * @param authentication La autenticación actual.
     * @return La vista del recurso, o una redirección al enlace externo.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        Recurso recurso = recursoService.buscarRecursoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!recursoService.puedeVerEnBiblioteca(recurso, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (recurso.esEnlaceExterno()) {
            return "redirect:" + recurso.getRutaArchivo();
        }
        boolean esPropio = usuario.getDocente() != null && recurso.getDocente().getIdDocente() == usuario.getDocente().getIdDocente();
        model.addAttribute("recurso", recurso);
        model.addAttribute("formato", formatoDeVista(recurso));
        model.addAttribute("urlArchivo", "/biblioteca/archivo/" + recurso.getIdRecurso());
        model.addAttribute("esPropio", esPropio);
        model.addAttribute("esAdministrador", usuario.esAdministradorActivo());
        model.addAttribute("menuActivo", "biblioteca");
        model.addAttribute("titulo", recurso.getTitulo() + " | Idóneos Online");
        return "pages/biblioteca/ver";
    }

    /**
     * Entrega el contenido de un recurso a la propia plataforma (al reproductor de video o al visor de PDF), con las mismas protecciones que
     * el de los materiales: solo responde si el pedido lo hace la página del recurso, no se guarda en la caché del navegador ni se sugiere
     * un nombre de archivo. Los videos admiten pedidos por rangos.
     *
     * @param id             Identificador del recurso.
     * @param authentication La autenticación actual.
     * @param request        El pedido HTTP, para controlar de dónde viene.
     * @return El contenido, o 403 / 404 si no puede abrirlo o no existe.
     */
    @GetMapping("/archivo/{id}")
    public ResponseEntity<Resource> abrirArchivo(@PathVariable("id") Integer id, Authentication authentication, HttpServletRequest request) {
        Recurso recurso = recursoService.buscarRecursoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!recursoService.puedeVerEnBiblioteca(recurso, (Usuario) authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!RecursoController.esPedidoDeLaPlataforma(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Path archivo = ManejadorArchivosUtil.resolver(directorioCursos, recurso.getRutaArchivo());
        if (archivo == null || !Files.isRegularFile(archivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        MediaType tipo = switch (formatoDeVista(recurso)) {
            case "video" -> MediaType.parseMediaType("video/" + recurso.getExtension());
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header(HttpHeaders.CACHE_CONTROL, "no-store, private")
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(archivo));
    }

    /** Cómo se muestra el recurso: "enlace", "video", "pdf" o "empty" si no se puede mostrar. */
    private static String formatoDeVista(Recurso recurso) {
        if (recurso.esEnlaceExterno()) {
            return "enlace";
        }
        if (!recurso.tieneArchivo()) {
            return "empty";
        }
        return switch (recurso.getExtension()) {
            case "mp4", "webm" -> "video";
            case "pdf" -> "pdf";
            default -> "empty";
        };
    }

    /**
     * Ventana de modificación de un recurso, que el listado pide al abrirla.
     *
     * @param id             El recurso.
     * @param model          El modelo de la vista.
     * @param authentication La autenticación actual.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        Docente docente = ((Usuario) authentication.getPrincipal()).getDocente();
        Recurso recurso = recursoService.buscarRecursoPorId(id)
                .filter(r -> docente != null && r.getDocente().getIdDocente() == docente.getIdDocente())
                .orElseThrow(() -> new IllegalArgumentException("Error! El recurso no se encuentra disponible."));
        model.addAttribute("r", recurso);
        model.addAttribute("usos", recursoService.contarUsos(recurso)); // si es mayor a cero, el recurso está en uso: no se modifica
        return "pages/biblioteca/modificar :: modal(r=${r}, usos=${usos})";
    }

    /**
     * Entrega el índice del documento (las entradas con su página) al visor y a los formularios.
     *
     * @param id             Identificador del recurso.
     * @param authentication La autenticación actual.
     * @return Las entradas del índice, o una lista vacía si no tiene.
     */
    @GetMapping("/indice/{id}")
    public ResponseEntity<List<IndiceRecursoService.Entrada>> indice(@PathVariable("id") Integer id, Authentication authentication) {
        Recurso recurso = recursoService.buscarRecursoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!recursoService.puedeVerEnBiblioteca(recurso, (Usuario) authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store, private").body(indiceService.buscar(recurso));
    }

    /** Guarda el índice que llegó con el formulario; si se cambió el archivo y no llegó un índice nuevo, el anterior ya no sirve y se borra. */
    private void guardarIndice(Recurso recurso, String indiceJson, MultipartFile archivo) {
        String json = indiceJson == null && archivo != null && !archivo.isEmpty() ? "[]" : indiceJson;
        indiceService.reemplazar(recurso, json);
        // Un PDF escaneado se pone en la cola del OCR (en segundo plano) para que se pueda buscar su texto
        if (archivo != null && !archivo.isEmpty()) {
            ocrService.encolarSiHaceFalta(recurso);
        }
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Registra un recurso en la biblioteca, sin asociarlo a un tema. Responde en JSON.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "autor", required = false) String autor,
            @RequestParam(value = "enlace", required = false) String enlace,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "editorial", required = false) String editorial,
            @RequestParam(value = "paginas", required = false) Integer paginas,
            @RequestParam(value = "fechaPublicacion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPublicacion,
            @RequestParam(value = "duracionMinutos", required = false) Integer duracionMinutos,
            @RequestParam(value = "privado", defaultValue = "true") boolean privado,
            @RequestParam(value = "terminos", defaultValue = "false") boolean terminos,
            @RequestParam(value = "indiceJson", required = false) String indiceJson,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo, Authentication authentication) {
        try {
            Docente docente = ((Usuario) authentication.getPrincipal()).getDocente();
            Recurso recurso = recursoService.subirABiblioteca(docente, tipo,
                    new RecursoService.DatosRecurso(titulo, descripcion, false, autor, enlace, isbn, editorial, paginas,
                            fechaPublicacion, duracionMinutos, privado),
                    archivo, terminos);
            guardarIndice(recurso, indiceJson, archivo);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recurso '" + recurso.getTitulo() + "' registrado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un recurso de la biblioteca. Responde en JSON.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "autor", required = false) String autor,
            @RequestParam(value = "enlace", required = false) String enlace,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "editorial", required = false) String editorial,
            @RequestParam(value = "paginas", required = false) Integer paginas,
            @RequestParam(value = "fechaPublicacion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPublicacion,
            @RequestParam(value = "duracionMinutos", required = false) Integer duracionMinutos,
            @RequestParam(value = "privado", defaultValue = "true") boolean privado,
            @RequestParam(value = "indiceJson", required = false) String indiceJson,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo, Authentication authentication) {
        try {
            Docente docente = ((Usuario) authentication.getPrincipal()).getDocente();
            recursoService.modificarEnBiblioteca(id, docente,
                    new RecursoService.DatosRecurso(titulo, descripcion, false, autor, enlace, isbn, editorial, paginas,
                            fechaPublicacion, duracionMinutos, privado),
                    archivo);
            recursoService.buscarRecursoPorId(id).ifPresent(r -> guardarIndice(r, indiceJson, archivo));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recurso modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * Publica un recurso en la biblioteca digital (lo pasa de privado a público). Se pregunta al final del alta de un recurso en un curso.
     * Responde en JSON.
     */
    @PostMapping("/publicar/{id}")
    public ResponseEntity<Map<String, String>> publicar(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            Docente docente = ((Usuario) authentication.getPrincipal()).getDocente();
            recursoService.publicar(id, docente);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recurso publicado en la biblioteca digital.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja varios recursos de la biblioteca a la vez, todos o ninguno. Responde en JSON.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(@RequestParam(value = "ids", required = false) List<Integer> ids,
            Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            if (usuario.esAdministradorActivo()) {
                recursoService.darDeBajaVariosComoAdministrador(ids);
            } else {
                recursoService.darDeBajaVariosEnBiblioteca(ids, usuario.getDocente());
            }
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recursos dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * Da de baja un recurso de la biblioteca. Responde en JSON.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            if (usuario.esAdministradorActivo()) {
                recursoService.darDeBajaComoAdministrador(id);
            } else {
                recursoService.darDeBajaEnBiblioteca(id, usuario.getDocente());
            }
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recurso dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Columnas de filtro, orden y página del listado «Mis recursos» (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<Recurso> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Recurso>("titulo", FiltroUtil.Tipo.TEXTO, Recurso::getTitulo),
                new FiltroUtil.Columna<Recurso>("tipo", FiltroUtil.Tipo.LISTA, Recurso::getTipo),
                new FiltroUtil.Columna<Recurso>("autor", FiltroUtil.Tipo.TEXTO, Recurso::getAutor),
                new FiltroUtil.Columna<Recurso>("isbn", FiltroUtil.Tipo.TEXTO, r -> r.getBibliografia() != null ? r.getBibliografia().getIsbn() : ""),
                new FiltroUtil.Columna<Recurso>("editorial", FiltroUtil.Tipo.TEXTO,
                        r -> r.getBibliografia() != null && r.getBibliografia().getEditorial() != null ? r.getBibliografia().getEditorial() : ""),
                new FiltroUtil.Columna<Recurso>("fechaPublicacion", FiltroUtil.Tipo.FECHA,
                        r -> r.getBibliografia() != null && r.getBibliografia().getFechaPublicacion() != null
                                ? FiltroUtil.formatearFecha(r.getBibliografia().getFechaPublicacion().atStartOfDay()) : ""),
                new FiltroUtil.Columna<Recurso>("docente", FiltroUtil.Tipo.LISTA, r -> r.getDocente().getUsuario().getNombreCompleto()),
                new FiltroUtil.Columna<Recurso>("biblioteca", FiltroUtil.Tipo.LISTA, r -> r.getPrivado() ? "No" : "Sí"),
                new FiltroUtil.Columna<Recurso>("generadoPorIa", FiltroUtil.Tipo.LISTA, r -> r.getGeneradoPorIa() ? "Sí" : "No"),
                new FiltroUtil.Columna<Recurso>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        r -> FiltroUtil.formatearFechaHora(r.getFechaCreacion()))),
                Recurso::getBaja);
    }
}
