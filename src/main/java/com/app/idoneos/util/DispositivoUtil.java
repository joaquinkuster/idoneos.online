package com.app.idoneos.util;

import jakarta.servlet.http.HttpServletRequest;

/** Utilidad de dispositivos. Obtiene la dirección IP y describe el dispositivo desde el que llega un pedido. */
public final class DispositivoUtil {

    private static final int LONGITUD_MAXIMA_IP = 45;

    private static final int LONGITUD_MAXIMA_DISPOSITIVO = 255;

    private DispositivoUtil() {
    }

    /**
     * Obtiene la dirección IP desde la que llega el pedido, teniendo en cuenta un servidor intermedio (proxy) si lo hay.
     *
     * @param request El pedido recibido.
     * @return La dirección IP, de hasta 45 caracteres.
     */
    public static String obtenerIp(HttpServletRequest request) {
        String reenviada = request.getHeader("X-Forwarded-For");
        String ip = reenviada != null && !reenviada.isBlank() ? reenviada.split(",")[0].trim() : request.getRemoteAddr();
        if (ip == null || ip.isBlank()) {
            ip = "desconocida";
        }
        return ip.length() > LONGITUD_MAXIMA_IP ? ip.substring(0, LONGITUD_MAXIMA_IP) : ip;
    }

    /**
     * Describe el dispositivo a partir del encabezado User-Agent, con el navegador y el sistema operativo.
     *
     * @param userAgent El encabezado User-Agent del pedido.
     * @return Un texto como "Chrome en Windows", o "Dispositivo desconocido" si no se reconoce.
     */
    public static String describir(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Dispositivo desconocido";
        }
        String agente = userAgent.toLowerCase();
        String navegador = agente.contains("edg/") ? "Edge"
                : agente.contains("opr/") || agente.contains("opera") ? "Opera"
                : agente.contains("firefox") ? "Firefox"
                : agente.contains("chrome") || agente.contains("crios") ? "Chrome"
                : agente.contains("safari") ? "Safari" : "Navegador";
        String sistema = agente.contains("iphone") ? "iPhone"
                : agente.contains("ipad") ? "iPad"
                : agente.contains("android") ? "Android"
                : agente.contains("windows") ? "Windows"
                : agente.contains("mac os") ? "macOS"
                : agente.contains("linux") ? "Linux" : "sistema desconocido";
        String descripcion = navegador + " en " + sistema;
        return descripcion.length() > LONGITUD_MAXIMA_DISPOSITIVO ? descripcion.substring(0, LONGITUD_MAXIMA_DISPOSITIVO) : descripcion;
    }
}
