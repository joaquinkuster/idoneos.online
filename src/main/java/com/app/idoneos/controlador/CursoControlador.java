package com.app.idoneos.controlador;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.CursoModalidad;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.utilidades.ImagenUtilidad;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Docente.DocenteServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Nivel.NivelServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;
import com.app.idoneos.utilidades.PaginacionUtilidad;
import com.app.idoneos.utilidades.RespuestaUtilidad;

/**
 * Controlador de la gestión de cursos (MOD-F-01).
 *
 * Mapea las pantallas de los casos de uso:
 * CU-01 Buscar curso (GET /curso/buscar), CU-03 Registrar curso (POST /curso/registrar),
 * CU-04 Modificar curso (POST /curso/modificar/{id}) y CU-05 Dar de baja curso (POST /curso/darDeBaja/{id}).
 * Los formularios de alta, modificación y baja se muestran como ventanas modales de la pantalla de búsqueda.
 */
@Controller
@RequestMapping("/curso")
public class CursoControlador {

    private static final int TAMANIO_PAGINA = 10;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private NivelServicioImpl nivelServicio;

    @Autowired
    private ModalidadServicioImpl modalidadServicio;

    @Autowired
    private DocenteServicioImpl docenteServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    /** Carpeta del servidor donde se guardan las imágenes de los cursos. */
    @Value("${idoneos.directorio-imagenes:./uploads/cursos}")
    private Path directorioImagenes;

