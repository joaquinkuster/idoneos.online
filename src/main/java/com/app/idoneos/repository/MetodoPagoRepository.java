package com.app.idoneos.repository;

import com.app.idoneos.model.MetodoPago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de MetodoPago. */
@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

    /** Busca el método de pago por su atributo único 'nombre'. */
    Optional<MetodoPago> findByNombre(String nombre);
}
