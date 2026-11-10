package com.app.idoneos.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utilidad de motivos de rechazo de pagos. Traduce al español los motivos que informa la plataforma de pagos. */
public final class MotivoPagoUtil {

    /** Un código de la plataforma: mayúsculas, números y guiones bajos, solo o seguido de ":" y un texto. */
    private static final Pattern CODIGO = Pattern.compile("^\\s*([A-Z][A-Z0-9_]+)\\s*(?::.*)?$", Pattern.DOTALL);

    private static final Map<String, String> TRADUCCIONES = Map.ofEntries(
            Map.entry("INSUFFICIENT_FUNDS", "Fondos insuficientes."),
            Map.entry("CARD_EXPIRED", "La tarjeta está vencida."),
            Map.entry("EXPIRED_CARD", "La tarjeta está vencida."),
            Map.entry("INVALID_CARD", "La tarjeta no es válida."),
            Map.entry("CARD_BLOCKED", "La tarjeta está bloqueada."),
            Map.entry("CARD_DISABLED", "La tarjeta está deshabilitada."),
            Map.entry("INVALID_SECURITY_CODE", "El código de seguridad de la tarjeta es incorrecto."),
            Map.entry("INVALID_CVV", "El código de seguridad de la tarjeta es incorrecto."),
            Map.entry("LIMIT_EXCEEDED", "Se superó el límite de la tarjeta o de la cuenta."),
            Map.entry("EXCEEDED_LIMIT", "Se superó el límite de la tarjeta o de la cuenta."),
            Map.entry("DO_NOT_HONOR", "El banco emisor no autorizó la operación."),
            Map.entry("REJECTED_BY_ISSUER", "El banco emisor rechazó la operación."),
            Map.entry("REJECTED_BY_BANK", "El banco rechazó la operación."),
            Map.entry("SUSPECTED_FRAUD", "La operación fue rechazada por seguridad."),
            Map.entry("FRAUD_SUSPECTED", "La operación fue rechazada por seguridad."),
            Map.entry("INVALID_AMOUNT", "El monto de la operación no es válido."),
            Map.entry("TRANSACTION_NOT_ALLOWED", "La operación no está permitida para esta tarjeta."),
            Map.entry("USER_CANCELLED", "El pago fue cancelado por el usuario."),
            Map.entry("CANCELLED_BY_USER", "El pago fue cancelado por el usuario."),
            Map.entry("PAYMENT_EXPIRED", "El pago venció antes de completarse."),
            Map.entry("EXPIRED", "El pago venció antes de completarse."),
            Map.entry("TIMEOUT", "El banco no respondió a tiempo."));

    private MotivoPagoUtil() {
    }

    /**
     * Traduce el motivo de rechazo.
     *
     * @param motivo El motivo tal como lo informa la plataforma (puede ser {@code null}).
     * @return El motivo en español, o el mismo texto si no es un código de la plataforma ({@code null} si no había motivo).
     */
    public static String traducir(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return motivo;
        }
        Matcher coincidencia = CODIGO.matcher(motivo);
        if (!coincidencia.matches()) {
            return motivo;
        }
        String codigo = coincidencia.group(1);
        String traduccion = TRADUCCIONES.get(codigo);
        return traduccion != null ? traduccion : "El pago fue rechazado por el medio de pago (" + codigo + ").";
    }
}
