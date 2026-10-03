package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Programa} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Programa}.
 */
@Repository
public interface ProgramaRepositorio extends JpaRepository<Programa, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Programa} activos.
     */
    List<Programa> findByBajaFalse();

    /**
     * Busca los registros vigentes de programa asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Programa asociados al Curso indicado
     */
    List<Programa> findByCursoAndBajaFalse(Curso curso);
}
