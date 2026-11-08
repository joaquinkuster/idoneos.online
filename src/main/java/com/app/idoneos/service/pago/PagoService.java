package com.app.idoneos.service.pago;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Descuento;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Pago;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las operaciones relacionadas con el pago.
 */
public interface PagoService extends CrudService<Pago> {

    /**
     * Busca un pago por su identificador.
     *
     * @param id el identificador
     * @return el pago, o vacío si no existe
     */
    Optional<Pago> buscarPorId(Integer id);

    // ---------------------------------------------------------------- Lecturas








    /**
     * Busca pagos por curso, alumno, estado y rango de fechas (CU-50). El alumno ve solo los suyos.
     *
     * @param cursoId el curso (opcional)
     * @param alumno parte del nombre, el DNI o el correo del alumno (opcional)
     * @param estado Pendiente, Aprobado o Rechazado (opcional)
     * @param desde la fecha desde la que se buscan los pagos (opcional)
     * @param hasta la fecha hasta la que se buscan los pagos (opcional)
     * @param soloDe el alumno dueño de los pagos, o {@code null} para ver los de todos (Administrador)
     * @return los pagos que cumplen los criterios, del más reciente al más antiguo
     */
    List<Pago> buscarConFiltros(Integer cursoId, String alumno, String estado, LocalDate desde, LocalDate hasta,
            Alumno soloDe);

    /**
     * Busca el descuento que le corresponde a una inscripción: entre los vigentes cuya condición cumple el alumno
     * (cursos comprados = inscripciones habilitadas con pago aprobado), el de mayor porcentaje.
     *
     * @param inscripcion la inscripción que se va a pagar
     * @return el descuento, o vacío si no corresponde ninguno
     */
    Optional<Descuento> calcularDescuento(Inscripcion inscripcion);

    /**
     * Calcula el descuento que le corresponde hoy a un alumno (el de mayor porcentaje entre los vigentes cuya condición de
     * cursos comprados cumple). Sin alumno (un visitante), solo cuentan los descuentos sin condición.
     *
     * @param alumno el alumno, o {@code null} si quien mira no es un alumno
     * @return el descuento aplicable, si hay alguno
     */
    Optional<Descuento> calcularDescuentoPara(Alumno alumno);

    /**
     * Detalle de lo que hay que pagar por una inscripción, para mostrarlo al administrador al registrar un pago.
     *
     * @param precio         El precio del curso.
     * @param descuento      El descuento aplicado, o {@code null} si no hay.
     * @param montoDescuento Cuánto se descuenta del precio (cero si no hay descuento).
     * @param pagado         Lo ya pagado (pagos aprobados).
     * @param total          Lo que falta pagar: el precio con el descuento menos lo ya pagado.
     */
    record DetallePago(float precio, Descuento descuento, float montoDescuento, float pagado, float total) {
    }

    /**
     * Arma el detalle de lo que hay que pagar por una inscripción: precio, descuento, lo ya pagado y lo que falta.
     *
     * @param inscripcion la inscripción
     * @return el detalle del pago
     */
    DetallePago calcularDetalle(Inscripcion inscripcion);

    /**
     * Habilita la inscripción sin pago si el descuento que le corresponde al alumno cubre todo el precio del curso (el
     * total a pagar es cero). Es lo que ocurre cuando el administrador registra la inscripción. Cuenta el uso del
     * descuento. Si hay algo que pagar, no hace nada.
     *
     * @param inscripcion la inscripción recién registrada
     * @return {@code true} si quedó habilitada sin pago
     */
    boolean habilitarSiNoHayQuePagar(Inscripcion inscripcion);

    /**
     * Calcula el monto que debe pagar el alumno por una inscripción: el precio del curso con el descuento aplicado.
     *
     * @param inscripcion la inscripción que se va a pagar
     * @return el monto a pagar
     */
    float calcularMontoAPagar(Inscripcion inscripcion);

