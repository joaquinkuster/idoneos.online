package com.app.idoneos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Comprobante;

/** Repositorio de Comprobante. */
@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {

    /** Busca el comprobante por su atributo único 'numero'. */
    Optional<Comprobante> findByNumero(String numero);
}
