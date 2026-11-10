package com.app.idoneos.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa los datos propios de un recurso que es un material de lectura. */
@Entity
@Table(name = "MaterialLectura")
@Check(name = "ck_material_lectura_paginas", constraints = "paginas is null or paginas > 0")
@Check(name = "ck_material_lectura_tamanio", constraints = "tamanio_bytes is null or tamanio_bytes >= 0")
@Getter
@Setter
@NoArgsConstructor
public class MaterialLectura {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMaterialLectura")
    private int idMaterialLectura;

    /** Relación con Recurso: el recurso del que esta tabla guarda los datos propios. */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /** Categoria material lectura de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCategoriaMaterialLectura", nullable = false)
    private CategoriaMaterialLectura categoriaMaterialLectura;

    /** Ruta del archivo, relativa a la carpeta de cursos. */
    @Column(name = "rutaArchivo", nullable = false, length = 500)
    private String rutaArchivo;

    /** Autor. */
    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

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

    /** Indica si lo generó una inteligencia artificial. */
    @Column(name = "generadoPorIa", nullable = false)
    private Boolean generadoPorIa = false;

    /** Crea el material de lectura con los datos básicos. */
    public MaterialLectura(Recurso recurso, CategoriaMaterialLectura categoriaMaterialLectura, String rutaArchivo,
            String autor) {
        this.recurso = recurso;
        this.categoriaMaterialLectura = categoriaMaterialLectura;
        this.rutaArchivo = rutaArchivo;
        this.autor = autor;
    }
}
