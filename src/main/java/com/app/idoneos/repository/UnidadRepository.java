package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Integer> {

    List<Unidad> findByCursoAndBajaFalseOrderByNumeroOrdenAsc(Curso curso);
}
