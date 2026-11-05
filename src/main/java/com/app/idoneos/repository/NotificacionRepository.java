package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Notificacion;
import com.app.idoneos.model.TipoNotificacion;
import com.app.idoneos.model.Usuario;

/**
 * Repositorio para acceder y gestionar la entidad {@link Notificacion} en la base de datos.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    /** Indica si ya existe una notificación de ese tipo para el usuario y la cohorte (para no avisar dos veces). */
    boolean existsByUsuarioAndCohorteAndTipoNotificacion(com.app.idoneos.model.Usuario usuario,
            com.app.idoneos.model.Cohorte cohorte, com.app.idoneos.model.TipoNotificacion tipoNotificacion);
}
