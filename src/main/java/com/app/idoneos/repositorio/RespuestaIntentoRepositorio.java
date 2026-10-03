package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.OpcionRespuesta;
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
     * Busca los registros de respuestaIntento asociados a intentoAutoevaluacion.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @return una lista de RespuestaIntento asociados al IntentoAutoevaluacion indicado
     */
    List<RespuestaIntento> findByIntentoAutoevaluacion(IntentoAutoevaluacion intentoAutoevaluacion);

    /**
     * Busca los registros de respuestaIntento asociados a opcionRespuesta.
     *
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return una lista de RespuestaIntento asociados al OpcionRespuesta indicado
     */
    List<RespuestaIntento> findByOpcionRespuesta(OpcionRespuesta opcionRespuesta);

    /**
     * Busca la respuesta del intento a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<RespuestaIntento> findByIntentoAutoevaluacionAndOpcionRespuesta(IntentoAutoevaluacion intentoAutoevaluacion, OpcionRespuesta opcionRespuesta);
}
