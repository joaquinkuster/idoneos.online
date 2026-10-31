package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Recurso} en la base de datos.
 */
@Repository
public interface RecursoRepositorio extends JpaRepository<Recurso, Integer> {

    /**
     * Busca los recursos activos de un docente.
     *
     * @param docente el docente dueño
     * @return los recursos activos
     */
    List<Recurso> findByDocenteAndBajaFalse(com.app.idoneos.modelo.Docente docente);
}
