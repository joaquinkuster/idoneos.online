package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado del ciclo de vida de una clase en vivo.
 */
@Entity
@Table(name = "EstadoClaseEnVivo")
@Getter
@Setter
@NoArgsConstructor
public class EstadoClaseEnVivo {

    /** Nombre del estado de una clase que se está transmitiendo en este momento. */
    public static final String EN_VIVO = "En vivo";

    /**
     * Identificador único del estado de la clase en vivo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoClaseEnVivo")
    private int idEstadoClaseEnVivo;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el estado de la clase en vivo con los datos básicos.
     *
     * @param nombre nombre.
     */
    public EstadoClaseEnVivo(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del estado de la clase en vivo.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
