package com.app.idoneos.service.inscripcion;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.CohorteRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.ProgresoRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.pago.PagoService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import com.app.idoneos.util.TextoUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de InscripcionService. */
@Service
public class InscripcionServiceImpl implements InscripcionService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Longitud máxima de las observaciones (la de la columna) y del motivo de la baja. */
    private static final int LONGITUD_OBSERVACIONES = 500;
    private static final int LONGITUD_MOTIVO = 300;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private com.app.idoneos.repository.EstadoInscripcionRepository estadoInscripcionRepository;

    @Autowired
    private com.app.idoneos.repository.EstadoAvanceRepository estadoAvanceRepository;

    private com.app.idoneos.model.EstadoInscripcion estadoInscripcion(String nombre) {
        return estadoInscripcionRepository.findByNombre(nombre).orElseThrow();
    }

    private com.app.idoneos.model.EstadoAvance estadoAvance(String nombre) {
        return estadoAvanceRepository.findByNombre(nombre).orElseThrow();
    }

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private ProgresoRepository progresoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la inscripción por su identificador. */
    @Override
    public Optional<Inscripcion> buscarPorId(Integer id) {
        return inscripcionRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Inscripcion> obtenerTodo() {
        return inscripcionRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return inscripcionRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de inscripcion asociados a cohorte. */
    @Override
    public List<Inscripcion> buscarPorCohorte(Cohorte cohorte) {
        return inscripcionRepository.findByCohorteAndBajaFalse(cohorte);
    }

    /** Busca las inscripciones vigentes de un alumno. */
    @Override
    public List<Inscripcion> buscarMisCursos(Alumno alumno, String nombreCurso, String estado) {
        String criterio = nombreCurso == null ? "" : nombreCurso.trim().toLowerCase();
        return inscripcionRepository.findByAlumnoAndBajaFalse(alumno).stream()
                .filter(inscripcion -> criterio.isEmpty()
                        || inscripcion.getCurso().getNombre().toLowerCase().contains(criterio))
                .filter(inscripcion -> estado == null || estado.isBlank()
                        || inscripcion.getEstado().equalsIgnoreCase(estado.trim()))
                .sorted(Comparator.comparingInt(Inscripcion::getIdInscripcion).reversed())
                .toList();
    }

    /** Verifica que el alumno pueda acceder al curso de la inscripción. */
    @Override
    public Inscripcion validarAcceso(int idInscripcion, Alumno alumno) {
        Inscripcion inscripcion = buscarPorId(idInscripcion)
                .filter(i -> i.getAlumno().getIdAlumno() == alumno.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! No tenés una inscripción vigente a este curso."));
        Cohorte cohorte = inscripcion.getCohorte();
        if (!inscripcion.tieneAcceso() || cohorte.esInactivo()) {
            throw new IllegalArgumentException("Error! Tu inscripción a este curso no está habilitada.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (inscripcion.getFechaVencimientoAcceso() != null && ahora.isAfter(inscripcion.getFechaVencimientoAcceso())) {
            throw new IllegalArgumentException("Error! Tu acceso a este curso venció el "
                    + inscripcion.getFechaVencimientoAcceso().format(FORMATO_FECHA) + ".");
        }
        if (cohorte.getFechaInicioDictado() != null && ahora.isBefore(cohorte.getFechaInicioDictado())) {
            throw new IllegalArgumentException("Error! El dictado de tu cohorte comienza el "
                    + cohorte.getFechaInicioDictado().format(FORMATO_FECHA) + ".");
        }
        return inscripcion;
    }

    /** Busca la inscripción con la que un alumno puede ver el contenido de un tema. */
    @Override
    public Optional<Inscripcion> buscarConAccesoATema(Alumno alumno, Tema tema) {
        return inscripcionRepository.findByAlumnoAndBajaFalse(alumno).stream()
                .filter(inscripcion -> inscripcion.getCurso().getIdCurso() == tema.getCurso().getIdCurso())
                .filter(inscripcion -> tieneAccesoVigente(inscripcion, alumno))
                .filter(inscripcion -> inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream()
                        .filter(cronograma -> cronograma.getTema().getIdTema() == tema.getIdTema())
                        .findFirst().map(inscripcion::estaTemaHabilitada).orElse(false))
                .findFirst();
    }

    @Override
    public List<Inscripcion> buscarConFiltros(Integer cursoId, String alumno, String estado) {
        String criterio = TextoUtil.normalizar(alumno);
        return inscripcionRepository.findByBajaFalse().stream()
                .filter(i -> cursoId == null || i.getCurso().getIdCurso() == cursoId)
                .filter(i -> estado == null || estado.isBlank() || i.getNombreEstado().equalsIgnoreCase(estado.trim()))
                .filter(i -> criterio.isEmpty() || datosDelAlumno(i.getAlumno()).contains(criterio))
                .sorted(Comparator.comparing(Inscripcion::getFechaCreacion)
                        .thenComparingInt(Inscripcion::getIdInscripcion).reversed())
                .toList();
    }

    @Override
    public List<Inscripcion> buscarTodasConBajas() {
        return inscripcionRepository.findAll().stream()
                .sorted(Comparator.comparing(Inscripcion::getFechaCreacion)
                        .thenComparingInt(Inscripcion::getIdInscripcion).reversed())
                .toList();
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la inscripción en la base de datos. */
    @Override
    public Inscripcion guardar(Inscripcion entidad) {
        return inscripcionRepository.save(entidad);
    }

    @Override
    @Transactional
    public Inscripcion inscribir(Alumno alumno, Integer idCohorte, boolean terminosAceptados) {
        if (alumno == null) {
            throw new IllegalArgumentException("Error! Solo los alumnos pueden inscribirse a un curso.");
        }
        return crearInscripcion(alumno, idCohorte, terminosAceptados,
                "Error! Debe aceptar los términos y condiciones de las políticas de privacidad para inscribirse.");
    }

    @Override
    @Transactional
    public Inscripcion registrar(Integer idAlumno, Integer idCohorte, boolean terminosConfirmados) {
        if (idAlumno == null) {
            throw RespuestaFormularioUtil.armarCampoInvalido("alumnoId", "Error! Debe seleccionar el alumno.");
        }
        Alumno alumno = alumnoRepository.findById(idAlumno).filter(a -> !a.getUsuario().esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El alumno no se encuentra activo."));
        Inscripcion inscripcion = crearInscripcion(alumno, idCohorte, terminosConfirmados,
                "Error! Debe confirmar que el alumno acepta los términos y condiciones de las políticas de privacidad.");
        // Si el descuento del alumno cubre todo el precio, no hay nada que pagar: queda habilitada
        pagoService.habilitarSiNoHayQuePagar(inscripcion);
        return inscripcion;
    }

    // ----------------------------------------------------------------------
    // Cierre de la inscripción
    // ----------------------------------------------------------------------

    private static final org.slf4j.Logger REGISTRO = org.slf4j.LoggerFactory.getLogger(InscripcionServiceImpl.class);

    @Override
    @Transactional
    public int cancelarPendientesDeInscripcionCerrada() {
        LocalDateTime ahora = LocalDateTime.now();
        com.app.idoneos.model.EstadoInscripcion cancelada = estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.CANCELADA);
        String nota = "Cancelada automáticamente: terminó la inscripción sin que se pagara.";
        int canceladas = 0;
        for (Inscripcion inscripcion : inscripcionRepository.buscarDeCohortesConLaInscripcionCerrada(
                com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO, ahora)) {
            if (!inscripcion.sePuedeCancelarPorFaltaDePago()) {
                continue;
            }
            inscripcion.setEstadoInscripcion(cancelada);
            inscripcion.setUltimaModificacion(ahora);
            String anteriores = inscripcion.getObservaciones();
            inscripcion.setObservaciones(anteriores == null || anteriores.isBlank() ? nota
                    : (anteriores.length() + nota.length() + 1 <= LONGITUD_OBSERVACIONES ? anteriores + " " + nota : anteriores));
            inscripcionRepository.save(inscripcion);
            canceladas++;
        }
        return canceladas;
    }

    @Override
    @Transactional
    public int vencerAccesos() {
        LocalDateTime ahora = LocalDateTime.now();
        com.app.idoneos.model.EstadoInscripcion vencida = estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.VENCIDA);
        int vencidas = 0;
        for (Inscripcion inscripcion : inscripcionRepository.buscarConElAccesoVencido(
                List.of(com.app.idoneos.model.EstadoInscripcion.HABILITADA, com.app.idoneos.model.EstadoInscripcion.SUSPENDIDA), ahora)) {
            inscripcion.setEstadoInscripcion(vencida);
            inscripcion.setUltimaModificacion(ahora);
            inscripcionRepository.save(inscripcion);
            vencidas++;
        }
        return vencidas;
    }

    /** Actualiza cada minuto los estados que dependen de una fecha. */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 60_000, initialDelay = 90_000)
    @Transactional // las llamadas internas no pasan por el proxy: la transacción se abre acá
    public void actualizarEstadosPeriodicamente() {
        try {
            int canceladas = cancelarPendientesDeInscripcionCerrada();
            if (canceladas > 0) {
                REGISTRO.info("Inscripciones canceladas por falta de pago: {}", canceladas);
            }
            int vencidas = vencerAccesos();
            if (vencidas > 0) {
                REGISTRO.info("Inscripciones con el acceso vencido: {}", vencidas);
            }
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudieron actualizar los estados de las inscripciones", e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Inscripcion modificar(Inscripcion entidad) {
        return inscripcionRepository.save(entidad);
    }

    @Override
    @Transactional
    public Inscripcion modificar(Integer idInscripcion, String observaciones, Boolean habilitada) {
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra activa."));
        // Cancelada por falta de pago y Dada de baja son estados finales: no admiten ningún cambio
        if (inscripcion.esInmutable()) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("modificar", "la inscripción de",
                    inscripcion.getAlumno().getUsuario().getNombreCompleto(),
                    inscripcion.estaCancelada() ? "está cancelada por falta de pago" : "está dada de baja", ""));
        }
        String observacionesLimpias = observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
        if (observacionesLimpias != null && observacionesLimpias.length() > LONGITUD_OBSERVACIONES) {
            throw RespuestaFormularioUtil.armarCampoInvalido("observaciones", MensajeErrorUtil.armarDemasiadoLargo("Las observaciones", LONGITUD_OBSERVACIONES, "caracteres"));
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (habilitada != null) {
            if (habilitada && !inscripcion.fueHabilitada() && !inscripcion.estaSuspendida()) {
                // El acceso se habilita solo al aprobarse el pago (o al inscribirse en un curso sin costo)
                throw new IllegalArgumentException(
                        "Error! No se puede habilitar una inscripción pendiente de pago: se habilita al aprobarse el pago.");
            } else if (habilitada && inscripcion.estaSuspendida()) {
                // Reactivar: conservó su pago y su cupo. Vuelve a estar habilitada, o vencida si su fecha ya pasó
                inscripcion.setEstadoInscripcion(estadoInscripcion(inscripcion.vencimientoPasado(ahora)
                        ? com.app.idoneos.model.EstadoInscripcion.VENCIDA : com.app.idoneos.model.EstadoInscripcion.HABILITADA));
            } else if (!habilitada && inscripcion.tieneAcceso(ahora)) {
                // Suspender: solo se suspende lo que estaba habilitado
                inscripcion.setEstadoInscripcion(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.SUSPENDIDA));
            }
        }
        inscripcion.setObservaciones(observacionesLimpias);
        inscripcion.setUltimaModificacion(ahora);
        return inscripcionRepository.save(inscripcion);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(Inscripcion entidad) {
        entidad.marcarInactivo(); // Baja lógica
        inscripcionRepository.save(entidad);
    }

    @Override
    @Transactional
    public void darDeBaja(Integer idInscripcion, Alumno alumno, String motivo) {
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra activa."));
        if (alumno != null && inscripcion.getAlumno().getIdAlumno() != alumno.getIdAlumno()) {
            throw new IllegalArgumentException("Error! La inscripción no te pertenece.");
        }
        if (inscripcion.esInmutable()) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("la inscripción de",
                    inscripcion.getAlumno().getUsuario().getNombreCompleto(), "está cancelada por falta de pago, un estado final", ""));
        }
        String motivoLimpio = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (motivoLimpio != null && motivoLimpio.length() > LONGITUD_MOTIVO) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarDemasiadoLargo("El motivo", LONGITUD_MOTIVO, "caracteres"));
        }
        if (motivoLimpio != null) {
            String nota = "Motivo de la baja: " + motivoLimpio;
            String anterior = inscripcion.getObservaciones();
            inscripcion.setObservaciones(anterior != null && anterior.length() + 3 + nota.length() <= LONGITUD_OBSERVACIONES
                    ? anterior + " | " + nota : nota);
        }
        inscripcion.marcarInactivo();
        inscripcion.setEstadoInscripcion(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.DADA_DE_BAJA));
        inscripcion.setUltimaModificacion(LocalDateTime.now());
        // El certificado se anula y los intentos se dan de baja solos: lo hace la base de datos.
        inscripcionRepository.save(inscripcion);
    }

    @Override
    @Transactional
    public void darDeBajaVarias(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos una inscripción.");
        }
        for (Integer id : ids) {
            try {
                darDeBaja(id, null, null);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error! No se dio de baja ninguna inscripción. "
                        + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
    }

    /** Crea la inscripción con las reglas comunes: valida la cohorte, la ventana de inscripción y el cupo. */
    private Inscripcion crearInscripcion(Alumno solicitante, Integer idCohorte, boolean terminos, String errorTerminos) {
        if (idCohorte == null) {
            throw RespuestaFormularioUtil.armarCampoInvalido("cohorteId", "Error! Debe seleccionar la cohorte.");
        }
        // El alumno que llega del usuario con sesión está desconectado de Hibernate: se trabaja con el alumno de la base
        Alumno alumno = alumnoRepository.findById(solicitante.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! El alumno no se encuentra activo."));
        // Se bloquea la cohorte: el cupo se cuenta en la base mientras nadie más lo cambia
        Cohorte cohorte = cohorteRepository.buscarPorIdConBloqueo(idCohorte)
                .filter(c -> !c.esInactivo() && !c.getPrograma().esInactivo() && !c.getPrograma().getCurso().esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra disponible."));
        LocalDateTime ahora = LocalDateTime.now();
        if (!cohorte.estaAbierta()) {
            throw new IllegalArgumentException("Error! La inscripción a esta cohorte ya no está habilitada.");
        }
        if (cohorte.getCupoMaximo() != null && inscripcionRepository.contarQueOcupanCupo(cohorte) >= cohorte.getCupoMaximo()) {
            throw new IllegalArgumentException("Error! La cohorte alcanzó su cupo máximo.");
        }
        if (inscripcionRepository.existsByCohorteAndAlumnoAndBajaFalse(cohorte, alumno)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("alumnoId", "Error! El alumno ya tiene una inscripción vigente a esta cohorte.");
        }
        if (!terminos) {
            throw RespuestaFormularioUtil.armarCampoInvalido("terminos", errorTerminos);
        }

        // El vencimiento del acceso se calcula al habilitarse por primera vez: un curso gratuito queda habilitado ya.
        // Uno con costo, cuando se apruebe el pago (o se habilite sin costo, o el administrador la dé de alta)
        Inscripcion inscripcion = new Inscripcion(cohorte, alumno, null);
        inscripcion.setFechaCreacion(ahora);
        inscripcion.setEstadoInscripcion(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO));
        inscripcion.setEstadoAvance(estadoAvance(com.app.idoneos.model.EstadoAvance.EN_PROGRESO));
        if (cohorte.getPrograma().getCurso().getPrecio() <= 0) {
            inscripcion.habilitar(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.HABILITADA), ahora);
        }
        inscripcion = inscripcionRepository.save(inscripcion);
        cohorte.getInscripciones().add(inscripcion);
        alumno.getInscripciones().add(inscripcion);

        // Progreso inicial: la primer tema del cronograma, sin completar
        List<TemaCronograma> cronograma = cohorte.getPrograma().getCronogramaOrdenado();
        if (!cronograma.isEmpty()) {
            Progreso progreso = progresoRepository.save(new Progreso(cronograma.get(0).getTema(), inscripcion));
            inscripcion.getProgresos().add(progreso);
        }
        return inscripcion;
    }

    private String datosDelAlumno(Alumno alumno) {
        return TextoUtil.normalizar(alumno.getUsuario().getNombreCompleto() + " " + alumno.getUsuario().getDni()
                + " " + alumno.getUsuario().getCorreo());
    }

    private boolean tieneAccesoVigente(Inscripcion inscripcion, Alumno alumno) {
        try {
            validarAcceso(inscripcion.getIdInscripcion(), alumno);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
