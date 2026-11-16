package com.app.idoneos.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pares de palabras opuestas de la lengua general (no de un tema): cambiar una por la otra vuelve falsa una oración. Es la única lista de palabras que
 * tiene el generador de preguntas, y es corta a propósito: solo sirve para armar opciones incorrectas seguras; lo que no está acá se resuelve cambiando
 * palabras del propio documento. Se busca por la forma escrita (sin tildes ni mayúsculas) y se devuelve ya con la tilde que corresponde.
 */
public final class OpuestosUtil {

    private OpuestosUtil() {
    }

    private static final Map<String, Set<String>> OPUESTOS = new HashMap<>();

    static {
        // Verbos en tercera persona: el plural se arma con «n»
        verbos("permite", "impide");
        verbos("favorece", "dificulta");
        verbos("mejora", "empeora");
        verbos("aumenta", "disminuye");
        verbos("incrementa", "reduce");
        verbos("previene", "provoca");
        verbos("facilita", "obstaculiza");
        verbos("fortalece", "debilita");
        verbos("promueve", "desalienta");
        verbos("incluye", "excluye");
        verbos("prioriza", "posterga");
        verbos("garantiza", "descarta");
        // Adjetivos que terminan en «o»: masculino y femenino, singular y plural
        adjetivos("interno", "externo");
        adjetivos("directo", "indirecto");
        adjetivos("primero", "último");
        adjetivos("máximo", "mínimo");
        adjetivos("alto", "bajo");
        adjetivos("simple", "complejo");
        // Palabras que no cambian
        dos("antes", "después");
        dos("mayor", "menor");
        dos("mayores", "menores");
        dos("total", "parcial");
        dos("totales", "parciales");
        dos("más", "menos");
        dos("nunca", "siempre");
        // Cambios de alcance: solo en un sentido
        una("siempre", "solo en casos puntuales");
        una("generalmente", "de forma excepcional");
        una("principalmente", "solo de manera marginal");
    }

    private static void verbos(String a, String b) {
        dos(a, b);
        dos(a + "n", b + "n");
    }

    private static void adjetivos(String a, String b) {
        String raizA = a.substring(0, a.length() - 1);
        String raizB = b.substring(0, b.length() - 1);
        for (String fin : new String[] { "o", "a", "os", "as" }) {
            dos(raizA + fin, raizB + fin);
        }
    }

    private static void dos(String a, String b) {
        una(a, b);
        una(b, a);
    }

    private static void una(String origen, String destino) {
        OPUESTOS.computeIfAbsent(LogicaPreguntasAurea.normalizar(origen), k -> new LinkedHashSet<>()).add(destino);
    }

    /** Los opuestos de una palabra, o una lista vacía si no tiene. Si la palabra empieza con mayúscula, el opuesto también. */
    public static List<String> de(String palabra) {
        Set<String> opuestos = OPUESTOS.get(LogicaPreguntasAurea.normalizar(palabra));
        if (opuestos == null) {
            return List.of();
        }
        boolean mayuscula = Character.isUpperCase(palabra.charAt(0));
        List<String> resultado = new ArrayList<>();
        for (String opuesto : opuestos) {
            resultado.add(mayuscula ? Character.toUpperCase(opuesto.charAt(0)) + opuesto.substring(1) : opuesto);
        }
        return resultado;
    }
}
