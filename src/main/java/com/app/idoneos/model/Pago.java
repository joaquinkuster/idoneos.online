package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private String paymentRequestID;

    @Column(name = "external_intention_id", nullable = false, length = 50)
    private String externalIntentionId;

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
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_pago_id", nullable = false)
    private EstadoPago estadoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id", nullable = false)
    private MetodoPago metodoPago;
}
