package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Docente} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Docente}.
 */
@Repository
public interface DocenteRepositorio extends JpaRepository<Docente, Integer> {

    /**
     * Busca el perfil de docente asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    Optional<Docente> findByUsuario(Usuario usuario);

    /**
     * Busca el docente por su atributo único 'avatarId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param avatarId el valor de 'avatarId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Docente> findByAvatarId(String avatarId);

    /**
     * Busca el docente por su atributo único 'matriculaCnv'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param matriculaCnv el valor de 'matriculaCnv' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Docente> findByMatriculaCnv(String matriculaCnv);

    /**
     * Busca el docente por su atributo único 'voiceId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param voiceId el valor de 'voiceId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Docente> findByVoiceId(String voiceId);
}
