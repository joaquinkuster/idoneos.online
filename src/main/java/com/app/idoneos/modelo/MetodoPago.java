package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Método mediante el cual se realiza un pago.
 */
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

    /**
     * Identificador único del método de pago.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMetodoPago")
    private int idMetodoPago;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el método de pago con los datos básicos.
     *
     * @param nombre nombre.
     */
    public MetodoPago(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del método de pago.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
