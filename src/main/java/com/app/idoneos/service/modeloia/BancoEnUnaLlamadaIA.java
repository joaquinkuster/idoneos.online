package com.app.idoneos.service.modeloia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.idoneos.util.Bm25Util;
import com.app.idoneos.util.GramaticaUtil;
import com.app.idoneos.util.LogicaPreguntasAurea;
import com.app.idoneos.util.LogicaPreguntasAurea.Opcion;
import com.app.idoneos.util.LogicaPreguntasAurea.Pregunta;
import com.app.idoneos.util.LogicaPreguntasAurea.Tipo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Camino alternativo para armar el banco, con el flujo del proyecto Aurea: el modelo escribe todo junto (enunciado, opciones y cuál es la correcta) en una
 * sola llamada por lote, y el código solo comprueba lo que escribió y completa las opciones que falten. A diferencia de Aurea, que le da al modelo el
 * principio del material (8000 caracteres), a cada lote se le dan pocas oraciones objetivo, las más importantes del documento, junto con unas oraciones
 * parecidas como contexto: el modelo lee mucho menos, tarda menos y cada pregunta sale de una oración concreta, que es su evidencia.
 */
final class BancoEnUnaLlamadaIA {

    private static final Logger REGISTRO = LoggerFactory.getLogger(BancoEnUnaLlamadaIA.class);

    /** Cuántas veces se pide un lote antes de dejar lo que falte a las reglas. */
    private static final int LOTES_MAXIMOS = 4;

    /** Cuántas oraciones objetivo tiene cada lote como máximo: una pregunta por oración. */
    private static final int OBJETIVOS_POR_LOTE = 5;

    /** Cuántas oraciones parecidas a las objetivo se agregan como contexto. */
    private static final int PARECIDAS_POR_LOTE = 5;

    /** Hasta cuántos caracteres de contexto (las oraciones parecidas) se le dan al modelo. */
    private static final int CARACTERES_DE_CONTEXTO = 1500;

    /** Los tokens que puede escribir el modelo en un lote. */
    private static final int TOKENS_POR_LOTE = 2200;

    private static final int OPCIONES = 4;

    /** Cuántos caracteres entran en el enunciado de una pregunta y en el texto de una opción en la base. */
    private static final int LARGO_MAXIMO = 255;

    /** Un texto de hasta esta cantidad de palabras de contenido se da por apoyado con una sola palabra compartida con el contexto; uno más largo necesita dos. */
    private static final int LEMAS_DE_UN_TEXTO_CORTO = 3;

    /** Una numeración al principio de una opción («1. », «b) »): el modelo la agrega y no es parte del texto. */
    private static final Pattern NUMERACION_AL_PRINCIPIO = Pattern.compile("^\\s*(?:\\d{1,2}|[a-dA-D])[.)]\\s+");

    /** Un enunciado que trae las opciones adentro («… ¿…? 1. Sí. 2. No.»). */
    private static final Pattern OPCIONES_EN_EL_ENUNCIADO = Pattern.compile("(?s).*(?:\\n|\\s[1-4][.)]\\s).*");

    private static final String INSTRUCCIONES = """
            Eres un docente experto en evaluación. Generas preguntas basadas SOLO en el material.
            No inventes datos ni uses trivia obvia. Evalúa comprensión, relaciones, condiciones, causa y efecto, comparaciones o aplicaciones del contenido.
            Las opciones deben ser plausibles, completas, de longitud similar y redactadas como frases naturales.
            Nunca uses distractores pobres como «Relacionado con…», palabras sueltas, «Todas las anteriores» o «Ninguna de las anteriores».
            Tampoco generes opciones incorrectas que solo cambien una palabra de forma antinatural o que rompan la gramática de la oración.
            Para VERDADERO_FALSO alterna entre afirmaciones verdaderas y falsas plausibles.
            Si una afirmación es falsa, cambia un matiz relevante del material (causa, condición, alcance, secuencia, cantidad o excepción) para que no sea obvia.
            Devuelves exclusivamente JSON válido sin texto extra.""";

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Lote(List<PreguntaDelLote> questions) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PreguntaDelLote(String type, String question, List<OpcionDelLote> options) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OpcionDelLote(String text, boolean correct) {
    }

