package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Recurso;

/**
 * Repositorio para acceder y gestionar la entidad {@link Recurso} en la base de datos.
 */
@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Integer> {

    /**
     * Busca los recursos activos de un docente.
     *
     * @param docente el docente dueño
     * @return los recursos activos
     */
    List<Recurso> findByDocenteAndBajaFalse(com.app.idoneos.model.Docente docente);

    /**
     * Busca los recursos públicos activos: los que cualquier docente puede ver desde la biblioteca.
     *
     * @return los recursos públicos.
     */
    List<Recurso> findByPrivadoFalseAndBajaFalse();

    /**
     * Busca todos los recursos activos, públicos y privados (lo que ve el administrador).
     *
     * @return los recursos activos.
     */
    List<Recurso> findByBajaFalse();
}
