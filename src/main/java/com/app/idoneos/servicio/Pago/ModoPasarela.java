package com.app.idoneos.servicio.Pago;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Conexión con la API de MODO (https://merchants.modo.com.ar/docs): autenticación con token, creación de solicitudes de
 * pago (payment requests) y consulta de su resultado. Con las credenciales de prueba funciona contra el ambiente de
 * preproducción, sin mover dinero real.
 *
 * El token dura una semana y se reutiliza (el servicio de tokens admite pocos pedidos por hora). Todos los pedidos
 * llevan el nombre del comercio en {@code User-Agent}, que MODO exige.
 */
@Service
@ConditionalOnProperty(name = "idoneos.modo.simulado", havingValue = "false", matchIfMissing = true)
public class ModoPasarela implements PasarelaPago {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ModoPasarela.class);

    /** Monto mínimo que admite MODO con el gateway Decidir. */
    private static final float MONTO_MINIMO = 30f;

    private static final int LONGITUD_DESCRIPCION = 100;

    /** Margen antes del vencimiento del token para pedir uno nuevo. */
    private static final long MARGEN_TOKEN_SEGUNDOS = 600;

    private final RestClient cliente;
    private final String usuario;
    private final String clave;
    private final String nombreComercio;
    private final String processorCode;
    private final String ccCode;
    private final String scriptModal;
    private final String urlWebhook;

    private volatile String token;
    private volatile Instant vencimientoDelToken = Instant.EPOCH;

    public ModoPasarela(RestClient.Builder constructor,
            @Value("${idoneos.modo.url-base:https://merchants.preprod.playdigital.com.ar}") String urlBase,
            @Value("${idoneos.modo.usuario:}") String usuario, @Value("${idoneos.modo.clave:}") String clave,
            @Value("${idoneos.modo.nombre-comercio:Idoneos Online}") String nombreComercio,
            @Value("${idoneos.modo.processor-code:P1019}") String processorCode,
            @Value("${idoneos.modo.cc-code:1CSI}") String ccCode,
            @Value("${idoneos.modo.script-modal:https://ecommerce-modal.preprod.modo.com.ar/bundle.js}") String scriptModal,
            @Value("${idoneos.modo.webhook-url:}") String urlWebhook) {
        this.cliente = constructor.baseUrl(urlBase).build();
        this.usuario = usuario;
        this.clave = clave;
        this.nombreComercio = nombreComercio;
        this.processorCode = processorCode;
        this.ccCode = ccCode;
        this.scriptModal = scriptModal;
        this.urlWebhook = urlWebhook;
    }

    @Override
    public Solicitud crearSolicitud(String idExterno, float monto, String descripcion, String nombre, String correo,
            String dni) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("description", descripcion.length() > LONGITUD_DESCRIPCION
                ? descripcion.substring(0, LONGITUD_DESCRIPCION) : descripcion);
        cuerpo.put("amount", BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP));
        cuerpo.put("currency", "ARS");
        cuerpo.put("cc_code", ccCode);
        cuerpo.put("processor_code", processorCode);
        cuerpo.put("external_intention_id", idExterno);
        if (urlWebhook != null && !urlWebhook.isBlank()) {
            cuerpo.put("webhook_notification", urlWebhook);
        }
        cuerpo.put("customer", Map.of("full_name", nombre, "email", correo, "identification", dni));
        try {
            JsonNode respuesta = conToken(() -> cliente.post().uri("/v2/payment-requests/")
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

    @Override
    public Resultado consultarPago(String idSolicitud) {
        try {
            JsonNode respuesta = conToken(() -> cliente.get().uri("/v2/payment-requests/{id}/data", idSolicitud)
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

    @Override
    public boolean esSimulada() {
        return false;
    }

    @Override
    public float montoMinimo() {
        return MONTO_MINIMO;
    }

    @Override
    public String scriptModal() {
        return scriptModal;
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
        if (!forzar && token != null && Instant.now().isBefore(vencimientoDelToken)) {
            return;
        }
        if (usuario.isBlank() || clave.isBlank()) {
            throw new IllegalStateException("Faltan las credenciales de MODO (idoneos.modo.usuario y idoneos.modo.clave)");
        }
        JsonNode respuesta = cliente.post().uri("/v2/stores/companies/token").header("User-Agent", nombreComercio)
                .contentType(MediaType.APPLICATION_JSON).body(Map.of("username", usuario, "password", clave))
                .retrieve().body(JsonNode.class);
        String nuevo = texto(respuesta, "access_token");
        if (nuevo == null) {
            throw new IllegalStateException("MODO no devolvió el token de acceso");
        }
        long segundos = respuesta.path("expires_in").asLong(3600);
        token = nuevo;
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
