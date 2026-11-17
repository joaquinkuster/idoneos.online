package com.app.idoneos.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa los datos propios de un documento que es un material bibliográfico. */
@Entity
@Table(name = "MaterialBibliografico")
@Getter
@Setter
@NoArgsConstructor
public class MaterialBibliografico {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMaterialBibliografico")
    private int idMaterialBibliografico;

    /** Relación con Documento: el documento del que esta tabla guarda los datos propios. */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idDocumento", nullable = false, unique = true)
    private Documento documento;

    /** Identificador de la obra: un ISBN, un ISSN o un DOI. */
    @Column(name = "identificador", nullable = false, length = 150)
    private String identificador;

    /** Dónde se publicó: la editorial, la revista o el congreso. */
    @Column(name = "publicadoEn", nullable = true, length = 150)
    private String publicadoEn;

    /** Fecha de publicación. */
    @Column(name = "fechaPublicacion", nullable = true)
    private LocalDate fechaPublicacion;

    /** Crea el material bibliográfico con los datos básicos y su documento. */
    public MaterialBibliografico(Recurso recurso, String identificador, String autor) {
        this.documento = new Documento(recurso, autor);
        this.documento.setMaterialBibliografico(this);
        this.identificador = identificador;
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