    /**
     * CU-01: Busca cursos según nombre, categoría, nivel, equipo docente y modalidad.
     * Si el usuario es docente (y no administrador), el resultado se restringe a los cursos
     * en los que participa como titular o ayudante.
     *
     * @param busqueda          Parte del nombre o la descripción del curso.
     * @param categoriaId       Identificador de la categoría.
     * @param nivelId           Identificador del nivel.
     * @param docenteId         Identificador de un docente del equipo docente.
     * @param modalidadId       Identificador de la modalidad de dictado.
     * @param orden             Orden de los resultados: "nombre" (A–Z) o "recientes". Los dados de baja van al final.
     * @param page              Número de página (empieza en 0).
     * @param porPagina         Cursos por página para el docente (10, 25, 50 o 0 para ver todos).
     * @param modelo            El modelo de la vista.
     * @param auth              La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de búsqueda de cursos.
     */
    @GetMapping("/buscar")
    public String buscarCursos(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "docenteId", required = false) Integer docenteId,
            @RequestParam(value = "modalidadId", required = false) Integer modalidadId,
            @RequestParam(value = "orden", defaultValue = "nombre") String orden,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "porPagina", defaultValue = "" + TAMANIO_PAGINA) int porPagina,
            Model modelo, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            boolean esAdministrador = usuario.esAdministradorActivo();
            Docente soloDeDocente = esAdministrador ? null : usuario.getDocente();

            List<Curso> cursos = cursoServicio.buscarConFiltros(busqueda, categoriaId, nivelId, docenteId,
                    modalidadId, orden, soloDeDocente);
            // El administrador ve el listado completo (la tabla se pagina en la pantalla); el docente, por páginas
            int tamanioPagina = porPagina > 0 ? porPagina : Math.max(cursos.size(), 1);
            int totalPaginas = esAdministrador ? 1 : PaginacionUtilidad.calcularTotalPaginas(cursos.size(), tamanioPagina);
            int pagina = PaginacionUtilidad.ajustarPagina(page, totalPaginas);
            List<Curso> cursosPagina = esAdministrador ? cursos : PaginacionUtilidad.obtenerPagina(cursos, pagina, tamanioPagina);

            // Inscripciones y programas activos por curso, para validar la baja y la modificación
            Map<Integer, List<Inscripcion>> inscripcionesPorCurso = new HashMap<>();
            Map<Integer, List<Programa>> programasPorCurso = new HashMap<>();
            Set<Integer> cursosConInscripcionAbierta = new HashSet<>();
            for (Curso curso : cursosPagina) {
                inscripcionesPorCurso.put(curso.getIdCurso(), cursoServicio.buscarInscripcionesActivas(curso));
                programasPorCurso.put(curso.getIdCurso(), programaServicio.buscarPorCurso(curso));
                if (!cursoServicio.buscarCohortesAbiertas(curso).isEmpty()) {
                    cursosConInscripcionAbierta.add(curso.getIdCurso());
                }
            }

            modelo.addAttribute("cursos", cursosPagina);
            modelo.addAttribute("inscripcionesPorCurso", inscripcionesPorCurso);
            modelo.addAttribute("programasPorCurso", programasPorCurso);
            modelo.addAttribute("cursosConInscripcionAbierta", cursosConInscripcionAbierta);
            modelo.addAttribute("totalCursos", cursos.size());
            modelo.addAttribute("porPagina", porPagina);
            modelo.addAttribute("desdeCurso", cursosPagina.isEmpty() ? 0 : pagina * tamanioPagina + 1);
            modelo.addAttribute("hastaCurso", cursosPagina.isEmpty() ? 0 : pagina * tamanioPagina + cursosPagina.size());
            modelo.addAttribute("currentPage", pagina);
            modelo.addAttribute("totalPages", totalPaginas);
            modelo.addAttribute("categorias", categoriaServicio.obtenerTodo());
            modelo.addAttribute("niveles", nivelServicio.obtenerTodo());
            modelo.addAttribute("modalidades", modalidadServicio.obtenerTodo());
            modelo.addAttribute("docentes", docenteServicio.buscarHabilitados());
            modelo.addAttribute("busqueda", busqueda);
            modelo.addAttribute("categoriaSeleccionada", categoriaId);
            modelo.addAttribute("nivelSeleccionado", nivelId);
            modelo.addAttribute("docenteSeleccionado", docenteId);
            modelo.addAttribute("modalidadSeleccionada", modalidadId);
            modelo.addAttribute("ordenSeleccionado", orden);
            modelo.addAttribute("precioMaximo", (long) Curso.PRECIO_MAXIMO);
            modelo.addAttribute("titulo", "Cursos | Idóneos Online");
            if (esAdministrador) {
                modelo.addAttribute("menuActivo", "cursos");
                return "pages/curso/buscarAdministrador";
            }
            return "pages/curso/buscarDocente";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inicio";
        }
    }

    /**
     * CU-03: Registra un curso con sus modalidades de dictado y su equipo docente. Responde en JSON para
     * que el formulario muestre el resultado sin recargar la página.
     *
     * @param nombre               El nombre del curso.
     * @param descripcion          La descripción del curso (opcional).
     * @param precio               El precio del curso.
     * @param imagenArchivo        La imagen de portada (opcional).
     * @param categoriaId          Identificador de la categoría.
     * @param nivelId              Identificador del nivel.
     * @param emiteCertificado     Si el curso emite certificado al finalizar.
     * @param idsModalidades       Identificadores de las modalidades de dictado.
     * @param docenteTitularId     Identificador del docente titular.
     * @param idsDocentesAyudantes Identificadores de los docentes ayudantes (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarCurso(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "precio", required = false) Float precio,
            @RequestParam(value = "imagenArchivo", required = false) MultipartFile imagenArchivo,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "emiteCertificado", defaultValue = "false") boolean emiteCertificado,
            @RequestParam(value = "idsModalidades", required = false) List<Integer> idsModalidades,
            @RequestParam(value = "docenteTitularId", required = false) Integer docenteTitularId,
            @RequestParam(value = "idsDocentesAyudantes", required = false) List<Integer> idsDocentesAyudantes) {
        String nombreImagen = null;
        try {
            if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                nombreImagen = ImagenUtilidad.guardar(imagenArchivo, directorioImagenes);
            }
            Curso curso = cursoServicio.registrarCurso(nombre, descripcion, precio, nombreImagen, categoriaId, nivelId,
                    emiteCertificado, idsModalidades, docenteTitularId, idsDocentesAyudantes);
            return RespuestaUtilidad.respuestaExitosa("Curso '" + curso.getNombre() + "' registrado con éxito.");
        } catch (Exception e) {
            ImagenUtilidad.eliminar(nombreImagen, directorioImagenes); // evita dejar archivos huérfanos
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-04: Modifica un curso activo. Responde en JSON para que el formulario muestre el resultado
     * sin recargar la página.
     *
     * @param id                   Identificador del curso.
     * @param nombre               El nombre del curso.
     * @param descripcion          La descripción del curso (opcional).
     * @param precio               El precio del curso.
     * @param imagenArchivo        La nueva imagen de portada (opcional; si no se envía se conserva la actual).
     * @param categoriaId          Identificador de la categoría.
     * @param nivelId              Identificador del nivel.
     * @param emiteCertificado     Si el curso emite certificado al finalizar.
     * @param idsModalidades       Identificadores de las modalidades de dictado.
     * @param docenteTitularId     Identificador del docente titular.
     * @param idsDocentesAyudantes Identificadores de los docentes ayudantes (opcional).
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificarCurso(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "precio", required = false) Float precio,
            @RequestParam(value = "imagenArchivo", required = false) MultipartFile imagenArchivo,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "emiteCertificado", defaultValue = "false") boolean emiteCertificado,
            @RequestParam(value = "idsModalidades", required = false) List<Integer> idsModalidades,
            @RequestParam(value = "docenteTitularId", required = false) Integer docenteTitularId,
            @RequestParam(value = "idsDocentesAyudantes", required = false) List<Integer> idsDocentesAyudantes) {
        String nombreImagen = null;
        try {
            String imagenAnterior = cursoServicio.buscarPorId(id).map(Curso::getImagen).orElse(null);
            if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                nombreImagen = ImagenUtilidad.guardar(imagenArchivo, directorioImagenes);
            }
            cursoServicio.modificarCurso(id, nombre, descripcion, precio, nombreImagen, categoriaId, nivelId,
                    emiteCertificado, idsModalidades, docenteTitularId, idsDocentesAyudantes);
            if (nombreImagen != null) {
                ImagenUtilidad.eliminar(imagenAnterior, directorioImagenes); // reemplaza la imagen anterior
            }
            return RespuestaUtilidad.respuestaExitosa("Curso modificado correctamente.");
        } catch (Exception e) {
            ImagenUtilidad.eliminar(nombreImagen, directorioImagenes); // evita dejar archivos huérfanos
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-05: Da de baja un curso que no tenga programas ni unidades activas asociadas. Responde en JSON.
     *
     * @param id Identificador del curso.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaCurso(@PathVariable("id") Integer id) {
        try {
            cursoServicio.darDeBajaCurso(id);
            return RespuestaUtilidad.respuestaExitosa("Curso dado de baja exitosamente.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * Da de baja varios cursos a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja
     * ninguno. Responde en JSON.
     *
     * @param ids Identificadores de los cursos seleccionados.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids) {
        try {
            cursoServicio.darDeBajaVarios(ids);
            return RespuestaUtilidad.respuestaExitosa("Cursos dados de baja exitosamente.");
        } catch (Exception e) {
            return RespuestaUtilidad.respuestaConError(e);
        }
    }

    /**
     * CU-27: Permite al docente acceder a un curso en el que participa, con todas sus unidades habilitadas y sin
     * avance. Se muestra el programa y la cohorte de su contexto de trabajo.
     *
     * @param idCurso            El identificador del curso.
     * @param seccion            La sección a mostrar: "unidades" (por defecto), "cronograma" o "clases".
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Acceder curso", o una redirección a "Mis cursos" si no puede acceder.
     */
    @GetMapping("/acceder/{idCurso}")
    public String accederCurso(@PathVariable("idCurso") int idCurso,
            @RequestParam(value = "seccion", defaultValue = "unidades") String seccion, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            Docente docente = usuario.getDocente();
            if (docente == null) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
            }
            ParticipacionDocente participacion = cursoServicio.validarAccesoDocente(idCurso, docente);
            String seccionActiva = List.of("unidades", "cronograma", "clases").contains(seccion) ? seccion : "unidades";

            modelo.addAttribute("inscripcion", null);
            modelo.addAttribute("curso", participacion.getCurso());
            modelo.addAttribute("programa", participacion.getProgramaDeTrabajo());
            modelo.addAttribute("cohorte", participacion.getCohorteDeTrabajo());
            modelo.addAttribute("esDocente", true);
            modelo.addAttribute("urlBase", "/curso/acceder/" + idCurso);
            modelo.addAttribute("urlMisCursos", "/curso/buscar");
            modelo.addAttribute("seccion", seccionActiva);
            modelo.addAttribute("menuActivo", seccionActiva);
            modelo.addAttribute("titulo", "CU-27 - " + participacion.getCurso().getNombre() + " | Idóneos Online");
            return "pages/curso/acceder";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/curso/buscar";
        }
    }
}
