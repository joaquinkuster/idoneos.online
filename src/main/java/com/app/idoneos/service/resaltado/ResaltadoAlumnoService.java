package com.app.idoneos.service.resaltado;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Recurso;
import java.util.List;

/** Servicio de las anotaciones del alumno sobre los documentos PDF. Son de lápiz y de marcador. */
public interface ResaltadoAlumnoService {

    /**
     * Los trazos de una página.
     *
     * @param pagina La página (la primera es la 1).
     * @param trazos Los trazos, como un JSON.
     */
    record Pagina(int pagina, String trazos) {
    }

    /**
     * Las páginas que el alumno anotó en un documento.
     *
     * @param alumno El alumno.
     * @param recurso El documento.
     * @return Las páginas con anotaciones, en orden.
     */
    List<Pagina> buscar(Alumno alumno, Recurso recurso);

    /**
     * Guarda los trazos de una página, reemplazando los anteriores.
     *
     * @param alumno El alumno.
     * @param recurso El documento.
     * @param pagina La página (la primera es la 1).
     * @param trazos Los trazos, como un JSON.
     * @throws IllegalArgumentException Si la página o los trazos no son válidos.
     */
    void guardar(Alumno alumno, Recurso recurso, int pagina, String trazos);
}
