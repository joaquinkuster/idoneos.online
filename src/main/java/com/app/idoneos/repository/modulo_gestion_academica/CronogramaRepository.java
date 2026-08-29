package com.app.idoneos.repository.modulo_gestion_academica;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CronogramaRepository extends JpaRepository<Cronograma, Integer> {
    List<Cronograma> findByProgramaOrderByNumeroOrden(Programa programa);
    List<Cronograma> findByProgramaOrderByNumeroOrdenAsc(Programa programa);
    Optional<Cronograma> findByProgramaAndUnidad(Programa programa, Unidad unidad);
    boolean existsByProgramaAndUnidad(Programa programa, Unidad unidad);
    int countByPrograma(Programa programa);
    List<Cronograma> findByUnidad(Unidad unidad);
}
