package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el estado de un pago. */
@Entity
@Table(name = "EstadoPago")
@Getter
@Setter
@NoArgsConstructor
public class EstadoPago {

    /** Estados de un pago. */
    public static final String PENDIENTE = "Pendiente";
    public static final String APROBADO = "Aprobado";
    public static final String RECHAZADO = "Rechazado";

    /** Identificador único del estado del pago. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoPago")
    private int idEstadoPago;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el estado del pago con los datos básicos. */
    public EstadoPago(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del estado del pago. */
    @Override
    public String toString() {
        return nombre;
    }
}
