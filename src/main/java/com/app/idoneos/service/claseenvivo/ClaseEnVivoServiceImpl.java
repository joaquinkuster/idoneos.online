package com.app.idoneos.service.claseenvivo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.EstadoClaseEnVivo;
import com.app.idoneos.model.EstadoCohorte;
import com.app.idoneos.model.Grabacion;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Modalidad;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.repository.ClaseEnVivoRepository;
import com.app.idoneos.repository.CohorteRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.EstadoClaseEnVivoRepository;
import com.app.idoneos.repository.GrabacionRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.RecursoRepository;
import com.app.idoneos.repository.TemaRecursoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.inscripcion.InscripcionService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.service.streaming.StreamingService;
import com.app.idoneos.service.streaming.StreamingService.DatosDeConexion;
import com.app.idoneos.service.streaming.StreamingService.EstadoDeSenal;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Implementación de ClaseEnVivoService. */
@Service
public class ClaseEnVivoServiceImpl implements ClaseEnVivoService {

    /** Largo máximo del título de la clase. */
    private static final int LONGITUD_TITULO = 50;

    private static final Logger REGISTRO = LoggerFactory.getLogger(ClaseEnVivoServiceImpl.class);

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Estados de la cohorte en los que se pueden programar clases en vivo. */
    private static final List<String> ESTADOS_CON_CLASES = List.of(EstadoCohorte.PROXIMA, EstadoCohorte.ABIERTA,
            EstadoCohorte.PENDIENTE, EstadoCohorte.EN_DICTADO);

    /** Datos de una solicitud ya leídos y validados. */
    private record Datos(String titulo, LocalDateTime inicio, LocalDateTime cierre) {
    }

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;

    @Autowired
    private EstadoClaseEnVivoRepository estadoClaseEnVivoRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private StreamingService streamingService;

    @Autowired
    private ChatClaseService chatService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private GrabacionRepository grabacionRepository;

    @Autowired
    private TemaRecursoRepository temaRecursoRepository;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Autowired
    private PlatformTransactionManager administradorDeTransacciones;

    private TransactionTemplate transacciones;

    /** Hilo en el que se procesan las grabaciones, una por vez, sin hacer esperar al docente ni a los alumnos. */
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor(tarea -> {
        Thread hilo = new Thread(tarea, "publicacion-de-grabaciones");
        hilo.setDaemon(true);
        return hilo;
    });

    @PostConstruct
    void iniciarTransacciones() {
        transacciones = new TransactionTemplate(administradorDeTransacciones);
    }

    @PreDestroy
    void detenerEjecutor() {
        ejecutor.shutdown();
    }

