package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Detalle de un campo modificado en una acción auditada, con su valor anterior y nuevo.
 */
@Entity
@Table(name = "DetalleAuditoria")
@Getter
@Setter
@NoArgsConstructor
public class DetalleAuditoria {

    /**
     * Identificador único del detalle de auditoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDetalleAuditoria")
    private int idDetalleAuditoria;

    /**
     * Relación con Auditoria.
     */
    @ManyToOne
    @JoinColumn(name = "idAuditoria", nullable = false)
    private Auditoria auditoria;

    /**
     * Nombre del campo modificado.
     */
    @Column(name = "campo", nullable = false, length = 50)
    private String campo;

    /**
     * Valor del campo antes de la modificación.
     */
    @Column(name = "valorAnterior", nullable = true, columnDefinition = "TEXT")
    private String valorAnterior;

    /**
     * Valor del campo después de la modificación.
     */
    @Column(name = "valorNuevo", nullable = false, columnDefinition = "TEXT")
    private String valorNuevo;

    /**
     * Constructor para crear el detalle de auditoría con los datos básicos.
     *
     * @param auditoria relación con Auditoria.
     * @param campo nombre del campo modificado.
     * @param valorNuevo valor del campo después de la modificación.
     */
    public DetalleAuditoria(Auditoria auditoria, String campo, String valorNuevo) {
        this.auditoria = auditoria;
        this.campo = campo;
        this.valorNuevo = valorNuevo;
    }

    /**
     * Devuelve una representación en forma de cadena del detalle de auditoría.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "DetalleAuditoria #" + idDetalleAuditoria;
    }
}
