package com.app.idoneos.modelo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos propios de un recurso que es un material de lectura (presentación, resumen...).
 */
@Entity
@Table(name = "MaterialLectura")
@Getter
@Setter
@NoArgsConstructor
public class MaterialLectura {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMaterialLectura")
    private int idMaterialLectura;

    /**
     * Relación con Recurso: el recurso del que esta tabla guarda los datos propios.
     */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /**
     * Relación con CategoriaMaterialLectura.
     */
    @ManyToOne
    @JoinColumn(name = "idCategoriaMaterialLectura", nullable = false)
    private CategoriaMaterialLectura categoriaMaterialLectura;

    /**
     * Ruta del archivo, relativa a la carpeta de cursos.
     */
    @Column(name = "rutaArchivo", nullable = false, length = 500)
    private String rutaArchivo;

    /**
     * Autor.
     */
    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

    /**
     * Cantidad de páginas. Es opcional.
     */
    @Column(name = "paginas", nullable = true)
    private Integer paginas;

    /**
     * Tamaño del archivo en bytes. Es opcional.
     */
    @Column(name = "tamanioBytes", nullable = true)
    private Long tamanioBytes;

    /**
     * Extensión del archivo. Es opcional.
     */
    @Column(name = "extension", nullable = true, length = 10)
    private String extension;

    /**
     * Indica si lo generó una inteligencia artificial.
     */
    @Column(name = "generadoPorIa", nullable = false)
    private Boolean generadoPorIa = false;

    /**
     * Constructor para crear el material de lectura con los datos básicos.
     *
     * @param recurso                  el recurso.
     * @param categoriaMaterialLectura la categoría.
     * @param rutaArchivo              la ruta del archivo.
     * @param autor                    el autor.
     */
    public MaterialLectura(Recurso recurso, CategoriaMaterialLectura categoriaMaterialLectura, String rutaArchivo,
            String autor) {
        this.recurso = recurso;
        this.categoriaMaterialLectura = categoriaMaterialLectura;
        this.rutaArchivo = rutaArchivo;
        this.autor = autor;
    }
}
