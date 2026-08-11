package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Pago: registra la transacción procesada.
 * Los datos del comprobante se guardan directamente como atributos de la entidad.
 */
@Entity
@Table(name = "\"Pago\"")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "monto", nullable = false)
    private double monto;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "payment_request_id", length = 50)
    private String paymentRequest;

    @Column(name = "external_intention_id", length = 50, nullable = false)
    private String externalIntentionId = "";

    @Column(name = "reference_code", length = 20)
    private String referenceCode;

    @Column(name = "tipo_pago", length = 20)
    private String tipoPago;

    @Column(name = "ultimos_digitos_tarjeta", length = 4)
    private String ultimosDigitosTarjeta;

    @Column(name = "detalle_estado", length = 100)
    private String detalleEstado;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "nombre_pagador", length = 50)
    private String nombrePagador;

    @Column(name = "numero_comprobante", length = 100)
    private String numeroComprobante;

    @Column(name = "fecha_emision_comprobante")
    private LocalDateTime fechaEmisionComprobante;

    @Column(name = "comprobante_enviado", nullable = false)
    private boolean comprobanteEnviado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id")
    private Inscripcion inscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_pago_id")
    private EstadoPago estadoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;

    public Pago() {}

    public Pago(double monto, Inscripcion inscripcion, EstadoPago estadoPago) {
        this.monto = monto;
        this.inscripcion = inscripcion;
        this.estadoPago = estadoPago;
        this.fecha = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getPaymentRequest() { return paymentRequest; }
    public void setPaymentRequest(String paymentRequest) { this.paymentRequest = paymentRequest; }

    public String getExternalIntentionId() { return externalIntentionId; }
    public void setExternalIntentionId(String externalIntentionId) { this.externalIntentionId = externalIntentionId; }

    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public String getUltimosDigitosTarjeta() { return ultimosDigitosTarjeta; }
    public void setUltimosDigitosTarjeta(String ultimosDigitosTarjeta) { this.ultimosDigitosTarjeta = ultimosDigitosTarjeta; }

    public String getDetalleEstado() { return detalleEstado; }
    public void setDetalleEstado(String detalleEstado) { this.detalleEstado = detalleEstado; }

    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    public String getNombrePagador() { return nombrePagador; }
    public void setNombrePagador(String nombrePagador) { this.nombrePagador = nombrePagador; }

    public String getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(String numeroComprobante) { this.numeroComprobante = numeroComprobante; }

    public LocalDateTime getFechaEmisionComprobante() { return fechaEmisionComprobante; }
    public void setFechaEmisionComprobante(LocalDateTime fechaEmisionComprobante) { this.fechaEmisionComprobante = fechaEmisionComprobante; }

    public boolean isComprobanteEnviado() { return comprobanteEnviado; }
    public boolean getComprobanteEnviado() { return comprobanteEnviado; }
    public void setComprobanteEnviado(boolean comprobanteEnviado) { this.comprobanteEnviado = comprobanteEnviado; }

    public Inscripcion getInscripcion() { return inscripcion; }
    public void setInscripcion(Inscripcion inscripcion) { this.inscripcion = inscripcion; }

    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    // Aliases para compatibilidad
    public void setPaymentId(String pId) { this.paymentRequest = pId; }
    public void setPreferenceId(String pId) { this.externalIntentionId = pId; }
    public void setEmailPagador(String email) { this.nombrePagador = email; }
}
