package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad Alumno: Subtipo de Usuario (relación 1 a 0..1 mediante clave compartida).
 * Representa al usuario con rol de estudiante que se inscribe a dictados de cursos y rinde autoevaluaciones.
 * Mapea directamente a la tabla "Alumno" en base_datos.sql.
 */
@Entity
@Table(name = "\"Alumno\"")
public class Alumno {

    /** Identificador del alumno, coincidente con el id de Usuario (clave primaria compartida). */
    @Id
    @Column(name = "id")
    private int id;

    /** Relación 1 a 1 con la entidad base Usuario (@MapsId vincula la PK con Usuario). */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    public Alumno() {}

    public Alumno(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.id = usuario.getId();
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
