package com.app.idoneos.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un documento elegido como fuente de un banco de preguntas generado con inteligencia artificial. */
@Entity
@Table(name = "PoolFuente")
@IdClass(PoolFuente.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class PoolFuente {

    /** Clave primaria compuesta de la fuente de un banco de preguntas. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int pool;
        private int documento;
    }

    /** Banco de preguntas que salió de la fuente. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idPool", nullable = false)
    private Pool pool;

    /** Documento elegido como fuente. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idDocumento", nullable = false)
    private Documento documento;

    /** Crea la fuente de un banco de preguntas. */
    public PoolFuente(Pool pool, Documento documento) {
        this.pool = pool;
        this.documento = documento;
    }
}
