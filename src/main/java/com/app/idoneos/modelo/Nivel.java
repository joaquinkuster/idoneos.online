package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Nivel de dificultad de un curso.
 */
@Entity
@Table(name = "Nivel")
@Getter
@Setter
@NoArgsConstructor
public class Nivel {

    /**
     * Identificador único del nivel.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNivel")
    private int idNivel;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el nivel con los datos básicos.
     *
     * @param nombre nombre.
     */
    public Nivel(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del nivel.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
