package com.app.idoneos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Comprobante;

/**
 * Repositorio para acceder y gestionar la entidad {@link Comprobante} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Comprobante}.
 */
@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {


    /**
     * Busca el comprobante por su atributo único 'numero'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param numero el valor de 'numero' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Comprobante> findByNumero(String numero);
}
