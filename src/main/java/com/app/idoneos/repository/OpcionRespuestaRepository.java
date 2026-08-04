package com.app.idoneos.repository;

import com.app.idoneos.model.OpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpcionRespuestaRepository extends JpaRepository<OpcionRespuesta, Integer> {
}
