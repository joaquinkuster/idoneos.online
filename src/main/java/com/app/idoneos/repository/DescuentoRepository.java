package com.app.idoneos.repository;

import com.app.idoneos.model.Descuento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Descuento. */
@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Descuento> findByBajaFalse();

    /** Busca el descuento por su atributo único 'nombre'. */
    Optional<Descuento> findByNombre(String nombre);
}
