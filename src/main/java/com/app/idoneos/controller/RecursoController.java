package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.util.PaginacionUtil;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Modalidad;
import com.app.idoneos.service.recurso.RecursoService.TipoRecurso;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.recurso.RecursoService;
import com.app.idoneos.service.indice.IndiceRecursoService;
import com.app.idoneos.service.resaltado.ResaltadoAlumnoService;
import com.app.idoneos.service.ocr.OCRService;
import com.app.idoneos.model.Recurso;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.app.idoneos.util.RespuestaFormularioUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import com.app.idoneos.util.ManejadorArchivosUtil;
import jakarta.servlet.http.HttpServletRequest;

/** Controlador del recurso. */
@Controller
@RequestMapping("/recurso")
public class RecursoController {

    @Autowired
    private IndiceRecursoService indiceService;

    @Autowired
    private OCRService ocrService;

    @Autowired
    private ResaltadoAlumnoService resaltadoService;

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private RecursoService materialService;

    /** Carpeta base donde se guardan los archivos de los cursos. */
    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /recurso/buscar: Busca el material de un curso (incluido el no publicado) según el tema, el tipo. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "temaId", required = false) Integer temaId,
            @RequestParam(value = "tipo", required = false) String tipoBuscado,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "generadoPorIa", required = false) Boolean generadoPorIa,
            @RequestParam(value = "baja", required = false) Boolean baja,
            @RequestParam(value = "nuevo", required = false) String nuevo,
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "materialId", required = false) Integer materialId, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ContextoCurso contexto = panel.contextoDelPanel(cursoId, usuario);
            boolean esAdministrador = contexto.esAdministrador();
            Docente docente = esAdministrador ? null : usuario.getDocente();
            Programa programa = contexto.programa();
            List<TemaCronograma> temas = panel.temasDelCursoEnOrden(contexto);
            // La grabación solo se ofrece si el curso tiene la modalidad Grabada
            List<TipoRecurso> tiposDeSubida = TipoRecurso.deSubidaManual().stream()
                    .filter(tipo -> tipo != TipoRecurso.GRABACION || contexto.curso().incluyeModalidad(Modalidad.GRABADA))
                    .toList();
            TipoRecurso nuevoTipo = TipoRecurso.deTexto(nuevo);
            boolean nuevoValido = nuevoTipo != null && tiposDeSubida.contains(nuevoTipo);

            List<TemaRecurso> materiales = materialService.buscarConFiltros(cursoId, null, null, null,
                    null, null, docente).stream() // listado completo: los filtros se aplican en la pantalla
                    .filter(m -> !m.getBaja()).toList(); // los materiales dados de baja no se muestran

            String abrirModal = null;
            boolean hayAccion = accion != null && materialId != null
                    && materiales.stream().anyMatch(m -> m.getIdTemaRecurso() == materialId && !m.getBaja());
            if (hayAccion && "baja".equals(accion)) {
                abrirModal = "modalBajaMaterial" + materialId;
            } else if (hayAccion && "editar".equals(accion) && !esAdministrador) {
                abrirModal = "modalModificarMaterial" + materialId;
            }

            // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
            PaginacionUtil.Pagina<TemaRecurso> pagina = Filtros.tabla.paginarMostrando(materiales, request.getParameterMap(),
                    m -> accion != null && materialId != null && m.getIdTemaRecurso() == materialId);
            PaginacionUtil.cargarEnModelo(model, pagina);
            // Cuántos otros temas usan el recurso de cada material de la página: si hay alguno no se modifica ni se da de baja
            panel.cargarContexto(model, contexto, "materiales");
            model.addAttribute("abrirModal", abrirModal);
            model.addAttribute("temas", temas);
            model.addAttribute("materiales", pagina.filas());
            if (PaginacionUtil.pideSoloFilas(request)) {
                model.addAttribute("puedeGestionar", !esAdministrador);
                return "pages/recurso/buscar :: filas"; // solo las filas de la página pedida
            }
            model.addAttribute("tiposMaterial", List.of(TipoRecurso.values()));
            model.addAttribute("tiposDeSubida", tiposDeSubida);
            model.addAttribute("temaSeleccionada", temaId);
            model.addAttribute("tipoSeleccionado", tipoBuscado);
            model.addAttribute("tituloBusqueda", titulo);
            model.addAttribute("iaSeleccionado", generadoPorIa);
            model.addAttribute("bajaSeleccionada", baja);
            model.addAttribute("nuevoTipo", nuevoValido && !esAdministrador ? nuevoTipo.name() : null);
            model.addAttribute("puedeGestionar", !esAdministrador);
            model.addAttribute("titulo", "Recursos | Idóneos Online");
            return "pages/recurso/buscar";
        } catch (IllegalArgumentException e) {
            return panel.volverAMisCursos(redirectAttributes, e);
        }
    }

    /** GET /recurso/ver/{id}: Muestra un material en una vista aparte: el reproductor de una grabación, el visor de páginas de un PDF. */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        TemaRecurso material = materialService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (!materialService.puedeAcceder(material, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (material.getRecurso().esEnlaceExterno()) {
            return "redirect:" + material.getRecurso().getRutaArchivo();
        }

        ContextoCurso contexto = panel.contextoDeLaTema(material.getTema(), usuario);

        panel.cargarContexto(model, contexto, "materiales");
        model.addAttribute("material", material);
        model.addAttribute("tema", material.getTema());
        model.addAttribute("formato", material.getFormatoDeVista());
        model.addAttribute("urlArchivo", "/recurso/archivo/" + material.getIdTemaRecurso());
        model.addAttribute("titulo", material.getTitulo() + " | Idóneos Online");
        return "pages/recurso/ver";
    }

    /** GET /recurso/archivo/{id}: Entrega el contenido de un material a la propia plataforma (al reproductor de video o al visor de PDF). */
    @GetMapping("/archivo/{id}")
    public ResponseEntity<Resource> abrirArchivo(@PathVariable("id") Integer id, Authentication authentication,
            HttpServletRequest request) {
        TemaRecurso material = materialService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!materialService.puedeAcceder(material, (Usuario) authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!esPedidoDeLaPlataforma(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Path archivo = ManejadorArchivosUtil.resolver(directorioCursos, material.getRecurso().getRutaArchivo());
        if (archivo == null || !Files.isRegularFile(archivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        MediaType tipo = switch (material.getFormatoDeVista()) {
            case "video" -> MediaType.parseMediaType("video/" + material.getRecurso().getExtension());
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header(HttpHeaders.CACHE_CONTROL, "no-store, private")
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(archivo));
    }

    /** GET /recurso/indice/{id}: Entrega el índice del documento (las entradas con su página) al visor y a los formularios. */
    @GetMapping("/indice/{id}")
    public ResponseEntity<List<IndiceRecursoService.Entrada>> indice(@PathVariable("id") Integer id, Authentication authentication) {
        TemaRecurso material = materialService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!materialService.puedeAcceder(material, (Usuario) authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store, private").body(indiceService.buscar(material.getRecurso()));
    }

    /** GET /recurso/resaltados/{id}: Las anotaciones (lápiz y marcador) que el alumno hizo sobre el documento, una entrada por página. */
    @GetMapping("/resaltados/{id}")
    public ResponseEntity<List<ResaltadoAlumnoService.Pagina>> resaltados(@PathVariable("id") Integer id, Authentication authentication) {
        TemaRecurso material = materialService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (!materialService.puedeAcceder(material, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        List<ResaltadoAlumnoService.Pagina> paginas = usuario.esAlumnoActivo() && usuario.getAlumno() != null
                ? resaltadoService.buscar(usuario.getAlumno(), material.getRecurso()) : List.of();
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store, private").body(paginas);
    }

    /** POST /recurso/resaltados/{id}/{pagina}: Guarda las anotaciones del alumno en una página del documento, reemplazando las anteriores. */
    @PostMapping("/resaltados/{id}/{pagina}")
    public ResponseEntity<Map<String, String>> guardarResaltados(@PathVariable("id") Integer id, @PathVariable("pagina") Integer pagina,
            @RequestParam(value = "trazos", required = false) String trazos, Authentication authentication) {
        try {
            TemaRecurso material = materialService.buscarPorId(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            Usuario usuario = (Usuario) authentication.getPrincipal();
            if (!materialService.puedeAcceder(material, usuario) || usuario.getAlumno() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            resaltadoService.guardar(usuario.getAlumno(), material.getRecurso(), pagina, trazos);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Anotaciones guardadas.");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Guarda el índice que llegó con el formulario. Si se cambió el archivo y no llegó un índice nuevo, se vacía. */
    private void guardarIndice(Recurso recurso, String indiceJson, MultipartFile archivo) {
        String json = indiceJson == null && archivo != null && !archivo.isEmpty() ? "[]" : indiceJson;
        indiceService.reemplazar(recurso, json);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** GET /recurso/estadoOcr: Devuelve el estado y el progreso del reconocimiento de texto de los recursos pedidos, para refrescar sin recargar. */
    @GetMapping("/estadoOcr")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<Integer, Map<String, Object>> estadoOcr(@RequestParam(value = "ids", required = false) List<Integer> ids) {
        Map<Integer, Map<String, Object>> estados = new java.util.LinkedHashMap<>();
        if (ids == null) {
            return estados;
        }
        ids.stream().limit(100).forEach(id -> materialService.buscarRecursoPorId(id).ifPresent(r -> {
            if (r.getEstadoOcr() != null) {
                Map<String, Object> datos = new java.util.LinkedHashMap<>();
                datos.put("estado", r.getEstadoOcr().getNombre());
                OCRService.Progreso progreso = ocrService.progreso(id);
                if (progreso != null) {
                    datos.put("porcentaje", progreso.porcentaje());
                    if (progreso.segundosRestantes() >= 0) {
                    datos.put("segundos", progreso.segundosRestantes());
                }
                datos.put("fase", progreso.fase());
                }
                estados.put(id, datos);
            }
        }));
        return estados;
    }

    /** POST /recurso/ocrPrevio: Empieza el reconocimiento de texto de un PDF escaneado apenas se elige en el formulario. */
    @PostMapping("/ocrPrevio")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, String> ocrPrevio(@RequestParam(value = "archivo", required = false) MultipartFile archivo) {
        try {
            String token = ocrService.iniciarPrevio(archivo);
            return token == null ? Map.of() : Map.of("token", token);
        } catch (Exception e) {
            return Map.of();
        }
    }

    /** GET /recurso/ocrPrevio/{token}: Devuelve el estado y el progreso del reconocimiento previo. */
    @GetMapping("/ocrPrevio/{token}")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> estadoOcrPrevio(@PathVariable("token") String token) {
        OCRService.Previo previo = ocrService.consultarPrevio(token);
        Map<String, Object> datos = new java.util.LinkedHashMap<>();
        if (previo != null) {
            datos.put("estado", previo.estado());
            if (previo.progreso() != null) {
                datos.put("porcentaje", previo.progreso().porcentaje());
                if (previo.progreso().segundosRestantes() >= 0) {
                    datos.put("segundos", previo.progreso().segundosRestantes());
                }
                datos.put("fase", previo.progreso().fase());
            }
        }
        return datos;
    }

    /** GET /recurso/ocrPrevio/{token}/archivo: Entrega el PDF con su texto ya reconocido, para armar el índice sobre él. */
    @GetMapping("/ocrPrevio/{token}/archivo")
    public ResponseEntity<Resource> archivoOcrPrevio(@PathVariable("token") String token) {
        Path archivo = ocrService.archivoPrevio(token);
        if (archivo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, private")
                .body(new FileSystemResource(archivo));
    }

    /** POST /recurso/subir: Sube un material a un tema. */
    @PostMapping("/subir")
    public ResponseEntity<Map<String, String>> subir(
            @RequestParam(value = "temaId", required = false) Integer temaId,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "oculto", defaultValue = "false") boolean oculto,
            @RequestParam(value = "autor", required = false) String autor,
            @RequestParam(value = "enlace", required = false) String enlace,
            @RequestParam(value = "isbn", required = false) String isbn,
            @RequestParam(value = "editorial", required = false) String editorial,
            @RequestParam(value = "paginas", required = false) Integer paginas,
            @RequestParam(value = "fechaPublicacion", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPublicacion,
            @RequestParam(value = "duracionMinutos", required = false) Integer duracionMinutos,
            @RequestParam(value = "terminos", defaultValue = "false") boolean terminos,
            @RequestParam(value = "privado", defaultValue = "true") boolean privado,
            @RequestParam(value = "indiceJson", required = false) String indiceJson,
            @RequestParam(value = "ocrToken", required = false) String ocrToken,
            @RequestParam(value = "archivo", required = false) MultipartFile archivoSubido, Authentication authentication) {
        try {
            // Si el PDF ya se escaneó al elegirlo, se guarda el que tiene el texto reconocido
            MultipartFile archivo = ocrService.reemplazarPorProcesado(ocrToken, archivoSubido);
            Usuario usuario = (Usuario) authentication.getPrincipal();
            TemaRecurso material = materialService.subir(usuario.getDocente(), temaId, tipo,
                    new RecursoService.DatosRecurso(titulo, descripcion, oculto, autor, enlace, isbn, editorial, paginas,
                            fechaPublicacion, duracionMinutos, privado),
                    archivo, terminos);
            guardarIndice(material.getRecurso(), indiceJson, archivo);
            ocrService.descartarPrevio(ocrToken);
            // Se muestra cómo quedó dentro del curso: el panel del curso con el tema del material expandido
            Programa vigente = material.getTema().getCurso().getProgramaVigente();
            String expandir = vigente == null ? "" : vigente.getCronogramaOrdenado().stream()
                    .filter(u -> u.getTema().getIdTema() == material.getTema().getIdTema())
                    .map(u -> "?expandir=" + u.getTema().getIdTema()).findFirst().orElse("");
            // Alerta guía: si todavía falta contenido, propone seguir completándolo
            ResponseEntity<Map<String, String>> respuesta = panel.exitoConContenidoPendiente("Recurso '" + material.getTitulo() + "' subido con éxito.",
                    "/curso/acceder/" + material.getTema().getCurso().getIdCurso() + expandir,
                    material.getTema().getCurso().getIdCurso(), usuario.getDocente());
            // Al final del alta se pregunta si se publica en la biblioteca digital (las bibliografías ya son públicas)
            if (!material.getRecurso().getPrivado()) {
                return respuesta;
            }
            Map<String, String> cuerpo = new java.util.LinkedHashMap<>(respuesta.getBody());
            cuerpo.put("publicarId", String.valueOf(material.getRecurso().getIdRecurso()));
            return ResponseEntity.ok(cuerpo);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** GET /recurso/reutilizables: Lista los recursos de la biblioteca digital que el docente puede reutilizar en un tema. */
    @GetMapping("/reutilizables")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<Map<String, Object>> reutilizables(@RequestParam(value = "tipo", required = false) String tipo,
            Authentication authentication) {
        Docente docente = ((Usuario) authentication.getPrincipal()).getDocente();
        TipoRecurso buscado = TipoRecurso.deTexto(tipo);
        List<Map<String, Object>> propios = new java.util.ArrayList<>();
        List<Map<String, Object>> ajenos = new java.util.ArrayList<>();
        for (com.app.idoneos.model.Recurso recurso : materialService.buscarRecursosVisibles(docente)) {
            TipoRecurso tipoDelRecurso = TipoRecurso.deTexto(recurso.getTipo());
            if (tipoDelRecurso == null || !tipoDelRecurso.permiteSubidaManual() || (buscado != null && tipoDelRecurso != buscado)) {
                continue;
            }
            boolean propio = recurso.getDocente().getIdDocente() == docente.getIdDocente();
            Map<String, Object> fila = new java.util.LinkedHashMap<>();
            fila.put("id", recurso.getIdRecurso());
            fila.put("titulo", recurso.getTitulo());
            fila.put("tipo", tipoDelRecurso.name());
            fila.put("autor", recurso.getAutor());
            fila.put("isbn", recurso.getBibliografia() == null ? null : recurso.getBibliografia().getIsbn());
            fila.put("detalle", detalleDeReutilizable(recurso, tipoDelRecurso));
            fila.put("propio", propio);
            fila.put("docente", recurso.getDocente().getUsuario().getNombreCompleto());
            (propio ? propios : ajenos).add(fila);
        }
        propios.addAll(ajenos);
        return propios;
    }

    /** Arma la línea de información que se muestra bajo el título de un recurso reutilizable, según su tipo. */
    private String detalleDeReutilizable(com.app.idoneos.model.Recurso recurso, TipoRecurso tipo) {
        java.util.List<String> partes = new java.util.ArrayList<>();
        switch (tipo) {
            case BIBLIOGRAFIA -> {
                partes.add(recurso.getAutor());
                partes.add(recurso.getBibliografia() == null ? null : recurso.getBibliografia().getIsbn());
            }
            case PRESENTACION, RESUMEN -> {
                partes.add(recurso.getAutor());
                Integer paginas = recurso.getMaterialLectura() == null ? null : recurso.getMaterialLectura().getPaginas();
                partes.add(paginas == null ? null : paginas + (paginas == 1 ? " página" : " páginas"));
            }
            case GRABACION -> {
                Integer minutos = recurso.getGrabacion() == null ? null : recurso.getGrabacion().getDuracionMinutos();
                partes.add(minutos == null ? "Video" : String.format("Duración: %d:%02d:00", minutos / 60, minutos % 60));
            }
            case ENLACE -> {
                String url = recurso.getEnlace() == null ? null : recurso.getEnlace().getUrl();
                try {
                    partes.add(url == null ? null : java.net.URI.create(url).getHost());
                } catch (IllegalArgumentException e) {
                    partes.add(url);
                }
            }
            default -> { }
        }
        return partes.stream().filter(p -> p != null && !p.isBlank()).collect(java.util.stream.Collectors.joining(" · "));
    }

    /** POST /recurso/reutilizar: Registra en un tema uno o varios recursos de la biblioteca digital (todos o ninguno). */
    @PostMapping("/reutilizar")
    public ResponseEntity<Map<String, String>> reutilizar(@RequestParam(value = "temaId", required = false) Integer temaId,
            @RequestParam(value = "recursoIds", required = false) List<Integer> recursoIds, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            List<TemaRecurso> materiales = materialService.reutilizar(usuario.getDocente(), temaId, recursoIds);
            TemaRecurso primero = materiales.get(0);
            Programa vigente = primero.getTema().getCurso().getProgramaVigente();
            String expandir = vigente == null ? "" : vigente.getCronogramaOrdenado().stream()
                    .filter(u -> u.getTema().getIdTema() == primero.getTema().getIdTema())
                    .map(u -> "?expandir=" + u.getTema().getIdTema()).findFirst().orElse("");
            String mensaje = materiales.size() == 1 ? "Recurso '" + primero.getTitulo() + "' registrado en el tema."
                    : materiales.size() + " recursos registrados en el tema.";
            return panel.exitoConContenidoPendiente(mensaje, "/curso/acceder/" + primero.getTema().getCurso().getIdCurso() + expandir,
                    primero.getTema().getCurso().getIdCurso(), usuario.getDocente());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /recurso/modificar/{id}: Cambia la visibilidad de un material en su tema. */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "oculto", defaultValue = "false") boolean oculto, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            materialService.cambiarVisibilidad(id, usuario.getDocente(), oculto);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recurso modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /recurso/darDeBaja/{id}: Da de baja un material activo. */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            materialService.darDeBaja(id, docenteQueGestiona(authentication));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recurso dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /recurso/darDeBajaMasiva: Da de baja varios materiales a la vez, todos o ninguno: si alguno no puede darse de baja. */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication authentication) {
        try {
            materialService.darDeBajaVarios(ids, docenteQueGestiona(authentication));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recursos dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** Indica si el pedido lo hace la propia página del material. */
    public static boolean esPedidoDeLaPlataforma(HttpServletRequest request) {
        String destino = request.getHeader("Sec-Fetch-Dest");
        String sitio = request.getHeader("Sec-Fetch-Site");
        return "same-origin".equals(sitio) && ("video".equals(destino) || "empty".equals(destino));
    }

    /** Obtiene el docente cuyas participaciones limitan la operación. */
    private Docente docenteQueGestiona(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return usuario.esAdministradorActivo() ? null : usuario.getDocente();
    }

    // ----------------------------------------------------------------------
    // Ventana de modificación a pedido
    // ----------------------------------------------------------------------

    /** GET /recurso/modificar/{id}: Ventana de modificación de un material. */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model, Authentication authentication) {
        TemaRecurso m = materialService.buscarPorId(id).filter(x -> !x.getBaja())
                .orElseThrow(() -> new IllegalArgumentException("Error! El recurso no se encuentra disponible."));
        ContextoCurso contexto = panel.contextoDelPanel(m.getCurso().getIdCurso(), (Usuario) authentication.getPrincipal());
        if (contexto.esAdministrador()) {
            throw new IllegalArgumentException("Error! Solo el equipo docente modifica el recurso.");
        }
        model.addAttribute("curso", contexto.curso());
        model.addAttribute("m", m);
        model.addAttribute("usos", materialService.contarUsos(m.getRecurso())); // los datos del recurso no se modifican mientras algún tema lo usa
        return "pages/recurso/modificar :: modal(m=${m}, usos=${usos})";
    }

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ----------------------------------------------------------------------
        // Listado (motor común)
        // ----------------------------------------------------------------------

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<TemaRecurso> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<TemaRecurso>("titulo", FiltroUtil.Tipo.TEXTO, TemaRecurso::getTitulo),
                new FiltroUtil.Columna<TemaRecurso>("tema", FiltroUtil.Tipo.LISTA, m -> m.getTema().getTitulo()),
                new FiltroUtil.Columna<TemaRecurso>("tipo", FiltroUtil.Tipo.LISTA, TemaRecurso::getTipo),
                new FiltroUtil.Columna<TemaRecurso>("autor", FiltroUtil.Tipo.TEXTO, TemaRecurso::getAutor),
                new FiltroUtil.Columna<TemaRecurso>("docente", FiltroUtil.Tipo.LISTA, m -> m.getRecurso().getDocente().getUsuario().getNombreCompleto()),
                new FiltroUtil.Columna<TemaRecurso>("visibilidad", FiltroUtil.Tipo.LISTA, m -> m.getOculto() ? "Oculto" : "Publicado"),
                new FiltroUtil.Columna<TemaRecurso>("biblioteca", FiltroUtil.Tipo.LISTA, m -> m.getRecurso().getPrivado() ? "No" : "Sí"),
                new FiltroUtil.Columna<TemaRecurso>("generadoPorIa", FiltroUtil.Tipo.LISTA,
                        m -> m.getRecurso().getGeneradoPorIa() ? "Sí" : "No"),
                new FiltroUtil.Columna<TemaRecurso>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        m -> FiltroUtil.formatearFechaHora(m.getFechaCreacion()))),
                TemaRecurso::getBaja);
    }
}
