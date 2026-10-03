package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado en el que se encuentra un pago.
 */
@Entity
@Table(name = "EstadoPago")
@Getter
@Setter
@NoArgsConstructor
public class EstadoPago {

    /**
     * Identificador único del estado del pago.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoPago")
    private int idEstadoPago;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el estado del pago con los datos básicos.
     *
     * @param nombre nombre.
     */
    public EstadoPago(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del estado del pago.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
