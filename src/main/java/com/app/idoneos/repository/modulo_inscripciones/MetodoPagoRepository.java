package com.app.idoneos.repository.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    Optional<MetodoPago> findByNombre(String nombre);

    @Query("SELECT m FROM MetodoPago m WHERE LOWER(m.nombre) = LOWER(:nombre)")
    Optional<MetodoPago> findByNombreIgnoreCase(@Param("nombre") String nombre);
}
