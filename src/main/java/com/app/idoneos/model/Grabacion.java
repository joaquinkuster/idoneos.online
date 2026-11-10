package com.app.idoneos.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa los datos propios de un recurso que es la grabación de una clase. */
@Entity
@Table(name = "Grabacion")
@Getter
@Setter
@NoArgsConstructor
public class Grabacion {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idGrabacion")
    private int idGrabacion;

    /** Relación con Recurso: el recurso del que esta tabla guarda los datos propios. */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /** Ruta del archivo de video, relativa a la carpeta de cursos. */
    @Column(name = "rutaArchivo", nullable = false, length = 500)
    private String rutaArchivo;

    /** Tamaño del archivo en bytes. */
    @Column(name = "tamanioBytes", nullable = true)
    private Long tamanioBytes;

    /** Duración del video en minutos. */
    @Column(name = "duracionMinutos", nullable = true)
    private Integer duracionMinutos;

    /** Conjunto de clases en vivo cuya grabación es este recurso. */
    @OneToMany(mappedBy = "grabacion")
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /** Crea la grabación con los datos básicos. */
    public Grabacion(Recurso recurso, String rutaArchivo) {
        this.recurso = recurso;
        this.rutaArchivo = rutaArchivo;
    }
}
