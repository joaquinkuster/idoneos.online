package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad ModalidadCurso: Tabla asociativa M:N entre Modalidad y Curso.
 * Usa clave compuesta (id_modalidad, id_curso) conforme al SQL.
 * Mapea directamente a la tabla "Modalidad Curso" en base_datos.sql.
 */
@Entity
@Table(name = "Modalidad Curso")
@IdClass(ModalidadCursoId.class)
public class ModalidadCurso {

    /** Parte de la clave compuesta: referencia a Modalidad. */
    @Id
    @Column(name = "id_modalidad")
    private int idModalidad;

    /** Parte de la clave compuesta: referencia a Curso. */
    @Id
    @Column(name = "id_curso")
    private int idCurso;

    /** Modalidad asociada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modalidad", insertable = false, updatable = false)
    private Modalidad modalidad;

    /** Curso asociado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", insertable = false, updatable = false)
    private Curso curso;

    public ModalidadCurso() {
    }

    public ModalidadCurso(Modalidad modalidad, Curso curso) {
        this.modalidad = modalidad;
        this.curso = curso;
        this.idModalidad = modalidad.getId();
        this.idCurso = curso.getId();
    }

    public int getIdModalidad() {
        return idModalidad;
    }

    public void setIdModalidad(int idModalidad) {
        this.idModalidad = idModalidad;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
        if (modalidad != null) this.idModalidad = modalidad.getId();
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
        if (curso != null) this.idCurso = curso.getId();
    }
}
