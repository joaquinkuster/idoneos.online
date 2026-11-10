package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la relación entre un curso y una modalidad de dictado en la que se ofrece. */
@Entity
@Table(name = "CursoModalidad", uniqueConstraints = { @UniqueConstraint(columnNames = {"idCurso", "idModalidad"}) })
@Getter
@Setter
@NoArgsConstructor
public class CursoModalidad {

    /** Identificador único de la relación entre curso y modalidad. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCursoModalidad")
    private int idCursoModalidad;

    /** Curso de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /** Modalidad de este registro. */
    @ManyToOne
    @JoinColumn(name = "idModalidad", nullable = false)
    private Modalidad modalidad;

    /** Crea la relación entre curso y modalidad con los datos básicos. */
    public CursoModalidad(Curso curso, Modalidad modalidad) {
        this.curso = curso;
        this.modalidad = modalidad;
    }

    /** Devuelve una representación en forma de cadena de la relación entre curso y modalidad. */
    @Override
    public String toString() {
        return "CursoModalidad #" + idCursoModalidad;
    }
}
