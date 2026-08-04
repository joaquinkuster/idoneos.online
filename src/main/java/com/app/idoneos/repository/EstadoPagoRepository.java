package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoPagoRepository extends JpaRepository<EstadoPago, Integer> {
    Optional<EstadoPago> findByNombre(String nombre);
}
