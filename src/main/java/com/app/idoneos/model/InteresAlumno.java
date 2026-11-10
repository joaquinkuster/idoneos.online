package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una categoría de cursos en la que un alumno dijo tener interés. */
@Entity
@Table(name = "InteresAlumno", uniqueConstraints = @UniqueConstraint(name = "uk_interes_alumno", columnNames = {"idAlumno", "idCategoria"}))
@Getter
@Setter
@NoArgsConstructor
public class InteresAlumno {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInteresAlumno")
    private int idInteresAlumno;

    /** Alumno de este registro. */
    @ManyToOne
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    /** Categoria de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria categoria;

    /** Crea el interés con los datos básicos. */
    public InteresAlumno(Alumno alumno, Categoria categoria) {
        this.alumno = alumno;
        this.categoria = categoria;
    }
}
