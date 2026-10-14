package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.servicio.PasarelaPago.PasarelaPagoModoServicioImpl;
import com.app.idoneos.servicio.PasarelaPago.PasarelaPagoServicio;

/**
 * Pruebas de la conexión con la API de MODO contra un servidor simulado: autenticación con token (que se reutiliza),
 * creación de solicitudes y consulta de su resultado, con los cuerpos que devuelve MODO. Los parámetros de la cuenta de
 * MODO se leen de {@link ParametroServicio} (acá, un mock respaldado por un mapa que cada prueba puede cambiar).
 */
class PasarelaPagoModoServicioImplTest {

    private static final String BASE = "https://modo.test";
    private static final String TOKEN = "{\"access_token\":\"TOKEN-1\",\"token_type\":\"Bearer\",\"expires_in\":604800}";
    private static final String SOLICITUD = "{\"id\":\"b3c97b14\",\"qr\":\"000201QR\",\"deeplink\":\"https://modo.test/pagar/\","
            + "\"status\":\"CREATED\",\"expiration_date\":\"2026-10-13T08:58:39.773Z\"}";

    private MockRestServiceServer servidor;

    /** Los parámetros de MODO que lee la pasarela: cada prueba puede cambiarlos. */
    private Map<String, String> valores;

    private PasarelaPagoModoServicioImpl pasarela(String usuario, String clave, String urlPublica) {
        valores = new HashMap<>();
        valores.put(Parametro.MODO_URL_API, BASE);
        valores.put(Parametro.MODO_USUARIO, usuario);
        valores.put(Parametro.MODO_CLAVE, clave);
        valores.put(Parametro.MODO_NOMBRE_COMERCIO, "Idoneos Online");
        valores.put(Parametro.MODO_PROCESSOR_CODE, "P1019");
        valores.put(Parametro.MODO_CC_CODE, "1CSI");
        valores.put(Parametro.MODO_URL_SCRIPT_MODAL, "https://modo.test/bundle.js");
        valores.put(Parametro.MODO_URL_PUBLICA, urlPublica);
        ParametroServicio parametros = mock(ParametroServicio.class);
        when(parametros.obtenerTexto(anyString())).thenAnswer(invocacion -> Optional.ofNullable(valores.get(invocacion.<String>getArgument(0))));
        when(parametros.obtenerDecimal(eq(Parametro.MODO_MONTO_MINIMO), anyFloat()))
                .thenAnswer(invocacion -> valores.containsKey(Parametro.MODO_MONTO_MINIMO)
                        ? Float.parseFloat(valores.get(Parametro.MODO_MONTO_MINIMO)) : invocacion.<Float>getArgument(1));
        RestClient.Builder constructor = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(constructor).build();
        return new PasarelaPagoModoServicioImpl(constructor, parametros);
    }

    private void esperarToken(String token) {
        servidor.expect(requestTo(BASE + "/v2/stores/companies/token")).andExpect(method(HttpMethod.POST))
                .andExpect(header("User-Agent", "Idoneos Online")).andExpect(jsonPath("$.username").value("usuario"))
                .andExpect(jsonPath("$.password").value("clave")).andRespond(withSuccess(token, MediaType.APPLICATION_JSON));
    }

    @BeforeEach
    void limpiar() {
        servidor = null;
    }

