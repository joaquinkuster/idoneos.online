package com.app.idoneos.service.respuestaforo;

import com.app.idoneos.service.CrudService;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.RespuestaForo;
import java.util.Optional;

/** Servicio de la respuesta del foro. */
public interface RespuestaForoService extends CrudService<RespuestaForo> {

    /**
     * Busca una respuesta vigente por su identificador.
     *
     * @param id el identificador
     * @return el registro, o vacío si no existe o está dado de baja
     */
    Optional<RespuestaForo> buscarPorId(Integer id);

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Registra la respuesta de un docente a una consulta.
     *
     * @param docente el docente que responde
     * @param consultaId el identificador de la consulta
     * @param texto el texto de la respuesta
     * @return la respuesta registrada
     * @throws IllegalArgumentException si falta el texto, supera el máximo, la consulta no está activa o el docente no participa en el curso
     */
    RespuestaForo registrar(Docente docente, Integer consultaId, String texto);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Modifica el texto de una respuesta propia, dentro del plazo de edición.
     *
     * @param id el identificador de la respuesta
     * @param docente el docente que modifica
     * @param texto el texto nuevo
     * @return la respuesta modificada
     * @throws IllegalArgumentException si no es suya, no está activa, pasó el plazo o el texto no es válido
     */
    RespuestaForo modificar(Integer id, Docente docente, String texto);

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Da de baja una respuesta (baja lógica).
     *
     * @param id el identificador de la respuesta
     * @throws IllegalArgumentException si la respuesta no está activa
     */
    void darDeBaja(Integer id);
}
