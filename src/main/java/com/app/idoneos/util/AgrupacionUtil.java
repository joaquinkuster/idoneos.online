package com.app.idoneos.util;

import com.app.idoneos.model.Categoria;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Utilidad de agrupación de listados. Agrupa las filas de una página por categoría, para dibujar una carta por cada una. */
public final class AgrupacionUtil {

    private AgrupacionUtil() {
    }

    /**
     * Un grupo de filas consecutivas de una misma categoría.
     *
     * @param categoria La categoría del grupo.
     * @param filas Las filas de la página que son de esa categoría, en el orden del listado.
     */
    public record Grupo<T>(Categoria categoria, List<T> filas) {
    }

    /**
     * Agrupa las filas en grupos de categorías consecutivas (el listado ya viene ordenado por categoría).
     *
     * @param filas Las filas de la página.
     * @param categoria La categoría de cada fila.
     * @return Los grupos, en el orden en que aparecen las filas.
     */
    public static <T> List<Grupo<T>> agrupar(List<T> filas, Function<T, Categoria> categoria) {
        List<Grupo<T>> grupos = new ArrayList<>();
        for (T fila : filas) {
            Categoria actual = categoria.apply(fila);
            if (grupos.isEmpty() || grupos.getLast().categoria().getIdCategoria() != actual.getIdCategoria()) {
                grupos.add(new Grupo<>(actual, new ArrayList<>()));
            }
            grupos.getLast().filas().add(fila);
        }
        return grupos;
    }
}
