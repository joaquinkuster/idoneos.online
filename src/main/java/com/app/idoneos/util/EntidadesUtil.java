package com.app.idoneos.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Los datos que se pueden cambiar de una oración para armar una respuesta incorrecta difícil: un número («5 fases») o un nombre propio («Winston Royce»).
 * Una opción incorrecta es la misma oración con ese dato reemplazado por otro del mismo documento: tiene el mismo estilo y la misma estructura, y solo se
 * distingue por el dato. Los datos los reconoce el etiquetador de OpenNLP (números y nombres propios) y se reemplazan por otros del mismo tipo y la misma
 * forma, sacados del propio documento.
 */
public final class EntidadesUtil {

    private EntidadesUtil() {
    }

    /** Clase de dato. */
    public enum Tipo { NUMERO, NOMBRE, OPUESTO, PALABRA }

    /**
     * Un dato dentro de una oración.
     *
     * @param texto El dato, tal como está escrito.
     * @param desde Dónde empieza en la oración.
     * @param hasta Dónde termina en la oración.
     * @param tipo Si es un número o un nombre.
     * @param forma Lo que tiene que compartir un dato para poder reemplazar a este: los números, la misma cantidad de cifras y de separadores («9.999»); los
     *        nombres, la misma cantidad de palabras.
     */
    public record Entidad(String texto, int desde, int hasta, Tipo tipo, String forma) {
    }

    /**
     * Los datos de todo un documento, agrupados por forma: de ahí salen los reemplazos.
     *
     * @param frecuentes Los lemas que están en demasiadas oraciones del documento: son del tema o verbos de uso general y no sirven para distinguir una
     *        oración de otra, así que no se cambian ni reemplazan.
     */
    public record Indice(Map<String, Map<String, Set<String>>> valoresPorForma, Set<String> enMinuscula, Set<String> frecuentes) {
    }

    /** Un lema que está en más de esta parte de las oraciones del documento es frecuente. */
    private static final double PARTE_DE_ORACIONES_FRECUENTE = 0.15;

    /** Las palabras con menos letras que estas no se cambian: son casi todas de función. */
    private static final int LETRAS_MINIMAS_DE_UNA_PALABRA = 5;

    /**
     * Cuántas letras del final tiene que compartir una palabra para reemplazar a otra: la persona, el número y el tiempo del verbo («-ado», «-ían»), el
     * género y el número del adjetivo («-nal», «-ica»).
     */
    private static final int LETRAS_DEL_FINAL = 3;

    /** Cuántas letras del principio no pueden coincidir: dos formas de la misma palabra («identifica», «identificada») no son un cambio. */
    private static final int LETRAS_DE_LA_RAIZ = 5;

    /** La palabra que está justo antes de una posición de la oración, sin tildes y en minúscula, o vacío si no hay. */
    private static String previa(String oracion, int desde) {
        String antes = LogicaPreguntasAurea.normalizar(oracion.substring(0, desde));
        int espacio = antes.lastIndexOf(' ');
        return espacio < 0 ? antes : antes.substring(espacio + 1);
    }

    private static final Pattern NUMERO = Pattern.compile("^\\d+(?:[.,]\\d+)*$");
    private static final Pattern PALABRA = Pattern.compile("^\\p{L}[\\p{L}'-]*$");

    /**
     * Los datos de una oración, en el orden en que están.
     *
     * @param enMinuscula Las palabras que el documento escribe alguna vez en minúscula: no son nombres propios. El etiquetador toma por nombre propio a
     *        muchas palabras comunes con mayúscula («Ingeniería»), y cambiarlas rompe la oración.
     */
    public static List<Entidad> entidadesDe(String oracion, Set<String> enMinuscula) {
        GramaticaUtil.Analisis a = GramaticaUtil.analizar(oracion);
        List<Entidad> entidades = new ArrayList<>();
        int i = 0;
        while (i < a.palabras().length) {
            Tipo tipo = tipoDe(a, i, enMinuscula);
            if (tipo == null) {
                i++;
                continue;
            }
            int j = i;
            while (j + 1 < a.palabras().length && tipoDe(a, j + 1, enMinuscula) == tipo) {
                j++;
            }
            int desde = a.inicios()[i];
            int hasta = a.inicios()[j] + a.palabras()[j].length();
            String texto = oracion.substring(desde, hasta);
            String forma = tipo == Tipo.NUMERO ? "N:" + texto.replaceAll("\\d", "9")
                    : "P:" + (j - i + 1) + (a.palabras()[i].equals(a.palabras()[i].toUpperCase(Locale.ROOT)) ? "S" : "C");
            entidades.add(new Entidad(texto, desde, hasta, tipo, forma));
            i = j + 1;
        }
        return entidades;
    }

