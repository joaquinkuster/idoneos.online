package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.ClaseEnVivo;
import com.app.ecomisiones.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaseEnVivoRepository extends JpaRepository<ClaseEnVivo, Integer> {
    List<ClaseEnVivo> findByDocenteAndBajaFalseOrderByFechaHoraDesc(Docente docente);
}
