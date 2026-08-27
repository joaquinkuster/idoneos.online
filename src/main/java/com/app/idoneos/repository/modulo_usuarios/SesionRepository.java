package com.app.idoneos.repository.modulo_usuarios;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {

    /** Sesiones activas (sin fecha de fin) de un usuario — CU-79. */
    List<Sesion> findByUsuarioAndFechaFinIsNull(Usuario usuario);

    /** Todas las sesiones de un usuario — CU-79 (Admin). */
    List<Sesion> findByUsuarioOrderByFechaInicioDesc(Usuario usuario);
}

