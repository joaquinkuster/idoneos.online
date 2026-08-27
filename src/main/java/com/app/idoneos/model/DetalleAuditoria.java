package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad DetalleAuditoria: Detalle granular de modificaciones registradas en una acción de auditoría.
 * Mapea directamente a la tabla "DetalleAuditoria" en base_datos.sql.
 */
@Entity
@Table(name = "DetalleAuditoria")
public class DetalleAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDetalleAuditoria")
    private int idDetalleAuditoria;

    /** Nombre del campo o atributo modificado. */
    @Column(name = "campo", length = 50)
    private String campo;

    /** Valor previo al cambio. */
    @Column(name = "valorAnterior", columnDefinition = "text")
    private String valorAnterior;

    /** Nuevo valor establecido. */
    @Column(name = "valorNuevo", columnDefinition = "text")
    private String valorNuevo;

    /** Registro de auditoría padre al que pertenece este detalle. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAuditoria", nullable = false)
    private Auditoria auditoria;

    public DetalleAuditoria() {
    }

    public DetalleAuditoria(String campo, String valorAnterior, String valorNuevo, Auditoria auditoria) {
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.auditoria = auditoria;
    }

    public int getIdDetalleAuditoria() {
        return idDetalleAuditoria;
    }

    public void setIdDetalleAuditoria(int idDetalleAuditoria) {
        this.idDetalleAuditoria = idDetalleAuditoria;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }
}
