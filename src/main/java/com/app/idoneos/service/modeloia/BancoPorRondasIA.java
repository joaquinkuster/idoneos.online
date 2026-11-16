package com.app.idoneos.service.modeloia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.idoneos.util.Bm25Util;
import com.app.idoneos.util.EntidadesUtil;
import com.app.idoneos.util.GramaticaUtil;
import com.app.idoneos.util.LogicaPreguntasAurea;
import com.app.idoneos.util.LogicaPreguntasAurea.Opcion;
import com.app.idoneos.util.LogicaPreguntasAurea.Pregunta;
import com.app.idoneos.util.LogicaPreguntasAurea.Tipo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Camino simplificado para armar el banco, por rondas. En cada ronda se toman unas pocas oraciones objetivo (las más importantes del documento) y, en una
 * sola llamada y con unas oraciones parecidas como contexto, el modelo escribe una pregunta por oración. Es lo único que escribe el modelo: la respuesta
 * correcta es la oración objetivo, tal cual, y las incorrectas las arma el código, primero cambiando una palabra de la oración (un opuesto, un número,
 * un nombre, un verbo o un adjetivo), después con otras oraciones que comparten palabras de contenido con ella y, si no alcanzan, con oraciones al azar.
 * Se empieza por las oraciones que admiten tres cambios de palabra, y las cuatro opciones van sin el sujeto (la pregunta ya lo dice) cuando se le puede
 * quitar a la correcta.
 */
final class BancoPorRondasIA {

    private static final Logger REGISTRO = LoggerFactory.getLogger(BancoPorRondasIA.class);

    /** Cuántas rondas como máximo, mientras haya tiempo y falten preguntas. */
    private static final int RONDAS_MAXIMAS = 40;

    /**
     * Cuántas oraciones objetivo tiene cada ronda. Es una: con cinco, el modelo chico escribía una sola pregunta y a veces la asignaba a otra oración;
     * con una, cada pregunta sale de su oración y el tiempo por pregunta es el mismo.
     */
    private static final int OBJETIVOS_POR_RONDA = 1;

    /** Los tokens que puede escribir el modelo en una ronda: una pregunta corta. */
    private static final int TOKENS_POR_RONDA = 300;

    private static final int INCORRECTAS = 3;

    /** Cuántos caracteres como mínimo tiene que tener una pregunta, sin los signos: menos es un «¿?» vacío. */
    private static final int LARGO_MINIMO_DE_UNA_PREGUNTA = 10;

    /** Cuántos caracteres entran en el enunciado de una pregunta y en el texto de una opción en la base. */
    private static final int LARGO_MAXIMO = 255;

    /** Un texto de hasta esta cantidad de palabras de contenido se da por apoyado con una sola palabra compartida con el contexto; uno más largo necesita dos. */
    private static final int LEMAS_DE_UN_TEXTO_CORTO = 3;

    private static final String INSTRUCCIONES = """
            Recibís un contexto y una lista de oraciones numeradas de un texto de estudio.
            Para cada oración escribí una pregunta de examen, con signos ¿?, cuya respuesta sea exactamente lo que dice esa oración.
            La pregunta no repite la oración ni dice la respuesta, y no agrega datos ni siglas que no estén en la oración. Es una sola pregunta por oración.
            Usá el contexto solo para entender de qué habla cada oración.

            Ejemplo.
            Oraciones: 1. Las acciones representan una parte del capital de una empresa y dan derecho a dividendos.
            Salida: {"preguntas": [{"oracion": 1, "pregunta": "¿Qué representan las acciones y qué derecho dan?"}]}""";

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Ronda(List<PreguntaDeRonda> preguntas) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PreguntaDeRonda(int oracion, String pregunta) {
    }

    private final ModeloIAService modelo;

    /** Por qué se descartó cada pregunta de la ronda actual: motivo y cantidad. */
    private final Map<String, Integer> motivos = new java.util.LinkedHashMap<>();

    BancoPorRondasIA(ModeloIAService modelo) {
        this.modelo = modelo;
    }