    /** Un número escrito con cifras, o un nombre propio (o una palabra con mayúscula que no abre la oración). Lo demás no es un dato. */
    private static Tipo tipoDe(GramaticaUtil.Analisis a, int i, Set<String> enMinuscula) {
        String palabra = a.palabras()[i];
        String categoria = a.categorias()[i];
        if (categoria.equals("NUM") && NUMERO.matcher(palabra).matches()) {
            return Tipo.NUMERO;
        }
        if (PALABRA.matcher(palabra).matches() && palabra.length() >= 3 && !enMinuscula.contains(LogicaPreguntasAurea.normalizar(palabra))
                && (categoria.equals("PROPN") || (categoria.equals("NOUN") && i > 0 && Character.isUpperCase(palabra.charAt(0))))) {
            return Tipo.NOMBRE;
        }
        return null;
    }

    /** Junta los datos de todas las oraciones de un documento. */
    public static Indice indexar(List<String> oraciones) {
        Set<String> enMinuscula = new HashSet<>();
        for (String oracion : oraciones) {
            for (String palabra : GramaticaUtil.analizar(oracion).palabras()) {
                if (Character.isLowerCase(palabra.charAt(0))) {
                    enMinuscula.add(LogicaPreguntasAurea.normalizar(palabra));
                }
            }
        }
        // Para cada forma, cada valor con las palabras que lo preceden en el documento: sirve para que un nombre solo reemplace a otro que se use igual
        Set<String> frecuentes = new HashSet<>();
        double limite = Math.log(1 / PARTE_DE_ORACIONES_FRECUENTE);
        GramaticaUtil.idfDeLemas(oraciones).forEach((lema, peso) -> {
            if (peso < limite) {
                frecuentes.add(lema);
            }
        });
        Indice indice = new Indice(new HashMap<>(), enMinuscula, frecuentes);
        for (String oracion : oraciones) {
            List<Entidad> entidades = new ArrayList<>(entidadesDe(oracion, enMinuscula));
            entidades.addAll(palabrasDe(oracion, frecuentes));
            for (Entidad e : entidades) {
                indice.valoresPorForma().computeIfAbsent(e.forma(), k -> new java.util.LinkedHashMap<>()).computeIfAbsent(e.texto(), k -> new HashSet<>())
                        .add(previa(oracion, e.desde()));
            }
        }
        return indice;
    }

    /**
     * Los verbos y adjetivos de una oración que se pueden cambiar por otros del documento: tienen que ser de uso poco frecuente en el documento y empezar
     * con minúscula. La forma es la categoría más las últimas letras, para que el reemplazo concuerde («identifica» por «controla», «distintos» por
     * «internos»).
     */
    public static List<Entidad> palabrasDe(String oracion, Set<String> frecuentes) {
        GramaticaUtil.Analisis a = GramaticaUtil.analizar(oracion);
        List<Entidad> palabras = new ArrayList<>();
        for (int i = 0; i < a.palabras().length; i++) {
            String palabra = a.palabras()[i];
            boolean verbo = a.categorias()[i].equals("VERB");
            if ((!verbo && !a.categorias()[i].equals("ADJ")) || palabra.length() < LETRAS_MINIMAS_DE_UNA_PALABRA || !PALABRA.matcher(palabra).matches()
                    || !Character.isLowerCase(palabra.charAt(0)) || frecuentes.contains(LogicaPreguntasAurea.normalizar(a.lemas()[i]))) {
                continue;
            }
            String normal = LogicaPreguntasAurea.normalizar(palabra);
            String forma = (verbo ? "V:" : "A:") + normal.substring(normal.length() - LETRAS_DEL_FINAL);
            palabras.add(new Entidad(palabra, a.inicios()[i], a.inicios()[i] + palabra.length(), Tipo.PALABRA, forma));
        }
        return palabras;
    }

