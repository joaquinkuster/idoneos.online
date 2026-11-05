package com.app.idoneos.repository;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TituloDocente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TituloDocente} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link TituloDocente}.
 */
@Repository
public interface TituloDocenteRepository extends JpaRepository<TituloDocente, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link TituloDocente} activos.
     */
    List<TituloDocente> findByBajaFalse();

    /**
     * Busca los registros vigentes de tituloDocente asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de TituloDocente asociados al Docente indicado
     */
    List<TituloDocente> findByDocenteAndBajaFalse(Docente docente);
}
