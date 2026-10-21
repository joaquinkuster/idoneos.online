package com.app.idoneos.servicio.Pago;

import com.app.idoneos.utilidades.MensajeValidacionUtilidad;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.modelo.EstadoPago;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.MetodoPago;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.DescuentoRepositorio;
import com.app.idoneos.repositorio.EstadoPagoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.MetodoPagoRepositorio;
import com.app.idoneos.repositorio.PagoRepositorio;
import com.app.idoneos.servicio.Comprobante.ComprobanteServicio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.servicio.PasarelaPago.PasarelaPagoServicio;
import com.app.idoneos.utilidades.TextoUtilidad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Pago}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link PagoServicio}.
 */
@Service
public class PagoServicioImpl implements PagoServicio, CrudServicio<Pago> {

    private static final Logger REGISTRO = LoggerFactory.getLogger(PagoServicioImpl.class);

    /** Longitud máxima de las observaciones del pago manual. */
    /** Código de referencia de MODO: solo letras y números (sin guiones). */
    private static final java.util.regex.Pattern FORMATO_REFERENCIA_MODO = java.util.regex.Pattern.compile("[A-Za-z0-9]{6,12}");

    /** Número de comprobante de MODO: solo números, de 4 a 100 dígitos. */
    private static final java.util.regex.Pattern FORMATO_COMPROBANTE_MODO = java.util.regex.Pattern.compile("\\d{4,100}");

    /** Número de comprobante de una transferencia: depende del banco; puede llevar letras, números y guiones. */
    private static final java.util.regex.Pattern FORMATO_COMPROBANTE_TRANSFERENCIA = java.util.regex.Pattern.compile("[A-Za-z0-9-]{4,100}");

    private static final int LONGITUD_OBSERVACIONES = 300;

    private static final DecimalFormat FORMATO_MONTO = new DecimalFormat("#,##0.##");

    @Autowired
    private PagoRepositorio pagoRepositorio;

    @Autowired
    private EstadoPagoRepositorio estadoPagoRepositorio;

    @Autowired
    private MetodoPagoRepositorio metodoPagoRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private DescuentoRepositorio descuentoRepositorio;

    @Autowired
    private ComprobanteServicio comprobanteServicio;

    @Autowired
    private ParametroServicio parametroServicio;

