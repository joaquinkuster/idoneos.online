package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.AutoevaluacionPool;
import com.app.idoneos.modelo.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link AutoevaluacionPool} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link AutoevaluacionPool}.
 */
@Repository
public interface AutoevaluacionPoolRepositorio extends JpaRepository<AutoevaluacionPool, Integer> {

    /**
     * Busca los registros de autoevaluacionPool asociados a pool.
     *
     * @param pool el registro de Pool asociado
     * @return una lista de AutoevaluacionPool asociados al Pool indicado
     */
    List<AutoevaluacionPool> findByPool(Pool pool);

    /**
     * Busca los registros de autoevaluacionPool asociados a autoevaluacion.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @return una lista de AutoevaluacionPool asociados al Autoevaluacion indicado
     */
    List<AutoevaluacionPool> findByAutoevaluacion(Autoevaluacion autoevaluacion);

    /**
     * Busca la relación entre autoevaluación y pool a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @param pool el registro de Pool asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<AutoevaluacionPool> findByAutoevaluacionAndPool(Autoevaluacion autoevaluacion, Pool pool);
}
