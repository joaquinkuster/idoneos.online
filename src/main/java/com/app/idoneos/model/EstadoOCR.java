package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el estado del reconocimiento de texto (OCR) de un PDF escaneado. */
@Entity
@Table(name = "EstadoOCR")
@Getter
@Setter
@NoArgsConstructor
public class EstadoOCR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoOCR")
    private int idEstadoOCR;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Crea el registro con los datos básicos. */
    public EstadoOCR(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return nombre;
    }

    /** El archivo espera su turno para el reconocimiento de texto. */
    public static final String PENDIENTE = "Pendiente";

    /** El archivo se está procesando. */
    public static final String PROCESANDO = "Procesando";

    /** El reconocimiento terminó: el archivo ya tiene su texto. */
    public static final String LISTO = "Listo";

    /** El reconocimiento falló: el archivo conserva su contenido original. */
    public static final String ERROR = "Error";
}
