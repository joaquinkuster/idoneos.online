package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modalidad")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "modalidad", cascade = CascadeType.ALL)
    private List<ModalidadCurso> modalidadesCursos = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<ModalidadCurso> getModalidadesCursos() { return modalidadesCursos; }
    public void setModalidadesCursos(List<ModalidadCurso> modalidadesCursos) { this.modalidadesCursos = modalidadesCursos; }


    public Modalidad(String nombre) { this.nombre = nombre; }

}
