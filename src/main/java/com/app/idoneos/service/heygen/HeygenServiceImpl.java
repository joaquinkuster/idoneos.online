package com.app.idoneos.service.heygen;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.app.idoneos.model.Parametro;
import com.app.idoneos.service.parametro.ParametroService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Implementación de HeygenService con la API v3 de HeyGen. */
@Service
public class HeygenServiceImpl implements HeygenService {

    private static final String URL_BASE = "https://api.heygen.com";

    /** Cada cuántos milisegundos se consulta si HeyGen terminó de procesar un avatar o una voz. */
    private static final long ESPERA_ENTRE_CONSULTAS = 2_000;

    /** Cuántas veces se consulta como máximo (unos 2 minutos). */
    private static final int CONSULTAS_MAXIMAS = 60;

    /** Los videos de prueba se piden en 720p con el motor Avatar III, el de menor costo. */
    private static final String RESOLUCION = "720p";

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final org.slf4j.Logger REGISTRO = org.slf4j.LoggerFactory.getLogger(HeygenServiceImpl.class);

    @Autowired
    private ParametroService parametroService;

    private final RestClient cliente;

    public HeygenServiceImpl(RestClient.Builder constructor) {
        this.cliente = constructor.baseUrl(URL_BASE).build();
    }

    // ----------------------------------------------------------------------
    // Avatar y voz
    // ----------------------------------------------------------------------

