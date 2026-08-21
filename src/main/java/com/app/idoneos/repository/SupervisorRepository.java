package com.app.idoneos.repository;

import com.app.idoneos.model.Supervisor;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupervisorRepository extends JpaRepository<Supervisor, Integer> {
    List<Supervisor> findByCurso(Curso curso);
    List<Supervisor> findByDocente(Docente docente);
}
