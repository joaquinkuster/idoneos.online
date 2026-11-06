package com.app.idoneos.util;

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
public final class MensajeErrorUtil {

    private MensajeErrorUtil() {
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
    public static String formatearCantidad(long cantidad, String singular, String plural) {
        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }

    /**
     * Lo que se aclara cuando no se puede modificar un programa, un tema o un cronograma que ya tiene inscripciones activas: es lo que el
     * alumno vio y pagó. Es la misma oración en las tres ABM.
     */
    public static final String LO_QUE_EL_ALUMNO_VIO_Y_PAGO = "Es lo que el alumno vio y pagó al inscribirse.";

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
    public static String armarBajaBloqueada(String tipo, String nombre, String motivo, String queHacer) {
        String mensaje = "Error! No se puede dar de baja " + tipo + " «" + nombre + "»: " + motivo + ".";
        return queHacer == null || queHacer.isBlank() ? mensaje : mensaje + " " + queHacer;
    }



    /**
     * No se puede hacer una acción sobre un registro (por ejemplo, ocultarlo).
     *
     * @param accion   La acción en infinitivo, por ejemplo "ocultar".
     * @param tipo     El tipo de registro con su artículo, por ejemplo "el material".
     * @param nombre   El nombre del registro.
     * @param motivo   Por qué no se puede, sin punto final.
     * @param queHacer El paso que puede dar el usuario (con su punto final); vacío si no hay ninguno posible.
     * @return El mensaje listo para mostrar.
     */
    public static String armarAccionBloqueada(String accion, String tipo, String nombre, String motivo, String queHacer) {
        String mensaje = "Error! No se puede " + accion + " " + tipo + " «" + nombre + "»: " + motivo + ".";
        return queHacer == null || queHacer.isBlank() ? mensaje : mensaje + " " + queHacer;
    }

    /**
     * Cierre común de los motivos por los que no se puede quitar, ocultar o cambiar contenido de un curso con cohortes activas: el
     * diseño del curso pasaría de completo a incompleto. Se usa en todos los pasos del diseño (temas, cronograma, material y
     * autoevaluaciones) para que el mensaje sea siempre el mismo.
     *
     * @param detalle Lo que pasaría con el registro, sin punto final (por ejemplo, "es la única clase grabada publicada del tema «X»").
     * @return El motivo completo, sin punto final.
     */
    public static String motivoDisenoIncompleto(String detalle) {
        return detalle + " y su curso tiene cohortes activas: el diseño del curso quedaría incompleto";
    }

    /** El valor no tiene el formato esperado. */
    public static String armarFormatoInvalido(String sujeto, String formatoValido) {
        return "Error! " + sujeto + " " + (esPlural(sujeto) ? "no son" : "no es") + " válid" + terminacion(sujeto)
                + ". Formato válido: " + formatoValido + ".";
    }

    /** El valor supera el largo máximo. */
    public static String armarDemasiadoLargo(String sujeto, Object maximo, String unidad) {
        return "Error! " + sujeto + " " + verbo(sujeto) + " demasiad" + terminacion(sujeto) + " larg" + terminacion(sujeto)
                + ". Máximo: " + maximo + " " + unidad + ".";
    }


    /** El archivo o la imagen supera el tamaño máximo. */
    public static String armarDemasiadoGrande(String sujeto, Object maximo, String unidad) {
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
