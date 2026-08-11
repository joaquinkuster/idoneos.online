package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modalidad_curso")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModalidadCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modalidad_id", nullable = false)
    private Modalidad modalidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    public ModalidadCurso(Modalidad modalidad, Curso curso) {
        this.modalidad = modalidad;
        this.curso = curso;
    }
}
