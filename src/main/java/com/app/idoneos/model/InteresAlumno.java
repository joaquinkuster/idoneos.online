package com.app.idoneos.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una categoría de cursos en la que un alumno dijo tener interés. */
@Entity
@Table(name = "InteresAlumno")
@IdClass(InteresAlumno.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class InteresAlumno {
    /** Clave primaria compuesta del interés del alumno. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int categoria;
        private int alumno;
    }

    /** Alumno de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    /** Categoria de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria categoria;

    /** Crea el interés con los datos básicos. */
    public InteresAlumno(Alumno alumno, Categoria categoria) {
        this.alumno = alumno;
        this.categoria = categoria;
    }
}
