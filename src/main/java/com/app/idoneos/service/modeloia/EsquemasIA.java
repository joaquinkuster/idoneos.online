package com.app.idoneos.service.modeloia;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Esquemas JSON de lo que se le pide al modelo, con límites de cantidad y de largo.
 * Ollama aplica el esquema mientras genera, así que el modelo no puede escribir más elementos ni textos más largos que los permitidos.
 * Un modelo chico ignora los límites que se le piden en el prompt: se pasa de largo, repite bloques y se queda sin espacio.
 * Los límites de largo cortan el texto donde llegan, aunque sea a mitad de una palabra, así que se dejan holgados
 * y, además, lo que llega pegado al límite se recorta después en un borde limpio.
 */
public final class EsquemasIA {

    private EsquemasIA() {
    }

    /** Esquema de lo que escribe el modelo cuando se le pide una pregunta: solo la pregunta. */
    public static Map<String, Object> pregunta() {
        return objeto(List.of("pregunta"), Map.of("pregunta", texto(10, 250)));
    }

    /** Esquema de una paráfrasis de una oración: solo la afirmación. */
    public static Map<String, Object> afirmacion() {
        return objeto(List.of("afirmacion"), Map.of("afirmacion", texto(10, 250)));
    }

    private static Map<String, Object> texto(int minimo, int maximo) {
        Map<String, Object> texto = new LinkedHashMap<>();
        texto.put("type", "string");
        if (minimo > 0) {
            texto.put("minLength", minimo);
        }
        texto.put("maxLength", maximo);
        return texto;
    }

    private static Map<String, Object> lista(Object elementos, int minimo, int maximo) {
        Map<String, Object> lista = new LinkedHashMap<>();
        lista.put("type", "array");
        lista.put("minItems", minimo);
        lista.put("maxItems", maximo);
        lista.put("items", elementos);
        return lista;
    }

    /** Arma un objeto con las propiedades en el orden de la lista, que es el orden en que las escribe el modelo. */
    private static Map<String, Object> objeto(List<String> orden, Map<String, Object> propiedades) {
        Map<String, Object> enOrden = new LinkedHashMap<>();
        for (String nombre : orden) {
            enOrden.put(nombre, propiedades.get(nombre));
        }
        return objetoEnOrden(orden, enOrden);
    }

    private static Map<String, Object> objetoEnOrden(List<String> obligatorias, Map<String, Object> propiedades) {
        Map<String, Object> objeto = new LinkedHashMap<>();
        objeto.put("type", "object");
        objeto.put("properties", propiedades);
        objeto.put("required", obligatorias);
        return objeto;
    }
}
