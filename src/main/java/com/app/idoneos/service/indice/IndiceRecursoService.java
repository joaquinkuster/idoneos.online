package com.app.idoneos.service.indice;

import com.app.idoneos.model.Recurso;
import java.util.List;

/**
 * Índice de un documento PDF: la lista de títulos con la página donde empiezan. El navegador lo arma al cargar el archivo (con los
 * marcadores del PDF o con los títulos que detecta en su texto), el docente lo puede corregir y el servidor lo guarda; el visor lo muestra.
 */
public interface IndiceRecursoService {

    /** Cantidad máxima de entradas de un índice. */
    int MAXIMO_DE_ENTRADAS = 500;

    /**
     * Una entrada del índice.
     *
     * @param titulo El título.
     * @param pagina La página donde empieza (la primera es la 1).
     * @param nivel  El nivel: 1 para un título principal, 2 y 3 para los subtítulos.
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
     * Reemplaza el índice de un documento por el que llega como JSON ({@code [{"titulo": "...", "pagina": 1, "nivel": 1}, ...]}). Un JSON
     * vacío ({@code []}) borra el índice; si no se recibe nada ({@code null}), el índice no cambia.
     *
     * @param recurso El documento.
     * @param json    El índice completo, o {@code null} si no hay que tocarlo.
     * @throws IllegalArgumentException Si el índice no es válido.
     */
    void reemplazar(Recurso recurso, String json);
}
