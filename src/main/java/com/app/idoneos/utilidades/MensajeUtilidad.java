package com.app.idoneos.utilidades;

/**
 * Métodos auxiliares para armar los mensajes que se muestran al usuario con un formato único:
 * los plurales siempre bien escritos (nunca "curso(s)") y los mensajes de baja con la misma estructura.
 */
public class MensajeUtilidad {

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
}
