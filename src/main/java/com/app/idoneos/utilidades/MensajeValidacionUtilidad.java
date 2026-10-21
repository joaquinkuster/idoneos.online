package com.app.idoneos.utilidades;

/**
 * Mensajes de validación con un criterio único: qué campo falló y qué se espera, sin información de más.
 * <ul>
 * <li>Formato: «Error! El DNI no es válido. Formato válido: solo números.»</li>
 * <li>Largo: «Error! El título es demasiado largo. Máximo: 50 caracteres.»</li>
 * <li>Corto: «Error! La clave es demasiado corta. Mínimo: 8 caracteres.»</li>
 * </ul>
 * El sujeto se pasa con su artículo («El título», «Las observaciones») para que el mensaje concuerde en género y número.
 */
public final class MensajeValidacionUtilidad {

    private MensajeValidacionUtilidad() {
    }

    /** El valor no tiene el formato esperado. */
    public static String formatoInvalido(String sujeto, String formatoValido) {
        return "Error! " + sujeto + " " + (esPlural(sujeto) ? "no son" : "no es") + " válid" + terminacion(sujeto)
                + ". Formato válido: " + formatoValido + ".";
    }

    /** El valor supera el largo máximo. */
    public static String demasiadoLargo(String sujeto, Object maximo, String unidad) {
        return "Error! " + sujeto + " " + verbo(sujeto) + " demasiad" + terminacion(sujeto) + " larg" + terminacion(sujeto)
                + ". Máximo: " + maximo + " " + unidad + ".";
    }

    /** El valor no llega al largo mínimo. */
    public static String demasiadoCorto(String sujeto, Object minimo, String unidad) {
        return "Error! " + sujeto + " " + verbo(sujeto) + " demasiad" + terminacion(sujeto) + " cort" + terminacion(sujeto)
                + ". Mínimo: " + minimo + " " + unidad + ".";
    }

    /** El archivo o la imagen supera el tamaño máximo. */
    public static String demasiadoGrande(String sujeto, Object maximo, String unidad) {
        return "Error! " + sujeto + " " + verbo(sujeto) + " demasiad" + terminacion(sujeto) + " grande"
                + (esPlural(sujeto) ? "s" : "") + ". Máximo: " + maximo + " " + unidad + ".";
    }

    private static boolean esPlural(String sujeto) {
        return sujeto.startsWith("Los ") || sujeto.startsWith("Las ");
    }

    private static String verbo(String sujeto) {
        return esPlural(sujeto) ? "son" : "es";
    }

    /** Terminación del adjetivo según el artículo: o, a, os, as. */
    private static String terminacion(String sujeto) {
        boolean femenino = sujeto.startsWith("La ") || sujeto.startsWith("Las ");
        return (femenino ? "a" : "o") + (esPlural(sujeto) ? "s" : "");
    }
}
