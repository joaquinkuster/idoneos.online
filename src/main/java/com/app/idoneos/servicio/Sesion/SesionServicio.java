package com.app.idoneos.servicio.Sesion;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Sesion;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con la sesión.
 */
public interface SesionServicio {

    /**
     * Busca los registros de sesion asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de Sesion asociados al Usuario indicado
     */
    List<Sesion> buscarPorUsuario(Usuario usuario);

    /**
     * Busca la sesión por su atributo único 'token'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param token el valor de 'token' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Sesion> buscarPorToken(String token);
}
