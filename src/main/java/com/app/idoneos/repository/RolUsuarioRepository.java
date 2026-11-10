package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.RolUsuario;

/** Repositorio de RolUsuario. */
@Repository
public interface RolUsuarioRepository extends JpaRepository<RolUsuario, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<RolUsuario> findByBajaFalse();

}
