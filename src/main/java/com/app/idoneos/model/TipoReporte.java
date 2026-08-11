package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_reporte")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoReporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "tipoReporte", cascade = CascadeType.ALL)
    private List<Reporte> reportes = new ArrayList<>();
}
