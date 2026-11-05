package com.app.idoneos.controller;

import com.app.idoneos.controller.component.PanelCursoComponent;
import com.app.idoneos.util.FiltroUtil;

import com.app.idoneos.service.curso.CursoService;
import com.app.idoneos.service.cohorte.CohorteService;

import com.app.idoneos.util.PaginacionUtil;
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

import com.app.idoneos.service.curso.CursoService.ContextoCurso;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.service.recurso.RecursoService.TipoRecurso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.util.DuracionUtil;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.service.categoria.CategoriaService;
import com.app.idoneos.service.docente.DocenteService;
import com.app.idoneos.service.modalidad.ModalidadService;
import com.app.idoneos.service.nivel.NivelService;
import com.app.idoneos.service.curso.CursoService.ResumenCursos;
import com.app.idoneos.service.programa.ProgramaService;
import com.app.idoneos.util.RespuestaFormularioUtil;

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
public class CursoController {

    @Autowired
    private PanelCursoComponent panel;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private CohorteService cohorteService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private NivelService nivelService;

    @Autowired
    private ModalidadService modalidadService;

    @Autowired
    private DocenteService docenteService;

    @Autowired
    private ProgramaService programaService;

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
     * @param model            El modelo de la vista.
     * @param authentication              La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de búsqueda de cursos.
     */
    /**
     * Ventana de modificación de un curso, que el listado pide al abrirla (en lugar de dibujar una por fila).
     *
     * @param id     El curso.
     * @param model El modelo de la vista.
     * @return El fragmento con la ventana.
     */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        Curso curso = cursoService.buscarActivoPorId(id);
        ResumenCursos resumen = cursoService.resumir(List.of(curso));
        model.addAttribute("curso", curso);
        model.addAttribute("bloqueado", resumen.inscripcionesPorCurso().containsKey(curso.getIdCurso())
                && !resumen.inscripcionesPorCurso().get(curso.getIdCurso()).isEmpty());
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("niveles", nivelService.obtenerTodo());
        model.addAttribute("modalidades", modalidadService.obtenerTodo());
        model.addAttribute("docentes", docenteService.buscarHabilitados());
        model.addAttribute("precioMaximo", (long) Curso.PRECIO_MAXIMO);
        return "pages/curso/modificar :: modal(curso=${curso}, bloqueado=${bloqueado})";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "docenteId", required = false) Integer docenteId,
            @RequestParam(value = "modalidadId", required = false) Integer modalidadId,
            @RequestParam(value = "orden", defaultValue = "nombre") String orden,
            @RequestParam(value = "accion", required = false) String accion,
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "vista", required = false) String vista,
            Model model, Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            if (usuario.esAdministradorActivo() && "cards".equals(vista)) {
                return buscarCardsParaAdministrador(model, authentication, redirectAttributes, request);
            }
            boolean esAdministrador = usuario.esAdministradorActivo();
            Docente soloDeDocente = esAdministrador ? null : usuario.getDocente();

            // Se entrega siempre el listado completo: los filtros (también los que llegan por la URL, que solo
            // completan los campos) y la paginación se aplican en la pantalla.
            List<Curso> cursos = cursoService.buscarConFiltros(null, null, null, null, null, orden, soloDeDocente);
            // El docente no ve los cursos dados de baja; el administrador sí
            if (!esAdministrador) {
                cursos = cursos.stream().filter(curso -> !curso.esInactivo()).toList();
            }

            // Inscripciones y programas activos por curso, para validar la baja y la modificación
            ResumenCursos resumen = esAdministrador ? cursoService.resumir(cursos) : null;
            if (!esAdministrador) {
                // "Mis cursos" del docente: cards, 8 por página; las filtra y pagina el servidor
                PaginacionUtil.Pagina<Curso> paginaCards = Filtros.cards.paginar(cursos, request.getParameterMap(), 8);
                PaginacionUtil.cargarEnModelo(model, paginaCards);
                cursos = paginaCards.filas();
                resumen = cursoService.resumir(cursos);
            }
            if (esAdministrador) {
                // El administrador ve una página por vez: el motor común filtra, ordena y pagina en el servidor
                PaginacionUtil.Pagina<Curso> pagina = Filtros.tabla(resumen).paginar(cursos, request.getParameterMap());
                PaginacionUtil.cargarEnModelo(model, pagina);
                cursos = pagina.filas();
            }

            model.addAttribute("cursos", cursos);
            model.addAttribute("inscripcionesPorCurso", resumen.inscripcionesPorCurso());
            model.addAttribute("programasPorCurso", resumen.programasPorCurso());
            model.addAttribute("cursosConInscripcionAbierta", resumen.cursosConInscripcionAbierta());
            if (PaginacionUtil.pideSoloFilas(request)) {
                return esAdministrador ? "pages/curso/buscarPorAdministrador :: filas" : "pages/curso/buscar :: filas"; // solo la página pedida
            }
            model.addAttribute("totalCursos", cursos.size());
            model.addAttribute("categorias", categoriaService.obtenerTodo());
            model.addAttribute("niveles", nivelService.obtenerTodo());
            model.addAttribute("modalidades", modalidadService.obtenerTodo());
            model.addAttribute("docentes", docenteService.buscarHabilitados());
            model.addAttribute("busqueda", busqueda);
            model.addAttribute("categoriaSeleccionada", categoriaId);
            model.addAttribute("nivelSeleccionado", nivelId);
            model.addAttribute("docenteSeleccionado", docenteId);
            model.addAttribute("modalidadSeleccionada", modalidadId);
            model.addAttribute("ordenSeleccionado", orden);
            model.addAttribute("precioMaximo", (long) Curso.PRECIO_MAXIMO);
            model.addAttribute("titulo", "Cursos | Idóneos Online");
            if (esAdministrador) {
                // Desde "Mis cursos" se llega con la acción elegida: la ventana correspondiente se abre sola
                String abrirModal = null;
                if ("nuevo".equals(accion)) {
                    abrirModal = "modalNuevoCurso";
                } else if (id != null && "editar".equals(accion)) {
                    abrirModal = "modalModificarCurso" + id;
                } else if (id != null && "baja".equals(accion)) {
                    abrirModal = "modalBajaCurso" + id;
                }
                model.addAttribute("abrirModal", abrirModal);
                model.addAttribute("menuActivo", "cursos");
                return "pages/curso/buscarPorAdministrador";
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
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Mis cursos" del administrador, o una redirección si el usuario no es administrador.
     */
    private String buscarCardsParaAdministrador(Model model, Authentication authentication, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        if (!usuario.esAdministradorActivo()) {
            return "redirect:/curso/buscar";
        }
        List<Curso> cursos = cursoService.buscarConFiltros(null, null, null, null, null, "nombre", null).stream()
                .filter(curso -> !curso.esInactivo()).toList();
        int totalCursos = cursos.size();
        // Tarjetas, 8 por página; las filtra y pagina el servidor
        PaginacionUtil.Pagina<Curso> pagina = Filtros.cards.paginar(cursos, request.getParameterMap(), 8);
        PaginacionUtil.cargarEnModelo(model, pagina);
        cursos = pagina.filas();
        ResumenCursos resumen = cursoService.resumir(cursos);
        model.addAttribute("cursos", cursos);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("programasPorCurso", resumen.programasPorCurso());
        model.addAttribute("cursosConInscripcionAbierta", resumen.cursosConInscripcionAbierta());
        model.addAttribute("esAdministrador", true);
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/curso/buscar :: filas"; // solo las cards de la página pedida
        }
        model.addAttribute("categorias", categoriaService.obtenerTodo());
        model.addAttribute("niveles", nivelService.obtenerTodo());
        model.addAttribute("modalidades", modalidadService.obtenerTodo());
        model.addAttribute("docentes", docenteService.buscarHabilitados());
        model.addAttribute("esAdministrador", true);
        model.addAttribute("titulo", "Mis cursos | Idóneos Online");
        return "pages/curso/buscar";
    }

    /**
     * CU-27: Permite al docente acceder a un curso en el que participa, con todos sus temas habilitados y sin
     * avance. Se muestra el programa y la cohorte de su contexto de trabajo.
     *
     * @param idCurso            El identificador del curso.
     * @param seccion            La sección a mostrar: "temas" (por defecto), "cronograma" o "clases".
     * @param model             El modelo de la vista.
     * @param authentication               La autenticación actual.
     * @param redirectAttributes Atributos para mensajes de redirección.
     * @return La vista de "Acceder curso", o una redirección a "Mis cursos" si no puede acceder.
     */
    @GetMapping("/acceder/{idCurso}")
    public String acceder(@PathVariable("idCurso") int idCurso,
            @RequestParam(value = "seccion", defaultValue = "temas") String seccion, Model model,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        boolean esAdministrador = false;
        try {
            // Si participa en el curso como docente trabaja como tal; si no, solo el administrador puede entrar
            DocenteCurso participacion = null;
            Docente docente = usuario.getDocente();
            if (docente != null) {
                try {
                    participacion = cursoService.validarAccesoDocente(idCurso, docente);
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
                curso = cursoService.buscarActivoPorId(idCurso);
                programa = curso.getProgramaVigente();
                cohorte = programa == null ? null : programa.getCohortesActivas().stream().findFirst().orElse(null);
            } else {
                curso = participacion.getCurso();
                programa = participacion.getProgramaDeTrabajo();
                cohorte = participacion.getCohorteDeTrabajo();
                if (cohorte != null && !cohorte.tienePrograma()) {
                    programa = null;   // la cohorte de trabajo todavía no tiene programa: se le asigna desde el inicio
                }
            }
            if ("cronograma".equals(seccion)) {
                // El cronograma del docente tiene su propia página, donde también se modifica
                return "redirect:/cronograma/buscar?cursoId=" + idCurso;
            }
            String seccionActiva = List.of("temas", "clases").contains(seccion) ? seccion : "temas";

            String menuActivo = "temas".equals(seccionActiva) ? "curso" : seccionActiva;
            panel.cargarContexto(model, esAdministrador ? ContextoCurso.deAdministrador(curso)
                    : ContextoCurso.deDocente(participacion), menuActivo);
            panel.cargarDiasMaximosDelCronograma(model, curso);
            model.addAttribute("inscripcion", null);
            model.addAttribute("cohorte", cohorte);
            // Selectores de programa y cohorte por defecto: solo los activos, el más reciente primero
            model.addAttribute("programasDelCurso", curso.getProgramasActivos());
            // La cohorte por defecto puede ser cualquiera del curso, también una pendiente que todavía no tiene programa
            model.addAttribute("cohortesDelPrograma", curso.getCohortesDeTrabajo());
            model.addAttribute("estadoDiseno", cursoService.calcularEstadoDiseno(curso, cohorte));
            // Lo que le falta al contenido de cada programa: la ventana de asignar programa avisa en qué estado queda la cohorte
            Map<Integer, List<String>> faltantesPorPrograma = new java.util.HashMap<>();
            if (!esAdministrador) {
                curso.getProgramasActivos().forEach(p -> faltantesPorPrograma.put(p.getIdPrograma(),
                        cohorteService.faltantesDeContenido(p)));
            }
            model.addAttribute("faltantesPorPrograma", faltantesPorPrograma);
            model.addAttribute("niveles", nivelService.obtenerTodo());
            // El programa de una cohorte se elige (o se cambia) mientras esté pendiente y sin inscriptos
            model.addAttribute("puedeAsignarPrograma", !esAdministrador && cohorte != null && cohorte.esPendiente()
                    && cohorte.getCantidadInscriptos() == 0);
            // Qué temas ya tienen todo su contenido (el docente ve los pendientes expandidos y con su insignia)
            Map<Integer, Boolean> contenidoCompleto = new java.util.HashMap<>();
            if (programa != null) {
                programa.getCronogramaOrdenado().forEach(uc -> contenidoCompleto.put(uc.getTema().getIdTema(),
                        cursoService.buscarContenidoDeTema(curso, uc.getTema()).getFaltantes().isEmpty()));
            }
            model.addAttribute("contenidoCompleto", contenidoCompleto);
            // Qué le falta a cada tema y, para «Completar contenido», cuál es el primero pendiente y el recurso que sigue
            Map<Integer, List<String>> faltantesPorTema = new java.util.HashMap<>();
            com.app.idoneos.model.Tema temaPendiente = null;
            if (programa != null) {
                for (var uc : programa.getCronogramaOrdenado()) {
                    List<String> faltantes = cursoService.buscarContenidoDeTema(curso, uc.getTema()).getFaltantes();
                    faltantesPorTema.put(uc.getTema().getIdTema(), faltantes);
                    if (temaPendiente == null && !faltantes.isEmpty()) {
                        temaPendiente = uc.getTema();
                    }
                }
            }
            model.addAttribute("faltantesPorTema", faltantesPorTema);
            if (temaPendiente != null && !esAdministrador) {
                model.addAttribute("temaPendiente", temaPendiente);
                model.addAttribute("siguienteRecurso", cursoService.buscarSiguienteRecurso(curso, programa, temaPendiente));
            }
            if (!esAdministrador) {
                model.addAttribute("tiposDeSubida", TipoRecurso.deSubidaManual());
            }
            // La página del curso se ve como la del docente, también para el administrador (sin gestionar contenido)
            model.addAttribute("esDocente", true);
            model.addAttribute("puedeGestionar", !esAdministrador);
            model.addAttribute("seccion", seccionActiva);
            model.addAttribute("titulo", curso.getNombre() + " | Idóneos Online");
            return "pages/curso/acceder";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return usuario.esAdministradorActivo() && usuario.getDocente() == null ? "redirect:/curso/buscar?vista=cards"
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
     * @param cargaHoraria             La carga horaria del curso para el alumno, en la unidad indicada.
     * @param cargaHorariaUnidad       La unidad de la carga horaria: "minutos" u "horas" (por defecto, horas).
     * @param duracion            La duración del curso, en la unidad indicada.
     * @param duracionUnidad      La unidad de la duración: "dias", "semanas" o "meses" (por defecto, días).
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
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "precio", required = false) Float precio,
            @RequestParam(value = "cargaHoraria", required = false) Integer cargaHoraria,
            @RequestParam(value = "cargaHorariaUnidad", defaultValue = "horas") String cargaHorariaUnidad,
            @RequestParam(value = "duracion", required = false) Integer duracion,
            @RequestParam(value = "duracionUnidad", defaultValue = "dias") String duracionUnidad,
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
                ManejadorArchivosUtil.validarImagen(imagenArchivo); // antes de registrar, para no crear un curso con imagen inválida
            }
            Curso curso = cursoService.registrar(nombre, descripcion, precio,
                    minutos(cargaHoraria, cargaHorariaUnidad),
                    dias(duracion, duracionUnidad), null, categoriaId, nivelId,
                    emiteCertificado, idsModalidades, docenteTitularId, idsDocentesAyudantes);
            if (hayImagen) {
                guardarImagen(curso, imagenArchivo);
            }
            // Alerta guía: el paso siguiente de un curso nuevo es abrir una cohorte (sin programa, hasta que el docente cargue uno)
            return RespuestaFormularioUtil.armarRespuestaExitosaConSiguiente(
                    "Curso '" + curso.getNombre() + "' registrado con éxito.",
                    "¿Querés crear una cohorte?",
                    "Una cohorte es un período de inscripción y dictado del curso. Podés registrarla ahora, sin programa: queda "
                            + "pendiente hasta que el docente cargue uno.",
                    "Crear cohorte", "/cohorte/buscar?cursoId=" + curso.getIdCurso() + "&nuevo=");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
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
     * @param cargaHoraria             La carga horaria del curso para el alumno, en la unidad indicada.
     * @param cargaHorariaUnidad       La unidad de la carga horaria: "minutos" u "horas" (por defecto, horas).
     * @param duracion            La duración del curso, en la unidad indicada.
     * @param duracionUnidad      La unidad de la duración: "dias", "semanas" o "meses" (por defecto, días).
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
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "precio", required = false) Float precio,
            @RequestParam(value = "cargaHoraria", required = false) Integer cargaHoraria,
            @RequestParam(value = "cargaHorariaUnidad", defaultValue = "horas") String cargaHorariaUnidad,
            @RequestParam(value = "duracion", required = false) Integer duracion,
            @RequestParam(value = "duracionUnidad", defaultValue = "dias") String duracionUnidad,
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
                ManejadorArchivosUtil.validarImagen(imagenArchivo);
            }
            // El nivel, la descripción y la carga horaria son del docente: el formulario del administrador no los envía y se conservan
            Curso actual = cursoService.buscarActivoPorId(id);
            Curso curso = cursoService.modificar(id, nombre, descripcion != null ? descripcion : actual.getDescripcion(), precio,
                    cargaHoraria != null ? minutos(cargaHoraria, cargaHorariaUnidad) : actual.getCargaHorariaMinutos(),
                    dias(duracion, duracionUnidad), null, categoriaId, nivelId,
                    emiteCertificado, idsModalidades, docenteTitularId, idsDocentesAyudantes);
            if (hayImagen) {
                guardarImagen(curso, imagenArchivo); // reemplaza la imagen anterior
            }
            return RespuestaFormularioUtil.armarRespuestaExitosa("Curso modificado correctamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ---------------------------------------------------------------- Unidades de duración

    /** Convierte la dedicación docente recibida (en minutos u horas) a minutos; {@code null} si no se envió. */
    private static Integer minutos(Integer cantidad, String unidad) {
        if (cantidad == null) {
            return null;
        }
        try {
            return DuracionUtil.convertirAMinutos(cantidad, unidad);
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE; // desborda: el servicio lo rechaza por superar el máximo
        }
    }

    /** Convierte la duración recibida (en días, semanas o meses) a días; {@code null} si no se envió. */
    private static Integer dias(Integer cantidad, String unidad) {
        if (cantidad == null) {
            return null;
        }
        try {
            return DuracionUtil.convertirADias(cantidad, unidad);
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Completa los datos académicos del curso (los que define el docente al diseñarlo): nivel, descripción, objetivos,
     * carga horaria para el certificado e imagen. Responde en JSON.
     *
     * @param id                 Identificador del curso.
     * @param nivelId            Identificador del nivel.
     * @param descripcion        La descripción (opcional).
     * @param objetivos          Los objetivos.
     * @param cargaHoraria       La carga horaria para el certificado, en la unidad indicada.
     * @param cargaHorariaUnidad La unidad de la carga horaria: "minutos" u "horas" (por defecto, horas).
     * @param imagenArchivo      La nueva imagen de portada (opcional; si no se envía se conserva la actual).
     * @param authentication               La autenticación actual.
     * @return Una respuesta con el mensaje de éxito o el mensaje de error.
     */
    @PostMapping("/modificarPorDocente/{id}")
    public ResponseEntity<Map<String, String>> modificarPorDocente(@PathVariable("id") Integer id,
            @RequestParam(value = "nivelId", required = false) Integer nivelId,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "objetivos", required = false) String objetivos,
            @RequestParam(value = "cargaHoraria", required = false) Integer cargaHoraria,
            @RequestParam(value = "cargaHorariaUnidad", defaultValue = "horas") String cargaHorariaUnidad,
            @RequestParam(value = "imagenArchivo", required = false) MultipartFile imagenArchivo,
            Authentication authentication) {
        try {
            boolean hayImagen = imagenArchivo != null && !imagenArchivo.isEmpty();
            if (hayImagen) {
                ManejadorArchivosUtil.validarImagen(imagenArchivo);
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Curso curso = cursoService.modificarPorDocente(id, usuario.getDocente(), nivelId, descripcion, objetivos,
                    minutos(cargaHoraria, cargaHorariaUnidad));
            if (hayImagen) {
                guardarImagen(curso, imagenArchivo);
            }
            // Alerta guía: lo que sigue es registrar el programa (o asignar uno, si el curso ya tiene)
            return panel.exitoConPasoSiguiente("Datos del curso guardados correctamente.", id, usuario.getDocente());
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
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
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id) {
        try {
            cursoService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Curso dado de baja exitosamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
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
            cursoService.darDeBajaVarios(ids);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Cursos dados de baja exitosamente.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /**
     * Guarda la imagen del curso en su carpeta ({@code curso{id}/imagen.{ext}}), reemplazando la anterior, y
     * registra su nombre en el curso.
     */
    private void guardarImagen(Curso curso, MultipartFile imagenArchivo) {
        String nombre;
        try {
            nombre = ManejadorArchivosUtil.guardarImagen(imagenArchivo,
                    ManejadorArchivosUtil.resolverCarpetaDeCurso(directorioCursos, curso.getIdCurso()), "imagen");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Error! El curso se guardó, pero no se pudo guardar la imagen. Modifique el curso para cargarla.");
        }
        cursoService.actualizarImagen(curso.getIdCurso(), nombre);
    }


    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        // ---------------------------------------------------------------- Tarjetas de cursos (motor común)

        /** Las columnas de las vistas de cards de cursos (catálogo y "Mis cursos"): lo que se filtra en el servidor. */
        public static final FiltroUtil<Curso> cards = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Curso>("busqueda", FiltroUtil.Tipo.TEXTO,
                        c -> c.getNombre() + " " + (c.getDescripcion() == null ? "" : c.getDescripcion())),
                new FiltroUtil.Columna<Curso>("categoriaId", FiltroUtil.Tipo.LISTA, c -> String.valueOf(c.getCategoria().getIdCategoria())),
                new FiltroUtil.Columna<Curso>("nivelId", FiltroUtil.Tipo.LISTA,
                        c -> c.getNivel() == null ? "" : String.valueOf(c.getNivel().getIdNivel())),
                new FiltroUtil.Columna<Curso>("docenteId", FiltroUtil.Tipo.LISTA,
                        c -> c.getEquipoDocente().stream().map(p -> String.valueOf(p.getDocente().getIdDocente()))
                                .collect(java.util.stream.Collectors.joining("|"))),
                new FiltroUtil.Columna<Curso>("modalidadId", FiltroUtil.Tipo.LISTA,
                        c -> c.getModalidades().stream().map(m -> String.valueOf(m.getIdModalidad()))
                                .collect(java.util.stream.Collectors.joining("|"))),
                new FiltroUtil.Columna<Curso>("precio", FiltroUtil.Tipo.LISTA, c -> c.getPrecio() <= 0 ? "gratis" : "pago")),
                c -> false);

        // ---------------------------------------------------------------- Listado del administrador (motor común)

        /** Las columnas del listado del administrador: lo que se filtra y se ordena en el servidor. */
        public static FiltroUtil<Curso> tabla(ResumenCursos resumen) {
            return new FiltroUtil<>(List.of(
                    new FiltroUtil.Columna<Curso>("nombre", FiltroUtil.Tipo.TEXTO, Curso::getNombre),
                    new FiltroUtil.Columna<Curso>("categoria", FiltroUtil.Tipo.LISTA, c -> c.getCategoria().getNombre()),
                    new FiltroUtil.Columna<Curso>("nivel", FiltroUtil.Tipo.LISTA, c -> c.getNivel() != null ? c.getNivel().getNombre() : ""),
                    new FiltroUtil.Columna<Curso>("precio", FiltroUtil.Tipo.NUMERO, c -> String.valueOf(c.getPrecio())),
                    new FiltroUtil.Columna<Curso>("cargaHoraria", FiltroUtil.Tipo.NUMERO,
                            c -> c.getCargaHorariaMinutos() == null ? "" : String.valueOf(c.getCargaHorariaMinutos() / 60.0)),
                    new FiltroUtil.Columna<Curso>("duracion", FiltroUtil.Tipo.NUMERO, c -> String.valueOf(c.getDuracionDias())),
                    new FiltroUtil.Columna<Curso>("modalidades", FiltroUtil.Tipo.LISTA,
                            c -> c.getModalidades().stream().map(m -> m.getNombre()).collect(java.util.stream.Collectors.joining("|"))),
                    new FiltroUtil.Columna<Curso>("docenteTitular", FiltroUtil.Tipo.LISTA,
                            c -> c.getDocenteTitular() != null ? c.getDocenteTitular().getUsuario().getNombreCompleto() : ""),
                    new FiltroUtil.Columna<Curso>("estado", FiltroUtil.Tipo.LISTA,
                            c -> c.getBaja() ? "Dado de baja" : (resumen.cursosConInscripcionAbierta().contains(c.getIdCurso()) ? "Inscripción abierta" : "Activo")),
                    new FiltroUtil.Columna<Curso>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                            c -> FiltroUtil.formatearFechaHora(c.getFechaCreacion()))),
                    Curso::getBaja);
        }
    }
}
