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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<ClaseEnVivo> getClasesEnVivo() { return clasesEnVivo; }
    public void setClasesEnVivo(List<ClaseEnVivo> clasesEnVivo) { this.clasesEnVivo = clasesEnVivo; }


    public EstadoClaseEnVivo(String nombre) { this.nombre = nombre; }

}
