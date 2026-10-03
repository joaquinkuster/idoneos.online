package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Conjunto de preguntas de una unidad, del que se arman las autoevaluaciones.
 */
@Entity
@Table(name = "Pool", uniqueConstraints = { @UniqueConstraint(columnNames = {"idUnidad", "nombre"}) })
@Getter
@Setter
@NoArgsConstructor
public class Pool {

    /**
     * Identificador único del pool.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPool")
    private int idPool;

    /**
     * Relación con Unidad.
     */
    @ManyToOne
    @JoinColumn(name = "idUnidad", nullable = false)
    private Unidad unidad;

    /**
     * Nombre del pool.
     */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

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
     * Conjunto de preguntas asociados.
     */
    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL)
    private Set<Pregunta> preguntas = new HashSet<>();

    /**
     * Conjunto de autoevaluacionPools asociados.
     */
    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL)
    private Set<AutoevaluacionPool> autoevaluacionPools = new HashSet<>();

    /**
     * Constructor para crear el pool con los datos básicos.
     *
     * @param unidad relación con Unidad.
     * @param nombre nombre del pool.
     */
    public Pool(Unidad unidad, String nombre) {
        this.unidad = unidad;
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
     * Devuelve una representación en forma de cadena del pool.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
