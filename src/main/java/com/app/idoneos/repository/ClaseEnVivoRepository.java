package com.app.idoneos.repository;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaseEnVivoRepository extends JpaRepository<ClaseEnVivo, Integer> {
    List<ClaseEnVivo> findByDocenteAndBajaFalseOrderByFechaHoraDesc(Docente docente);
}
