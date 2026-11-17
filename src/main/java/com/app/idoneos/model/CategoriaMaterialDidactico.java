package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la categoría de un material de lectura, por ejemplo presentación o resumen. */
@Entity
@Table(name = "CategoriaMaterialDidactico")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaMaterialDidactico {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCategoriaMaterialDidactico")
    private int idCategoriaMaterialDidactico;

    /** Nombre. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el registro con los datos básicos. */
    public CategoriaMaterialDidactico(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return nombre;
    }

    /** Diapositivas de una clase. */
    public static final String PRESENTACION = "Presentación";

    /** Resúmenes, transcripciones de clases o compilaciones teóricas del equipo docente. */
    public static final String APUNTE_DE_CATEDRA = "Apunte de Cátedra";

    /** Guías de lectura, de ejercicios o integradores que orientan el estudio de un tema. */
    public static final String GUIA_DE_ESTUDIO = "Guía de Estudio";
}
