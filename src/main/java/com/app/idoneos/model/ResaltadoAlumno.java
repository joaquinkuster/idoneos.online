package com.app.idoneos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa las anotaciones de un alumno sobre una página de un documento PDF. */
@Entity
@Table(name = "ResaltadoAlumno", uniqueConstraints = @UniqueConstraint(name = "uk_resaltado_alumno_pagina", columnNames = {"idAlumno", "idRecurso", "pagina"}),
        indexes = @Index(name = "ix_resaltado_alumno_recurso", columnList = "idAlumno, idRecurso"))
@Getter
@Setter
@NoArgsConstructor
public class ResaltadoAlumno {

    /** Largo máximo del JSON de trazos de una página. */
    public static final int LONGITUD_TRAZOS = 600_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idResaltadoAlumno")
    private int idResaltadoAlumno;

    /** Alumno que hizo las anotaciones. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idRecurso", nullable = false)
    private Recurso recurso;

    /** Página anotada (la primera es la 1). */
    @Column(name = "pagina", nullable = false)
    private int pagina;

    /** Los trazos de la página, como un JSON. */
    @Column(name = "trazos", nullable = false, columnDefinition = "text")
    private String trazos;

    /** Fecha de creación. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación. */
    @Column(name = "ultimaModificacion")
    private LocalDateTime ultimaModificacion;

    /** Crea las anotaciones de una página. */
    public ResaltadoAlumno(Alumno alumno, Recurso recurso, int pagina, String trazos) {
        this.alumno = alumno;
        this.recurso = recurso;
        this.pagina = pagina;
        this.trazos = trazos;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return "Anotaciones de la página " + pagina;
    }
}
