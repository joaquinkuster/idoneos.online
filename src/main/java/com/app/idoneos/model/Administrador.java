package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "administrador")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Administrador {

    @Id
    @Column(name = "id")
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<Reporte> reportes = new ArrayList<>();

    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private List<Configuracion> configuraciones = new ArrayList<>();

    public Administrador(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.id = usuario.getId();
        }
    }
    public void setId(int id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Reporte> getReportes() { return reportes; }
    public void setReportes(List<Reporte> reportes) { this.reportes = reportes; }

    public List<Configuracion> getConfiguraciones() { return configuraciones; }
    public void setConfiguraciones(List<Configuracion> configuraciones) { this.configuraciones = configuraciones; }
}
