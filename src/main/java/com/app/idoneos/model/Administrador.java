package com.app.idoneos.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Subtipo de Usuario con rol de Administrador (relación 1 a 0..1 con Usuario).
 * Gestiona parámetros del sistema y genera reportes.
 */
@Entity
@Table(name = "administrador")
public class Administrador {

    /** Identificador único del administrador, mapeado con la clave primaria compartida de Usuario. */
    @Id
    @Column(name = "id")
    private int id;

    /** Relación uno a uno con la entidad base Usuario mediante claves compartidas. */
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    /** Lista de reportes generados por este administrador. */
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<Reporte> reportes = new ArrayList<>();

    /** Lista de parámetros de configuración creados o modificados por este administrador. */
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<Configuracion> configuraciones = new ArrayList<>();

    public Administrador() {}

    public Administrador(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.id = usuario.getId();
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Reporte> getReportes() { return reportes; }
    public void setReportes(List<Reporte> reportes) { this.reportes = reportes; }

    public List<Configuracion> getConfiguraciones() { return configuraciones; }
    public void setConfiguraciones(List<Configuracion> configuraciones) { this.configuraciones = configuraciones; }
}
