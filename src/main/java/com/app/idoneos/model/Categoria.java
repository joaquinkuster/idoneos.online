package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Categoría temática de los cursos (Ej: Finanzas, Economía, Mercado de Capitales, Impuestos).
 */
@Entity
@Table(name = "categorias")
@Getter @Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = true, length = 500)
    private String descripcion;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * DDL: fecha_creacion timestamp — cambiado de ausente a LocalDateTime.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * DDL: ultima_modificacion timestamp — campo agregado según modelo conceptual.
     */
    @Column(name = "ultima_modificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    @OneToMany(mappedBy = "categoria")
    private Set<Curso> cursos = new HashSet<>();

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean esInactivo() {
        return baja != null && baja;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
