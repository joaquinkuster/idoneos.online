package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;

/**
 * Entidad PoolAutoevaluacion: Tabla asociativa M:N entre Pool de preguntas y Autoevaluación.
 * Usa clave compuesta (id_pool, id_autoevaluacion) conforme al SQL.
 * Mapea directamente a la tabla "Pool Autoevaluacion" en base_datos.sql.
 */
@Entity
@Table(name = "Pool Autoevaluacion")
@IdClass(PoolAutoevaluacionId.class)
public class PoolAutoevaluacion {

    /** Parte de la clave compuesta: referencia a Pool. */
    @Id
    @Column(name = "id_pool")
    private int idPool;

    /** Parte de la clave compuesta: referencia a Autoevaluacion. */
    @Id
    @Column(name = "id_autoevaluacion")
    private int idAutoevaluacion;

    /** Banco de preguntas (Pool) asociado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pool", insertable = false, updatable = false)
    private Pool pool;

    /** Autoevaluación asociada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autoevaluacion", insertable = false, updatable = false)
    private Autoevaluacion autoevaluacion;

    public PoolAutoevaluacion() {
    }

    public PoolAutoevaluacion(Pool pool, Autoevaluacion autoevaluacion) {
        this.pool = pool;
        this.autoevaluacion = autoevaluacion;
        this.idPool = pool.getId();
        this.idAutoevaluacion = autoevaluacion.getId();
    }

    public int getIdPool() {
        return idPool;
    }

    public void setIdPool(int idPool) {
        this.idPool = idPool;
    }

    public int getIdAutoevaluacion() {
        return idAutoevaluacion;
    }

    public void setIdAutoevaluacion(int idAutoevaluacion) {
        this.idAutoevaluacion = idAutoevaluacion;
    }

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
        if (pool != null) this.idPool = pool.getId();
    }

    public Autoevaluacion getAutoevaluacion() {
        return autoevaluacion;
    }

    public void setAutoevaluacion(Autoevaluacion autoevaluacion) {
        this.autoevaluacion = autoevaluacion;
        if (autoevaluacion != null) this.idAutoevaluacion = autoevaluacion.getId();
    }
}

