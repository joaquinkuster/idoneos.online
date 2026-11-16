package com.app.idoneos.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Usuario;

/** Repositorio de Usuario. */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Usuario> findByBajaFalse();

    /** Busca el usuario por su atributo único 'correo'. */
    Optional<Usuario> findByCorreo(String correo);

    /** Verifica si existe un usuario con el DNI dado. */
    boolean existsByDni(String dni);

    /** Verifica si existe otro usuario, distinto del indicado, con el DNI dado. */
    boolean existsByDniAndIdUsuarioNot(String dni, int idUsuario);

    /** Busca el usuario por su atributo único 'tokenVerificacion'. */
    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);

}
