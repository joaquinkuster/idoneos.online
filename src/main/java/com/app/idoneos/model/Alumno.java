package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Alumno: Perfil de alumno vinculado a un Usuario.
 * Posee su propia PK (id_alumno) y una FK a Usuario (id_usuario).
 * Mapea directamente a la tabla "Alumno" en base_datos.sql.
 */
@Entity
@Table(name = "Alumno")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alumno")
    private int idAlumno;

    /** Usuario base al que pertenece este perfil de alumno. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Inscripciones del alumno a cohortes. */
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    /** Consultas realizadas en el foro. */
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private List<ConsultaForo> consultasForo = new ArrayList<>();

    public Alumno() {
    }

    public Alumno(Usuario usuario) {
        this.usuario = usuario;
    }

    public int getId() {
        return idAlumno;
    }

    public int getIdAlumno() {
        return idAlumno;
    }

    public void setId(int id) {
        this.idAlumno = id;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public List<ConsultaForo> getConsultasForo() {
        return consultasForo;
    }

    public void setConsultasForo(List<ConsultaForo> consultasForo) {
        this.consultasForo = consultasForo;
    }
}

