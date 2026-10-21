package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.PreguntaIntento;
import com.app.idoneos.modelo.RespuestaIntento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link RespuestaIntento} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link RespuestaIntento}.
 */
@Repository
public interface RespuestaIntentoRepositorio extends JpaRepository<RespuestaIntento, Integer> {

    /**
     * Busca los registros de respuestaIntento asociados a preguntaIntento.
     *
     * @param preguntaIntento el registro de PreguntaIntento asociado
     * @return una lista de RespuestaIntento asociados al PreguntaIntento indicado
     */
    List<RespuestaIntento> findByPreguntaIntento(PreguntaIntento preguntaIntento);

    /**
     * Busca los registros de respuestaIntento asociados a opcionRespuesta.
     *
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return una lista de RespuestaIntento asociados al OpcionRespuesta indicado
     */
    List<RespuestaIntento> findByOpcionRespuesta(OpcionRespuesta opcionRespuesta);

    /**
     * Busca la respuesta del intento a partir de la combinación única de sus relaciones.
     *
     * @param preguntaIntento el registro de PreguntaIntento asociado
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<RespuestaIntento> findByPreguntaIntentoAndOpcionRespuesta(PreguntaIntento preguntaIntento, OpcionRespuesta opcionRespuesta);
}
