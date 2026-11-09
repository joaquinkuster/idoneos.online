package com.app.idoneos.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estado del reconocimiento de texto (OCR) de un archivo PDF escaneado. Un archivo que no necesita OCR no tiene estado.
 */
@Entity
@Table(name = "EstadoOCR")
@Getter
@Setter
@NoArgsConstructor
public class EstadoOCR {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoOCR")
    private int idEstadoOCR;

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
    public EstadoOCR(String nombre) {
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

    /** El archivo espera su turno para el reconocimiento de texto. */
    public static final String PENDIENTE = "Pendiente";

    /** El archivo se está procesando. */
    public static final String PROCESANDO = "Procesando";

    /** El reconocimiento terminó: el archivo ya tiene su texto. */
    public static final String LISTO = "Listo";

    /** El reconocimiento falló: el archivo conserva su contenido original. */
    public static final String ERROR = "Error";
}
