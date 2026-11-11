package com.app.idoneos.service.recurso;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Bibliografia;
import com.app.idoneos.model.CategoriaMaterialLectura;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Enlace;
import com.app.idoneos.model.Grabacion;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.MaterialLectura;
import com.app.idoneos.model.Modalidad;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.service.recurso.RecursoService.TipoRecurso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.BibliografiaRepository;
import com.app.idoneos.repository.CategoriaMaterialLecturaRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.EnlaceRepository;
import com.app.idoneos.repository.GrabacionRepository;
import com.app.idoneos.repository.MaterialLecturaRepository;
import com.app.idoneos.repository.RecursoRepository;
import com.app.idoneos.repository.TemaRecursoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.inscripcion.InscripcionService;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Implementación de RecursoService. */
@Service
public class RecursoServiceImpl implements RecursoService {

    private static final int LARGO_TITULO = 150;
    private static final int LARGO_DESCRIPCION = 500;
    private static final int LARGO_AUTOR = 100;
    private static final int LARGO_ISBN = 17;
    private static final int LARGO_EDITORIAL = 100;
    private static final int LARGO_ENLACE = 500;

    @Autowired
    private com.app.idoneos.service.programa.ProgramaService programaService;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Autowired
    private TemaRecursoRepository temaRecursoRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private EnlaceRepository enlaceRepository;

    @Autowired
    private GrabacionRepository grabacionRepository;

    @Autowired
    private MaterialLecturaRepository materialLecturaRepository;

    @Autowired
    private BibliografiaRepository bibliografiaRepository;

    @Autowired
    private CategoriaMaterialLecturaRepository categoriaMaterialLecturaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el material por su identificador. */
    @Override
    public Optional<TemaRecurso> buscarPorId(Integer id) {
        return temaRecursoRepository.findById(id).filter(entidad -> !entidad.esInactivo());
    }

    /** Obtiene todos los registros activos. */
    @Override
    public List<TemaRecurso> obtenerTodo() {
        return temaRecursoRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro activo con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return buscarPorId(id).isPresent();
    }

    /** Busca los materiales activos de un tema, en su orden. */
    @Override
    public List<TemaRecurso> buscarPorTema(Tema tema) {
        return temaRecursoRepository.findByTemaAndBajaFalse(tema).stream()
                .sorted(Comparator.comparingInt(TemaRecurso::getNumeroOrden).thenComparingInt(TemaRecurso::getIdTemaRecurso))
                .toList();
    }

    /** Busca materiales con filtros opcionales. */
    @Override
    public List<TemaRecurso> buscarConFiltros(Integer idCurso, Integer idTema, String tipo, String titulo,
            Boolean generadoPorIa, Boolean baja, Docente soloDeDocente) {
        String criterio = titulo == null ? "" : titulo.trim().toLowerCase();
        TipoRecurso tipoBuscado = TipoRecurso.deTexto(tipo);
        // El curso, el tema y la baja se filtran en la base. El resto, acá
        return temaRecursoRepository.buscarPorFiltros(idCurso, idTema, baja).stream()
                .filter(m -> tipoBuscado == null || m.getRecurso().getTipo().equals(tipoBuscado.getNombre()))
                .filter(m -> criterio.isEmpty() || m.getTitulo().toLowerCase().contains(criterio))
                .filter(m -> generadoPorIa == null || m.getRecurso().getGeneradoPorIa() == generadoPorIa)
                .filter(m -> soloDeDocente == null || participaEnElCurso(m.getCurso(), soloDeDocente))
                .sorted(Comparator.comparing(TemaRecurso::getBaja)
                        .thenComparingInt((TemaRecurso m) -> m.getTema().getIdTema())
                        .thenComparingInt(TemaRecurso::getNumeroOrden).thenComparingInt(TemaRecurso::getIdTemaRecurso))
                .toList();
    }

