package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;

/**
 * Entidad TipoReporte: Catálogo de tipos de reportes de administración.
 * Mapea directamente a la tabla "TipoReporte" en base_datos.sql.
 */
@Entity
@Table(name = "TipoReporte")
public class TipoReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_reporte")
    private int idTipoReporte;

    /** Nombre del tipo de reporte. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    public TipoReporte() {
    }

    public TipoReporte(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return idTipoReporte;
    }

    public int getIdTipoReporte() {
        return idTipoReporte;
    }

    public void setId(int id) {
        this.idTipoReporte = id;
    }

    public void setIdTipoReporte(int idTipoReporte) {
        this.idTipoReporte = idTipoReporte;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

