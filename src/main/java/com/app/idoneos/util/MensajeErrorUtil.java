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

    /** Motivo cuando el bloqueo lo causa el curso al que pertenece el registro (programa, cronograma, tema, categoría). */
    public static final String MOTIVO_CURSO_PUBLICADO = "el curso tiene cohortes publicadas en el catálogo";

    /** Motivo cuando el registro es el propio curso. */
    public static final String MOTIVO_PUBLICADO = "tiene cohortes publicadas en el catálogo";

    /** Por qué lo publicado no se toca: es el compromiso de la empresa con los alumnos. */
    public static final String COMPROMISO_PUBLICADO = "Lo publicado es un compromiso de la empresa con los alumnos que lo consultan o ya se inscribieron, "
            + "y modificarlo afectaría lo que ellos tuvieron en cuenta al inscribirse.";

    /** Versión corta del compromiso, para el aviso que acompaña a un formulario consultable. */
    public static final String COMPROMISO_PUBLICADO_CORTO = "Lo publicado es un compromiso de la empresa con los alumnos que lo consultan o ya se inscribieron.";

    private static final String OTRO_DISENO = "Si necesitás otro diseño, solicitá al administrador la baja de la cohorte para crearla de nuevo.";

    /** Cierre del mensaje de error de un curso: lo único que se puede modificar y cómo cambiar el diseño. */
    public static final String CIERRE_CURSO = "Solo podés modificar el precio, la portada y el equipo docente. " + OTRO_DISENO;

    /** Cierre del mensaje de error de un programa, cronograma, tema o categoría. */
    public static final String CIERRE_DISENO = OTRO_DISENO;

    /** Cierre del mensaje de error de una cohorte publicada. */
    public static final String CIERRE_COHORTE = "Si necesitás otro diseño, solicitá al administrador su baja para crearla de nuevo.";

    /** Aviso que acompaña al formulario consultable de un curso publicado (campos bloqueados). */
    public static final String AVISO_CURSO = COMPROMISO_PUBLICADO_CORTO + " Podés consultar todos sus datos; solo el precio, la portada y el equipo docente se pueden modificar. " + OTRO_DISENO;

    /** Aviso que acompaña al formulario consultable de un programa, tema o categoría de un curso publicado. */
    public static final String AVISO_DISENO = COMPROMISO_PUBLICADO_CORTO + " Podés consultar todos sus datos. " + OTRO_DISENO;

    /** Aviso que acompaña al formulario consultable de una cohorte publicada. */
    public static final String AVISO_COHORTE = COMPROMISO_PUBLICADO_CORTO + " Podés consultar todos sus datos. " + CIERRE_COHORTE;

    /**
     * Arma el mensaje de error cuando algo no se puede modificar porque el curso tiene cohortes publicadas en el catálogo. Es el único
     * mensaje de este tipo de toda la aplicación (curso, programa, cronograma, cohorte, tema y categoría).
     *
     * @param tipo   El tipo de registro con su artículo, por ejemplo "el curso" o "el cronograma del programa".
     * @param nombre El nombre del registro.
     * @param motivo {@link #MOTIVO_PUBLICADO} o {@link #MOTIVO_CURSO_PUBLICADO} (o el de la cohorte).
     * @param cierre {@link #CIERRE_CURSO}, {@link #CIERRE_DISENO} o {@link #CIERRE_COHORTE}.
     * @return El mensaje listo para mostrar.
     */
    public static String bloqueoPorPublicacion(String tipo, String nombre, String motivo, String cierre) {
        return armarAccionBloqueada("modificar", tipo, nombre, motivo, COMPROMISO_PUBLICADO + " " + cierre);
    }

    /** Título del aviso de un formulario consultable: «El curso no se puede modificar: tiene cohortes publicadas en el catálogo.» */
    public static String tituloAviso(String sujeto, String motivo) {
        return sujeto + " no se puede modificar: " + motivo + ".";
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
        return detalle + " y el programa del curso quedaría incompleto";
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
