package com.app.idoneos.service.modeloia;

/** Servicio del modelo de inteligencia artificial que genera el contenido académico. */
public interface ModeloIAService {

    /**
     * Pide al modelo un texto libre.
     *
     * @param instrucciones Lo que el modelo debe hacer, con el formato que se espera de la respuesta.
     * @param contenido El material sobre el que trabaja, por ejemplo un fragmento de la bibliografía.
     * @param maximoTokens El largo máximo de la respuesta, para que el modelo no se extienda sin límite.
     * @return El texto que respondió el modelo.
     * @throws ModeloIAException Si el modelo no está disponible, no respondió a tiempo o devolvió una respuesta vacía.
     */
    String generarTexto(String instrucciones, String contenido, int maximoTokens);

    /**
     * Pide al modelo un texto libre sin instrucciones de sistema: el pedido va todo en el mensaje. Un modelo chico a veces repite las instrucciones
     * de sistema en lugar de cumplirlas, y para respuestas muy cortas, como un título, anda mejor así.
     *
     * @param pedido El pedido completo, con el texto y lo que se quiere de él.
     * @param maximoTokens El largo máximo de la respuesta.
     * @return El texto que respondió el modelo.
     * @throws ModeloIAException Si el modelo no está disponible, no respondió a tiempo o devolvió una respuesta vacía.
     */
    String generarTexto(String pedido, int maximoTokens);

    /**
     * Pide al modelo una respuesta estructurada y la convierte al tipo indicado.
     *
     * @param instrucciones Lo que el modelo debe hacer.
     * @param contenido El material sobre el que trabaja.
     * @param tipo La clase a la que se convierte la respuesta.
     * @param esquema El esquema JSON de la respuesta, con sus límites de cantidad y de largo, que Ollama aplica al generar.
     * @param maximoTokens El largo máximo de la respuesta.
     * @param <T> El tipo de la respuesta.
     * @return La respuesta ya convertida.
     * @throws ModeloIAException Si el modelo no está disponible, no respondió a tiempo o su respuesta no tiene el formato pedido.
     */
    <T> T generarObjeto(String instrucciones, String contenido, Class<T> tipo, java.util.Map<String, Object> esquema, int maximoTokens);

    /**
     * Igual que la anterior, pero con los parámetros de muestreo indicados.
     *
     * @param instrucciones Lo que el modelo debe hacer.
     * @param contenido El material sobre el que trabaja.
     * @param tipo La clase a la que se convierte la respuesta.
     * @param esquema El esquema JSON de la respuesta.
     * @param maximoTokens El largo máximo de la respuesta.
     * @param temperatura La creatividad: más baja es más precisa.
     * @param topP El corte de probabilidad acumulada.
     * @param topK Cuántas palabras candidatas se consideran en cada paso.
     * @param penalizacionRepeticion La penalización de repetir palabras.
     * @param <T> El tipo de la respuesta.
     * @return La respuesta ya convertida.
     * @throws ModeloIAException Si el modelo no está disponible, no respondió a tiempo o su respuesta no tiene el formato pedido.
     */
    <T> T generarObjeto(String instrucciones, String contenido, Class<T> tipo, java.util.Map<String, Object> esquema, int maximoTokens,
            double temperatura, double topP, int topK, double penalizacionRepeticion);

    /**
     * Consulta si el servidor del modelo responde y tiene descargado el modelo configurado.
     *
     * @return Verdadero si se puede generar contenido.
     */
    boolean estaDisponible();
}
