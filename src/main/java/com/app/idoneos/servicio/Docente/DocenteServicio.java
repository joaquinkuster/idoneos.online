package com.app.idoneos.servicio.Docente;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio para gestionar las operaciones relacionadas con el docente.
 */
public interface DocenteServicio {

    /**
     * Busca el perfil de docente asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    Optional<Docente> buscarPorUsuario(Usuario usuario);

    /**
     * Busca el docente por su atributo único 'avatarId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param avatarId el valor de 'avatarId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Docente> buscarPorAvatarId(String avatarId);

    /**
     * Busca el docente por su atributo único 'matriculaCnv'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param matriculaCnv el valor de 'matriculaCnv' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Docente> buscarPorMatriculaCnv(String matriculaCnv);

    /**
     * Busca el docente por su atributo único 'voiceId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param voiceId el valor de 'voiceId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Docente> buscarPorVoiceId(String voiceId);

    /**
     * Busca los docentes activos y habilitados, que pueden participar en cursos.
     *
     * @return una lista de docentes habilitados
     */
    List<Docente> buscarHabilitados();
}
