package com.app.idoneos.repository;

import com.app.idoneos.model.TipoNotificacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de TipoNotificacion. */
@Repository
public interface TipoNotificacionRepository extends JpaRepository<TipoNotificacion, Integer> {

    /** Busca el registro por su atributo único 'nombre'. */
    Optional<TipoNotificacion> findByNombre(String nombre);
}
