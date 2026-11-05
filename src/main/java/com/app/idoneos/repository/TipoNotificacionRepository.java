package com.app.idoneos.repository;

import com.app.idoneos.model.TipoNotificacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TipoNotificacion} en la base de datos.
 */
@Repository
public interface TipoNotificacionRepository extends JpaRepository<TipoNotificacion, Integer> {

    /**
     * Busca el registro por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<TipoNotificacion> findByNombre(String nombre);
}
