package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Oferta educativa principal de la plataforma. Se dicta en una o más modalidades y se organiza en programas y unidades.
 */
@Entity
@Table(name = "Curso")
@Getter
@Setter
@NoArgsConstructor
public class Curso {

    /**
     * Identificador único del curso.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCurso")
    private int idCurso;

    /**
     * Relación con Nivel.
     */
    @ManyToOne
    @JoinColumn(name = "idNivel", nullable = false)
    private Nivel nivel;

    /**
     * Relación con Categoria.
     */
    @ManyToOne
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria categoria;

    /**
     * Nombre del curso.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Descripción breve del curso.
     */
    @Column(name = "descripcion", nullable = true, length = 150)
    private String descripcion;

    /**
     * Precio del curso.
     */
    @Column(name = "precio", nullable = false)
    private float precio;

    /**
     * Ruta de la imagen de portada del curso. Es opcional.
     */
    @Column(name = "imagen", nullable = true, length = 150)
    private String imagen;

    /**
     * Indica si el curso emite certificado de aprobación.
     */
    @Column(name = "emiteCertificado", nullable = false)
    private Boolean emiteCertificado = false;

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
     * Conjunto de reportes asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Reporte> reportes = new HashSet<>();

    /**
     * Conjunto de unidades asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Unidad> unidades = new HashSet<>();

    /**
     * Conjunto de programas asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<Programa> programas = new HashSet<>();

    /**
     * Conjunto de participacionesDocente asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<ParticipacionDocente> participacionesDocente = new HashSet<>();

    /**
     * Conjunto de cursoModalidades asociados.
     */
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private Set<CursoModalidad> cursoModalidades = new HashSet<>();

    /**
     * Constructor para crear el curso con los datos básicos.
     *
     * @param nivel relación con Nivel.
     * @param categoria relación con Categoria.
     * @param nombre nombre del curso.
     * @param precio precio del curso.
     */
    public Curso(Nivel nivel, Categoria categoria, String nombre, float precio) {
        this.nivel = nivel;
        this.categoria = categoria;
        this.nombre = nombre;
        this.precio = precio;
    }

    /**
     * Devuelve una representación en forma de cadena del curso.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
