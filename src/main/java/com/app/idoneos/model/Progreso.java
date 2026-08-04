package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Registra el progreso secuencial de un alumno en una unidad específica.
 */
@Entity
@Table(name = "progreso")
@Getter @Setter
@NoArgsConstructor
public class Progreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_inscripcion", nullable = false)
    private Inscripcion inscripcion;

    @ManyToOne
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    @Column(name = "completada", nullable = false)
    private Boolean completada = false;

    @Column(name = "fecha_completado", nullable = true)
    private LocalDate fechaCompletado;

    public Progreso(Inscripcion inscripcion, Unidad unidad, Boolean completada) {
        this.inscripcion = inscripcion;
        this.unidad = unidad;
        this.completada = completada;
        if (completada) {
            this.fechaCompletado = LocalDate.now();
        }
    }
}
