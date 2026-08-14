package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad TérminoGlosario: Término técnico estructurado con su definición
 * asociado a una Unidad.
 * Mapea directamente a la tabla "TerminoGlosario" en base_datos.sql.
 */
@Entity
@Table(name = "TerminoGlosario")
public class TerminoGlosario {

    /** Identificador único del término del glosario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Palabra o concepto financiero/económico definido. */
    @Column(name = "termino", nullable = false, length = 50)
    private String termino;

    /** Definición conceptual del término. */
    @Column(name = "definicion", nullable = false, length = 150)
    private String definicion;

    /** Estado de baja lógica del término. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Unidad temática a la que pertenece el término del glosario. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    public TerminoGlosario() {
    }

    public TerminoGlosario(String termino, String definicion, Unidad unidad) {
        this.termino = termino;
        this.definicion = definicion;
        this.unidad = unidad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTermino() {
        return termino;
    }

    public void setTermino(String termino) {
        this.termino = termino;
    }

    public String getDefinicion() {
        return definicion;
    }

    public void setDefinicion(String definicion) {
        this.definicion = definicion;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean getBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }
}
