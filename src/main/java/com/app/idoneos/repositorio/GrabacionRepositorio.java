package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Grabacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Grabacion} en la base de datos.
 */
@Repository
public interface GrabacionRepositorio extends JpaRepository<Grabacion, Integer> {
}
