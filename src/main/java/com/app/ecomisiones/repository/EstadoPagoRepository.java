package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoPagoRepository extends JpaRepository<EstadoPago, Integer> {
}
