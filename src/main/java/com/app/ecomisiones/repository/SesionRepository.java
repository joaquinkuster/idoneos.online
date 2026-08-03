package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {
}
