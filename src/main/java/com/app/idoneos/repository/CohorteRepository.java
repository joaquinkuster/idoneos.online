package com.app.idoneos.repository;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CohorteRepository extends JpaRepository<Cohorte, Integer> {
    List<Cohorte> findByPrograma(Programa programa);
    List<Cohorte> findByProgramaAndBajaFalse(Programa programa);
    List<Cohorte> findByBajaFalse();
}
