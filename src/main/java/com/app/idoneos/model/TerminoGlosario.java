package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Término del glosario de una unidad.
 * Cuelga directo de Unidad (dato estructurado par término-definición),
 * NO es un TipoMaterial — se modela como entidad separada.
 */
@Entity
@Table(name = "termino_glosario")
@Getter @Setter
@NoArgsConstructor
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
    private Boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    public TerminoGlosario(String termino, String definicion, Unidad unidad) {
        this.termino = termino;
        this.definicion = definicion;
        this.unidad = unidad;
    }
}
