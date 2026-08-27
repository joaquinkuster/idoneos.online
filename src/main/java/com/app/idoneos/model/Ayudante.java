package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;

import jakarta.persistence.*;

/**
 * Entidad Ayudante: Asociación entre un Docente y un Curso en el que actúa como ayudante.
 * Mapea directamente a la tabla "Ayudante" en diseño_base_datos.sql.
 */
@Entity
@Table(name = "Ayudante")
public class Ayudante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAyudante")
    private int idAyudante;

    /** Curso en el que participa como ayudante. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /** Docente que actúa como ayudante del curso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDocente", nullable = false)
    private Docente docente;

    public Ayudante() {
    }

    public Ayudante(Curso curso, Docente docente) {
        this.curso = curso;
        this.docente = docente;
    }

    public int getId() {
        return idAyudante;
    }

    public int getIdAyudante() {
        return idAyudante;
    }

    public void setId(int id) {
        this.idAyudante = id;
    }

    public void setIdAyudante(int idAyudante) {
        this.idAyudante = idAyudante;
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
