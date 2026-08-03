package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.ClaseEnVivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaseEnVivoRepository extends JpaRepository<ClaseEnVivo, Integer> {
}
