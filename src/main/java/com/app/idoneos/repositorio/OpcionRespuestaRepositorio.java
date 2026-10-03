package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link OpcionRespuesta} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link OpcionRespuesta}.
 */
@Repository
public interface OpcionRespuestaRepositorio extends JpaRepository<OpcionRespuesta, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link OpcionRespuesta} activos.
     */
    List<OpcionRespuesta> findByBajaFalse();

    /**
     * Busca los registros vigentes de opcionRespuesta asociados a pregunta.
     *
     * @param pregunta el registro de Pregunta asociado
     * @return una lista de OpcionRespuesta asociados al Pregunta indicado
     */
    List<OpcionRespuesta> findByPreguntaAndBajaFalse(Pregunta pregunta);
}
