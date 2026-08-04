package com.app.idoneos.repository;

import com.app.idoneos.model.RespuestaIntento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespuestaIntentoRepository extends JpaRepository<RespuestaIntento, Integer> {
}
