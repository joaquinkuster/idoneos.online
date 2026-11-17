package com.app.idoneos.util;

import java.util.Locale;
import java.util.regex.Pattern;

/** Utilidad de los identificadores de un material bibliográfico: reconoce si es un ISBN, un ISSN o un DOI por su formato. */
public final class IdentificadorUtil {

    private IdentificadorUtil() {
    }

    /** Clase de identificador. */
    public enum Tipo {
        /** Número de un libro: diez cifras (la última puede ser X) o trece, con o sin guiones y espacios. */
        ISBN,
        /** Número de una publicación periódica, como una revista: cuatro cifras, un guion y cuatro más (la última puede ser X). */
        ISSN,
        /** Identificador de una obra concreta: empieza con 10., sigue el número del registrador, una barra y el resto. */
        DOI
    }

    private static final Pattern ISBN = Pattern.compile("^(?:\\d{9}[\\dX]|\\d{13})$");
    private static final Pattern ISSN = Pattern.compile("^\\d{4}-\\d{3}[\\dX]$");
    private static final Pattern DOI = Pattern.compile("^10\\.\\d{4,9}/\\S+$");

    /**
     * Reconoce la clase de un identificador por su formato.
     *
     * @param texto El identificador, tal como lo escribió el usuario.
     * @return La clase, o nulo si no es un ISBN, un ISSN ni un DOI.
     */
    public static Tipo tipoDe(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim().toUpperCase(Locale.ROOT);
        if (ISSN.matcher(limpio).matches()) {
            return Tipo.ISSN;
        }
        if (ISBN.matcher(limpio.replace("-", "").replace(" ", "")).matches()) {
            return Tipo.ISBN;
        }
        if (DOI.matcher(texto.trim()).matches()) {
            return Tipo.DOI;
        }
        return null;
    }

    /**
     * Indica si dos materiales no pueden tener el mismo identificador. El ISSN es de la revista y no de la obra, así que lo comparten todos sus
     * artículos.
     */
    public static boolean debeSerUnico(Tipo tipo) {
        return tipo != Tipo.ISSN;
    }

    /** Deja el identificador listo para compararlo: sin guiones ni espacios y en mayúscula. */
    public static String normalizar(String texto) {
        return texto == null ? null : texto.trim().replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
    }
}
