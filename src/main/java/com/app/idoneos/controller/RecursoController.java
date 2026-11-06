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

/**
 * Controlador del ABM de materiales (MOD-F-02): CU-28 Buscar material (GET /recurso/buscar), CU-29 Subir material
 * (POST /recurso/subir), CU-30 Modificar material (POST /recurso/modificar/{id}) y CU-31 Dar de baja material
 * (POST /recurso/darDeBaja/{id} y /recurso/darDeBajaMasiva).
 *
 * El docente (titular o ayudante) gestiona el material de los cursos en los que participa; el administrador puede
 * buscar y dar de baja el de cualquier curso.
 */
@Controller
@RequestMapping("/recurso")
public class RecursoController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private RecursoService materialService;

    /** Carpeta base donde se guardan los archivos de los cursos. */
    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-28: Busca el material de un curso (incluido el no publicado) según el tema, el tipo, el título y si fue
     * generado con IA. Si se indica {@code nuevo}, la página abre sola el formulario para subir un material de ese
     * tipo (es el destino del selector "Agregar recurso o actividad" de la vista del curso).
     *
     * @param cursoId       Identificador del curso.
     * @param temaId      Identificador del tema (opcional).
     * @param tipoId        Identificador del tipo de material (opcional).
     * @param titulo        Parte del título (opcional).
     * @param generadoPorIa Si fue generado con IA (opcional).
     * @param baja          {@code true} solo dados de baja, {@code false} solo activos, vacío todos (opcional).
     * @param nuevo         Identificador del tipo de material a subir (opcional).
     * @param accion        "editar" o "baja": abre sola la ventana de ese material (desde la vista del curso).
     * @param materialId    Identificador del material de la acción (opcional).
     * @param model        El modelo de la vista.
     * @param authentication          La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de búsqueda de materiales, o una redirección a "Mis cursos" si no se puede acceder.
     */
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
            List<TemaCronograma> temas = programa == null ? List.of() : programa.getCronogramaOrdenado();
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
            PaginacionUtil.Pagina<TemaRecurso> pagina = Filtros.tabla.paginar(materiales, request.getParameterMap());
            PaginacionUtil.cargarEnModelo(model, pagina);
            // Cuántos otros temas usan el recurso de cada material de la página: si hay alguno no se modifica ni se da de baja
            Map<Integer, Long> otrosUsosPorMaterial = new java.util.HashMap<>();
            pagina.filas().forEach(m -> otrosUsosPorMaterial.put(m.getIdTemaRecurso(), materialService.contarOtrosUsos(m)));
            model.addAttribute("otrosUsosPorMaterial", otrosUsosPorMaterial);
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

    /**
     * Muestra un material en una vista aparte: el reproductor de una grabación, el visor de páginas de un PDF. No hay
     * forma de descargarlo: se ve únicamente desde la plataforma. Un enlace externo no tiene vista: se
     * redirige directamente a la página. Lo ve quien tiene acceso al material (administrador, docente del curso y
     * alumno con el tema habilitado).
     *
     * @param id     Identificador del material.
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual.
     * @return La vista del material, o una redirección al enlace externo.
     */
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

    /**
     * Entrega el contenido de un material a la propia plataforma (al reproductor de video o al visor de PDF), para que
     * se vea únicamente desde ella. Solo responde si el pedido lo hace la página del material: abrir la dirección en
     * otra pestaña, en un iframe o descargarla directamente se rechaza (el navegador informa de dónde sale el pedido
     * en los encabezados Sec-Fetch-Dest y Sec-Fetch-Site, que una página no puede falsificar). Tampoco se permite
     * guardarlo en la caché del navegador ni se sugiere un nombre de archivo.
     *
     * Ningún mecanismo web puede impedir del todo que alguien capture la pantalla o copie el pedido desde las
     * herramientas del navegador: por eso el visor suma una marca de agua con el nombre de quien mira.
     *
     * Lo reciben el administrador, el docente del curso y el alumno con el tema habilitado. Los videos admiten
     * pedidos por rangos, para poder adelantarlos sin descargarlos enteros.
     *
     * @param id      Identificador del material.
     * @param authentication    La autenticación actual.
     * @param request  El pedido HTTP, para controlar de dónde viene.
     * @return El contenido, o 403 / 404 si no puede abrirlo o no existe.
     */
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

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-29: Sube un material a un tema. Responde en JSON para que el formulario muestre el resultado sin
     * recargar la página.
     *
     * @param temaId       Identificador del tema.
     * @param tipoMaterialId Identificador del tipo de material (Grabación, Bibliografía o Presentación).
     * @param titulo         El título del material.
     * @param oculto         Si el material queda oculto para los alumnos.
     * @param autor          El autor (opcional).
     * @param enlace         El enlace externo (solo bibliografía, alternativo al archivo).
     * @param archivo        El archivo del material.
     * @param authentication           La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
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
            @RequestParam(value = "archivo", required = false) MultipartFile archivo, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            TemaRecurso material = materialService.subir(usuario.getDocente(), temaId, tipo,
                    new RecursoService.DatosRecurso(titulo, descripcion, oculto, autor, enlace, isbn, editorial, paginas,
                            fechaPublicacion, duracionMinutos, privado),
                    archivo, terminos);
            // Se muestra cómo quedó dentro del curso: el panel del curso con el tema del material expandido
            Programa vigente = material.getTema().getCurso().getProgramaVigente();
            String expandir = vigente == null ? "" : vigente.getCronogramaOrdenado().stream()
                    .filter(u -> u.getTema().getIdTema() == material.getTema().getIdTema())
                    .map(u -> "?expandir=" + u.getNumeroOrden()).findFirst().orElse("");
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

    /**
     * Lista los recursos de la biblioteca digital que el docente puede reutilizar en un tema: los suyos y los públicos de otros docentes.
     * Alimenta la pestaña «De la biblioteca» del selector de recursos.
     *
     * @param tipo           El tipo de recurso (opcional: sin tipo trae todos los que se registran a mano).
     * @param authentication La autenticación actual.
     * @return La lista, con los del docente primero.
     */
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

    /** La línea de información que se muestra bajo el título de un recurso reutilizable, según su tipo. */
    private String detalleDeReutilizable(com.app.idoneos.model.Recurso recurso, TipoRecurso tipo) {
        java.util.List<String> partes = new java.util.ArrayList<>();
        switch (tipo) {
            case BIBLIOGRAFIA -> {
                partes.add(recurso.getAutor());
                partes.add(recurso.getBibliografia() == null ? null : recurso.getBibliografia().getIsbn());
            }
            case PRESENTACION -> {
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

    /**
     * Registra en un tema uno o varios recursos de la biblioteca digital (todos o ninguno). Responde en JSON, como el alta.
     *
     * @param temaId         El tema.
     * @param recursoIds     Los recursos de la biblioteca.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito (y la alerta guía si todavía falta contenido) o el mensaje de error.
     */
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
                    .map(u -> "?expandir=" + u.getNumeroOrden()).findFirst().orElse("");
            String mensaje = materiales.size() == 1 ? "Recurso '" + primero.getTitulo() + "' registrado en el tema."
                    : materiales.size() + " recursos registrados en el tema.";
            return panel.exitoConContenidoPendiente(mensaje, "/curso/acceder/" + primero.getTema().getCurso().getIdCurso() + expandir,
                    primero.getTema().getCurso().getIdCurso(), usuario.getDocente());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-30: Modifica un material activo. Responde en JSON.
     *
     * @param id      Identificador del material.
     * @param titulo  El título del material.
     * @param oculto  Si el material queda oculto para los alumnos.
     * @param autor   El autor (opcional).
     * @param enlace  El enlace externo (solo bibliografía, alternativo al archivo; opcional).
     * @param archivo El archivo nuevo (opcional: si no se envía se conserva el actual).
     * @param authentication    La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
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
            @RequestParam(value = "privado", defaultValue = "true") boolean privado,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            materialService.modificar(id, usuario.getDocente(),
                    new RecursoService.DatosRecurso(titulo, descripcion, oculto, autor, enlace, isbn, editorial, paginas,
                            fechaPublicacion, duracionMinutos, privado),
                    archivo);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recurso modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-31: Da de baja un material activo. Responde en JSON.
     *
     * @param id   Identificador del material.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            materialService.darDeBaja(id, docenteQueGestiona(authentication));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Recurso dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * Da de baja varios materiales a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja
     * ninguno. Responde en JSON.
     *
     * @param ids  Identificadores de los materiales seleccionados.
     * @param authentication La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
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

    /**
     * Indica si el pedido lo hace la propia página del material: lo carga el reproductor de video o el visor de PDF
     * (destino "video" o "empty") desde el mismo sitio. Si el pedido es una navegación (destino "document"), un iframe
     * u otro sitio, o el navegador no informa el origen, se rechaza.
     */
    public static boolean esPedidoDeLaPlataforma(HttpServletRequest request) {
        String destino = request.getHeader("Sec-Fetch-Dest");
        String sitio = request.getHeader("Sec-Fetch-Site");
        return "same-origin".equals(sitio) && ("video".equals(destino) || "empty".equals(destino));
    }

    /**
     * Obtiene el docente cuyas participaciones limitan la operación, o {@code null} si la hace el administrador
     * (que puede dar de baja el material de cualquier curso).
     */
    private Docente docenteQueGestiona(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return usuario.esAdministradorActivo() ? null : usuario.getDocente();
    }

    // ---------------------------------------------------------------- Ventana de modificación a pedido

    /**
     * Ventana de modificación de un material, que el listado pide al abrirla (en lugar de dibujar una por fila).
     *
     * @param id     El material.
     * @param model El modelo de la vista.
     * @param authentication   La autenticación actual.
     * @return El fragmento con la ventana.
     */
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
        model.addAttribute("otrosUsos", materialService.contarOtrosUsos(m)); // si es mayor a cero, el recurso está compartido: no se modifica
        model.addAttribute("esDuenio", materialService.esDuenio(m, ((Usuario) authentication.getPrincipal()).getDocente()));
        return "pages/recurso/modificar :: modal(m=${m}, otrosUsos=${otrosUsos}, esDuenio=${esDuenio})";
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Listado (motor común)

        /** Las columnas del listado: lo que se filtra y se ordena en el servidor. */
        public static final FiltroUtil<TemaRecurso> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<TemaRecurso>("titulo", FiltroUtil.Tipo.TEXTO, TemaRecurso::getTitulo),
                new FiltroUtil.Columna<TemaRecurso>("tema", FiltroUtil.Tipo.LISTA, m -> m.getTema().getTitulo()),
                new FiltroUtil.Columna<TemaRecurso>("tipo", FiltroUtil.Tipo.LISTA, TemaRecurso::getTipo),
                new FiltroUtil.Columna<TemaRecurso>("autor", FiltroUtil.Tipo.TEXTO, TemaRecurso::getAutor),
                new FiltroUtil.Columna<TemaRecurso>("docente", FiltroUtil.Tipo.LISTA, m -> m.getRecurso().getDocente().getUsuario().getNombreCompleto()),
                new FiltroUtil.Columna<TemaRecurso>("visibilidad", FiltroUtil.Tipo.LISTA, m -> m.getOculto() ? "Oculto" : "Publicado"),
                new FiltroUtil.Columna<TemaRecurso>("biblioteca", FiltroUtil.Tipo.LISTA, m -> m.getRecurso().getPrivado() ? "Privado" : "Público"),
                new FiltroUtil.Columna<TemaRecurso>("generadoPorIa", FiltroUtil.Tipo.LISTA,
                        m -> m.getRecurso().getGeneradoPorIa() ? "Sí" : "No"),
                new FiltroUtil.Columna<TemaRecurso>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        m -> FiltroUtil.formatearFechaHora(m.getFechaCreacion()))),
                TemaRecurso::getBaja);
    }
}
