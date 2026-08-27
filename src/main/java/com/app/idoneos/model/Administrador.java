package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Administrador: Perfil de administrador vinculado a un Usuario.
 * Posee su propia PK (id_administrador) y una FK a Usuario (id_usuario).
 * Mapea directamente a la tabla "Administrador" en base_datos.sql.
 */
@Entity
@Table(name = "Administrador")
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_administrador")
    private int idAdministrador;

    /** Usuario base al que pertenece este perfil de administrador. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Reportes generados por este administrador. */
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<Reporte> reportes = new ArrayList<>();

    /** Configuraciones creadas por este administrador. */
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<Configuracion> configuraciones = new ArrayList<>();

    public Administrador() {
    }

    public Administrador(Usuario usuario) {
        this.usuario = usuario;
    }

    public int getId() {
        return idAdministrador;
    }

    public int getIdAdministrador() {
        return idAdministrador;
    }

    public void setId(int id) {
        this.idAdministrador = id;
    }

    public void setIdAdministrador(int idAdministrador) {
        this.idAdministrador = idAdministrador;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Reporte> getReportes() {
        return reportes;
    }

    public void setReportes(List<Reporte> reportes) {
        this.reportes = reportes;
    }

    public List<Configuracion> getConfiguraciones() {
        return configuraciones;
    }

    public void setConfiguraciones(List<Configuracion> configuraciones) {
        this.configuraciones = configuraciones;
    }
}
