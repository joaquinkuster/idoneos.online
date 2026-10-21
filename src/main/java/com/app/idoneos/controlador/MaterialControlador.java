package com.app.idoneos.controlador;

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

import com.app.idoneos.servicio.Curso.ContextoCurso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.servicio.Material.MaterialServicioImpl;
import com.app.idoneos.servicio.TipoMaterial.TipoMaterialServicioImpl;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import com.app.idoneos.utilidades.ManejadorArchivosUtilidad;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador del ABM de materiales (MOD-F-02): CU-28 Buscar material (GET /material/buscar), CU-29 Subir material
 * (POST /material/subir), CU-30 Modificar material (POST /material/modificar/{id}) y CU-31 Dar de baja material
 * (POST /material/darDeBaja/{id} y /material/darDeBajaMasiva).
 *
 * El docente (titular o ayudante) gestiona el material de los cursos en los que participa; el administrador puede
 * buscar y dar de baja el de cualquier curso.
 */
@Controller
@RequestMapping("/material")
public class MaterialControlador extends PanelCursoControlador {

    @Autowired
    private MaterialServicioImpl materialServicio;

    @Autowired
    private TipoMaterialServicioImpl tipoMaterialServicio;

    /** Carpeta base donde se guardan los archivos de los cursos. */
    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-28: Busca el material de un curso (incluido el no publicado) según la unidad, el tipo, el título y si fue
     * generado con IA. Si se indica {@code nuevo}, la página abre sola el formulario para subir un material de ese
     * tipo (es el destino del selector "Agregar recurso o actividad" de la vista del curso).
     *
     * @param cursoId       Identificador del curso.
     * @param unidadId      Identificador de la unidad (opcional).
     * @param tipoId        Identificador del tipo de material (opcional).
     * @param titulo        Parte del título (opcional).
     * @param generadoPorIa Si fue generado con IA (opcional).
     * @param baja          {@code true} solo dados de baja, {@code false} solo activos, vacío todos (opcional).
     * @param nuevo         Identificador del tipo de material a subir (opcional).
     * @param accion        "editar" o "baja": abre sola la ventana de ese material (desde la vista del curso).
     * @param materialId    Identificador del material de la acción (opcional).
     * @param modelo        El modelo de la vista.
     * @param auth          La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de búsqueda de materiales, o una redirección a "Mis cursos" si no se puede acceder.
     */
    @GetMapping("/buscar")
    public String buscarMateriales(@RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "unidadId", required = false) Integer unidadId,
            @RequestParam(value = "tipoId", required = false) Integer tipoId,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "generadoPorIa", required = false) Boolean generadoPorIa,
            @RequestParam(value = "baja", required = false) Boolean baja,
            @RequestParam(value = "nuevo", required = false) Integer nuevo,
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "materialId", required = false) Integer materialId, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            ContextoCurso contexto = contextoDelPanel(cursoId, usuario);
            boolean esAdministrador = contexto.esAdministrador();
            Docente docente = esAdministrador ? null : usuario.getDocente();
            Programa programa = contexto.programa();
            List<UnidadCronograma> unidades = programa == null ? List.of() : programa.getCronogramaOrdenado();
            List<TipoMaterial> tiposDeSubida = tipoMaterialServicio.buscarSubidosManualmente();
            boolean nuevoValido = nuevo != null
                    && tiposDeSubida.stream().anyMatch(tipo -> tipo.getIdTipoMaterial() == nuevo);

            List<Material> materiales = materialServicio.buscarConFiltros(cursoId, null, null, null,
                    null, null, docente); // listado completo: los filtros se aplican en la pantalla

            String abrirModal = null;
            boolean hayAccion = accion != null && materialId != null
                    && materiales.stream().anyMatch(m -> m.getIdMaterial() == materialId && !m.getBaja());
            if (hayAccion && "baja".equals(accion)) {
                abrirModal = "modalBajaMaterial" + materialId;
            } else if (hayAccion && "editar".equals(accion) && !esAdministrador) {
                abrirModal = "modalEditarMaterial" + materialId;
            }

            cargarContexto(modelo, contexto, "materiales");
            modelo.addAttribute("abrirModal", abrirModal);
            modelo.addAttribute("unidades", unidades);
            modelo.addAttribute("materiales", materiales);
            modelo.addAttribute("tiposMaterial", tipoMaterialServicio.obtenerTodo());
            modelo.addAttribute("tiposDeSubida", tiposDeSubida);
            modelo.addAttribute("unidadSeleccionada", unidadId);
            modelo.addAttribute("tipoSeleccionado", tipoId);
            modelo.addAttribute("tituloBusqueda", titulo);
            modelo.addAttribute("iaSeleccionado", generadoPorIa);
            modelo.addAttribute("bajaSeleccionada", baja);
            modelo.addAttribute("nuevoTipoId", nuevoValido && !esAdministrador ? nuevo : null);
            modelo.addAttribute("puedeGestionar", !esAdministrador);
            modelo.addAttribute("titulo", "Materiales | Idóneos Online");
            return "pages/material/buscar";
        } catch (IllegalArgumentException e) {
            return volverAMisCursos(redirectAttributes, e);
        }
    }

    /**
     * Muestra un material en una vista aparte: el reproductor de una grabación, el visor de páginas de un PDF. No hay
     * forma de descargarlo: se ve únicamente desde la plataforma. Un enlace externo no tiene vista: se
     * redirige directamente a la página. Lo ve quien tiene acceso al material (administrador, docente del curso y
     * alumno con la unidad habilitada).
     *
     * @param id     Identificador del material.
     * @param modelo El modelo de la vista.
     * @param auth   La autenticación actual.
     * @return La vista del material, o una redirección al enlace externo.
     */
    @GetMapping("/ver/{id}")
    public String verMaterial(@PathVariable("id") Integer id, Model modelo, Authentication auth) {
        Material material = materialServicio.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Usuario usuario = (Usuario) auth.getPrincipal();
        if (!materialServicio.puedeAcceder(material, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (material.esEnlaceExterno()) {
            return "redirect:" + material.getRutaArchivo();
        }

        ContextoCurso contexto = contextoDeLaUnidad(material.getUnidad(), usuario);

        cargarContexto(modelo, contexto, "materiales");
        modelo.addAttribute("material", material);
        modelo.addAttribute("unidad", material.getUnidad());
        modelo.addAttribute("formato", material.getFormatoDeVista());
        modelo.addAttribute("urlArchivo", "/material/archivo/" + material.getIdMaterial());
        modelo.addAttribute("titulo", material.getTitulo() + " | Idóneos Online");
        return "pages/material/ver";
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
     * Lo reciben el administrador, el docente del curso y el alumno con la unidad habilitada. Los videos admiten
     * pedidos por rangos, para poder adelantarlos sin descargarlos enteros.
     *
     * @param id      Identificador del material.
     * @param auth    La autenticación actual.
     * @param pedido  El pedido HTTP, para controlar de dónde viene.
     * @return El contenido, o 403 / 404 si no puede abrirlo o no existe.
     */
    @GetMapping("/archivo/{id}")
    public ResponseEntity<Resource> abrirArchivo(@PathVariable("id") Integer id, Authentication auth,
            HttpServletRequest pedido) {
        Material material = materialServicio.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!materialServicio.puedeAcceder(material, (Usuario) auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!esPedidoDeLaPlataforma(pedido)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Path archivo = ManejadorArchivosUtilidad.resolver(directorioCursos, material.getRutaArchivo());
        if (archivo == null || !Files.isRegularFile(archivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        MediaType tipo = switch (material.getFormatoDeVista()) {
            case "video" -> MediaType.parseMediaType("video/" + material.getExtension());
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
     * CU-29: Sube un material a una unidad. Responde en JSON para que el formulario muestre el resultado sin
     * recargar la página.
     *
     * @param unidadId       Identificador de la unidad.
     * @param tipoMaterialId Identificador del tipo de material (Grabación, Bibliografía o Presentación).
     * @param titulo         El título del material.
     * @param oculto         Si el material queda oculto para los alumnos.
     * @param autor          El autor (opcional).
     * @param enlace         El enlace externo (solo bibliografía, alternativo al archivo).
     * @param archivo        El archivo del material.
     * @param auth           La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/subir")
    public ResponseEntity<Map<String, String>> subirMaterial(
            @RequestParam(value = "unidadId", required = false) Integer unidadId,
            @RequestParam(value = "tipoMaterialId", required = false) Integer tipoMaterialId,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "oculto", defaultValue = "false") boolean oculto,
            @RequestParam(value = "autor", required = false) String autor,
            @RequestParam(value = "enlace", required = false) String enlace,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Material material = materialServicio.subirMaterial(usuario.getDocente(), unidadId, tipoMaterialId, titulo,
                    oculto, autor, enlace, archivo);
            return RespuestaFormularioUtilidad.respuestaExitosa("Material '" + material.getTitulo() + "' subido con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
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
     * @param auth    La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarMaterial(@PathVariable("id") Integer id,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "oculto", defaultValue = "false") boolean oculto,
            @RequestParam(value = "autor", required = false) String autor,
            @RequestParam(value = "enlace", required = false) String enlace,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo, Authentication auth) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            materialServicio.modificarMaterial(id, usuario.getDocente(), titulo, oculto, autor, enlace, archivo);
            return RespuestaFormularioUtilidad.respuestaExitosa("Material modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-31: Da de baja un material activo. Responde en JSON.
     *
     * @param id   Identificador del material.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaMaterial(@PathVariable("id") Integer id, Authentication auth) {
        try {
            materialServicio.darDeBajaMaterial(id, docenteQueGestiona(auth));
            return RespuestaFormularioUtilidad.respuestaExitosa("Material dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Da de baja varios materiales a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja
     * ninguno. Responde en JSON.
     *
     * @param ids  Identificadores de los materiales seleccionados.
     * @param auth La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication auth) {
        try {
            materialServicio.darDeBajaVarios(ids, docenteQueGestiona(auth));
            return RespuestaFormularioUtilidad.respuestaExitosa("Materiales dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Indica si el pedido lo hace la propia página del material: lo carga el reproductor de video o el visor de PDF
     * (destino "video" o "empty") desde el mismo sitio. Si el pedido es una navegación (destino "document"), un iframe
     * u otro sitio, o el navegador no informa el origen, se rechaza.
     */
    private boolean esPedidoDeLaPlataforma(HttpServletRequest pedido) {
        String destino = pedido.getHeader("Sec-Fetch-Dest");
        String sitio = pedido.getHeader("Sec-Fetch-Site");
        return "same-origin".equals(sitio) && ("video".equals(destino) || "empty".equals(destino));
    }

    /**
     * Obtiene el docente cuyas participaciones limitan la operación, o {@code null} si la hace el administrador
     * (que puede dar de baja el material de cualquier curso).
     */
    private Docente docenteQueGestiona(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return usuario.esAdministradorActivo() ? null : usuario.getDocente();
    }
}
