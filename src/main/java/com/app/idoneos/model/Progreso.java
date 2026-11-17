package com.app.idoneos.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el avance de una inscripción sobre un tema. */
@Entity
@Table(name = "Progreso")
@IdClass(Progreso.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class Progreso {
    /** Clave primaria compuesta del progreso de una inscripción. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int inscripcion;
        private int tema;
    }

    /** Tema de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /** Inscripcion de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idInscripcion", nullable = false)
    private Inscripcion inscripcion;

    /** Indica si el tema fue completada. */
    @Column(name = "completada", nullable = false)
    private Boolean completada = false;

    /** Fecha y hora en que se completó el tema. */
    @Column(name = "fechaCompletada", nullable = true)
    private LocalDateTime fechaCompletada;

    /** Crea el progreso con los datos básicos. */
    public Progreso(Tema tema, Inscripcion inscripcion) {
        this.tema = tema;
        this.inscripcion = inscripcion;
    }

    /** Devuelve una representación en forma de cadena del progreso. */
    @Override
    public String toString() {
        return "Progreso #" + inscripcion.getIdInscripcion() + "-" + tema.getIdTema();
    }
}
