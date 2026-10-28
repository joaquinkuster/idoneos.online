package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Tema} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Tema}.
 */
@Repository
public interface TemaRepositorio extends JpaRepository<Tema, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Tema} activos.
     */
    List<Tema> findByBajaFalse();

    /**
     * Busca los registros vigentes de tema asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Tema asociados al Curso indicado
     */
    List<Tema> findByCursoAndBajaFalse(Curso curso);
}
