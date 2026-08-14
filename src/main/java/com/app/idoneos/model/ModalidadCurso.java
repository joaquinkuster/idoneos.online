package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad ModalidadCurso: Tabla asociativa M a N entre Modalidad y Curso
 * ("Modalidad Curso").
 * Mapea directamente a la tabla "Modalidad Curso" en base_datos.sql.
 */
@Entity
@Table(name = "Modalidad Curso")
public class ModalidadCurso {

    /** Identificador único del vínculo de modalidad por curso. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Modalidad asociada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modalidad_id")
    private Modalidad modalidad;

    /** Curso asociado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    public ModalidadCurso() {
    }

    public ModalidadCurso(Modalidad modalidad, Curso curso) {
        this.modalidad = modalidad;
        this.curso = curso;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}
