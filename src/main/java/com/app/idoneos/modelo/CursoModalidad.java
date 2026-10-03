package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relación entre un curso y una modalidad de dictado en la que se ofrece.
 */
@Entity
@Table(name = "CursoModalidad", uniqueConstraints = { @UniqueConstraint(columnNames = {"idCurso", "idModalidad"}) })
@Getter
@Setter
@NoArgsConstructor
public class CursoModalidad {

    /**
     * Identificador único de la relación entre curso y modalidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCursoModalidad")
    private int idCursoModalidad;

    /**
     * Relación con Curso.
     */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /**
     * Relación con Modalidad.
     */
    @ManyToOne
    @JoinColumn(name = "idModalidad", nullable = false)
    private Modalidad modalidad;

    /**
     * Constructor para crear la relación entre curso y modalidad con los datos básicos.
     *
     * @param curso relación con Curso.
     * @param modalidad relación con Modalidad.
     */
    public CursoModalidad(Curso curso, Modalidad modalidad) {
        this.curso = curso;
        this.modalidad = modalidad;
    }

    /**
     * Devuelve una representación en forma de cadena de la relación entre curso y modalidad.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "CursoModalidad #" + idCursoModalidad;
    }
}
