package com.app.idoneos.repository.modulo_reportes;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {
    List<Reporte> findByAdministradorOrderByFechaGeneracionDesc(Administrador administrador);
    List<Reporte> findByCursoOrderByFechaGeneracionDesc(Curso curso);
    List<Reporte> findAllByOrderByFechaGeneracionDesc();
}
