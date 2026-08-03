package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Pool;
import com.app.ecomisiones.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {
    List<Pregunta> findByPoolAndBajaFalse(Pool pool);
}
