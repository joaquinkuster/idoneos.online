package com.app.idoneos.repository.modulo_clases_vivo;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoClaseEnVivoRepository extends JpaRepository<EstadoClaseEnVivo, Integer> {
    Optional<EstadoClaseEnVivo> findByNombre(String nombre);
}

