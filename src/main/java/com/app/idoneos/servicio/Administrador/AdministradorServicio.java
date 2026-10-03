package com.app.idoneos.servicio.Administrador;

import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con el administrador.
 */
public interface AdministradorServicio {

    /**
     * Busca el perfil de administrador asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    Optional<Administrador> buscarPorUsuario(Usuario usuario);
}
