package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.RolUsuario;
import com.app.idoneos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link RolUsuario} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link RolUsuario}.
 */
@Repository
public interface RolUsuarioRepositorio extends JpaRepository<RolUsuario, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link RolUsuario} activos.
     */
    List<RolUsuario> findByBajaFalse();

    /**
     * Busca los registros vigentes de rolUsuario asociados a rol.
     *
     * @param rol el registro de Rol asociado
     * @return una lista de RolUsuario asociados al Rol indicado
     */
    List<RolUsuario> findByRolAndBajaFalse(Rol rol);

    /**
     * Busca los registros vigentes de rolUsuario asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de RolUsuario asociados al Usuario indicado
     */
    List<RolUsuario> findByUsuarioAndBajaFalse(Usuario usuario);

    /**
     * Busca la asignación de rol a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param rol el registro de Rol asociado
     * @param usuario el registro de Usuario asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<RolUsuario> findByRolAndUsuario(Rol rol, Usuario usuario);
}
