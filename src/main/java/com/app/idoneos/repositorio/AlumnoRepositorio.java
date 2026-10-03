package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Alumno} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Alumno}.
 */
@Repository
public interface AlumnoRepositorio extends JpaRepository<Alumno, Integer> {

    /**
     * Busca el perfil de alumno asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    Optional<Alumno> findByUsuario(Usuario usuario);
}
