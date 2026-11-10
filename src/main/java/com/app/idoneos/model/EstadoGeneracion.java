package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el estado de la generación de un video con clon de inteligencia artificial. */
@Entity
@Table(name = "EstadoGeneracion")
@Getter
@Setter
@NoArgsConstructor
public class EstadoGeneracion {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoGeneracion")
    private int idEstadoGeneracion;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el registro con los datos básicos. */
    public EstadoGeneracion(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return nombre;
    }

    /** Estado de un video cuya generación todavía no se pidió o está en espera. */
    public static final String PENDIENTE = "Pendiente";

    /** Estado de un video que se está generando. */
    public static final String GENERANDO = "Generando";

    /** Estado de un video ya generado. */
    public static final String GENERADO = "Generado";

    /** Estado de un video cuya generación falló. */
    public static final String ERROR = "Error";
}
