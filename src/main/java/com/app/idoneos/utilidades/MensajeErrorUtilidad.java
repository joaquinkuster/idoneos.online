package com.app.idoneos.utilidades;

/**
 * Mensajes estándar que se muestran al usuario, con un criterio único para toda la aplicación:
 * <ul>
 * <li>Cantidades con su sustantivo bien escrito (nunca «curso(s)») y bajas bloqueadas con la misma estructura.</li>
 * <li>Validaciones de campo: qué campo falló y qué se espera, sin información de más.
 * Formato: «Error! El DNI no es válido. Formato válido: solo números.»; largo: «Error! El título es demasiado largo.
 * Máximo: 50 caracteres.»; corto: «Error! La clave es demasiado corta. Mínimo: 8 caracteres.»</li>
 * </ul>
 * El sujeto se pasa con su artículo («El título», «Las observaciones») para que el mensaje concuerde en género y número.
 */
public final class MensajeErrorUtilidad {

    private MensajeErrorUtilidad() {
    }


    /**
     * Escribe una cantidad con su sustantivo en singular o plural: "1 curso activo asociado" o
     * "3 cursos activos asociados".
     *
     * @param cantidad La cantidad.
     * @param singular El texto para una unidad.
     * @param plural   El texto para cualquier otra cantidad.
     * @return La cantidad seguida del texto que corresponde.
     */
    public static String cantidad(long cantidad, String singular, String plural) {
        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }

    /**
     * Arma el mensaje de una baja que no se puede realizar, con la estructura
     * {@code Error! No se puede dar de baja {tipo} «{nombre}»: {motivo}. {qué hacer}}.
     *
     * @param tipo    El tipo de registro con su artículo, por ejemplo "la categoría" o "el curso".
     * @param nombre  El nombre del registro.
     * @param motivo  Por qué no se puede, sin punto final, por ejemplo "tiene 2 cursos activos asociados".
     * @param queHacer El paso que puede dar el usuario (con su punto final); vacío si no hay ninguno posible.
     * @return El mensaje listo para mostrar.
     */
    public static String bajaBloqueada(String tipo, String nombre, String motivo, String queHacer) {
        String mensaje = "Error! No se puede dar de baja " + tipo + " «" + nombre + "»: " + motivo + ".";
        return queHacer == null || queHacer.isBlank() ? mensaje : mensaje + " " + queHacer;
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
