package com.app.idoneos.service.pago;

import java.util.regex.Pattern;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Descuento;
import com.app.idoneos.model.EstadoPago;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.MetodoPago;
import com.app.idoneos.model.Pago;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.CohorteRepository;
import com.app.idoneos.repository.DescuentoRepository;
import com.app.idoneos.repository.EstadoPagoRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.MetodoPagoRepository;
import com.app.idoneos.repository.PagoRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.comprobante.ComprobanteService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.service.pasarelapago.PasarelaPagoService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import com.app.idoneos.util.TextoUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de PagoService. */
@Service
public class PagoServiceImpl implements PagoService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(PagoServiceImpl.class);

    /** Longitud máxima de las observaciones del pago manual. */
    /** Código de referencia de MODO: solo letras y números (sin guiones). */
    private static final java.util.regex.Pattern FORMATO_REFERENCIA_MODO = java.util.regex.Pattern.compile("[A-Za-z0-9]{6,12}");

    /** Número de comprobante de MODO: solo números, de 4 a 100 dígitos. */
    private static final java.util.regex.Pattern FORMATO_COMPROBANTE_MODO = java.util.regex.Pattern.compile("\\d{4,100}");

    /** Número de comprobante de una transferencia: depende del banco. Puede llevar letras, números y guiones. */
    private static final java.util.regex.Pattern FORMATO_COMPROBANTE_TRANSFERENCIA = java.util.regex.Pattern.compile("[A-Za-z0-9-]{4,100}");

    private static final int LONGITUD_OBSERVACIONES = 300;

    private static final DecimalFormat FORMATO_MONTO = new DecimalFormat("#,##0.##");

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private EstadoPagoRepository estadoPagoRepository;

    @Autowired
    private com.app.idoneos.repository.EstadoInscripcionRepository estadoInscripcionRepository;

    private com.app.idoneos.model.EstadoInscripcion estadoInscripcion(String nombre) {
        return estadoInscripcionRepository.findByNombre(nombre).orElseThrow();
    }

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Autowired
    private ComprobanteService comprobanteService;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private PasarelaPagoService pasarela;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el pago por su identificador. */
    @Override
    public Optional<Pago> buscarPorId(Integer id) {
        return pagoRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Pago> obtenerTodo() {
        return pagoRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return pagoRepository.existsById(id);
    }

    @Override
    public List<Pago> buscarConFiltros(Integer cursoId, String alumno, String estado, LocalDate desde, LocalDate hasta,
            Alumno soloDe) {
        String criterio = TextoUtil.normalizar(alumno);
        return pagoRepository.findAll().stream()
                .filter(p -> soloDe == null || p.getInscripcion().getAlumno().getIdAlumno() == soloDe.getIdAlumno())
                .filter(p -> cursoId == null || p.getInscripcion().getCurso().getIdCurso() == cursoId)
                .filter(p -> estado == null || estado.isBlank() || p.getEstadoPago().getNombre().equalsIgnoreCase(estado.trim()))
                .filter(p -> desde == null || !p.getFechaDelPago().toLocalDate().isBefore(desde))
                .filter(p -> hasta == null || !p.getFechaDelPago().toLocalDate().isAfter(hasta))
                .filter(p -> criterio.isEmpty() || TextoUtil.normalizar(p.getInscripcion().getAlumno().getUsuario()
                        .getNombreCompleto() + " " + p.getInscripcion().getAlumno().getUsuario().getDni() + " "
                        + p.getInscripcion().getAlumno().getUsuario().getCorreo()).contains(criterio))
                .sorted(Comparator.comparing(Pago::getFechaDelPago).thenComparingInt(Pago::getIdPago).reversed())
                .toList();
    }

    @Override
    public Optional<Descuento> calcularDescuento(Inscripcion inscripcion) {
        // Cursos comprados: inscripciones habilitadas con pago aprobado (sin contar la que se está pagando)
        long comprados = inscripcionRepository.findByAlumnoAndBajaFalse(inscripcion.getAlumno()).stream()
                .filter(otra -> otra.getIdInscripcion() != inscripcion.getIdInscripcion())
                .filter(otra -> otra.fueHabilitada() && otra.tienePagoAprobado()).count();
        return mejorDescuento(comprados);
    }

    @Override
    public Optional<Descuento> calcularDescuentoPara(Alumno alumno) {
        long comprados = alumno == null ? 0
                : inscripcionRepository.findByAlumnoAndBajaFalse(alumno).stream()
                        .filter(otra -> otra.fueHabilitada() && otra.tienePagoAprobado()).count();
        return mejorDescuento(comprados);
    }

    /** Busca el descuento vigente de mayor porcentaje entre los que exigen, como mucho, esa cantidad de cursos comprados. */
    private Optional<Descuento> mejorDescuento(long comprados) {
        LocalDateTime ahora = LocalDateTime.now();
        return descuentoRepository.findByBajaFalse().stream()
                .filter(d -> d.estaVigente(ahora) && d.getCursosRequeridos() <= comprados)
                .max(Comparator.comparing(Descuento::getPorcentaje)
                        .thenComparing(Comparator.comparingInt(Descuento::getIdDescuento).reversed()));
    }

    @Override
    public float calcularMontoAPagar(Inscripcion inscripcion) {
        float precio = inscripcion.getCurso().getPrecio();
        return calcularDescuento(inscripcion).map(d -> d.aplicarA(precio)).orElse(precio);
    }

    @Override
    public float calcularMontoRequerido(Inscripcion inscripcion) {
        float precio = inscripcion.getCurso().getPrecio();
        if (precio <= 0) {
            return 0;
        }
        // Un pago que se hizo con descuento fija lo que había que pagar (el descuento puede agotarse después)
        for (Pago pago : inscripcion.getPagos()) {
            if (pago.estaAprobado() && pago.getDescuento() != null) {
                return pago.getDescuento().aplicarA(precio);
            }
        }
        return calcularMontoAPagar(inscripcion);
    }

    @Override
    @Transactional
    public boolean habilitarSiNoHayQuePagar(Inscripcion inscripcion) {
        if (inscripcion.fueHabilitada() || inscripcion.getCurso().getPrecio() <= 0) {
            return false;
        }
        Descuento descuento = calcularDescuento(inscripcion).orElse(null);
        if (descuento == null || descuento.aplicarA(inscripcion.getCurso().getPrecio()) > 0) {
            return false;
        }
        habilitarSinPago(inscripcion, descuento);
        return true;
    }

    @Override
    public DetallePago calcularDetalle(Inscripcion inscripcion) {
        float precio = inscripcion.getCurso().getPrecio();
        // Un pago aprobado con descuento fija el descuento. Si no, el que le corresponde hoy
        Descuento descuento = inscripcion.getPagos().stream().filter(pago -> pago.estaAprobado() && pago.getDescuento() != null)
                .map(Pago::getDescuento).findFirst().orElseGet(() -> calcularDescuento(inscripcion).orElse(null));
        float conDescuento = descuento != null ? descuento.aplicarA(precio) : precio;
        return new DetallePago(precio, descuento, precio - conDescuento, inscripcion.getMontoPagado(),
                calcularMontoPendiente(inscripcion));
    }

    @Override
    public float calcularMontoPendiente(Inscripcion inscripcion) {
        return Math.max(0, calcularMontoRequerido(inscripcion) - inscripcion.getMontoPagado());
    }

    @Override
    public boolean cubreElPrecio(Inscripcion inscripcion) {
        if (inscripcion.getCurso().getPrecio() <= 0) {
            return true;
        }
        return inscripcion.tienePagoAprobado()
                && inscripcion.getMontoPagado() + 0.005f >= calcularMontoRequerido(inscripcion);
    }

    @Override
    @Transactional
    public Pago consultarEstado(Integer idPago, Alumno alumno) {
        Pago pago = buscarVisible(idPago, alumno);
        conciliar(pago);
        return pago;
    }

    @Override
    public Pago buscarVisible(Integer idPago, Alumno alumno) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("Error! El pago no se encuentra disponible."));
        if (alumno != null && pago.getInscripcion().getAlumno().getIdAlumno() != alumno.getIdAlumno()) {
            throw new IllegalArgumentException("Error! El pago no se encuentra disponible.");
        }
        return pago;
    }

    @Override
    public int plazoConfirmacionMinutos() {
        int minutos = parametroService.obtenerEntero(Parametro.PLAZO_CONFIRMACION_PAGO,
                Parametro.PLAZO_CONFIRMACION_PAGO_POR_DEFECTO);
        return Math.min(10, Math.max(5, minutos));
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el pago en la base de datos. */
    @Override
    public Pago guardar(Pago entidad) {
        return pagoRepository.save(entidad);
    }

    @Override
    @Transactional
    public SolicitudPago iniciar(Integer idInscripcion, Alumno alumno) {
        return iniciar(idInscripcion, alumno, null);
    }

    @Override
    @Transactional
    public SolicitudPago iniciar(Integer idInscripcion, Alumno alumno, Float montoEsperado) {
        bloquearCohorteDeLaInscripcion(idInscripcion);
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra activa."));
        if (alumno == null || inscripcion.getAlumno().getIdAlumno() != alumno.getIdAlumno()) {
            throw new IllegalArgumentException("Error! No tenés una inscripción pendiente de pago para este curso.");
        }
        return iniciar(inscripcion, montoEsperado);
    }

    @Override
    @Transactional
    public Pago registrarManual(Integer idInscripcion, Float monto, String numeroComprobante, String observaciones) {
        bloquearCohorteDeLaInscripcion(idInscripcion);
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra activa."));
        if (inscripcion.fueHabilitada() && inscripcion.tienePagoAprobado()) {
            throw new IllegalArgumentException("Error! El pago de esta inscripción ya está registrado.");
        }
        String comprobanteLimpio = numeroComprobante == null ? "" : numeroComprobante.trim();
        if (monto == null || comprobanteLimpio.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe completar el monto y el número de comprobante de la transferencia.");
        }
        validarMonto(monto);
        validarNumeroComprobante(comprobanteLimpio, MetodoPago.TRANSFERENCIA, null);
        validarMontoNoSuperaLaDeuda(inscripcion, monto);
        validarNoCancelada(inscripcion);
        // Cerrada la inscripción no se registran pagos, tampoco el que completa un pago parcial: esa inscripción se cancela por falta de pago
        if (inscripcion.getCohorte().inscripcionCerrada()) {
            throw new IllegalArgumentException("Error! La inscripción a esta cohorte ya cerró: no se puede registrar un pago.");
        }
        validarCupo(inscripcion);
        String notas = observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
        if (notas != null && notas.length() > LONGITUD_OBSERVACIONES) {
            throw new IllegalArgumentException(
                    MensajeErrorUtil.armarDemasiadoLargo("Las observaciones", LONGITUD_OBSERVACIONES, "caracteres"));
        }
        // Un pendiente (por ejemplo, de MODO sin confirmar) queda rechazado.
        rechazarPendientes(inscripcion, null);
        Pago pago = new Pago(estado(EstadoPago.PENDIENTE), metodo(MetodoPago.TRANSFERENCIA), inscripcion,
                "IDN-" + UUID.randomUUID(), monto);
        pago.setNumeroComprobante(comprobanteLimpio);
        pago.setDetalleMetodoPago(notas != null ? "Transferencia al CBU de la institución · " + notas
                : "Transferencia al CBU de la institución");
        pago = pagoRepository.save(pago);
        inscripcion.getPagos().add(pago);
        Usuario usuario = inscripcion.getAlumno().getUsuario();
        aprobar(pago, usuario.getNombreCompleto(), usuario.getDni());
        return pago;
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Pago modificar(Pago entidad) {
        return pagoRepository.save(entidad);
    }

    @Override
    @Transactional
    public SolicitudPago reintentar(Integer idPago, Alumno alumno) {
        Pago pago = buscarVisible(idPago, alumno);
        if (!pago.estaRechazado() || pago.getInscripcion().fueHabilitada() || pago.getInscripcion().esInactivo()) {
            throw new IllegalArgumentException(
                    "Error! El pago no se encuentra rechazado o su inscripción ya tiene el acceso habilitado.");
        }
        return iniciar(pago.getInscripcion(), null);
    }

    @Override
    @Transactional
    public boolean procesarNotificacion(String idSolicitud, String idIntencionExterna) {
        Optional<Pago> pago = Optional.empty();
        if (idSolicitud != null && !idSolicitud.isBlank()) {
            pago = pagoRepository.findByIdSolicitudPago(idSolicitud);
        }
        if (pago.isEmpty() && idIntencionExterna != null && !idIntencionExterna.isBlank()) {
            pago = pagoRepository.findByIdIntencionExterna(idIntencionExterna);
        }
        pago.ifPresent(this::conciliar);
        return pago.isPresent();
    }

    @Override
    @Transactional
    public int conciliarPendientes() {
        int resueltos = 0;
        for (Pago pago : pagoRepository.findByEstadoPago(estado(EstadoPago.PENDIENTE))) {
            conciliar(pago);
            if (!pago.estaPendiente()) {
                resueltos++;
            }
        }
        return resueltos;
    }

    /** Concilia cada minuto los pagos online pendientes, porque el aviso de MODO por webhook puede no llegar. */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    @Transactional // la llamada a conciliarPendientes() es interna (no pasa por el proxy): la transacción tiene que abrirse acá
    public void conciliarPagosPendientesPeriodicamente() {
        try {
            int resueltos = conciliarPendientes();
            if (resueltos > 0) {
                REGISTRO.info("Pagos conciliados: {}", resueltos);
            }
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudieron conciliar los pagos pendientes", e);
        }
    }

    @Override
    @Transactional
    public Pago modificar(Integer idPago, String estado, Float monto, String referencia, String numeroComprobante,
            String nombrePagador, String dniPagador, String motivo) {
        // Un pago aprobado o rechazado ya no se modifica: solo se resuelve el que está pendiente
        Pago pago = pagoRepository.findById(idPago).filter(Pago::estaPendiente)
                .orElseThrow(() -> new IllegalArgumentException("Error! Solo se puede modificar un pago pendiente: el aprobado y el rechazado no se modifican."));
        String nuevoEstado = estado == null ? "" : estado.trim();
        boolean aprobar = EstadoPago.APROBADO.equalsIgnoreCase(nuevoEstado);
        boolean rechazar = EstadoPago.RECHAZADO.equalsIgnoreCase(nuevoEstado);
        if (!aprobar && !rechazar) {
            throw RespuestaFormularioUtil.armarCampoInvalido("estado", "Error! Debe marcar el pago como aprobado o rechazado.");
        }
        if (rechazar) {
            // Rechazar solo pide el motivo: no hay cobro ni comprobante que registrar
            String motivoLimpio = motivo == null ? "" : motivo.trim();
            if (motivoLimpio.isEmpty()) {
                throw RespuestaFormularioUtil.armarCampoInvalido("motivo", "Error! Debe completar el motivo del rechazo.");
            }
            rechazar(pago, motivoLimpio);
            return pago;
        }
        String referenciaLimpia = referencia == null || referencia.isBlank() ? null : referencia.trim().toUpperCase();
        String comprobanteLimpio = numeroComprobante == null || numeroComprobante.isBlank() ? null : numeroComprobante.trim();
        if (monto == null) {
            throw RespuestaFormularioUtil.armarCampoInvalido("monto", "Error! El monto es obligatorio.");
        }
        // Acreditar una transferencia pide su número de comprobante.
        if (pago.esDeModo()) {
            if (referenciaLimpia == null || comprobanteLimpio == null) {
                throw new IllegalArgumentException(
                        "Error! Debe completar el código de referencia y el número de comprobante de MODO para acreditar el pago.");
            }
            referenciaLimpia = validarCodigoReferencia(referenciaLimpia, pago);
        } else {
            if (comprobanteLimpio == null) {
                throw RespuestaFormularioUtil.armarCampoInvalido("numeroComprobante", "Error! Debe completar el número de comprobante de la transferencia.");
            }
            referenciaLimpia = null; // la referencia es solo de MODO
        }
        validarMonto(monto);
        validarNumeroComprobante(comprobanteLimpio, pago.getMetodoPago().getNombre(), pago);
        validarMontoNoSuperaLaDeuda(pago.getInscripcion(), monto);
        validarNoCancelada(pago.getInscripcion());
        String nombre = nombrePagador == null ? "" : nombrePagador.trim();
        String dni = dniPagador == null ? "" : dniPagador.trim();
        if (pago.esDeModo() && (nombre.isEmpty() || !dni.matches("\\d{7,8}"))) {
            throw new IllegalArgumentException(
                    MensajeErrorUtil.armarFormatoInvalido("El nombre o el DNI del pagador", "nombre completo y DNI de 7 u 8 números"));
        }
        pago.setMonto(monto);
        if (referenciaLimpia != null) {
            pago.setCodigoReferencia(referenciaLimpia);
        }
        pago.setNumeroComprobante(comprobanteLimpio);
        Usuario usuario = pago.getInscripcion().getAlumno().getUsuario();
        // El medio de pago ya antepone el nombre del método: acá solo se agrega el detalle
        String detalleAnterior = pago.getDetalleMetodoPago();
        pago.setDetalleMetodoPago((detalleAnterior == null || detalleAnterior.isBlank() ? "" : detalleAnterior + " · ")
                + "Acreditado manualmente por el administrador");
        rechazarPendientes(pago.getInscripcion(), pago);
        aprobar(pago, nombre.isEmpty() ? usuario.getNombreCompleto() : nombre, dni.isEmpty() ? usuario.getDni() : dni);
        return pago;
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Pago entidad) {
        pagoRepository.delete(entidad);
    }

    /** La habilitación de la inscripción ocupa un cupo de la cohorte: si ya no queda, no se puede pagar. */
    /** Una inscripción cancelada no se paga: ya no hay un lugar reservado para ella. */
    private static void validarNoCancelada(Inscripcion inscripcion) {
        if (inscripcion.estaCancelada()) {
            throw new IllegalArgumentException("Error! La inscripción fue cancelada: no se puede pagar.");
        }
    }

    /** Toma el bloqueo de la cohorte de la inscripción y recarga la inscripción de la base. */
    private void bloquearCohorteDeLaInscripcion(Integer idInscripcion) {
        inscripcionRepository.buscarIdCohorte(idInscripcion).ifPresent(cohorteRepository::buscarPorIdConBloqueo);
        inscripcionRepository.findById(idInscripcion).ifPresent(this::recargar);
    }

    /** Vuelca los cambios pendientes y vuelve a leer la entidad de la base. */
    private void recargar(Object entidad) {
        entityManager.flush();
        entityManager.refresh(entidad);
    }

    private void validarCupo(Inscripcion inscripcion) {
        var cohorte = inscripcion.getCohorte();
        if (!inscripcion.ocupaCupo() && cohorte.getCupoMaximo() != null
                && inscripcionRepository.contarQueOcupanCupo(cohorte) >= cohorte.getCupoMaximo()) {
            throw new IllegalArgumentException(
                    "Error! La cohorte alcanzó su cupo máximo: no se puede pagar esta inscripción.");
        }
    }

    /** Inicia el pago con las reglas comunes: arma el pago de la inscripción y crea la solicitud en la plataforma. */
    private SolicitudPago iniciar(Inscripcion inscripcion, Float montoEsperado) {
        validarNoCancelada(inscripcion);
        if (inscripcion.tienePagoAprobado()) {
            throw new IllegalArgumentException("Error! " + (inscripcion.fueHabilitada()
                    ? "El pago de esta inscripción ya está aprobado."
                    : "La inscripción ya tiene un pago aprobado que no cubre el precio: el resto lo acredita el administrador."));
        }
        if (inscripcion.fueHabilitada()) {
            throw new IllegalArgumentException("Error! La inscripción no tiene un pago pendiente.");
        }
        validarCupo(inscripcion);
        LocalDateTime ahora = LocalDateTime.now();
        Pago existente = inscripcion.getPagoSinAprobar();
        if (existente != null && existente.estaPendiente()) {
            conciliar(existente);
            if (existente.estaAprobado()) {
                return new SolicitudPago(existente.getIdPago(), existente.getIdSolicitudPago(), true);
            }
            if (existente.estaPendiente()) {
                // Hay un pago en curso. Vencida su solicitud, se está verificando con MODO: no se abre otra mientras ese pago pueda cobrarse
                if (estaVerificando(existente)) {
                    throw estaVerificandoElPago();
                }
                // Dentro del plazo se retoma su solicitud (el mismo QR) en lugar de crear otra
                PasarelaPagoService.Resultado enCurso = pasarela.consultarPago(existente.getIdSolicitudPago());
                if (enCurso.estado() == PasarelaPagoService.Estado.PROCESANDO) {
                    throw estaVerificandoElPago();
                }
                if (enCurso.estado() == PasarelaPagoService.Estado.PENDIENTE && enCurso.solicitud() != null) {
                    validarMontoEsperado(montoEsperado, existente.getMonto());
                    return new SolicitudPago(existente.getIdPago(), existente.getIdSolicitudPago(), false);
                }
            }
        }
        // Un pago ya iniciado se puede continuar (arriba) aunque la inscripción haya cerrado, pero desde el cierre no se inicia uno nuevo
        if (inscripcion.getCohorte().inscripcionCerrada()) {
            throw new IllegalArgumentException("Error! La inscripción a esta cohorte ya cerró: no se puede iniciar un pago nuevo.");
        }

        Descuento descuento = calcularDescuento(inscripcion).orElse(null);
        float precio = inscripcion.getCurso().getPrecio();
        float monto = descuento != null ? descuento.aplicarA(precio) : precio;
        validarMontoEsperado(montoEsperado, monto);
        if (monto <= 0) {
            return habilitarSinPago(inscripcion, descuento);
        }
        if (monto < pasarela.montoMinimo()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("monto", "Error! El monto a pagar ($" + FORMATO_MONTO.format(monto)
                    + ") es menor al mínimo que admite el medio de pago ($" + FORMATO_MONTO.format(pasarela.montoMinimo())
                    + ").");
        }

        // Cada intento es un pago nuevo: los rechazados quedan con su solicitud y su motivo, para poder rastrear cualquier aviso tardío de MODO
        String idExterno = "IDN-" + UUID.randomUUID();
        Pago pago = new Pago(estado(EstadoPago.PENDIENTE), metodo(MetodoPago.MODO), inscripcion, idExterno, monto);
        pago.setDescuento(descuento);
        pago.setUltimaModificacion(ahora);
        pago = pagoRepository.save(pago);
        inscripcion.getPagos().add(pago);

        Usuario usuario = inscripcion.getAlumno().getUsuario();
        PasarelaPagoService.Solicitud solicitud = pasarela.crearSolicitud(idExterno, monto,
                "Curso: " + inscripcion.getCurso().getNombre(), usuario.getNombreCompleto(), usuario.getCorreo(),
                usuario.getDni());
        pago.setIdSolicitudPago(solicitud.id());
        pagoRepository.save(pago);
        // Con el pago en curso la inscripción pasa a "Pago en curso" y reserva su lugar hasta que se resuelva
        inscripcion.setEstadoInscripcion(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.PAGO_EN_CURSO));
        inscripcion.setUltimaModificacion(ahora);
        inscripcionRepository.save(inscripcion);
        return new SolicitudPago(pago.getIdPago(), solicitud.id(), false);
    }

    /** Arma el mensaje de un pago que MODO todavía no resolvió. */
    private static IllegalArgumentException estaVerificandoElPago() {
        return new IllegalArgumentException(
                "Error! Estamos verificando tu pago con MODO. Aguardá unos minutos y revisá el resultado en Mis pagos.");
    }

    @Override
    public boolean estaVerificando(Pago pago) {
        return pago.estaPendiente() && pago.esDeModo()
                && pago.getFechaCreacion().plusSeconds(pasarela.vigenciaSolicitudSegundos()).isBefore(LocalDateTime.now());
    }

    /** Habilita el acceso sin pago cuando el descuento cubre todo el precio y no hay nada que cobrar. */
    private SolicitudPago habilitarSinPago(Inscripcion inscripcion, Descuento descuento) {
        LocalDateTime ahora = LocalDateTime.now();
        inscripcion.habilitar(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.HABILITADA), ahora);
        inscripcion.setUltimaModificacion(ahora);
        inscripcionRepository.save(inscripcion);
        if (descuento != null) {
            descuento.setCantidadUsada(descuento.getCantidadUsada() + 1);
            descuentoRepository.save(descuento);
        }
        return new SolicitudPago(null, null, true);
    }

    /** Resuelve un pago online pendiente consultando a MODO. */
    private void conciliar(Pago pago) {
        if (!pago.estaPendiente() || !pago.esDeModo() || pago.getIdSolicitudPago() == null) {
            return;
        }
        PasarelaPagoService.Resultado resultado;
        try {
            resultado = pasarela.consultarPago(pago.getIdSolicitudPago());
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudo consultar el pago {} en la plataforma: {}", pago.getIdPago(), e.getMessage());
            return; // sin respuesta no se decide nada
        }
        if (resultado.estado() != PasarelaPagoService.Estado.PROCESANDO) {
            // Se va a resolver el pago: se toma el bloqueo de la cohorte y se vuelve a mirar el pago, por si otro proceso (el aviso
            // de MODO, la tarea periódica o el administrador) ya lo resolvió mientras se consultaba
            bloquearCohorteDeLaInscripcion(pago.getInscripcion().getIdInscripcion());
            recargar(pago);
            if (!pago.estaPendiente()) {
                return;
            }
        }
        switch (resultado.estado()) {
            case APROBADO -> {
                aplicarDatosDeLaPlataforma(pago, resultado);
                Usuario usuario = pago.getInscripcion().getAlumno().getUsuario();
                aprobar(pago, resultado.nombrePagador() != null ? resultado.nombrePagador() : usuario.getNombreCompleto(),
                        resultado.dniPagador() != null ? resultado.dniPagador() : usuario.getDni());
            }
            case RECHAZADO -> {
                aplicarDatosDeLaPlataforma(pago, resultado);
                rechazar(pago, resultado.motivoError() != null ? resultado.motivoError() : "El pago fue rechazado.");
            }
            case PROCESANDO -> {
                // El pago está en vuelo: se espera a que MODO lo resuelva
            }
            case PENDIENTE -> {
                LocalDateTime vencimiento = resultado.vencimiento() != null ? resultado.vencimiento()
                        : pago.getFechaCreacion().plusSeconds(pasarela.vigenciaSolicitudSegundos());
                if (LocalDateTime.now().isAfter(vencimiento.plusMinutes(MARGEN_MINUTOS))) {
                    rechazar(pago, "El pago no se realizó dentro del plazo de " + plazoConfirmacionMinutos() + " minutos.");
                }
            }
        }
    }

    /** Minutos que se esperan después del vencimiento de la solicitud antes de darla por no pagada (el estado de MODO se actualiza de forma asíncrona). */
    private static final int MARGEN_MINUTOS = 2;

    private void aplicarDatosDeLaPlataforma(Pago pago, PasarelaPagoService.Resultado resultado) {
        pago.setDetalleMetodoPago(resultado.medioPago());
        pago.setUltimosDigitosTarjeta(resultado.ultimosDigitos() == null ? null
                : resultado.ultimosDigitos().substring(Math.max(0, resultado.ultimosDigitos().length() - 4)));
        if (resultado.referencia() != null && pagoRepository.findByCodigoReferencia(resultado.referencia())
                .filter(otro -> otro.getIdPago() != pago.getIdPago()).isEmpty()) {
            pago.setCodigoReferencia(resultado.referencia().length() > 20 ? resultado.referencia().substring(0, 20)
                    : resultado.referencia());
        }
    }

    /** Aprueba el pago: registra la fecha, habilita el acceso de la inscripción si lo pagado cubre el precio. */
    private void aprobar(Pago pago, String nombrePagador, String dniPagador) {
        LocalDateTime ahora = LocalDateTime.now();
        pago.setEstadoPago(estado(EstadoPago.APROBADO));
        pago.setFechaAprobacion(ahora);
        pago.setUltimaModificacion(ahora);
        pago.setMotivoRechazo(null);
        Inscripcion inscripcion = pago.getInscripcion();
        // Solo se habilita cuando lo pagado cubre el precio del curso: un pago menor queda aprobado, pero la
        // inscripción sigue pendiente. Primera habilitación: ocupa un cupo y se calcula el vencimiento del acceso.
        if (cubreElPrecio(inscripcion)) {
            inscripcion.habilitar(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.HABILITADA), ahora);
        } else if (inscripcion.estaConPagoEnCurso()) {
            inscripcion.setEstadoInscripcion(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO));
        }
        inscripcion.setUltimaModificacion(ahora);
        inscripcionRepository.save(inscripcion);
        Descuento descuento = pago.getDescuento();
        if (descuento != null) {
            // El uso del descuento se cuenta al aprobarse el pago (un pago rechazado no consume cupo)
            descuento.setCantidadUsada(descuento.getCantidadUsada() + 1);
            descuentoRepository.save(descuento);
        }
        pagoRepository.save(pago);
        comprobanteService.emitir(pago, nombrePagador, dniPagador);
    }

    /** Motivo con el que se rechaza un pago pendiente cuando se aprueba otro de la misma inscripción. */
    static final String MOTIVO_OTRO_PAGO_APROBADO = "Se aprobó otro pago de esta inscripción.";

    /** Rechaza los pagos pendientes de la inscripción (menos excepto). */
    private void rechazarPendientes(Inscripcion inscripcion, Pago excepto) {
        for (Pago otro : new ArrayList<>(inscripcion.getPagos())) {
            if (otro != excepto && otro.estaPendiente()) {
                rechazar(otro, MOTIVO_OTRO_PAGO_APROBADO);
            }
        }
    }

    private void rechazar(Pago pago, String motivo) {
        pago.setEstadoPago(estado(EstadoPago.RECHAZADO));
        pago.setMotivoRechazo(motivo);
        pago.setUltimaModificacion(LocalDateTime.now());
        pagoRepository.save(pago);
        // Sin otro pago en curso, la inscripción vuelve a "Pendiente de pago" y libera el lugar que reservaba
        Inscripcion inscripcion = pago.getInscripcion();
        if (inscripcion.estaConPagoEnCurso() && inscripcion.getPagos().stream().noneMatch(Pago::estaPendiente)) {
            inscripcion.setEstadoInscripcion(estadoInscripcion(com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO));
            inscripcionRepository.save(inscripcion);
        }
    }

    private void validarMonto(Float monto) {
        if (monto.isNaN() || monto <= 0) {
            throw RespuestaFormularioUtil.armarCampoInvalido("monto", "Error! El monto debe ser mayor a cero.");
        }
    }

    /** Valida el código de referencia de MODO: solo letras y números, de 6 a 12 caracteres. */
    private String validarCodigoReferencia(String codigo, Pago pago) {
        if (codigo == null) {
            return null;
        }
        if (!FORMATO_REFERENCIA_MODO.matcher(codigo).matches()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("referencia", MensajeErrorUtil.armarFormatoInvalido("El código de referencia",
                    "letras y números, sin guiones, de 6 a 12 caracteres"));
        }
        if (pagoRepository.findByCodigoReferencia(codigo).filter(otro -> pago == null || otro.getIdPago() != pago.getIdPago())
                .isPresent()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("referencia", "Error! Ya existe un pago registrado con ese código de referencia.");
        }
        return codigo;
    }

    /** Valida el número de comprobante según el medio de pago. */
    private void validarNumeroComprobante(String numero, String medio, Pago pago) {
        if (numero == null) {
            return;
        }
        if (MetodoPago.MODO.equals(medio)) {
            if (!FORMATO_COMPROBANTE_MODO.matcher(numero).matches()) {
                throw new IllegalArgumentException(
                        MensajeErrorUtil.armarFormatoInvalido("El número de comprobante", "solo números, de 4 a 100 dígitos"));
            }
        } else if (!FORMATO_COMPROBANTE_TRANSFERENCIA.matcher(numero).matches()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("numeroComprobante", MensajeErrorUtil.armarFormatoInvalido("El número de comprobante",
                    "letras, números y guiones, de 4 a 100 caracteres"));
        }
        if (pagoRepository.findFirstByNumeroComprobanteAndMetodoPago(numero, metodo(medio))
                .filter(otro -> pago == null || otro.getIdPago() != pago.getIdPago()).isPresent()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("numeroComprobante", "Error! Ya existe un pago registrado con ese número de comprobante.");
        }
    }

    /** Valida el monto esperado. Si el alumno vio un monto en la caja y ahora corresponde cobrar otro, no se cobra y se le avisa. */
    private void validarMontoEsperado(Float montoEsperado, float monto) {
        if (montoEsperado != null && Math.abs(monto - montoEsperado) > 0.005f) {
            throw new MontoCambiadoException(
                    "El monto a pagar cambió: el descuento que se aplicaba ya no está disponible. Revisá la caja antes de pagar.");
        }
    }

    /** Valida que el monto que se acredita no supere lo que todavía debe la inscripción. */
    private void validarMontoNoSuperaLaDeuda(Inscripcion inscripcion, float monto) {
        float debe = calcularMontoPendiente(inscripcion);
        if (monto > debe + 0.005f) {
            throw RespuestaFormularioUtil.armarCampoInvalido("monto", "Error! El monto no puede superar lo que debe la inscripción ($"
                    + FORMATO_MONTO.format(debe) + ").");
        }
    }

    private EstadoPago estado(String nombre) {
        return estadoPagoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el estado de pago " + nombre));
    }

    private MetodoPago metodo(String nombre) {
        return metodoPagoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el método de pago " + nombre));
    }
}
