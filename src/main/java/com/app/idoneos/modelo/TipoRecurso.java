package com.app.idoneos.modelo;

import java.util.List;

/**
 * Clases de recurso que el docente puede cargar a mano, con lo que cada una pide: los formatos de archivo que admite y su
 * tamaño máximo. El tipo de un recurso guardado se deduce de la tabla que tiene sus datos ({@link Recurso#getTipo()}).
 */
public enum TipoRecurso {

    /** Diapositivas de una clase (un archivo PDF). */
    PRESENTACION(Recurso.PRESENTACION, true, List.of("pdf"), 50, "Diapositivas de una clase."),

    /** Resumen de un tema (lo genera el sistema). */
    RESUMEN(Recurso.RESUMEN, false, List.of("pdf"), 50, "Resumen de un tema."),

    /** Texto o material de lectura, con su autor y su ISBN (el archivo PDF es opcional). */
    BIBLIOGRAFIA(Recurso.BIBLIOGRAFIA, true, List.of("pdf"), 50, "Texto o material de lectura, con su autor y su ISBN."),

    /** Video de una clase dictada. */
    GRABACION(Recurso.GRABACION, true, List.of("mp4", "webm"), 500, "Video de una clase dictada."),

    /** Dirección web de un material que está fuera de la plataforma. */
    ENLACE(Recurso.ENLACE, true, List.of(), 0, "Dirección web de un material externo."),

    /** Video con el clon de inteligencia artificial del docente (lo genera el sistema). */
    VIDEO_CLON(Recurso.VIDEO_CLON, false, List.of("mp4", "webm"), 500, "Video con el clon del docente.");

    private final String nombre;
    private final boolean subidaManual;
    private final List<String> extensiones;
    private final long tamanioMaximoMb;
    private final String descripcion;

    TipoRecurso(String nombre, boolean subidaManual, List<String> extensiones, long tamanioMaximoMb, String descripcion) {
        this.nombre = nombre;
        this.subidaManual = subidaManual;
        this.extensiones = extensiones;
        this.tamanioMaximoMb = tamanioMaximoMb;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el nombre del tipo, como se muestra.
     *
     * @return Por ejemplo "Presentación".
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Indica si el docente puede cargar este tipo de recurso a mano.
     *
     * @return {@code true} si se carga a mano.
     */
    public boolean permiteSubidaManual() {
        return subidaManual;
    }

    /**
     * Obtiene los formatos de archivo que admite.
     *
     * @return Las extensiones permitidas (vacía si no lleva archivo).
     */
    public List<String> getExtensionesPermitidas() {
        return extensiones;
    }

    /**
     * Obtiene el tamaño máximo del archivo.
     *
     * @return El tamaño en bytes.
     */
    public long getTamanioMaximo() {
        return tamanioMaximoMb * 1024 * 1024;
    }

    /**
     * Describe lo que se sube, con sus formatos y su tamaño máximo.
     *
     * @return Un texto para el formulario.
     */
    public String getDescripcionDeSubida() {
        if (extensiones.isEmpty()) {
            return descripcion;
        }
        return descripcion + " Formatos: " + String.join(", ", extensiones).toUpperCase() + ". Máximo " + tamanioMaximoMb
                + " MB.";
    }

    /**
     * Busca un tipo por su nombre de enumeración o por su nombre visible.
     *
     * @param texto El texto ("PRESENTACION" o "Presentación").
     * @return El tipo, o {@code null} si no existe.
     */
    public static TipoRecurso deTexto(String texto) {
        if (texto == null) {
            return null;
        }
        for (TipoRecurso tipo : values()) {
            if (tipo.name().equalsIgnoreCase(texto.trim()) || tipo.nombre.equalsIgnoreCase(texto.trim())) {
                return tipo;
            }
        }
        return null;
    }

    /**
     * Devuelve los tipos que el docente carga a mano.
     *
     * @return Los tipos con subida manual.
     */
    public static List<TipoRecurso> deSubidaManual() {
        return List.of(values()).stream().filter(TipoRecurso::permiteSubidaManual).toList();
    }
}
