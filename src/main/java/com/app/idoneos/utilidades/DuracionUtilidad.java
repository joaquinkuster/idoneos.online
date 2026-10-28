package com.app.idoneos.utilidades;

/**
 * Duraciones. Las de calendario se guardan siempre en días y se expresan en la unidad que mejor las describe: meses
 * (30 días), semanas (7 días) o días. Las de trabajo (carga horaria) se guardan en minutos y se expresan en horas o minutos.
 */
public final class DuracionUtilidad {

    /** Días que tiene una semana. */
    public static final int DIAS_POR_SEMANA = 7;

    /** Días que se cuentan por mes. */
    public static final int DIAS_POR_MES = 30;

    /** Minutos que tiene una hora. */
    public static final int MINUTOS_POR_HORA = 60;

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
     * Escribe una carga horaria: horas si es múltiplo de 60 minutos, "1 h 30 min" si pasa de una hora y minutos en
     * cualquier otro caso.
     *
     * @param minutos La duración en minutos.
     * @return Por ejemplo "45 minutos", "2 horas" o "1 h 30 min".
     */
    public static String formatearMinutos(int minutos) {
        if (minutos > 0 && minutos % MINUTOS_POR_HORA == 0) {
            return MensajeErrorUtilidad.cantidad(minutos / MINUTOS_POR_HORA, "hora", "horas");
        }
        if (minutos > MINUTOS_POR_HORA) {
            return minutos / MINUTOS_POR_HORA + " h " + minutos % MINUTOS_POR_HORA + " min";
        }
        return MensajeErrorUtilidad.cantidad(minutos, "minuto", "minutos");
    }

    /**
     * Obtiene la unidad más grande que expresa una duración en días de forma exacta (para mostrarla en un formulario).
     *
     * @param dias La duración en días.
     * @return "meses", "semanas" o "dias".
     */
    public static String unidadEnDias(int dias) {
        if (dias > 0 && dias % DIAS_POR_MES == 0) {
            return "meses";
        }
        return dias > 0 && dias % DIAS_POR_SEMANA == 0 ? "semanas" : "dias";
    }

    /**
     * Obtiene la cantidad de una duración en días, en la unidad que devuelve {@link #unidadEnDias(int)}.
     *
     * @param dias La duración en días.
     * @return La cantidad.
     */
    public static int cantidadEnDias(int dias) {
        return switch (unidadEnDias(dias)) {
            case "meses" -> dias / DIAS_POR_MES;
            case "semanas" -> dias / DIAS_POR_SEMANA;
            default -> dias;
        };
    }

    /**
     * Obtiene la unidad que expresa una duración en minutos de forma exacta (para mostrarla en un formulario).
     *
     * @param minutos La duración en minutos.
     * @return "horas" si es múltiplo de 60 minutos y "minutos" en otro caso.
     */
    public static String unidadEnMinutos(int minutos) {
        return minutos > 0 && minutos % MINUTOS_POR_HORA == 0 ? "horas" : "minutos";
    }

    /**
     * Obtiene la cantidad de una duración en minutos, en la unidad que devuelve {@link #unidadEnMinutos(int)}.
     *
     * @param minutos La duración en minutos.
     * @return La cantidad.
     */
    public static int cantidadEnMinutos(int minutos) {
        return "horas".equals(unidadEnMinutos(minutos)) ? minutos / MINUTOS_POR_HORA : minutos;
    }

    /**
     * Convierte una cantidad expresada en minutos u horas a minutos.
     *
     * @param cantidad La cantidad en la unidad indicada.
     * @param unidad   "minutos" u "horas".
     * @return La duración en minutos.
     * @throws IllegalArgumentException si la unidad no es válida.
     */
    public static int aMinutos(int cantidad, String unidad) {
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
     * @param unidad   "dias", "semanas" o "meses".
     * @return La duración en días.
     * @throws IllegalArgumentException si la unidad no es válida.
     */
    public static int aDias(int cantidad, String unidad) {
        return switch (unidad) {
            case "dias" -> cantidad;
            case "semanas" -> Math.multiplyExact(cantidad, DIAS_POR_SEMANA);
            case "meses" -> Math.multiplyExact(cantidad, DIAS_POR_MES);
            default -> throw new IllegalArgumentException("Unidad de duración desconocida: " + unidad);
        };
    }
}
