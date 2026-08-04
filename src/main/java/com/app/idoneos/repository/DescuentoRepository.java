package com.app.idoneos.repository;

import com.app.idoneos.model.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Integer> {
}
