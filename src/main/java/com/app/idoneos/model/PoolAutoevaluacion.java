package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Tabla asociativa M a N entre Pool de preguntas y Autoevaluación.
 * Permite que una autoevaluación incluya preguntas provenientes de múltiples
 * pools.
 */
@Entity
@Table(name = "Pool Autoevaluacion")
public class PoolAutoevaluacion {

    /** Identificador único del vínculo asociativo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Banco de preguntas (Pool) asociado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    /** Autoevaluación asociada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autoevaluacion_id", nullable = false)
    private Autoevaluacion autoevaluacion;

    public PoolAutoevaluacion() {
    }

    public PoolAutoevaluacion(Pool pool, Autoevaluacion autoevaluacion) {
        this.pool = pool;
        this.autoevaluacion = autoevaluacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Autoevaluacion getAutoevaluacion() {
        return autoevaluacion;
    }

    public void setAutoevaluacion(Autoevaluacion autoevaluacion) {
        this.autoevaluacion = autoevaluacion;
    }
}