    private final ModeloIAService modelo;

    /** Por qué se descartó cada pregunta del lote actual: motivo y cantidad. */
    private final Map<String, Integer> motivos = new java.util.LinkedHashMap<>();

    BancoEnUnaLlamadaIA(ModeloIAService modelo) {
        this.modelo = modelo;
    }

    /**
     * Arma preguntas pidiéndole al modelo lotes de una pregunta por oración objetivo. Cada lote toma las siguientes oraciones de la lista, le suma como
     * contexto las más parecidas del documento y se queda con las preguntas que pasan las comprobaciones.
     *
     * @param cantidad Cuántas preguntas como máximo.
     * @param objetivos Las oraciones del documento de las que se arman las preguntas, de la más a la menos importante.
     * @param todas Todas las oraciones del documento: de ahí sale el contexto.
     * @param usados Los enunciados ya usados, normalizados; se agregan los que se aceptan.
     * @param existentes Los enunciados de otros bancos del tema, para pedirle al modelo que no los repita.
     * @param paraRellenar Oraciones importantes del documento, que se mutan para completar las opciones que falten.
     * @return Las preguntas aceptadas, hasta {@code cantidad}.
     */
    List<Pregunta> armar(int cantidad, List<String> objetivos, List<String> todas, Set<String> usados, List<String> existentes, List<String> paraRellenar) {
        List<List<String>> lemasDeTodas = new ArrayList<>();
        todas.forEach(oracion -> lemasDeTodas.add(GramaticaUtil.lemasDeContenido(oracion)));
        List<Pregunta> aceptadas = new ArrayList<>();
        int siguiente = 0;
        for (int lote = 0; lote < LOTES_MAXIMOS && aceptadas.size() < cantidad && siguiente < objetivos.size(); lote++) {
            int cuantas = Math.min(OBJETIVOS_POR_LOTE, Math.min(cantidad - aceptadas.size() + 2, objetivos.size() - siguiente));
            List<String> deEsteLote = objetivos.subList(siguiente, siguiente + cuantas);
            siguiente += cuantas;
            String contexto = contextoDe(deEsteLote, todas, lemasDeTodas);
            Lote respuesta;
            try {
                // Con el esquema, Ollama obliga a que la respuesta sea el JSON pedido: sin él, el modelo chico contesta en prosa
                respuesta = modelo.generarObjeto(INSTRUCCIONES, pedido(deEsteLote, contexto), Lote.class, EsquemasIA.lote(cuantas), TOKENS_POR_LOTE,
                        0.25, 0.9, 40, 1.1);
            } catch (ModeloIAException e) {
                REGISTRO.warn("El modelo no respondió el lote {}: {}", lote + 1, e.getMessage());
                continue;
            }
            if (respuesta == null || respuesta.questions() == null) {
                continue;
            }
            motivos.clear();
            Set<String> lemasDelContexto = new HashSet<>(GramaticaUtil.conjuntoDeLemas(contexto));
            deEsteLote.forEach(oracion -> lemasDelContexto.addAll(GramaticaUtil.conjuntoDeLemas(oracion)));
            for (PreguntaDelLote candidata : respuesta.questions()) {
                Pregunta pregunta = comprobar(candidata, lemasDelContexto, usados, paraRellenar);
                if (pregunta != null && aceptadas.size() < cantidad) {
                    usados.add(LogicaPreguntasAurea.normalizar(pregunta.enunciado()));
                    aceptadas.add(pregunta);
                }
            }
            REGISTRO.info("Lote {} del flujo en una llamada: {} oraciones objetivo, {} preguntas escritas; {} de {} aceptadas hasta ahora. Descartadas por motivo: {}",
                    lote + 1, cuantas, respuesta.questions().size(), aceptadas.size(), cantidad, motivos.isEmpty() ? "ninguna" : motivos);
        }
        return aceptadas;
    }

