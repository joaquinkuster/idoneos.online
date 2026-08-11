package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estado_clase_clon_ia")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoClaseClonIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "estadoClaseClonIA", cascade = CascadeType.ALL)
    private List<ClaseClonIA> clasesClonIA = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<ClaseClonIA> getClasesClonIA() { return clasesClonIA; }
    public void setClasesClonIA(List<ClaseClonIA> clasesClonIA) { this.clasesClonIA = clasesClonIA; }


    public EstadoClaseClonIA(String nombre) { this.nombre = nombre; }

}
