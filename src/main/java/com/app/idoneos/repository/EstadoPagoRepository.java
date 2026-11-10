package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoPago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de EstadoPago. */
@Repository
public interface EstadoPagoRepository extends JpaRepository<EstadoPago, Integer> {

    /** Busca el estado del pago por su atributo único 'nombre'. */
    Optional<EstadoPago> findByNombre(String nombre);
}
