package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.EstadoClaseClonIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoClaseClonIARepository extends JpaRepository<EstadoClaseClonIA, Integer> {
}
