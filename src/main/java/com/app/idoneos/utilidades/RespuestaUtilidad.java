package com.app.idoneos.utilidades;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * Métodos auxiliares para armar las respuestas JSON de los formularios que se envían sin recargar la página.
 */
public class RespuestaUtilidad {

    private static final Logger REGISTRO = LoggerFactory.getLogger(RespuestaUtilidad.class);

    /**
     * Arma la respuesta JSON de una operación de formulario que se realizó con éxito.
     *
     * @param mensaje El mensaje que se muestra al usuario.
     * @return Una respuesta HTTP 200 con el mensaje.
     */
    public static ResponseEntity<Map<String, String>> respuestaExitosa(String mensaje) {
        return ResponseEntity.ok(Map.of("mensaje", mensaje));
    }

    /**
     * Arma la respuesta JSON de una operación de formulario que falló. Los errores de validación
     * ({@link IllegalArgumentException}) muestran su mensaje; cualquier otro error muestra un mensaje genérico
     * para no exponer detalles internos.
     *
     * @param e La excepción ocurrida.
     * @return Una respuesta HTTP 400 con el mensaje de error.
     */
    public static ResponseEntity<Map<String, String>> respuestaConError(Exception e) {
        boolean esDeValidacion = e instanceof IllegalArgumentException && e.getMessage() != null;
        if (!esDeValidacion) {
            // El usuario solo ve un mensaje genérico: el detalle queda en el log del servidor para poder diagnosticarlo
            REGISTRO.error("Error inesperado al procesar un formulario", e);
        }
        String mensaje = esDeValidacion ? e.getMessage() : "Error! Ocurrió un error inesperado. Intente nuevamente.";
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }
}
