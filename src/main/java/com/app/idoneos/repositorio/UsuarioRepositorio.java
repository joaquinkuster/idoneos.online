package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Usuario} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Usuario}.
 */
@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Usuario} activos.
     */
    List<Usuario> findByBajaFalse();

    /**
     * Busca los registros vigentes de usuario asociados a rol.
     *
     * @param rolPorDefecto el registro de Rol asociado
     * @return una lista de Usuario asociados al Rol indicado
     */
    List<Usuario> findByRolPorDefectoAndBajaFalse(Rol rolPorDefecto);

    /**
     * Busca el usuario por su atributo único 'correo'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param correo el valor de 'correo' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Busca el usuario por su atributo único 'dni'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param dni el valor de 'dni' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> findByDni(String dni);

    /**
     * Busca el usuario por su atributo único 'googleId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param googleId el valor de 'googleId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> findByGoogleId(String googleId);

    /**
     * Busca el usuario por su atributo único 'tokenVerificacion'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param tokenVerificacion el valor de 'tokenVerificacion' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> findByTokenVerificacion(String tokenVerificacion);
}
