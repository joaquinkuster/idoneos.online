package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.CursoModalidad;

/**
 * Repositorio para acceder y gestionar la entidad {@link CursoModalidad} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link CursoModalidad}.
 */
@Repository
public interface CursoModalidadRepository extends JpaRepository<CursoModalidad, Integer> {

    /**
     * Busca los registros de cursoModalidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de CursoModalidad asociados al Curso indicado
     */
    List<CursoModalidad> findByCurso(Curso curso);


}
