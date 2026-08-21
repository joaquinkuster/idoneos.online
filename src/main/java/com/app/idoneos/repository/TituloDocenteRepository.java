package com.app.idoneos.repository;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TituloDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TituloDocenteRepository extends JpaRepository<TituloDocente, Integer> {
    List<TituloDocente> findByDocente(Docente docente);
}