    /** Las palabras de una oración que tienen un opuesto en la lista de {@link OpuestosUtil}. */
    public static List<Entidad> opuestosDe(String oracion) {
        GramaticaUtil.Analisis a = GramaticaUtil.analizar(oracion);
        List<Entidad> opuestos = new ArrayList<>();
        for (int i = 0; i < a.palabras().length; i++) {
            // Solo verbos, adjetivos y adverbios: «bajo» como preposición no tiene opuesto, y «todos» con un artículo detrás no admite «muy pocos»
            String categoria = a.categorias()[i];
            if ((categoria.equals("VERB") || categoria.equals("ADJ") || categoria.equals("ADV")) && !OpuestosUtil.de(a.palabras()[i]).isEmpty()) {
                opuestos.add(new Entidad(a.palabras()[i], a.inicios()[i], a.inicios()[i] + a.palabras()[i].length(), Tipo.OPUESTO,
                        "O:" + LogicaPreguntasAurea.normalizar(a.palabras()[i])));
            }
        }
        return opuestos;
    }

    /** Todo lo que se puede cambiar en una oración, de más a menos seguro: opuestos, números, nombres, verbos y adjetivos. */
    private static List<Entidad> candidatas(String oracion, Indice indice) {
        List<Entidad> todas = new ArrayList<>(opuestosDe(oracion));
        todas.addAll(entidadesDe(oracion, indice.enMinuscula()));
        todas.addAll(palabrasDe(oracion, indice.frecuentes()));
        return todas;
    }

    /**
     * Elige el dato de una oración que sirve para cambiar y armar las incorrectas: tiene que haber otros del mismo tipo y forma en el documento. Los
     * números van primero; entre los nombres, se queda con el más raro del documento (el que menos oraciones tienen), que es el que más distingue.
     *
     * @param idf El peso de cada lema en el documento, para elegir entre nombres.
     * @return El dato, o nulo si ninguno tiene reemplazos.
     */
    public static Entidad elegir(String oracion, Indice indice, Map<String, Double> idf) {
        Entidad mejor = null;
        double mejorPeso = -1;
        for (Entidad e : candidatas(oracion, indice)) {
            if (reemplazos(e, oracion, indice).isEmpty()) {
                continue;
            }
            // Primero los opuestos (la oración cambiada es falsa seguro), luego los números, después los nombres y al final los verbos y adjetivos
            double peso = switch (e.tipo()) {
                case OPUESTO -> 30000;
                case NUMERO -> 20000;
                case NOMBRE -> 10000 + idf.getOrDefault(LogicaPreguntasAurea.normalizar(e.texto()), 1.0);
                case PALABRA -> idf.getOrDefault(LogicaPreguntasAurea.normalizar(e.texto()), 1.0);
            };
            if (peso > mejorPeso) {
                mejor = e;
                mejorPeso = peso;
            }
        }
        return mejor;
    }

