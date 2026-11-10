package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el estado del avance de un alumno en su inscripción. */
@Entity
@Table(name = "EstadoAvance")
@Getter
@Setter
@NoArgsConstructor
public class EstadoAvance {

    /** Todavía no completó todos los temas. */
    public static final String EN_PROGRESO = "En progreso";

    /** Completó todos los temas del cronograma. */
    public static final String FINALIZADO = "Finalizado";

    /** Identificador único del estado del avance. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoAvance")
    private int idEstadoAvance;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el estado del avance con los datos básicos. */
    public EstadoAvance(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del estado del avance. */
    @Override
    public String toString() {
        return nombre;
    }
}
