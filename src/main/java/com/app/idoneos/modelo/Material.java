package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Material didáctico de una unidad (grabación, bibliografía, resumen, presentación) cargado por un docente.
 */
@Entity
@Table(name = "Material")
@Getter
@Setter
@NoArgsConstructor
public class Material {

    /**
     * Identificador único del material.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMaterial")
    private int idMaterial;

    /**
     * Relación con ParticipacionDocente.
     */
    @ManyToOne
    @JoinColumn(name = "idParticipacionDocente", nullable = false)
    private ParticipacionDocente participacionDocente;

    /**
     * Relación con TipoMaterial.
     */
    @ManyToOne
    @JoinColumn(name = "idTipoMaterial", nullable = false)
    private TipoMaterial tipoMaterial;

    /**
     * Relación con Unidad.
     */
    @ManyToOne
    @JoinColumn(name = "idUnidad", nullable = false)
    private Unidad unidad;

    /**
     * Título del material.
     */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /**
     * Ruta del archivo del material. Es opcional.
     */
    @Column(name = "rutaArchivo", nullable = true, length = 150)
    private String rutaArchivo;

    /**
     * Contenido textual del material (por ejemplo, un resumen). Es opcional.
     */
    @Column(name = "contenido", nullable = true, length = 500)
    private String contenido;

    /**
     * Autor del material. Es opcional.
     */
    @Column(name = "autor", nullable = true, length = 50)
    private String autor;

    /**
     * Indica si el material fue generado con inteligencia artificial.
     */
    @Column(name = "generadoPorIa", nullable = false)
    private Boolean generadoPorIa = false;

    /**
     * Indica si el elemento está oculto para los alumnos.
     */
    @Column(name = "oculto", nullable = false)
    private Boolean oculto = false;

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
     * Conjunto de clasesEnVivo asociados.
     */
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    private Set<ClaseEnVivo> clasesEnVivo = new HashSet<>();

    /**
     * Conjunto de clasesClon asociados.
     */
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    private Set<ClaseClon> clasesClon = new HashSet<>();

    /**
     * Constructor para crear el material con los datos básicos.
     *
     * @param participacionDocente relación con ParticipacionDocente.
     * @param tipoMaterial relación con TipoMaterial.
     * @param unidad relación con Unidad.
     * @param titulo título del material.
     */
    public Material(ParticipacionDocente participacionDocente, TipoMaterial tipoMaterial, Unidad unidad, String titulo) {
        this.participacionDocente = participacionDocente;
        this.tipoMaterial = tipoMaterial;
        this.unidad = unidad;
        this.titulo = titulo;
    }

    /**
     * Devuelve una representación en forma de cadena del material.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return titulo;
    }
}
