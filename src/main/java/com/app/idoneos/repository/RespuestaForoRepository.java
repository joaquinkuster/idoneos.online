package com.app.idoneos.repository;

import com.app.idoneos.model.RespuestaForo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespuestaForoRepository extends JpaRepository<RespuestaForo, Integer> {
}
