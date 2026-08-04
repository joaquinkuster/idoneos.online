package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Pregunta de evaluación. Puede ser de opción múltiple o verdadero/falso.
 */
@Entity
@Table(name = "preguntas")
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

    public Pregunta(String texto, Boolean esOpcionMultiple, Pool pool) {
        this.texto = texto;
        this.esOpcionMultiple = esOpcionMultiple;
        this.pool = pool;
    }
}
