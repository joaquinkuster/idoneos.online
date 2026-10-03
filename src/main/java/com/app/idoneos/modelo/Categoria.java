package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Categoría temática que agrupa cursos (por ejemplo, finanzas o mercado de capitales).
 */
@Entity
@Table(name = "Categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    /**
     * Identificador único de la categoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCategoria")
    private int idCategoria;

    /**
     * Nombre de la categoría.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Descripción de la categoría.
     */
    @Column(name = "descripcion", nullable = true, length = 150)
    private String descripcion;

    /**
     * Fecha y hora de creación del registro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Indica si el registro fue dado de baja (baja lógica). El valor predeterminado es "false", indicando que está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Conjunto de cursos asociados.
     */
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)
    private Set<Curso> cursos = new HashSet<>();

    /**
     * Constructor para crear la categoría con los datos básicos.
     *
     * @param nombre nombre de la categoría.
     */
    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true.
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja (baja = true), false si está vigente.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena de la categoría.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
