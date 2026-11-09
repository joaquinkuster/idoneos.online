package com.app.idoneos.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una entrada del índice de un documento (un título con la página donde empieza). El índice es del documento, no de su uso en un tema:
 * se arma solo con los marcadores del PDF o con los títulos que se detectan en su texto, y el docente puede corregirlo.
 */
@Entity
@Table(name = "IndiceRecurso", uniqueConstraints = @UniqueConstraint(name = "uk_indice_recurso_orden", columnNames = {"idRecurso", "numeroOrden"}))
@Getter
@Setter
@NoArgsConstructor
public class IndiceRecurso {

    /** Cantidad máxima de niveles de un índice. */
    public static final int NIVEL_MAXIMO = 3;

    /** Largo máximo del título de una entrada. */
    public static final int LONGITUD_TITULO = 150;

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idIndiceRecurso")
    private int idIndiceRecurso;

    /**
     * Documento al que pertenece la entrada.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "idRecurso", nullable = false)
    private Recurso recurso;

    /**
     * Título de la entrada.
     */
    @Column(name = "titulo", nullable = false, length = LONGITUD_TITULO)
    private String titulo;

    /**
     * Página donde empieza (la primera es la 1).
     */
    @Column(name = "pagina", nullable = false)
    private int pagina;

    /**
     * Nivel de anidación: 1 para un título principal, 2 y 3 para los subtítulos.
     */
    @Column(name = "nivel", nullable = false)
    private short nivel;

    /**
     * Posición de la entrada en el índice (única por documento).
     */
    @Column(name = "numeroOrden", nullable = false)
    private int numeroOrden;

    /**
     * Fecha de creación.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha de la última modificación.
     */
    @Column(name = "ultimaModificacion")
    private LocalDateTime ultimaModificacion;

    /**
     * Constructor para crear la entrada con sus datos.
     *
     * @param recurso     el documento.
     * @param titulo      el título.
     * @param pagina      la página.
     * @param nivel       el nivel (de 1 a {@link #NIVEL_MAXIMO}).
     * @param numeroOrden la posición en el índice.
     */
    public IndiceRecurso(Recurso recurso, String titulo, int pagina, int nivel, int numeroOrden) {
        this.recurso = recurso;
        this.titulo = titulo;
        this.pagina = pagina;
        this.nivel = (short) nivel;
        this.numeroOrden = numeroOrden;
    }

    /**
     * Devuelve una representación en forma de cadena.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return titulo + " (pág. " + pagina + ")";
    }
}
