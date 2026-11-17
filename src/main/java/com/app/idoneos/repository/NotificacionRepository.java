package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Notificacion;
import com.app.idoneos.model.TipoNotificacion;
import com.app.idoneos.model.Usuario;

/** Repositorio de Notificacion. */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    /** Indica si ya existe una notificación de ese tipo y con ese mensaje para el usuario (para no avisar dos veces). */
    boolean existsByUsuarioAndTipoNotificacionNombreAndMensaje(com.app.idoneos.model.Usuario usuario, String tipo, String mensaje);

    /** Busca las notificaciones más recientes de un usuario. */
    java.util.List<Notificacion> findTop10ByUsuarioOrderByFechaCreacionDescIdNotificacionDesc(com.app.idoneos.model.Usuario usuario);

    /** Busca todas las notificaciones de un usuario, de la más reciente a la más antigua. */
    java.util.List<Notificacion> findByUsuarioOrderByFechaCreacionDescIdNotificacionDesc(com.app.idoneos.model.Usuario usuario);

    /** Busca las notificaciones de un usuario que todavía no leyó. */
    java.util.List<Notificacion> findByUsuarioAndFechaLecturaIsNull(com.app.idoneos.model.Usuario usuario);

    /** Cuenta las notificaciones de un usuario que todavía no leyó. */
    long countByUsuarioAndFechaLecturaIsNull(com.app.idoneos.model.Usuario usuario);

    /** Busca las notificaciones con correo pendiente de envío, o fallido con menos intentos que el máximo. */
    java.util.List<Notificacion> findByEstadoEnvioNombreAndIntentosLessThan(String estado, int intentos);

    /** Indica si ya existe una notificación de ese tipo para el usuario y la cohorte (para no avisar dos veces). */
    boolean existsByUsuarioAndCohorteAndTipoNotificacion(com.app.idoneos.model.Usuario usuario,
            com.app.idoneos.model.Cohorte cohorte, com.app.idoneos.model.TipoNotificacion tipoNotificacion);
}
