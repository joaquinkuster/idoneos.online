package com.app.idoneos.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el comprobante digital emitido para un pago aprobado. */
@Entity
@Table(name = "Comprobante")
@Check(name = "ck_comprobante_dni", constraints = "dni_pagador ~ '^[0-9]{7,8}$'")
@Getter
@Setter
@NoArgsConstructor
public class Comprobante {

    /** Longitud máxima del nombre y del DNI del pagador (las de sus columnas). */
    public static final int LONGITUD_NOMBRE_PAGADOR = 100;
    public static final int LONGITUD_DNI_PAGADOR = 8;

    /** Arma el número de un comprobante: COMP-AAAA-NNNNNN, con el año de la emisión y el identificador del pago. */
    public static String generarNumero(int idPago, LocalDateTime fechaEmision) {
        return String.format("COMP-%d-%06d", fechaEmision.getYear(), idPago);
    }

    /** Identificador único del comprobante. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idComprobante")
    private int idComprobante;

    /** Pago de este registro. */
    @ManyToOne
    @JoinColumn(name = "idPago", nullable = false)
    private Pago pago;

    /** Número del comprobante. */
    @Column(name = "numero", nullable = false, length = 100, unique = true)
    private String numero;

    /** Fecha y hora de emisión del comprobante. */
    @Column(name = "fechaEmision", nullable = false)
    private LocalDateTime fechaEmision;

    /** Indica si el comprobante fue enviado por correo electrónico al pagador. */
    @Column(name = "enviado", nullable = false)
    private Boolean enviado = false;

    /** Nombre completo del pagador al momento de la emisión. */
    @Column(name = "nombrePagador", nullable = false, length = 100)
    private String nombrePagador;

    /** DNI del pagador al momento de la emisión. */
    @Column(name = "dniPagador", nullable = false, length = 8)
    private String dniPagador;

    /** Crea el comprobante con los datos básicos. */
    public Comprobante(Pago pago, String numero, LocalDateTime fechaEmision, String nombrePagador, String dniPagador) {
        this.pago = pago;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.nombrePagador = nombrePagador;
        this.dniPagador = dniPagador;
    }

    /** Devuelve una representación en forma de cadena del comprobante. */
    @Override
    public String toString() {
        return numero;
    }
}
