package com.app.idoneos.servicio.Inscripcion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Progreso;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.AlumnoRepositorio;
import com.app.idoneos.repositorio.CohorteRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.ProgresoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Pago.PagoServicio;
import com.app.idoneos.utilidades.TextoUtilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Inscripcion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link InscripcionServicio}.
 */
@Service
public class InscripcionServicioImpl implements InscripcionServicio, CrudServicio<Inscripcion> {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Longitud máxima de las observaciones (la de la columna) y del motivo de la baja. */
    private static final int LONGITUD_OBSERVACIONES = 500;
    private static final int LONGITUD_MOTIVO = 300;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private CohorteRepositorio cohorteRepositorio;

    @Autowired
    private AlumnoRepositorio alumnoRepositorio;

    @Autowired
    private PagoServicio pagoServicio;

    @Autowired
    private ProgresoRepositorio progresoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la inscripción por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Inscripcion> buscarPorId(Integer id) {
        return inscripcionRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Inscripcion> obtenerTodo() {
        return inscripcionRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return inscripcionRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de inscripcion asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de Inscripcion asociados al Cohorte indicado
     */
    @Override
    public List<Inscripcion> buscarPorCohorte(Cohorte cohorte) {
        return inscripcionRepositorio.findByCohorteAndBajaFalse(cohorte);
    }

    /**
     * Busca los registros vigentes de inscripcion asociados a alumno.
     *
     * @param alumno el registro de Alumno asociado
     * @return una lista de Inscripcion asociados al Alumno indicado
     */
    @Override
    public List<Inscripcion> buscarPorAlumno(Alumno alumno) {
        return inscripcionRepositorio.findByAlumnoAndBajaFalse(alumno);
    }

    /**
     * Busca las inscripciones vigentes de un alumno, filtrando opcionalmente por el nombre del curso
     * y por el estado de la inscripción (En Progreso o Finalizado).
     *
     * @param alumno el alumno dueño de las inscripciones
     * @param nombreCurso parte del nombre del curso (opcional)
     * @param estado el estado de la inscripción (opcional)
     * @return una lista de inscripciones que cumplen los criterios, de la más reciente a la más antigua
     */
    @Override
    public List<Inscripcion> buscarMisCursos(Alumno alumno, String nombreCurso, String estado) {
        String criterio = nombreCurso == null ? "" : nombreCurso.trim().toLowerCase();
        return inscripcionRepositorio.findByAlumnoAndBajaFalse(alumno).stream()
                .filter(inscripcion -> criterio.isEmpty()
                        || inscripcion.getCurso().getNombre().toLowerCase().contains(criterio))
                .filter(inscripcion -> estado == null || estado.isBlank()
                        || inscripcion.getEstado().equalsIgnoreCase(estado.trim()))
                .sorted(Comparator.comparingInt(Inscripcion::getIdInscripcion).reversed())
                .toList();
    }

    /**
     * Verifica que el alumno pueda acceder al curso de la inscripción (CU-27).
     *
     * @param idInscripcion El identificador de la inscripción.
     * @param alumno        El alumno que solicita el acceso.
     * @return La inscripción a la que puede acceder.
     * @throws IllegalArgumentException Si el alumno no puede acceder, con el motivo.
     */
    @Override
    public Inscripcion validarAcceso(int idInscripcion, Alumno alumno) {
        Inscripcion inscripcion = buscarPorId(idInscripcion)
                .filter(i -> i.getAlumno().getIdAlumno() == alumno.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! No tenés una inscripción vigente a este curso."));
        Cohorte cohorte = inscripcion.getCohorte();
        if (Boolean.FALSE.equals(inscripcion.getHabilitado()) || cohorte.esInactivo()) {
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

    /**
     * Busca la inscripción con la que un alumno puede ver el contenido de una unidad.
     */
    @Override
    public Optional<Inscripcion> buscarConAccesoAUnidad(Alumno alumno, Unidad unidad) {
        return inscripcionRepositorio.findByAlumnoAndBajaFalse(alumno).stream()
                .filter(inscripcion -> inscripcion.getCurso().getIdCurso() == unidad.getCurso().getIdCurso())
                .filter(inscripcion -> tieneAccesoVigente(inscripcion, alumno))
                .filter(inscripcion -> inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream()
                        .filter(cronograma -> cronograma.getUnidad().getIdUnidad() == unidad.getIdUnidad())
                        .findFirst().map(inscripcion::estaUnidadHabilitada).orElse(false))
                .findFirst();
    }

    @Override
    public List<Inscripcion> buscarConFiltros(Integer cursoId, String alumno, String estadoAcceso) {
        String criterio = TextoUtilidad.normalizar(alumno);
        LocalDateTime ahora = LocalDateTime.now();
        return inscripcionRepositorio.findByBajaFalse().stream()
                .filter(i -> cursoId == null || i.getCurso().getIdCurso() == cursoId)
                .filter(i -> estadoAcceso == null || estadoAcceso.isBlank()
                        || i.getEstadoAcceso(ahora).equalsIgnoreCase(estadoAcceso.trim()))
                .filter(i -> criterio.isEmpty() || datosDelAlumno(i.getAlumno()).contains(criterio))
                .sorted(Comparator.comparing(Inscripcion::getFechaCreacion)
                        .thenComparingInt(Inscripcion::getIdInscripcion).reversed())
                .toList();
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la inscripción en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Inscripcion guardar(Inscripcion entidad) {
        return inscripcionRepositorio.save(entidad);
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
    public Inscripcion registrarInscripcion(Integer idAlumno, Integer idCohorte, boolean terminosConfirmados) {
        if (idAlumno == null) {
            throw new IllegalArgumentException("Error! Debe seleccionar el alumno.");
        }
        Alumno alumno = alumnoRepositorio.findById(idAlumno).filter(a -> !a.getUsuario().esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El alumno no se encuentra activo."));
        return crearInscripcion(alumno, idCohorte, terminosConfirmados,
                "Error! Debe confirmar que el alumno acepta los términos y condiciones de las políticas de privacidad.");
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Inscripcion modificar(Inscripcion entidad) {
        return inscripcionRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public Inscripcion modificarInscripcion(Integer idInscripcion, String observaciones, Boolean habilitada,
            LocalDate vencimiento) {
        Inscripcion inscripcion = inscripcionRepositorio.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra activa."));
        // Mientras la inscripción no se habilitó no tiene vencimiento: se calcula al habilitarla
        if (vencimiento == null && inscripcion.getFechaVencimientoAcceso() != null) {
            throw new IllegalArgumentException("Error! La fecha de vencimiento del acceso es obligatoria.");
        }
        String observacionesLimpias = observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
        if (observacionesLimpias != null && observacionesLimpias.length() > LONGITUD_OBSERVACIONES) {
            throw new IllegalArgumentException("Error! Las observaciones no pueden superar los "
                    + LONGITUD_OBSERVACIONES + " caracteres.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (vencimiento != null) {
            LocalDateTime minimo = inscripcion.getVencimientoMinimo();
            if (vencimiento.isBefore(minimo.toLocalDate())) {
                throw new IllegalArgumentException("Error! La fecha de vencimiento del acceso no puede ser anterior al "
                        + minimo.format(FORMATO_FECHA) + ", que resulta de la duración del cronograma del programa.");
            }
        }
        if (habilitada != null) {
            if (habilitada && !inscripcion.estaHabilitada()) {
                // Un curso con costo solo se habilita si lo pagado cubre su precio
                validarPagosCubrenElPrecio(inscripcion);
                // Primera habilitación: ocupa un cupo y se calcula el vencimiento del acceso
                if (!inscripcion.getCohorte().tieneCupo()) {
                    throw new IllegalArgumentException("Error! La cohorte alcanzó su cupo máximo.");
                }
                inscripcion.habilitar(ahora);
            } else {
                inscripcion.setHabilitado(habilitada);
            }
        }
        // Si no cambió el día, se conserva el momento exacto; si cambió, el acceso dura hasta el final de ese día
        if (vencimiento != null && (inscripcion.getFechaVencimientoAcceso() == null
                || !vencimiento.equals(inscripcion.getFechaVencimientoAcceso().toLocalDate()))) {
            inscripcion.setFechaVencimientoAcceso(vencimiento.atTime(23, 59, 59));
        }
        inscripcion.setObservaciones(observacionesLimpias);
        inscripcion.setUltimaModificacion(ahora);
        return inscripcionRepositorio.save(inscripcion);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Inscripcion entidad) {
        entidad.marcarInactivo(); // Baja lógica
        inscripcionRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public void darDeBajaInscripcion(Integer idInscripcion, Alumno alumno, String motivo) {
        Inscripcion inscripcion = inscripcionRepositorio.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra activa."));
        if (alumno != null && inscripcion.getAlumno().getIdAlumno() != alumno.getIdAlumno()) {
            throw new IllegalArgumentException("Error! La inscripción no te pertenece.");
        }
        String motivoLimpio = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (motivoLimpio != null && motivoLimpio.length() > LONGITUD_MOTIVO) {
            throw new IllegalArgumentException("Error! El motivo no puede superar los " + LONGITUD_MOTIVO + " caracteres.");
        }
        if (motivoLimpio != null) {
            String nota = "Motivo de la baja: " + motivoLimpio;
            String anterior = inscripcion.getObservaciones();
            inscripcion.setObservaciones(anterior != null && anterior.length() + 3 + nota.length() <= LONGITUD_OBSERVACIONES
                    ? anterior + " | " + nota : nota);
        }
        inscripcion.marcarInactivo();
        inscripcion.setUltimaModificacion(LocalDateTime.now());
        // El certificado y los intentos se dan de baja; los progresos quedan sin acceso con la inscripción y las
        // consultas de foro se muestran de forma anónima (ConsultaForo.getAutor)
        inscripcion.getCertificados().forEach(Certificado::anular);
        inscripcion.getIntentosAutoevaluacion().forEach(IntentoAutoevaluacion::marcarInactivo);
        inscripcionRepositorio.save(inscripcion);
    }

    /**
     * El administrador no puede habilitar a mano una inscripción de un curso con costo si no tiene pagos aprobados o
     * si lo aprobado no llega al precio del curso (con el descuento del pago, si tuvo).
     */
    private void validarPagosCubrenElPrecio(Inscripcion inscripcion) {
        if (!pagoServicio.cubreElPrecio(inscripcion)) {
            throw new IllegalArgumentException("Error! No se puede habilitar la inscripción: "
                    + (inscripcion.tienePagoAprobado() ? "los pagos aprobados no llegan al precio del curso"
                            : "no tiene ningún pago aprobado")
                    + ". Registrá o acreditá el pago primero.");
        }
    }

    /**
     * Reglas comunes de CU-45 y CU-46: valida la cohorte, la ventana de inscripción, el cupo, la inscripción
     * duplicada y los términos, y registra la inscripción con su progreso inicial.
     */
    private Inscripcion crearInscripcion(Alumno solicitante, Integer idCohorte, boolean terminos, String errorTerminos) {
        if (idCohorte == null) {
            throw new IllegalArgumentException("Error! Debe seleccionar la cohorte.");
        }
        // El alumno que llega del usuario con sesión está desconectado de Hibernate: se trabaja con el alumno de la base
        Alumno alumno = alumnoRepositorio.findById(solicitante.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! El alumno no se encuentra activo."));
        Cohorte cohorte = cohorteRepositorio.findById(idCohorte)
                .filter(c -> !c.esInactivo() && !c.getPrograma().esInactivo() && !c.getPrograma().getCurso().esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra disponible."));
        LocalDateTime ahora = LocalDateTime.now();
        if (!cohorte.estaAbierta()) {
            throw new IllegalArgumentException("Error! La inscripción a esta cohorte ya no está habilitada.");
        }
        if (cohorte.getCupoDisponible() != null && cohorte.getCupoDisponible() == 0) {
            throw new IllegalArgumentException("Error! La cohorte alcanzó su cupo máximo.");
        }
        if (inscripcionRepositorio.existsByCohorteAndAlumnoAndBajaFalse(cohorte, alumno)) {
            throw new IllegalArgumentException("Error! El alumno ya tiene una inscripción vigente a esta cohorte.");
        }
        if (!terminos) {
            throw new IllegalArgumentException(errorTerminos);
        }

        // El vencimiento del acceso se calcula al habilitarse por primera vez: un curso gratuito queda habilitado ya;
        // uno con costo, cuando se apruebe el pago (o se habilite sin costo, o el administrador la dé de alta)
        Inscripcion inscripcion = new Inscripcion(cohorte, alumno, null);
        inscripcion.setFechaCreacion(ahora);
        inscripcion.setHabilitado(false);
        if (cohorte.getPrograma().getCurso().getPrecio() <= 0) {
            inscripcion.habilitar(ahora);
        }
        inscripcion = inscripcionRepositorio.save(inscripcion);
        cohorte.getInscripciones().add(inscripcion);
        alumno.getInscripciones().add(inscripcion);

        // Progreso inicial: la primera unidad del cronograma, sin completar
        List<UnidadCronograma> cronograma = cohorte.getPrograma().getCronogramaOrdenado();
        if (!cronograma.isEmpty()) {
            Progreso progreso = progresoRepositorio.save(new Progreso(cronograma.get(0).getUnidad(), inscripcion));
            inscripcion.getProgresos().add(progreso);
        }
        return inscripcion;
    }

    private String datosDelAlumno(Alumno alumno) {
        return TextoUtilidad.normalizar(alumno.getUsuario().getNombreCompleto() + " " + alumno.getUsuario().getDni()
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
