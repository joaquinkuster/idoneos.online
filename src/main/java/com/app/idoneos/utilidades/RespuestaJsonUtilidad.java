package com.app.idoneos.utilidades;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Métodos auxiliares para armar las respuestas JSON de los formularios que se envían sin recargar la página.
 * Todas las respuestas tienen la misma forma: {@code mensaje} (y opcionalmente {@code destino}) si salió bien,
 * {@code error} si falló.
 */
public class RespuestaJsonUtilidad {

    private static final Logger REGISTRO = LoggerFactory.getLogger(RespuestaJsonUtilidad.class);

    private static final String MENSAJE_GENERICO = "Error! Ocurrió un error inesperado. Intente nuevamente.";

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
     * Arma la respuesta JSON de una operación exitosa después de la cual se debe navegar a otra página.
     *
     * @param mensaje El mensaje que se muestra al usuario.
     * @param destino La dirección a la que se redirige luego de mostrar el mensaje.
     * @return Una respuesta HTTP 200 con el mensaje y el destino.
     */
    public static ResponseEntity<Map<String, String>> respuestaExitosa(String mensaje, String destino) {
        return ResponseEntity.ok(Map.of("mensaje", mensaje, "destino", destino));
    }

    /**
     * Arma una respuesta HTTP 200 con datos propios de la operación (por ejemplo, el estado de un pago).
     *
     * @param datos Los datos que se devuelven, en el orden dado.
     * @return Una respuesta HTTP 200 con los datos.
     */
    public static ResponseEntity<Map<String, Object>> respuestaConDatos(Map<String, Object> datos) {
        return ResponseEntity.ok(new LinkedHashMap<>(datos));
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
        return respuestaConError(HttpStatus.BAD_REQUEST, mensajeDeError(e));
    }

    /**
     * Arma una respuesta de error con el estado y el mensaje indicados.
     *
     * @param estado  El estado HTTP de la respuesta.
     * @param mensaje El mensaje de error que se muestra al usuario.
     * @return Una respuesta con el estado dado y el mensaje de error.
     */
    public static ResponseEntity<Map<String, String>> respuestaConError(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }

    /**
     * Igual que {@link #respuestaConError(Exception)}, para los endpoints cuya respuesta exitosa lleva datos
     * ({@link #respuestaConDatos(Map)}) y por lo tanto declaran un cuerpo de tipo {@code Map<String, Object>}.
     *
     * @param e La excepción ocurrida.
     * @return Una respuesta HTTP 400 con el mensaje de error.
     */
    public static ResponseEntity<Map<String, Object>> respuestaConErrorEnDatos(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", mensajeDeError(e)));
    }

    private static String mensajeDeError(Exception e) {
        boolean esDeValidacion = e instanceof IllegalArgumentException && e.getMessage() != null;
        if (!esDeValidacion) {
            // El usuario solo ve un mensaje genérico: el detalle queda en el log del servidor para poder diagnosticarlo
            REGISTRO.error("Error inesperado al procesar un formulario", e);
        }
        return esDeValidacion ? e.getMessage() : MENSAJE_GENERICO;
    }
}
