package com.app.idoneos.modelo;

import java.util.List;
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

    /** Nombre del tipo de material que es la grabación de una clase. */
    public static final String GRABACION = "Grabación";

    /** Nombre del tipo de material que es bibliografía (archivo de lectura o enlace externo). */
    public static final String BIBLIOGRAFIA = "Bibliografía";

    /** Nombre del tipo de material que es la presentación de una clase. */
    public static final String PRESENTACION = "Presentación";

    /** Nombre del tipo de material que es un resumen (se genera con inteligencia artificial). */
    public static final String RESUMEN = "Resumen";

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

    /**
     * Indica si el docente puede subir manualmente material de este tipo (los resúmenes los genera la IA).
     *
     * @return {@code true} si el tipo es Grabación, Bibliografía o Presentación.
     */
    public boolean permiteSubidaManual() {
        return GRABACION.equals(nombre) || BIBLIOGRAFIA.equals(nombre) || PRESENTACION.equals(nombre);
    }

    /**
     * Indica si el material de este tipo puede ser un enlace externo en lugar de un archivo.
     *
     * @return {@code true} si el tipo es Bibliografía.
     */
    public boolean permiteEnlaceExterno() {
        return BIBLIOGRAFIA.equals(nombre);
    }

    /**
     * Obtiene las extensiones de archivo permitidas para este tipo de material.
     *
     * @return Las extensiones permitidas, en minúscula y sin punto.
     */
    public List<String> getExtensionesPermitidas() {
        // Los documentos (presentación y bibliografía) son solo PDF: es el único formato que se muestra dentro de la plataforma
        return GRABACION.equals(nombre) ? List.of("mp4", "webm") : List.of("pdf");
    }

    /**
     * Obtiene el tamaño máximo de archivo permitido para este tipo de material.
     *
     * @return El tamaño máximo en bytes (500 MB para grabaciones y 50 MB para el resto).
     */
    public long getTamanioMaximo() {
        return (GRABACION.equals(nombre) ? 500L : 50L) * 1024 * 1024;
    }

    /**
     * Describe cómo se sube este tipo de material: qué es, si admite enlace externo, los formatos y el tamaño máximo.
     *
     * @return La descripción que se muestra al elegir el tipo.
     */
    public String getDescripcionDeSubida() {
        String base = GRABACION.equals(nombre) ? "Video de una clase dictada."
                : BIBLIOGRAFIA.equals(nombre) ? "Texto o material de lectura, con su autor." : "Diapositivas de una clase.";
        return base + (permiteEnlaceExterno() ? " Se sube un archivo o se indica un enlace externo." : " Se sube un archivo.")
                + " Formatos: " + String.join(", ", getExtensionesPermitidas()).toUpperCase() + ". Máximo "
                + (getTamanioMaximo() / (1024 * 1024)) + " MB.";
    }
}
