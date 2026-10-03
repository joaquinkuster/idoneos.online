package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Administrador} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Administrador}.
 */
@Repository
public interface AdministradorRepositorio extends JpaRepository<Administrador, Integer> {

    /**
     * Busca el perfil de administrador asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    Optional<Administrador> findByUsuario(Usuario usuario);
}
