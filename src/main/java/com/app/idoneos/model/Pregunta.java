package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Pregunta de evaluación. Puede ser de opción múltiple o verdadero/falso.
 */
@Entity
@Table(name = "pregunta")
@Getter @Setter
@NoArgsConstructor
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /**
     * true = opción múltiple; false = verdadero/falso.
     */
    @Column(name = "es_opcion_multiple", nullable = false)
    private Boolean esOpcionMultiple = true;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pool", nullable = false)
    private Pool pool;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcionRespuesta> opciones = new ArrayList<>();

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public Boolean getEsOpcionMultiple() { return esOpcionMultiple; }
    public void setEsOpcionMultiple(Boolean esOpcionMultiple) { this.esOpcionMultiple = esOpcionMultiple; }

    public Boolean getBaja() { return baja; }
    public void setBaja(Boolean baja) { this.baja = baja; }

    public Pool getPool() { return pool; }
    public void setPool(Pool pool) { this.pool = pool; }

    public List<OpcionRespuesta> getOpciones() { return opciones; }
    public void setOpciones(List<OpcionRespuesta> opciones) { this.opciones = opciones; }

    public Pregunta(String texto, Boolean esOpcionMultiple, Pool pool) {
        this.texto = texto;
        this.esOpcionMultiple = esOpcionMultiple;
        this.pool = pool;
    }
}
