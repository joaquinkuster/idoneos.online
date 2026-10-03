package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de avance de una inscripción sobre una unidad.
 */
@Entity
@Table(name = "Progreso", uniqueConstraints = { @UniqueConstraint(columnNames = {"idInscripcion", "idUnidad"}) })
@Getter
@Setter
@NoArgsConstructor
public class Progreso {

    /**
     * Identificador único del progreso.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProgreso")
    private int idProgreso;

    /**
     * Relación con Unidad.
     */
    @ManyToOne
    @JoinColumn(name = "idUnidad", nullable = false)
    private Unidad unidad;

    /**
     * Relación con Inscripcion.
     */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /**
     * Indica si la unidad fue completada.
     */
    @Column(name = "completada", nullable = false)
    private Boolean completada = false;

    /**
     * Fecha y hora en que se completó la unidad. Es opcional.
     */
    @Column(name = "fechaCompletada", nullable = true)
    private LocalDateTime fechaCompletada;

    /**
     * Constructor para crear el progreso con los datos básicos.
     *
     * @param unidad relación con Unidad.
     * @param inscripcion relación con Inscripcion.
     */
    public Progreso(Unidad unidad, Inscripcion inscripcion) {
        this.unidad = unidad;
        this.inscripcion = inscripcion;
    }

    /**
     * Devuelve una representación en forma de cadena del progreso.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Progreso #" + idProgreso;
    }
}
