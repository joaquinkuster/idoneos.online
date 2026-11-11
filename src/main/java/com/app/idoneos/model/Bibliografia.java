package com.app.idoneos.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa los datos propios de un recurso que es una bibliografía. */
@Entity
@Table(name = "Bibliografia")
@Check(name = "ck_bibliografia_paginas", constraints = "paginas is null or paginas > 0")
@Check(name = "ck_bibliografia_tamanio", constraints = "tamanio_bytes is null or tamanio_bytes >= 0")
@Getter
@Setter
@NoArgsConstructor
public class Bibliografia {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBibliografia")
    private int idBibliografia;

    /** Relación con Recurso: el recurso del que esta tabla guarda los datos propios. */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /** ISBN. */
    @Column(name = "isbn", nullable = false, length = 17)
    private String isbn;

    /** Autor. */
    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

    /** Ruta del archivo, relativa a la carpeta de cursos. */
    @Column(name = "rutaArchivo", nullable = false, length = 500)
    private String rutaArchivo;

    /** Editorial. */
    @Column(name = "editorial", nullable = true, length = 100)
    private String editorial;

    /** Cantidad de páginas. */
    @Column(name = "paginas", nullable = true)
    private Integer paginas;

    /** Tamaño del archivo en bytes. */
    @Column(name = "tamanioBytes", nullable = true)
    private Long tamanioBytes;

    /** Estado del reconocimiento de texto (OCR) del archivo. */
    @ManyToOne
    @JoinColumn(name = "idEstadoOCR")
    private EstadoOCR estadoOcr;

    /** Extensión del archivo. */
    @Column(name = "extension", nullable = true, length = 10)
    private String extension;

    /** Fecha de publicación. */
    @Column(name = "fechaPublicacion", nullable = true)
    private LocalDate fechaPublicacion;

    /** Crea la bibliografía con los datos básicos. */
    public Bibliografia(Recurso recurso, String isbn, String autor) {
        this.recurso = recurso;
        this.isbn = isbn;
        this.autor = autor;
    }
}
