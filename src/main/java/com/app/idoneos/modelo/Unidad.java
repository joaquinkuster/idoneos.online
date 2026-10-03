package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unidad temática de un curso, con su contenido.
 */
@Entity
@Table(name = "Unidad", uniqueConstraints = { @UniqueConstraint(columnNames = {"idCurso", "titulo"}) })
@Getter
@Setter
@NoArgsConstructor
public class Unidad {

    /**
     * Identificador único de la unidad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUnidad")
    private int idUnidad;

    /**
     * Relación con Curso.
     */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /**
     * Título de la unidad.
     */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /**
     * Descripción breve de la unidad.
     */
    @Column(name = "descripcion", nullable = true, length = 150)
    private String descripcion;

    /**
     * Contenido de la unidad.
     */
    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

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
     * Conjunto de progresos asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<Progreso> progresos = new HashSet<>();

    /**
     * Conjunto de materiales asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<Material> materiales = new HashSet<>();

    /**
     * Conjunto de terminosGlosario asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<TerminoGlosario> terminosGlosario = new HashSet<>();

    /**
     * Conjunto de consultasForo asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<ConsultaForo> consultasForo = new HashSet<>();

    /**
     * Conjunto de unidadesCronograma asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<UnidadCronograma> unidadesCronograma = new HashSet<>();

    /**
     * Conjunto de pooles asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<Pool> pooles = new HashSet<>();

    /**
     * Conjunto de autoevaluaciones asociados.
     */
    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private Set<Autoevaluacion> autoevaluaciones = new HashSet<>();

    /**
     * Constructor para crear la unidad con los datos básicos.
     *
     * @param curso relación con Curso.
     * @param titulo título de la unidad.
     * @param contenido contenido de la unidad.
     */
    public Unidad(Curso curso, String titulo, String contenido) {
        this.curso = curso;
        this.titulo = titulo;
        this.contenido = contenido;
    }

    /**
     * Devuelve una representación en forma de cadena de la unidad.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return titulo;
    }
}
