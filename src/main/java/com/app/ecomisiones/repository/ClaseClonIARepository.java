package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.ClaseClonIA;
import com.app.ecomisiones.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaseClonIARepository extends JpaRepository<ClaseClonIA, Integer> {
    List<ClaseClonIA> findByDocenteAndBajaFalse(Docente docente);
}
