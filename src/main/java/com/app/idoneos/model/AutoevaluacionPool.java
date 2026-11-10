package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la relación entre una autoevaluación y un pool del que se extraen preguntas. */
@Entity
@Table(name = "AutoevaluacionPool", uniqueConstraints = { @UniqueConstraint(columnNames = {"idAutoevaluacion", "idPool"}) })
@Getter
@Setter
@NoArgsConstructor
public class AutoevaluacionPool {

    /** Identificador único de la relación entre autoevaluación y pool. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAutoevaluacionPool")
    private int idAutoevaluacionPool;

    /** Pool de este registro. */
    @ManyToOne
    @JoinColumn(name = "idPool", nullable = false)
    private Pool pool;

    /** Autoevaluacion de este registro. */
    @ManyToOne
    @JoinColumn(name = "idAutoevaluacion", nullable = false)
    private Autoevaluacion autoevaluacion;

    /** Crea la relación entre autoevaluación y pool con los datos básicos. */
    public AutoevaluacionPool(Pool pool, Autoevaluacion autoevaluacion) {
        this.pool = pool;
        this.autoevaluacion = autoevaluacion;
    }

    /** Devuelve una representación en forma de cadena de la relación entre autoevaluación y pool. */
    @Override
    public String toString() {
        return "AutoevaluacionPool #" + idAutoevaluacionPool;
    }
}
