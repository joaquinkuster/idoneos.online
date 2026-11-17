package com.app.idoneos.service.videoclon;

import java.util.List;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TemaRecurso;

/** Servicio de los videos con clon: el docente pide un video en el que su clon dice un guion y el sistema lo sigue hasta que HeyGen lo termina. */
public interface VideoClonService {

    /**
     * Obtiene los videos con clon activos de un curso.
     *
     * @param curso el curso
     * @param docente si no es {@code null}, solo los pedidos por ese docente
     * @return los videos, con su tema, del más reciente al más antiguo
     */
    List<TemaRecurso> buscarPorCurso(Curso curso, Docente docente);

    /**
     * Pide un video con el clon del docente. Queda oculto, en estado pendiente, hasta que HeyGen lo termina.
     *
     * @param docente el docente que lo pide (debe tener su clon creado)
     * @param idTema tema al que va el video
     * @param titulo título del video
     * @param guion lo que dice el clon
     * @return el video pedido, vinculado a su tema
     * @throws IllegalArgumentException si falta algún dato, el docente no tiene clon, superó el tope semanal o HeyGen rechaza el pedido
     */
    TemaRecurso solicitar(Docente docente, Integer idTema, String titulo, String guion);

    /** Consulta a HeyGen los videos pendientes: descarga los que terminaron y marca como fallidos los que fallaron. */
    void actualizarPendientes();
}
