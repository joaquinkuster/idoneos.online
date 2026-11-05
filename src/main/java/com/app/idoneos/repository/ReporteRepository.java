package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Reporte;

/**
 * Repositorio para acceder y gestionar la entidad {@link Reporte} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Reporte}.
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {



    /**
     * Busca los registros de reporte asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Reporte asociados al Curso indicado
     */
    List<Reporte> findByCurso(Curso curso);
}
