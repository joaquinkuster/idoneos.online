package com.app.idoneos.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Check;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un recurso didáctico de un docente. Puede ser una grabación, un material de lectura o una bibliografía. */
@Entity
@Table(name = "Recurso")
@Check(name = "ck_recurso_titulo", constraints = "trim(titulo) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Recurso {

    /** Tipo de recurso: diapositivas de una clase. */
    public static final String PRESENTACION = "Presentación";

    /** Tipo de recurso: resúmenes, transcripciones de clases o compilaciones teóricas del equipo docente. */
    public static final String APUNTE_DE_CATEDRA = "Apunte de Cátedra";

    /** Tipo de recurso: guía de lectura, de ejercicios o integrador que orienta el estudio de un tema. */
    public static final String GUIA_DE_ESTUDIO = "Guía de Estudio";

    /** Tipo de recurso: libro, manual o publicación formal con identificador. */
    public static final String MATERIAL_BIBLIOGRAFICO = "Material bibliográfico";

    /** Tipo de recurso: grabación de una clase. */
    public static final String GRABACION = "Grabación";

    /** Tipo de recurso: enlace externo. */
    public static final String ENLACE = "Enlace";

    /** Tipo de recurso: video con clon de inteligencia artificial. */
    public static final String VIDEO_CLON = "Video con clon";

    /** Identificador único del recurso. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRecurso")
    private int idRecurso;

    /** Relación con Docente: el docente dueño del recurso. */
    @ManyToOne
    @JoinColumn(name = "idDocente", nullable = false)
    private Docente docente;

    /** Título. */
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    /** Descripción. */
    @Column(name = "descripcion", nullable = true, length = 500)
    private String descripcion;

    /** Indica si el recurso es privado (solo lo puede usar su dueño) o puede usarlo cualquier docente. */
    @Column(name = "privado", nullable = false)
    private Boolean privado = true;

    /** Fecha y hora en que el docente aceptó los términos y condiciones del recurso (derechos sobre su contenido). */
    @Column(name = "fechaAceptacionTyc", nullable = false)
    private LocalDateTime fechaAceptacionTyc = LocalDateTime.now();

    /** Fecha y hora de creación. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Baja lógica. */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Datos del recurso si es un enlace externo. */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private Enlace enlace;

    /** Datos del recurso si es una grabación. */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private Grabacion grabacion;

    /** Documento del recurso, si tiene un archivo con texto (material bibliográfico o didáctico). */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private Documento documento;

    /** Datos del recurso si es un video con clon. */
    @OneToOne(mappedBy = "recurso", cascade = CascadeType.ALL)
    private VideoClon videoClon;

    /** Conjunto de temas que usan el recurso. */
    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL)
    private Set<TemaRecurso> temasRecurso = new HashSet<>();

    /** Crea el recurso con los datos básicos. */
    public Recurso(Docente docente, String titulo) {
        this.docente = docente;
        this.titulo = titulo;
    }

    /** Da de baja el recurso (baja lógica). */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el recurso está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena del recurso. */
    @Override
    public String toString() {
        return titulo;
    }

    /** Obtiene el tipo del recurso según la tabla que guarda sus datos. */
    public String getTipo() {
        if (getMaterialDidactico() != null) {
            return getMaterialDidactico().getCategoriaMaterialDidactico().getNombre();
        }
        if (getMaterialBibliografico() != null) {
            return MATERIAL_BIBLIOGRAFICO;
        }
        if (grabacion != null) {
            return GRABACION;
        }
        if (videoClon != null) {
            return VIDEO_CLON;
        }
        return ENLACE;
    }

    /** Obtiene la ruta del archivo del recurso, o su dirección si es un enlace externo. */
    public String getRutaArchivo() {
        if (getMaterialDidactico() != null) {
            return getMaterialDidactico().getRutaArchivo();
        }
        if (getMaterialBibliografico() != null) {
            return getMaterialBibliografico().getRutaArchivo();
        }
        if (grabacion != null) {
            return grabacion.getRutaArchivo();
        }
        if (videoClon != null) {
            return videoClon.getRutaArchivo();
        }
        return enlace == null ? null : enlace.getUrl();
    }

    /** Obtiene el estado del reconocimiento de texto (OCR) del archivo del recurso (de un material de lectura o de una bibliografía). */
    public EstadoOCR getEstadoOcr() {
        if (getMaterialDidactico() != null) {
            return getMaterialDidactico().getEstadoOcr();
        }
        return getMaterialBibliografico() == null ? null : getMaterialBibliografico().getEstadoOcr();
    }

    /** Cambia el estado del OCR del archivo del recurso (solo lo tienen los materiales de lectura y las bibliografías). */
    public void setEstadoOcr(EstadoOCR estado) {
        if (getMaterialDidactico() != null) {
            getMaterialDidactico().setEstadoOcr(estado);
        } else if (getMaterialBibliografico() != null) {
            getMaterialBibliografico().setEstadoOcr(estado);
        }
    }

    /** Actualiza el tamaño del archivo del recurso. */
    public void actualizarTamanioBytes(long bytes) {
        if (getMaterialDidactico() != null) {
            getMaterialDidactico().setTamanioBytes(bytes);
        } else if (getMaterialBibliografico() != null) {
            getMaterialBibliografico().setTamanioBytes(bytes);
        }
    }

    /** Obtiene el autor del recurso (de un material de lectura o de una bibliografía). */
    public String getAutor() {
        if (getMaterialDidactico() != null) {
            return getMaterialDidactico().getAutor();
        }
        return getMaterialBibliografico() == null ? null : getMaterialBibliografico().getAutor();
    }

    /** Indica si el recurso lo generó una inteligencia artificial. */
    public boolean getGeneradoPorIa() {
        return videoClon != null || (getMaterialDidactico() != null && getMaterialDidactico().getGeneradoPorIa());
    }

    /** Obtiene el estado de la generación con inteligencia artificial, o nulo si lo cargó el docente a mano. */
    public String getEstadoGeneracion() {
        if (getMaterialDidactico() != null && getMaterialDidactico().getEstadoGeneracionIa() != null) {
            return getMaterialDidactico().getEstadoGeneracionIa().getNombre();
        }
        return videoClon != null ? videoClon.getEstadoGeneracionIa().getNombre() : null;
    }

    /** Obtiene el motivo por el que falló la generación con inteligencia artificial, si falló. */
    public String getDetalleErrorGeneracion() {
        if (getMaterialDidactico() != null) {
            return getMaterialDidactico().getDetalleError();
        }
        return videoClon == null ? null : videoClon.getDetalleError();
    }

    /** Indica si ya se puede usar: lo cargó el docente a mano o la inteligencia artificial terminó de generarlo. */
    public boolean getDisponible() {
        return getMaterialDidactico() == null || getMaterialDidactico().estaDisponible();
    }

    /** Indica si el recurso es un enlace externo. */
    public boolean esEnlaceExterno() {
        return enlace != null;
    }

    /** Indica si el recurso tiene un archivo guardado en la plataforma. */
    public boolean tieneArchivo() {
        String ruta = getRutaArchivo();
        return ruta != null && enlace == null;
    }

    /** Obtiene la extensión del archivo del recurso. */
    public String getExtension() {
        String ruta = getRutaArchivo();
        if (!tieneArchivo() || !ruta.contains(".")) {
            return "";
        }
        return ruta.substring(ruta.lastIndexOf('.') + 1).toLowerCase();
    }

    /** Obtiene la modalidad del curso a la que corresponde el recurso. */
    public String getModalidadAsociada() {
        if (grabacion != null) {
            return grabacion.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja()) ? Modalidad.EN_VIVO
                    : Modalidad.GRABADA;
        }
        return videoClon != null ? Modalidad.CLON_IA : null;
    }

    /** Devuelve los datos del material didáctico, o nulo si el recurso no lo es. */
    public MaterialDidactico getMaterialDidactico() {
        return documento == null ? null : documento.getMaterialDidactico();
    }

    /** Asocia los datos del material didáctico recién guardado, con su documento. */
    public void setMaterialDidactico(MaterialDidactico materialDidactico) {
        this.documento = materialDidactico == null ? null : materialDidactico.getDocumento();
    }

    /** Devuelve los datos del material bibliográfico, o nulo si el recurso no lo es. */
    public MaterialBibliografico getMaterialBibliografico() {
        return documento == null ? null : documento.getMaterialBibliografico();
    }

    /** Asocia los datos del material bibliográfico recién guardado, con su documento. */
    public void setMaterialBibliografico(MaterialBibliografico materialBibliografico) {
        this.documento = materialBibliografico == null ? null : materialBibliografico.getDocumento();
    }
}
