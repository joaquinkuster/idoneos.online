package com.app.idoneos.util;

/** Utilidad de duraciones. */
public final class DuracionUtil {

    /** Días que tiene una semana. */
    public static final int DIAS_POR_SEMANA = 7;

    /** Días que se cuentan por mes. */
    public static final int DIAS_POR_MES = 30;

    /** Minutos que tiene una hora. */
    public static final int MINUTOS_POR_HORA = 60;

    /** Minutos que tiene un día. */
    public static final int MINUTOS_POR_DIA = 24 * MINUTOS_POR_HORA;

    private DuracionUtil() {
    }

    /**
     * Escribe una duración en la unidad más grande que la expresa en forma exacta.
     *
     * @param dias La duración en días.
     * @return Por ejemplo "1 día", "10 días", "3 semanas" o "2 meses".
     */
    public static String formatear(int dias) {
        if (dias > 0 && dias % DIAS_POR_MES == 0) {
            return MensajeErrorUtil.formatearCantidad(dias / DIAS_POR_MES, "mes", "meses");
        }
        if (dias > 0 && dias % DIAS_POR_SEMANA == 0) {
            return MensajeErrorUtil.formatearCantidad(dias / DIAS_POR_SEMANA, "semana", "semanas");
        }
        return MensajeErrorUtil.formatearCantidad(dias, "día", "días");
    }

    /**
     * Escribe una dedicación docente: horas si es múltiplo de 60 minutos.
     *
     * @param minutos La duración en minutos.
     * @return Por ejemplo "45 minutos", "2 horas" o "1 h 30 min".
     */
    public static String formatearMinutos(int minutos) {
        if (minutos > 0 && minutos % MINUTOS_POR_HORA == 0) {
            return MensajeErrorUtil.formatearCantidad(minutos / MINUTOS_POR_HORA, "hora", "horas");
        }
        if (minutos > MINUTOS_POR_HORA) {
            return minutos / MINUTOS_POR_HORA + " h " + minutos % MINUTOS_POR_HORA + " min";
        }
        return MensajeErrorUtil.formatearCantidad(minutos, "minuto", "minutos");
    }

    /**
     * Convierte una cantidad expresada en minutos u horas a minutos.
     *
     * @param cantidad La cantidad en la unidad indicada.
     * @param unidad "minutos" u "horas".
     * @return La duración en minutos.
     * @throws IllegalArgumentException si la unidad no es válida.
     */
    public static int convertirAMinutos(int cantidad, String unidad) {
        return switch (unidad) {
            case "minutos" -> cantidad;
            case "horas" -> Math.multiplyExact(cantidad, MINUTOS_POR_HORA);
            default -> throw new IllegalArgumentException("Unidad de duración desconocida: " + unidad);
        };
    }

    /**
     * Convierte una cantidad expresada en una unidad a días.
     *
     * @param cantidad La cantidad en la unidad indicada.
     * @param unidad "dias", "semanas" o "meses".
     * @return La duración en días.
     * @throws IllegalArgumentException si la unidad no es válida.
     */
    public static int convertirADias(int cantidad, String unidad) {
        return switch (unidad) {
            case "dias" -> cantidad;
            case "semanas" -> Math.multiplyExact(cantidad, DIAS_POR_SEMANA);
            case "meses" -> Math.multiplyExact(cantidad, DIAS_POR_MES);
            default -> throw new IllegalArgumentException("Unidad de duración desconocida: " + unidad);
        };
    }
}
