package com.app.idoneos.servicio.Usuario;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con el usuario.
 */
public interface UsuarioServicio {

    /**
     * Busca los registros vigentes de usuario asociados a rol.
     *
     * @param rolPorDefecto el registro de Rol asociado
     * @return una lista de Usuario asociados al Rol indicado
     */
    List<Usuario> buscarPorRolPorDefecto(Rol rolPorDefecto);

    /**
     * Busca el usuario por su atributo único 'correo'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param correo el valor de 'correo' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> buscarPorCorreo(String correo);

    /**
     * Busca el usuario por su atributo único 'dni'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param dni el valor de 'dni' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> buscarPorDni(String dni);

    /**
     * Busca el usuario por su atributo único 'googleId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param googleId el valor de 'googleId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> buscarPorGoogleId(String googleId);

    /**
     * Busca el usuario por su atributo único 'tokenVerificacion'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param tokenVerificacion el valor de 'tokenVerificacion' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> buscarPorTokenVerificacion(String tokenVerificacion);

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
