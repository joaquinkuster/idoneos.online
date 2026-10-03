package com.app.idoneos.servicio.RolUsuario;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.RolUsuario;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con la asignación de rol.
 */
public interface RolUsuarioServicio {

    /**
     * Busca los registros vigentes de rolUsuario asociados a rol.
     *
     * @param rol el registro de Rol asociado
     * @return una lista de RolUsuario asociados al Rol indicado
     */
    List<RolUsuario> buscarPorRol(Rol rol);

    /**
     * Busca los registros vigentes de rolUsuario asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de RolUsuario asociados al Usuario indicado
     */
    List<RolUsuario> buscarPorUsuario(Usuario usuario);

    /**
     * Busca la asignación de rol a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param rol el registro de Rol asociado
     * @param usuario el registro de Usuario asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<RolUsuario> buscarPorRolYUsuario(Rol rol, Usuario usuario);
}
