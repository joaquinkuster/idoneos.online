package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link EstadoEnvio} en la base de datos.
 */
@Repository
public interface EstadoEnvioRepositorio extends JpaRepository<EstadoEnvio, Integer> {

    /**
     * Busca el registro por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoEnvio> findByNombre(String nombre);
}
