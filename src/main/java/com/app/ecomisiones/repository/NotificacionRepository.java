package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Notificacion;
import com.app.ecomisiones.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuarioOrderByFechaDesc(Usuario usuario);
}
