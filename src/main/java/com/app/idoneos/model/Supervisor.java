package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;

import jakarta.persistence.*;

/**
 * Entidad Supervisor / Ayudante: Asociación entre un Docente y un Curso en el que actúa como ayudante/supervisor.
 * Mapea directamente a la tabla "Ayudante" en diseño_base_datos.sql.
 */
@Entity
@Table(name = "Ayudante")
public class Supervisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAyudante")
    private int id;

    /** Curso supervisado / asistido. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /** Docente que actúa como supervisor / ayudante del curso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDocente", nullable = false)
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

    public int getIdAyudante() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIdAyudante(int id) {
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
