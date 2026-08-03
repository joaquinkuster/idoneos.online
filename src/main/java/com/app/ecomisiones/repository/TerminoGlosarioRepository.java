package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.TerminoGlosario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminoGlosarioRepository extends JpaRepository<TerminoGlosario, Integer> {
}
