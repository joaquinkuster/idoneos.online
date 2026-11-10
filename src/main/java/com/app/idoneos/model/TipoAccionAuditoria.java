package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un tipo de acción auditada. Puede ser crear, modificar o eliminar. */
@Entity
@Table(name = "TipoAccionAuditoria")
@Getter
@Setter
@NoArgsConstructor
public class TipoAccionAuditoria {

    /** Identificador único del tipo de acción auditada. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoAuditoria")
    private int idTipoAuditoria;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el tipo de acción auditada con los datos básicos. */
    public TipoAccionAuditoria(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del tipo de acción auditada. */
    @Override
    public String toString() {
        return nombre;
    }
}
