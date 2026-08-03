package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.ConsultaForo;
import com.app.ecomisiones.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaForoRepository extends JpaRepository<ConsultaForo, Integer> {
    List<ConsultaForo> findByUnidadAndBajaFalseOrderByFechaDesc(Unidad unidad);
}
