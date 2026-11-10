package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un conjunto de preguntas de un tema, del que se arman las autoevaluaciones. */
@Entity
@Table(name = "Pool", uniqueConstraints = { @UniqueConstraint(columnNames = {"idTema", "nombre"}) })
@Check(name = "ck_pool_nombre", constraints = "trim(nombre) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Pool {

    /** Identificador único del pool. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPool")
    private int idPool;

    /** Tema de este registro. */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /** Nombre del pool. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Preguntas que referencian a este registro. */
    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL)
    private Set<Pregunta> preguntas = new HashSet<>();

    /** Autoevaluacion pools que referencian a este registro. */
    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL)
    private Set<AutoevaluacionPool> autoevaluacionPools = new HashSet<>();

    /** Crea el pool con los datos básicos. */
    public Pool(Tema tema, String nombre) {
        this.tema = tema;
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

    /** Obtiene las preguntas vigentes del pool. */
    public List<Pregunta> getPreguntasActivas() {
        return preguntas.stream().filter(pregunta -> !pregunta.getBaja())
                .sorted(Comparator.comparingInt(Pregunta::getIdPregunta)).toList();
    }

    /** Cuenta las preguntas vigentes del pool. */
    public int getCantidadPreguntas() {
        return getPreguntasActivas().size();
    }

    /** Obtiene las autoevaluaciones vigentes que usan el pool. */
    public List<Autoevaluacion> getAutoevaluacionesActivas() {
        return autoevaluacionPools.stream().map(AutoevaluacionPool::getAutoevaluacion)
                .filter(autoevaluacion -> !autoevaluacion.getBaja()).distinct()
                .sorted(Comparator.comparingInt(Autoevaluacion::getIdAutoevaluacion)).toList();
    }

    /** Indica si alguna autoevaluación que usa el pool tiene intentos vigentes. */
    public boolean tieneIntentosActivos() {
        return getAutoevaluacionesActivas().stream().anyMatch(Autoevaluacion::tieneIntentosActivos);
    }

    /** Devuelve una representación en forma de cadena del pool. */
    @Override
    public String toString() {
        return nombre;
    }
}
