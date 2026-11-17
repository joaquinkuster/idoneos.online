package com.app.idoneos.model;

import org.hibernate.annotations.Check;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una oración de un documento, guardada en el alta con la entrada del índice a la que pertenece. */
@Entity
@Table(name = "Oracion", indexes = @Index(name = "ix_oracion_indice_documento", columnList = "idIndiceDocumento, numeroOrden"))
@Check(name = "ck_oracion_texto", constraints = "trim(texto) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class Oracion {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOracion")
    private int idOracion;

    /** Entrada del índice a la que pertenece la oración. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "idIndiceDocumento", nullable = false)
    private IndiceDocumento indiceDocumento;

    /** Posición de la oración dentro de su entrada del índice (la primera es la 1). */
    @Column(name = "numeroOrden", nullable = false)
    private int numeroOrden;

    /** Texto de la oración. */
    @Column(name = "texto", nullable = false, columnDefinition = "text")
    private String texto;

    /** Crea la oración con sus datos. */
    public Oracion(IndiceDocumento indiceDocumento, int numeroOrden, String texto) {
        this.indiceDocumento = indiceDocumento;
        this.numeroOrden = numeroOrden;
        this.texto = texto;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return texto;
    }
}
