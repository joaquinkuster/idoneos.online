package com.app.idoneos.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la relación entre un curso y una modalidad de dictado en la que se ofrece. */
@Entity
@Table(name = "CursoModalidad")
@IdClass(CursoModalidad.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class CursoModalidad {
    /** Clave primaria compuesta de la relación entre curso y modalidad. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int modalidad;
        private int curso;
    }

    /** Curso de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /** Modalidad de este registro. */
    @Id
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
        return "CursoModalidad #" + modalidad.getIdModalidad() + "-" + curso.getIdCurso();
    }
}
