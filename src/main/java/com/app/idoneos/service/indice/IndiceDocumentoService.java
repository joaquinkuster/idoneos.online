package com.app.idoneos.service.indice;

import com.app.idoneos.model.Documento;
import java.util.List;

/** Servicio del índice de un documento PDF. El índice es la lista de títulos con la página donde empiezan. */
public interface IndiceDocumentoService {

    /** Cantidad máxima de entradas de un índice. */
    int MAXIMO_DE_ENTRADAS = 500;

    /**
     * Una entrada del índice.
     *
     * @param titulo El título.
     * @param pagina La página donde empieza (la primera es la 1).
     * @param nivel El nivel: 1 para un título principal, 2 a 4 para los subtítulos.
     */
    record Entrada(String titulo, int pagina, int nivel) {
    }

    /**
     * Las entradas del índice de un documento, en su orden.
     *
     * @param documento El documento.
     * @return Las entradas (vacío si no tiene índice).
     */
    List<Entrada> buscar(Documento documento);

    /**
     * Guarda el índice de un documento recién creado con el que llega como JSON. Un documento siempre tiene al menos una entrada: si el índice
     * llega vacío, la entrada es el título del documento, en el nivel 1 y la página 1.
     *
     * @param documento El documento.
     * @param json El índice completo, o {@code null} o vacío si el docente no armó ninguno.
     * @throws IllegalArgumentException Si el índice no es válido.
     */
    void crear(Documento documento, String json);

    /**
     * Reemplaza el índice de un documento por el que llega como JSON. Las oraciones del documento se borran, porque se acomodan al índice nuevo: quien
     * llama las vuelve a armar. Si el índice llega vacío queda la entrada por defecto.
     *
     * @param documento El documento.
     * @param json El índice completo, o {@code null} si no hay que tocarlo.
     * @throws IllegalArgumentException Si el índice no es válido.
     */
    void reemplazar(Documento documento, String json);
}
