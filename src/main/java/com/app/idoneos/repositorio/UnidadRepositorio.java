package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Unidad} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Unidad}.
 */
@Repository
public interface UnidadRepositorio extends JpaRepository<Unidad, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Unidad} activos.
     */
    List<Unidad> findByBajaFalse();

    /**
     * Busca los registros vigentes de unidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Unidad asociados al Curso indicado
     */
    List<Unidad> findByCursoAndBajaFalse(Curso curso);
}