    /**
     * Calcula el monto que deben cubrir los pagos aprobados para habilitar la inscripción: el del descuento del pago
     * aprobado, si lo tuvo, o el que le corresponde pagar hoy al alumno.
     *
     * @param inscripcion la inscripción
     * @return el monto que debe cubrirse (cero si el curso no tiene costo)
     */
    float calcularMontoRequerido(Inscripcion inscripcion);

    /**
     * Calcula lo que todavía debe la inscripción: el monto que deben cubrir los pagos aprobados menos lo ya pagado.
     *
     * @param inscripcion la inscripción
     * @return el monto pendiente (cero si ya está cubierto)
     */
    float calcularMontoPendiente(Inscripcion inscripcion);

    /**
     * Indica si los pagos aprobados de la inscripción cubren lo que hay que pagar. Un curso sin costo no necesita pagos.
     *
     * @param inscripcion la inscripción
     * @return {@code true} si se puede habilitar el acceso
     */
    boolean cubreElPrecio(Inscripcion inscripcion);

    /**
     * Consulta el estado de un pago. Si está pendiente, lo concilia con la plataforma: lo aprueba o rechaza según
     * informe, o lo rechaza si venció el plazo de confirmación.
     *
     * @param idPago el pago
     * @param alumno el alumno dueño del pago, o {@code null} si consulta el Administrador
     * @return el pago actualizado
     * @throws IllegalArgumentException si el pago no existe o no es del alumno
     */
    Pago consultarEstado(Integer idPago, Alumno alumno);

    /**
     * Indica si un pago online está siendo verificado con MODO: sigue pendiente pero ya venció la solicitud, así que no se puede pagar de nuevo hasta
     * que MODO informe cómo terminó. Mientras tanto la inscripción conserva su lugar.
     *
     * @param pago El pago.
     * @return {@code true} si está pendiente y su solicitud ya venció.
     */
    boolean estaVerificando(Pago pago);

    /**
     * Busca un pago al que puede acceder quien lo pide: el alumno, solo los suyos; el administrador, cualquiera.
     *
     * @param idPago el pago
     * @param alumno el alumno, o {@code null} si es el Administrador
     * @return el pago
     * @throws IllegalArgumentException si no existe o no es del alumno
     */
    Pago buscarVisible(Integer idPago, Alumno alumno);

    /**
     * Plazo, en minutos, que se espera la confirmación de un pago online antes de rechazarlo (entre 5 y 10).
     *
     * @return el plazo
     */
    int plazoConfirmacionMinutos();

    // ---------------------------------------------------------------- Creaciones

    /**
     * Inicia el pago online de una inscripción pendiente de pago (CU-51): calcula el monto con el descuento, registra
     * el pago como pendiente y crea la solicitud en la plataforma. Si ya hay un pago en curso lo retoma; si el último
     * fue rechazado reutiliza su registro (CU-52). Si el descuento cubre todo el precio, habilita la inscripción
     * directamente, sin registrar un pago ni pasar por la plataforma (la respuesta viene aprobada y sin pago).
     *
     * @param idInscripcion la inscripción que se paga
     * @param alumno el alumno dueño de la inscripción
     * @return los datos para que el alumno complete el pago
     * @throws IllegalArgumentException si no se puede pagar la inscripción
     */
    SolicitudPago iniciar(Integer idInscripcion, Alumno alumno);

    /**
     * Igual que {@link #iniciar(Integer, Alumno)}, pero comprueba que el monto a cobrar sea el que el alumno vio en la
     * caja: si el descuento aplicado dejó de estar disponible (o apareció otro) mientras tanto, no cobra y avisa.
     *
     * @param idInscripcion El identificador de la inscripción.
     * @param alumno        El alumno que paga.
     * @param montoEsperado El monto que el alumno vio en la caja (nulo para no comprobarlo).
     * @return La solicitud de pago.
     * @throws MontoCambiadoException Si el monto a cobrar es distinto del que el alumno vio.
     */
    SolicitudPago iniciar(Integer idInscripcion, Alumno alumno, Float montoEsperado);

