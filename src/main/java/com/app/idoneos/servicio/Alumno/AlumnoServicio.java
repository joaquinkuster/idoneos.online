package com.app.idoneos.servicio.Alumno;

import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con el alumno.
 */
public interface AlumnoServicio {

    /**
     * Busca el perfil de alumno asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    Optional<Alumno> buscarPorUsuario(Usuario usuario);
}
