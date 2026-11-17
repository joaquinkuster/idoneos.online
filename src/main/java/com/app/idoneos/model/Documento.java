package com.app.idoneos.model;

import org.hibernate.annotations.Check;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el archivo con texto de un recurso, con los datos que comparten los materiales bibliográficos y los didácticos. */
@Entity
@Table(name = "Documento")
@Check(name = "ck_documento_paginas", constraints = "paginas is null or paginas > 0")
@Check(name = "ck_documento_tamanio", constraints = "tamanio_bytes is null or tamanio_bytes >= 0")
@Getter
@Setter
@NoArgsConstructor
public class Documento {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDocumento")
    private int idDocumento;

    /** Relación con Recurso: el recurso al que pertenece el archivo. */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /** Estado del reconocimiento de texto (OCR) del archivo. */
    @ManyToOne
    @JoinColumn(name = "idEstadoOCR")
    private EstadoOCR estadoOcr;

    /** Autor. */
    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

    /** Cantidad de páginas. */
    @Column(name = "paginas", nullable = true)
    private Integer paginas;

    /** Tamaño del archivo en bytes. */
    @Column(name = "tamanioBytes", nullable = true)
    private Long tamanioBytes;

    /** Extensión del archivo. */
    @Column(name = "extension", nullable = true, length = 10)
    private String extension;

    /** Ruta del archivo, relativa a la carpeta de cursos. Es nula mientras se genera un material con inteligencia artificial. */
    @Column(name = "rutaArchivo", nullable = true, length = 500)
    private String rutaArchivo;

    /** Datos propios del material bibliográfico, si el documento es uno. */
    @OneToOne(mappedBy = "documento")
    private MaterialBibliografico materialBibliografico;

    /** Datos propios del material didáctico, si el documento es uno. */
    @OneToOne(mappedBy = "documento")
    private MaterialDidactico materialDidactico;

    /** Crea el documento de un recurso con su autor. */
    public Documento(Recurso recurso, String autor) {
        this.recurso = recurso;
        this.autor = autor;
    }

    /** Devuelve una representación en forma de cadena del documento. */
    @Override
    public String toString() {
        return "Documento #" + idDocumento;
    }
}