    /** Indica si un usuario puede abrir un material. */
    @Override
    public boolean puedeAcceder(TemaRecurso material, Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return true;
        }
        Docente docente = usuario.getDocente();
        if (docente != null && participaEnElCurso(material.getCurso(), docente)) {
            return true;
        }
        Alumno alumno = usuario.getAlumno();
        return alumno != null && buscarInscripcionConAcceso(material, alumno).isPresent();
    }

    /** Busca la inscripción con la que un alumno accede a un material (debe estar publicado). */
    @Override
    public Optional<Inscripcion> buscarInscripcionConAcceso(TemaRecurso material, Alumno alumno) {
        if (!material.estaPublicado()) {
            return Optional.empty();
        }
        return inscripcionService.buscarConAccesoATema(alumno, material.getTema());
    }

    // ----------------------------------------------------------------------
    // Biblioteca del docente
    // ----------------------------------------------------------------------

    @Override
    public List<Recurso> buscarRecursosDelDocente(Docente docente) {
        return recursoRepository.findByDocenteAndBajaFalse(docente).stream()
                .sorted(Comparator.comparing(Recurso::getFechaCreacion).reversed()).toList();
    }

    @Override
    public List<Recurso> buscarRecursosVisibles(Docente docente) {
        return recursoRepository.findByBajaFalse().stream()
                .filter(recurso -> !recurso.getPrivado() || (docente != null && recurso.getDocente().getIdDocente() == docente.getIdDocente()))
                .sorted(Comparator.comparing(Recurso::getFechaCreacion).reversed()).toList();
    }

    @Override
    public List<Recurso> buscarRecursosPublicos(Docente excluido) {
        return recursoRepository.findByPrivadoFalseAndBajaFalse().stream()
                .filter(recurso -> excluido == null || recurso.getDocente().getIdDocente() != excluido.getIdDocente())
                .sorted(Comparator.comparing(Recurso::getFechaCreacion).reversed()).toList();
    }

    @Override
    public List<Recurso> buscarTodosLosRecursos() {
        return recursoRepository.findByBajaFalse().stream()
                .sorted(Comparator.comparing(Recurso::getFechaCreacion).reversed()).toList();
    }

    @Override
    @Transactional
    public void darDeBajaComoAdministrador(Integer idRecurso) {
        Recurso recurso = buscarRecursoPorId(idRecurso)
                .orElseThrow(() -> new IllegalArgumentException("Error! El recurso no se encuentra activo."));
        darDeBajaSinUsos(recurso);
    }

    @Override
    public boolean puedeVerEnBiblioteca(Recurso recurso, Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return true;
        }
        Docente docente = usuario.getDocente();
        return docente != null && (!recurso.getPrivado() || recurso.getDocente().getIdDocente() == docente.getIdDocente());
    }

    @Override
    public Optional<Recurso> buscarRecursoPorId(Integer idRecurso) {
        return recursoRepository.findById(idRecurso == null ? -1 : idRecurso).filter(recurso -> !recurso.esInactivo());
    }

    @Override
    public long contarUsos(Recurso recurso) {
        return recurso.getTemasRecurso().stream().filter(uso -> !uso.esInactivo()).count();
    }

    @Override
    @Transactional
    public Recurso subirABiblioteca(Docente docente, String tipoTexto, DatosRecurso datos, MultipartFile archivo, boolean terminos) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        TipoRecurso tipo = TipoRecurso.deTexto(tipoTexto);
        if (tipo == null || !tipo.permiteSubidaManual()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("tipo", "Error! Debe seleccionar un tipo de recurso de la lista.");
        }
        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        validarDatos(tipo, datos, hayArchivo, true);
        validarIsbnNoRepetido(tipo, datos, -1);
        if (hayArchivo) {
            ManejadorArchivosUtil.validar(archivo, tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        }
        if (!terminos) {
            throw RespuestaFormularioUtil.armarCampoInvalido("terminos",
                    "Error! Debe aceptar los términos y condiciones sobre el contenido del recurso.");
        }
        Recurso recurso = new Recurso(docente, datos.titulo().trim());
        recurso.setDescripcion(convertirVacioANulo(datos.descripcion()));
        recurso.setPrivado(tipo != TipoRecurso.BIBLIOGRAFIA && datos.privado()); // las bibliografías son siempre públicas
        recurso.setFechaAceptacionTyc(LocalDateTime.now());
        recurso = recursoRepository.saveAndFlush(recurso); // para conocer su identificador
        String ruta = hayArchivo ? guardarArchivoEnBiblioteca(recurso, docente, tipo, archivo) : null;
        crearDatosPropios(recurso, tipo, datos, ruta, hayArchivo ? archivo.getSize() : null);
        return recurso;
    }

    @Override
    @Transactional
    public Recurso modificarEnBiblioteca(Integer idRecurso, Docente docente, DatosRecurso datos, MultipartFile archivo) {
        Recurso recurso = obtenerRecursoDelDocente(idRecurso, docente, "modificar");
        TipoRecurso tipo = TipoRecurso.deTexto(recurso.getTipo());
        if (tipo == null || !tipo.permiteSubidaManual()) {
            throw new IllegalArgumentException("Error! Este tipo de recurso no se puede modificar manualmente.");
        }
        long usos = contarUsos(recurso);
        if (usos > 0) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("modificar", "el recurso", recurso.getTitulo(),
                    "tiene " + MensajeErrorUtil.formatearCantidad(usos, "tema asociado", "temas asociados"), ""));
        }
        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        validarDatos(tipo, datos, hayArchivo, false);
        validarIsbnNoRepetido(tipo, datos, recurso.getIdRecurso());
        if (hayArchivo) {
            ManejadorArchivosUtil.validar(archivo, tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        }
        String rutaAnterior = recurso.getRutaArchivo();
        String rutaNueva = hayArchivo ? guardarArchivoEnBiblioteca(recurso, docente, tipo, archivo) : null;
        if (rutaNueva != null && rutaAnterior != null && !rutaAnterior.equals(rutaNueva)) {
            ManejadorArchivosUtil.eliminar(rutaAnterior, directorioCursos);
        }
        actualizarDatosPropios(recurso, tipo, datos, rutaNueva, hayArchivo ? archivo.getSize() : null);
        recurso.setTitulo(datos.titulo().trim());
        recurso.setDescripcion(convertirVacioANulo(datos.descripcion()));
        recurso.setPrivado(tipo != TipoRecurso.BIBLIOGRAFIA && datos.privado()); // las bibliografías son siempre públicas
        recurso.setUltimaModificacion(LocalDateTime.now());
        return recursoRepository.save(recurso);
    }

    @Override
    @Transactional
    public void darDeBajaEnBiblioteca(Integer idRecurso, Docente docente) {
        darDeBajaSinUsos(obtenerRecursoDelDocente(idRecurso, docente, "dar de baja"));
    }

    /** Da de baja el recurso si ningún tema lo usa. */
    private void darDeBajaSinUsos(Recurso recurso) {
        validarSinUsos(recurso);
        marcarRecursoDeBaja(recurso);
    }

    /** Comprueba que ningún tema use el recurso. Si alguno lo usa, la baja no se puede. */
    private void validarSinUsos(Recurso recurso) {
        long usos = contarUsos(recurso);
        if (usos > 0) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el recurso", recurso.getTitulo(),
                    "tiene " + MensajeErrorUtil.formatearCantidad(usos, "tema asociado", "temas asociados"),
                    "Dé de baja primero su recurso en " + (usos == 1 ? "ese tema." : "esos temas.")));
        }
    }

    private void marcarRecursoDeBaja(Recurso recurso) {
        recurso.marcarInactivo();
        recurso.setUltimaModificacion(LocalDateTime.now());
        recursoRepository.save(recurso);
    }

    @Override
    @Transactional
    public void publicar(Integer idRecurso, Docente docente) {
        Recurso recurso = obtenerRecursoDelDocente(idRecurso, docente, "publicar");
        recurso.setPrivado(false);
        recurso.setUltimaModificacion(LocalDateTime.now());
        recursoRepository.save(recurso);
    }

    @Override
    @Transactional
    public void darDeBajaVariosEnBiblioteca(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un recurso.");
        }
        List<Recurso> recursos = ids.stream().distinct().map(id -> obtenerRecursoDelDocente(id, docente, "dar de baja")).toList();
        recursos.forEach(this::validarSinUsos); // todos o ninguno
        recursos.forEach(this::marcarRecursoDeBaja);
    }

    @Override
    @Transactional
    public void darDeBajaVariosComoAdministrador(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un recurso.");
        }
        List<Recurso> recursos = ids.stream().distinct().map(id -> buscarRecursoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El recurso no se encuentra activo."))).toList();
        recursos.forEach(this::validarSinUsos); // todos o ninguno
        recursos.forEach(this::marcarRecursoDeBaja);
    }

    /** Busca un recurso activo y comprueba que sea del docente. */
    private Recurso obtenerRecursoDelDocente(Integer idRecurso, Docente docente, String accion) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        Recurso recurso = buscarRecursoPorId(idRecurso)
                .orElseThrow(() -> new IllegalArgumentException("Error! El recurso no se encuentra activo."));
        if (recurso.getDocente().getIdDocente() != docente.getIdDocente()) {
            String mensaje = "dar de baja".equals(accion)
                    ? MensajeErrorUtil.armarBajaBloqueada("el recurso", recurso.getTitulo(), "pertenece a otro docente", "")
                    : MensajeErrorUtil.armarAccionBloqueada(accion, "el recurso", recurso.getTitulo(), "pertenece a otro docente", "");
            throw new IllegalArgumentException(mensaje);
        }
        return recurso;
    }

    private String guardarArchivoEnBiblioteca(Recurso recurso, Docente docente, TipoRecurso tipo, MultipartFile archivo) {
        Path carpeta = ManejadorArchivosUtil.resolverCarpetaDeBiblioteca(directorioCursos, docente.getIdDocente());
        String nombre = ManejadorArchivosUtil.guardar(archivo, carpeta, "recurso" + recurso.getIdRecurso(),
                tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        return ManejadorArchivosUtil.construirRutaRelativa(directorioCursos, carpeta.resolve(nombre));
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el material en la base de datos. */
    @Override
    public TemaRecurso guardar(TemaRecurso entidad) {
        return temaRecursoRepository.save(entidad);
    }

    /** Carga un material a un tema. */
    @Override
    @Transactional
    public TemaRecurso subir(Docente docente, Integer idTema, String tipoTexto, DatosRecurso datos,
            MultipartFile archivo, boolean terminos) {
        Tema tema = temaRepository.findById(idTema == null ? -1 : idTema).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        DocenteCurso participacion = obtenerParticipacion(tema.getCurso(), docente);
        TipoRecurso tipo = TipoRecurso.deTexto(tipoTexto);
        if (tipo == null || !tipo.permiteSubidaManual()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("tipo", "Error! Debe seleccionar un tipo de recurso de la lista.");
        }
        if (tipo == TipoRecurso.GRABACION && !tema.getCurso().incluyeModalidad(Modalidad.GRABADA)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("tipo", "Error! El curso no tiene la modalidad '"
                    + Modalidad.GRABADA + "': no puede registrarse una grabación.");
        }
        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        validarDatos(tipo, datos, hayArchivo, true);
        validarIsbnNoRepetido(tipo, datos, -1);
        if (hayArchivo) {
            ManejadorArchivosUtil.validar(archivo, tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        }
        if (!terminos) {
            throw RespuestaFormularioUtil.armarCampoInvalido("terminos",
                    "Error! Debe aceptar los términos y condiciones sobre el contenido del recurso.");
        }

        Recurso recurso = new Recurso(docente, datos.titulo().trim());
        recurso.setDescripcion(convertirVacioANulo(datos.descripcion()));
        recurso.setPrivado(tipo != TipoRecurso.BIBLIOGRAFIA && datos.privado()); // las bibliografías son siempre públicas
        recurso.setFechaAceptacionTyc(LocalDateTime.now());
        recurso = recursoRepository.saveAndFlush(recurso); // para conocer su identificador
        String ruta = hayArchivo ? guardarArchivo(recurso, tipo, archivo) : null;
        crearDatosPropios(recurso, tipo, datos, ruta, hayArchivo ? archivo.getSize() : null);

        int orden = temaRecursoRepository.findByTemaAndBajaFalse(tema).stream()
                .mapToInt(TemaRecurso::getNumeroOrden).max().orElse(0) + 1;
        TemaRecurso material = new TemaRecurso(tema, recurso, participacion, orden);
        material.setOculto(datos.oculto());
        material = temaRecursoRepository.save(material);
        tema.getTemasRecurso().add(material);
        recurso.getTemasRecurso().add(material);
        return material;
    }

    /** Registra en un tema un recurso de la biblioteca digital. */
    @Override
    @Transactional
    public TemaRecurso reutilizar(Docente docente, Integer idTema, Integer idRecurso) {
        return reutilizar(docente, idTema, List.of(idRecurso == null ? -1 : idRecurso)).get(0);
    }

    /** Registra en un tema varios recursos de la biblioteca digital (todos o ninguno). */
    @Override
    @Transactional
    public List<TemaRecurso> reutilizar(Docente docente, Integer idTema, List<Integer> idsRecurso) {
        List<Integer> ids = idsRecurso == null ? List.of() : idsRecurso.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe elegir al menos un recurso de la biblioteca.");
        }
        Tema tema = temaRepository.findById(idTema == null ? -1 : idTema).filter(u -> !u.esInactivo())
                .orElseThrow(() -> RespuestaFormularioUtil.armarCampoInvalido("temaId", "Error! Debe seleccionar el tema."));
        DocenteCurso participacion = obtenerParticipacion(tema.getCurso(), docente);
        List<TemaRecurso> delTema = temaRecursoRepository.findByTemaAndBajaFalse(tema);
        List<Recurso> recursos = new java.util.ArrayList<>();
        for (Integer idRecurso : ids) {
            Recurso recurso = recursoRepository.findById(idRecurso).filter(r -> !r.esInactivo())
                    .filter(r -> !r.getPrivado() || r.getDocente().getIdDocente() == docente.getIdDocente())
                    .orElseThrow(() -> new IllegalArgumentException("Error! El recurso no se encuentra disponible en la biblioteca."));
            TipoRecurso tipo = TipoRecurso.deTexto(recurso.getTipo());
            if (tipo == null || !tipo.permiteSubidaManual()) {
                throw new IllegalArgumentException("Error! Este tipo de recurso no se puede registrar a mano en un tema.");
            }
            if (tipo == TipoRecurso.GRABACION && !tema.getCurso().incluyeModalidad(Modalidad.GRABADA)) {
                throw new IllegalArgumentException("Error! El curso no tiene la modalidad '" + Modalidad.GRABADA
                        + "': no puede registrarse una grabación.");
            }
            if (delTema.stream().anyMatch(m -> m.getRecurso().getIdRecurso() == recurso.getIdRecurso())) {
                throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("registrar", "el recurso", recurso.getTitulo(),
                        "ya está en el tema «" + tema.getTitulo() + "»", "Elegí otro tema."));
            }
            recursos.add(recurso);
        }
        int orden = delTema.stream().mapToInt(TemaRecurso::getNumeroOrden).max().orElse(0);
        List<TemaRecurso> registrados = new java.util.ArrayList<>();
        for (Recurso recurso : recursos) {
            TemaRecurso material = temaRecursoRepository.save(new TemaRecurso(tema, recurso, participacion, ++orden));
            tema.getTemasRecurso().add(material);
            recurso.getTemasRecurso().add(material);
            registrados.add(material);
        }
        return registrados;
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public TemaRecurso modificar(TemaRecurso entidad) {
        return temaRecursoRepository.save(entidad);
    }

    /** Cambia la visibilidad de un material en su tema. */
    @Override
    @Transactional
    public TemaRecurso cambiarVisibilidad(Integer id, Docente docente, boolean oculto) {
        TemaRecurso material = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El recurso no se encuentra activo."));
        obtenerParticipacion(material.getCurso(), docente);

        if (!oculto && material.getOculto()) {
            // Volver a publicar un material cuya modalidad el curso ya no tiene
            String modalidad = material.getModalidadAsociada();
            if (modalidad != null && !material.getCurso().incluyeModalidad(modalidad)) {
                throw RespuestaFormularioUtil.armarCampoInvalido("oculto", "Error! No se puede publicar el recurso «"
                        + material.getTitulo() + "»: el curso no tiene la modalidad '" + modalidad + "'.");
            }
        }
        if (oculto && !material.getOculto()) {
            programaService.buscarMotivoDeMinimoPorRecurso(material.getTema(), Set.of(material.getIdTemaRecurso())).ifPresent(motivo -> {
                // El error es del campo "Visible para alumnos": se muestra debajo de él, como en las autoevaluaciones
                throw RespuestaFormularioUtil.armarCampoInvalido("oculto",
                        MensajeErrorUtil.armarAccionBloqueada("ocultar", "el recurso", material.getTitulo(), motivo,
                                "Registrá otro antes de ocultarlo."));
            });
            // Ocultarlo no puede dejar el diseño del curso incompleto mientras el curso tiene cohortes activas
            programaService.buscarMotivoDeDisenoIncompleto(material.getTema(), Set.of(material.getIdTemaRecurso())).ifPresent(motivo -> {
                throw RespuestaFormularioUtil.armarCampoInvalido("oculto",
                        MensajeErrorUtil.armarAccionBloqueada("ocultar", "el recurso", material.getTitulo(), motivo,
                                "Registrá otro antes de ocultarlo."));
            });
        }
        material.setOculto(oculto);
        material.setUltimaModificacion(LocalDateTime.now());
        return temaRecursoRepository.save(material);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica). */
    @Override
    public void borrar(TemaRecurso entidad) {
        entidad.marcarInactivo();
        temaRecursoRepository.save(entidad);
    }

    /** Da de baja un material. */
    @Override
    @Transactional
    public void darDeBaja(Integer id, Docente docente) {
        TemaRecurso material = validarBaja(id, docente);
        validarMinimoAlDarDeBaja(List.of(material));
        darDeBaja(material);
    }

    /** Da de baja varios materiales a la vez (todos o ninguno). */
    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un recurso.");
        }
        List<TemaRecurso> materiales = ids.stream().distinct().map(id -> validarBaja(id, docente)).toList();
        validarMinimoAlDarDeBaja(materiales);
        materiales.forEach(this::darDeBaja);
    }

    private TemaRecurso validarBaja(Integer id, Docente docente) {
        TemaRecurso material = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El recurso no se encuentra activo."));
        if (docente != null) {
            obtenerParticipacion(material.getCurso(), docente);
        }
        return material;
    }

    private void validarMinimoAlDarDeBaja(List<TemaRecurso> materiales) {
        Set<Integer> ids = materiales.stream().map(TemaRecurso::getIdTemaRecurso).collect(Collectors.toSet());
        for (TemaRecurso material : materiales) {
            programaService.buscarMotivoDeMinimoPorRecurso(material.getTema(), ids).ifPresent(motivo -> {
                throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el recurso", material.getTitulo(), motivo,
                        "Registrá otro antes de darlo de baja."));
            });
            // El diseño del curso no puede pasar de completo a incompleto mientras el curso tiene cohortes activas
            programaService.buscarMotivoDeDisenoIncompleto(material.getTema(), ids).ifPresent(motivo -> {
                throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el recurso", material.getTitulo(), motivo,
                        "Registrá otro antes de darlo de baja."));
            });
        }
    }

    /** Da de baja el uso del recurso en el tema. El recurso sigue en la biblioteca para reutilizarlo en otro tema. */
    private void darDeBaja(TemaRecurso material) {
        material.marcarInactivo();
        material.setUltimaModificacion(LocalDateTime.now());
        temaRecursoRepository.save(material);
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Indica si el docente es el dueño del recurso del material (solo él puede modificar el recurso). */
    @Override
    public boolean esDuenio(TemaRecurso material, Docente docente) {
        return docente != null && material.getRecurso().getDocente().getIdDocente() == docente.getIdDocente();
    }

    /** Cuenta los otros temas activos que usan el mismo recurso. */
    private boolean participaEnElCurso(Curso curso, Docente docente) {
        return docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
    }

    private DocenteCurso obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
    }

    /** Valida los datos que pide cada tipo de material. */
    private void validarDatos(TipoRecurso tipo, DatosRecurso datos, boolean hayArchivo, boolean esAlta) {
        if (datos.titulo() == null || datos.titulo().isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("titulo", "Error! Debe completar el título.");
        }
        largoMaximo("titulo", "El título", datos.titulo().trim(), LARGO_TITULO);
        largoMaximo("descripcion", "La descripción", convertirVacioANulo(datos.descripcion()), LARGO_DESCRIPCION);
        String autor = convertirVacioANulo(datos.autor());
        if ((tipo == TipoRecurso.PRESENTACION || tipo == TipoRecurso.RESUMEN || tipo == TipoRecurso.BIBLIOGRAFIA) && autor == null) {
            throw RespuestaFormularioUtil.armarCampoInvalido("autor", "Error! Debe completar el autor.");
        }
        largoMaximo("autor", "El autor", autor, LARGO_AUTOR);
        if (datos.paginas() != null && datos.paginas() <= 0) {
            throw RespuestaFormularioUtil.armarCampoInvalido("paginas", MensajeErrorUtil.armarFormatoInvalido("La cantidad de páginas", "número entero mayor a cero"));
        }
        if (datos.duracionMinutos() != null && datos.duracionMinutos() <= 0) {
            throw RespuestaFormularioUtil.armarCampoInvalido("duracionMinutos", MensajeErrorUtil.armarFormatoInvalido("La duración", "número entero de minutos mayor a cero"));
        }
        String enlace = convertirVacioANulo(datos.enlace());
        if (tipo == TipoRecurso.ENLACE) {
            if (enlace == null) {
                throw RespuestaFormularioUtil.armarCampoInvalido("enlace", "Error! Debe ingresar el enlace.");
            }
            largoMaximo("enlace", "El enlace", enlace, LARGO_ENLACE);
            if (!ManejadorArchivosUtil.esEnlaceExterno(enlace)) {
                throw RespuestaFormularioUtil.armarCampoInvalido("enlace", MensajeErrorUtil.armarFormatoInvalido("El enlace", "comienza con http:// o https://"));
            }
        } else if (enlace != null) {
            throw RespuestaFormularioUtil.armarCampoInvalido("enlace", "Error! Este tipo de recurso no admite un enlace externo.");
        }
        if (tipo == TipoRecurso.BIBLIOGRAFIA) {
            String isbn = convertirVacioANulo(datos.isbn());
            if (isbn == null) {
                throw RespuestaFormularioUtil.armarCampoInvalido("isbn", "Error! Debe completar el ISBN.");
            }
            largoMaximo("isbn", "El ISBN", isbn, LARGO_ISBN);
            largoMaximo("editorial", "La editorial", convertirVacioANulo(datos.editorial()), LARGO_EDITORIAL);
        }
        if (tipo == TipoRecurso.ENLACE && hayArchivo) {
            throw RespuestaFormularioUtil.armarCampoInvalido("enlace", "Error! Este tipo de recurso no lleva archivo.");
        }
        if (esAlta && !hayArchivo && tipo != TipoRecurso.ENLACE) {
            String mensaje = switch (tipo) {
                case GRABACION -> "Error! Debe seleccionar el archivo de video.";
                case RESUMEN -> "Error! Debe seleccionar el archivo del resumen.";
                case BIBLIOGRAFIA -> "Error! Debe seleccionar el archivo de la bibliografía.";
                default -> "Error! Debe seleccionar el archivo de la presentación.";
            };
            throw RespuestaFormularioUtil.armarCampoInvalido("archivo", mensaje);
        }
    }

    /** Valida que no haya dos bibliografías vigentes con el mismo ISBN, ignorando guiones y espacios. */
    private void validarIsbnNoRepetido(TipoRecurso tipo, DatosRecurso datos, int idRecursoActual) {
        String isbn = tipo == TipoRecurso.BIBLIOGRAFIA ? convertirVacioANulo(datos.isbn()) : null;
        if (isbn == null) {
            return;
        }
        String normalizado = isbn.replace("-", "").replace(" ", "").toUpperCase();
        if (bibliografiaRepository.existePorIsbn(normalizado, idRecursoActual)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("isbn", "Error! Ya existe una bibliografía registrada con ese ISBN.");
        }
    }

    private void largoMaximo(String campo, String nombre, String valor, int maximo) {
        if (valor != null && valor.length() > maximo) {
            throw RespuestaFormularioUtil.armarCampoInvalido(campo, MensajeErrorUtil.armarDemasiadoLargo(nombre, maximo, "caracteres"));
        }
    }

    /** Crea la tabla con los datos propios del tipo de recurso. */
    private void crearDatosPropios(Recurso recurso, TipoRecurso tipo, DatosRecurso datos, String ruta, Long tamanio) {
        String extension = ruta == null ? null : ManejadorArchivosUtil.obtenerExtension(ruta);
        switch (tipo) {
            case PRESENTACION, RESUMEN -> {
                String nombreCategoria = tipo == TipoRecurso.RESUMEN ? CategoriaMaterialLectura.RESUMEN : CategoriaMaterialLectura.PRESENTACION;
                CategoriaMaterialLectura categoria = categoriaMaterialLecturaRepository.findByNombre(nombreCategoria)
                        .orElseThrow(() -> new IllegalStateException("Falta la categoría de material de lectura '" + nombreCategoria + "'."));
                MaterialLectura lectura = new MaterialLectura(recurso, categoria, ruta, convertirVacioANulo(datos.autor()));
                lectura.setPaginas(datos.paginas());
                lectura.setTamanioBytes(tamanio);
                lectura.setExtension(extension);
                recurso.setMaterialLectura(materialLecturaRepository.save(lectura));
            }
            case BIBLIOGRAFIA -> {
                Bibliografia bibliografia = new Bibliografia(recurso, convertirVacioANulo(datos.isbn()), convertirVacioANulo(datos.autor()));
                bibliografia.setEditorial(convertirVacioANulo(datos.editorial()));
                bibliografia.setPaginas(datos.paginas());
                bibliografia.setFechaPublicacion(datos.fechaPublicacion());
                bibliografia.setRutaArchivo(ruta);
                bibliografia.setTamanioBytes(tamanio);
                bibliografia.setExtension(extension);
                recurso.setBibliografia(bibliografiaRepository.save(bibliografia));
            }
            case GRABACION -> {
                Grabacion grabacion = new Grabacion(recurso, ruta);
                grabacion.setTamanioBytes(tamanio);
                grabacion.setDuracionMinutos(datos.duracionMinutos());
                recurso.setGrabacion(grabacionRepository.save(grabacion));
            }
            case ENLACE -> recurso.setEnlace(enlaceRepository.save(new Enlace(recurso, convertirVacioANulo(datos.enlace()))));
            default -> throw new IllegalArgumentException("Error! Este tipo de recurso no se puede cargar manualmente.");
        }
    }

    /** Actualiza los datos propios del tipo de recurso. Si hay un archivo nuevo, también su ruta. */
    private void actualizarDatosPropios(Recurso recurso, TipoRecurso tipo, DatosRecurso datos, String rutaNueva, Long tamanio) {
        String extension = rutaNueva == null ? null : ManejadorArchivosUtil.obtenerExtension(rutaNueva);
        switch (tipo) {
            case PRESENTACION, RESUMEN -> {
                MaterialLectura lectura = recurso.getMaterialLectura();
                lectura.setAutor(convertirVacioANulo(datos.autor()));
                lectura.setPaginas(datos.paginas());
                if (rutaNueva != null) {
                    lectura.setRutaArchivo(rutaNueva);
                    lectura.setTamanioBytes(tamanio);
                    lectura.setExtension(extension);
                }
                materialLecturaRepository.save(lectura);
            }
            case BIBLIOGRAFIA -> {
                Bibliografia bibliografia = recurso.getBibliografia();
                bibliografia.setIsbn(convertirVacioANulo(datos.isbn()));
                bibliografia.setAutor(convertirVacioANulo(datos.autor()));
                bibliografia.setEditorial(convertirVacioANulo(datos.editorial()));
                bibliografia.setPaginas(datos.paginas());
                bibliografia.setFechaPublicacion(datos.fechaPublicacion());
                if (rutaNueva != null) {
                    bibliografia.setRutaArchivo(rutaNueva);
                    bibliografia.setTamanioBytes(tamanio);
                    bibliografia.setExtension(extension);
                }
                bibliografiaRepository.save(bibliografia);
            }
            case GRABACION -> {
                Grabacion grabacion = recurso.getGrabacion();
                grabacion.setDuracionMinutos(datos.duracionMinutos());
                if (rutaNueva != null) {
                    grabacion.setRutaArchivo(rutaNueva);
                    grabacion.setTamanioBytes(tamanio);
                }
                grabacionRepository.save(grabacion);
            }
            case ENLACE -> {
                Enlace enlace = recurso.getEnlace();
                enlace.setUrl(convertirVacioANulo(datos.enlace()));
                enlaceRepository.save(enlace);
            }
            default -> throw new IllegalArgumentException("Error! Este tipo de recurso no se puede modificar manualmente.");
        }
    }

    /** Guarda el archivo de un recurso en la carpeta de la biblioteca de su docente: los temas solo lo usan, no lo guardan. */
    private String guardarArchivo(Recurso recurso, TipoRecurso tipo, MultipartFile archivo) {
        Path carpeta = ManejadorArchivosUtil.resolverCarpetaDeBiblioteca(directorioCursos, recurso.getDocente().getIdDocente());
        String nombre = ManejadorArchivosUtil.guardar(archivo, carpeta, "recurso" + recurso.getIdRecurso(),
                tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        return ManejadorArchivosUtil.construirRutaRelativa(directorioCursos, carpeta.resolve(nombre));
    }

    private String convertirVacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