    /** El contexto de un lote: las oraciones del documento más parecidas a las objetivo (según BM25), sin repetir las objetivo, hasta el largo permitido. */
    static String contextoDe(List<String> objetivos, List<String> todas, List<List<String>> lemasDeTodas) {
        Set<String> consulta = new HashSet<>();
        objetivos.forEach(oracion -> consulta.addAll(GramaticaUtil.conjuntoDeLemas(oracion)));
        double[] puntajes = Bm25Util.puntajes(lemasDeTodas, consulta);
        List<Integer> orden = new ArrayList<>();
        for (int i = 0; i < todas.size(); i++) {
            orden.add(i);
        }
        orden.sort((x, y) -> Double.compare(puntajes[y], puntajes[x]));
        Set<String> elegidas = new LinkedHashSet<>();
        int largo = 0;
        for (int i : orden) {
            String oracion = todas.get(i);
            if (puntajes[i] <= 0 || elegidas.size() >= PARECIDAS_POR_LOTE || objetivos.contains(oracion)) {
                continue;
            }
            if (largo + oracion.length() > CARACTERES_DE_CONTEXTO) {
                break;
            }
            largo += oracion.length();
            elegidas.add(oracion);
        }
        return String.join("\n", elegidas);
    }

    private static String pedido(List<String> objetivos, String contexto) {
        StringBuilder pedido = new StringBuilder();
        pedido.append("Genera ").append(objetivos.size()).append(" preguntas de examen: una por cada oración objetivo, en el mismo orden. ");
        pedido.append("Cada pregunta trata solo de su oración objetivo; usa el contexto únicamente para entender de qué habla. ");
        pedido.append("Requisitos generales: preguntas originales, mezcla de dificultad media y media-alta, sin inventar fuera del material, evitando preguntas de memoria literal. ");
        pedido.append("Usa SOLO estos tipos: MULTIPLE_CHOICE, VERDADERO_FALSO, UNICA_RESPUESTA. Incluye el campo 'type' en cada pregunta. ");
        pedido.append("Para MULTIPLE_CHOICE: exactamente 4 opciones y exactamente 2 correctas. ");
        pedido.append("Para UNICA_RESPUESTA: exactamente 4 opciones y exactamente 1 correcta. ");
        pedido.append("Para VERDADERO_FALSO: el campo 'question' debe ser una afirmación completa; en 'options' devuelve exactamente 'Verdadero' y 'Falso', con una sola correcta. ");
        pedido.append("El campo 'question' lleva solo la pregunta, sin las opciones ni su numeración. ");
        pedido.append("Todas las opciones deben tener entre 6 y 22 palabras aproximadamente y sonar plausibles entre sí. ");
        pedido.append("Evita que la respuesta correcta destaque por ser la única concreta o la más larga. ");
        pedido.append("No uses opciones del tipo 'dato curioso', palabras aisladas, ni alternativas que solo sustituyan una palabra y suenen raras. ");
        pedido.append("Piensa antes de responder y verifica que todo esté respaldado por el material. ");
        pedido.append("Formato JSON EXACTO: {\"questions\":[{\"type\":\"MULTIPLE_CHOICE\",\"question\":\"...\",\"options\":[");
        pedido.append("{\"text\":\"...\",\"correct\":true},{\"text\":\"...\",\"correct\":false},");
        pedido.append("{\"text\":\"...\",\"correct\":false},{\"text\":\"...\",\"correct\":false}]}]}");
        // La lista de preguntas ya usadas no va en el pedido: un modelo chico la copia como si fuera un ejemplo. Los repetidos los descarta el código
        if (!contexto.isEmpty()) {
            pedido.append("\n\nContexto:\n").append(contexto);
        }
        pedido.append("\n\nOraciones objetivo:");
        for (int i = 0; i < objetivos.size(); i++) {
            pedido.append("\n").append(i + 1).append(". ").append(objetivos.get(i));
        }
        return pedido.toString();
    }