    @Autowired
    private PasarelaPagoServicio pasarela;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el pago por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Pago> buscarPorId(Integer id) {
        return pagoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Pago> obtenerTodo() {
        return pagoRepositorio.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return pagoRepositorio.existsById(id);
    }

    /**
     * Busca los registros de pago asociados a estadoPago.
     *
     * @param estadoPago el registro de EstadoPago asociado
     * @return una lista de Pago asociados al EstadoPago indicado
     */
    @Override
    public List<Pago> buscarPorEstadoPago(EstadoPago estadoPago) {
        return pagoRepositorio.findByEstadoPago(estadoPago);
    }

    /**
     * Busca los registros de pago asociados a metodoPago.
     *
     * @param metodoPago el registro de MetodoPago asociado
     * @return una lista de Pago asociados al MetodoPago indicado
     */
    @Override
    public List<Pago> buscarPorMetodoPago(MetodoPago metodoPago) {
        return pagoRepositorio.findByMetodoPago(metodoPago);
    }

    /**
     * Busca los registros de pago asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Pago asociados al Inscripcion indicado
     */
    @Override
    public List<Pago> buscarPorInscripcion(Inscripcion inscripcion) {
        return pagoRepositorio.findByInscripcion(inscripcion);
    }

    /**
     * Busca los registros de pago asociados a descuento.
     *
     * @param descuento el registro de Descuento asociado
     * @return una lista de Pago asociados al Descuento indicado
     */
    @Override
    public List<Pago> buscarPorDescuento(Descuento descuento) {
        return pagoRepositorio.findByDescuento(descuento);
    }

    /**
     * Busca el pago por su atributo único 'codigoReferencia'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param codigoReferencia el valor de 'codigoReferencia' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Pago> buscarPorCodigoReferencia(String codigoReferencia) {
        return pagoRepositorio.findByCodigoReferencia(codigoReferencia);
    }

    /**
     * Busca el pago por su atributo único 'idIntencionExterna'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param idIntencionExterna el valor de 'idIntencionExterna' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Pago> buscarPorIdIntencionExterna(String idIntencionExterna) {
        return pagoRepositorio.findByIdIntencionExterna(idIntencionExterna);
    }

    /**
     * Busca el pago por su atributo único 'idSolicitudPago'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param idSolicitudPago el valor de 'idSolicitudPago' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Pago> buscarPorIdSolicitudPago(String idSolicitudPago) {
        return pagoRepositorio.findByIdSolicitudPago(idSolicitudPago);
    }

    @Override
    public List<Pago> buscarConFiltros(Integer cursoId, String alumno, String estado, LocalDate desde, LocalDate hasta,
            Alumno soloDe) {
        String criterio = TextoUtilidad.normalizar(alumno);
        return pagoRepositorio.findAll().stream()
                .filter(p -> soloDe == null || p.getInscripcion().getAlumno().getIdAlumno() == soloDe.getIdAlumno())
                .filter(p -> cursoId == null || p.getInscripcion().getCurso().getIdCurso() == cursoId)
                .filter(p -> estado == null || estado.isBlank() || p.getEstadoPago().getNombre().equalsIgnoreCase(estado.trim()))
                .filter(p -> desde == null || !p.getFechaDelPago().toLocalDate().isBefore(desde))
                .filter(p -> hasta == null || !p.getFechaDelPago().toLocalDate().isAfter(hasta))
                .filter(p -> criterio.isEmpty() || TextoUtilidad.normalizar(p.getInscripcion().getAlumno().getUsuario()
                        .getNombreCompleto() + " " + p.getInscripcion().getAlumno().getUsuario().getDni() + " "
                        + p.getInscripcion().getAlumno().getUsuario().getCorreo()).contains(criterio))
                .sorted(Comparator.comparing(Pago::getFechaDelPago).thenComparingInt(Pago::getIdPago).reversed())
                .toList();
    }

    @Override
    public Optional<Descuento> calcularDescuento(Inscripcion inscripcion) {
        // Cursos comprados: inscripciones habilitadas con pago aprobado (sin contar la que se está pagando)
        long comprados = inscripcionRepositorio.findByAlumnoAndBajaFalse(inscripcion.getAlumno()).stream()
                .filter(otra -> otra.getIdInscripcion() != inscripcion.getIdInscripcion())
                .filter(otra -> otra.estaHabilitada() && otra.tienePagoAprobado()).count();
        return mejorDescuento(comprados);
    }

    @Override
    public Optional<Descuento> calcularDescuentoPara(Alumno alumno) {
        long comprados = alumno == null ? 0
                : inscripcionRepositorio.findByAlumnoAndBajaFalse(alumno).stream()
                        .filter(otra -> otra.estaHabilitada() && otra.tienePagoAprobado()).count();
        return mejorDescuento(comprados);
    }

    /** El descuento vigente de mayor porcentaje entre los que exigen, como mucho, esa cantidad de cursos comprados. */
    private Optional<Descuento> mejorDescuento(long comprados) {
        LocalDateTime ahora = LocalDateTime.now();
        return descuentoRepositorio.findByBajaFalse().stream()
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
        if (inscripcion.estaHabilitada() || inscripcion.getCurso().getPrecio() <= 0) {
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
        // Un pago aprobado con descuento fija el descuento; si no, el que le corresponde hoy
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
        Pago pago = pagoRepositorio.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("Error! El pago no se encuentra disponible."));
        if (alumno != null && pago.getInscripcion().getAlumno().getIdAlumno() != alumno.getIdAlumno()) {
            throw new IllegalArgumentException("Error! El pago no se encuentra disponible.");
        }
        return pago;
    }

    @Override
    public int plazoConfirmacionMinutos() {
        int minutos = parametroServicio.obtenerEntero(Parametro.PLAZO_CONFIRMACION_PAGO,
                Parametro.PLAZO_CONFIRMACION_PAGO_POR_DEFECTO);
        return Math.min(10, Math.max(5, minutos));
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el pago en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Pago guardar(Pago entidad) {
        return pagoRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public SolicitudPago iniciarPago(Integer idInscripcion, Alumno alumno) {
        return iniciarPago(idInscripcion, alumno, null);
    }

    @Override
    @Transactional
    public SolicitudPago iniciarPago(Integer idInscripcion, Alumno alumno, Float montoEsperado) {
        Inscripcion inscripcion = inscripcionRepositorio.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra activa."));
        if (alumno == null || inscripcion.getAlumno().getIdAlumno() != alumno.getIdAlumno()) {
            throw new IllegalArgumentException("Error! No tenés una inscripción pendiente de pago para este curso.");
        }
        return iniciar(inscripcion, montoEsperado);
    }

    @Override
    @Transactional
    public Pago registrarPagoManual(Integer idInscripcion, Float monto, String numeroComprobante, String observaciones) {
        Inscripcion inscripcion = inscripcionRepositorio.findById(idInscripcion).filter(i -> !i.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La inscripción no se encuentra activa."));
        if (inscripcion.estaHabilitada() && inscripcion.tienePagoAprobado()) {
            throw new IllegalArgumentException("Error! El pago de esta inscripción ya está registrado.");
        }
        String comprobanteLimpio = numeroComprobante == null ? "" : numeroComprobante.trim();
        if (monto == null || comprobanteLimpio.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe completar el monto y el número de comprobante de la transferencia.");
        }
        validarMonto(monto);
        validarNumeroComprobante(comprobanteLimpio, MetodoPago.TRANSFERENCIA, null);
        validarMontoNoSuperaLaDeuda(inscripcion, monto);
        validarCupo(inscripcion);
        String notas = observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
        if (notas != null && notas.length() > LONGITUD_OBSERVACIONES) {
            throw new IllegalArgumentException(
                    MensajeValidacionUtilidad.demasiadoLargo("Las observaciones", LONGITUD_OBSERVACIONES, "caracteres"));
        }
        // Si había un pago sin aprobar (por ejemplo, uno de MODO que no se confirmó), se reutiliza su registro
        Pago pago = inscripcion.getPagoSinAprobar();
        if (pago == null) {
            pago = new Pago(estado(EstadoPago.PENDIENTE), metodo(MetodoPago.TRANSFERENCIA), inscripcion,
                    "IDN-" + UUID.randomUUID(), monto);
        }
        pago.setMetodoPago(metodo(MetodoPago.TRANSFERENCIA));
        pago.setIdIntencionExterna("IDN-" + UUID.randomUUID());
        pago.setMonto(monto);
        pago.setDescuento(null);
        limpiarResultado(pago);
        pago.setNumeroComprobante(comprobanteLimpio);
        pago.setDetalleMetodoPago(notas != null ? "Transferencia al CBU de la institución · " + notas
                : "Transferencia al CBU de la institución");
        pago = pagoRepositorio.save(pago);
        inscripcion.getPagos().add(pago);
        Usuario usuario = inscripcion.getAlumno().getUsuario();
        aprobar(pago, usuario.getNombreCompleto(), usuario.getDni());
        return pago;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Pago modificar(Pago entidad) {
        return pagoRepositorio.save(entidad);
    }

    @Override
    @Transactional
    public SolicitudPago reintentarPago(Integer idPago, Alumno alumno) {
        Pago pago = buscarVisible(idPago, alumno);
        if (!pago.estaRechazado() || pago.getInscripcion().estaHabilitada() || pago.getInscripcion().esInactivo()) {
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
            pago = pagoRepositorio.findByIdSolicitudPago(idSolicitud);
        }
        if (pago.isEmpty() && idIntencionExterna != null && !idIntencionExterna.isBlank()) {
            pago = pagoRepositorio.findByIdIntencionExterna(idIntencionExterna);
        }
        pago.ifPresent(this::conciliar);
        return pago.isPresent();
    }

    @Override
    @Transactional
    public int conciliarPagosPendientes() {
        int resueltos = 0;
        for (Pago pago : pagoRepositorio.findByEstadoPago(estado(EstadoPago.PENDIENTE))) {
            conciliar(pago);
            if (!pago.estaPendiente()) {
                resueltos++;
            }
        }
        return resueltos;
    }

    /** Cada minuto concilia los pagos online pendientes (el aviso de MODO por webhook puede no llegar). */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void conciliarPagosPendientesPeriodicamente() {
        try {
            int resueltos = conciliarPagosPendientes();
            if (resueltos > 0) {
                REGISTRO.info("Pagos conciliados: {}", resueltos);
            }
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudieron conciliar los pagos pendientes", e);
        }
    }

    @Override
    @Transactional
    public Pago modificarPago(Integer idPago, String estado, Float monto, String referencia, String numeroComprobante,
            String nombrePagador, String dniPagador, String motivo) {
        Pago pago = pagoRepositorio.findById(idPago).filter(p -> !p.estaAprobado())
                .orElseThrow(() -> new IllegalArgumentException("Error! El pago no se encuentra pendiente o rechazado."));
        String nuevoEstado = estado == null ? "" : estado.trim();
        boolean aprobar = EstadoPago.APROBADO.equalsIgnoreCase(nuevoEstado);
        boolean rechazar = EstadoPago.RECHAZADO.equalsIgnoreCase(nuevoEstado);
        if (!aprobar && !rechazar) {
            throw new IllegalArgumentException("Error! Debe marcar el pago como aprobado o rechazado.");
        }
        String referenciaLimpia = referencia == null || referencia.isBlank() ? null : referencia.trim().toUpperCase();
        String comprobanteLimpio = numeroComprobante == null || numeroComprobante.isBlank() ? null : numeroComprobante.trim();
        if (monto == null) {
            throw new IllegalArgumentException("Error! El monto es obligatorio.");
        }
        // Una transferencia siempre lleva su número de comprobante. Un pago de MODO, al acreditarlo, lleva el código de
        // referencia y el número de comprobante: son la prueba de que el administrador verificó el cobro en MODO. Al
        // rechazarlo no hay cobro que probar, así que son opcionales (si se completan, se validan igual).
        if (pago.esDeModo()) {
            if (aprobar && (referenciaLimpia == null || comprobanteLimpio == null)) {
                throw new IllegalArgumentException(
                        "Error! Debe completar el código de referencia y el número de comprobante de MODO para acreditar el pago.");
            }
            referenciaLimpia = validarCodigoReferencia(referenciaLimpia, pago);
        } else {
            if (comprobanteLimpio == null) {
                throw new IllegalArgumentException("Error! Debe completar el número de comprobante de la transferencia.");
            }
            referenciaLimpia = null; // la referencia es solo de MODO
        }
        validarMonto(monto);
        validarNumeroComprobante(comprobanteLimpio, pago.getMetodoPago().getNombre(), pago);
        if (aprobar) {
            validarMontoNoSuperaLaDeuda(pago.getInscripcion(), monto);
        }
        String nombre = nombrePagador == null ? "" : nombrePagador.trim();
        String dni = dniPagador == null ? "" : dniPagador.trim();
        if (aprobar && pago.esDeModo() && (nombre.isEmpty() || !dni.matches("\\d{7,8}"))) {
            throw new IllegalArgumentException(
                    MensajeValidacionUtilidad.formatoInvalido("El nombre o el DNI del pagador", "nombre completo y DNI de 7 u 8 números"));
        }
        pago.setMonto(monto);
        if (referenciaLimpia != null) {
            pago.setCodigoReferencia(referenciaLimpia);
        }
        if (comprobanteLimpio != null) {
            pago.setNumeroComprobante(comprobanteLimpio);
        }
        if (rechazar) {
            rechazar(pago, motivo == null || motivo.isBlank() ? "Rechazado por el administrador." : motivo.trim());
            return pago;
        }
        Usuario usuario = pago.getInscripcion().getAlumno().getUsuario();
        pago.setDetalleMetodoPago((pago.getDetalleMetodoPago() == null ? pago.getMetodoPago().getNombre()
                : pago.getDetalleMetodoPago()) + " · Acreditado manualmente por el administrador");
        aprobar(pago, nombre.isEmpty() ? usuario.getNombreCompleto() : nombre, dni.isEmpty() ? usuario.getDni() : dni);
        return pago;
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Pago entidad) {
        pagoRepositorio.delete(entidad);
    }

    /**
     * La habilitación de la inscripción ocupa un cupo de la cohorte: si ya no queda, no se puede pagar. (Si el pago
     * online ya se cobró cuando se aprueba, la inscripción se habilita igual: no se puede rechazar un pago acreditado.)
     */
    private void validarCupo(Inscripcion inscripcion) {
        if (!inscripcion.getCohorte().tieneCupo()) {
            throw new IllegalArgumentException(
                    "Error! La cohorte alcanzó su cupo máximo: no se puede pagar esta inscripción.");
        }
    }

    /** Reglas comunes de CU-51 y CU-52: arma el pago de la inscripción y crea la solicitud en la plataforma. */
    private SolicitudPago iniciar(Inscripcion inscripcion, Float montoEsperado) {
        if (inscripcion.tienePagoAprobado()) {
            throw new IllegalArgumentException("Error! " + (inscripcion.estaHabilitada()
                    ? "El pago de esta inscripción ya está aprobado."
                    : "La inscripción ya tiene un pago aprobado que no cubre el precio: el resto lo acredita el administrador."));
        }
        if (inscripcion.estaHabilitada()) {
            throw new IllegalArgumentException("Error! La inscripción no tiene un pago pendiente.");
        }
        validarCupo(inscripcion);
        LocalDateTime ahora = LocalDateTime.now();
        Pago existente = inscripcion.getPagoSinAprobar();
        if (existente != null && existente.estaPendiente()) {
            conciliar(existente);
            if (existente.estaAprobado()) {
                return new SolicitudPago(existente.getIdPago(), existente.getIdSolicitudPago(), null, null,
                        pasarela.scriptModal(), true);
            }
            if (existente.estaPendiente()) {
                // Hay un pago en curso: se retoma su solicitud (MODO devuelve su QR) en lugar de crear otra
                PasarelaPagoServicio.Resultado enCurso = pasarela.consultarPago(existente.getIdSolicitudPago());
                if (enCurso.estado() == PasarelaPagoServicio.Estado.PENDIENTE && enCurso.solicitud() != null) {
                    validarMontoEsperado(montoEsperado, existente.getMonto());
                    return new SolicitudPago(existente.getIdPago(), existente.getIdSolicitudPago(),
                            enCurso.solicitud().qr(), enCurso.solicitud().deeplink(), pasarela.scriptModal(), false);
                }
            }
        }
        // Un pago rechazado se reutiliza (CU-52); si no hay ninguno, se registra uno nuevo
        Pago pago = existente != null && existente.estaRechazado() ? existente : null;

        Descuento descuento = calcularDescuento(inscripcion).orElse(null);
        float precio = inscripcion.getCurso().getPrecio();
        float monto = descuento != null ? descuento.aplicarA(precio) : precio;
        validarMontoEsperado(montoEsperado, monto);
        if (monto <= 0) {
            return habilitarSinPago(inscripcion, descuento);
        }
        if (monto < pasarela.montoMinimo()) {
            throw new IllegalArgumentException("Error! El monto a pagar ($" + FORMATO_MONTO.format(monto)
                    + ") es menor al mínimo que admite el medio de pago ($" + FORMATO_MONTO.format(pasarela.montoMinimo())
                    + ").");
        }

        String idExterno = "IDN-" + UUID.randomUUID();
        if (pago == null) {
            pago = new Pago(estado(EstadoPago.PENDIENTE), metodo(MetodoPago.MODO), inscripcion, idExterno, monto);
        }
        pago.setEstadoPago(estado(EstadoPago.PENDIENTE));
        pago.setMetodoPago(metodo(MetodoPago.MODO));
        pago.setIdIntencionExterna(idExterno);
        pago.setMonto(monto);
        pago.setDescuento(descuento);
        limpiarResultado(pago);
        pago.setUltimaModificacion(ahora);
        pago = pagoRepositorio.save(pago);
        inscripcion.getPagos().add(pago);

        Usuario usuario = inscripcion.getAlumno().getUsuario();
        PasarelaPagoServicio.Solicitud solicitud = pasarela.crearSolicitud(idExterno, monto,
                "Curso: " + inscripcion.getCurso().getNombre(), usuario.getNombreCompleto(), usuario.getCorreo(),
                usuario.getDni());
        pago.setIdSolicitudPago(solicitud.id());
        pagoRepositorio.save(pago);
        return new SolicitudPago(pago.getIdPago(), solicitud.id(), solicitud.qr(), solicitud.deeplink(),
                pasarela.scriptModal(), false);
    }

    /**
     * El descuento cubre todo el precio: no hay nada que cobrar, así que se habilita el acceso directamente, sin
     * registrar un pago ni emitir comprobante. Como no hay pago al que asociar el descuento, su uso se cuenta acá
     * (si no, un descuento del 100% no tendría tope). Esta inscripción tampoco cuenta como curso comprado, porque
     * no tiene pago aprobado.
     */
    private SolicitudPago habilitarSinPago(Inscripcion inscripcion, Descuento descuento) {
        LocalDateTime ahora = LocalDateTime.now();
        inscripcion.habilitar(ahora);
        inscripcion.setUltimaModificacion(ahora);
        inscripcionRepositorio.save(inscripcion);
        if (descuento != null) {
            descuento.setCantidadUsada(descuento.getCantidadUsada() + 1);
            descuentoRepositorio.save(descuento);
        }
        return new SolicitudPago(null, null, null, null, pasarela.scriptModal(), true);
    }

    /**
     * Resuelve un pago online pendiente: consulta a la plataforma y lo aprueba o rechaza según informe; si sigue
     * pendiente y venció el plazo de confirmación, lo rechaza.
     */
    private void conciliar(Pago pago) {
        if (!pago.estaPendiente() || !pago.esDeModo() || pago.getIdSolicitudPago() == null) {
            return;
        }
        PasarelaPagoServicio.Resultado resultado = null;
        try {
            resultado = pasarela.consultarPago(pago.getIdSolicitudPago());
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudo consultar el pago {} en la plataforma: {}", pago.getIdPago(), e.getMessage());
        }
        if (resultado != null && resultado.estado() == PasarelaPagoServicio.Estado.APROBADO) {
            aplicarDatosDeLaPlataforma(pago, resultado);
            Usuario usuario = pago.getInscripcion().getAlumno().getUsuario();
            aprobar(pago, resultado.nombrePagador() != null ? resultado.nombrePagador() : usuario.getNombreCompleto(),
                    resultado.dniPagador() != null ? resultado.dniPagador() : usuario.getDni());
            return;
        }
        if (resultado != null && resultado.estado() == PasarelaPagoServicio.Estado.RECHAZADO) {
            aplicarDatosDeLaPlataforma(pago, resultado);
            rechazar(pago, resultado.motivoError() != null ? resultado.motivoError() : "El pago fue rechazado.");
            return;
        }
        LocalDateTime inicio = pago.getUltimaModificacion() != null ? pago.getUltimaModificacion() : pago.getFechaCreacion();
        if (LocalDateTime.now().isAfter(inicio.plusMinutes(plazoConfirmacionMinutos()))) {
            rechazar(pago, "El pago no se confirmó dentro del plazo de " + plazoConfirmacionMinutos() + " minutos.");
        }
    }

    private void aplicarDatosDeLaPlataforma(Pago pago, PasarelaPagoServicio.Resultado resultado) {
        pago.setDetalleMetodoPago(resultado.medioPago());
        pago.setUltimosDigitosTarjeta(resultado.ultimosDigitos() == null ? null
                : resultado.ultimosDigitos().substring(Math.max(0, resultado.ultimosDigitos().length() - 4)));
        if (resultado.referencia() != null && pagoRepositorio.findByCodigoReferencia(resultado.referencia())
                .filter(otro -> otro.getIdPago() != pago.getIdPago()).isEmpty()) {
            pago.setCodigoReferencia(resultado.referencia().length() > 20 ? resultado.referencia().substring(0, 20)
                    : resultado.referencia());
        }
    }

    /** Aprueba el pago: registra la fecha, habilita el acceso de la inscripción si lo pagado cubre el precio, cuenta el uso del descuento y emite el comprobante. */
    private void aprobar(Pago pago, String nombrePagador, String dniPagador) {
        LocalDateTime ahora = LocalDateTime.now();
        pago.setEstadoPago(estado(EstadoPago.APROBADO));
        pago.setFechaAprobacion(ahora);
        pago.setUltimaModificacion(ahora);
        pago.setDetalleErrorOperacion(null);
        Inscripcion inscripcion = pago.getInscripcion();
        // Solo se habilita cuando lo pagado cubre el precio del curso: un pago menor queda aprobado, pero la
        // inscripción sigue pendiente. Primera habilitación: ocupa un cupo y se calcula el vencimiento del acceso.
        if (cubreElPrecio(inscripcion)) {
            inscripcion.habilitar(ahora);
        }
        inscripcion.setUltimaModificacion(ahora);
        inscripcionRepositorio.save(inscripcion);
        Descuento descuento = pago.getDescuento();
        if (descuento != null) {
            // El uso del descuento se cuenta al aprobarse el pago (un pago rechazado no consume cupo)
            descuento.setCantidadUsada(descuento.getCantidadUsada() + 1);
            descuentoRepositorio.save(descuento);
        }
        pagoRepositorio.save(pago);
        comprobanteServicio.emitirComprobante(pago, nombrePagador, dniPagador);
    }

    private void rechazar(Pago pago, String motivo) {
        pago.setEstadoPago(estado(EstadoPago.RECHAZADO));
        pago.setDetalleErrorOperacion(motivo);
        pago.setUltimaModificacion(LocalDateTime.now());
        pagoRepositorio.save(pago);
    }

    /** Deja el pago sin los datos del intento anterior, para reutilizar su registro. */
    private void limpiarResultado(Pago pago) {
        pago.setIdSolicitudPago(null);
        pago.setCodigoReferencia(null);
        pago.setNumeroComprobante(null);
        pago.setUltimosDigitosTarjeta(null);
        pago.setDetalleMetodoPago(null);
        pago.setDetalleErrorOperacion(null);
        pago.setFechaAprobacion(null);
    }

    private void validarMonto(Float monto) {
        if (monto.isNaN() || monto <= 0) {
            throw new IllegalArgumentException("Error! El monto debe ser mayor a cero.");
        }
    }

    /**
     * Valida el código de referencia de MODO: solo letras y números, de 6 a 12 caracteres, y que no esté en otro pago.
     *
     * @return el código validado, o {@code null} si no se indicó
     */
    private String validarCodigoReferencia(String codigo, Pago pago) {
        if (codigo == null) {
            return null;
        }
        if (!FORMATO_REFERENCIA_MODO.matcher(codigo).matches()) {
            throw new IllegalArgumentException(MensajeValidacionUtilidad.formatoInvalido("El código de referencia",
                    "letras y números, sin guiones, de 6 a 12 caracteres"));
        }
        if (pagoRepositorio.findByCodigoReferencia(codigo).filter(otro -> pago == null || otro.getIdPago() != pago.getIdPago())
                .isPresent()) {
            throw new IllegalArgumentException("Error! Ya existe un pago registrado con ese código de referencia.");
        }
        return codigo;
    }

    /**
     * Valida el número de comprobante según el medio de pago (MODO: solo números; transferencia: letras, números y
     * guiones) y que no esté repetido entre los pagos del mismo medio.
     */
    private void validarNumeroComprobante(String numero, String medio, Pago pago) {
        if (numero == null) {
            return;
        }
        if (MetodoPago.MODO.equals(medio)) {
            if (!FORMATO_COMPROBANTE_MODO.matcher(numero).matches()) {
                throw new IllegalArgumentException(
                        MensajeValidacionUtilidad.formatoInvalido("El número de comprobante", "solo números, de 4 a 100 dígitos"));
            }
        } else if (!FORMATO_COMPROBANTE_TRANSFERENCIA.matcher(numero).matches()) {
            throw new IllegalArgumentException(MensajeValidacionUtilidad.formatoInvalido("El número de comprobante",
                    "letras, números y guiones, de 4 a 100 caracteres"));
        }
        if (pagoRepositorio.findFirstByNumeroComprobanteAndMetodoPago(numero, metodo(medio))
                .filter(otro -> pago == null || otro.getIdPago() != pago.getIdPago()).isPresent()) {
            throw new IllegalArgumentException("Error! Ya existe un pago registrado con ese número de comprobante.");
        }
    }

    /** Si el alumno vio un monto en la caja y ahora corresponde cobrar otro, no se cobra y se le avisa. */
    private void validarMontoEsperado(Float montoEsperado, float monto) {
        if (montoEsperado != null && Math.abs(monto - montoEsperado) > 0.005f) {
            throw new MontoCambiadoException(
                    "El monto a pagar cambió: el descuento que se aplicaba ya no está disponible. Revisá la caja antes de pagar.");
        }
    }

    /** El monto que se acredita no puede superar lo que todavía debe la inscripción. */
    private void validarMontoNoSuperaLaDeuda(Inscripcion inscripcion, float monto) {
        float debe = calcularMontoPendiente(inscripcion);
        if (monto > debe + 0.005f) {
            throw new IllegalArgumentException("Error! El monto no puede superar lo que debe la inscripción ($"
                    + FORMATO_MONTO.format(debe) + ").");
        }
    }

    private EstadoPago estado(String nombre) {
        return estadoPagoRepositorio.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el estado de pago " + nombre));
    }

    private MetodoPago metodo(String nombre) {
        return metodoPagoRepositorio.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el método de pago " + nombre));
    }
}
