package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Notificacion} en la base de datos.
 */
@Repository
public interface NotificacionRepositorio extends JpaRepository<Notificacion, Integer> {

    /** Indica si ya existe una notificación de ese tipo para el usuario y la cohorte (para no avisar dos veces). */
    boolean existsByUsuarioAndCohorteAndTipoNotificacion(com.app.idoneos.modelo.Usuario usuario,
            com.app.idoneos.modelo.Cohorte cohorte, com.app.idoneos.modelo.TipoNotificacion tipoNotificacion);
}