    /**
     * El monto a cobrar cambió entre el momento en que el alumno lo vio en la caja y el momento de pagar.
     */
    class MontoCambiadoException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        public MontoCambiadoException(String mensaje) {
            super(mensaje);
        }
    }

    /**
     * El administrador registra el pago de una inscripción recibido por fuera de MODO, por transferencia bancaria
     * (CU-53). El pago queda aprobado, se habilita el acceso y se emite el comprobante.
     *
     * @param idInscripcion la inscripción que se paga
     * @param monto el monto recibido
     * @param numeroComprobante el número de comprobante de la transferencia
     * @param observaciones notas del administrador (opcional)
     * @return el pago registrado
     * @throws IllegalArgumentException si falta algún dato o la inscripción ya está paga
     */
    Pago registrarManual(Integer idInscripcion, Float monto, String numeroComprobante, String observaciones);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Reintenta un pago rechazado reutilizando su registro (CU-52).
     *
     * @param idPago el pago rechazado
     * @param alumno el alumno dueño del pago
     * @return los datos para que el alumno complete el pago
     * @throws IllegalArgumentException si el pago no está rechazado o su inscripción ya tiene acceso habilitado
     */
    SolicitudPago reintentar(Integer idPago, Alumno alumno);

    /**
     * Procesa el aviso de la plataforma de que cambió el estado de un pago: lo concilia consultándolo (el aviso solo
     * dice cuál es el pago, el resultado se toma de la plataforma).
     *
     * @param idSolicitud el identificador de la solicitud en la plataforma (opcional)
     * @param idIntencionExterna el identificador externo de la intención de pago (opcional)
     * @return {@code true} si se encontró el pago
     */
    boolean procesarNotificacion(String idSolicitud, String idIntencionExterna);

    /**
     * Concilia todos los pagos online pendientes: resuelve los que la plataforma ya aprobó o rechazó y rechaza los que
     * vencieron sin confirmación.
     *
     * @return la cantidad de pagos que dejaron de estar pendientes
     */
    int conciliarPendientes();

    /**
     * El administrador acredita o rechaza manualmente un pago pendiente (el aprobado y el rechazado no se modifican), con su monto o
     * comprobante (CU-54). Al aprobarlo se habilita el acceso y se emite el comprobante.
     *
     * @param idPago el pago
     * @param estado el nuevo estado: Aprobado o Rechazado
     * @param monto el monto (obligatorio)
     * @param referencia el código de referencia de MODO (obligatorio al acreditar un pago de MODO)
     * @param numeroComprobante el número de comprobante (obligatorio en una transferencia y al acreditar un pago de MODO)
     * @param nombrePagador el nombre del pagador (obligatorio al aprobar un pago de MODO)
     * @param dniPagador el DNI del pagador (obligatorio al aprobar un pago de MODO)
     * @param motivo el motivo del rechazo (obligatorio al rechazar)
     * @return el pago actualizado
     * @throws IllegalArgumentException si el pago no está pendiente (el aprobado y el rechazado no se modifican) o falta algún dato
     */
    Pago modificar(Integer idPago, String estado, Float monto, String referencia, String numeroComprobante,
            String nombrePagador, String dniPagador, String motivo);

    /**
     * Datos que necesita el navegador para que el alumno complete un pago online: el pago que se registró y la solicitud
     * creada en la plataforma. Con ella el controlador arma la dirección del sitio de pagos a la que se redirige al comprador.
     *
     * @param idPago      El pago registrado, pendiente de confirmación (nulo si el descuento cubre todo el precio: en ese
     *                    caso no se registra ningún pago).
     * @param idSolicitud El identificador de la solicitud en la plataforma.
     * @param aprobado    Si el pago ya quedó aprobado (o, si no hay pago, si el descuento cubre todo el precio y se
     *                    habilitó el acceso).
     */
    record SolicitudPago(Integer idPago, String idSolicitud, boolean aprobado) {
    }
}
