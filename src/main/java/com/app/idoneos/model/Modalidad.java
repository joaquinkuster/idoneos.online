package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de modalidades de dictado: En vivo, Grabada, Clon IA.
 * Un curso puede tener múltiples modalidades (relación M:N vía ModalidadCurso).
 */
@Entity
@Table(name = "modalidades")
@Getter @Setter
@NoArgsConstructor
public class Modalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    public Modalidad(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