    /** Los datos del documento que pueden reemplazar a este: de la misma forma, distintos de él y que no estén ya en la oración. */
    public static List<String> reemplazos(Entidad entidad, String oracion, Indice indice) {
        String normalOracion = " " + LogicaPreguntasAurea.normalizar(oracion) + " ";
        String previa = previa(oracion, entidad.desde());
        List<String> candidatos = new ArrayList<>();
        if (entidad.tipo() == Tipo.OPUESTO) {
            for (String opuesto : OpuestosUtil.de(entidad.texto())) {
                if (!normalOracion.contains(" " + LogicaPreguntasAurea.normalizar(opuesto) + " ")) {
                    candidatos.add(opuesto);
                }
            }
            return candidatos;
        }
        String raiz = LogicaPreguntasAurea.normalizar(entidad.texto());
        raiz = raiz.substring(0, Math.min(LETRAS_DE_LA_RAIZ, raiz.length()));
        for (Map.Entry<String, Set<String>> par : indice.valoresPorForma().getOrDefault(entidad.forma(), Map.of()).entrySet()) {
            String valor = par.getKey();
            // Un nombre solo reemplaza a otro que en el documento aparezca después de la misma palabra («por Royce» y «por Boehm»): así no se cambia una
            // organización por una persona. Un número o una palabra pueden reemplazar a cualquier otra de su forma
            boolean seUsaIgual = entidad.tipo() != Tipo.NOMBRE || par.getValue().contains(previa);
            boolean otraPalabra = entidad.tipo() != Tipo.PALABRA || !LogicaPreguntasAurea.normalizar(valor).startsWith(raiz);
            if (seUsaIgual && otraPalabra && !valor.equalsIgnoreCase(entidad.texto())
                    && !normalOracion.contains(" " + LogicaPreguntasAurea.normalizar(valor) + " ")) {
                candidatos.add(valor);
            }
        }
        // Un número entero también se cambia por el siguiente y por el de cinco más: parecen un dato real y no están en el documento
        if (entidad.tipo() == Tipo.NUMERO && entidad.texto().matches("\\d{1,6}")) {
            long n = Long.parseLong(entidad.texto());
            for (long otro : new long[] { n + 1, n + 5 }) {
                String valor = String.valueOf(otro);
                if (!candidatos.contains(valor) && !normalOracion.contains(" " + valor + " ")) {
                    candidatos.add(valor);
                }
            }
        }
        return candidatos;
    }

    /** La oración con el dato cambiado por otro. */
    public static String cambiar(String oracion, Entidad entidad, String valor) {
        return oracion.substring(0, entidad.desde()) + valor + oracion.substring(entidad.hasta());
    }

    /**
     * Arma las oraciones incorrectas cambiando el dato por otros del documento. Se descarta la que ya está en el documento tal cual (sería verdadera) y la
     * que se repite.
     *
     * @param oraciones Todas las oraciones del documento, para comprobar que lo que se arma no existe.
     * @param cantidad Cuántas como máximo.
     */
    public static List<String> incorrectas(String oracion, Entidad entidad, Indice indice, List<String> oraciones, int cantidad) {
        Set<String> existentes = new HashSet<>();
        oraciones.forEach(o -> existentes.add(LogicaPreguntasAurea.normalizar(o)));
        List<String> valores = reemplazos(entidad, oracion, indice);
        Collections.shuffle(valores);
        List<String> incorrectas = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        for (String valor : valores) {
            String cambiada = EntidadesUtil.cambiar(oracion, entidad, valor);
            if (incorrectas.size() < cantidad && !existentes.contains(LogicaPreguntasAurea.normalizar(cambiada)) && vistas.add(LogicaPreguntasAurea.normalizar(cambiada))) {
                incorrectas.add(cambiada);
            }
        }
        return incorrectas;
    }

    /**
     * Arma las oraciones incorrectas cambiando, de a una por vez, lo que se pueda de la oración: un opuesto, un número, un nombre, un verbo o un adjetivo,
     * en ese orden. De cada cosa que se cambia salen como mucho dos oraciones, para que las incorrectas no cambien todas lo mismo.
     *
     * @param oraciones Todas las oraciones del documento, para comprobar que lo que se arma no existe.
     * @param cantidad Cuántas como máximo.
     * @return Las oraciones cambiadas; menos de las pedidas, o ninguna, si la oración no tiene nada que se pueda cambiar.
     */
    public static List<String> incorrectasPorCambio(String oracion, Indice indice, List<String> oraciones, int cantidad) {
        List<String> resultado = new ArrayList<>();
        for (Entidad entidad : candidatas(oracion, indice)) {
            if (resultado.size() >= cantidad) {
                break;
            }
            for (String cambiada : incorrectas(oracion, entidad, indice, oraciones, Math.min(2, cantidad - resultado.size()))) {
                if (!resultado.contains(cambiada)) {
                    resultado.add(cambiada);
                }
            }
        }
        return resultado.size() > cantidad ? resultado.subList(0, cantidad) : resultado;
    }

    /** Para describir un dato en un mensaje. */
    public static String descripcion(Entidad e) {
        return e.tipo().name().toLowerCase(Locale.ROOT) + " «" + e.texto() + "»";
    }
}
