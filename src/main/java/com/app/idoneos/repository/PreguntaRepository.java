package com.app.idoneos.repository;

import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {
    List<Pregunta> findByPoolAndBajaFalse(Pool pool);
}
