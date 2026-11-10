package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una categoría temática que agrupa cursos, por ejemplo finanzas. */
@Entity
@Table(name = "Categoria")
@Check(name = "ck_categoria_nombre", constraints = "trim(nombre) <> ''")
@Check(name = "ck_categoria_descripcion", constraints = "descripcion is null or trim(descripcion) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    /** Identificador único de la categoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCategoria")
    private int idCategoria;

    /** Nombre de la categoría. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Descripción de la categoría. */
    @Column(name = "descripcion", nullable = true, length = 150)
    private String descripcion;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Cursos que referencian a este registro. */
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)
    private Set<Curso> cursos = new HashSet<>();

    /** Crea la categoría con los datos básicos. */
    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena de la categoría. */
    @Override
    public String toString() {
        return nombre;
    }
}
