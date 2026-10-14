package com.app.idoneos.soporte;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.app.idoneos.servicio.PasarelaPago.PasarelaPagoServicio;

/**
 * Configura el mock de la pasarela de pago en las pruebas: los tests no hablan con MODO (haría falta red, credenciales
 * y un celular para pagar), así que se declara qué responde MODO en cada caso. El pago real se prueba a mano.
 *
 * Por defecto MODO aprueba los pagos cuando se los consulta; {@link #dejarPendiente()} retiene la confirmación y
 * {@link #resolver} fija el resultado de una solicitud (por ejemplo, un rechazo con su motivo).
 */
public class PasarelaPagoMockeada {

    private static final String SCRIPT_MODAL = "https://modo.test/bundle.js";
    private static final float MONTO_MINIMO = 30f;

    private final Map<String, PasarelaPagoServicio.Solicitud> solicitudes = new ConcurrentHashMap<>();
    private final Map<String, PasarelaPagoServicio.Resultado> resultados = new ConcurrentHashMap<>();
    private final AtomicInteger referencias = new AtomicInteger();
    private volatile boolean esperarConfirmacion;

    /**
     * Configura el mock con el comportamiento normal: las solicitudes se crean y, al consultarlas, MODO las aprueba.
     *
     * @param pasarela El mock de la pasarela (declarado en la prueba con {@code @MockitoBean}).
     */
    public PasarelaPagoMockeada(PasarelaPagoServicio pasarela) {
        when(pasarela.montoMinimo()).thenReturn(MONTO_MINIMO);
        when(pasarela.scriptModal()).thenReturn(SCRIPT_MODAL);
        when(pasarela.crearSolicitud(anyString(), anyFloat(), any(), any(), any(), any())).thenAnswer(invocacion -> {
            PasarelaPagoServicio.Solicitud solicitud = new PasarelaPagoServicio.Solicitud("SOL-" + UUID.randomUUID(),
                    "QR-" + invocacion.getArgument(0), "https://modo.test/pagar/");
            solicitudes.put(solicitud.id(), solicitud);
            return solicitud;
        });
        when(pasarela.consultarPago(anyString())).thenAnswer(invocacion -> consultar(invocacion.getArgument(0)));
    }

    private PasarelaPagoServicio.Resultado consultar(String idSolicitud) {
        PasarelaPagoServicio.Solicitud solicitud = solicitudes.get(idSolicitud);
        if (solicitud == null) {
            throw new IllegalStateException("Solicitud de pago desconocida: " + idSolicitud);
        }
        PasarelaPagoServicio.Resultado fijado = resultados.get(idSolicitud);
        if (fijado != null) {
            return fijado;
        }
        if (esperarConfirmacion) {
            return PasarelaPagoServicio.Resultado.pendiente(solicitud);
        }
        return new PasarelaPagoServicio.Resultado(PasarelaPagoServicio.Estado.APROBADO, solicitud, "Tarjeta de crédito Visa", "0008",
                "Pagador de Prueba", "30111222", "REF" + String.format("%05d", referencias.incrementAndGet()), null,
                LocalDateTime.now());
    }

    /** MODO no confirma nada: las solicitudes siguen pendientes hasta que se resuelvan. */
    public void dejarPendiente() {
        esperarConfirmacion = true;
    }

    /** MODO vuelve a aprobar los pagos cuando se los consulta (el comportamiento normal). */
    public void aprobarTodo() {
        esperarConfirmacion = false;
    }

    /**
     * Fija el resultado que informa MODO para una solicitud.
     *
     * @param idSolicitud La solicitud.
     * @param resultado   Lo que informará MODO al consultarla.
     */
    public void resolver(String idSolicitud, PasarelaPagoServicio.Resultado resultado) {
        resultados.put(idSolicitud, resultado);
    }
}
