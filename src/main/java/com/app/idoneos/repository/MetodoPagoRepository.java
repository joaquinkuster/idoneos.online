package com.app.idoneos.repository;

import com.app.idoneos.model.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    Optional<MetodoPago> findByNombre(String nombre);
}
