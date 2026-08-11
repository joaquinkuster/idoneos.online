package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "termino_glosario")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminoGlosario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "termino", nullable = false, length = 50)
    private String termino;

    @Column(name = "definicion", nullable = false, length = 150)
    private String definicion;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;
}
