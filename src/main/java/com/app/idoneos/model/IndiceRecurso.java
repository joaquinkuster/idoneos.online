package com.app.idoneos.model;

import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una entrada del índice de un documento, con un título y la página donde empieza. */
@Entity
@Table(name = "IndiceRecurso", uniqueConstraints = @UniqueConstraint(name = "uk_indice_recurso_orden", columnNames = {"idRecurso", "numeroOrden"}))
@Check(name = "ck_indice_recurso_titulo", constraints = "trim(titulo) <> ''")
@Check(name = "ck_indice_recurso_nivel", constraints = "nivel >= 1 and nivel <= 4")
@Check(name = "ck_indice_recurso_pagina", constraints = "pagina >= 1")
@Getter
@Setter
@NoArgsConstructor
public class IndiceRecurso {

    /** Cantidad máxima de niveles de un índice. */
    public static final int NIVEL_MAXIMO = 4;

    /** Largo máximo del título de una entrada. */
    public static final int LONGITUD_TITULO = 150;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idIndiceRecurso")
    private int idIndiceRecurso;

    /** Documento al que pertenece la entrada. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "idRecurso", nullable = false)
    private Recurso recurso;

    /** Título de la entrada. */
    @Column(name = "titulo", nullable = false, length = LONGITUD_TITULO)
    private String titulo;

    /** Página donde empieza (la primera es la 1). */
    @Column(name = "pagina", nullable = false)
    private int pagina;

    /** Nivel de anidación: 1 para un título principal, 2 y 3 para los subtítulos. */
    @Column(name = "nivel", nullable = false)
    private short nivel;

    /** Posición de la entrada en el índice (única por documento). */
    @Column(name = "numeroOrden", nullable = false)
    private int numeroOrden;

    /** Fecha de creación. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha de la última modificación. */
    @Column(name = "ultimaModificacion")
    private LocalDateTime ultimaModificacion;

    /** Crea la entrada con sus datos. */
    public IndiceRecurso(Recurso recurso, String titulo, int pagina, int nivel, int numeroOrden) {
        this.recurso = recurso;
        this.titulo = titulo;
        this.pagina = pagina;
        this.nivel = (short) nivel;
        this.numeroOrden = numeroOrden;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return titulo + " (pág. " + pagina + ")";
    }
}