    /** Crea el avatar a partir de la foto y espera a que termine de procesarse. */
    @Override
    public String crearAvatarConFoto(String nombre, byte[] imagen, String archivo) {
        String asset = subirAsset(imagen, archivo);
        JsonNode respuesta = ejecutar(() -> cliente.post().uri("/v3/avatars").header("x-api-key", clave()).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("type", "photo", "name", nombre, "file", Map.of("type", "asset_id", "asset_id", asset))).retrieve().body(String.class));
        JsonNode avatar = respuesta.path("data").path("avatar_item");
        String id = avatar.path("id").asText("");
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Error! HeyGen no devolvió el avatar. Probá con otra foto.");
        }
        String estado = avatar.path("status").asText("completed");
        for (int i = 0; i < CONSULTAS_MAXIMAS && !"completed".equals(estado); i++) {
            if ("failed".equals(estado)) {
                throw new IllegalArgumentException("Error! " + mensaje(avatar.path("error"), "HeyGen no pudo crear el avatar con esa foto. Probá con otra."));
            }
            dormir();
            JsonNode actual = ejecutar(() -> cliente.get().uri("/v3/avatars/looks/{id}", id).header("x-api-key", clave()).retrieve().body(String.class))
                    .path("data");
            estado = actual.path("status").asText("completed");
            if ("failed".equals(estado)) {
                throw new IllegalArgumentException("Error! " + mensaje(actual.path("error"), "HeyGen no pudo crear el avatar con esa foto. Probá con otra."));
            }
            if ("pending_consent".equals(estado)) {
                throw new IllegalArgumentException("Error! HeyGen pide un consentimiento adicional para usar esa foto.");
            }
        }
        if (!"completed".equals(estado)) {
            throw new IllegalArgumentException("Error! HeyGen tarda demasiado en procesar la foto. Intentá nuevamente en unos minutos.");
        }
        return id;
    }

    /** Crea la voz a partir de la muestra y espera a que HeyGen la active. */
    @Override
    public String crearVozConAudio(String nombre, byte[] audio, String archivo) {
        String asset = subirAsset(audio, archivo);
        String idempotencia = UUID.randomUUID().toString();
        JsonNode respuesta = ejecutar(() -> cliente.post().uri("/v3/models/audio/voices").header("x-api-key", clave()).header("Idempotency-Key", idempotencia)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("mode", "instant", "name", nombre, "audio", List.of(Map.of("type", "asset_id", "asset_id", asset)))).retrieve().body(String.class));
        String id = respuesta.path("data").path("voice_id").asText("");
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Error! HeyGen no devolvió la voz. Probá con otra muestra de audio.");
        }
        for (int i = 0; i < CONSULTAS_MAXIMAS; i++) {
            JsonNode voz = ejecutar(() -> cliente.get().uri("/v3/models/audio/voices/{id}", id).header("x-api-key", clave()).retrieve().body(String.class))
                    .path("data");
            String estado = voz.path("status").asText("ACTIVE").toUpperCase();
            if ("ACTIVE".equals(estado)) {
                return id;
            }
            if ("FAILED".equals(estado)) {
                throw new IllegalArgumentException("Error! " + mensaje(voz.path("error"), "HeyGen no pudo crear la voz con esa muestra. Probá con una más clara y más larga."));
            }
            dormir();
        }
        throw new IllegalArgumentException("Error! HeyGen tarda demasiado en procesar la voz. Intentá nuevamente en unos minutos.");
    }

    // ----------------------------------------------------------------------
    // Videos
    // ----------------------------------------------------------------------

    /** Pide el video del clon. */
    @Override
    public String crearVideo(String avatarId, String voiceId, String titulo, String guion) {
        Map<String, Object> cuerpo = Map.of("type", "avatar", "avatar_id", avatarId, "voice_id", voiceId, "script", guion, "title", titulo,
                "resolution", RESOLUCION, "aspect_ratio", "16:9", "engine", Map.of("type", "avatar_iii"));
        JsonNode respuesta = ejecutar(() -> cliente.post().uri("/v3/videos").header("x-api-key", clave()).header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve().body(String.class));
        String id = respuesta.path("data").path("video_id").asText("");
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Error! HeyGen no devolvió el video.");
        }
        return id;
    }

    /** Consulta el estado de un video. */
    @Override
    public Video consultarVideo(String idVideo) {
        JsonNode datos = ejecutar(() -> cliente.get().uri("/v3/videos/{id}", idVideo).header("x-api-key", clave()).retrieve().body(String.class)).path("data");
        String error = datos.path("failure_message").isNull() ? null : datos.path("failure_message").asText(null);
        return new Video(datos.path("status").asText("pending"), datos.path("video_url").isNull() ? null : datos.path("video_url").asText(null),
                datos.path("duration").isNumber() ? datos.path("duration").asDouble() : null, error);
    }

    /** Descarga el archivo del video. */
    @Override
    public byte[] descargar(String url) {
        try {
            return RestClient.create().get().uri(java.net.URI.create(url)).retrieve().body(byte[].class);
        } catch (RestClientException e) {
            throw new IllegalArgumentException("Error! No se pudo descargar el video de HeyGen.");
        }
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Sube un archivo a HeyGen y devuelve el identificador del asset. */
    private String subirAsset(byte[] contenido, String archivo) {
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("file", new ByteArrayResource(contenido) {
            @Override
            public String getFilename() {
                return archivo;
            }
        });
        JsonNode respuesta = ejecutar(() -> cliente.post().uri("/v3/assets").header("x-api-key", clave()).contentType(MediaType.MULTIPART_FORM_DATA)
                .body(cuerpo).retrieve().body(String.class));
        String id = respuesta.path("data").path("asset_id").asText("");
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Error! HeyGen no pudo recibir el archivo.");
        }
        return id;
    }

    /** Lee la clave de la API, cargada por el administrador en Parámetros. */
    private String clave() {
        return parametroService.obtenerTexto(Parametro.HEYGEN_API_KEY).map(String::trim).filter(v -> !v.isEmpty())
                .orElseThrow(() -> new IllegalStateException("Error! Falta cargar la clave de la API de HeyGen en Parámetros."));
    }

    /** Ejecuta un pedido a HeyGen y lo devuelve como JSON; los errores de HeyGen se traducen a un mensaje para el usuario. */
    private JsonNode ejecutar(java.util.function.Supplier<String> pedido) {
        try {
            String cuerpo = pedido.get();
            return JSON.readTree(cuerpo == null ? "{}" : cuerpo);
        } catch (RestClientResponseException e) {
            String detalle = "";
            try {
                detalle = JSON.readTree(e.getResponseBodyAsString()).path("error").path("message").asText("");
            } catch (Exception ignorada) {
                // el cuerpo no es JSON: se usa el mensaje general
            }
            if (e.getStatusCode().value() == 401) {
                throw new IllegalStateException("Error! La clave de la API de HeyGen no es válida. Revisala en Parámetros.");
            }
            if (e.getStatusCode().value() == 429) {
                throw new IllegalArgumentException("Error! HeyGen recibió demasiados pedidos. Intentá nuevamente en unos segundos.");
            }
            throw new IllegalArgumentException("Error! " + (detalle.isEmpty() ? "HeyGen rechazó el pedido (" + e.getStatusCode().value() + ")." : detalle));
        } catch (RestClientException e) {
            REGISTRO.warn("No se pudo conectar con HeyGen: {}", e.getMessage());
            throw new IllegalArgumentException("Error! No se pudo conectar con HeyGen. Intentá nuevamente.");
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Error! HeyGen devolvió una respuesta inesperada.");
        }
    }

    private static String mensaje(JsonNode error, String porDefecto) {
        String texto = error.path("message").asText("");
        return texto.isEmpty() ? porDefecto : texto;
    }

    private static void dormir() {
        try {
            Thread.sleep(ESPERA_ENTRE_CONSULTAS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Error! La operación se interrumpió.");
        }
    }
}
