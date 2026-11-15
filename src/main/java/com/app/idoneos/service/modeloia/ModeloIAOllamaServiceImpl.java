package com.app.idoneos.service.modeloia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.app.idoneos.util.JsonTruncadoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Implementación de ModeloIAService con Spring AI sobre un servidor Ollama. */
@Service
public class ModeloIAOllamaServiceImpl implements ModeloIAService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ModeloIAOllamaServiceImpl.class);

    private final ChatClient cliente;
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String urlServidor;

    @Value("${spring.ai.ollama.chat.options.model:llama3.2:1b}")
    private String modelo;

    /** Crea el servicio con el cliente de chat que arma Spring AI. */
    public ModeloIAOllamaServiceImpl(ChatClient.Builder constructor) {
        this.cliente = constructor.build();
    }

    // ----------------------------------------------------------------------
    // Generación
    // ----------------------------------------------------------------------

    /** Pide al modelo un texto libre. */
    @Override
    public String generarTexto(String instrucciones, String contenido, int maximoTokens) {
        try {
            String respuesta = cliente.prompt()
                    .system(s -> s.text("{instrucciones}").param("instrucciones", instrucciones))
                    .user(u -> u.text("{contenido}").param("contenido", contenido))
                    // Los mismos parámetros que usa AUREA para redactar: poca creatividad, vocabulario acotado y penalización de las repeticiones
                    .options(OllamaOptions.builder().numPredict(maximoTokens).temperature(0.1).topP(0.85).topK(10).repeatPenalty(1.2)
                            .stop(java.util.List.of("[INST]", "[/INST]")).build())
                    .call()
                    .content();
            if (respuesta == null || respuesta.isBlank()) {
                throw new ModeloIAException("El modelo devolvió una respuesta vacía.");
            }
            return respuesta.trim();
        } catch (ModeloIAException e) {
            throw e;
        } catch (RuntimeException e) {
            throw fallo(e);
        }
    }

    /** Pide al modelo un texto libre, sin instrucciones de sistema. */
    @Override
    public String generarTexto(String pedido, int maximoTokens) {
        try {
            String respuesta = cliente.prompt()
                    .user(u -> u.text("{pedido}").param("pedido", pedido))
                    .options(OllamaOptions.builder().numPredict(maximoTokens).temperature(0.1).topP(0.85).topK(10).repeatPenalty(1.2)
                            .stop(java.util.List.of("[INST]", "[/INST]")).build())
                    .call()
                    .content();
            if (respuesta == null || respuesta.isBlank()) {
                throw new ModeloIAException("El modelo devolvió una respuesta vacía.");
            }
            return respuesta.trim();
        } catch (ModeloIAException e) {
            throw e;
        } catch (RuntimeException e) {
            throw fallo(e);
        }
    }

    /** Pide al modelo una respuesta estructurada y la convierte al tipo indicado. */
    @Override
    public <T> T generarObjeto(String instrucciones, String contenido, Class<T> tipo, java.util.Map<String, Object> esquema, int maximoTokens) {
        return generarObjeto(instrucciones, contenido, tipo, esquema, maximoTokens, 0.1, 0.85, 10, 1.2);
    }

    /** Pide al modelo una respuesta estructurada, con los parámetros de muestreo indicados. */
    @Override
    public <T> T generarObjeto(String instrucciones, String contenido, Class<T> tipo, java.util.Map<String, Object> esquema, int maximoTokens,
            double temperatura, double topP, int topK, double penalizacionRepeticion) {
        try {
            // El esquema se le da a Ollama, que lo aplica al generar: un modelo chico ignora los límites que se le piden en el prompt
            BeanOutputConverter<T> conversor = new BeanOutputConverter<>(tipo);
            String json = cliente.prompt()
                    .system(s -> s.text("{instrucciones}").param("instrucciones", instrucciones))
                    .user(u -> u.text("{contenido}").param("contenido", contenido))
                    .options(OllamaOptions.builder().format(esquema).numPredict(maximoTokens).temperature(temperatura).topP(topP).topK(topK).repeatPenalty(penalizacionRepeticion).build())
                    .call()
                    .content();
            if (json == null || json.isBlank()) {
                throw new ModeloIAException("El modelo devolvió una respuesta vacía.");
            }
            try {
                return conversor.convert(json);
            } catch (RuntimeException e) {
                // Si el modelo se quedó sin espacio y dejó el JSON cortado, se recupera lo que llegó completo
                String reparado = JsonTruncadoUtil.reparar(json);
                if (!reparado.equals(json)) {
                    try {
                        T recuperado = conversor.convert(reparado);
                        REGISTRO.warn("La respuesta del modelo llegó cortada y se recuperó la parte completa.");
                        return recuperado;
                    } catch (RuntimeException ignorada) {
                        // Se informa el error original
                    }
                }
                REGISTRO.warn("Respuesta del modelo con formato inválido: {}", json);
                throw new ModeloIAException("El modelo devolvió una respuesta con un formato inválido.", e);
            }
        } catch (ModeloIAException e) {
            throw e;
        } catch (RuntimeException e) {
            throw fallo(e);
        }
    }

    // ----------------------------------------------------------------------
    // Disponibilidad
    // ----------------------------------------------------------------------

    /** Consulta si el servidor del modelo responde y tiene descargado el modelo configurado. */
    @Override
    public boolean estaDisponible() {
        try {
            HttpRequest pedido = HttpRequest.newBuilder(URI.create(urlServidor + "/api/tags"))
                    .timeout(Duration.ofSeconds(5)).GET().build();
            HttpResponse<String> respuesta = http.send(pedido, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                return false;
            }
            for (JsonNode descargado : json.readTree(respuesta.body()).path("models")) {
                String nombre = descargado.path("name").asText("");
                // Ollama agrega ":latest" a los modelos que se descargaron sin etiqueta
                if (nombre.equals(modelo) || nombre.equals(modelo + ":latest")) {
                    return true;
                }
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            REGISTRO.warn("El modelo de IA no está disponible en {}: {}", urlServidor, e.getMessage());
            return false;
        }
    }

    // ----------------------------------------------------------------------
    // Errores
    // ----------------------------------------------------------------------

    /** Arma la excepción con un motivo que se le puede mostrar al docente. */
    private ModeloIAException fallo(RuntimeException causa) {
        REGISTRO.warn("Falló la generación con el modelo {}: {}", modelo, causa.getMessage());
        String detalle = String.valueOf(causa.getMessage()).toLowerCase();
        if (detalle.contains("connect") || detalle.contains("refused") || detalle.contains("i/o error")) {
            return new ModeloIAException("No se pudo conectar con el servidor del modelo de IA.", causa);
        }
        if (detalle.contains("timed out") || detalle.contains("timeout")) {
            return new ModeloIAException("El modelo de IA tardó demasiado en responder.", causa);
        }
        if (detalle.contains("not found")) {
            return new ModeloIAException("El modelo " + modelo + " no está descargado en el servidor de IA.", causa);
        }
        return new ModeloIAException("El modelo de IA no pudo generar el contenido.", causa);
    }
}
