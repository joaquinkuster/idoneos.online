package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el método con el que se realiza un pago. */
@Entity
@Table(name = "MetodoPago")
@Getter
@Setter
@NoArgsConstructor
public class MetodoPago {

    /** Pago online con MODO (tarjeta o cuenta, desde la app de MODO o de un banco). */
    public static final String MODO = "MODO";

    /** Transferencia bancaria al CBU de la institución, registrada por el administrador. */
    public static final String TRANSFERENCIA = "Transferencia bancaria";

    /** Identificador único del método de pago. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMetodoPago")
    private int idMetodoPago;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el método de pago con los datos básicos. */
    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del método de pago. */
    @Override
    public String toString() {
        return nombre;
    }
}
