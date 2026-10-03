package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Sesion;
import com.app.idoneos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Sesion} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Sesion}.
 */
@Repository
public interface SesionRepositorio extends JpaRepository<Sesion, Integer> {

    /**
     * Busca los registros de sesion asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de Sesion asociados al Usuario indicado
     */
    List<Sesion> findByUsuario(Usuario usuario);

    /**
     * Busca la sesión por su atributo único 'token'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param token el valor de 'token' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Sesion> findByToken(String token);
}