    /** Última vez que se vio la señal de cada clase en vivo, para saber cuánto hace que se cortó. */
    private final Map<Integer, LocalDateTime> ultimaSenal = new ConcurrentHashMap<>();

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la clase en vivo por su identificador. */
    @Override
    public Optional<ClaseEnVivo> buscarPorId(Integer id) {
        return claseEnVivoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<ClaseEnVivo> obtenerTodo() {
        return claseEnVivoRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return claseEnVivoRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de claseEnVivo asociados a cohorte. */
    @Override
    public List<ClaseEnVivo> buscarPorCohorte(Cohorte cohorte) {
        return claseEnVivoRepository.findByCohorteAndBajaFalse(cohorte);
    }

    /** Obtiene el lugar del estado en el listado: En vivo, Programada, Finalizada y Cancelada. */
    private static int ordenDeEstado(ClaseEnVivo clase) {
        if (clase.esEnVivo()) {
            return 0;
        }
        if (clase.esProgramada()) {
            return 1;
        }
        return clase.esFinalizada() ? 2 : 3;
    }

    /** Busca las clases en vivo de un curso (incluidas las dadas de baja) aplicando filtros opcionales. */
    @Override
    public List<ClaseEnVivo> buscarConFiltros(Integer idCurso, Boolean baja, Docente soloDeDocente) {
        return claseEnVivoRepository.buscarPorFiltros(idCurso, baja).stream()
                .filter(c -> soloDeDocente == null || participaEnElCurso(c.getCohorte().getCurso(), soloDeDocente))
                // Primero las que están en vivo y las programadas, de la más próxima a la más lejana. Después las finalizadas y, al
                // final, las canceladas, de la más reciente a la más antigua
                .sorted(Comparator.comparing(ClaseEnVivo::getBaja)
                        .thenComparingInt(ClaseEnVivoServiceImpl::ordenDeEstado)
                        .thenComparing((x, y) -> x.esEnVivo() || x.esProgramada() ? x.getFechaHoraInicio().compareTo(y.getFechaHoraInicio())
                                : y.getFechaHoraInicio().compareTo(x.getFechaHoraInicio())))
                .toList();
    }

    /** Busca la clase en vivo vigente que identifica un camino del servidor de medios. */
    @Override
    public Optional<ClaseEnVivo> buscarPorCamino(String camino) {
        if (camino == null || !camino.startsWith("clase-")) {
            return Optional.empty();
        }
        try {
            return buscarPorId(Integer.parseInt(camino.substring("clase-".length())));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Obtiene los minutos que se espera la reconexión del docente antes de cerrar sola una clase sin señal. */
    @Override
    public int minutosDeEsperaSinSenal() {
        return Math.max(1, parametroService.obtenerEntero(Parametro.CLASE_CORTE_SENAL_MINUTOS,
                Parametro.CLASE_CORTE_SENAL_MINUTOS_POR_DEFECTO));
    }

    /** Obtiene los minutos de anticipación con que se puede iniciar una clase. */
    @Override
    public int minutosDeAnticipacion() {
        return Math.max(0, parametroService.obtenerEntero(Parametro.CLASE_ANTICIPACION_MINUTOS,
                Parametro.CLASE_ANTICIPACION_MINUTOS_POR_DEFECTO));
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la clase en vivo en la base de datos. */
    @Override
    public ClaseEnVivo guardar(ClaseEnVivo entidad) {
        return claseEnVivoRepository.save(entidad);
    }

    /** Programa una clase en vivo para una cohorte activa. */
    @Override
    @Transactional
    public ClaseEnVivo registrar(Docente docente, SolicitudClaseEnVivo solicitud) {
        Cohorte cohorte = cohorteRepository.findById(solicitud.idCohorte() == null ? -1 : solicitud.idCohorte())
                .filter(c -> !c.getBaja())
                .orElseThrow(() -> RespuestaFormularioUtil.armarCampoInvalido("idCohorte",
                        "Error! La cohorte no se encuentra activa."));
        if (!ESTADOS_CON_CLASES.contains(cohorte.getEstado())) {
            throw RespuestaFormularioUtil.armarCampoInvalido("idCohorte", "Error! Solo se programan clases para cohortes en "
                    + "inscripción próxima, abierta, pendiente de contenido o en dictado.");
        }
        Curso curso = cohorte.getCurso();
        DocenteCurso participacion = obtenerParticipacion(curso, docente);
        if (!curso.incluyeModalidad(Modalidad.EN_VIVO)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("idCohorte",
                    "Error! El curso no tiene habilitada la modalidad en vivo.");
        }
        Tema tema = temaRepository.findById(solicitud.idTema() == null ? -1 : solicitud.idTema())
                .filter(t -> !t.esInactivo() && t.getCurso().getIdCurso() == curso.getIdCurso())
                .orElseThrow(() -> RespuestaFormularioUtil.armarCampoInvalido("idTema",
                        "Error! Debe seleccionar un tema del curso."));
        Datos datos = leer(solicitud, cohorte, docente, null);

        ClaseEnVivo clase = new ClaseEnVivo(participacion, estado(EstadoClaseEnVivo.PROGRAMADA), tema, cohorte,
                datos.titulo(), datos.inicio(), datos.cierre());
        return claseEnVivoRepository.save(clase);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public ClaseEnVivo modificar(ClaseEnVivo entidad) {
        return claseEnVivoRepository.save(entidad);
    }

    /** Modifica el título, la fecha y hora de inicio y de cierre, y la visibilidad de una clase programada. */
    @Override
    @Transactional
    public ClaseEnVivo modificar(Integer idClaseEnVivo, Docente docente, SolicitudClaseEnVivo solicitud) {
        ClaseEnVivo clase = claseProgramadaDelDocente(idClaseEnVivo, docente);
        Datos datos = leer(solicitud, clase.getCohorte(), docente, clase);
        clase.setTitulo(datos.titulo());
        clase.setFechaHoraInicio(datos.inicio());
        clase.setFechaHoraCierre(datos.cierre());
        clase.setUltimaModificacion(LocalDateTime.now());
        return claseEnVivoRepository.save(clase);
    }

    /** Cancela una clase programada que todavía no se transmitió. */
    @Override
    @Transactional
    public void cancelar(Integer idClaseEnVivo, Docente docente) {
        cancelarVarias(List.of(idClaseEnVivo == null ? -1 : idClaseEnVivo), docente);
    }

    /** Cancela varias clases programadas, todas o ninguna. */
    @Override
    @Transactional
    public void cancelarVarias(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos una clase en vivo.");
        }
        List<ClaseEnVivo> clases = ids.stream().distinct().map(id -> claseProgramadaDelDocente(id, docente)).toList();
        EstadoClaseEnVivo cancelada = estado(EstadoClaseEnVivo.CANCELADA);
        LocalDateTime ahora = LocalDateTime.now();
        for (ClaseEnVivo clase : clases) {
            clase.setEstadoClaseEnVivo(cancelada);
            clase.setUltimaModificacion(ahora);
            claseEnVivoRepository.save(clase);
        }
    }

    /** Cancela las clases programadas que no se iniciaron dentro de la tolerancia de espera. */
    @Override
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    @Transactional
    public int cancelarNoIniciadas() {
        int tolerancia = Math.max(0, parametroService.obtenerEntero(Parametro.CLASE_TOLERANCIA_MINUTOS,
                Parametro.CLASE_TOLERANCIA_MINUTOS_POR_DEFECTO));
        LocalDateTime ahora = LocalDateTime.now();
        EstadoClaseEnVivo cancelada = estado(EstadoClaseEnVivo.CANCELADA);
        List<ClaseEnVivo> vencidas = claseEnVivoRepository
                .findByEstadoClaseEnVivoAndBajaFalse(estado(EstadoClaseEnVivo.PROGRAMADA)).stream()
                .filter(c -> c.getFechaHoraInicio().plusMinutes(tolerancia).isBefore(ahora)).toList();
        for (ClaseEnVivo clase : vencidas) {
            clase.setEstadoClaseEnVivo(cancelada);
            clase.setUltimaModificacion(ahora);
            claseEnVivoRepository.save(clase);
        }
        return vencidas.size();
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(ClaseEnVivo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        claseEnVivoRepository.save(entidad);
    }

    /** Da de baja una clase finalizada, junto con su grabación si la tiene. */
    @Override
    @Transactional
    public void darDeBaja(Integer idClaseEnVivo) {
        darDeBajaVarias(List.of(idClaseEnVivo == null ? -1 : idClaseEnVivo));
    }

    /** Da de baja varias clases finalizadas, todas o ninguna. */
    @Override
    @Transactional
    public void darDeBajaVarias(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos una clase en vivo.");
        }
        List<ClaseEnVivo> clases = ids.stream().distinct().map(this::claseActiva).toList();
        LocalDateTime ahora = LocalDateTime.now();
        for (ClaseEnVivo clase : clases) {
            if (clase.esEnVivo()) {
                streamingService.cortarSenal(clase);
                chatService.cerrar(clase.getIdClaseEnVivo());
                ultimaSenal.remove(clase.getIdClaseEnVivo());
            }
            clase.marcarInactivo();
            clase.setUltimaModificacion(ahora);
            if (clase.getGrabacion() != null) {
                darDeBajaGrabacion(clase.getGrabacion().getRecurso());
            }
            claseEnVivoRepository.save(clase);
        }
    }

    /** Da de baja el recurso de la grabación y su publicación en los temas. */
    private void darDeBajaGrabacion(Recurso recurso) {
        // Primero las publicaciones, con flush, porque el recurso no se puede dar de baja con temas activos
        recurso.getTemasRecurso().forEach(publicacion -> publicacion.marcarInactivo());
        temaRecursoRepository.saveAllAndFlush(recurso.getTemasRecurso());
        recurso.marcarInactivo();
        try {
            recursoRepository.saveAndFlush(recurso);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            Throwable causa = e.getMostSpecificCause();
            throw new IllegalArgumentException(causa.getMessage() != null ? causa.getMessage()
                    : "Error! No se pudo dar de baja la grabación de la clase.");
        }
    }

    // ----------------------------------------------------------------------
    // Transmisión
    // ----------------------------------------------------------------------

    /** Inicia la transmisión de una clase programada: genera los datos de conexión y la pasa al estado En vivo. */
    @Override
    @Transactional
    public DatosDeConexion iniciar(Integer idClaseEnVivo, Docente docente) {
        ClaseEnVivo clase = claseProgramadaDelDocente(idClaseEnVivo, docente);
        LocalDateTime ahora = LocalDateTime.now();
        int anticipacion = minutosDeAnticipacion();
        if (!clase.puedeIniciarse(ahora, anticipacion)) {
            throw new IllegalArgumentException("Error! Todavía no podés iniciar la clase «" + clase.getTitulo()
                    + "»: se puede iniciar desde las " + clase.getFechaHoraInicio().minusMinutes(anticipacion).format(FORMATO_FECHA_HORA)
                    + ".");
        }
        DatosDeConexion datos = streamingService.generarConexion(clase);
        clase.setUrlRtmp(datos.urlRtmp());
        clase.setClaveStream(datos.resumen());
        clase.setEstadoClaseEnVivo(estado(EstadoClaseEnVivo.EN_VIVO));
        clase.setUltimaModificacion(ahora);
        claseEnVivoRepository.save(clase);
        ultimaSenal.remove(clase.getIdClaseEnVivo());
        return datos;
    }

    /** Genera una clave de transmisión nueva para una clase en vivo, y corta la señal que usaba la anterior. */
    @Override
    @Transactional
    public DatosDeConexion regenerarClave(Integer idClaseEnVivo, Docente docente) {
        ClaseEnVivo clase = buscarEnVivoDelDocente(idClaseEnVivo, docente);
        streamingService.cortarSenal(clase);
        DatosDeConexion datos = streamingService.generarConexion(clase);
        clase.setUrlRtmp(datos.urlRtmp());
        clase.setClaveStream(datos.resumen());
        clase.setUltimaModificacion(LocalDateTime.now());
        claseEnVivoRepository.save(clase);
        ultimaSenal.remove(clase.getIdClaseEnVivo());
        return datos;
    }

    /** Finaliza la transmisión: corta la señal, pasa la clase a Finalizada y publica su grabación en el tema de la clase. */
    @Override
    @Transactional
    public ClaseEnVivo finalizar(Integer idClaseEnVivo, Docente docente) {
        ClaseEnVivo clase = buscarEnVivoDelDocente(idClaseEnVivo, docente);
        if (clase.getTema().esInactivo()) {
            throw new IllegalArgumentException("Error! El tema «" + clase.getTema().getTitulo()
                    + "» de la clase no se encuentra activo: no se puede publicar la grabación.");
        }
        return cerrarTransmision(clase);
    }

    /** Busca la clase que el docente está transmitiendo. */
    @Override
    public ClaseEnVivo buscarEnVivoDelDocente(Integer idClaseEnVivo, Docente docente) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(idClaseEnVivo == null ? -1 : idClaseEnVivo).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La clase en vivo no se encuentra activa."));
        obtenerParticipacion(clase.getCohorte().getCurso(), docente);
        if (!clase.esDeDocente(docente)) {
            throw new IllegalArgumentException("Error! La clase en vivo «" + clase.getTitulo()
                    + "» la programó otro docente: solo quien la programó puede transmitirla.");
        }
        if (!clase.esEnVivo()) {
            throw new IllegalArgumentException("Error! La clase en vivo «" + clase.getTitulo() + "» no se está transmitiendo.");
        }
        return clase;
    }

    /** Verifica que un alumno pueda ingresar a la transmisión de una clase. */
    @Override
    public ClaseEnVivo autorizarIngreso(Integer idClaseEnVivo, Alumno alumno) {
        ClaseEnVivo clase = buscarPorId(idClaseEnVivo == null ? -1 : idClaseEnVivo)
                .orElseThrow(() -> new IllegalArgumentException("Error! La clase en vivo no se encuentra disponible."));
        if (alumno == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
        }
        Inscripcion inscripcion = inscripcionRepository.findByAlumnoAndBajaFalse(alumno).stream()
                .filter(i -> i.getCohorte().getIdCohorte() == clase.getCohorte().getIdCohorte()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No tenés una inscripción vigente a esta clase."));
        inscripcionService.validarAcceso(inscripcion.getIdInscripcion(), alumno);
        if (!clase.esEnVivo() ) {
            throw new IllegalArgumentException("Error! La transmisión no está disponible en este momento.");
        }
        return clase;
    }

    /** Obtiene el papel con el que un usuario puede estar en la sala de una clase. */
    @Override
    public Optional<RolEnSala> rolEnSala(ClaseEnVivo clase, com.app.idoneos.model.Usuario usuario) {
        if (usuario == null) {
            return Optional.empty();
        }
        if (usuario.getDocente() != null && clase.esDeDocente(usuario.getDocente())) {
            return Optional.of(RolEnSala.DOCENTE);
        }
        Alumno alumno = usuario.getAlumno();
        if (alumno == null ) {
            return Optional.empty();
        }
        return inscripcionRepository.findByAlumnoAndBajaFalse(alumno).stream()
                .filter(i -> i.getCohorte().getIdCohorte() == clase.getCohorte().getIdCohorte()).findFirst()
                .filter(i -> {
                    try {
                        inscripcionService.validarAcceso(i.getIdInscripcion(), alumno);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                }).map(i -> RolEnSala.ALUMNO);
    }

    /** Cierra las clases en vivo cuya señal se cortó hace más del tiempo que se espera la reconexión. */
    @Override
    @Scheduled(fixedDelay = 30_000, initialDelay = 60_000)
    @Transactional
    public int cerrarTransmisionesSinSenal() {
        int espera = minutosDeEsperaSinSenal();
        LocalDateTime ahora = LocalDateTime.now();
        int cerradas = 0;
        for (ClaseEnVivo clase : claseEnVivoRepository.findByEstadoClaseEnVivoAndBajaFalse(estado(EstadoClaseEnVivo.EN_VIVO))) {
            // Si no se puede consultar al servidor de medios no se cierra nada, porque no se sabe si hay señal
            EstadoDeSenal senal = streamingService.consultarSenal(clase).orElse(null);
            if (senal == null) {
                continue;
            }
            if (senal.transmitiendo()) {
                ultimaSenal.put(clase.getIdClaseEnVivo(), ahora);
                continue;
            }
            LocalDateTime desde = ultimaSenal.getOrDefault(clase.getIdClaseEnVivo(),
                    clase.getUltimaModificacion() != null ? clase.getUltimaModificacion() : clase.getFechaHoraInicio());
            if (desde.plusMinutes(espera).isAfter(ahora)) {
                continue;
            }
            if (streamingService.buscarGrabaciones(clase).isEmpty()) {
                cancelarSinSenal(clase);
            } else {
                cerrarTransmision(clase);
            }
            cerradas++;
        }
        return cerradas;
    }

    /** Elimina las grabaciones que pasaron su fecha de vencimiento. */
    @Override
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 120_000)
    @Transactional
    public int vencerGrabaciones() {
        int eliminadas = 0;
        for (ClaseEnVivo clase : claseEnVivoRepository
                .findByGrabacionIsNotNullAndFechaVencimientoGrabacionBefore(LocalDate.now())) {
            Recurso recurso = clase.getGrabacion().getRecurso();
            String ruta = clase.getGrabacion().getRutaArchivo();
            // Un recurso dado de baja antes del vencimiento conserva su archivo hasta esta fecha: ahora se borra
            Path archivo = ManejadorArchivosUtil.resolver(directorioCursos, ruta);
            boolean hayArchivo = archivo != null && Files.isRegularFile(archivo);
            if (recurso.esInactivo() && !hayArchivo) {
                continue;
            }
            try {
                if (!recurso.esInactivo()) {
                    darDeBajaGrabacion(recurso);
                }
                if (hayArchivo) {
                    ManejadorArchivosUtil.eliminar(ruta, directorioCursos);
                }
                eliminadas++;
            } catch (RuntimeException e) {
                REGISTRO.warn("No se pudo vencer la grabación de la clase {}: {}", clase.getIdClaseEnVivo(), e.getMessage());
            }
        }
        return eliminadas;
    }

    /** Cancela una clase que se inició pero de la que nunca llegó señal. */
    private void cancelarSinSenal(ClaseEnVivo clase) {
        clase.setEstadoClaseEnVivo(estado(EstadoClaseEnVivo.CANCELADA));
        clase.setUltimaModificacion(LocalDateTime.now());
        claseEnVivoRepository.save(clase);
        chatService.cerrar(clase.getIdClaseEnVivo());
        ultimaSenal.remove(clase.getIdClaseEnVivo());
    }

    /**
     * Corta la señal, pasa la clase a Finalizada y borra el chat, todo enseguida para que los alumnos se enteren al instante.
     * La grabación se publica después, en segundo plano, porque procesarla puede tardar.
     */
    private ClaseEnVivo cerrarTransmision(ClaseEnVivo clase) {
        LocalDateTime inicio = clase.getUltimaModificacion() != null ? clase.getUltimaModificacion() : clase.getFechaHoraInicio();
        streamingService.cortarSenal(clase);
        clase.setEstadoClaseEnVivo(estado(EstadoClaseEnVivo.FINALIZADA));
        LocalDateTime fin = LocalDateTime.now();
        clase.setUltimaModificacion(fin);
        chatService.cerrar(clase.getIdClaseEnVivo());
        ultimaSenal.remove(clase.getIdClaseEnVivo());
        ClaseEnVivo guardada = claseEnVivoRepository.save(clase);
        programarPublicacion(guardada.getIdClaseEnVivo(), inicio, fin);
        return guardada;
    }

    /** Publica la grabación en segundo plano, una vez confirmado el cambio de estado de la clase. */
    private void programarPublicacion(int idClase, LocalDateTime inicio, LocalDateTime fin) {
        Runnable tarea = () -> {
            try {
                transacciones.executeWithoutResult(estadoTransaccion -> claseEnVivoRepository.findById(idClase)
                        .filter(c -> c.getGrabacion() == null).ifPresent(c -> publicarGrabacion(c, inicio, fin)));
            } catch (RuntimeException e) {
                REGISTRO.error("No se pudo publicar la grabación de la clase {}", idClase, e);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ejecutor.execute(tarea);
                }
            });
        } else {
            ejecutor.execute(tarea);
        }
    }

    /** Publica la grabación de una clase finalizada como recurso de su tema. */
    @Override
    @Transactional
    public ClaseEnVivo publicarGrabacion(Integer idClaseEnVivo) {
        ClaseEnVivo clase = claseActiva(idClaseEnVivo);
        if (!clase.esFinalizada() || clase.getGrabacion() != null) {
            throw new IllegalArgumentException("Error! La clase no está finalizada o ya tiene su grabación.");
        }
        publicarGrabacion(clase, clase.getFechaHoraInicio(), LocalDateTime.now());
        return claseEnVivoRepository.save(clase);
    }

    /** Carga la grabación de la clase como recurso publicado de su tema. Si falla, la clase queda finalizada sin grabación. */
    private void publicarGrabacion(ClaseEnVivo clase, LocalDateTime inicio, LocalDateTime fin) {
        try {
            List<Path> archivos = streamingService.buscarGrabaciones(clase);
            if (archivos.isEmpty()) {
                REGISTRO.warn("El servidor no generó la grabación de la clase {}", clase.getIdClaseEnVivo());
                return;
            }
            // Si la señal se cortó y volvió hay un archivo por cada tramo, y se publica el más largo
            Path origen = archivos.stream().max(java.util.Comparator.comparingLong(a -> a.toFile().length())).orElseThrow();
            Docente docente = clase.getDocenteCurso().getDocente();
            Recurso recurso = new Recurso(docente, "Grabación: " + clase.getTitulo());
            recurso.setFechaAceptacionTyc(LocalDateTime.now());
            recurso = recursoRepository.saveAndFlush(recurso);
            // El archivo se llama como los demás recursos de la biblioteca, con su tipo y el identificador del recurso
            Path carpeta = ManejadorArchivosUtil.resolverCarpetaDeBiblioteca(directorioCursos, docente.getIdDocente());
            Path destino = carpeta.resolve("clase" + recurso.getIdRecurso() + ".mp4");
            try {
                streamingService.exportarGrabacion(origen, destino);
            } catch (IOException e) {
                recurso.marcarInactivo();
                recursoRepository.save(recurso);
                throw e;
            }
            Grabacion grabacion = new Grabacion(recurso, ManejadorArchivosUtil.construirRutaRelativa(directorioCursos, destino));
            grabacion.setTamanioBytes(Files.size(destino));
            grabacion.setDuracionMinutos((int) Math.max(1, Duration.between(inicio, fin).toMinutes()));
            grabacion = grabacionRepository.save(grabacion);
            recurso.setGrabacion(grabacion);

            Tema tema = clase.getTema();
            int orden = temaRecursoRepository.findByTemaAndBajaFalse(tema).stream().mapToInt(TemaRecurso::getNumeroOrden).max()
                    .orElse(0) + 1;
            TemaRecurso publicacion = temaRecursoRepository.save(new TemaRecurso(tema, recurso, clase.getDocenteCurso(), orden));
            tema.getTemasRecurso().add(publicacion);
            recurso.getTemasRecurso().add(publicacion);

            int meses = Math.max(1, parametroService.obtenerEntero(Parametro.CLASE_RETENCION_GRABACION_MESES,
                    Parametro.CLASE_RETENCION_GRABACION_MESES_POR_DEFECTO));
            clase.setGrabacion(grabacion);
            clase.setFechaVencimientoGrabacion(LocalDate.now().plusMonths(meses));
            claseEnVivoRepository.save(clase);
            streamingService.borrarGrabaciones(clase);
        } catch (IOException | RuntimeException e) {
            REGISTRO.error("No se pudo publicar la grabación de la clase {}", clase.getIdClaseEnVivo(), e);
        }
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    private EstadoClaseEnVivo estado(String nombre) {
        return estadoClaseEnVivoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("No está cargado el estado de clase en vivo " + nombre));
    }

    private boolean participaEnElCurso(Curso curso, Docente docente) {
        return docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
    }

    /** Verifica que el docente participe en el curso (como titular o ayudante) y devuelve su participación. */
    private DocenteCurso obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
    }

    /** Busca la clase activa y programada, y verifica que la haya registrado el docente. */
    private ClaseEnVivo claseProgramadaDelDocente(Integer id, Docente docente) {
        ClaseEnVivo clase = claseEnVivoRepository.findById(id == null ? -1 : id).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La clase en vivo no se encuentra activa."));
        obtenerParticipacion(clase.getCohorte().getCurso(), docente);
        if (!clase.esDeDocente(docente)) {
            throw new IllegalArgumentException("Error! La clase en vivo «" + clase.getTitulo()
                    + "» la programó otro docente: solo quien la programó puede gestionarla.");
        }
        if (!clase.esProgramada()) {
            throw new IllegalArgumentException("Error! La clase en vivo «" + clase.getTitulo()
                    + "» no se encuentra programada: solo se pueden gestionar las clases que todavía no se transmitieron.");
        }
        return clase;
    }

    /** Busca la clase activa, en cualquier estado. */
    private ClaseEnVivo claseActiva(Integer id) {
        return claseEnVivoRepository.findById(id == null ? -1 : id).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La clase en vivo no se encuentra activa."));
    }

    // ----------------------------------------------------------------------
    // Lectura y validación de los datos del formulario
    // ----------------------------------------------------------------------

    /** Lee y valida los datos de una solicitud: campos obligatorios, horario, fecha futura, dictado de la cohorte y agenda. */
    private Datos leer(SolicitudClaseEnVivo s, Cohorte cohorte, Docente docente, ClaseEnVivo actual) {
        if (s.titulo() == null || s.titulo().isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("titulo", "Error! Debe completar el título de la clase.");
        }
        String titulo = s.titulo().trim();
        if (titulo.length() > LONGITUD_TITULO) {
            throw RespuestaFormularioUtil.armarCampoInvalido("titulo",
                    MensajeErrorUtil.armarDemasiadoLargo("El título de la clase", LONGITUD_TITULO, "caracteres"));
        }
        if (s.fechaHoraInicio() == null || s.fechaHoraInicio().isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaHoraInicio", "Error! Debe completar la fecha y hora de inicio.");
        }
        if (s.fechaHoraCierre() == null || s.fechaHoraCierre().isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaHoraCierre", "Error! Debe completar la fecha y hora de cierre.");
        }
        LocalDateTime inicio = fecha(s.fechaHoraInicio(), "fechaHoraInicio", "La fecha y hora de inicio");
        LocalDateTime cierre = fecha(s.fechaHoraCierre(), "fechaHoraCierre", "La fecha y hora de cierre");
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaHoraInicio",
                    "Error! La fecha y hora de inicio deben ser posteriores al momento actual.");
        }
        if (!cierre.isAfter(inicio)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaHoraCierre",
                    "Error! La fecha y hora de cierre deben ser posteriores a las de inicio.");
        }
        int duracion = (int) Duration.between(inicio, cierre).toMinutes();
        int maximoHoras = Math.max(1, parametroService.obtenerEntero(Parametro.CLASE_DURACION_MAXIMA_HORAS,
                Parametro.CLASE_DURACION_MAXIMA_HORAS_POR_DEFECTO));
        if (duracion > maximoHoras * 60) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaHoraCierre", "Error! La clase no puede durar más de "
                    + maximoHoras + (maximoHoras == 1 ? " hora" : " horas") + ".");
        }
        validarDentroDelDictado(inicio, cohorte);
        validarAgenda(docente, inicio, duracion, actual);
        return new Datos(titulo, inicio, cierre);
    }

    private LocalDateTime fecha(String valor, String campo, String sujeto) {
        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw RespuestaFormularioUtil.armarCampoInvalido(campo, "Error! " + sujeto + " no es válida.");
        }
    }

    /** Verifica que la clase se dicte dentro del período de dictado de la cohorte. */
    private void validarDentroDelDictado(LocalDateTime inicioClase, Cohorte cohorte) {
        LocalDateTime inicio = cohorte.getFechaInicioDictado();
        if (inicio == null) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaHoraInicio",
                    "Error! La cohorte todavía no tiene fechas de dictado.");
        }
        LocalDateTime fin = cohorte.getFechaFinDictado() != null ? cohorte.getFechaFinDictado()
                : cohorte.getFechaFinInscripcion().plusDays(cohorte.getAccesoDias());
        if (inicioClase.isBefore(inicio) || inicioClase.isAfter(fin)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaHoraInicio", "Error! La fecha y hora de inicio deben estar dentro "
                    + "del período de dictado de la cohorte (del " + inicio.format(FORMATO_FECHA) + " al "
                    + fin.format(FORMATO_FECHA) + ").");
        }
    }

    /** Verifica que, con su duración estimada, la clase no se superponga con otra clase programada o en vivo del mismo docente. */
    private void validarAgenda(Docente docente, LocalDateTime inicio, int duracion, ClaseEnVivo actual) {
        LocalDateTime fin = inicio.plusMinutes(duracion);
        List<ClaseEnVivo> ocupadas = claseEnVivoRepository.buscarOcupadasDelDocente(docente,
                List.of(EstadoClaseEnVivo.PROGRAMADA, EstadoClaseEnVivo.EN_VIVO));
        for (ClaseEnVivo otra : ocupadas) {
            boolean esLaMisma = actual != null && otra.getIdClaseEnVivo() == actual.getIdClaseEnVivo();
            if (!esLaMisma && inicio.isBefore(otra.getFechaHoraCierre()) && otra.getFechaHoraInicio().isBefore(fin)) {
                throw RespuestaFormularioUtil.armarCampoInvalido("fechaHoraInicio", "Error! La clase se superpone con tu clase «"
                        + otra.getTitulo() + "» (" + otra.getFechaHoraInicio().format(FORMATO_FECHA_HORA) + " a "
                        + otra.getFechaHoraCierre().format(DateTimeFormatter.ofPattern("HH:mm")) + " h). Elegí otro horario.");
            }
        }
    }
}
