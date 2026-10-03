package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relación entre una autoevaluación y un pool de preguntas del cual se extraen las preguntas.
 */
@Entity
@Table(name = "AutoevaluacionPool", uniqueConstraints = { @UniqueConstraint(columnNames = {"idAutoevaluacion", "idPool"}) })
@Getter
@Setter
@NoArgsConstructor
public class AutoevaluacionPool {

    /**
     * Identificador único de la relación entre autoevaluación y pool.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAutoevaluacionPool")
    private int idAutoevaluacionPool;

    /**
     * Relación con Pool.
     */
    @ManyToOne
    @JoinColumn(name = "idPool", nullable = false)
    private Pool pool;

    /**
     * Relación con Autoevaluacion.
     */
    @ManyToOne
    @JoinColumn(name = "idAutoevaluacion", nullable = false)
    private Autoevaluacion autoevaluacion;

    /**
     * Constructor para crear la relación entre autoevaluación y pool con los datos básicos.
     *
     * @param pool relación con Pool.
     * @param autoevaluacion relación con Autoevaluacion.
     */
    public AutoevaluacionPool(Pool pool, Autoevaluacion autoevaluacion) {
        this.pool = pool;
        this.autoevaluacion = autoevaluacion;
    }

    /**
     * Devuelve una representación en forma de cadena de la relación entre autoevaluación y pool.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "AutoevaluacionPool #" + idAutoevaluacionPool;
    }
}
