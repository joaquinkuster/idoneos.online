package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Reporte;

/** Repositorio de Reporte. */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    /** Busca los registros de reporte asociados a curso. */
    List<Reporte> findByCurso(Curso curso);
}