    /** Anota por qué se descarta una pregunta y devuelve nulo, para usarlo en un {@code return}. */
    private Pregunta descartar(String motivo) {
        motivos.merge(motivo, 1, Integer::sum);
        return null;
    }

    /** Aplica las comprobaciones: formato, enunciado nuevo, respuesta correcta y apoyo en el contexto. Devuelve nulo si no pasa. */
    private Pregunta comprobar(PreguntaDelLote candidata, Set<String> lemasDelContexto, Set<String> usados, List<String> paraRellenar) {
        if (candidata == null || candidata.question() == null || candidata.question().isBlank() || candidata.options() == null) {
            return descartar("sin enunciado u opciones");
        }
        String enunciado = candidata.question().trim();
        if (OPCIONES_EN_EL_ENUNCIADO.matcher(enunciado).matches()) {
            return descartar("opciones dentro del enunciado");
        }
        // La base guarda como mucho 255 caracteres en el enunciado y en cada opción: una pregunta más larga haría fallar todo el banco al guardar
        if (enunciado.length() > LARGO_MAXIMO) {
            return descartar("enunciado de más de 255 caracteres");
        }
        if (usados.contains(LogicaPreguntasAurea.normalizar(enunciado))) {
            return descartar("enunciado repetido");
        }
        Tipo tipo = tipoDe(candidata.type());
        if (tipo == Tipo.VERDADERO_FALSO) {
            return verdaderoFalso(enunciado, candidata, lemasDelContexto);
        }
        List<Opcion> opciones = new ArrayList<>();
        Set<String> vistas = new HashSet<>();
        for (OpcionDelLote opcion : candidata.options()) {
            String texto = opcion.text() == null ? "" : NUMERACION_AL_PRINCIPIO.matcher(opcion.text()).replaceFirst("").trim();
            // Una opción de relleno, o incorrecta de menos de cuatro palabras, o repetida, no entra
            if (texto.isEmpty() || texto.length() > LARGO_MAXIMO || RellenoDeAureaIA.esRelleno(texto) || (!opcion.correct() && texto.split("\\s+").length < 4)
                    || !vistas.add(RellenoDeAureaIA.normalizar(texto))) {
                continue;
            }
            opciones.add(new Opcion(texto, opcion.correct()));
        }
        // Las correctas, de la que más se apoya en el contexto a la que menos
        List<Opcion> marcadas = opciones.stream().filter(Opcion::correcta)
                .sorted((x, y) -> Integer.compare(solapamiento(y.texto(), lemasDelContexto), solapamiento(x.texto(), lemasDelContexto))).toList();
        if (marcadas.isEmpty()) {
            return descartar("sin opción correcta");
        }
        // La clave tiene que ser, al menos, tan fiel al contexto como la incorrecta que más lo es: si no, el modelo marcó la que no era
        int mejorIncorrecta = opciones.stream().filter(o -> !o.correcta()).mapToInt(o -> solapamiento(o.texto(), lemasDelContexto)).max().orElse(0);
        if (solapamiento(marcadas.get(0).texto(), lemasDelContexto) < mejorIncorrecta) {
            return descartar("la correcta se apoya menos que una incorrecta");
        }
        // El tipo lo dice la cantidad de correctas, no lo que escribió el modelo, que siempre repite el del ejemplo: dos correctas es opción múltiple y una es única
        Tipo tipoFinal = tipo == Tipo.MULTIPLE_CHOICE && marcadas.size() >= 2 ? Tipo.MULTIPLE_CHOICE : Tipo.UNICA_RESPUESTA;
        List<Opcion> correctas = marcadas.stream().limit(tipoFinal == Tipo.MULTIPLE_CHOICE ? 2 : 1).toList();
        // Se apoya en el material si lo hace la pregunta o la respuesta correcta
        if (!apoyado(enunciado, lemasDelContexto) && !apoyado(correctas.get(0).texto(), lemasDelContexto)) {
            return descartar("sin apoyo en el contexto");
        }
        List<Opcion> finales = new ArrayList<>(correctas);
        for (Opcion opcion : opciones) {
            // Una incorrecta casi igual a una correcta se quita
            if (!opcion.correcta() && finales.size() < OPCIONES
                    && correctas.stream().noneMatch(c -> RellenoDeAureaIA.demasiadoParecida(c.texto(), opcion.texto()))) {
                finales.add(opcion);
            }
        }
        // Lo que falta se arma como en Aurea: cambiando la respuesta correcta, hasta seis intentos
        Set<String> usadasEnLaPregunta = new HashSet<>();
        finales.forEach(o -> usadasEnLaPregunta.add(RellenoDeAureaIA.normalizar(o.texto())));
        for (int intento = 0; finales.size() < OPCIONES && intento < 6; intento++) {
            String relleno = RellenoDeAureaIA.distractor(correctas.get(0).texto(), paraRellenar, usadasEnLaPregunta, intento);
            if (relleno.length() <= LARGO_MAXIMO && !RellenoDeAureaIA.esRelleno(relleno) && !RellenoDeAureaIA.demasiadoParecida(correctas.get(0).texto(), relleno)
                    && usadasEnLaPregunta.add(RellenoDeAureaIA.normalizar(relleno))) {
                finales.add(new Opcion(relleno, false));
            }
        }
        if (finales.size() < OPCIONES) {
            return descartar("no se pudieron completar las 4 opciones");
        }
        Collections.shuffle(finales);
        return new Pregunta(enunciado, finales, tipoFinal);
    }

