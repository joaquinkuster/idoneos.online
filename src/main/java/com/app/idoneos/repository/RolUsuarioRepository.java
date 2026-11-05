package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.RolUsuario;

/**
 * Repositorio para acceder y gestionar la entidad {@link RolUsuario} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link RolUsuario}.
 */
@Repository
public interface RolUsuarioRepository extends JpaRepository<RolUsuario, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link RolUsuario} activos.
     */
    List<RolUsuario> findByBajaFalse();



}