    /**
     * Arma preguntas de opción única por rondas.
     *
     * @param cantidad Cuántas preguntas como máximo.
     * @param objetivos Las oraciones de las que se arman las preguntas, de la más a la menos importante.
     * @param todas Todas las oraciones del documento: de ahí salen el contexto y las opciones incorrectas.
     * @param usados Los enunciados ya usados, normalizados; se agregan los que se aceptan.
     * @param indice Los números, nombres y palabras del documento, de donde salen los cambios de palabra.
     * @param hayTiempo Dice si todavía se puede seguir con otra ronda.
     * @return Las preguntas aceptadas, hasta {@code cantidad}.
     */
    List<Pregunta> armar(int cantidad, List<String> objetivos, List<String> todas, Set<String> usados, EntidadesUtil.Indice indice, BooleanSupplier hayTiempo) {
        List<List<String>> lemasDeTodas = new ArrayList<>();
        todas.forEach(oracion -> lemasDeTodas.add(GramaticaUtil.lemasDeContenido(oracion)));
        List<Pregunta> aceptadas = new ArrayList<>();
        objetivos = conCambiosPrimero(objetivos, todas, indice);
        int siguiente = 0;
        for (int ronda = 1; ronda <= RONDAS_MAXIMAS && aceptadas.size() < cantidad && siguiente < objetivos.size() && hayTiempo.getAsBoolean(); ronda++) {
            int cuantas = Math.min(OBJETIVOS_POR_RONDA, objetivos.size() - siguiente);
            List<String> deEstaRonda = objetivos.subList(siguiente, siguiente + cuantas);
            siguiente += cuantas;
            String contexto = BancoEnUnaLlamadaIA.contextoDe(deEstaRonda, todas, lemasDeTodas);
            Ronda respuesta;
            try {
                respuesta = modelo.generarObjeto(INSTRUCCIONES, pedido(deEstaRonda, contexto), Ronda.class, EsquemasIA.ronda(cuantas), TOKENS_POR_RONDA);
            } catch (ModeloIAException e) {
                REGISTRO.warn("El modelo no respondió la ronda {}: {}", ronda, e.getMessage());
                continue;
            }
            if (respuesta == null || respuesta.preguntas() == null) {
                continue;
            }
            motivos.clear();
            Set<String> lemasDelContexto = new HashSet<>(GramaticaUtil.conjuntoDeLemas(contexto));
            deEstaRonda.forEach(oracion -> lemasDelContexto.addAll(GramaticaUtil.conjuntoDeLemas(oracion)));
            Set<Integer> yaTienePregunta = new HashSet<>();
            for (PreguntaDeRonda candidata : respuesta.preguntas()) {
                if (aceptadas.size() >= cantidad) {
                    break;
                }
                if (candidata == null || candidata.oracion() < 1 || candidata.oracion() > cuantas || !yaTienePregunta.add(candidata.oracion())) {
                    descartar("oración inexistente o repetida");
                    continue;
                }
                String objetivo = deEstaRonda.get(candidata.oracion() - 1);
                String enunciado = enunciadoValido(candidata.pregunta(), objetivo, lemasDelContexto, usados);
                if (enunciado == null) {
                    continue;
                }
                // Las cuatro opciones van sin el sujeto, o ninguna: si solo las incorrectas lo perdieran, la que lo conserva sería la correcta
                String sinSujeto = GramaticaUtil.sinSujeto(objetivo);
                String correcta = sinSujeto != null ? sinSujeto : objetivo;
                List<String> porCambio = EntidadesUtil.incorrectasPorCambio(correcta, indice, todas, INCORRECTAS, palabrasDe(enunciado));
                // Sin tres cambios que no repitan la pregunta, mejor otra oración, mientras queden; si no, completan otras oraciones del documento
                if (porCambio.size() < INCORRECTAS && siguiente < objetivos.size()) {
                    descartar("menos de tres cambios sin repetir palabras de la pregunta");
                    continue;
                }
                List<String> incorrectas = incorrectasDe(objetivo, correcta, sinSujeto != null, porCambio, todas, lemasDeTodas);
                if (incorrectas.size() < INCORRECTAS) {
                    descartar("no se pudieron armar tres opciones incorrectas");
                    continue;
                }
                List<Opcion> opciones = new ArrayList<>();
                opciones.add(new Opcion(correcta, true));
                incorrectas.forEach(texto -> opciones.add(new Opcion(texto, false)));
                Collections.shuffle(opciones);
                usados.add(LogicaPreguntasAurea.normalizar(enunciado));
                // También queda marcada la oración, para que las reglas no armen otra pregunta con ella
                usados.add(LogicaPreguntasAurea.normalizar(objetivo));
                aceptadas.add(new Pregunta(enunciado, opciones, Tipo.UNICA_RESPUESTA));
            }
            REGISTRO.info("Ronda {}: {} oraciones objetivo, {} preguntas escritas; {} de {} aceptadas hasta ahora. Descartadas por motivo: {}", ronda, cuantas,
                    respuesta.preguntas().size(), aceptadas.size(), cantidad, motivos.isEmpty() ? "ninguna" : motivos);
        }
        return aceptadas;
    }

