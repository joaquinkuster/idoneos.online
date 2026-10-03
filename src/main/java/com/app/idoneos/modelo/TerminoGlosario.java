package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Término del glosario de una unidad, con su definición.
 */
@Entity
@Table(name = "TerminoGlosario", uniqueConstraints = { @UniqueConstraint(columnNames = {"idUnidad", "termino"}) })
@Getter
@Setter
@NoArgsConstructor
public class TerminoGlosario {

    /**
     * Identificador único del término del glosario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTermino")
    private int idTermino;

    /**
     * Relación con Unidad.
     */
    @ManyToOne
    @JoinColumn(name = "idUnidad", nullable = false)
    private Unidad unidad;

    /**
     * Término del glosario.
     */
    @Column(name = "termino", nullable = false, length = 50)
    private String termino;

    /**
     * Definición del término.
     */
    @Column(name = "definicion", nullable = false, length = 150)
    private String definicion;

    /**
     * Fecha y hora de creación del registro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Indica si el registro fue dado de baja (baja lógica). El valor predeterminado es "false", indicando que está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Constructor para crear el término del glosario con los datos básicos.
     *
     * @param unidad relación con Unidad.
     * @param termino término del glosario.
     * @param definicion definición del término.
     */
    public TerminoGlosario(Unidad unidad, String termino, String definicion) {
        this.unidad = unidad;
        this.termino = termino;
        this.definicion = definicion;
    }

    /**
     * Devuelve una representación en forma de cadena del término del glosario.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return termino;
    }
}
