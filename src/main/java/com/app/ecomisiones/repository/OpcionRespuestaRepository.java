package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Integer> {
}
