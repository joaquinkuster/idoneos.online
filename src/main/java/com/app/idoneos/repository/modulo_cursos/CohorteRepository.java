package com.app.idoneos.repository.modulo_cursos;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CohorteRepository extends JpaRepository<Cohorte, Integer> {
    List<Cohorte> findByPrograma(Programa programa);
    List<Cohorte> findByProgramaAndBajaFalse(Programa programa);
    List<Cohorte> findByBajaFalse();
}

