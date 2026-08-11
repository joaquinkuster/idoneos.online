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
}
