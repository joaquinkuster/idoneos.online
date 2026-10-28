package com.app.idoneos.utilidades;

/**
 * Duraciones de calendario. Se guardan siempre en días y se expresan en la unidad que mejor las describe:
 * meses (30 días), semanas (7 días) o días.
 */
public final class DuracionUtilidad {

    /** Días que tiene una semana. */
    public static final int DIAS_POR_SEMANA = 7;

    /** Días que se cuentan por mes. */
    public static final int DIAS_POR_MES = 30;

    private DuracionUtilidad() {
    }

    /**
     * Escribe una duración en la unidad más grande que la expresa en forma exacta: meses si es múltiplo de 30 días,
     * semanas si es múltiplo de 7 y días en cualquier otro caso.
     *
     * @param dias La duración en días.
     * @return Por ejemplo "1 día", "10 días", "3 semanas" o "2 meses".
     */
    public static String formatear(int dias) {
        if (dias > 0 && dias % DIAS_POR_MES == 0) {
            return MensajeErrorUtilidad.cantidad(dias / DIAS_POR_MES, "mes", "meses");
        }
        if (dias > 0 && dias % DIAS_POR_SEMANA == 0) {
            return MensajeErrorUtilidad.cantidad(dias / DIAS_POR_SEMANA, "semana", "semanas");
        }
        return MensajeErrorUtilidad.cantidad(dias, "día", "días");
    }

    /**
     * Convierte una cantidad expresada en una unidad a días.
     *
     * @param cantidad La cantidad en la unidad indicada.
     * @param unidad   "dias", "semanas" o "meses".
     * @return La duración en días.
     * @throws IllegalArgumentException si la unidad no es válida.
     */
    public static int aDias(int cantidad, String unidad) {
        return switch (unidad) {
            case "dias" -> cantidad;
            case "semanas" -> cantidad * DIAS_POR_SEMANA;
            case "meses" -> cantidad * DIAS_POR_MES;
            default -> throw new IllegalArgumentException("Unidad de duración desconocida: " + unidad);
        };
    }
}
