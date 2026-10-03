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
     * Busca el usuario por su atributo único 'dni'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param dni el valor de 'dni' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> buscarPorDni(String dni);

    /**
     * Busca el usuario por su atributo único 'email'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param email el valor de 'email' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> buscarPorEmail(String email);

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
}
