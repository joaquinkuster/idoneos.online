package com.app.idoneos.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Utilidad de respuestas de formularios. Arma las respuestas JSON de los formularios que se envían sin recargar la página. */
public class RespuestaFormularioUtil {

    private static final Logger REGISTRO = LoggerFactory.getLogger(RespuestaFormularioUtil.class);

    private static final String MENSAJE_GENERICO = "Error! Ocurrió un error inesperado. Intentá nuevamente.";

    /**
     * Arma la respuesta JSON de una operación de formulario que se realizó con éxito.
     *
     * @param mensaje El mensaje que se muestra al usuario.
     * @return Una respuesta HTTP 200 con el mensaje.
     */
    public static ResponseEntity<Map<String, String>> armarRespuestaExitosa(String mensaje) {
        return ResponseEntity.ok(Map.of("mensaje", mensaje));
    }

    /**
     * Arma la respuesta JSON de una operación que lleva directo a otra página, sin alerta de éxito.
     *
     * @param destino La dirección a la que se redirige.
     * @return Una respuesta HTTP 200 solo con el destino.
     */
    public static ResponseEntity<Map<String, String>> armarRespuestaDeRedireccion(String destino) {
        return ResponseEntity.ok(Map.of("destino", destino));
    }

    /**
     * Arma la respuesta JSON de una operación exitosa después de la cual se debe navegar a otra página.
     *
     * @param mensaje El mensaje que se muestra al usuario.
     * @param destino La dirección a la que se redirige luego de mostrar el mensaje.
     * @return Una respuesta HTTP 200 con el mensaje y el destino.
     */
    public static ResponseEntity<Map<String, String>> armarRespuestaExitosa(String mensaje, String destino) {
        return ResponseEntity.ok(Map.of("mensaje", mensaje, "destino", destino));
    }

    /**
     * Arma la respuesta JSON de una operación exitosa después de la cual la pantalla propone el paso siguiente del flujo (por ejemplo, crear una cohorte después de crear un curso): se muestra el mensaje y, a continuación, una alerta que pregunta si se quiere seguir. Si se acepta se navega a siguienteUrl, y si no, se recarga la pantalla.
     *
     * @param mensaje El mensaje de éxito.
     * @param titulo El título de la alerta del paso siguiente.
     * @param texto El texto de la alerta del paso siguiente.
     * @param confirmar El texto del botón que acepta seguir.
     * @param url La dirección a la que se va si se acepta.
     * @return Una respuesta HTTP 200 con el mensaje y los datos del paso siguiente.
     */
    public static ResponseEntity<Map<String, String>> armarRespuestaExitosaConSiguiente(String mensaje, String titulo, String texto,
            String confirmar, String url) {
        Map<String, String> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("siguienteTitulo", titulo);
        cuerpo.put("siguienteTexto", texto);
        cuerpo.put("siguienteConfirmar", confirmar);
        cuerpo.put("siguienteUrl", url);
        return ResponseEntity.ok(cuerpo);
    }

    /**
     * Arma una respuesta HTTP 200 con datos propios de la operación (por ejemplo, el estado de un pago).
     *
     * @param datos Los datos que se devuelven, en el orden dado.
     * @return Una respuesta HTTP 200 con los datos.
     */
    public static ResponseEntity<Map<String, Object>> armarRespuestaConDatos(Map<String, Object> datos) {
        return ResponseEntity.ok(new LinkedHashMap<>(datos));
    }

    /**
     * Arma la respuesta JSON de una operación de formulario que falló.
     *
     * @param e La excepción ocurrida.
     * @return Una respuesta HTTP 400 con el mensaje de error.
     */
    public static ResponseEntity<Map<String, String>> armarRespuestaConError(Exception e) {
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
     * @param estado El estado HTTP de la respuesta.
     * @param mensaje El mensaje de error que se muestra al usuario.
     * @return Una respuesta con el estado dado y el mensaje de error.
     */
    public static ResponseEntity<Map<String, String>> armarRespuestaConError(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", mensaje));
    }

    /**
     * Igual que armarRespuestaConError, pero con el cuerpo como mapa de objetos.
     *
     * @param e La excepción ocurrida.
     * @return Una respuesta HTTP 400 con el mensaje de error.
     */
    public static ResponseEntity<Map<String, Object>> armarRespuestaConErrorEnDatos(Exception e) {
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
     * @param campo El {@code name} del input del formulario.
     * @param mensaje El mensaje que se muestra debajo del campo.
     * @return La excepción lista para lanzar.
     */
    public static IllegalArgumentException armarCampoInvalido(String campo, String mensaje) {
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
