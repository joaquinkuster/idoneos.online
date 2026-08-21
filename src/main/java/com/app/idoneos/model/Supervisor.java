package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad Supervisor: Asociación entre un Docente y un Curso en el que actúa como supervisor.
 * Mapea directamente a la tabla "Supervisor" en base_datos.sql.
 */
@Entity
@Table(name = "Supervisor")
public class Supervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Curso supervisado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    /** Docente que actúa como supervisor del curso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    public Supervisor() {
    }

    public Supervisor(Curso curso, Docente docente) {
        this.curso = curso;
        this.docente = docente;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }
}
