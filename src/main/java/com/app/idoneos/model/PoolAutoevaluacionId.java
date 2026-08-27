package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import java.io.Serializable;
import java.util.Objects;

/**
 * Clase de clave compuesta para la entidad PoolAutoevaluacion.
 * Corresponde a la PK (id_pool, id_autoevaluacion) de la tabla "Pool Autoevaluacion".
 */
public class PoolAutoevaluacionId implements Serializable {

    private int idPool;
    private int idAutoevaluacion;

    public PoolAutoevaluacionId() {
    }

    public PoolAutoevaluacionId(int idPool, int idAutoevaluacion) {
        this.idPool = idPool;
        this.idAutoevaluacion = idAutoevaluacion;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PoolAutoevaluacionId)) return false;
        PoolAutoevaluacionId that = (PoolAutoevaluacionId) o;
        return idPool == that.idPool && idAutoevaluacion == that.idAutoevaluacion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPool, idAutoevaluacion);
    }
}

