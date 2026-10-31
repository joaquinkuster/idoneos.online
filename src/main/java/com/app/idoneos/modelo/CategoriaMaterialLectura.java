package com.app.idoneos.modelo;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Categoría de un material de lectura (presentación, resumen...).
 */
@Entity
@Table(name = "CategoriaMaterialLectura")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaMaterialLectura {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCategoriaMaterialLectura")
    private int idCategoriaMaterialLectura;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el registro con los datos básicos.
     *
     * @param nombre nombre.
     */
    public CategoriaMaterialLectura(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }

    /** Diapositivas de una clase. */
    public static final String PRESENTACION = "Presentación";

    /** Resumen de un tema. */
    public static final String RESUMEN = "Resumen";
}
