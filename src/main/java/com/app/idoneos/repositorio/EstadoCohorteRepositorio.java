package com.app.idoneos.repositorio;

import java.util.Optional;
import com.app.idoneos.modelo.EstadoCohorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link EstadoCohorte} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link EstadoCohorte}.
 */
@Repository
public interface EstadoCohorteRepositorio extends JpaRepository<EstadoCohorte, Integer> {

    /**
     * Busca el estado de la cohorte por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<EstadoCohorte> findByNombre(String nombre);
}
