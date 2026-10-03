package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Programa de un curso, con sus objetivos, carga horaria y bibliografía.
 */
@Entity
@Table(name = "Programa")
@Getter
@Setter
@NoArgsConstructor
public class Programa {

    /**
     * Identificador único del programa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPrograma")
    private int idPrograma;

    /**
     * Relación con Curso.
     */
    @ManyToOne
    @JoinColumn(name = "idCurso", nullable = false)
    private Curso curso;

    /**
     * Nombre del programa.
     */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /**
     * Descripción breve del programa.
     */
    @Column(name = "descripcion", nullable = true, length = 150)
    private String descripcion;

    /**
     * Objetivos del programa.
     */
    @Column(name = "objetivos", nullable = false, columnDefinition = "TEXT")
    private String objetivos;

    /**
     * Carga horaria total del programa, en horas. Es opcional.
     */
    @Column(name = "cargaHorariaTotal", nullable = true)
    private Integer cargaHorariaTotal;

    /**
     * Bibliografía del programa.
     */
    @Column(name = "bibliografia", nullable = false, columnDefinition = "TEXT")
    private String bibliografia;

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
     * Conjunto de cohortes asociados.
     */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private Set<Cohorte> cohortes = new HashSet<>();

    /**
     * Conjunto de unidadesCronograma asociados.
     */
    @OneToMany(mappedBy = "programa", cascade = CascadeType.ALL)
    private Set<UnidadCronograma> unidadesCronograma = new HashSet<>();

    /**
     * Conjunto de participacionesDocente asociados.
     */
    @OneToMany(mappedBy = "programaPorDefecto", cascade = CascadeType.ALL)
    private Set<ParticipacionDocente> participacionesDocente = new HashSet<>();

    /**
     * Constructor para crear el programa con los datos básicos.
     *
     * @param curso relación con Curso.
     * @param nombre nombre del programa.
     * @param objetivos objetivos del programa.
     * @param bibliografia bibliografía del programa.
     */
    public Programa(Curso curso, String nombre, String objetivos, String bibliografia) {
        this.curso = curso;
        this.nombre = nombre;
        this.objetivos = objetivos;
        this.bibliografia = bibliografia;
    }

    /**
     * Devuelve una representación en forma de cadena del programa.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
