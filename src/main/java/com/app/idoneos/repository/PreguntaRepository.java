package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Pregunta;

/**
 * Repositorio para acceder y gestionar la entidad {@link Pregunta} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Pregunta}.
 */
@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Pregunta} activos.
     */
    List<Pregunta> findByBajaFalse();

}
