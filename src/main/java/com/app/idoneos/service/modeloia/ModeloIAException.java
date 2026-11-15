package com.app.idoneos.service.modeloia;

/** Excepción de una generación de contenido que el modelo de inteligencia artificial no pudo completar. */
public class ModeloIAException extends RuntimeException {

    /** Crea la excepción con el motivo. */
    public ModeloIAException(String mensaje) {
        super(mensaje);
    }

    /** Crea la excepción con el motivo y la causa. */
    public ModeloIAException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
