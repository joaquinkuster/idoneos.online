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
 * {@code error} (y opcionalmente {@code campo}) si falló.
 * <p>
 * Cuando un error de validación corresponde a un campo, se lanza con {@link #campoInvalido(String, String)} indicando el
 * {@code name} del input: la respuesta devuelve ese nombre y la pantalla marca el campo debajo, en su paso, en lugar de
 * mostrar una alerta general. Los errores sin campo siguen lanzándose como {@link IllegalArgumentException}.
 */
public class RespuestaFormularioUtilidad {

    private static final Logger REGISTRO = LoggerFactory.getLogger(RespuestaFormularioUtilidad.class);

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
        Map<String, String> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", mensajeDeError(e));
        if (campoDe(e) != null) {
            cuerpo.put("campo", campoDe(e));
        }
        return ResponseEntity.badRequest().body(cuerpo);
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
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", mensajeDeError(e));
        if (campoDe(e) != null) {
            cuerpo.put("campo", campoDe(e));
        }
        return ResponseEntity.badRequest().body(cuerpo);
    }

    private static String mensajeDeError(Exception e) {
        boolean esDeValidacion = e instanceof IllegalArgumentException && e.getMessage() != null;
        if (!esDeValidacion) {
            // El usuario solo ve un mensaje genérico: el detalle queda en el log del servidor para poder diagnosticarlo
            REGISTRO.error("Error inesperado al procesar un formulario", e);
        }
        return esDeValidacion ? e.getMessage() : MENSAJE_GENERICO;
    }

    /**
     * Arma el error de validación de un campo.
     *
     * @param campo   El {@code name} del input del formulario.
     * @param mensaje El mensaje que se muestra debajo del campo.
     * @return La excepción lista para lanzar.
     */
    public static IllegalArgumentException campoInvalido(String campo, String mensaje) {
        return new CampoInvalido(campo, mensaje);
    }

    private static String campoDe(Exception e) {
        return e instanceof CampoInvalido invalido ? invalido.campo : null;
    }

    private static final class CampoInvalido extends IllegalArgumentException {

        private final String campo;

        private CampoInvalido(String campo, String mensaje) {
            super(mensaje);
            this.campo = campo;
        }
    }
}
