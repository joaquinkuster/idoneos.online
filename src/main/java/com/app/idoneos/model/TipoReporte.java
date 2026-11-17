package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un tipo de reporte que se puede generar. */
@Entity
@Table(name = "TipoReporte")
@Getter
@Setter
@NoArgsConstructor
public class TipoReporte {

    /** Informe de alumnos inscriptos a un curso. */
    public static final String INFORME_ALUMNOS = "Informe de alumnos";

    /** Informe de ingresos de un curso. */
    public static final String INFORME_INGRESOS = "Informe de ingresos";

    /** Identificador único del tipo de reporte. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoReporte")
    private int idTipoReporte;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el tipo de reporte con los datos básicos. */
    public TipoReporte(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena del tipo de reporte. */
    @Override
    public String toString() {
        return nombre;
    }
}
