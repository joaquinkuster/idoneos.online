package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pool_autoevaluacion")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolAutoevaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autoevaluacion_id", nullable = false)
    private Autoevaluacion autoevaluacion;

    public PoolAutoevaluacion(Pool pool, Autoevaluacion autoevaluacion) {
        this.pool = pool;
        this.autoevaluacion = autoevaluacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Pool getPool() { return pool; }
    public void setPool(Pool pool) { this.pool = pool; }

    public Autoevaluacion getAutoevaluacion() { return autoevaluacion; }
    public void setAutoevaluacion(Autoevaluacion autoevaluacion) { this.autoevaluacion = autoevaluacion; }


    public PoolAutoevaluacion() {}

}
