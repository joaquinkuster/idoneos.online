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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTermino() { return termino; }
    public void setTermino(String termino) { this.termino = termino; }

    public String getDefinicion() { return definicion; }
    public void setDefinicion(String definicion) { this.definicion = definicion; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }


    public TerminoGlosario(String termino, String definicion, Unidad unidad) {
        this.termino = termino;
        this.definicion = definicion;
        this.unidad = unidad;
    }

}
