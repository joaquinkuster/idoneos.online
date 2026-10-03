package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Pregunta} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Pregunta}.
 */
@Repository
public interface PreguntaRepositorio extends JpaRepository<Pregunta, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Pregunta} activos.
     */
    List<Pregunta> findByBajaFalse();

    /**
     * Busca los registros vigentes de pregunta asociados a pool.
     *
     * @param pool el registro de Pool asociado
     * @return una lista de Pregunta asociados al Pool indicado
     */
    List<Pregunta> findByPoolAndBajaFalse(Pool pool);
}