    @Test
    @DisplayName("Crea la solicitud con los datos que exige MODO y devuelve su identificador, QR y enlace")
    void creaLaSolicitud() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/")).andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer TOKEN-1")).andExpect(header("User-Agent", "Idoneos Online"))
                .andExpect(jsonPath("$.description").value("Curso: Finanzas"))
                .andExpect(jsonPath("$.amount").value(72000.0)).andExpect(jsonPath("$.currency").value("ARS"))
                .andExpect(jsonPath("$.cc_code").value("1CSI")).andExpect(jsonPath("$.processor_code").value("P1019"))
                .andExpect(jsonPath("$.external_intention_id").value("IDN-123"))
                .andExpect(jsonPath("$.customer.full_name").value("Lucía Fernández"))
                .andExpect(jsonPath("$.customer.email").value("lucia@correo.com"))
                .andExpect(jsonPath("$.customer.identification").value("40123456"))
                .andExpect(jsonPath("$.webhook_notification").doesNotExist())
                .andRespond(withSuccess(SOLICITUD, MediaType.APPLICATION_JSON));

        PasarelaPagoServicio.Solicitud solicitud = modo.crearSolicitud("IDN-123", 72000f, "Curso: Finanzas", "Lucía Fernández",
                "lucia@correo.com", "40123456");

        assertEquals("b3c97b14", solicitud.id());
        assertEquals("000201QR", solicitud.qr());
        assertEquals("https://modo.test/pagar/", solicitud.deeplink());
        servidor.verify();
    }

    @Test
    @DisplayName("Con la dirección pública configurada envía la del webhook, y recorta la descripción a 100 caracteres")
    void enviaElWebhook() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "https://idoneos.test/"); // con barra final: se normaliza
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/"))
                .andExpect(jsonPath("$.webhook_notification").value("https://idoneos.test/pago/webhook"))
                .andExpect(jsonPath("$.description").value("x".repeat(100)))
                .andRespond(withSuccess(SOLICITUD, MediaType.APPLICATION_JSON));

        modo.crearSolicitud("IDN-1", 100f, "x".repeat(150), "Ana Pérez", "ana@correo.com", "30111222");

        servidor.verify();
    }

    @Test
    @DisplayName("El token se reutiliza entre pedidos (MODO limita los pedidos de token)")
    void reutilizaElToken() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/")).andRespond(withSuccess(SOLICITUD, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(BASE + "/v2/payment-requests/")).andExpect(header("Authorization", "Bearer TOKEN-1"))
                .andRespond(withSuccess(SOLICITUD, MediaType.APPLICATION_JSON));

        modo.crearSolicitud("IDN-1", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222");
        modo.crearSolicitud("IDN-2", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222");

        servidor.verify(); // un solo pedido de token para los dos
    }

    @Test
    @DisplayName("Si MODO rechaza el token por vencido, pide uno nuevo y reintenta el pedido")
    void renuevaElTokenVencido() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/")).andExpect(header("Authorization", "Bearer TOKEN-1"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        esperarToken(TOKEN.replace("TOKEN-1", "TOKEN-2"));
        servidor.expect(requestTo(BASE + "/v2/payment-requests/")).andExpect(header("Authorization", "Bearer TOKEN-2"))
                .andRespond(withSuccess(SOLICITUD, MediaType.APPLICATION_JSON));

        assertEquals("b3c97b14", modo.crearSolicitud("IDN-1", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222").id());
        servidor.verify();
    }

    @Test
    @DisplayName("Un error de MODO al crear la solicitud se informa sin detalles internos")
    void errorAlCrearLaSolicitud() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/")).andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"monto invalido\"}"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> modo.crearSolicitud("IDN-1", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222"));
        assertTrue(error.getMessage().contains("No se pudo iniciar el pago con MODO"));
        assertFalse(error.getMessage().contains("monto invalido"));
    }

    @Test
    @DisplayName("Sin credenciales no se puede crear la solicitud y se informa el problema")
    void sinCredenciales() {
        PasarelaPagoModoServicioImpl modo = pasarela("", "", "");

        assertThrows(IllegalArgumentException.class,
                () -> modo.crearSolicitud("IDN-1", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222"));
        assertThrows(IllegalStateException.class, () -> modo.consultarPago("b3c97b14"));
    }

    @Test
    @DisplayName("Una solicitud recién creada se consulta como pendiente y trae su QR para retomarla")
    void consultaUnPagoPendiente() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/b3c97b14/data")).andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer TOKEN-1"))
                .andRespond(withSuccess(SOLICITUD.replace("}", ",\"transaction_data\":{},\"transactions\":[]}"), MediaType.APPLICATION_JSON));

        PasarelaPagoServicio.Resultado resultado = modo.consultarPago("b3c97b14");

        assertEquals(PasarelaPagoServicio.Estado.PENDIENTE, resultado.estado());
        assertEquals("000201QR", resultado.solicitud().qr());
    }

    @Test
    @DisplayName("Un pago con tarjeta aprobado trae el pagador, el medio, los últimos dígitos y la referencia")
    void consultaUnPagoAprobadoConTarjeta() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/b3c97b14/data")).andRespond(withSuccess("""
                {"id":"b3c97b14","qr":"000201QR","status":"ACCEPTED","deeplink":"https://modo.test/pagar/",
                 "transaction_data":{"reference_code":"QRYT7ZD0","first_name":"Lucas","last_name":"Barale","dni":"38784087",
                   "payment_method":"CARD","payment_method_detail":{"type":"CREDIT","issuer_name":"master","last_digits":"0008","bank_name":"BBVA"},
                   "operation_error":null,"paid_at":"2025-12-12T17:00:39.007Z","status":"APPROVED","amount":"100.000"}}
                """, MediaType.APPLICATION_JSON));

        PasarelaPagoServicio.Resultado resultado = modo.consultarPago("b3c97b14");

        assertEquals(PasarelaPagoServicio.Estado.APROBADO, resultado.estado());
        assertEquals("Lucas Barale", resultado.nombrePagador());
        assertEquals("38784087", resultado.dniPagador());
        assertEquals("Tarjeta de crédito Master", resultado.medioPago());
        assertEquals("0008", resultado.ultimosDigitos());
        assertEquals("QRYT7ZD0", resultado.referencia());
        assertNull(resultado.motivoError());
        assertNotNull(resultado.fechaPago());
    }

    @Test
    @DisplayName("Un pago con cuenta bancaria aprobado se describe con el banco")
    void consultaUnPagoAprobadoConCuenta() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/b3c97b14/data")).andRespond(withSuccess("""
                {"id":"b3c97b14","status":"ACCEPTED","transaction_data":{"reference_code":"NJD0XZ12","first_name":"Prueba",
                 "last_name":"Macro","dni":"23801244","payment_method":"ACCOUNT",
                 "payment_method_detail":{"type":"SAVINGS","bank_name":"Macro","last_digits":"6168"},"status":"APPROVED"}}
                """, MediaType.APPLICATION_JSON));

        PasarelaPagoServicio.Resultado resultado = modo.consultarPago("b3c97b14");

        assertEquals(PasarelaPagoServicio.Estado.APROBADO, resultado.estado());
        assertEquals("Cuenta bancaria Macro", resultado.medioPago());
        assertEquals("6168", resultado.ultimosDigitos());
    }

    @Test
    @DisplayName("Un pago rechazado trae el motivo que informa MODO")
    void consultaUnPagoRechazado() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/b3c97b14/data")).andRespond(withSuccess("""
                {"id":"b3c97b14","status":"REJECTED","transaction_data":{"first_name":"Lucas","last_name":"Barale","dni":"38784087",
                 "payment_method":"CARD","payment_method_detail":{"type":"DEBIT","issuer_name":"visa","last_digits":"5609"},
                 "operation_error":{"original_error":"INSUFFICIENT_FUNDS","message":"INSUFFICIENT_FUNDS: fondos insuficientes"},
                 "status":"REJECTED"}}
                """, MediaType.APPLICATION_JSON));

        PasarelaPagoServicio.Resultado resultado = modo.consultarPago("b3c97b14");

        assertEquals(PasarelaPagoServicio.Estado.RECHAZADO, resultado.estado());
        assertEquals("INSUFFICIENT_FUNDS: fondos insuficientes", resultado.motivoError());
        assertEquals("Tarjeta de débito Visa", resultado.medioPago());
    }

    @Test
    @DisplayName("Una falla de MODO al consultar se informa para que el pago siga pendiente")
    void fallaAlConsultar() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/b3c97b14/data")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(IllegalStateException.class, () -> modo.consultarPago("b3c97b14"));
    }

    @Test
    @DisplayName("Expone el monto mínimo y el script del modal")
    void datosDeLaPlataforma() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");

        assertEquals(30f, modo.montoMinimo());
        assertEquals("https://modo.test/bundle.js", modo.scriptModal());
    }

    @Test
    @DisplayName("Si el cliente cambia las credenciales, se pide un token nuevo con las credenciales nuevas")
    void cambiaLasCredenciales() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        // Las expectativas se declaran todas antes del primer pedido, en el orden en que ocurren
        esperarToken(TOKEN);
        servidor.expect(requestTo(BASE + "/v2/payment-requests/")).andExpect(header("Authorization", "Bearer TOKEN-1"))
                .andRespond(withSuccess(SOLICITUD, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(BASE + "/v2/stores/companies/token")).andExpect(jsonPath("$.username").value("cuenta-real"))
                .andExpect(jsonPath("$.password").value("clave-real"))
                .andRespond(withSuccess(TOKEN.replace("TOKEN-1", "TOKEN-2"), MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(BASE + "/v2/payment-requests/")).andExpect(header("Authorization", "Bearer TOKEN-2"))
                .andRespond(withSuccess(SOLICITUD, MediaType.APPLICATION_JSON));

        modo.crearSolicitud("IDN-1", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222");
        valores.put(Parametro.MODO_USUARIO, "cuenta-real");
        valores.put(Parametro.MODO_CLAVE, "clave-real");
        modo.crearSolicitud("IDN-2", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222");

        servidor.verify();
    }

    @Test
    @DisplayName("Si el cliente cambia la dirección de la API (de preproducción a producción), usa la nueva")
    void cambiaLaDireccionDeLaApi() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        valores.put(Parametro.MODO_URL_API, "https://produccion.modo.test/"); // con barra final: se normaliza
        servidor.expect(requestTo("https://produccion.modo.test/v2/stores/companies/token"))
                .andRespond(withSuccess(TOKEN, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo("https://produccion.modo.test/v2/payment-requests/"))
                .andRespond(withSuccess(SOLICITUD, MediaType.APPLICATION_JSON));

        modo.crearSolicitud("IDN-1", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222");

        servidor.verify();
    }

    @Test
    @DisplayName("Con un parámetro obligatorio sin cargar, el pago online no está disponible (sin salir a MODO)")
    void faltaUnParametro() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        valores.remove(Parametro.MODO_PROCESSOR_CODE);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> modo.crearSolicitud("IDN-1", 100f, "Prueba", "Ana Pérez", "ana@correo.com", "30111222"));

        assertTrue(error.getMessage().contains("no está disponible"));
        servidor.verify(); // no se hizo ningún pedido a MODO
    }

    @Test
    @DisplayName("El monto mínimo se toma del parámetro y, si no está cargado, es 30")
    void montoMinimoConfigurable() {
        PasarelaPagoModoServicioImpl modo = pasarela("usuario", "clave", "");
        assertEquals(30f, modo.montoMinimo());

        valores.put(Parametro.MODO_MONTO_MINIMO, "0.01");

        assertEquals(0.01f, modo.montoMinimo());
    }
}
