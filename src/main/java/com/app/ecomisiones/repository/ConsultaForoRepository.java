package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.ConsultaForo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaForoRepository extends JpaRepository<ConsultaForo, Integer> {
}
