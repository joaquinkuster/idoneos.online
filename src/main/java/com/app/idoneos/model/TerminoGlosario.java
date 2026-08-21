package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;

/**
 * Entidad TerminoGlosario: Término técnico con definición asociado a una Unidad.
 * Mapea directamente a la tabla "TerminoGlosario" en base_datos.sql.
 */
@Entity
@Table(name = "TerminoGlosario")
public class TerminoGlosario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_termino_glosario")
    private int idTerminoGlosario;

    /** Palabra o concepto definido. */
    @Column(name = "termino", nullable = false, length = 50)
    private String termino;

    /** Definición conceptual del término. */
    @Column(name = "definicion", nullable = false, length = 150)
    private String definicion;

    /** Estado de baja lógica del término. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Unidad temática a la que pertenece el término. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    public TerminoGlosario() {
    }

    public TerminoGlosario(String termino, String definicion, Unidad unidad) {
        this.termino = termino;
        this.definicion = definicion;
        this.unidad = unidad;
    }

    public int getId() {
        return idTerminoGlosario;
    }

    public int getIdTerminoGlosario() {
        return idTerminoGlosario;
    }

    public void setId(int id) {
        this.idTerminoGlosario = id;
    }

    public void setIdTerminoGlosario(int idTerminoGlosario) {
        this.idTerminoGlosario = idTerminoGlosario;
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

