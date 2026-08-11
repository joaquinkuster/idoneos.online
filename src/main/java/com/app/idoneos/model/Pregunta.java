package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pregunta")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    @Column(name = "es_opcion_multiple", nullable = false)
    private boolean esOpcionMultiple = true;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    private List<OpcionRespuesta> opcionesRespuesta = new ArrayList<>();
}
