package com.app.idoneos.repository;

import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaForoRepository extends JpaRepository<ConsultaForo, Integer> {
    List<ConsultaForo> findByUnidadAndBajaFalseOrderByFechaDesc(Unidad unidad);
    List<ConsultaForo> findByUnidad(Unidad unidad);
}
