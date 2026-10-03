package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado del ciclo de vida de una clase con clon de inteligencia artificial.
 */
@Entity
@Table(name = "EstadoClaseClon")
@Getter
@Setter
@NoArgsConstructor
public class EstadoClaseClon {

    /**
     * Identificador único del estado de la clase con clon.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoClaseClon")
    private int idEstadoClaseClon;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el estado de la clase con clon con los datos básicos.
     *
     * @param nombre nombre.
     */
    public EstadoClaseClon(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del estado de la clase con clon.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
