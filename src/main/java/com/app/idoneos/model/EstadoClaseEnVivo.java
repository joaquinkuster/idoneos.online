package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estado_clase_en_vivo")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoClaseEnVivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "estadoClaseEnVivo", cascade = CascadeType.ALL)
    private List<ClaseEnVivo> clasesEnVivo = new ArrayList<>();
}
