package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.EstadoClaseEnVivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoClaseEnVivoRepository extends JpaRepository<EstadoClaseEnVivo, Integer> {
}
