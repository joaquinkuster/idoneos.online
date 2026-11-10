package com.app.idoneos.service.indice;

import com.app.idoneos.model.Recurso;
import java.util.List;

/** Servicio del índice de un documento PDF. El índice es la lista de títulos con la página donde empiezan. */
public interface IndiceRecursoService {

    /** Cantidad máxima de entradas de un índice. */
    int MAXIMO_DE_ENTRADAS = 500;

    /**
     * Una entrada del índice.
     *
     * @param titulo El título.
     * @param pagina La página donde empieza (la primera es la 1).
     * @param nivel El nivel: 1 para un título principal, 2 y 3 para los subtítulos.
     */
    record Entrada(String titulo, int pagina, int nivel) {
    }

    /**
     * Las entradas del índice de un documento, en su orden.
     *
     * @param recurso El documento.
     * @return Las entradas (vacío si no tiene índice).
     */
    List<Entrada> buscar(Recurso recurso);

    /**
     * Reemplaza el índice de un documento por el que llega como JSON.
     *
     * @param recurso El documento.
     * @param json El índice completo, o {@code null} si no hay que tocarlo.
     * @throws IllegalArgumentException Si el índice no es válido.
     */
    void reemplazar(Recurso recurso, String json);
}
