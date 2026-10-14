package com.app.idoneos.servicio.Pago;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.modelo.EstadoPago;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.MetodoPago;
import com.app.idoneos.modelo.Pago;

/**
 * Servicio para gestionar las operaciones relacionadas con el pago.
 */
public interface PagoServicio {

    /**
     * Busca los registros de pago asociados a estadoPago.
     *
     * @param estadoPago el registro de EstadoPago asociado
     * @return una lista de Pago asociados al EstadoPago indicado
     */
    List<Pago> buscarPorEstadoPago(EstadoPago estadoPago);

    /**
     * Busca los registros de pago asociados a metodoPago.
     *
     * @param metodoPago el registro de MetodoPago asociado
     * @return una lista de Pago asociados al MetodoPago indicado
     */
    List<Pago> buscarPorMetodoPago(MetodoPago metodoPago);

    /**
     * Busca los registros de pago asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Pago asociados al Inscripcion indicado
     */
    List<Pago> buscarPorInscripcion(Inscripcion inscripcion);

    /**
     * Busca los registros de pago asociados a descuento.
     *
     * @param descuento el registro de Descuento asociado
     * @return una lista de Pago asociados al Descuento indicado
     */
    List<Pago> buscarPorDescuento(Descuento descuento);

    /**
     * Busca el pago por su atributo único 'codigoReferencia'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param codigoReferencia el valor de 'codigoReferencia' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Pago> buscarPorCodigoReferencia(String codigoReferencia);

    /**
     * Busca el pago por su atributo único 'idIntencionExterna'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param idIntencionExterna el valor de 'idIntencionExterna' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Pago> buscarPorIdIntencionExterna(String idIntencionExterna);

    /**
     * Busca el pago por su atributo único 'idSolicitudPago'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param idSolicitudPago el valor de 'idSolicitudPago' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Pago> buscarPorIdSolicitudPago(String idSolicitudPago);

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
     * Calcula el monto que debe pagar el alumno por una inscripción: el precio del curso con el descuento aplicado.
     *
     * @param inscripcion la inscripción que se va a pagar
     * @return el monto a pagar
     */
    float calcularMontoAPagar(Inscripcion inscripcion);

    /**
     * Inicia el pago online de una inscripción pendiente de pago (CU-51): calcula el monto con el descuento, registra
     * el pago como pendiente y crea la solicitud en la plataforma. Si ya hay un pago en curso lo retoma; si el último
     * fue rechazado reutiliza su registro (CU-52). Si el descuento cubre todo el precio, aprueba el pago sin pasar por
     * la plataforma.
     *
     * @param idInscripcion la inscripción que se paga
     * @param alumno el alumno dueño de la inscripción
     * @return los datos para que el alumno complete el pago
     * @throws IllegalArgumentException si no se puede pagar la inscripción
     */
    SolicitudDePago iniciarPago(Integer idInscripcion, Alumno alumno);

    /**
     * Reintenta un pago rechazado reutilizando su registro (CU-52).
     *
     * @param idPago el pago rechazado
     * @param alumno el alumno dueño del pago
     * @return los datos para que el alumno complete el pago
     * @throws IllegalArgumentException si el pago no está rechazado o su inscripción ya tiene acceso habilitado
     */
    SolicitudDePago reintentarPago(Integer idPago, Alumno alumno);

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
    int conciliarPagosPendientes();

    /**
     * El administrador registra el pago de una inscripción recibido por fuera de MODO, por transferencia bancaria
     * (CU-53). El pago queda aprobado, se habilita el acceso y se emite el comprobante.
     *
     * @param idInscripcion la inscripción que se paga
     * @param monto el monto recibido
     * @param referencia el comprobante de la transferencia (número de operación)
     * @param observaciones notas del administrador (opcional)
     * @return el pago registrado
     * @throws IllegalArgumentException si falta algún dato o la inscripción ya está paga
     */
    Pago registrarPagoManual(Integer idInscripcion, Float monto, String referencia, String observaciones);

    /**
     * El administrador acredita o rechaza manualmente un pago pendiente o rechazado, corrigiendo su monto o
     * comprobante (CU-54). Al aprobarlo se habilita el acceso y se emite el comprobante.
     *
     * @param idPago el pago
     * @param estado el nuevo estado: Aprobado o Rechazado
     * @param monto el monto (obligatorio)
     * @param referencia el comprobante de la transferencia (obligatorio si es una transferencia)
     * @param nombrePagador el nombre del pagador (obligatorio al aprobar un pago de MODO)
     * @param dniPagador el DNI del pagador (obligatorio al aprobar un pago de MODO)
     * @param motivo el motivo, si se rechaza (opcional)
     * @return el pago actualizado
     * @throws IllegalArgumentException si el pago no está pendiente o rechazado o falta algún dato
     */
    Pago modificarPago(Integer idPago, String estado, Float monto, String referencia, String nombrePagador,
            String dniPagador, String motivo);

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
}
