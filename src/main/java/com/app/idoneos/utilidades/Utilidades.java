package com.app.idoneos.utilidades;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * Métodos auxiliares genéricos de la aplicación.
 */
public class Utilidades {

    private static final Logger REGISTRO = LoggerFactory.getLogger(Utilidades.class);

    /**
     * Calcula la cantidad de páginas necesarias para mostrar una cantidad de elementos.
     * Siempre devuelve al menos una página.
     *
     * @param totalElementos La cantidad total de elementos.
     * @param tamanioPagina  La cantidad de elementos por página.
     * @return La cantidad de páginas.
     */
    public static int calcularTotalPaginas(int totalElementos, int tamanioPagina) {
        return Math.max(1, (int) Math.ceil((double) totalElementos / tamanioPagina));
    }

    /**
     * Ajusta un número de página al rango válido (de 0 a la última página).
     *
     * @param pagina       El número de página solicitado (empieza en 0).
     * @param totalPaginas La cantidad total de páginas.
     * @return El número de página ajustado.
     */
    public static int ajustarPagina(int pagina, int totalPaginas) {
        return Math.min(Math.max(pagina, 0), totalPaginas - 1);
    }

    /**
     * Obtiene los elementos de una página de una lista.
     *
     * @param lista         La lista completa.
     * @param pagina        El número de página (empieza en 0).
     * @param tamanioPagina La cantidad de elementos por página.
     * @param <T>           El tipo de los elementos.
     * @return La sublista correspondiente a la página.
     */
    public static <T> List<T> obtenerPagina(List<T> lista, int pagina, int tamanioPagina) {
        int desde = pagina * tamanioPagina;
        if (desde >= lista.size()) {
            return Collections.emptyList();
        }
        return lista.subList(desde, Math.min(desde + tamanioPagina, lista.size()));
    }

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
