package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Posición y duración en semanas de una unidad dentro del cronograma de un programa.
 */
@Entity
@Table(name = "UnidadCronograma", uniqueConstraints = { @UniqueConstraint(columnNames = {"idPrograma", "idUnidad"}), @UniqueConstraint(columnNames = {"idPrograma", "numeroOrden"}) })
@Getter
@Setter
@NoArgsConstructor
public class UnidadCronograma {

    /**
     * Identificador único de la unidad del cronograma.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUnidadCronograma")
    private int idUnidadCronograma;

    /**
     * Relación con Programa.
     */
    @ManyToOne
    @JoinColumn(name = "idPrograma", nullable = false)
    private Programa programa;

    /**
     * Relación con Unidad.
     */
    @ManyToOne
    @JoinColumn(name = "idUnidad", nullable = false)
    private Unidad unidad;

    /**
     * Número de orden de la unidad dentro del cronograma.
     */
    @Column(name = "numeroOrden", nullable = false)
    private int numeroOrden;

    /**
     * Duración de la unidad en semanas.
     */
    @Column(name = "semanasDuracion", nullable = false)
    private int semanasDuracion;

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
     * Constructor para crear la unidad del cronograma con los datos básicos.
     *
     * @param programa relación con Programa.
     * @param unidad relación con Unidad.
     * @param numeroOrden número de orden de la unidad dentro del cronograma.
     * @param semanasDuracion duración de la unidad en semanas.
     */
    public UnidadCronograma(Programa programa, Unidad unidad, int numeroOrden, int semanasDuracion) {
        this.programa = programa;
        this.unidad = unidad;
        this.numeroOrden = numeroOrden;
        this.semanasDuracion = semanasDuracion;
    }

    /**
     * Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true.
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja (baja = true), false si está vigente.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena de la unidad del cronograma.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "UnidadCronograma #" + idUnidadCronograma;
    }
}
