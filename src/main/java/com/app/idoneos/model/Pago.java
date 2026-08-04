package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de un pago procesado a través de la API de pagos.
 * paymentId y preferenceId permiten conciliar el flujo asincrónico (webhook).
 */
@Entity
@Table(name = "pago")
@Getter @Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "monto", nullable = false)
    private Double monto;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "payment_id", nullable = true, length = 100)
    private String paymentId;

    @Column(name = "preference_id", nullable = true, length = 100)
    private String preferenceId;

    @Column(name = "email_pagador", nullable = true, length = 150)
    private String emailPagador;

    @Column(name = "nombre_pagador", nullable = true, length = 50)
    private String nombrePagador;

    @Column(name = "tipo_pago", nullable = true, length = 20)
    private String tipoPago;

    @Column(name = "ultimos_digitos_tarjeta", nullable = true, length = 4)
    private String ultimosDigitosTarjeta;

    @Column(name = "detalle_estado", nullable = true, length = 100)
    private String detalleEstado;

    @Column(name = "fecha_aprobacion", nullable = true)
    private LocalDateTime fechaAprobacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_pago", nullable = false)
    private EstadoPago estadoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago", nullable = true)
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private Inscripcion inscripcion;

    @OneToOne(mappedBy = "pago", cascade = CascadeType.ALL)
    private Comprobante comprobante;

    public Pago(Double monto, Inscripcion inscripcion, EstadoPago estadoPago) {
        this.monto = monto;
        this.inscripcion = inscripcion;
        this.estadoPago = estadoPago;
    }
}
