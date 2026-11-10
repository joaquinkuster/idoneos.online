package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el estado del ciclo de vida de una clase en vivo. */
@Entity
@Table(name = "EstadoClaseEnVivo")
@Getter
@Setter
@NoArgsConstructor
public class EstadoClaseEnVivo {

    /** Nombre del estado de una clase que se está transmitiendo en este momento. */
    public static final String EN_VIVO = "En vivo";

    /** Identificador único del estado de la clase en vivo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoClaseEnVivo")
    private int idEstadoClaseEnVivo;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el estado de la clase en vivo con los datos básicos. */
    public EstadoClaseEnVivo(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del estado de la clase en vivo. */
    @Override
    public String toString() {
        return nombre;
    }
}
