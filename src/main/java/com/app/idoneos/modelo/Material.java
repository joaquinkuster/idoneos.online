package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Material didáctico de una unidad (grabación, bibliografía, resumen, presentación) cargado por un docente.
 */
@Entity
@Table(name = "Material")
@Check(name = "ck_material_titulo", constraints = "trim(titulo) <> ''")
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
     * Relación con DocenteCurso.
     */
    @ManyToOne
    @JoinColumn(name = "idDocenteCurso", nullable = false)
    private DocenteCurso docenteCurso;

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
     * Ruta del archivo del material (relativa a la carpeta de los cursos) o enlace externo. Es opcional.
     */
    @Column(name = "rutaArchivo", nullable = true, length = 500)
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
     * @param docenteCurso relación con DocenteCurso.
     * @param tipoMaterial relación con TipoMaterial.
     * @param unidad relación con Unidad.
     * @param titulo título del material.
     */
    public Material(DocenteCurso docenteCurso, TipoMaterial tipoMaterial, Unidad unidad, String titulo) {
        this.docenteCurso = docenteCurso;
        this.tipoMaterial = tipoMaterial;
        this.unidad = unidad;
        this.titulo = titulo;
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
     * Devuelve una representación en forma de cadena del material.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return titulo;
    }

    /**
     * Indica si el material está publicado: vigente y visible para los alumnos.
     *
     * @return {@code true} si no está dado de baja ni oculto.
     */
    public boolean estaPublicado() {
        return !baja && !oculto;
    }

    /**
     * Obtiene el curso al que pertenece el material, a través de su unidad.
     *
     * @return El curso del material.
     */
    public Curso getCurso() {
        return unidad.getCurso();
    }

    /**
     * Indica si el material es un enlace externo (en lugar de un archivo guardado en el servidor).
     *
     * @return {@code true} si la ruta es un enlace http o https.
     */
    public boolean esEnlaceExterno() {
        return rutaArchivo != null && (rutaArchivo.startsWith("http://") || rutaArchivo.startsWith("https://"));
    }

    /**
     * Indica si el material tiene un archivo guardado en el servidor.
     *
     * @return {@code true} si tiene ruta y no es un enlace externo.
     */
    public boolean tieneArchivo() {
        return rutaArchivo != null && !esEnlaceExterno();
    }

    /**
     * Obtiene la extensión del archivo del material.
     *
     * @return La extensión en minúscula y sin punto, o una cadena vacía si no tiene archivo en el servidor.
     */
    public String getExtension() {
        if (!tieneArchivo() || !rutaArchivo.contains(".")) {
            return "";
        }
        return rutaArchivo.substring(rutaArchivo.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Indica cómo se muestra el material en la plataforma: "enlace" (se abre la página externa), "video" (reproductor)
     * o "pdf" (visor de páginas). Si todavía no tiene archivo o enlace, o su formato ya no se admite, es "vacio".
     *
     * @return El formato de vista del material.
     */
    public String getFormatoDeVista() {
        if (esEnlaceExterno()) {
            return "enlace";
        }
        if (!tieneArchivo()) {
            return "vacio";
        }
        return switch (getExtension()) {
            case "mp4", "webm" -> "video";
            case "pdf" -> "pdf";
            default -> "vacio";
        };
    }
}
