package com.app.idoneos.repository;

import com.app.idoneos.model.ClaseClonIA;
import com.app.idoneos.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaseClonIARepository extends JpaRepository<ClaseClonIA, Integer> {
    List<ClaseClonIA> findByDocenteAndBajaFalse(Docente docente);
}