    /**
     * Pone primero las oraciones de las que se pueden armar las tres incorrectas cambiando una palabra, y al final las que no: con estas las incorrectas
     * serían otras oraciones del documento, más fáciles de descartar. Dentro de cada grupo se mantiene el orden de importancia.
     */
    private static List<String> conCambiosPrimero(List<String> objetivos, List<String> todas, EntidadesUtil.Indice indice) {
        List<String> conCambios = new ArrayList<>();
        List<String> sinCambios = new ArrayList<>();
        for (String objetivo : objetivos) {
            String sinSujeto = GramaticaUtil.sinSujeto(objetivo);
            String base = sinSujeto != null ? sinSujeto : objetivo;
            (EntidadesUtil.incorrectasPorCambio(base, indice, todas, INCORRECTAS).size() >= INCORRECTAS ? conCambios : sinCambios).add(objetivo);
        }
        REGISTRO.info("De {} oraciones objetivo, {} permiten tres incorrectas por cambio de palabra.", objetivos.size(), conCambios.size());
        conCambios.addAll(sinCambios);
        return conCambios;
    }

    /** Las palabras y los lemas de un texto, sin tildes y en minúscula. */
    private static Set<String> palabrasDe(String texto) {
        GramaticaUtil.Analisis a = GramaticaUtil.analizar(texto);
        Set<String> palabras = new HashSet<>();
        for (int i = 0; i < a.palabras().length; i++) {
            palabras.add(LogicaPreguntasAurea.normalizar(a.palabras()[i]));
            palabras.add(LogicaPreguntasAurea.normalizar(a.lemas()[i]));
        }
        return palabras;
    }

    private static String pedido(List<String> objetivos, String contexto) {
        StringBuilder pedido = new StringBuilder();
        if (!contexto.isEmpty()) {
            pedido.append("Contexto:\n").append(contexto).append("\n\n");
        }
        pedido.append("Oraciones:");
        for (int i = 0; i < objetivos.size(); i++) {
            pedido.append("\n").append(i + 1).append(". ").append(objetivos.get(i));
        }
        return pedido.toString();
    }

    private void descartar(String motivo) {
        motivos.merge(motivo, 1, Integer::sum);
    }

