package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tipo de acción auditada (crear, modificar, eliminar).
 */
@Entity
@Table(name = "TipoAccionAuditoria")
@Getter
@Setter
@NoArgsConstructor
public class TipoAccionAuditoria {

    /**
     * Identificador único del tipo de acción auditada.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoAuditoria")
    private int idTipoAuditoria;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el tipo de acción auditada con los datos básicos.
     *
     * @param nombre nombre.
     */
    public TipoAccionAuditoria(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del tipo de acción auditada.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
