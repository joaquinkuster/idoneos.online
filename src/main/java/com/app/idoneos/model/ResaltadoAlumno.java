package com.app.idoneos.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa las anotaciones de un alumno sobre una página de un documento PDF. */
@Entity
@Table(name = "ResaltadoAlumno", indexes = @Index(name = "ix_resaltado_alumno_documento", columnList = "idAlumno, idDocumento"))
@IdClass(ResaltadoAlumno.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class ResaltadoAlumno {

    /** Largo máximo del JSON de trazos de una página. */
    public static final int LONGITUD_TRAZOS = 600_000;

    /** Clave primaria compuesta de las anotaciones de una página. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int documento;
        private int alumno;
        private int pagina;
    }

    /** Alumno que hizo las anotaciones. */
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    /** Documento anotado. */
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "idDocumento", nullable = false)
    private Documento documento;

    /** Página anotada (la primera es la 1). */
    @Id
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
    public ResaltadoAlumno(Alumno alumno, Documento documento, int pagina, String trazos) {
        this.alumno = alumno;
        this.documento = documento;
        this.pagina = pagina;
        this.trazos = trazos;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return "Anotaciones de la página " + pagina;
    }
}
