package com.app.idoneos.servicio.Pago;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Plataforma de pagos simulada, para correr el sistema y las pruebas sin conexión a MODO ni celular
 * ({@code idoneos.modo.simulado=true}).
 *
 * Reproduce el contrato de MODO: crea solicitudes con identificador y QR y, al consultarlas, las resuelve. Igual que en
 * el ambiente de pruebas de MODO, el monto decide el rechazo: los montos con centavos entre 25,55 y 25,74 (y 25,90)
 * simulan los errores de la tabla de rechazos de MODO; cualquier otro monto se aprueba. Las pruebas pueden retener la
 * confirmación ({@link #setEsperarConfirmacion}) o fijar el resultado de una solicitud ({@link #resolver}).
 */
@Service
@ConditionalOnProperty(name = "idoneos.modo.simulado", havingValue = "true")
public class PasarelaSimulada implements PasarelaPago {

    /** Errores de MODO que se simulan según los centavos del monto (25,55 a 25,74 y 25,90). */
    private static final Map<Integer, String> RECHAZOS = Map.ofEntries(
            Map.entry(2555, "SECURITY_CODE_ERROR"), Map.entry(2556, "DENIED_INVALID_INSTALLMENTS"),
            Map.entry(2557, "DENIED_UNAVAILABLE_INSTALLMENTS"), Map.entry(2558, "DENIED_OFFLINE_ISSUER"),
            Map.entry(2559, "DENIED_RETAIN_CARD"), Map.entry(2560, "DENIED_SYSTEM_ERROR"), Map.entry(2561, "DENIED"),
            Map.entry(2562, "DISABLED_CARD"), Map.entry(2563, "EXPIRED_CARD"), Map.entry(2564, "INSUFFICIENT_FUNDS"),
            Map.entry(2565, "INVALID_AMOUNT"), Map.entry(2566, "INVALID_CARD_EXPIRATION_DATE"),
            Map.entry(2567, "INVALID_CARD"), Map.entry(2568, "INVALID_MERCHANT"), Map.entry(2569, "LIMIT_EXCEEDED"),
            Map.entry(2570, "MAX_INSTALLMENTS_EXCEEDED"), Map.entry(2571, "NOT_CURRENT_CARD"),
            Map.entry(2572, "PENDING_AUTHORIZATION"), Map.entry(2574, "TRANSACTION_ERROR"), Map.entry(2590, "DENIED_05"));

    private final Map<String, Float> montos = new ConcurrentHashMap<>();
    private final Map<String, Resultado> resultados = new ConcurrentHashMap<>();
    private final AtomicInteger referencias = new AtomicInteger();
    private volatile boolean esperarConfirmacion;

    @Override
    public Solicitud crearSolicitud(String idExterno, float monto, String descripcion, String nombre, String correo,
            String dni) {
        Solicitud solicitud = new Solicitud("SIM-" + UUID.randomUUID(), "SIMULADO-QR-" + idExterno,
                "https://simulado.modo.local/pagar/");
        montos.put(solicitud.id(), monto);
        return solicitud;
    }

    @Override
    public Resultado consultarPago(String idSolicitud) {
        Float monto = montos.get(idSolicitud);
        if (monto == null) {
            throw new IllegalStateException("Solicitud de pago desconocida: " + idSolicitud);
        }
        Solicitud solicitud = new Solicitud(idSolicitud, "SIMULADO-QR", "https://simulado.modo.local/pagar/");
        Resultado fijado = resultados.get(idSolicitud);
        if (fijado != null) {
            return fijado;
        }
        if (esperarConfirmacion) {
            return Resultado.pendiente(solicitud);
        }
        String rechazo = RECHAZOS.get(Math.round(monto * 100));
        if (rechazo != null) {
            return new Resultado(Estado.RECHAZADO, solicitud, "Tarjeta de crédito Visa", "0008", "Pagador Simulado",
                    "30111222", null, rechazo, null);
        }
        return new Resultado(Estado.APROBADO, solicitud, "Tarjeta de crédito Visa", "0008", "Pagador Simulado",
                "30111222", "SIM" + String.format("%05d", referencias.incrementAndGet()), null, LocalDateTime.now());
    }

    /**
     * Fija el resultado de una solicitud (para las pruebas).
     *
     * @param idSolicitud La solicitud.
     * @param resultado   Lo que informará la plataforma al consultarla.
     */
    public void resolver(String idSolicitud, Resultado resultado) {
        resultados.put(idSolicitud, resultado);
    }

    /**
     * Retiene la confirmación: las solicitudes quedan pendientes hasta que se resuelvan (para las pruebas).
     *
     * @param esperar {@code true} para que las consultas devuelvan pendiente.
     */
    public void setEsperarConfirmacion(boolean esperar) {
        this.esperarConfirmacion = esperar;
    }

    /** Vuelve la plataforma al comportamiento normal (aprueba salvo los montos de rechazo). */
    public void reiniciar() {
        resultados.clear();
        montos.clear();
        esperarConfirmacion = false;
    }

    @Override
    public boolean esSimulada() {
        return true;
    }

    @Override
    public float montoMinimo() {
        return 30f;
    }

    @Override
    public String scriptModal() {
        return null;
    }
}
