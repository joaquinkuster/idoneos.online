package com.app.idoneos.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.RolUsuario;

/** Repositorio de RolUsuario. */
@Repository
public interface RolUsuarioRepository extends JpaRepository<RolUsuario, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<RolUsuario> findByBajaFalse();

    /** Cuenta los usuarios activos con el rol indicado, sin contar al usuario dado. */
    @Query("select count(ru) from RolUsuario ru where ru.rol.nombre = :rol and ru.baja = false "
            + "and ru.usuario.baja = false and ru.usuario.idUsuario <> :idUsuario")
    long contarOtrosUsuariosActivosConRol(@Param("rol") String rol, @Param("idUsuario") int idUsuario);

}
