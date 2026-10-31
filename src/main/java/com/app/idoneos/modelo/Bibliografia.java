package com.app.idoneos.modelo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos propios de un recurso que es una bibliografía.
 */
@Entity
@Table(name = "Bibliografia")
@Getter
@Setter
@NoArgsConstructor
public class Bibliografia {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBibliografia")
    private int idBibliografia;

    /**
     * Relación con Recurso: el recurso del que esta tabla guarda los datos propios.
     */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /**
     * ISBN.
     */
    @Column(name = "isbn", nullable = false, length = 17)
    private String isbn;

    /**
     * Autor.
     */
    @Column(name = "autor", nullable = false, length = 100)
    private String autor;

    /**
     * Ruta del archivo, relativa a la carpeta de cursos. Es opcional.
     */
    @Column(name = "rutaArchivo", nullable = true, length = 500)
    private String rutaArchivo;

    /**
     * Editorial. Es opcional.
     */
    @Column(name = "editorial", nullable = true, length = 100)
    private String editorial;

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
     * Fecha de publicación. Es opcional.
     */
    @Column(name = "fechaPublicacion", nullable = true)
    private LocalDate fechaPublicacion;

    /**
     * Constructor para crear la bibliografía con los datos básicos.
     *
     * @param recurso el recurso.
     * @param isbn    el ISBN.
     * @param autor   el autor.
     */
    public Bibliografia(Recurso recurso, String isbn, String autor) {
        this.recurso = recurso;
        this.isbn = isbn;
        this.autor = autor;
    }
}
