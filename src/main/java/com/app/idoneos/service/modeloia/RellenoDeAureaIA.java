package com.app.idoneos.service.modeloia;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cómo completa las opciones incorrectas que faltan el proyecto Aurea: cambia palabras de la respuesta correcta con listas de pares de opuestos, suma a
 * los números, antepone «Solo» o arma frases genéricas. Es una copia de ese comportamiento para el camino alternativo del banco; las listas son suyas.
 */
final class RellenoDeAureaIA {

    private RellenoDeAureaIA() {
    }

    /** Las mismas palabras que ignora Aurea al comparar textos. */
    static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "de", "la", "que", "el", "en", "y", "a", "los", "del", "se", "las", "por", "un", "para", "con", "no", "una", "su", "al", "lo",
            "como", "mas", "pero", "sus", "le", "ya", "o", "este", "si", "porque", "esta", "entre", "cuando", "muy", "sin", "sobre", "tambien",
            "me", "hasta", "hay", "donde", "quien", "desde", "todo", "nos", "durante", "todos", "uno", "les", "ni", "contra", "otros", "ese",
            "eso", "ante", "ellos", "e", "esto", "antes", "algunos", "unos", "otro", "otras", "otra", "tanto", "esa", "estos", "mucho",
            "quienes", "nada", "muchos", "cual", "poco", "ella", "estar", "estas", "algunas", "algo", "nosotros", "mi", "mis", "tu", "tus",
            "ellas", "nosotras", "vosostros", "vosotras", "os", "mio", "mia", "mios", "mias", "tuyo", "tuya", "tuyos", "tuyas", "suyo",
            "suya", "suyos", "suyas", "nuestro", "nuestra", "nuestros", "nuestras", "vuestro", "vuestra", "vuestros", "vuestras", "es",
            "son", "ser", "fue", "fueron", "ha", "han", "hace", "hacer", "cada", "material", "tema", "concepto", "aspecto", "sistema",
            "proceso", "procesos", "funcion", "funciones", "dato", "curioso", "curiosa", "informacion", "elemento", "rol"));

    private static final String[][] CAMBIOS_SEMANTICOS = {
            { "permite", "impide" }, { "impide", "permite" }, { "favorece", "dificulta" }, { "dificulta", "favorece" },
            { "mejora", "empeora" }, { "empeora", "mejora" }, { "incrementa", "reduce" }, { "reduce", "incrementa" },
            { "aumenta", "disminuye" }, { "disminuye", "aumenta" }, { "requiere", "prescinde de" }, { "prescinde de", "requiere" },
            { "depende de", "es independiente de" }, { "es independiente de", "depende de" }, { "previene", "provoca" }, { "provoca", "previene" },
            { "facilita", "obstaculiza" }, { "obstaculiza", "facilita" }, { "fortalece", "debilita" }, { "debilita", "fortalece" },
            { "promueve", "desalienta" }, { "desalienta", "promueve" }, { "incluye", "excluye" }, { "excluye", "incluye" },
            { "prioriza", "posterga" }, { "posterga", "prioriza" }, { "garantiza", "descarta" }, { "descarta", "garantiza" } };

    private static final String[][] CAMBIOS_DE_ALCANCE = {
            { "siempre", "solo en casos puntuales" }, { "generalmente", "de forma excepcional" }, { "principalmente", "solo de manera marginal" },
            { "antes", "despues" }, { "despues", "antes" }, { "mayor", "menor" }, { "menor", "mayor" }, { "todos", "muy pocos" },
            { "varios", "un unico" }, { "puede", "necesariamente debe" } };

    /** Una oración partida en sujeto, verbo y predicado. */
    private record Sentencia(String sujeto, String verbo, String predicado) {
    }

    /**
     * Arma una opción incorrecta a partir de la respuesta correcta: la primera de varios candidatos que no es relleno, no se parece demasiado a la correcta
     * y no se usó. Siempre devuelve algo.
     *
     * @param referencia La respuesta correcta.
     * @param sentencias Oraciones importantes del material, que también se mutan.
     * @param usados Las opciones ya usadas, normalizadas.
     * @param indice Cuántos intentos van, para variar la elección.
     */
    static String distractor(String referencia, List<String> sentencias, Set<String> usados, int indice) {
        List<String> candidatos = new ArrayList<>();
        if (referencia != null && !referencia.isBlank()) {
            candidatos.addAll(variantesEstructuradas(referencia));
            candidatos.add(limitar(mutar(referencia, indice), 180));
            candidatos.add(limitar(mutar(referencia, indice + 1), 180));
        }
        if (sentencias != null) {
            for (int i = 0; i < sentencias.size(); i++) {
                String sentencia = limpiar(sentencias.get(Math.floorMod(indice + i, sentencias.size())));
                if (!sentencia.isBlank()) {
                    candidatos.add(limitar(mutar(sentencia, indice + i + 2), 180));
                }
            }
        }
        candidatos.add("El material lo presenta como un aspecto secundario y no como un eje central del contenido.");
        candidatos.add("Se lo describe como una condicion puntual, sin efectos sostenidos sobre el desarrollo del proceso.");
        for (String candidato : candidatos) {
            String limpio = limpiar(candidato);
            if (limpio.isBlank() || esRelleno(limpio) || demasiadoParecida(referencia, limpio)) {
                continue;
            }
            if (!usados.contains(normalizar(limpio))) {
                return limpio;
            }
        }
        return "El texto lo presenta como una condicion accesoria, sin el alcance principal que se desarrolla en el material.";
    }

    /** Cambia una palabra de la oración por su opuesto, suma a un número o antepone «Solo», según lo que encuentre. */
    private static String mutar(String sentencia, int semilla) {
        String base = limpiar(sentencia);
        List<String> candidatos = new ArrayList<>(variantesEstructuradas(base));
        String semantico = reemplazarPrimera(base, CAMBIOS_SEMANTICOS, semilla);
        if (!semantico.equals(base)) {
            candidatos.add(semantico);
        }
        String alcance = reemplazarPrimera(base, CAMBIOS_DE_ALCANCE, semilla + 1);
        if (!alcance.equals(base)) {
            candidatos.add(alcance);
        }
        String numero = alterarNumero(base);
        if (!numero.equals(base)) {
            candidatos.add(numero);
        }
        String restriccion = conRestriccion(base);
        if (!restriccion.equals(base)) {
            candidatos.add(restriccion);
        }
        for (int i = 0; i < candidatos.size(); i++) {
            String candidato = limpiar(candidatos.get(Math.floorMod(semilla + i, candidatos.size())));
            if (!candidato.isBlank() && !normalizar(candidato).equals(normalizar(base)) && !demasiadoParecida(base, candidato)) {
                return candidato;
            }
        }
        if (base.toLowerCase(Locale.ROOT).contains(" es ")) {
            return base.replaceFirst("(?i)\\bes\\b", "no es");
        }
        if (base.toLowerCase(Locale.ROOT).contains(" son ")) {
            return base.replaceFirst("(?i)\\bson\\b", "no son");
        }
        return "Solo " + minuscula(base);
    }

    private static String reemplazarPrimera(String texto, String[][] reemplazos, int semilla) {
        for (int i = 0; i < reemplazos.length; i++) {
            String[] par = reemplazos[(semilla + i) % reemplazos.length];
            String regex = "(?i)\\b" + Pattern.quote(par[0]) + "\\b";
            if (Pattern.compile(regex).matcher(texto).find()) {
                return texto.replaceFirst(regex, Matcher.quoteReplacement(par[1]));
            }
        }
        return texto;
    }

    private static String alterarNumero(String texto) {
        Matcher matcher = Pattern.compile("\\b\\d+\\b").matcher(texto);
        if (!matcher.find()) {
            return texto;
        }
        int valor = Integer.parseInt(matcher.group());
        return matcher.replaceFirst(String.valueOf(valor >= 10 ? valor + 5 : valor + 1));
    }

    private static String conRestriccion(String texto) {
        String limpio = limpiar(texto);
        String minuscula = limpio.toLowerCase(Locale.ROOT);
        if (minuscula.startsWith("solo ") || minuscula.startsWith("exclusivamente ")) {
            return limpio;
        }
        return "Solo " + minuscula(limpio);
    }

    private static List<String> variantesEstructuradas(String sentencia) {
        List<String> variantes = new ArrayList<>();
        Sentencia estructura = descomponer(sentencia);
        if (estructura == null) {
            return variantes;
        }
        String sujeto = mayuscula(estructura.sujeto());
        String sujetoEnTexto = minuscula(estructura.sujeto());
        List<String> fragmentos = fragmentar(estructura.predicado());
        String principal = !fragmentos.isEmpty() ? fragmentos.get(0) : resumir(estructura.predicado(), 9);
        String secundario = fragmentos.size() > 1 ? fragmentos.get(1) : "otros aspectos centrales del contenido";
        String verbo = estructura.verbo();
        if (verbo.equals("es") || verbo.equals("son")) {
            variantes.add(sujeto + " " + verbo + " un fenomeno limitado a un solo plano, sin las demas dimensiones que menciona el material.");
            variantes.add("El material describe " + sujetoEnTexto + " como un hecho aislado y no como un proceso gradual con multiples implicancias.");
            variantes.add(sujeto + " " + verbo + " una condicion secundaria que solo aparece en situaciones excepcionales.");
            return variantes;
        }
        if (Arrays.asList("incluye", "incluyen", "implica", "implican", "requiere", "requieren", "permite", "permiten", "favorece", "favorecen",
                "ayuda a", "ayudan a").contains(verbo)) {
            variantes.add(sujeto + " " + verbo + " solo " + principal + ", sin abarcar el resto de los aspectos destacados en el material.");
            variantes.add("El material presenta " + sujetoEnTexto + " como una responsabilidad mas acotada, centrada en " + principal
                    + " y no en un acompanamiento integral.");
            variantes.add(sujeto + " " + verbo + " " + principal + ", pero no " + secundario + " ni otras tareas de seguimiento.");
            return variantes;
        }
        variantes.add(sujeto + " " + verbo + " solo " + principal + ", dejando fuera otras implicancias relevantes del material.");
        variantes.add("El material presenta " + sujetoEnTexto + " como un factor accesorio y no como un elemento decisivo del proceso.");
        variantes.add(sujeto + " " + verbo + " efectos puntuales, pero no modificaciones sostenidas en el contexto analizado.");
        return variantes;
    }

    private static Sentencia descomponer(String sentencia) {
        if (sentencia == null || sentencia.isBlank()) {
            return null;
        }
        String limpia = limpiar(sentencia).replaceAll("[.]+$", "");
        String[] verbos = { "ayudan a", "ayuda a", "contribuyen a", "contribuye a", "dependen de", "depende de", "incluyen", "incluye", "implican",
                "implica", "requieren", "requiere", "permiten", "permite", "favorecen", "favorece", "previenen", "previene", "reducen", "reduce",
                "aumentan", "aumenta", "fortalecen", "fortalece", "debilitan", "debilita", "representan", "representa", "son", "es" };
        for (String verbo : verbos) {
            Matcher matcher = Pattern.compile("(?i)^(.+?)\\s+" + Pattern.quote(verbo) + "\\s+(.+)$").matcher(limpia);
            if (matcher.find()) {
                String sujeto = limpiar(matcher.group(1));
                String predicado = limpiar(matcher.group(2));
                if (!sujeto.isBlank() && !predicado.isBlank()) {
                    return new Sentencia(sujeto, verbo, predicado);
                }
            }
        }
        return null;
    }

    private static List<String> fragmentar(String predicado) {
        List<String> fragmentos = new ArrayList<>();
        if (predicado == null || predicado.isBlank()) {
            return fragmentos;
        }
        String limpio = predicado.replaceAll("[.]+$", "");
        for (String parte : limpio.split(",|\\sy\\s|\\se\\s")) {
            String candidato = limpiar(parte);
            if (!candidato.isBlank()) {
                fragmentos.add(resumir(candidato, 10));
                if (fragmentos.size() >= 3) {
                    break;
                }
            }
        }
        if (fragmentos.isEmpty()) {
            fragmentos.add(resumir(limpio, 10));
        }
        return fragmentos;
    }

    private static String resumir(String texto, int maximoDePalabras) {
        String[] palabras = limpiar(texto).split("\\s+");
        return palabras.length <= maximoDePalabras ? limpiar(texto) : String.join(" ", Arrays.copyOfRange(palabras, 0, maximoDePalabras));
    }

    /** Una opción es relleno si empieza con «relacionado con» o «dato curioso», dice «todas las anteriores» o tiene menos de dos palabras. */
    static boolean esRelleno(String texto) {
        String normal = normalizar(texto);
        return normal.isBlank() || normal.startsWith("relacionado con ") || normal.startsWith("dato curioso") || normal.contains("respuesta esperada")
                || normal.contains("no se menciona en el material") || normal.contains("ninguna de las opciones") || normal.contains("todas las anteriores")
                || normal.split("\\s+").length < 2;
    }

    /** Dos textos son demasiado parecidos si comparten el 85 % de sus palabras relevantes en ambos sentidos y difieren en dos palabras o menos. */
    static boolean demasiadoParecida(String base, String candidata) {
        if (base == null || base.isBlank() || candidata == null || candidata.isBlank()) {
            return false;
        }
        Set<String> tokensBase = relevantes(base);
        Set<String> tokensCandidata = relevantes(candidata);
        if (tokensBase.isEmpty() || tokensCandidata.isEmpty()) {
            return false;
        }
        Set<String> interseccion = new HashSet<>(tokensBase);
        interseccion.retainAll(tokensCandidata);
        double coberturaBase = interseccion.size() / (double) tokensBase.size();
        double coberturaCandidata = interseccion.size() / (double) tokensCandidata.size();
        int diferencia = Math.abs(limpiar(base).split("\\s+").length - limpiar(candidata).split("\\s+").length);
        return coberturaBase >= 0.85 && coberturaCandidata >= 0.85 && diferencia <= 2;
    }

    private static Set<String> relevantes(String texto) {
        Set<String> tokens = new HashSet<>();
        for (String token : normalizar(texto).split("\\s+")) {
            if (token.length() >= 4 && !STOPWORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replaceAll("\\s+", " ").trim();
    }

    private static String limitar(String texto, int maximo) {
        String limpio = limpiar(texto);
        return limpio.length() <= maximo ? limpio : limpio.substring(0, Math.max(0, maximo - 3)) + "...";
    }

    private static String minuscula(String texto) {
        return texto == null || texto.isBlank() ? "" : texto.substring(0, 1).toLowerCase(Locale.ROOT) + texto.substring(1);
    }

    private static String mayuscula(String texto) {
        return texto == null || texto.isBlank() ? "" : texto.substring(0, 1).toUpperCase(Locale.ROOT) + texto.substring(1);
    }
}
