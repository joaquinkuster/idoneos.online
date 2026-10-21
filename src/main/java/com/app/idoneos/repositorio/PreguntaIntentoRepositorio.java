package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.modelo.PreguntaIntento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link PreguntaIntento} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link PreguntaIntento}.
 */
@Repository
public interface PreguntaIntentoRepositorio extends JpaRepository<PreguntaIntento, Integer> {

    /**
     * Busca los registros de preguntaIntento asociados a intentoAutoevaluacion.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @return una lista de PreguntaIntento asociados al IntentoAutoevaluacion indicado
     */
    List<PreguntaIntento> findByIntentoAutoevaluacion(IntentoAutoevaluacion intentoAutoevaluacion);

    /**
     * Busca los registros de preguntaIntento asociados a pregunta.
     *
     * @param pregunta el registro de Pregunta asociado
     * @return una lista de PreguntaIntento asociados al Pregunta indicado
     */
    List<PreguntaIntento> findByPregunta(Pregunta pregunta);

    /**
     * Busca la pregunta del intento a partir de la combinación única de sus relaciones.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @param pregunta el registro de Pregunta asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<PreguntaIntento> findByIntentoAutoevaluacionAndPregunta(IntentoAutoevaluacion intentoAutoevaluacion, Pregunta pregunta);
}
