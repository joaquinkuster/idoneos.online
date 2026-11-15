package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el estado de la generación de un contenido con inteligencia artificial. */
@Entity
@Table(name = "EstadoGeneracionIA")
@Getter
@Setter
@NoArgsConstructor
public class EstadoGeneracionIA {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoGeneracionIA")
    private int idEstadoGeneracionIA;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el registro con los datos básicos. */
    public EstadoGeneracionIA(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return nombre;
    }

    /** Estado de un contenido cuya generación está en espera o en curso. */
    public static final String PENDIENTE = "Pendiente";

    /** Estado de un contenido ya generado. */
    public static final String LISTO = "Listo";

    /** Estado de un contenido cuya generación falló. */
    public static final String FALLIDO = "Fallido";
}
