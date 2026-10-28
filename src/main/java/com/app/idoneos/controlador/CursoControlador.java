package com.app.idoneos.controlador;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.utilidades.ManejadorArchivosUtilidad;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Docente.DocenteServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Nivel.NivelServicioImpl;
import com.app.idoneos.servicio.Curso.ResumenCursos;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import com.app.idoneos.servicio.TipoMaterial.TipoMaterialServicioImpl;

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
public class CursoControlador extends PanelCursoControlador {

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private NivelServicioImpl nivelServicio;

    @Autowired
    private ModalidadServicioImpl modalidadServicio;

    @Autowired
    private DocenteServicioImpl docenteServicio;

    @Autowired
    private TipoMaterialServicioImpl tipoMaterialServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    /** Carpeta base del servidor donde se guardan los archivos de los cursos. */
    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    // ---------------------------------------------------------------- Lecturas

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
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "id", required = false) Integer id,
            Model modelo, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) auth.getPrincipal();
            boolean esAdministrador = usuario.esAdministradorActivo();
            Docente soloDeDocente = esAdministrador ? null : usuario.getDocente();

            // Se entrega siempre el listado completo: los filtros (también los que llegan por la URL, que solo
            // completan los campos) y la paginación se aplican en la pantalla.
            List<Curso> cursos = cursoServicio.buscarConFiltros(null, null, null, null, null, orden, soloDeDocente);
            // El docente no ve los cursos dados de baja; el administrador sí
            if (!esAdministrador) {
                cursos = cursos.stream().filter(curso -> !curso.esInactivo()).toList();
            }

            // Inscripciones y programas activos por curso, para validar la baja y la modificación
            ResumenCursos resumen = cursoServicio.resumir(cursos);

            modelo.addAttribute("cursos", cursos);
            modelo.addAttribute("inscripcionesPorCurso", resumen.inscripcionesPorCurso());
            modelo.addAttribute("programasPorCurso", resumen.programasPorCurso());
            modelo.addAttribute("cursosConInscripcionAbierta", resumen.cursosConInscripcionAbierta());
            modelo.addAttribute("totalCursos", cursos.size());
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
                // Desde "Mis cursos" se llega con la acción elegida: la ventana correspondiente se abre sola
                String abrirModal = null;
                if ("nuevo".equals(accion)) {
                    abrirModal = "modalNuevoCurso";
                } else if (id != null && "editar".equals(accion)) {
                    abrirModal = "modalEditarCurso" + id;
                } else if (id != null && "baja".equals(accion)) {
                    abrirModal = "modalBajaCurso" + id;
                }
                modelo.addAttribute("abrirModal", abrirModal);
                modelo.addAttribute("menuActivo", "cursos");
                return "pages/curso/buscarAdministrador";
            }
            return "pages/curso/buscar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/inicio";
        }
    }

    /**
     * "Mis cursos" del administrador: la misma vista del docente, con todos los cursos activos de la plataforma, el
     * acceso al panel del curso y atajos al ABM de cursos para editarlo, darlo de baja o registrar uno nuevo. Los cursos dados de baja no
     * se muestran. Los filtros y la paginación se aplican en la pantalla.
     *
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Mis cursos" del administrador, o una redirección si el usuario no es administrador.
     */
    @GetMapping("/misCursos")
    public String misCursosAdministrador(Model modelo, Authentication auth, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        if (!usuario.esAdministradorActivo()) {
            return "redirect:/curso/buscar";
        }
        List<Curso> cursos = cursoServicio.buscarConFiltros(null, null, null, null, null, "nombre", null).stream()
                .filter(curso -> !curso.esInactivo()).toList();
        ResumenCursos resumen = cursoServicio.resumir(cursos);
        modelo.addAttribute("cursos", cursos);
        modelo.addAttribute("totalCursos", cursos.size());
        modelo.addAttribute("programasPorCurso", resumen.programasPorCurso());
        modelo.addAttribute("cursosConInscripcionAbierta", resumen.cursosConInscripcionAbierta());
        modelo.addAttribute("categorias", categoriaServicio.obtenerTodo());
        modelo.addAttribute("niveles", nivelServicio.obtenerTodo());
        modelo.addAttribute("modalidades", modalidadServicio.obtenerTodo());
        modelo.addAttribute("docentes", docenteServicio.buscarHabilitados());
        modelo.addAttribute("esAdministrador", true);
        modelo.addAttribute("titulo", "Mis cursos | Idóneos Online");
        return "pages/curso/buscar";
    }

    /**
     * CU-27: Permite al docente acceder a un curso en el que participa, con todos sus temas habilitados y sin
     * avance. Se muestra el programa y la cohorte de su contexto de trabajo.
     *
     * @param idCurso            El identificador del curso.
     * @param seccion            La sección a mostrar: "temas" (por defecto), "cronograma" o "clases".
     * @param modelo             El modelo de la vista.
     * @param auth               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Acceder curso", o una redirección a "Mis cursos" si no puede acceder.
     */
    @GetMapping("/acceder/{idCurso}")
    public String accederCurso(@PathVariable("idCurso") int idCurso,
            @RequestParam(value = "seccion", defaultValue = "temas") String seccion, Model modelo,
            Authentication auth, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        boolean esAdministrador = false;
        try {
            // Si participa en el curso como docente trabaja como tal; si no, solo el administrador puede entrar
            DocenteCurso participacion = null;
            Docente docente = usuario.getDocente();
            if (docente != null) {
                try {
                    participacion = cursoServicio.validarAccesoDocente(idCurso, docente);
                } catch (IllegalArgumentException e) {
                    if (!usuario.esAdministradorActivo()) {
                        throw e;
                    }
                }
            } else if (!usuario.esAdministradorActivo()) {
                throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
            }
            esAdministrador = participacion == null;
            Curso curso;
            Programa programa;
            Cohorte cohorte;
            if (esAdministrador) {
                // El administrador ve el panel del curso como un docente, sin gestionar su contenido: trabaja con el
                // programa vigente y su cohorte más reciente
                curso = cursoServicio.buscarActivoPorId(idCurso);
                programa = curso.getProgramaVigente();
                cohorte = programa == null ? null : programa.getCohortesActivas().stream().findFirst().orElse(null);
            } else {
                curso = participacion.getCurso();
                programa = participacion.getProgramaDeTrabajo();
                cohorte = participacion.getCohorteDeTrabajo();
            }
            if ("cronograma".equals(seccion)) {
                // El cronograma del docente tiene su propia página, donde también se modifica
                return "redirect:/cronograma/buscar?cursoId=" + idCurso;
            }
            String seccionActiva = List.of("temas", "clases").contains(seccion) ? seccion : "temas";

            String menuActivo = "temas".equals(seccionActiva) ? "curso" : seccionActiva;
            cargarContexto(modelo, esAdministrador ? ContextoCurso.deAdministrador(curso)
                    : ContextoCurso.deDocente(participacion), menuActivo);
            cargarDiasMaximosDelCronograma(modelo);
            modelo.addAttribute("inscripcion", null);
            modelo.addAttribute("cohorte", cohorte);
            // Selectores de programa y cohorte por defecto: solo los activos, el más reciente primero
            modelo.addAttribute("programasDelCurso", curso.getProgramasActivos());
            modelo.addAttribute("cohortesDelPrograma", programa == null ? List.of() : programa.getCohortesActivas());
            if (!esAdministrador) {
                modelo.addAttribute("tiposDeSubida", tipoMaterialServicio.buscarSubidosManualmente());
            }
            // La página del curso se ve como la del docente, también para el administrador (sin gestionar contenido)
            modelo.addAttribute("esDocente", true);
            modelo.addAttribute("puedeGestionar", !esAdministrador);
            modelo.addAttribute("seccion", seccionActiva);
            modelo.addAttribute("titulo", curso.getNombre() + " | Idóneos Online");
            return "pages/curso/acceder";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return usuario.esAdministradorActivo() && usuario.getDocente() == null ? "redirect:/curso/misCursos"
                    : "redirect:/curso/buscar";
        }
    }

    // ---------------------------------------------------------------- Creaciones

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
        try {
            boolean hayImagen = imagenArchivo != null && !imagenArchivo.isEmpty();
            if (hayImagen) {
                ManejadorArchivosUtilidad.validarImagen(imagenArchivo); // antes de registrar, para no crear un curso con imagen inválida
            }
            Curso curso = cursoServicio.registrarCurso(nombre, descripcion, precio, null, categoriaId, nivelId,
                    emiteCertificado, idsModalidades, docenteTitularId, idsDocentesAyudantes);
            if (hayImagen) {
                guardarImagen(curso, imagenArchivo);
            }
            return RespuestaFormularioUtilidad.respuestaExitosa("Curso '" + curso.getNombre() + "' registrado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Modificaciones

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
        try {
            boolean hayImagen = imagenArchivo != null && !imagenArchivo.isEmpty();
            if (hayImagen) {
                ManejadorArchivosUtilidad.validarImagen(imagenArchivo);
            }
            Curso curso = cursoServicio.modificarCurso(id, nombre, descripcion, precio, null, categoriaId, nivelId,
                    emiteCertificado, idsModalidades, docenteTitularId, idsDocentesAyudantes);
            if (hayImagen) {
                guardarImagen(curso, imagenArchivo); // reemplaza la imagen anterior
            }
            return RespuestaFormularioUtilidad.respuestaExitosa("Curso modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-05: Da de baja un curso que no tenga programas ni temas activos asociadas. Responde en JSON.
     *
     * @param id Identificador del curso.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBajaCurso(@PathVariable("id") Integer id) {
        try {
            cursoServicio.darDeBajaCurso(id);
            return RespuestaFormularioUtilidad.respuestaExitosa("Curso dado de baja exitosamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
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
            return RespuestaFormularioUtilidad.respuestaExitosa("Cursos dados de baja exitosamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtilidad.respuestaConError(e);
        }
    }

    /**
     * Guarda la imagen del curso en su carpeta ({@code curso{id}/imagen.{ext}}), reemplazando la anterior, y
     * registra su nombre en el curso.
     */
    private void guardarImagen(Curso curso, MultipartFile imagenArchivo) {
        String nombre;
        try {
            nombre = ManejadorArchivosUtilidad.guardarImagen(imagenArchivo,
                    ManejadorArchivosUtilidad.carpetaDeCurso(directorioCursos, curso.getIdCurso()), "imagen");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Error! El curso se guardó, pero no se pudo guardar la imagen. Modifique el curso para cargarla.");
        }
        cursoServicio.actualizarImagen(curso.getIdCurso(), nombre);
    }
}
