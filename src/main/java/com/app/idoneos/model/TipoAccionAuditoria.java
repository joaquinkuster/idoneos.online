package com.app.idoneos.model;
import com.app.idoneos.service.Reportes.*;


import jakarta.persistence.*;

/**
 * Entidad TipoAccionAuditoria: Catálogo de acciones de auditoría.
 * Mapea directamente a la tabla "TipoAccionAuditoria" en base_datos.sql.
 */
@Entity
@Table(name = "TipoAccionAuditoria")
public class TipoAccionAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_accion_auditoria")
    private int idTipoAccionAuditoria;

    /** Nombre del tipo de acción (ej. "Crear", "Modificar", "Eliminar", "Consultar"). */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public TipoAccionAuditoria() {
    }

    public TipoAccionAuditoria(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idTipoAccionAuditoria;
    }

    public int getIdTipoAccionAuditoria() {
        return idTipoAccionAuditoria;
    }

    public void setId(int id) {
        this.idTipoAccionAuditoria = id;
    }

    public void setIdTipoAccionAuditoria(int idTipoAccionAuditoria) {
        this.idTipoAccionAuditoria = idTipoAccionAuditoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

