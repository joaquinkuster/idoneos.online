package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumno")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alumno {

    @Id
    @Column(name = "id")
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private List<ConsultaForo> consultasForo = new ArrayList<>();

    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private List<IntentoAutoevaluacion> intentosAutoevaluacion = new ArrayList<>();

    public Alumno(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.id = usuario.getId();
        }
    }
}
