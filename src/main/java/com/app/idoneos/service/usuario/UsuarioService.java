package com.app.idoneos.service.usuario;

import com.app.idoneos.service.CrudService;

import java.util.Optional;

import com.app.idoneos.model.Rol;
import com.app.idoneos.model.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con el usuario.
 */
public interface UsuarioService extends CrudService<Usuario> {

    // ---------------------------------------------------------------- Lecturas


    /**
     * Busca el usuario por su atributo único 'correo'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param correo el valor de 'correo' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> buscarPorCorreo(String correo);




    // ---------------------------------------------------------------- Modificaciones

    /**
     * Cambia el rol por defecto de un usuario. El rol debe estar vigente entre los roles del usuario.
     *
     * @param idUsuario Identificador del usuario.
     * @param idRol     Identificador del rol que pasa a ser el rol por defecto.
     * @return El rol establecido como rol por defecto.
     * @throws IllegalArgumentException Si el usuario no existe o no tiene ese rol vigente.
     */
    Rol cambiarRolPorDefecto(int idUsuario, Integer idRol);
}
