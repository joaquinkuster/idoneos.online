package com.app.idoneos.modelo;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Categoría de cursos en la que un alumno dijo tener interés.
 */
@Entity
@Table(name = "InteresAlumno", uniqueConstraints = @UniqueConstraint(name = "uk_interes_alumno", columnNames = {"idAlumno", "idCategoria"}))
@Getter
@Setter
@NoArgsConstructor
public class InteresAlumno {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInteresAlumno")
    private int idInteresAlumno;

    /**
     * Relación con Alumno.
     */
    @ManyToOne
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    /**
     * Relación con Categoria.
     */
    @ManyToOne
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria categoria;

    /**
     * Constructor para crear el interés con los datos básicos.
     *
     * @param alumno    el alumno.
     * @param categoria la categoría.
     */
    public InteresAlumno(Alumno alumno, Categoria categoria) {
        this.alumno = alumno;
        this.categoria = categoria;
    }
}
