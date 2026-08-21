package com.app.idoneos.repository;

import com.app.idoneos.model.Cronograma;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CronogramaRepository extends JpaRepository<Cronograma, Integer> {
    List<Cronograma> findByProgramaOrderByNumeroOrden(Programa programa);
    List<Cronograma> findByUnidad(Unidad unidad);
}