    private Pregunta verdaderoFalso(String enunciado, PreguntaDelLote candidata, Set<String> lemasDelContexto) {
        Boolean verdadera = null;
        for (OpcionDelLote opcion : candidata.options()) {
            if (opcion.correct() && opcion.text() != null) {
                String texto = RellenoDeAureaIA.normalizar(opcion.text());
                verdadera = texto.contains("verdadero") ? Boolean.TRUE : texto.contains("falso") ? Boolean.FALSE : verdadera;
            }
        }
        if (verdadera == null) {
            return descartar("V/F sin respuesta clara");
        }
        if (!apoyado(enunciado, lemasDelContexto)) {
            return descartar("V/F sin apoyo en el contexto");
        }
        return new Pregunta(enunciado, List.of(new Opcion("Verdadero", verdadera), new Opcion("Falso", !verdadera)), Tipo.VERDADERO_FALSO);
    }

    private static Tipo tipoDe(String tipo) {
        String normal = tipo == null ? "" : tipo.trim().toUpperCase(java.util.Locale.ROOT);
        return normal.contains("FALSO") ? Tipo.VERDADERO_FALSO : normal.contains("UNICA") ? Tipo.UNICA_RESPUESTA : Tipo.MULTIPLE_CHOICE;
    }

    /**
     * El texto se apoya en el contexto si comparte con él al menos dos palabras de contenido (lemas, según OpenNLP), o una si el texto es corto. Es una
     * comprobación permisiva: solo descarta lo que no tiene nada que ver con el contexto.
     */
    private static boolean apoyado(String texto, Set<String> lemasDelContexto) {
        Set<String> lemas = GramaticaUtil.conjuntoDeLemas(texto);
        if (lemas.isEmpty()) {
            return false;
        }
        return solapamiento(texto, lemasDelContexto) >= (lemas.size() <= LEMAS_DE_UN_TEXTO_CORTO ? 1 : 2);
    }

    /** Cuántas palabras de contenido (lemas) del texto están en el contexto. */
    private static int solapamiento(String texto, Set<String> lemasDelContexto) {
        return (int) GramaticaUtil.conjuntoDeLemas(texto).stream().filter(lemasDelContexto::contains).count();
    }
}
