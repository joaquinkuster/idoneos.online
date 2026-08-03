package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.RespuestaForo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespuestaForoRepository extends JpaRepository<RespuestaForo, Integer> {
}
