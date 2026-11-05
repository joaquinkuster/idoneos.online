package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.RespuestaForo;

/**
 * Repositorio para acceder y gestionar la entidad {@link RespuestaForo} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link RespuestaForo}.
 */
@Repository
public interface RespuestaForoRepository extends JpaRepository<RespuestaForo, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link RespuestaForo} activos.
     */
    List<RespuestaForo> findByBajaFalse();


}
