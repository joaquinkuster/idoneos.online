package com.app.idoneos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tipo de reporte que puede generarse.
 */
@Entity
@Table(name = "TipoReporte")
@Getter
@Setter
@NoArgsConstructor
public class TipoReporte {

    /**
     * Identificador único del tipo de reporte.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoReporte")
    private int idTipoReporte;

    /**
     * Nombre.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Constructor para crear el tipo de reporte con los datos básicos.
     *
     * @param nombre nombre.
     */
    public TipoReporte(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación en forma de cadena del tipo de reporte.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
