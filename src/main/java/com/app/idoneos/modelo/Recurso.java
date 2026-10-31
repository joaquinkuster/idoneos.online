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
 * Recurso didáctico de un docente: una grabación, un material de lectura, una bibliografía, un enlace o un video con clon.
 * Los datos propios de cada clase están en su tabla (relación uno a uno con {@code idRecurso}). Un recurso se usa en los
 * temas que lo incluyen ({@link TemaRecurso}).
 */
@Entity
@Table(name = "Recurso")
@Check(name = "ck_recurso_titulo", constraints = "trim(titulo) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Recurso {

    /** Tipo de recurso: diapositivas de una clase. */
    public static final String PRESENTACION = "Presentación";

    /** Tipo de recurso: resumen de un tema. */
    public static final String RESUMEN = "Resumen";

    /** Tipo de recurso: bibliografía. */
    public static final String BIBLIOGRAFIA = "Bibliografía";

    /** Tipo de recurso: grabación de una clase. */
    public static final String GRABACION = "Grabación";

    /** Tipo de recurso: enlace externo. */
    public static final String ENLACE = "Enlace";

    /** Tipo de recurso: video con clon de inteligencia artificial. */
    public static final String VIDEO_CLON = "Video clon";

    /**
     * Identificador único del recurso.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRecurso")
    private int idRecurso;

    /**
     * Relación con Docente: el docente dueño del recurso.
     */
    @ManyToOne
    @JoinColumn(name = "idDocente", nullable = false)
    private Docente docente;

    /**
     * Título.
     */
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    /**
     * Descripción. Es opcional.
     */
    @Column(name = "descripcion", nullable = true, length = 500)
    private String descripcion;

    /**
     * Indica si el recurso es privado (solo lo puede usar su dueño) o puede usarlo cualquier docente.
     */
    @Column(name = "privado", nullable = false)
    private Boolean privado = true;

    /**
     * Fecha y hora en que el docente aceptó los términos y condiciones del recurso (derechos sobre su contenido).
     */
    @Column(name = "fechaAceptacionTyc", nullable = false)
    private LocalDateTime fechaAceptacionTyc = LocalDateTime.now();

    /**
     * Fecha y hora de creación.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación. Es opcional.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Baja lógica.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Datos del recurso si es un enlace externo.
     */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private Enlace enlace;

    /**
     * Datos del recurso si es una grabación.
     */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private Grabacion grabacion;

    /**
     * Datos del recurso si es un material de lectura (presentación, resumen...).
     */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private MaterialLectura materialLectura;

    /**
     * Datos del recurso si es una bibliografía.
     */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private Bibliografia bibliografia;

    /**
     * Datos del recurso si es un video con clon.
     */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private VideoClon videoClon;

    /**
     * Conjunto de temas que usan el recurso.
     */
    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL)
    private Set<TemaRecurso> temasRecurso = new HashSet<>();

    /**
     * Constructor para crear el recurso con los datos básicos.
     *
     * @param docente el docente dueño.
     * @param titulo  título.
     */
    public Recurso(Docente docente, String titulo) {
        this.docente = docente;
        this.titulo = titulo;
    }

    /**
     * Da de baja el recurso (baja lógica).
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el recurso está dado de baja.
     *
     * @return true si está dado de baja.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena del recurso.
     *
     * @return El título.
     */
    @Override
    public String toString() {
        return titulo;
    }

    /**
     * Obtiene el tipo del recurso según la tabla que guarda sus datos.
     *
     * @return "Presentación", "Resumen", "Bibliografía", "Grabación", "Enlace" o "Video clon".
     */
    public String getTipo() {
        if (materialLectura != null) {
            return materialLectura.getCategoriaMaterialLectura().getNombre();
        }
        if (bibliografia != null) {
            return BIBLIOGRAFIA;
        }
        if (grabacion != null) {
            return GRABACION;
        }
        if (videoClon != null) {
            return VIDEO_CLON;
        }
        return ENLACE;
    }

    /**
     * Obtiene la ruta del archivo del recurso, o su dirección si es un enlace externo.
     *
     * @return La ruta o la dirección, o {@code null} si no tiene ninguna.
     */
    public String getRutaArchivo() {
        if (materialLectura != null) {
            return materialLectura.getRutaArchivo();
        }
        if (bibliografia != null) {
            return bibliografia.getRutaArchivo();
        }
        if (grabacion != null) {
            return grabacion.getRutaArchivo();
        }
        if (videoClon != null) {
            return videoClon.getRutaArchivo();
        }
        return enlace == null ? null : enlace.getUrl();
    }

    /**
     * Obtiene el autor del recurso (de un material de lectura o de una bibliografía).
     *
     * @return El autor, o {@code null} si el recurso no lo tiene.
     */
    public String getAutor() {
        if (materialLectura != null) {
            return materialLectura.getAutor();
        }
        return bibliografia == null ? null : bibliografia.getAutor();
    }

    /**
     * Indica si el recurso lo generó una inteligencia artificial.
     *
     * @return true si es un material de lectura generado por IA o un video con clon.
     */
    public boolean getGeneradoPorIa() {
        return videoClon != null || (materialLectura != null && materialLectura.getGeneradoPorIa());
    }

    /**
     * Indica si el recurso es un enlace externo.
     *
     * @return true si su contenido es una dirección web.
     */
    public boolean esEnlaceExterno() {
        return enlace != null;
    }

    /**
     * Indica si el recurso tiene un archivo guardado en la plataforma.
     *
     * @return true si tiene archivo.
     */
    public boolean tieneArchivo() {
        String ruta = getRutaArchivo();
        return ruta != null && enlace == null;
    }

    /**
     * Obtiene la extensión del archivo del recurso.
     *
     * @return La extensión en minúsculas, o una cadena vacía si no tiene archivo.
     */
    public String getExtension() {
        String ruta = getRutaArchivo();
        if (!tieneArchivo() || !ruta.contains(".")) {
            return "";
        }
        return ruta.substring(ruta.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Obtiene la modalidad del curso a la que corresponde el recurso: En vivo si es la grabación de una clase en vivo,
     * Grabada si es una grabación suelta, Clon de IA si es un video con clon.
     *
     * @return La modalidad, o {@code null} si el recurso no corresponde a ninguna (lectura, bibliografía, enlace).
     */
    public String getModalidadAsociada() {
        if (grabacion != null) {
            return grabacion.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja()) ? Modalidad.EN_VIVO
                    : Modalidad.GRABADA;
        }
        return videoClon != null ? Modalidad.CLON_IA : null;
    }
}
