package com.app.idoneos.repository;

import com.app.idoneos.model.Sesion;
import com.app.idoneos.model.Usuario;
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
