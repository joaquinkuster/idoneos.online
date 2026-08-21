package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Integer> {

    @Query("SELECT DISTINCT c.unidad FROM Cronograma c WHERE c.programa.curso = :curso AND c.unidad.baja = false ORDER BY c.numeroOrden ASC")
    List<Unidad> findByCursoAndBajaFalseOrderByNumeroOrdenAsc(@Param("curso") Curso curso);

    @Query("SELECT DISTINCT c.unidad FROM Cronograma c WHERE c.programa = :programa AND c.unidad.baja = false ORDER BY c.numeroOrden ASC")
    List<Unidad> findByPrograma(@Param("programa") com.app.idoneos.model.Programa programa);
}
