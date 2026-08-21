package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Pago: Transacción de pago de una Inscripción.
 * Mapea directamente a la tabla "Pago" en base_datos.sql.
 */
@Entity
@Table(name = "Pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private int idPago;

    /** Monto total cobrado. */
    @Column(name = "monto", nullable = false)
    private float monto;

    /** Fecha y hora de la transacción. */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    /** ID de solicitud de pago (MercadoPago u otro gateway). */
    @Column(name = "payment_request_id", length = 50)
    private String paymentRequestId;

    /** ID de intención de pago externo. */
    @Column(name = "external_intention_id", length = 50, nullable = false)
    private String externalIntentionId = "";

    /** Código de referencia de la transacción. */
    @Column(name = "reference_code", length = 20)
    private String referenceCode;

    /** Últimos 4 dígitos de la tarjeta utilizada. */
    @Column(name = "ultimos_digitos_tarjeta", length = 4)
    private String ultimosDigitosTarjeta;

    /** Detalle del estado retornado por el gateway. */
    @Column(name = "detalle_estado", length = 100)
    private String detalleEstado;

    /** Fecha y hora de aprobación del pago. */
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    /** Nombre del titular pagador. */
    @Column(name = "nombre_pagador", length = 50)
    private String nombrePagador;

    /** DNI del titular pagador. */
    @Column(name = "dni_pagador", length = 8)
    private String dniPagador;

    /** Número del comprobante de pago. */
    @Column(name = "numero_comprobante", length = 100)
    private String numeroComprobante;

    /** Fecha de emisión del comprobante. */
    @Column(name = "fecha_emision_comprobante")
    private LocalDateTime fechaEmisionComprobante;

    /** Indica si el comprobante fue enviado al alumno. */
    @Column(name = "comprobante_enviado", nullable = false)
    private boolean comprobanteEnviado = false;

    /** Estado del pago (Pendiente, Aprobado, Rechazado). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_pago", nullable = false)
    private EstadoPago estadoPago;

    /** Método de pago utilizado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    /** Inscripción asociada al pago. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private Inscripcion inscripcion;

    /** Descuento aplicado al pago (puede ser nulo). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_descuento")
    private Descuento descuento;

    public Pago() {
    }

    public Pago(float monto, Inscripcion inscripcion, EstadoPago estadoPago) {
        this.monto = monto;
        this.inscripcion = inscripcion;
        this.estadoPago = estadoPago;
        this.fecha = LocalDateTime.now();
    }

    public int getId() {
        return idPago;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setId(int id) {
        this.idPago = id;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public float getMonto() {
        return monto;
    }

    public void setMonto(float monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public String getPaymentRequest() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public void setPaymentRequest(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public String getExternalIntentionId() {
        return externalIntentionId;
    }

    public void setExternalIntentionId(String externalIntentionId) {
        this.externalIntentionId = externalIntentionId;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getUltimosDigitosTarjeta() {
        return ultimosDigitosTarjeta;
    }

    public void setUltimosDigitosTarjeta(String ultimosDigitosTarjeta) {
        this.ultimosDigitosTarjeta = ultimosDigitosTarjeta;
    }

    public String getDetalleEstado() {
        return detalleEstado;
    }

    public void setDetalleEstado(String detalleEstado) {
        this.detalleEstado = detalleEstado;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public String getNombrePagador() {
        return nombrePagador;
    }

    public void setNombrePagador(String nombrePagador) {
        this.nombrePagador = nombrePagador;
    }

    public String getDniPagador() {
        return dniPagador;
    }

    public void setDniPagador(String dniPagador) {
        this.dniPagador = dniPagador;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public LocalDateTime getFechaEmisionComprobante() {
        return fechaEmisionComprobante;
    }

    public void setFechaEmisionComprobante(LocalDateTime fechaEmisionComprobante) {
        this.fechaEmisionComprobante = fechaEmisionComprobante;
    }

    public boolean isComprobanteEnviado() {
        return comprobanteEnviado;
    }

    public boolean getComprobanteEnviado() {
        return comprobanteEnviado;
    }

    public void setComprobanteEnviado(boolean comprobanteEnviado) {
        this.comprobanteEnviado = comprobanteEnviado;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }

    public Descuento getDescuento() {
        return descuento;
    }

    public void setDescuento(Descuento descuento) {
        this.descuento = descuento;
    }

    // Aliases de compatibilidad
    public void setPaymentId(String pId) {
        this.paymentRequestId = pId;
    }

    public void setPreferenceId(String pId) {
        this.externalIntentionId = pId;
    }

    public void setEmailPagador(String email) {
        this.nombrePagador = email;
    }
}
