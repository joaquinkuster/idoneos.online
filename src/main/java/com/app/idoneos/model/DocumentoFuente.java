package com.app.idoneos.model;

import java.io.Serializable;

import org.hibernate.annotations.Check;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un documento elegido como fuente de un material generado con inteligencia artificial (un resumen o una presentación). */
@Entity
@Table(name = "DocumentoFuente")
@IdClass(DocumentoFuente.Clave.class)
@Check(name = "ck_documento_fuente_distintos", constraints = "id_documento <> id_documento_fuente")
@Getter
@Setter
@NoArgsConstructor
public class DocumentoFuente {

    /** Clave primaria compuesta de la fuente de un material generado. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int documento;
        private int documentoFuente;
    }

    /** Documento generado a partir de la fuente. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idDocumento", nullable = false)
    private Documento documento;

    /** Documento elegido como fuente. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idDocumentoFuente", nullable = false)
    private Documento documentoFuente;

    /** Crea la fuente de un material generado. */
    public DocumentoFuente(Documento documento, Documento documentoFuente) {
        this.documento = documento;
        this.documentoFuente = documentoFuente;
    }
}
