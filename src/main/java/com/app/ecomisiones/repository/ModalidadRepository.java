package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Modalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModalidadRepository extends JpaRepository<Modalidad, Integer> {
}
