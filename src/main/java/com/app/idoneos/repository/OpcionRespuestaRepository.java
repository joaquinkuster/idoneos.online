package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.OpcionRespuesta;

/**
 * Repositorio para acceder y gestionar la entidad {@link OpcionRespuesta} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link OpcionRespuesta}.
 */
@Repository
public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link OpcionRespuesta} activos.
     */
    List<OpcionRespuesta> findByBajaFalse();

}
