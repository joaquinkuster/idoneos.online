package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_material")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "tipoMaterial", cascade = CascadeType.ALL)
    private List<Material> materiales = new ArrayList<>();

    public TipoMaterial(String nombre) {
        this.nombre = nombre;
    }
}
