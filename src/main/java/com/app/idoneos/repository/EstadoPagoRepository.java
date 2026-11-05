package com.app.idoneos.repository;

import com.app.idoneos.model.EstadoPago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link EstadoPago} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link EstadoPago}.
 */
@Repository
public interface EstadoPagoRepository extends JpaRepository<EstadoPago, Integer> {

    /**
     * Busca el estado del pago por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoPago> findByNombre(String nombre);
}