    /**
     * Comprueba la pregunta del modelo y la deja con un solo par de signos. Es permisiva: solo pide que sea una pregunta, que no repita la oración ni un
     * enunciado ya usado, y que comparta con el contexto una o dos palabras de contenido (una si es corta).
     *
     * @return La pregunta, o nulo si se descarta.
     */
    private String enunciadoValido(String pregunta, String objetivo, Set<String> lemasDelContexto, Set<String> usados) {
        if (pregunta == null || pregunta.isBlank()) {
            descartar("pregunta vacía");
            return null;
        }
        String bruta = pregunta.trim();
        if (bruta.indexOf('?') < 0 || bruta.contains("\n")) {
            descartar("sin signo de pregunta o con varias líneas");
            return null;
        }
        // La primera pregunta con contenido: el modelo a veces escribe un «¿?» vacío antes de la pregunta o en lugar de ella
        String texto = null;
        int desde = 0;
        for (int fin = bruta.indexOf('?', desde); fin >= 0 && texto == null; fin = bruta.indexOf('?', desde)) {
            String contenido = bruta.substring(desde, fin).replaceAll("^[¿\\s]+", "").trim();
            if (contenido.length() >= LARGO_MINIMO_DE_UNA_PREGUNTA) {
                texto = "¿" + contenido + "?";
            }
            desde = fin + 1;
        }
        if (texto == null) {
            descartar("pregunta vacía («¿?»)");
            REGISTRO.info("Pregunta sin contenido, tal como la escribió el modelo: «{}» (para la oración: «{}»)", pregunta, objetivo);
            return null;
        }
        if (texto.length() > LARGO_MAXIMO) {
            descartar("enunciado de más de 255 caracteres");
            return null;
        }
        String normal = LogicaPreguntasAurea.normalizar(texto);
        if (usados.contains(normal)) {
            descartar("enunciado repetido");
            return null;
        }
        if (normal.contains(LogicaPreguntasAurea.normalizar(objetivo))) {
            descartar("la pregunta repite la oración");
            return null;
        }
        Set<String> lemas = GramaticaUtil.conjuntoDeLemas(texto);
        long compartidos = lemas.stream().filter(lemasDelContexto::contains).count();
        if (lemas.isEmpty() || compartidos < (lemas.size() <= LEMAS_DE_UN_TEXTO_CORTO ? 1 : 2)) {
            descartar("sin palabras de contenido del contexto");
            REGISTRO.info("Pregunta rechazada por no compartir palabras con el contexto: «{}» (lemas de la pregunta: {}; para la oración: «{}»)", texto, lemas,
                    objetivo);
            return null;
        }
        // Además de apoyarse en el contexto, tiene que hablar de la oración objetivo: si no, el modelo escribió la pregunta de otra oración del contexto
        Set<String> lemasDelObjetivo = GramaticaUtil.conjuntoDeLemas(objetivo);
        if (lemas.stream().noneMatch(lemasDelObjetivo::contains)) {
            descartar("sin palabras de la oración objetivo");
            REGISTRO.info("Pregunta que no habla de su oración: «{}» (para la oración: «{}»)", texto, objetivo);
            return null;
        }
        return texto;
    }

    /**
     * Las tres oraciones incorrectas de una oración. Primero las que cambian una palabra de la oración; después otras oraciones que comparten palabras de
     * contenido con ella (las más parecidas primero), y al final oraciones al azar.
     */
    private static List<String> incorrectasDe(String objetivo, String correcta, boolean sinSujeto, List<String> porCambio, List<String> todas,
            List<List<String>> lemasDeTodas) {
        List<String> incorrectas = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        vistas.add(LogicaPreguntasAurea.normalizar(correcta));
        for (String cambiada : porCambio) {
            if (cambiada.length() <= LARGO_MAXIMO && vistas.add(LogicaPreguntasAurea.normalizar(cambiada))) {
                incorrectas.add(cambiada);
            }
        }
        // Otras oraciones que comparten palabras de contenido, la que más comparte primero
        double[] puntajes = Bm25Util.puntajes(lemasDeTodas, GramaticaUtil.conjuntoDeLemas(objetivo));
        List<Integer> orden = new ArrayList<>();
        for (int i = 0; i < todas.size(); i++) {
            if (puntajes[i] > 0) {
                orden.add(i);
            }
        }
        orden.sort((x, y) -> Double.compare(puntajes[y], puntajes[x]));
        for (int i : orden) {
            agregar(incorrectas, vistas, todas.get(i), correcta, sinSujeto);
        }
        // Si no hay más, oraciones al azar
        List<String> alAzar = new ArrayList<>(todas);
        Collections.shuffle(alAzar);
        for (String oracion : alAzar) {
            agregar(incorrectas, vistas, oracion, correcta, sinSujeto);
        }
        return incorrectas.size() > INCORRECTAS ? incorrectas.subList(0, INCORRECTAS) : incorrectas;
    }

    /**
     * Agrega una oración como opción incorrecta si todavía hacen falta, si cabe en la base, es nueva y no es casi igual a la correcta. Si la correcta va sin
     * sujeto, la oración también: la que no se le puede quitar se salta.
     */
    private static void agregar(List<String> incorrectas, Set<String> vistas, String oracion, String correcta, boolean sinSujeto) {
        if (incorrectas.size() >= INCORRECTAS) {
            return;
        }
        String opcion = sinSujeto ? GramaticaUtil.sinSujeto(oracion) : oracion;
        if (opcion != null && opcion.length() <= LARGO_MAXIMO && !RellenoDeAureaIA.demasiadoParecida(correcta, opcion)
                && vistas.add(LogicaPreguntasAurea.normalizar(opcion))) {
            incorrectas.add(opcion);
        }
    }
}
