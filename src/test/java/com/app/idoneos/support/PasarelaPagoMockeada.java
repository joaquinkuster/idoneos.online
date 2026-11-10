package com.app.idoneos.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.app.idoneos.service.pasarelapago.PasarelaPagoService;

/** Configura el mock de la pasarela de pago en las pruebas: los tests no hablan con MODO. */
public class PasarelaPagoMockeada {

    private static final String URL_CHECKOUT = "https://site.modo.test/checkout";
    private static final float MONTO_MINIMO = 30f;

    private final Map<String, PasarelaPagoService.Solicitud> solicitudes = new ConcurrentHashMap<>();
    private final Map<String, PasarelaPagoService.Resultado> resultados = new ConcurrentHashMap<>();
    /** Lo que dura una solicitud de pago en las pruebas: el plazo por defecto (10 minutos). */
    public static final int VIGENCIA_SEGUNDOS = 600;

    private final AtomicInteger referencias = new AtomicInteger();
    private volatile boolean esperarConfirmacion;
    private volatile boolean fallarAlConsultar;

    /** Configura el mock con el comportamiento normal: las solicitudes se crean y, al consultarlas. */
    public PasarelaPagoMockeada(PasarelaPagoService pasarela) {
        when(pasarela.montoMinimo()).thenReturn(MONTO_MINIMO);
        when(pasarela.vigenciaSolicitudSegundos()).thenReturn(VIGENCIA_SEGUNDOS);
        when(pasarela.urlCheckout(anyString(), anyString(), anyString())).thenAnswer(invocacion -> URL_CHECKOUT + "?id="
                + invocacion.getArgument(0) + "&callbackSuccess=" + invocacion.getArgument(1) + "&callback=" + invocacion.getArgument(2));
        when(pasarela.crearSolicitud(anyString(), anyFloat(), any(), any(), any(), any())).thenAnswer(invocacion -> {
            PasarelaPagoService.Solicitud solicitud = new PasarelaPagoService.Solicitud("SOL-" + UUID.randomUUID());
            solicitudes.put(solicitud.id(), solicitud);
            return solicitud;
        });
        when(pasarela.consultarPago(anyString())).thenAnswer(invocacion -> consultar(invocacion.getArgument(0)));
    }

    private PasarelaPagoService.Resultado consultar(String idSolicitud) {
        if (fallarAlConsultar) {
            throw new IllegalArgumentException("Error! No se pudo consultar el pago en MODO. Intentá nuevamente en unos minutos.");
        }
        PasarelaPagoService.Solicitud solicitud = solicitudes.get(idSolicitud);
        if (solicitud == null) {
            throw new IllegalStateException("Solicitud de pago desconocida: " + idSolicitud);
        }
        PasarelaPagoService.Resultado fijado = resultados.get(idSolicitud);
        if (fijado != null) {
            return fijado;
        }
        if (esperarConfirmacion) {
            return PasarelaPagoService.Resultado.pendiente(solicitud);
        }
        return new PasarelaPagoService.Resultado(PasarelaPagoService.Estado.APROBADO, solicitud, "Tarjeta de crédito Visa", "0008",
                "Pagador de Prueba", "30111222", "REF" + String.format("%05d", referencias.incrementAndGet()), null,
                LocalDateTime.now());
    }

    /** MODO no confirma nada: las solicitudes siguen pendientes hasta que se resuelvan. */
    public void dejarPendiente() {
        esperarConfirmacion = true;
    }

    /** MODO no responde: la consulta de cualquier pago falla. */
    public void fallarConsultas() {
        fallarAlConsultar = true;
    }

    /** MODO vuelve a aprobar los pagos cuando se los consulta (el comportamiento normal). */
    public void aprobarTodo() {
        esperarConfirmacion = false;
        fallarAlConsultar = false;
    }

    /** Fija el resultado que informa MODO para una solicitud. */
    public void resolver(String idSolicitud, PasarelaPagoService.Resultado resultado) {
        resultados.put(idSolicitud, resultado);
    }
}
