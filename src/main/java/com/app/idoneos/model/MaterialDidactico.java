package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa los datos propios de un documento que es un material didáctico. */
@Entity
@Table(name = "MaterialDidactico")
@Getter
@Setter
@NoArgsConstructor
public class MaterialDidactico {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMaterialDidactico")
    private int idMaterialDidactico;

    /** Relación con Documento: el documento del que esta tabla guarda los datos propios. */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idDocumento", nullable = false, unique = true)
    private Documento documento;

    /** Categoría de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCategoriaMaterialDidactico", nullable = false)
    private CategoriaMaterialDidactico categoriaMaterialDidactico;

    /** Estado de la generación con inteligencia artificial. Es nulo si el docente lo cargó a mano. */
    @ManyToOne
    @JoinColumn(name = "idEstadoGeneracionIA")
    private EstadoGeneracionIA estadoGeneracionIa;

    /** Detalle del error si la generación falló. */
    @Column(name = "detalleError", nullable = true, length = 500)
    private String detalleError;

    /** Crea el material didáctico con los datos básicos y su documento. */
    public MaterialDidactico(Recurso recurso, CategoriaMaterialDidactico categoriaMaterialDidactico, String rutaArchivo,
            String autor) {
        this.documento = new Documento(recurso, autor);
        this.documento.setMaterialDidactico(this);
        this.documento.setRutaArchivo(rutaArchivo);
        this.categoriaMaterialDidactico = categoriaMaterialDidactico;
    }

    /** Indica si lo generó una inteligencia artificial, que se deduce de tener un estado de generación. */
    public boolean getGeneradoPorIa() {
        return estadoGeneracionIa != null;
    }

    /** Indica si ya se puede usar: lo cargó el docente a mano o la inteligencia artificial terminó de generarlo. */
    public boolean estaDisponible() {
        return estadoGeneracionIa == null || EstadoGeneracionIA.LISTO.equals(estadoGeneracionIa.getNombre());
    }

    /** Devuelve el recurso al que pertenece el documento. */
    public Recurso getRecurso() {
        return documento.getRecurso();
    }

    /** Devuelve el autor del documento. */
    public String getAutor() {
        return documento.getAutor();
    }

    /** Cambia el autor del documento. */
    public void setAutor(String autor) {
        documento.setAutor(autor);
    }

    /** Devuelve la ruta del archivo del documento. */
    public String getRutaArchivo() {
        return documento.getRutaArchivo();
    }

    /** Cambia la ruta del archivo del documento. */
    public void setRutaArchivo(String rutaArchivo) {
        documento.setRutaArchivo(rutaArchivo);
    }

    /** Devuelve la cantidad de páginas del documento. */
    public Integer getPaginas() {
        return documento.getPaginas();
    }

    /** Cambia la cantidad de páginas del documento. */
    public void setPaginas(Integer paginas) {
        documento.setPaginas(paginas);
    }

    /** Devuelve el tamaño del archivo del documento en bytes. */
    public Long getTamanioBytes() {
        return documento.getTamanioBytes();
    }

    /** Cambia el tamaño del archivo del documento en bytes. */
    public void setTamanioBytes(Long tamanioBytes) {
        documento.setTamanioBytes(tamanioBytes);
    }

    /** Devuelve la extensión del archivo del documento. */
    public String getExtension() {
        return documento.getExtension();
    }

    /** Cambia la extensión del archivo del documento. */
    public void setExtension(String extension) {
        documento.setExtension(extension);
    }

    /** Devuelve el estado del reconocimiento de texto del documento. */
    public EstadoOCR getEstadoOcr() {
        return documento.getEstadoOcr();
    }

    /** Cambia el estado del reconocimiento de texto del documento. */
    public void setEstadoOcr(EstadoOCR estadoOcr) {
        documento.setEstadoOcr(estadoOcr);
    }
}
