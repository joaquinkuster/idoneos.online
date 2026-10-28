package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro de avance de una inscripción sobre un tema.
 */
@Entity
@Table(name = "Progreso", uniqueConstraints = { @UniqueConstraint(columnNames = {"idInscripcion", "idTema"}) })
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
     * Relación con Tema.
     */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /**
     * Relación con Inscripcion.
     */
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /**
     * Indica si el tema fue completada.
     */
    @Column(name = "completada", nullable = false)
    private Boolean completada = false;

    /**
     * Fecha y hora en que se completó el tema. Es opcional.
     */
    @Column(name = "fechaCompletada", nullable = true)
    private LocalDateTime fechaCompletada;

    /**
     * Constructor para crear el progreso con los datos básicos.
     *
     * @param tema relación con Tema.
     * @param inscripcion relación con Inscripcion.
     */
    public Progreso(Tema tema, Inscripcion inscripcion) {
        this.tema = tema;
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
