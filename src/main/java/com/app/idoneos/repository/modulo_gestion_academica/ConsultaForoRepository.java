package com.app.idoneos.repository.modulo_gestion_academica;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaForoRepository extends JpaRepository<ConsultaForo, Integer> {
    List<ConsultaForo> findByUnidadAndBajaFalseOrderByFechaDesc(Unidad unidad);
    List<ConsultaForo> findByUnidad(Unidad unidad);
}

