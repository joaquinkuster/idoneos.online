package com.app.idoneos.repository;

import com.app.idoneos.model.Notificacion;
import com.app.idoneos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuarioOrderByFechaDesc(Usuario usuario);
}
