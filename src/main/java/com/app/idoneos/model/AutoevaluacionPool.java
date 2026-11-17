package com.app.idoneos.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la relación entre una autoevaluación y un pool del que se extraen preguntas. */
@Entity
@Table(name = "AutoevaluacionPool")
@IdClass(AutoevaluacionPool.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class AutoevaluacionPool {
    /** Clave primaria compuesta de la relación entre autoevaluación y pool. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int autoevaluacion;
        private int pool;
    }

    /** Pool de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idPool", nullable = false)
    private Pool pool;

    /** Autoevaluacion de este registro. */
    @Id
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
        return "AutoevaluacionPool #" + autoevaluacion.getIdAutoevaluacion() + "-" + pool.getIdPool();
    }
}
