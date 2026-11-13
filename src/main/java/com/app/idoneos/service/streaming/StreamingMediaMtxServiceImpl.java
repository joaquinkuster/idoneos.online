package com.app.idoneos.service.streaming;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.ClaseEnVivo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Implementación de StreamingService con el servidor de medios MediaMTX. */
@Service
public class StreamingMediaMtxServiceImpl implements StreamingService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(StreamingMediaMtxServiceImpl.class);

    /** Usuario con el que OBS se presenta al servidor: lo que importa es la clave. */
    private static final String USUARIO_OBS = "docente";

    /** Horas que vale el acceso de un usuario a la transmisión desde que entra a la sala. */
    private static final long HORAS_DE_ACCESO = 8;

    /** Intentos de un segundo para esperar a que el servidor termine de escribir la grabación. */
    private static final int ESPERAS_DE_GRABACION = 15;

    private final SecureRandom azar = new SecureRandom();
    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    @Value("${idoneos.streaming.rtmp-url:rtmp://localhost:1935}")
    private String urlRtmp;

    @Value("${idoneos.streaming.hls-url:http://localhost:8888}")
    private String urlHls;

    @Value("${idoneos.streaming.api-url:http://localhost:9997}")
    private String urlApi;

    @Value("${idoneos.streaming.grabaciones:./streaming/grabaciones}")
    private String carpetaGrabaciones;

    @Value("${idoneos.streaming.secreto:idoneos-streaming-secreto-de-desarrollo}")
    private String secreto;

    // ----------------------------------------------------------------------
    // Conexión del docente
    // ----------------------------------------------------------------------

    /** Genera los datos de conexión de una clase, con una clave nueva. */
    @Override
    public DatosDeConexion generarConexion(ClaseEnVivo clase) {
        byte[] bytes = new byte[24];
        azar.nextBytes(bytes);
        String clave = HexFormat.of().formatHex(bytes);
        // OBS une el servidor y la clave con una barra: el camino va en la clave para que no quede una barra al final,
        // y MediaMTX lee el usuario y la contraseña de los parámetros que la siguen
        return new DatosDeConexion(urlRtmp, camino(clase) + "?user=" + USUARIO_OBS + "&pass=" + clave, resumirClave(clave));
    }

    /** Obtiene el resumen con el que se guarda una clave de transmisión. */
    @Override
    public String resumirClave(String clave) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(clave.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Corta la señal que está transmitiendo el docente de una clase. */
    @Override
    public void cortarSenal(ClaseEnVivo clase) {
        try {
            JsonNode camino = consultar("/v3/paths/get/" + camino(clase)).orElse(null);
            if (camino == null || !camino.path("ready").asBoolean(false)) {
                return;
            }
            JsonNode origen = camino.path("source");
            String tipo = origen.path("type").asText("");
            String id = origen.path("id").asText("");
            if (!id.isEmpty() && tipo.toLowerCase().startsWith("rtmp")) {
                pedir("POST", "/v3/" + (tipo.equals("rtmpsConn") ? "rtmpsconns" : "rtmpconns") + "/kick/" + id);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            REGISTRO.warn("No se pudo cortar la señal de la clase {}: {}", clase.getIdClaseEnVivo(), e.getMessage());
        }
    }

    /** Consulta si el servidor recibe la señal de una clase. */
    @Override
    public Optional<EstadoDeSenal> consultarSenal(ClaseEnVivo clase) {
        try {
            HttpResponse<String> respuesta = pedir("GET", "/v3/paths/get/" + camino(clase));
            if (respuesta.statusCode() == 404) {
                return Optional.of(new EstadoDeSenal(false, 0));
            }
            if (respuesta.statusCode() != 200) {
                return Optional.empty();
            }
            JsonNode camino = json.readTree(respuesta.body());
            return Optional.of(new EstadoDeSenal(camino.path("ready").asBoolean(false), camino.path("readers").size()));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Optional.empty();
        }
    }

    private Optional<JsonNode> consultar(String ruta) throws IOException, InterruptedException {
        HttpResponse<String> respuesta = pedir("GET", ruta);
        return respuesta.statusCode() == 200 ? Optional.of(json.readTree(respuesta.body())) : Optional.empty();
    }

    private HttpResponse<String> pedir(String metodo, String ruta) throws IOException, InterruptedException {
        String credenciales = Base64.getEncoder().encodeToString(("app:" + secreto).getBytes(StandardCharsets.UTF_8));
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(urlApi + ruta)).timeout(Duration.ofSeconds(3))
                .header("Authorization", "Basic " + credenciales)
                .method(metodo, HttpRequest.BodyPublishers.noBody()).build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }

    // ----------------------------------------------------------------------
    // Reproducción
    // ----------------------------------------------------------------------

    /** Firma el acceso de un usuario a la transmisión de una clase. */
    @Override
    public String firmarAcceso(ClaseEnVivo clase, int idUsuario) {
        long vence = Instant.now().plusSeconds(HORAS_DE_ACCESO * 3600).getEpochSecond();
        String datos = clase.getIdClaseEnVivo() + "." + idUsuario + "." + vence;
        return datos + "." + firma(datos);
    }

    /** Verifica un token de acceso a la transmisión. */
    @Override
    public boolean validarAcceso(String token, String camino) {
        if (token == null || camino == null) {
            return false;
        }
        String[] partes = token.split("\\.");
        if (partes.length != 4) {
            return false;
        }
        String datos = partes[0] + "." + partes[1] + "." + partes[2];
        if (!MessageDigest.isEqual(firma(datos).getBytes(StandardCharsets.UTF_8), partes[3].getBytes(StandardCharsets.UTF_8))) {
            return false;
        }
        try {
            return camino.equals("clase-" + partes[0]) && Long.parseLong(partes[2]) > Instant.now().getEpochSecond();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String firma(String datos) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(datos.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Obtiene la dirección donde el reproductor pide la transmisión de una clase. */
    @Override
    public String urlDeReproduccion(ClaseEnVivo clase) {
        return urlHls + "/" + camino(clase) + "/index.m3u8";
    }

    /** Obtiene el camino con que el servidor identifica la transmisión de una clase. */
    @Override
    public String camino(ClaseEnVivo clase) {
        return "clase-" + clase.getIdClaseEnVivo();
    }

    /** Verifica la clave de la API con la que el servidor se consulta a sí mismo. */
    @Override
    public boolean esClaveDeApi(String clave) {
        return clave != null && MessageDigest.isEqual(secreto.getBytes(StandardCharsets.UTF_8), clave.getBytes(StandardCharsets.UTF_8));
    }

    // ----------------------------------------------------------------------
    // Grabación
    // ----------------------------------------------------------------------

    /** Busca los archivos que el servidor grabó de una clase, esperando a que termine de escribirlos. */
    @Override
    public List<Path> buscarGrabaciones(ClaseEnVivo clase) {
        Path carpeta = Path.of(carpetaGrabaciones).resolve(camino(clase));
        if (!Files.isDirectory(carpeta)) {
            return List.of();
        }
        long tamanioAnterior = -1;
        for (int intento = 0; intento < ESPERAS_DE_GRABACION; intento++) {
            List<Path> archivos = listar(carpeta);
            long tamanio = archivos.stream().mapToLong(a -> a.toFile().length()).sum();
            if (!archivos.isEmpty() && tamanio == tamanioAnterior) {
                return archivos;
            }
            tamanioAnterior = tamanio;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return listar(carpeta);
    }

    private List<Path> listar(Path carpeta) {
        if (!Files.isDirectory(carpeta)) {
            return List.of();
        }
        try (Stream<Path> archivos = Files.list(carpeta)) {
            return archivos.filter(a -> a.getFileName().toString().endsWith(".mp4"))
                    .sorted(Comparator.comparing(a -> a.getFileName().toString())).toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /** Guarda una grabación del servidor en su destino, lista para reproducirse en el navegador. */
    @Override
    public void exportarGrabacion(Path origen, Path destino) throws IOException {
        Files.createDirectories(destino.getParent());
        // El servidor graba fragmentos sueltos sin índice, y el navegador tarda en cargarlos: ffmpeg los reordena sin recodificar
        try {
            Process proceso = new ProcessBuilder("ffmpeg", "-y", "-loglevel", "error", "-i", origen.toString(), "-c", "copy",
                    "-movflags", "+faststart", destino.toString()).redirectErrorStream(true).start();
            proceso.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            if (proceso.waitFor() == 0 && Files.size(destino) > 0) {
                return;
            }
        } catch (IOException e) {
            REGISTRO.warn("No se pudo usar ffmpeg, la grabación se publica sin reordenar: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Files.copy(origen, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /** Borra lo que el servidor grabó de una clase. */
    @Override
    public void borrarGrabaciones(ClaseEnVivo clase) {
        Path carpeta = Path.of(carpetaGrabaciones).resolve(camino(clase));
        if (!Files.isDirectory(carpeta)) {
            return;
        }
        try (Stream<Path> todo = Files.walk(carpeta)) {
            todo.sorted(Comparator.reverseOrder()).forEach(a -> a.toFile().delete());
        } catch (IOException e) {
            REGISTRO.warn("No se pudo borrar la grabación de la clase {}: {}", clase.getIdClaseEnVivo(), e.getMessage());
        }
    }
}
