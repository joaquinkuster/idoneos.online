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

    @Query("SELECT u FROM Unidad u WHERE u.programa.curso = :curso AND u.baja = false ORDER BY u.numeroOrden ASC")
    List<Unidad> findByCursoAndBajaFalseOrderByNumeroOrdenAsc(@Param("curso") Curso curso);

    List<Unidad> findByPrograma(com.app.idoneos.model.Programa programa);
}
