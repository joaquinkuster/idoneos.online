package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Pool} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Pool}.
 */
@Repository
public interface PoolRepositorio extends JpaRepository<Pool, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Pool} activos.
     */
    List<Pool> findByBajaFalse();

    /**
     * Busca los registros vigentes de pool asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Pool asociados al Unidad indicado
     */
    List<Pool> findByUnidadAndBajaFalse(Unidad unidad);
}
