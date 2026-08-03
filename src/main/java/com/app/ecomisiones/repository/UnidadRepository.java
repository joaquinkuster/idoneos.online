package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Integer> {

    List<Unidad> findByCursoAndBajaFalseOrderByNumeroOrdenAsc(Curso curso);
}
