package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tipo de material didáctico.
 */
@Entity
@Table(name = "TipoMaterial")
@Getter
@Setter
@NoArgsConstructor
public class TipoMaterial {

    /**
     * Identificador único del tipo de material.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoMaterial")
    private int idTipoMaterial;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el tipo de material con los datos básicos.
     *
     * @param nombre nombre.
     */
    public TipoMaterial(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del tipo de material.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
