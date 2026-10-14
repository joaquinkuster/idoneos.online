package com.app.idoneos.servicio.PasarelaPago;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Conexión con la API de MODO (https://merchants.modo.com.ar/docs): autenticación con token, creación de solicitudes de
 * pago (payment requests) y consulta de su resultado.
 *
 * Todo lo que depende de la cuenta de MODO del cliente (credenciales, dirección de la API, códigos del gateway, etc.)
 * es un parámetro del sistema ({@code modo.*} en {@link Parametro}) y se lee en cada pedido: cuando el cliente
 * reemplaza las credenciales de prueba por las de su cuenta no hace falta reiniciar. Si falta algún parámetro, el pago
 * online no se puede iniciar y se informa que no está disponible.
 *
 * Si el parámetro {@code modo.url_publica} (la dirección pública del sistema) está cargado, cada solicitud lleva la
 * dirección del webhook ({@link #RUTA_WEBHOOK}) para que MODO avise los cambios de estado. Sin ella MODO no puede avisar
 * y el pago se confirma consultándolo.
 *
 * El token dura una semana y se reutiliza (el servicio de tokens admite pocos pedidos por hora); se pide uno nuevo si
 * cambian las credenciales o la dirección de la API. Todos los pedidos llevan el nombre del comercio en
 * {@code User-Agent}, que MODO exige.
 */
@Service
public class PasarelaPagoModoServicioImpl implements PasarelaPagoServicio {

    private static final Logger REGISTRO = LoggerFactory.getLogger(PasarelaPagoModoServicioImpl.class);

    private static final int LONGITUD_DESCRIPCION = 100;

    /** Margen antes del vencimiento del token para pedir uno nuevo. */
    private static final long MARGEN_TOKEN_SEGUNDOS = 600;

    /** Parámetros sin los cuales no se puede hablar con MODO. */
    private static final List<String> PARAMETROS_OBLIGATORIOS = List.of(Parametro.MODO_USUARIO, Parametro.MODO_CLAVE,
            Parametro.MODO_URL_API, Parametro.MODO_NOMBRE_COMERCIO, Parametro.MODO_PROCESSOR_CODE, Parametro.MODO_CC_CODE);

    private final RestClient cliente;
    private final ParametroServicio parametros;

    private volatile String token;
    private volatile Instant vencimientoDelToken = Instant.EPOCH;
    /** Credenciales y dirección con las que se pidió el token: si cambian, el token deja de servir. */
    private volatile String origenDelToken;

    public PasarelaPagoModoServicioImpl(RestClient.Builder constructor, ParametroServicio parametros) {
        this.cliente = constructor.build();
        this.parametros = parametros;
    }

    // ---------------------------------------------------------------- Lecturas

    @Override
    public Resultado consultarPago(String idSolicitud) {
        String urlApi = urlApi();
        String nombreComercio = requerido(Parametro.MODO_NOMBRE_COMERCIO);
        try {
            JsonNode respuesta = conToken(() -> cliente.get().uri(urlApi + "/v2/payment-requests/{id}/data", idSolicitud)
                    .header("User-Agent", nombreComercio).header("Authorization", "Bearer " + token)
                    .retrieve().body(JsonNode.class));
            return interpretar(respuesta);
        } catch (RestClientException e) {
            throw new IllegalStateException("No se pudo consultar el pago en MODO", e);
        }
    }

    /**
     * Interpreta la respuesta de la consulta de un pago (público para poder probar cada caso).
     *
     * @param respuesta El JSON que devolvió MODO.
     * @return El resultado del pago.
     */
    Resultado interpretar(JsonNode respuesta) {
        JsonNode transaccion = respuesta.path("transaction_data");
        String estado = texto(respuesta, "status");
        String estadoDelPago = texto(transaccion, "status");
        Solicitud solicitud = new Solicitud(texto(respuesta, "id"), texto(respuesta, "qr"), texto(respuesta, "deeplink"));
        boolean aprobado = "ACCEPTED".equals(estado) || "APPROVED".equals(estadoDelPago);
        boolean rechazado = "REJECTED".equals(estado) || "REJECTED".equals(estadoDelPago);
        if (!aprobado && !rechazado) {
            return Resultado.pendiente(solicitud);
        }
        String nombre = String.join(" ", textoONada(transaccion, "first_name"), textoONada(transaccion, "last_name")).trim();
        JsonNode detalle = transaccion.path("payment_method_detail");
        LocalDateTime fechaPago = null;
        String pagadoEl = texto(transaccion, "paid_at");
        if (pagadoEl != null) {
            fechaPago = LocalDateTime.ofInstant(Instant.parse(pagadoEl), ZoneId.systemDefault());
        }
        return new Resultado(aprobado ? Estado.APROBADO : Estado.RECHAZADO, solicitud, describirMedio(transaccion, detalle),
                texto(detalle, "last_digits"), nombre.isEmpty() ? null : nombre, texto(transaccion, "dni"),
                texto(transaccion, "reference_code"), aprobado ? null : motivoDelError(transaccion), fechaPago);
    }

    @Override
    public float montoMinimo() {
        return parametros.obtenerDecimal(Parametro.MODO_MONTO_MINIMO, Parametro.MODO_MONTO_MINIMO_POR_DEFECTO);
    }

    @Override
    public String scriptModal() {
        return parametros.obtenerTexto(Parametro.MODO_URL_SCRIPT_MODAL).filter(v -> !v.isBlank()).orElse(null);
    }

    // ---------------------------------------------------------------- Creaciones

    @Override
    public Solicitud crearSolicitud(String idExterno, float monto, String descripcion, String nombre, String correo,
            String dni) {
        List<String> faltantes = parametrosFaltantes();
        if (!faltantes.isEmpty()) {
            REGISTRO.error("No se puede crear la solicitud de pago: faltan los parámetros de MODO {}", faltantes);
            throw new IllegalArgumentException(
                    "Error! El pago online no está disponible en este momento: falta configurar el medio de pago.");
        }
        String urlApi = urlApi();
        String nombreComercio = requerido(Parametro.MODO_NOMBRE_COMERCIO);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("description", descripcion.length() > LONGITUD_DESCRIPCION
                ? descripcion.substring(0, LONGITUD_DESCRIPCION) : descripcion);
        cuerpo.put("amount", BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP));
        cuerpo.put("currency", "ARS");
        cuerpo.put("cc_code", requerido(Parametro.MODO_CC_CODE));
        cuerpo.put("processor_code", requerido(Parametro.MODO_PROCESSOR_CODE));
        cuerpo.put("external_intention_id", idExterno);
        String urlWebhook = urlWebhook();
        if (urlWebhook != null) {
            cuerpo.put("webhook_notification", urlWebhook);
        }
        cuerpo.put("customer", Map.of("full_name", nombre, "email", correo, "identification", dni));
        try {
            JsonNode respuesta = conToken(() -> cliente.post().uri(urlApi + "/v2/payment-requests/")
                    .header("User-Agent", nombreComercio).header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve().body(JsonNode.class));
            String id = texto(respuesta, "id");
            if (id == null) {
                throw new IllegalStateException("MODO no devolvió el identificador de la solicitud");
            }
            return new Solicitud(id, texto(respuesta, "qr"), texto(respuesta, "deeplink"));
        } catch (RestClientException | IllegalStateException e) {
            REGISTRO.error("No se pudo crear la solicitud de pago en MODO", e);
            throw new IllegalArgumentException("Error! No se pudo iniciar el pago con MODO. Intentá nuevamente en unos minutos.");
        }
    }

    private String describirMedio(JsonNode transaccion, JsonNode detalle) {
        String medio = texto(transaccion, "payment_method");
        if ("ACCOUNT".equals(medio)) {
            String banco = texto(detalle, "bank_name");
            return "Cuenta bancaria" + (banco == null ? "" : " " + banco);
        }
        String tipo = texto(detalle, "type");
        String marca = texto(detalle, "issuer_name");
        StringBuilder texto = new StringBuilder("Tarjeta");
        if ("CREDIT".equals(tipo)) {
            texto.append(" de crédito");
        } else if ("DEBIT".equals(tipo)) {
            texto.append(" de débito");
        }
        if (marca != null) {
            texto.append(' ').append(marca.substring(0, 1).toUpperCase()).append(marca.substring(1));
        }
        return medio == null && tipo == null && marca == null ? null : texto.toString();
    }

    private String motivoDelError(JsonNode transaccion) {
        JsonNode error = transaccion.path("operation_error");
        String mensaje = texto(error, "message");
        return mensaje != null ? mensaje : (texto(error, "original_error") != null ? texto(error, "original_error")
                : "El pago fue rechazado.");
    }

    /** Los parámetros obligatorios que no están cargados o están vacíos. */
    private List<String> parametrosFaltantes() {
        List<String> faltantes = new ArrayList<>();
        for (String clave : PARAMETROS_OBLIGATORIOS) {
            if (parametros.obtenerTexto(clave).filter(valor -> !valor.isBlank()).isEmpty()) {
                faltantes.add(clave);
            }
        }
        return faltantes;
    }

    private String requerido(String clave) {
        return parametros.obtenerTexto(clave).map(String::trim).filter(valor -> !valor.isEmpty())
                .orElseThrow(() -> new IllegalStateException("Falta el parámetro «" + clave + "» de MODO"));
    }

    /** La dirección de la API (preproducción o producción), sin barra final. */
    private String urlApi() {
        return requerido(Parametro.MODO_URL_API).replaceAll("/+$", "");
    }

    /** La dirección del webhook, o {@code null} si el sistema no tiene una dirección pública a la que MODO pueda llegar. */
    private String urlWebhook() {
        return parametros.obtenerTexto(Parametro.MODO_URL_PUBLICA).map(String::trim).filter(valor -> !valor.isEmpty())
                .map(url -> url.replaceAll("/+$", "") + RUTA_WEBHOOK).orElse(null);
    }

    /** Ejecuta el pedido con un token vigente; si MODO lo rechaza por vencido, pide uno nuevo y reintenta una vez. */
    private <T> T conToken(java.util.function.Supplier<T> pedido) {
        asegurarToken(false);
        try {
            return pedido.get();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() != 401) {
                throw e;
            }
            asegurarToken(true);
            return pedido.get();
        }
    }

    private synchronized void asegurarToken(boolean forzar) {
        String urlApi = urlApi();
        String usuario = requerido(Parametro.MODO_USUARIO);
        String clave = requerido(Parametro.MODO_CLAVE);
        String origen = String.join("|", urlApi, usuario, clave);
        if (!forzar && token != null && origen.equals(origenDelToken) && Instant.now().isBefore(vencimientoDelToken)) {
            return;
        }
        JsonNode respuesta = cliente.post().uri(urlApi + "/v2/stores/companies/token")
                .header("User-Agent", requerido(Parametro.MODO_NOMBRE_COMERCIO)).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", usuario, "password", clave)).retrieve().body(JsonNode.class);
        String nuevo = texto(respuesta, "access_token");
        if (nuevo == null) {
            throw new IllegalStateException("MODO no devolvió el token de acceso");
        }
        long segundos = respuesta.path("expires_in").asLong(3600);
        token = nuevo;
        origenDelToken = origen;
        vencimientoDelToken = Instant.now().plusSeconds(Math.max(60, segundos - MARGEN_TOKEN_SEGUNDOS));
    }

    private static String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo == null ? null : nodo.get(campo);
        return valor == null || valor.isNull() || valor.asText().isBlank() ? null : valor.asText();
    }

    private static String textoONada(JsonNode nodo, String campo) {
        String valor = texto(nodo, campo);
        return valor == null ? "" : valor;
    }
}
